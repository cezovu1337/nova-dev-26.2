package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.MessageBox;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiBannerResultRenderer;
import net.minecraft.client.gui.render.pip.GuiBookModelRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.GuiProfilerChartRenderer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraftforge.client.event.ViewportEvent;
import net.optifine.Config;
import net.optifine.GlErrors;
import net.optifine.Lagometer;
import net.optifine.RandomEntities;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorResolver;
import net.optifine.render.RenderUtils;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.MemoryMonitor;
import net.optifine.util.TimedEvent;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class GameRenderer implements AutoCloseable, TrackedWaypoint.Projector {
    private static final Identifier BLUR_POST_CHAIN_ID = Identifier.withDefaultNamespace("blur");
    public static final int MAX_BLUR_RADIUS = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float PROJECTION_3D_HUD_Z_FAR = 100.0F;
    private static final float PORTAL_SPINNING_SPEED = 20.0F;
    private static final float NAUSEA_SPINNING_SPEED = 7.0F;
    private final Minecraft minecraft;
    private final GameRenderState gameRenderState = new GameRenderState();
    private final RandomSource random = RandomSource.create();
    public final ItemInHandRenderer itemInHandRenderer;
    private final ScreenEffectRenderer screenEffectRenderer;
    private final DebugCrosshairRenderer debugCrosshairRenderer;
    private final RenderBuffers renderBuffers;
    private final RenderTarget mainRenderTarget;
    private float spinningEffectTime;
    private float spinningEffectSpeed;
    private float bossOverlayWorldDarkening;
    private float bossOverlayWorldDarkeningO;
    private boolean renderBlockOutline = true;
    private long lastScreenshotAttempt;
    private boolean hasWorldScreenshot;
    private final Lightmap lightmap = new Lightmap();
    private final LightmapRenderStateExtractor lightmapRenderStateExtractor;
    private final UiLightmap uiLightmap = new UiLightmap();
    private boolean useUiLightmap;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    protected final Panorama panorama = new Panorama();
    private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
    private final FogRenderer fogRenderer = new FogRenderer();
    private final GuiRenderer guiRenderer;
    private final FeatureRenderDispatcher featureRenderDispatcher;
    private final SubmitNodeStorage handAndScreenSubmitNodeStorage = new SubmitNodeStorage();
    private Identifier postEffectId;
    private boolean effectActive;
    private final ActiveRenderInfo mainCamera = new ActiveRenderInfo();
    protected final Projection hudProjection = new Projection();
    private final Lighting lighting = new Lighting();
    private final GlobalSettingsUniform globalSettingsUniform = new GlobalSettingsUniform();
    private final ProjectionMatrixBuffer levelProjectionMatrixBuffer = new ProjectionMatrixBuffer("level");
    protected final ProjectionMatrixBuffer hud3dProjectionMatrixBuffer = new ProjectionMatrixBuffer("3d hud");
    private boolean initialized = false;
    private Level updatedWorld = null;
    private int frameCount = 0;
    private float clipDistance = 128.0F;
    private long lastServerTime = 0L;
    private int lastServerTicks = 0;
    private int serverWaitTime = 0;
    private int serverWaitTimeCurrent = 0;
    private float avgServerTimeDiff = 0.0F;
    private float avgServerTickDiff = 0.0F;
    private Identifier[] fxaaShaders = new Identifier[10];
    private final StagedVertexBuffer handStagedVertexBuffer;
    private final FeatureRenderDispatcher handFeatureRenderDispatcher;

    public GameRenderer(Minecraft minecraft, ItemInHandRenderer itemInHandRenderer, ModelManager modelManager) {
        this.minecraft = minecraft;
        this.itemInHandRenderer = itemInHandRenderer;
        this.lightmapRenderStateExtractor = new LightmapRenderStateExtractor(this, minecraft);

        try {
            int maxSectionBuilders = Runtime.getRuntime().availableProcessors();
            this.renderBuffers = new RenderBuffers(maxSectionBuilders);
        } catch (OutOfMemoryError e) {
            MessageBox.error(
                "Oh no! The game was unable to allocate memory off-heap while trying to start. You may try to free some memory by closing other applications on your computer, check that your system meets the minimum requirements, and try again. If the problem persists, please visit: "
                    + CommonLinks.GENERAL_HELP
            );
            throw new SilentInitException("Unable to allocate render buffers", e);
        }

        AtlasManager atlasManager = minecraft.getAtlasManager();
        this.featureRenderDispatcher = new FeatureRenderDispatcher(this.renderBuffers, modelManager, atlasManager, minecraft.font, this.gameRenderState);
        this.guiRenderer = new GuiRenderer(
            this.gameRenderState.guiRenderState,
            this.featureRenderDispatcher,
            List.of(
                new GuiEntityRenderer(minecraft.getEntityRenderDispatcher()),
                new GuiSkinRenderer(),
                new GuiBookModelRenderer(),
                new GuiBannerResultRenderer(atlasManager),
                new GuiProfilerChartRenderer()
            )
        );
        this.screenEffectRenderer = new ScreenEffectRenderer(minecraft, atlasManager);
        this.debugCrosshairRenderer = new DebugCrosshairRenderer();
        this.mainRenderTarget = new MainTarget(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        this.handStagedVertexBuffer = new StagedVertexBuffer(() -> "Shaders Buffer", 65536);
        this.handFeatureRenderDispatcher = new FeatureRenderDispatcher(
            this.handStagedVertexBuffer, modelManager, atlasManager, minecraft.font, this.gameRenderState
        );
    }

    @Override
    public void close() {
        this.debugCrosshairRenderer.close();
        this.globalSettingsUniform.close();
        this.lightmap.close();
        this.overlayTexture.close();
        this.uiLightmap.close();
        this.resourcePool.close();
        this.guiRenderer.close();
        this.levelProjectionMatrixBuffer.close();
        this.hud3dProjectionMatrixBuffer.close();
        this.lighting.close();
        this.fogRenderer.close();
        this.featureRenderDispatcher.close();
        this.mainRenderTarget.destroyBuffers();
        this.renderBuffers.close();
    }

    public RenderBuffers renderBuffers() {
        return this.renderBuffers;
    }

    public FeatureRenderDispatcher featureRenderDispatcher() {
        return this.featureRenderDispatcher;
    }

    public GameRenderState gameRenderState() {
        return this.gameRenderState;
    }

    public void setRenderBlockOutline(boolean renderBlockOutline) {
        this.renderBlockOutline = renderBlockOutline;
    }

    public void clearPostEffect() {
        this.postEffectId = null;
        this.effectActive = false;
    }

    public void togglePostEffect() {
        this.effectActive = !this.effectActive;
    }

    public void checkEntityPostEffect(Entity cameraEntity) {
        switch (cameraEntity) {
            case Creeper ignored:
                this.setPostEffect(Identifier.withDefaultNamespace("creeper"));
                break;
            case Spider ignored:
                this.setPostEffect(Identifier.withDefaultNamespace("spider"));
                break;
            case EnderMan ignored:
                this.setPostEffect(Identifier.withDefaultNamespace("invert"));
                break;
            case null:
            default:
                this.clearPostEffect();
                if (Reflector.EntitySpectatorShaderManager_get.exists() && cameraEntity != null) {
                    Identifier rl = (Identifier)Reflector.EntitySpectatorShaderManager_get.call(cameraEntity.getType());
                    if (rl != null) {
                        this.setPostEffect(rl);
                    }
                }
        }
    }

    private void setPostEffect(Identifier id) {
        if (Config.isMainFramebufferEnabled()) {
            this.postEffectId = id;
            this.effectActive = true;
        }
    }

    public void processBlurEffect() {
        if (Config.isMainFramebufferEnabled()) {
            GlStateManager._disableDepthTest();
            PostChain postChain = this.minecraft.getShaderManager().getPostChain(BLUR_POST_CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
            if (postChain != null) {
                postChain.process(this.mainRenderTarget, this.resourcePool);
            }
        }
    }

    public void preloadUiShader(ResourceProvider resourceProvider) {
        GpuDevice device = RenderSystem.getDevice();
        ShaderSource shaderSource = (id, type) -> {
            Identifier location = type.idConverter().idToFile(id);

            try (Reader reader = resourceProvider.getResourceOrThrow(location).openAsReader()) {
                return IOUtils.toString(reader);
            } catch (IOException exception) {
                LOGGER.error("Coudln't preload {} shader {}: {}", type, id, exception);
                return null;
            }
        };
        device.precompilePipeline(RenderPipelines.GUI, shaderSource);
        device.precompilePipeline(RenderPipelines.GUI_TEXTURED, shaderSource);
        if (TracyClient.isAvailable()) {
            device.precompilePipeline(RenderPipelines.TRACY_BLIT, shaderSource);
        }
    }

    public void tick() {
        this.lightmapRenderStateExtractor.tick();
        LocalPlayer player = this.minecraft.player;
        if (this.mainCamera.entity() == null) {
            this.mainCamera.setEntity(player);
        }

        this.mainCamera.tick();
        this.itemInHandRenderer.tick();
        float portalIntensity = player.portalEffectIntensity;
        float nauseaIntensity = player.getEffectBlendFactor(MobEffects.NAUSEA, 1.0F);
        if (!(portalIntensity > 0.0F) && !(nauseaIntensity > 0.0F)) {
            this.spinningEffectSpeed = 0.0F;
        } else {
            this.spinningEffectSpeed = (portalIntensity * 20.0F + nauseaIntensity * 7.0F) / (portalIntensity + nauseaIntensity);
            this.spinningEffectTime = this.spinningEffectTime + this.spinningEffectSpeed;
        }

        if (this.minecraft.level.tickRateManager().runsNormally()) {
            this.bossOverlayWorldDarkeningO = this.bossOverlayWorldDarkening;
            if (this.minecraft.gui.hud.getBossOverlay().shouldDarkenScreen()) {
                this.bossOverlayWorldDarkening += 0.05F;
                if (this.bossOverlayWorldDarkening > 1.0F) {
                    this.bossOverlayWorldDarkening = 1.0F;
                }
            } else if (this.bossOverlayWorldDarkening > 0.0F) {
                this.bossOverlayWorldDarkening -= 0.0125F;
            }

            this.screenEffectRenderer.tick();
        }
    }

    public Identifier currentPostEffect() {
        return this.postEffectId;
    }

    public void resize(int width, int height) {
        this.resourcePool.clear();
        this.mainRenderTarget.resize(width, height);
        this.minecraft.levelRenderer.resize(width, height);
    }

    private void bobHurt(CameraRenderState cameraState, PoseStack poseStack) {
        if (cameraState.entityRenderState.isLiving) {
            float hurt = cameraState.entityRenderState.hurtTime;
            if (cameraState.entityRenderState.isDeadOrDying) {
                float duration = Math.min(cameraState.entityRenderState.deathTime, 20.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (duration + 200.0F)));
            }

            if (hurt < 0.0F) {
                return;
            }

            hurt /= cameraState.entityRenderState.hurtDuration;
            hurt = Mth.sin(hurt * hurt * hurt * hurt * (float) Math.PI);
            float rr = cameraState.entityRenderState.hurtDir;
            poseStack.mulPose(Axis.YP.rotationDegrees(-rr));
            float tiltAmount = (float)(-hurt * 14.0 * this.gameRenderState.optionsRenderState.damageTiltStrength);
            poseStack.mulPose(Axis.ZP.rotationDegrees(tiltAmount));
            poseStack.mulPose(Axis.YP.rotationDegrees(rr));
        }
    }

    private void bobView(CameraRenderState cameraState, PoseStack poseStack) {
        if (cameraState.entityRenderState.isPlayer) {
            float backwardsInterpolatedWalkDistance = cameraState.entityRenderState.backwardsInterpolatedWalkDistance;
            float bob = cameraState.entityRenderState.bob;
            poseStack.translate(
                Mth.sin(backwardsInterpolatedWalkDistance * (float) Math.PI) * bob * 0.5F,
                -Math.abs(Mth.cos(backwardsInterpolatedWalkDistance * (float) Math.PI) * bob),
                0.0F
            );
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(backwardsInterpolatedWalkDistance * (float) Math.PI) * bob * 3.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(backwardsInterpolatedWalkDistance * (float) Math.PI - 0.2F) * bob) * 5.0F));
        }
    }

    private void renderItemInHand(CameraRenderState cameraState, float deltaPartialTick, Matrix4fc modelViewMatrix) {
        this.renderItemInHand(cameraState, deltaPartialTick, modelViewMatrix, false);
    }

    public void renderItemInHand(CameraRenderState cameraState, float deltaPartialTick, Matrix4fc modelViewMatrix, boolean renderTranslucent) {
        if (!cameraState.isPanoramicMode
            && this.gameRenderState.optionsRenderState.cameraType.isFirstPerson()
            && !cameraState.entityRenderState.isSleeping
            && !this.gameRenderState.guiRenderState.isHudHidden
            && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();
            poseStack.mulPose(modelViewMatrix.invert(new Matrix4f()));
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix().set(modelViewMatrix);
            this.bobHurt(cameraState, poseStack);
            if (this.gameRenderState.optionsRenderState.bobView) {
                this.bobView(cameraState, poseStack);
            }

            if (Config.isShaders()) {
                ShadersRender.renderItemFP(
                    this.itemInHandRenderer,
                    deltaPartialTick,
                    poseStack,
                    this.handAndScreenSubmitNodeStorage,
                    this.minecraft.player,
                    this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, deltaPartialTick),
                    renderTranslucent
                );
            } else {
                this.itemInHandRenderer
                    .submitHandsWithItems(
                        deltaPartialTick,
                        poseStack,
                        this.handAndScreenSubmitNodeStorage,
                        this.minecraft.player,
                        this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, deltaPartialTick)
                    );
            }

            this.handFeatureRenderDispatcher.renderAllFeatures(this.handAndScreenSubmitNodeStorage);
            modelViewStack.popMatrix();
            poseStack.popPose();
        }
    }

    public static float nightVisionScale(LivingEntity camera, float a) {
        MobEffectInstance nightVision = camera.getEffect(MobEffects.NIGHT_VISION);
        return !nightVision.endsWithin(200) ? 1.0F : 0.7F + Mth.sin((nightVision.getDuration() - a) * (float) Math.PI * 0.2F) * 0.3F;
    }

    public void update(DeltaTracker deltaTracker) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("camera");
        this.mainCamera.update(deltaTracker);
        profiler.pop();
    }

    public void extract(DeltaTracker deltaTracker, boolean advanceGameTime) {
        boolean resourcesLoaded = this.minecraft.isGameLoadFinished();
        boolean readyForLevelRendering = resourcesLoaded && advanceGameTime && this.minecraft.level != null;
        float worldPartialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
        this.extractWindow();
        this.extractOptions();
        if (readyForLevelRendering) {
            this.lightmapRenderStateExtractor.extract(this.gameRenderState.lightmapRenderState, 1.0F);
            float cameraEntityPartialTicks = this.mainCamera.getCameraEntityPartialTicks(deltaTracker);
            this.extractCamera(deltaTracker, worldPartialTicks, cameraEntityPartialTicks);
            this.minecraft.levelExtractor.extract(deltaTracker, this.mainCamera, worldPartialTicks);
        }

        this.minecraft.gui.extractRenderState(deltaTracker, readyForLevelRendering, resourcesLoaded);
        this.minecraft.getMetricsRecorder().sampleDuringExtract();
    }

    public void render(DeltaTracker deltaTracker, boolean advanceGameTime) {
        this.frameInit();
        ProfilerFiller profiler = Profiler.get();
        profiler.push("render");
        WindowRenderState windowRenderState = this.gameRenderState.windowRenderState;
        if (windowRenderState.width != this.mainRenderTarget.width || windowRenderState.height != this.mainRenderTarget.height) {
            this.resize(windowRenderState.width, windowRenderState.height);
        }

        RenderSystem.getDevice()
            .createCommandEncoder()
            .clearColorAndDepthTextures(
                this.mainRenderTarget.getColorTexture(), this.gameRenderState.guiRenderState.clearColorOverride, this.mainRenderTarget.getDepthTexture(), 0.0
            );
        boolean resourcesLoaded = this.minecraft.isGameLoadFinished();
        boolean shouldRenderLevel = resourcesLoaded && advanceGameTime && this.minecraft.level != null;
        this.globalSettingsUniform
            .update(
                windowRenderState.width,
                windowRenderState.height,
                this.gameRenderState.optionsRenderState.glintStrength,
                this.minecraft.level == null ? 0L : this.minecraft.level.getGameTime(),
                deltaTracker,
                this.gameRenderState.optionsRenderState.menuBackgroundBlurriness,
                this.gameRenderState.levelRenderState.cameraRenderState.pos,
                this.gameRenderState.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS
            );
        if (Config.isReloadingResources()) {
            shouldRenderLevel = false;
        }

        if (shouldRenderLevel) {
            this.lightmap.render(this.gameRenderState.lightmapRenderState);
            profiler.push("world");
            this.renderLevel(deltaTracker);
            this.tryTakeScreenshotIfNeeded();
            this.minecraft.levelRenderer.doEntityOutline();
            if (this.postEffectId != null && this.effectActive) {
                PostChain postChain = this.minecraft.getShaderManager().getPostChain(this.postEffectId, LevelTargetBundle.MAIN_TARGETS);
                if (postChain != null) {
                    postChain.process(this.mainRenderTarget, this.resourcePool);
                }

                GlStateManager.enableTexture();
            }

            profiler.pop();
        } else {
            GlStateManager._viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        }

        this.fogRenderer.endFrame();
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.mainRenderTarget.getDepthTexture(), 0.0);
        this.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        this.useUiLightmap = true;
        profiler.push("gui");
        this.guiRenderer.render();
        this.guiRenderer.endFrame();
        profiler.pop();
        this.useUiLightmap = false;
        this.renderBuffers.endFrame();
        this.resourcePool.endFrame();
        profiler.pop();
        this.frameFinish();
        this.handStagedVertexBuffer.endFrame();
        this.waitForServerThread();
        MemoryMonitor.update();
        Lagometer.updateLagometer();
    }

    private void tryTakeScreenshotIfNeeded() {
        if (!this.hasWorldScreenshot && this.minecraft.isLocalServer()) {
            long time = Util.getMillis();
            if (time - this.lastScreenshotAttempt >= 1000L) {
                this.lastScreenshotAttempt = time;
                IntegratedServer server = this.minecraft.getSingleplayerServer();
                if (server != null && !server.isStopped()) {
                    server.getWorldScreenshotFile().ifPresent(path -> {
                        if (Files.isRegularFile(path)) {
                            this.hasWorldScreenshot = true;
                        } else {
                            this.takeAutoScreenshot(path);
                        }
                    });
                }
            }
        }
    }

    private void takeAutoScreenshot(Path screenshotFile) {
        if (this.minecraft.levelExtractor.countRenderedSections() > 10 && this.minecraft.levelRenderer.hasRenderedAllSections()) {
            Screenshot.takeScreenshot(this.mainRenderTarget, screenshot -> Util.ioPool().execute(() -> {
                int width = screenshot.getWidth();
                int height = screenshot.getHeight();
                int x = 0;
                int y = 0;
                if (width > height) {
                    x = (width - height) / 2;
                    width = height;
                } else {
                    y = (height - width) / 2;
                    height = width;
                }

                try (NativeImage scaled = new NativeImage(64, 64, false)) {
                    screenshot.resizeSubRectTo(x, y, width, height, scaled);
                    scaled.writeToFile(screenshotFile);
                } catch (IOException e) {
                    LOGGER.warn("Couldn't save auto screenshot", e);
                } finally {
                    screenshot.close();
                }
            }));
        }
    }

    private boolean shouldRenderBlockOutline() {
        if (!this.renderBlockOutline) {
            return false;
        }

        Entity cameraEntity = this.minecraft.getCameraEntity();
        boolean renderOutline = cameraEntity instanceof Player && !this.minecraft.gui.hud.isHidden();
        if (renderOutline && !((Player)cameraEntity).getAbilities().mayBuild) {
            ItemStack itemStack = ((LivingEntity)cameraEntity).getMainHandItem();
            HitResult hitResult = this.minecraft.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
                BlockState blockState = this.minecraft.level.getBlockState(pos);
                if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                    renderOutline = blockState.getMenuProvider(this.minecraft.level, pos) != null;
                } else {
                    BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, pos, false);
                    Registry<Block> blockRegistry = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BLOCK);
                    renderOutline = !itemStack.isEmpty()
                        && (itemStack.canBreakBlockInAdventureMode(blockInWorld) || itemStack.canPlaceOnBlockInAdventureMode(blockInWorld));
                    if (Reflector.ForgeHooksClient.exists() && !renderOutline && blockState.hasBlockEntity()) {
                        BlockEntity blockEntity = this.minecraft.level.getBlockEntity(pos);
                        renderOutline = blockEntity != null && blockEntity.hasCustomOutlineRendering();
                    }
                }
            }
        }

        return renderOutline;
    }

    public void renderLevel(DeltaTracker deltaTracker) {
        boolean isShaders = Config.isShaders();
        if (isShaders) {
            Shaders.frameInit(this.minecraft);
        }

        float worldPartialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
        float cameraEntityPartialTicks = this.mainCamera.getCameraEntityPartialTicks(deltaTracker);
        LocalPlayer player = this.minecraft.player;
        ProfilerFiller profiler = Profiler.get();
        boolean renderOutline = this.shouldRenderBlockOutline();
        OptionsRenderState optionsState = this.gameRenderState.optionsRenderState;
        CameraRenderState cameraState = this.gameRenderState.levelRenderState.cameraRenderState;
        Matrix4fc modelViewMatrix = cameraState.viewRotationMatrix;
        profiler.push("matrices");
        Matrix4f projectionMatrix = new Matrix4f(cameraState.projectionMatrix);
        Matrix4f matrixProjection = projectionMatrix;
        if (Shaders.isEffectsModelView()) {
            projectionMatrix = new Matrix4f();
        }

        PoseStack bobStack = new PoseStack();
        this.bobHurt(cameraState, bobStack);
        if (optionsState.bobView) {
            this.bobView(cameraState, bobStack);
        }

        if (Reflector.ForgeEventFactoryClient_fireComputeCameraAngles.exists()) {
            ViewportEvent.ComputeCameraAngles cameraSetup = (ViewportEvent.ComputeCameraAngles)Reflector.ForgeEventFactoryClient_fireComputeCameraAngles
                .call(this, this.mainCamera, worldPartialTicks);
            this.mainCamera.setRotation(cameraSetup.getYaw(), cameraSetup.getPitch(), cameraSetup.getRoll());
        }

        projectionMatrix.mul(bobStack.last().pose());
        float screenEffectScale = optionsState.screenEffectScale;
        float portalIntensity = Mth.lerp(worldPartialTicks, player.oPortalEffectIntensity, player.portalEffectIntensity);
        float nauseaIntensity = player.getEffectBlendFactor(MobEffects.NAUSEA, worldPartialTicks);
        float spinningEffectIntensity = Math.max(portalIntensity, nauseaIntensity) * (screenEffectScale * screenEffectScale);
        if (spinningEffectIntensity > 0.0F) {
            float skew = 5.0F / (spinningEffectIntensity * spinningEffectIntensity + 5.0F) - spinningEffectIntensity * 0.04F;
            skew *= skew;
            Vector3f axis = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            float angle = (this.spinningEffectTime + worldPartialTicks * this.spinningEffectSpeed) * (float) (Math.PI / 180.0);
            projectionMatrix.rotate(angle, axis);
            projectionMatrix.scale(1.0F / skew, 1.0F, 1.0F);
            projectionMatrix.rotate(-angle, axis);
        }

        Matrix4f matrixEffects = projectionMatrix;
        if (Shaders.isEffectsModelView()) {
            projectionMatrix = matrixProjection;
        }

        RenderSystem.setProjectionMatrix(this.levelProjectionMatrixBuffer.getBuffer(projectionMatrix), ProjectionType.PERSPECTIVE);
        if (Shaders.isEffectsModelView()) {
            modelViewMatrix = matrixEffects.mul(modelViewMatrix);
            cameraState.viewRotationMatrix.set(modelViewMatrix);
        }

        profiler.popPush("fog");
        this.fogRenderer.updateBuffer(cameraState.fogData);
        GpuBufferSlice terrainFog = this.fogRenderer.getBuffer(FogRenderer.FogMode.WORLD);
        profiler.popPush("level");
        if (isShaders) {
            Shaders.beginRender(this.mainCamera, modelViewMatrix, projectionMatrix, deltaTracker, worldPartialTicks);
            Shaders.beginRenderPass(worldPartialTicks);
        }

        boolean shouldCreateBossFog = this.minecraft.gui.hud.getBossOverlay().shouldCreateWorldFog();
        this.minecraft
            .levelRenderer
            .render(this.resourcePool, deltaTracker, renderOutline, cameraState, modelViewMatrix, terrainFog, cameraState.fogData.color, !shouldCreateBossFog);
        profiler.popPush("hand");
        boolean isSleeping = cameraState.entityRenderState.isSleeping;
        this.hudProjection
            .setupPerspective(0.05F, 100.0F, cameraState.hudFov, this.gameRenderState.windowRenderState.width, this.gameRenderState.windowRenderState.height);
        if (isShaders) {
            Shaders.beginRenderHand(true);
            RenderSystem.setProjectionMatrix(this.hud3dProjectionMatrixBuffer.getBuffer(this.hudProjection), ProjectionType.PERSPECTIVE);
            ShadersRender.renderHandTranslucent(this, cameraState, modelViewMatrix, worldPartialTicks);
            Shaders.endRenderHand();
        } else {
            RenderSystem.setProjectionMatrix(this.hud3dProjectionMatrixBuffer.getBuffer(this.hudProjection), ProjectionType.PERSPECTIVE);
            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.mainRenderTarget.getDepthTexture(), 0.0);
            this.renderItemInHand(cameraState, cameraEntityPartialTicks, modelViewMatrix);
        }

        if (isShaders) {
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(this.levelProjectionMatrixBuffer.getBuffer(projectionMatrix), ProjectionType.PERSPECTIVE);
            Shaders.renderCompositeFinal();
            RenderSystem.restoreProjectionMatrix();
        }

        if (isShaders) {
            Shaders.beginFPOverlay();
        }

        profiler.popPush("screenEffects");
        this.screenEffectRenderer
            .submit(
                optionsState.cameraType.isFirstPerson(),
                isSleeping,
                worldPartialTicks,
                this.handAndScreenSubmitNodeStorage,
                this.gameRenderState.guiRenderState.isHudHidden
            );
        this.featureRenderDispatcher.renderAllFeatures(this.handAndScreenSubmitNodeStorage);
        profiler.pop();
        if (isShaders) {
            Shaders.endFPOverlay();
        }

        if (isShaders) {
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(this.levelProjectionMatrixBuffer.getBuffer(projectionMatrix), ProjectionType.PERSPECTIVE);
            Shaders.endRender();
            RenderSystem.restoreProjectionMatrix();
        }

        RenderSystem.setShaderFog(this.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        if (this.gameRenderState.levelRenderState.render3dCrosshair
            && optionsState.cameraType.isFirstPerson()
            && !this.gameRenderState.guiRenderState.isHudHidden) {
            this.debugCrosshairRenderer.render(cameraState, this.gameRenderState.windowRenderState.guiScale);
        }
    }

    private void extractWindow() {
        WindowRenderState windowState = this.gameRenderState.windowRenderState;
        Window window = this.minecraft.getWindow();
        windowState.width = window.getWidth();
        windowState.height = window.getHeight();
        windowState.guiScale = window.getGuiScale();
        windowState.appropriateLineWidth = window.getAppropriateLineWidth();
        windowState.isMinimized = window.isMinimized();
    }

    private void extractOptions() {
        OptionsRenderState optionsState = this.gameRenderState.optionsRenderState;
        Options options = this.minecraft.options;
        optionsState.cloudRange = options.cloudRange().get();
        optionsState.cutoutLeaves = options.cutoutLeaves().get();
        optionsState.improvedTransparency = options.improvedTransparency().get();
        optionsState.ambientOcclusion = options.ambientOcclusion().get();
        optionsState.menuBackgroundBlurriness = options.getMenuBackgroundBlurriness();
        optionsState.panoramaSpeed = options.panoramaSpeed().get();
        optionsState.maxAnisotropyValue = options.maxAnisotropyValue();
        optionsState.textureFiltering = options.textureFiltering().get();
        optionsState.bobView = options.bobView().get();
        optionsState.screenEffectScale = options.screenEffectScale().get().floatValue();
        optionsState.glintSpeed = options.glintSpeed().get();
        optionsState.glintStrength = options.glintStrength().get();
        optionsState.damageTiltStrength = options.damageTiltStrength().get();
        optionsState.backgroundForChatOnly = options.backgroundForChatOnly().get();
        optionsState.textBackgroundOpacity = options.textBackgroundOpacity().get().floatValue();
        optionsState.cloudStatus = options.getCloudStatus();
        optionsState.cameraType = options.getCameraType();
        optionsState.renderDistance = options.getEffectiveRenderDistance();
        optionsState.chunkSectionFadeInTime = options.chunkSectionFadeInTime().get();
        optionsState.prioritizeChunkUpdates = options.prioritizeChunkUpdates().get();
        optionsState.fov = options.fov().get();
    }

    private void extractCamera(DeltaTracker deltaTracker, float worldPartialTicks, float cameraEntityPartialTicks) {
        CameraRenderState cameraState = this.gameRenderState.levelRenderState.cameraRenderState;
        this.mainCamera.extractRenderState(cameraState, cameraEntityPartialTicks);
        cameraState.fogType = this.mainCamera.getFluidInCamera();
        cameraState.fogData = this.fogRenderer
            .setupFog(
                this.mainCamera,
                this.minecraft.options.getEffectiveRenderDistance(),
                deltaTracker,
                this.bossOverlayWorldDarkening(worldPartialTicks),
                this.minecraft.level
            );
    }

    public void resetData() {
        this.screenEffectRenderer.resetItemActivation();
        this.minecraft.getMapTextureManager().resetData();
        this.mainCamera.reset();
        this.hasWorldScreenshot = false;
    }

    private void waitForServerThread() {
        this.serverWaitTimeCurrent = 0;
        if (!Config.isSmoothWorld() || !Config.isSingleProcessor()) {
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
        } else if (this.minecraft.isLocalServer()) {
            IntegratedServer srv = this.minecraft.getSingleplayerServer();
            if (srv != null) {
                boolean paused = this.minecraft.isPaused();
                if (!paused && !(this.minecraft.gui.screen() instanceof LevelLoadingScreen)) {
                    if (this.serverWaitTime > 0) {
                        Lagometer.timerServer.start();
                        Config.sleep(this.serverWaitTime);
                        Lagometer.timerServer.end();
                        this.serverWaitTimeCurrent = this.serverWaitTime;
                    }

                    long timeNow = System.nanoTime() / 1000000L;
                    if (this.lastServerTime != 0L && this.lastServerTicks != 0) {
                        long timeDiff = timeNow - this.lastServerTime;
                        if (timeDiff < 0L) {
                            this.lastServerTime = timeNow;
                            timeDiff = 0L;
                        }

                        if (timeDiff >= 50L) {
                            this.lastServerTime = timeNow;
                            int ticks = srv.getTickCount();
                            int tickDiff = ticks - this.lastServerTicks;
                            if (tickDiff < 0) {
                                this.lastServerTicks = ticks;
                                tickDiff = 0;
                            }

                            if (tickDiff < 1 && this.serverWaitTime < 100) {
                                this.serverWaitTime += 2;
                            }

                            if (tickDiff > 1 && this.serverWaitTime > 0) {
                                this.serverWaitTime--;
                            }

                            this.lastServerTicks = ticks;
                        }
                    } else {
                        this.lastServerTime = timeNow;
                        this.lastServerTicks = srv.getTickCount();
                        this.avgServerTickDiff = 1.0F;
                        this.avgServerTimeDiff = 50.0F;
                    }
                } else {
                    if (this.minecraft.gui.screen() instanceof LevelLoadingScreen) {
                        Config.sleep(20L);
                    }

                    this.lastServerTime = 0L;
                    this.lastServerTicks = 0;
                }
            }
        }
    }

    private void frameInit() {
        this.frameCount++;
        Config.frameStart();
        GlErrors.frameStart();
        RenderUtils.frameStart();
        if (!this.initialized) {
            ReflectorResolver.resolve();
            if (Config.getBitsOs() == 64 && Config.getBitsJre() == 32) {
                Config.setNotify64BitJava(true);
            }

            this.initialized = true;
        }

        Level world = this.minecraft.level;
        if (world != null) {
            if (Config.getNewRelease() != null) {
                String userEdition = "HD_U".replace("HD_U", "HD Ultra").replace("L", "Light");
                String fullNewVer = userEdition + " " + Config.getNewRelease();
                MutableComponent msg = Component.literal(I18n.get("of.message.newVersion", "\u00c2\u00a7n" + fullNewVer + "\u00c2\u00a7r"));
                msg.setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://optifine.net/downloads"))));
                this.minecraft.gui.hud.getChat().addClientSystemMessage(msg);
                Config.setNewRelease(null);
            }

            if (Config.isNotify64BitJava()) {
                Config.setNotify64BitJava(false);
                Component msg = Component.literal(I18n.get("of.message.java64Bit"));
                this.minecraft.gui.hud.getChat().addClientSystemMessage(msg);
            }
        }

        if (this.updatedWorld != world) {
            RandomEntities.worldChanged(this.updatedWorld, world);
            Config.updateThreadPriorities();
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
            this.updatedWorld = world;
        }

        if (!this.setFxaaShader(Shaders.configAntialiasingLevel)) {
            Shaders.configAntialiasingLevel = 0;
        }
    }

    private void frameFinish() {
        if (this.minecraft.level != null && Config.isShowGlErrors() && TimedEvent.isActive("CheckGlErrorFrameFinish", 10000L)) {
            int err = GlStateManager._getError();
            if (err != 0 && GlErrors.isEnabled(err)) {
                String text = Config.getGlErrorString(err);
                Component msg = Component.literal(I18n.get("of.message.openglError", err, text));
                this.minecraft.gui.hud.getChat().addClientSystemMessage(msg);
            }
        }
    }

    public boolean setFxaaShader(int fxaaLevel) {
        if (!Config.isMainFramebufferEnabled()) {
            return false;
        }

        if (this.postEffectId != null && this.postEffectId != this.fxaaShaders[2] && this.postEffectId != this.fxaaShaders[4]) {
            return true;
        }

        if (fxaaLevel != 2 && fxaaLevel != 4) {
            if (this.postEffectId == null) {
                return true;
            }

            this.postEffectId = null;
            return true;
        } else {
            if (this.postEffectId != null && this.postEffectId == this.fxaaShaders[fxaaLevel]) {
                return true;
            }

            if (this.minecraft.level == null) {
                return true;
            }

            Identifier loc = new Identifier("fxaa_of_" + fxaaLevel + "x");
            this.fxaaShaders[fxaaLevel] = loc;
            this.setPostEffect(loc);
            return this.effectActive;
        }
    }

    public static float getRenderPartialTicks() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public GuiRenderer getGuiRenderer() {
        return this.guiRenderer;
    }

    public int getRenderDistance() {
        return this.minecraft.options.getEffectiveRenderDistance() * 16;
    }

    public SubmitNodeStorage getHandAndScreenSubmitNodeStorage() {
        return this.handAndScreenSubmitNodeStorage;
    }

    public StagedVertexBuffer getHandStagedVertexBuffer() {
        return this.handStagedVertexBuffer;
    }

    public FeatureRenderDispatcher getHandFeatureRenderDispatcher() {
        return this.handFeatureRenderDispatcher;
    }

    public void displayItemActivation(ItemStack itemStack) {
        this.screenEffectRenderer.displayItemActivation(itemStack, this.random);
    }

    public float bossOverlayWorldDarkening(float a) {
        return Mth.lerp(a, this.bossOverlayWorldDarkeningO, this.bossOverlayWorldDarkening);
    }

    public Camera mainCamera() {
        return this.mainCamera;
    }

    public ActiveRenderInfo getActiveRenderInfo() {
        return this.mainCamera;
    }

    public void renderHand(com.mojang.blaze3d.matrix.MatrixStack matrixStack, ActiveRenderInfo activeRenderInfo, float partialTicks, boolean a, boolean b, boolean c) {
    }

    public GpuTextureView lightmap() {
        return this.useUiLightmap ? this.uiLightmap.getTextureView() : this.lightmap.getTextureView();
    }

    public GpuTextureView levelLightmap() {
        return this.lightmap.getTextureView();
    }

    public OverlayTexture overlayTexture() {
        return this.overlayTexture;
    }

    public RenderTarget mainRenderTarget() {
        return this.mainRenderTarget;
    }

    @Override
    public Vec3 projectPointToScreen(Vec3 point) {
        Matrix4f mvp = this.mainCamera.getViewRotationProjectionMatrix(new Matrix4f());
        Vec3 camPos = this.mainCamera.position();
        Vec3 offset = point.subtract(camPos);
        Vector3f vector3f = mvp.transformProject(offset.toVector3f());
        return new Vec3(vector3f);
    }

    @Override
    public double projectHorizonToScreen() {
        float xRot = this.mainCamera.xRot();
        if (xRot <= -90.0F) {
            return Double.NEGATIVE_INFINITY;
        }

        if (xRot >= 90.0F) {
            return Double.POSITIVE_INFINITY;
        }

        float fov = this.mainCamera.getFov();
        return Math.tan(xRot * (float) (Math.PI / 180.0)) / Math.tan(fov / 2.0F * (float) (Math.PI / 180.0));
    }

    public Lighting lighting() {
        return this.lighting;
    }

    public void setLevel(ClientLevel level) {
        if (level != null) {
            this.lighting.updateLevel(level.dimensionType().cardinalLightType());
        }

        this.mainCamera.setLevel(level);
    }

    public Panorama panorama() {
        return this.panorama;
    }

    public void registerPanoramaTextures(TextureManager textureManager) {
        this.guiRenderer.registerPanoramaTextures(textureManager);
    }

    public Object getShaderGroup() {
        return this.postEffectId;
    }
}
