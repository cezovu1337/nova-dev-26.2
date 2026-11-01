package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.player.PlayerItemsLayer;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderState;
import org.joml.Quaternionf;

public class EntityRenderDispatcher implements ResourceManagerReloadListener, EntityRendererManager {
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers = ImmutableMap.of();
    private Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> playerRenderers = Map.of();
    private Map<PlayerModelType, AvatarRenderer<ClientMannequin>> mannequinRenderers = Map.of();
    public final TextureManager textureManager;
    public Camera camera;
    public Camera info;
    public Entity crosshairPickEntity;
    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;
    private final MapRenderer mapRenderer;
    private final ItemInHandRenderer itemInHandRenderer;
    private final AtlasManager atlasManager;
    private final Font font;
    public final Options options;
    private final Supplier<EntityModelSet> entityModels;
    private final EquipmentAssetManager equipmentAssets;
    private final PlayerSkinRenderCache playerSkinRenderCache;
    private EntityRendererProvider.Context context = null;

    public <E extends Entity> int getPackedLightCoords(E entity, float partialTickTime) {
        return this.getRenderer(entity).getPackedLightCoords(entity, partialTickTime);
    }

    public EntityRenderDispatcher(
        Minecraft minecraft,
        TextureManager textureManager,
        BlockModelResolver blockModelResolver,
        ItemModelResolver itemModelResolver,
        MapRenderer mapRenderer,
        AtlasManager atlasManager,
        Font font,
        Options options,
        Supplier<EntityModelSet> entityModels,
        EquipmentAssetManager equipmentAssets,
        PlayerSkinRenderCache playerSkinRenderCache
    ) {
        this.textureManager = textureManager;
        this.blockModelResolver = blockModelResolver;
        this.itemModelResolver = itemModelResolver;
        this.mapRenderer = mapRenderer;
        this.atlasManager = atlasManager;
        this.playerSkinRenderCache = playerSkinRenderCache;
        this.itemInHandRenderer = new ItemInHandRenderer(minecraft, this, itemModelResolver);
        this.font = font;
        this.options = options;
        this.entityModels = entityModels;
        this.equipmentAssets = equipmentAssets;
    }

    public <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity) {
        return (EntityRenderer<? super T, ?>)(switch (entity) {
            case AbstractClientPlayer player -> this.getAvatarRenderer(this.playerRenderers, player);
            case ClientMannequin mannequin -> this.getAvatarRenderer(this.mannequinRenderers, mannequin);
            default -> (EntityRenderer)this.renderers.get(entity.getType());
        });
    }

    public AvatarRenderer<AbstractClientPlayer> getPlayerRenderer(AbstractClientPlayer player) {
        return this.getAvatarRenderer(this.playerRenderers, player);
    }

    private <T extends Avatar & ClientAvatarEntity> AvatarRenderer<T> getAvatarRenderer(Map<PlayerModelType, AvatarRenderer<T>> renderers, T entity) {
        PlayerModelType model = entity.getSkin().model();
        AvatarRenderer<T> playerRenderer = renderers.get(model);
        return playerRenderer != null ? playerRenderer : renderers.get(PlayerModelType.WIDE);
    }

    public <S extends EntityRenderState> EntityRenderer<?, ? super S> getRenderer(S entityRenderState) {
        if (entityRenderState instanceof AvatarRenderState player) {
            PlayerModelType model = player.skin.model();
            EntityRenderer<? extends Avatar, ?> playerRenderer = (EntityRenderer<? extends Avatar, ?>)this.playerRenderers.get(model);
            return (EntityRenderer<?, ? super S>)(playerRenderer != null ? playerRenderer : (EntityRenderer)this.playerRenderers.get(PlayerModelType.WIDE));
        } else {
            return (EntityRenderer<?, ? super S>)this.renderers.get(entityRenderState.entityType);
        }
    }

    public void prepare(Camera camera, Entity crosshairPickEntity) {
        this.camera = camera;
        this.info = camera;
        this.crosshairPickEntity = crosshairPickEntity;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum culler, double camX, double camY, double camZ) {
        EntityRenderer<? super E, ?> renderer = this.getRenderer(entity);
        return renderer.shouldRender(entity, culler, camX, camY, camZ);
    }

    public <E extends Entity> EntityRenderState extractEntity(E entity, float partialTicks) {
        EntityRenderer<? super E, ?> renderer = this.getRenderer(entity);

        try {
            return renderer.createRenderState(entity, partialTicks);
        } catch (Throwable t) {
            CrashReport report = CrashReport.forThrowable(t, "Extracting render state for an entity in world");
            CrashReportCategory entityCat = report.addCategory("Entity being extracted");
            entity.fillCrashReportCategory(entityCat);
            CrashReportCategory rendererCategory = this.fillRendererDetails(renderer, report);
            rendererCategory.setDetail("Delta", partialTicks);
            throw new ReportedException(report);
        }
    }

    public <S extends EntityRenderState> void submit(
        S renderState, CameraRenderState camera, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector submitNodeCollector
    ) {
        EntityRenderer<?, ? super S> renderer = this.getRenderer(renderState);

        try {
            Vec3 pos = renderer.getRenderOffset(renderState);
            double relativeX = x + pos.x();
            double relativeY = y + pos.y();
            double relativeZ = z + pos.z();
            poseStack.pushPose();
            poseStack.translate(relativeX, relativeY, relativeZ);
            if (renderState.entity != null) {
                renderer = CustomEntityModels.getEntityRenderer(renderState.entity, renderer);
            }

            renderState.entityRenderer = renderer;
            EntityRenderState statePrev = RenderState.setEntityRenderState(renderState);
            renderer.submit(renderState, poseStack, submitNodeCollector, camera);
            RenderState.setEntityRenderState(statePrev);
            if (renderState.displayFireAnimation) {
                submitNodeCollector.submitFlame(poseStack, renderState, Mth.rotationAroundAxis(Mth.Y_AXIS, camera.orientation, new Quaternionf()));
            }

            if (renderState instanceof AvatarRenderState) {
                poseStack.translate(-pos.x(), -pos.y(), -pos.z());
            }

            if (!renderState.shadowPieces.isEmpty()) {
                boolean shadowOffset = CustomEntityModels.isActive() && renderer.shadowOffsetX != 0.0F && renderer.shadowOffsetZ != 0.0F;
                if (shadowOffset) {
                    float sx = renderer.shadowOffsetX;
                    float sz = renderer.shadowOffsetZ;
                    float yaw = renderState.entity != null ? -renderState.entity.getYRot() * (float) (Math.PI / 180.0) : 0.0F;
                    float cYaw = Mth.cos(yaw);
                    float sYaw = Mth.sin(yaw);
                    float tx = sx * cYaw + sz * sYaw;
                    float tz = sz * cYaw - sx * sYaw;
                    poseStack.pushPose();
                    poseStack.translate(tx, 0.0F, tz);
                }

                submitNodeCollector.submitShadow(poseStack, renderState.shadowRadius, renderState.shadowPieces);
                if (shadowOffset) {
                    poseStack.popPose();
                }
            }

            if (!(renderState instanceof AvatarRenderState)) {
                poseStack.translate(-pos.x(), -pos.y(), -pos.z());
            }

            poseStack.popPose();
        } catch (Throwable t) {
            CrashReport report = CrashReport.forThrowable(t, "Rendering entity in world");
            CrashReportCategory entityCat = report.addCategory("EntityRenderState being rendered");
            renderState.fillCrashReportCategory(entityCat);
            this.fillRendererDetails(renderer, report);
            throw new ReportedException(report);
        }
    }

    private <S extends EntityRenderState> CrashReportCategory fillRendererDetails(EntityRenderer<?, S> renderer, CrashReport report) {
        CrashReportCategory category = report.addCategory("Renderer details");
        category.setDetail("Assigned renderer", renderer);
        return category;
    }

    public void resetCamera() {
        this.camera = null;
    }

    public double distanceToSqr(Entity entity) {
        return this.camera.position().distanceToSqr(entity.position());
    }

    public ItemInHandRenderer getItemInHandRenderer() {
        return this.itemInHandRenderer;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(
            this,
            this.blockModelResolver,
            this.itemModelResolver,
            this.mapRenderer,
            resourceManager,
            this.entityModels.get(),
            this.equipmentAssets,
            this.atlasManager,
            this.font,
            this.playerSkinRenderCache
        );
        this.context = context;
        this.renderers = EntityRenderers.createEntityRenderers(context);
        this.playerRenderers = EntityRenderers.createAvatarRenderers(context);
        this.mannequinRenderers = EntityRenderers.createAvatarRenderers(context);
        registerPlayerItems(this.playerRenderers);
        if (Reflector.ForgeEventFactoryClient_onGatherLayers.exists()) {
            Reflector.ForgeEventFactoryClient_onGatherLayers.call(this.renderers, this.playerRenderers, this.mannequinRenderers, context);
        }
    }

    private static void registerPlayerItems(Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> renderPlayerMap) {
        boolean registered = false;

        for (AvatarRenderer<AbstractClientPlayer> renderer : renderPlayerMap.values()) {
            renderer.removeLayers(PlayerItemsLayer.class);
            renderer.addLayer(new PlayerItemsLayer(renderer));
            registered = true;
        }

        if (!registered) {
            Config.warn("PlayerItemsLayer not registered");
        }
    }

    public Map<EntityType, EntityRenderer> getEntityRenderMap() {
        if (this.renderers instanceof ImmutableMap) {
            this.renderers = new HashMap<>(this.renderers);
        }

        return ((Map<EntityType,EntityRenderer>) (Map) this.renderers);
    }

    public EntityRendererProvider.Context getContext() {
        return this.context;
    }

    public Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> getPlayerRenderers() {
        return Collections.unmodifiableMap(this.playerRenderers);
    }

    public Map<PlayerModelType, AvatarRenderer<ClientMannequin>> getMannequinRenderers() {
        return Collections.unmodifiableMap(this.mannequinRenderers);
    }

    public net.minecraft.util.math.vector.Quaternion getCameraOrientation() {
        if (this.camera == null) return net.minecraft.util.math.vector.Quaternion.ONE;
        return new net.minecraft.util.math.vector.Quaternion(this.camera.rotation());
    }
}
