package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.render.RenderState;

public class BlockEntityRenderDispatcher implements ResourceManagerReloadListener {
    private Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers = ImmutableMap.of();
    private final Font font;
    private final Supplier<EntityModelSet> entityModelSet;
    private Vec3 cameraPos;
    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;
    private final EntityRenderDispatcher entityRenderer;
    private final SpriteGetter sprites;
    private final PlayerSkinRenderCache playerSkinRenderCache;
    private BlockEntityRendererProvider.Context context;

    public BlockEntityRenderDispatcher(
        Font font,
        Supplier<EntityModelSet> entityModelSet,
        BlockModelResolver blockModelResolver,
        ItemModelResolver itemModelResolver,
        EntityRenderDispatcher entityRenderer,
        SpriteGetter sprites,
        PlayerSkinRenderCache playerSkinRenderCache
    ) {
        this.blockModelResolver = blockModelResolver;
        this.itemModelResolver = itemModelResolver;
        this.entityRenderer = entityRenderer;
        this.font = font;
        this.entityModelSet = entityModelSet;
        this.sprites = sprites;
        this.playerSkinRenderCache = playerSkinRenderCache;
    }

    public <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getRenderer(E blockEntity) {
        return (BlockEntityRenderer<E, S>)this.renderers.get(blockEntity.getType());
    }

    public <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getRenderer(S state) {
        return (BlockEntityRenderer<E, S>)this.renderers.get(state.blockEntityType);
    }

    public void prepare(Vec3 cameraPos) {
        this.cameraPos = cameraPos;
    }

    public <E extends BlockEntity, S extends BlockEntityRenderState> S tryExtractRenderState(
        E blockEntity, float partialTicks, ModelFeatureRenderer.CrumblingOverlay breakProgress, boolean isGloballyRendered
    ) {
        BlockEntityRenderer<E, S> renderer = this.getRenderer(blockEntity);
        if (renderer == null) {
            return null;
        }

        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
            return null;
        }

        if (isGloballyRendered != renderer.shouldRenderOffScreen()) {
            return null;
        }

        if (!renderer.shouldRender(blockEntity, this.cameraPos)) {
            return null;
        }

        Vec3 cameraPosition = this.cameraPos;
        S state = renderer.createRenderState();
        renderer.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        return state;
    }

    public <S extends BlockEntityRenderState> void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        BlockEntityRenderer<?, S> renderer = this.getRenderer(state);
        if (renderer != null) {
            try {
                renderer = CustomEntityModels.getBlockEntityRenderer(state.blockEntity, renderer);
                state.blockEntityRenderer = renderer;
                BlockEntityRenderState statePrev = RenderState.setBlockEntityRenderState(state);
                renderer.submit(state, poseStack, submitNodeCollector, camera);
                RenderState.setBlockEntityRenderState(statePrev);
            } catch (Throwable t) {
                CrashReport report = CrashReport.forThrowable(t, "Rendering Block Entity");
                CrashReportCategory category = report.addCategory("Block Entity Details");
                state.fillCrashReportCategory(category);
                throw new ReportedException(report);
            }
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        BlockEntityRendererProvider.Context context = new BlockEntityRendererProvider.Context(
            this,
            this.blockModelResolver,
            this.itemModelResolver,
            this.entityRenderer,
            this.entityModelSet.get(),
            this.font,
            this.sprites,
            this.playerSkinRenderCache
        );
        this.context = context;
        this.renderers = BlockEntityRenderers.createEntityRenderers(context);
    }

    public BlockEntityRenderer getRenderer(BlockEntityType type) {
        return this.renderers.get(type);
    }

    public BlockEntityRendererProvider.Context getContext() {
        return this.context;
    }

    public Map<BlockEntityType, BlockEntityRenderer> getBlockEntityRenderMap() {
        if (this.renderers instanceof ImmutableMap) {
            this.renderers = new HashMap<>(this.renderers);
        }

        return ((Map<BlockEntityType,BlockEntityRenderer>) (Map) this.renderers);
    }
}
