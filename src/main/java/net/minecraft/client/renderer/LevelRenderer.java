package net.minecraft.client.renderer;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SectionUpdateRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.DynamicLights;
import net.optifine.Lagometer;
import net.optifine.SmartAnimations;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import net.optifine.render.ChunkVisibility;
import net.optifine.render.RegionLayerRenderer;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.RenderStage;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.BiomeUtils;
import net.optifine.util.GpuFrameTimer;
import net.optifine.util.GpuMemory;
import net.optifine.util.RandomUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11;

public class LevelRenderer implements AutoCloseable {
    private static final Identifier TRANSPARENCY_POST_CHAIN_ID = Identifier.withDefaultNamespace("transparency");
    private static final Identifier ENTITY_OUTLINE_POST_CHAIN_ID = Identifier.withDefaultNamespace("entity_outline");
    private static final int MINIMUM_TRANSPARENT_SORT_COUNT = 15;
    private static final float CHUNK_VISIBILITY_THRESHOLD = 0.3F;
    private static final Vector4fc SCREEN_SIZE_TARGET_CLEAR_COLOR = new Vector4f(0.0F);
    private static final Vector4fc ENTITY_OUTLINE_CLEAR_COLOR = new Vector4f(0.0F);
    private final GameRenderer gameRenderer;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private final FeatureRenderDispatcher featureRenderDispatcher;
    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final ModelManager modelManager;
    private final TextureManager textureManager;
    private final AtlasManager atlasManager;
    private final ShaderManager shaderManager;
    private final LevelRenderState levelRenderState;
    private final OptionsRenderState optionsRenderState;
    private SkyRenderer skyRenderer;
    private final CloudRenderer cloudRenderer = new CloudRenderer();
    private final WorldBorderRenderer worldBorderRenderer = new WorldBorderRenderer();
    private WeatherEffectRenderer weatherEffectRenderer = new WeatherEffectRenderer();
    private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> nearbyVisibleSections = new ObjectArrayList<>(50);
    private ViewArea viewArea;
    private final RenderTarget entityOutlineTarget;
    private final LevelTargetBundle targets = new LevelTargetBundle();
    private SectionRenderDispatcher sectionRenderDispatcher;
    private BlockPos lastTranslucentSortBlockPos;
    private int translucencyResortIterationIndex;
    private GpuSampler chunkLayerSampler;
    private final SimpleGizmoCollector renderThreadGizmos = new SimpleGizmoCollector();
    private LevelRenderer.FinalizedGizmos finalizedGizmos = new LevelRenderer.FinalizedGizmos(new DrawableGizmoPrimitives(), new DrawableGizmoPrimitives());
    private Set<SectionRenderDispatcher.RenderSection> chunksToResortTransparency = new LinkedHashSet<>();
    private int countChunksToUpdate = 0;
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> renderInfosTerrain = new ObjectArrayList<>(1024);
    private LongOpenHashSet renderInfosEntities = new LongOpenHashSet(1024);
    private List<SectionRenderDispatcher.RenderSection> renderInfosTileEntities = new ArrayList<>(1024);
    private ObjectArrayList renderInfosTerrainNormal = new ObjectArrayList(1024);
    private LongOpenHashSet renderInfosEntitiesNormal = new LongOpenHashSet(1024);
    private List renderInfosTileEntitiesNormal = new ArrayList(1024);
    private ObjectArrayList renderInfosTerrainShadow = new ObjectArrayList(1024);
    private LongOpenHashSet renderInfosEntitiesShadow = new LongOpenHashSet(1024);
    private List renderInfosTileEntitiesShadow = new ArrayList(1024);
    private RenderEnv renderEnv = new RenderEnv(Blocks.AIR.defaultBlockState(), new BlockPos(0, 0, 0));
    private boolean firstWorldLoad = false;
    private static int renderEntitiesCounter = 0;
    public int loadVisibleChunksCounter = -1;
    public static MessageSignature loadVisibleChunksMessageId = new MessageSignature(RandomUtils.getRandomBytes(256));
    private static boolean ambientOcclusion = false;
    private RegionLayerRenderer regionLayerRenderer = new RegionLayerRenderer();
    private int frameId;
    private EnumMap<ChunkSectionLayer, Int2ObjectOpenHashMap<List<RenderPass.Draw<GpuBufferSlice[]>>>> enumMapLayerDraws = new EnumMap<>(
        ChunkSectionLayer.class
    );
    private Minecraft minecraft = Minecraft.getInstance();

    public LevelRenderer(
        EntityRenderDispatcher entityRenderDispatcher,
        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
        ModelManager modelManager,
        TextureManager textureManager,
        AtlasManager atlasManager,
        ShaderManager shaderManager,
        GameRenderer gameRenderer,
        int width,
        int height
    ) {
        this.gameRenderer = gameRenderer;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.renderBuffers = gameRenderer.renderBuffers();
        this.featureRenderDispatcher = gameRenderer.featureRenderDispatcher();
        this.modelManager = modelManager;
        this.textureManager = textureManager;
        this.atlasManager = atlasManager;
        this.shaderManager = shaderManager;
        this.levelRenderState = gameRenderer.gameRenderState().levelRenderState;
        this.optionsRenderState = gameRenderer.gameRenderState().optionsRenderState;
        this.entityOutlineTarget = new TextureTarget("Entity Outline", width, height, true, GpuFormat.RGBA8_UNORM);
        Reflector.ForgeEventFactoryClient_onInitLevelRenderer.call();
    }

    public void render(
        GraphicsResourceAllocator resourceAllocator,
        DeltaTracker deltaTracker,
        boolean renderOutline,
        CameraRenderState cameraState,
        Matrix4fc modelViewMatrix,
        GpuBufferSlice terrainFog,
        Vector4f fogColor,
        boolean shouldRenderSky
    ) {
        float deltaPartialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        final ProfilerFiller profiler = Profiler.get();
        profiler.push("repositionCamera");
        this.repositionCamera(cameraState);
        if (Config.isShaders()) {
            Shaders.setViewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        } else {
            GlStateManager._viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        }

        boolean isShaders = Config.isShaders();
        this.checkLoadVisibleChunks(cameraState, cameraState.cullFrustum, this.minecraft.player.isSpectator());
        this.frameId++;
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(modelViewMatrix);
        profiler.popPush("submitFeatures");
        this.submitFeatures(this.levelRenderState, this.submitNodeStorage, renderOutline);
        profiler.popPush("prepareFeatures");
        FeatureRenderDispatcher.PreparedFrame featureFrame = this.featureRenderDispatcher.prepareFrame(this.submitNodeStorage);
        profiler.popPush("setupFrameGraph");
        FrameGraphBuilder frame = new FrameGraphBuilder();
        this.targets.main = frame.importExternal("main", this.gameRenderer.mainRenderTarget());
        int screenWidth = this.gameRenderer.mainRenderTarget().width;
        int screenHeight = this.gameRenderer.mainRenderTarget().height;
        RenderTargetDescriptor screenSizeTargetDescriptor = new RenderTargetDescriptor(
            screenWidth, screenHeight, true, SCREEN_SIZE_TARGET_CLEAR_COLOR, GpuFormat.RGBA8_UNORM
        );
        PostChain transparencyChain = this.getTransparencyChain();
        if (transparencyChain != null) {
            this.targets.translucent = frame.createInternal("translucent", screenSizeTargetDescriptor);
            this.targets.itemEntity = frame.createInternal("item_entity", screenSizeTargetDescriptor);
            this.targets.particles = frame.createInternal("particles", screenSizeTargetDescriptor);
            this.targets.weather = frame.createInternal("weather", screenSizeTargetDescriptor);
            this.targets.clouds = frame.createInternal("clouds", screenSizeTargetDescriptor);
        }

        this.targets.entityOutline = frame.importExternal("entity_outline", this.entityOutlineTarget);
        FramePass clearPass = frame.addPass("clear");
        this.targets.main = clearPass.readsAndWrites(this.targets.main);
        clearPass.executes(
            () -> {
                RenderTarget mainRenderTarget = this.gameRenderer.mainRenderTarget();
                RenderSystem.getDevice()
                    .createCommandEncoder()
                    .clearColorAndDepthTextures(
                        mainRenderTarget.getColorTexture(), new Vector4f(fogColor.x, fogColor.y, fogColor.z, 0.0F), mainRenderTarget.getDepthTexture(), 0.0
                    );
                if (isShaders) {
                    Shaders.clearRenderBuffer();
                    Shaders.setCamera(modelViewMatrix, RenderSystem.getProjectionMatrix(), Config.getMainCamera(), deltaPartialTick);
                    Shaders.renderPrepare();
                }
            }
        );
        if (shouldRenderSky) {
            if ((Config.isSkyEnabled() || Config.isSunMoonEnabled() || Config.isStarsEnabled()) && !Shaders.isShadowPass) {
                this.addSkyPass(frame, cameraState, terrainFog);
                if (this.skyRenderer != null) {
                    this.skyRenderer.setRenderParameters(this.minecraft.level, deltaPartialTick);
                }
            } else {
                GlStateManager._disableBlend();
            }
        }

        ChunkSectionsToRender chunkSectionsToRender = this.prepareChunkRenders(this.levelRenderState.cameraRenderState.viewRotationMatrix);
        this.addMainPass(frame, featureFrame, terrainFog, this.levelRenderState, profiler, chunkSectionsToRender, modelViewMatrix, deltaPartialTick);
        PostChain entityOutlineChain = this.shaderManager.getPostChain(ENTITY_OUTLINE_POST_CHAIN_ID, LevelTargetBundle.OUTLINE_TARGETS);
        if (featureFrame.hasAnyOutline() && entityOutlineChain != null) {
            entityOutlineChain.addToFrame(frame, screenWidth, screenHeight, this.targets);
        }

        CloudStatus cloudStatus = this.optionsRenderState.cloudStatus;
        if (!Config.isCloudsOff() && cloudStatus != CloudStatus.OFF && ARGB.alpha(this.levelRenderState.cloudColor) > 0) {
            this.levelRenderState.cloudHeight = this.levelRenderState.cloudHeight + (float)this.minecraft.options.ofCloudsHeight * 128.0F;
            this.addCloudsPass(
                frame,
                cloudStatus,
                this.levelRenderState.cameraRenderState.pos,
                this.levelRenderState.gameTime,
                deltaPartialTick,
                this.levelRenderState.cloudColor,
                this.levelRenderState.cloudHeight,
                this.optionsRenderState.cloudRange,
                terrainFog
            );
        }

        this.addWeatherPass(frame, terrainFog);
        if (transparencyChain != null) {
            transparencyChain.addToFrame(frame, screenWidth, screenHeight, this.targets);
        }

        this.addAlwaysOnTopPass(frame, featureFrame, terrainFog);
        if (Reflector.FramePassManager_insertForgePasses.exists()) {
            Reflector.FramePassManager_insertForgePasses.call(frame, this.targets, this.levelRenderState);
        }

        profiler.popPush("executeFrameGraph");
        frame.execute(resourceAllocator, new FrameGraphBuilder.Inspector() {
            @Override
            public void beforeExecutePass(String name) {
                profiler.push(name);
            }

            @Override
            public void afterExecutePass(String name) {
                profiler.pop();
            }
        });
        profiler.pop();
        this.targets.clear();
        modelViewStack.popMatrix();
        featureFrame.close();
        profiler.push("compileSections");
        this.compileSections(cameraState);
        profiler.pop();
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.lock();
            profiler.push("uploadTerrainBuffers");

            try {
                this.sectionRenderDispatcher.uploadTerrainBuffersToGpu();
            } finally {
                this.sectionRenderDispatcher.unlock();
            }

            profiler.pop();
        }

        profiler.push("updateSectionOcclusion");
        this.sectionOcclusionGraph.update(cameraState, this.optionsRenderState.fov, this.levelRenderState.chunkLoadingRenderState);
        profiler.pop();
        Runnable playerCompiledSectionCallback = this.levelRenderState.playerCompiledSectionCallback;
        if (playerCompiledSectionCallback != null && this.isSectionCompiledAndVisible(this.levelRenderState.cameraRenderState.blockPos)) {
            playerCompiledSectionCallback.run();
        }
    }

    private void submitFeatures(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline) {
        PoseStack poseStack = new PoseStack();
        this.submitEntities(poseStack, levelRenderState, submitNodeCollector);
        levelRenderState.entityRenderStates.clear();
        this.submitBlockEntities(poseStack, levelRenderState, submitNodeCollector);
        levelRenderState.blockEntityRenderStates.clear();
        this.submitBlockDestroyAnimation(poseStack, submitNodeCollector, levelRenderState);
        levelRenderState.blockBreakingRenderStates.clear();
        levelRenderState.particlesRenderState.submit(submitNodeCollector, levelRenderState.cameraRenderState);
        if (renderOutline) {
            this.submitBlockOutline(poseStack, this.submitNodeStorage, levelRenderState);
        }

        this.finalizeGizmoCollection();
        this.finalizedGizmos.standardPrimitives().submit(submitNodeCollector, levelRenderState.cameraRenderState, false);
        this.finalizedGizmos.alwaysOnTopPrimitives().submit(submitNodeCollector, levelRenderState.cameraRenderState, true);
        if (!levelRenderState.shouldShowEntityOutlines) {
            for (SubmitNodeCollection collection : this.submitNodeStorage.getSubmitsPerOrder().values()) {
                collection.outline.clear();
            }
        }

        this.checkPoseStack(poseStack);
    }

    private void repositionCamera(CameraRenderState camera) {
        Vec3 cameraPos = camera.pos;
        SectionPos cameraSectionPos = SectionPos.of(cameraPos);
        if (this.viewArea.repositionCamera(cameraSectionPos)) {
            this.worldBorderRenderer.invalidate();
        }

        if (Config.isDynamicLights()) {
            DynamicLights.update(this);
        }

        this.sectionRenderDispatcher.setCameraPosition(cameraPos);
    }

    private void addSkyPass(FrameGraphBuilder frame, CameraRenderState cameraState, GpuBufferSlice skyFog) {
        FogType fogType = cameraState.fogType;
        if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && !cameraState.entityRenderState.doesMobEffectBlockSky) {
            if (this.levelRenderState.shouldResetSkyRenderer || this.skyRenderer == null) {
                if (this.skyRenderer != null) {
                    this.skyRenderer.close();
                }

                this.skyRenderer = new SkyRenderer(this.textureManager, this.atlasManager, this.gameRenderer.mainRenderTarget());
                Config.getLevelExtractor().setShouldResetSkyRenderer(false);
            }

            SkyRenderState state = this.levelRenderState.skyRenderState;
            if (state.skybox != DimensionType.Skybox.NONE) {
                FramePass pass = frame.addPass("sky");
                this.targets.main = pass.readsAndWrites(this.targets.main);
                pass.executes(
                    () -> {
                        boolean isShaders = Config.isShaders();
                        if (isShaders) {
                            Shaders.beginSky();
                        }

                        RenderSystem.setShaderFog(skyFog);
                        if (state.skybox == DimensionType.Skybox.END) {
                            this.skyRenderer.renderEndSky();
                            if (state.endFlashIntensity > 1.0E-5F) {
                                PoseStack poseStack = new PoseStack();
                                this.skyRenderer.renderEndFlash(poseStack, state.endFlashIntensity, state.endFlashXAngle, state.endFlashYAngle);
                            }
                        } else {
                            PoseStack poseStack = new PoseStack();
                            if (isShaders) {
                                Shaders.disableTexture2D();
                            }

                            state.skyColor = CustomColors.getSkyColor(
                                state.skyColor,
                                this.minecraft.level,
                                this.minecraft.getCameraEntity().getX(),
                                this.minecraft.getCameraEntity().getY() + 1.0,
                                this.minecraft.getCameraEntity().getZ()
                            );
                            if (isShaders) {
                                Shaders.setSkyColor(state.skyColor);
                                RenderSystem.setColorToAttribute(true);
                                Shaders.enableFog();
                                Shaders.preSkyList(poseStack);
                            }

                            if (Config.isSkyEnabled()) {
                                this.skyRenderer.renderSkyDisc(state.skyColor);
                            }

                            if (isShaders) {
                                Shaders.disableFog();
                            }

                            if (Config.isSunMoonEnabled()) {
                                if (isShaders) {
                                    Shaders.disableTexture2D();
                                }

                                if (isShaders) {
                                    Shaders.setRenderStage(RenderStage.SUNSET);
                                }

                                this.skyRenderer.renderSunriseAndSunset(poseStack, state.sunAngle, state.sunriseAndSunsetColor);
                                if (isShaders) {
                                    Shaders.enableTexture2D();
                                }

                                this.skyRenderer
                                    .renderSunMoonAndStars(
                                        poseStack,
                                        state.sunAngle,
                                        state.moonAngle,
                                        state.starAngle,
                                        state.moonPhase,
                                        state.rainBrightness,
                                        state.starBrightness
                                    );
                                if (isShaders) {
                                    Shaders.disableTexture2D();
                                }
                            }

                            if (state.shouldRenderDarkDisc) {
                                if (isShaders) {
                                    Shaders.setRenderStage(RenderStage.VOID);
                                }

                                this.skyRenderer.renderDarkDisc();
                            }

                            if (isShaders) {
                                RenderSystem.setColorToAttribute(false);
                            }
                        }

                        if (isShaders) {
                            Shaders.endSky();
                        }
                    }
                );
            }
        }
    }

    private void addMainPass(
        FrameGraphBuilder frame,
        FeatureRenderDispatcher.PreparedFrame featureFrame,
        GpuBufferSlice terrainFog,
        LevelRenderState levelRenderState,
        ProfilerFiller profiler,
        ChunkSectionsToRender chunkSectionsToRender
    ) {
        this.addMainPass(frame, featureFrame, terrainFog, levelRenderState, profiler, chunkSectionsToRender, null, 0.0F);
    }

    private void addMainPass(
        FrameGraphBuilder frame,
        FeatureRenderDispatcher.PreparedFrame featureFrame,
        GpuBufferSlice terrainFog,
        LevelRenderState levelRenderState,
        ProfilerFiller profiler,
        ChunkSectionsToRender chunkSectionsToRender,
        Matrix4fc modelView,
        float partialTicks
    ) {
        FramePass pass = frame.addPass("main");
        this.targets.main = pass.readsAndWrites(this.targets.main);
        if (this.targets.translucent != null) {
            this.targets.translucent = pass.readsAndWrites(this.targets.translucent);
        }

        if (this.targets.itemEntity != null) {
            this.targets.itemEntity = pass.readsAndWrites(this.targets.itemEntity);
        }

        if (this.targets.weather != null) {
            this.targets.weather = pass.readsAndWrites(this.targets.weather);
        }

        if (this.targets.particles != null) {
            this.targets.particles = pass.readsAndWrites(this.targets.particles);
        }

        if (featureFrame.hasAnyOutline() && this.targets.entityOutline != null) {
            this.targets.entityOutline = pass.readsAndWrites(this.targets.entityOutline);
        }

        ResourceHandle<RenderTarget> mainTarget = this.targets.main;
        ResourceHandle<RenderTarget> translucentTarget = this.targets.translucent;
        ResourceHandle<RenderTarget> itemEntityTarget = this.targets.itemEntity;
        ResourceHandle<RenderTarget> entityOutlineTarget = this.targets.entityOutline;
        ResourceHandle<RenderTarget> particleTarget = this.targets.particles;
        pass.executes(
            () -> {
                RenderSystem.setShaderFog(terrainFog);
                Lagometer.timerTerrain.start();
                if (this.minecraft.options.ofSmoothFps) {
                    profiler.popPush("finish");
                    GL11.glFinish();
                    profiler.popPush("terrain");
                }

                boolean isShaders = Config.isShaders();
                Camera mainCamera = this.minecraft.gameRenderer.mainCamera();
                if (levelRenderState.shouldResetChunkLayerSampler || this.chunkLayerSampler == null) {
                    if (this.chunkLayerSampler != null) {
                        this.chunkLayerSampler.close();
                    }

                    int maxAnisotropy = this.optionsRenderState.textureFiltering == TextureFilteringMethod.ANISOTROPIC
                        ? this.optionsRenderState.maxAnisotropyValue
                        : 1;
                    FilterMode filterMode = Config.isShaders() ? FilterMode.NEAREST : FilterMode.LINEAR;
                    this.chunkLayerSampler = RenderSystem.getDevice()
                        .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, filterMode, filterMode, maxAnisotropy, OptionalDouble.empty());
                }

                profiler.push("solidTerrain");
                if (isShaders) {
                    ShadersRender.beginTerrainSolid();
                }

                chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.OPAQUE, this.chunkLayerSampler);
                if (isShaders) {
                    ShadersRender.endTerrain();
                }

                Lagometer.timerTerrain.end();
                this.gameRenderer.lighting().setupFor(Lighting.Entry.LEVEL);
                if (levelRenderState.shouldShowEntityOutlines && entityOutlineTarget != null) {
                    RenderTarget outlineTarget = entityOutlineTarget.get();
                    RenderSystem.getDevice()
                        .createCommandEncoder()
                        .clearColorAndDepthTextures(outlineTarget.getColorTexture(), ENTITY_OUTLINE_CLEAR_COLOR, outlineTarget.getDepthTexture(), 0.0);
                }

                ItemFrameRenderer.updateItemRenderDistance();
                AbstractSignRenderer.updateTextRenderDistance();
                renderEntitiesCounter++;
                profiler.popPush("renderSolidFeatures");
                featureFrame.executeSolid();
                profiler.pop();
                if (translucentTarget != null) {
                    translucentTarget.get().copyDepthFrom(mainTarget.get());
                }

                if (itemEntityTarget != null) {
                    itemEntityTarget.get().copyDepthFrom(mainTarget.get());
                }

                if (particleTarget != null) {
                    particleTarget.get().copyDepthFrom(mainTarget.get());
                }

                if (isShaders) {
                    Shaders.preRenderHand();
                    RenderSystem.backupProjectionMatrix();
                    Shaders.beginRenderHand(false);
                    RenderSystem.setProjectionMatrix(
                        this.minecraft.gameRenderer.hud3dProjectionMatrixBuffer.getBuffer(this.minecraft.gameRenderer.hudProjection),
                        ProjectionType.PERSPECTIVE
                    );
                    ShadersRender.renderHandSolid(Config.getGameRenderer(), levelRenderState.cameraRenderState, modelView, partialTicks);
                    Shaders.endRenderHand();
                    RenderSystem.restoreProjectionMatrix();
                }

                if (Shaders.isParticlesBeforeDeferred()) {
                    profiler.popPush("particles");
                    RenderSystem.setShaderFog(terrainFog);
                    levelRenderState.particlesRenderState.submit(this.submitNodeStorage, this.levelRenderState.cameraRenderState);
                    this.gameRenderer.getHandFeatureRenderDispatcher().renderAllFeatures(this.submitNodeStorage);
                    levelRenderState.particlesRenderState.reset();
                }

                if (isShaders) {
                    Shaders.preWater();
                }

                profiler.push("renderTranslucentFeatures");
                featureFrame.executeTranslucent();
                profiler.pop();
                renderEntitiesCounter--;
                featureFrame.executeOutline();
                profiler.push("translucentTerrain");
                Lagometer.timerTerrain.start();
                if (isShaders) {
                    Shaders.beginWater();
                }

                chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.TRANSLUCENT, this.chunkLayerSampler);
                if (isShaders) {
                    Shaders.endWater();
                }

                Lagometer.timerTerrain.end();
                profiler.pop();
                featureFrame.executeTranslucentAfterTerrain();
            }
        );
    }

    private void addCloudsPass(
        FrameGraphBuilder frame,
        CloudStatus cloudStatus,
        Vec3 cameraPosition,
        long gameTime,
        float partialTicks,
        int cloudColor,
        float cloudHeight,
        int cloudRange
    ) {
        this.addCloudsPass(frame, cloudStatus, cameraPosition, gameTime, partialTicks, cloudColor, cloudHeight, cloudRange, null);
    }

    private void addCloudsPass(
        FrameGraphBuilder frame,
        CloudStatus cloudStatus,
        Vec3 cameraPosition,
        long gameTime,
        float partialTicks,
        int cloudColor,
        float cloudHeight,
        int cloudRange,
        GpuBufferSlice fogBufferIn
    ) {
        FramePass pass = frame.addPass("clouds");
        if (this.targets.clouds != null) {
            this.targets.clouds = pass.readsAndWrites(this.targets.clouds);
        } else {
            this.targets.main = pass.readsAndWrites(this.targets.main);
        }

        pass.executes(() -> this.cloudRenderer.render(cloudColor, cloudStatus, cloudHeight, cloudRange, cameraPosition, gameTime, partialTicks, fogBufferIn));
    }

    private void addWeatherPass(FrameGraphBuilder frame, GpuBufferSlice fog) {
        int renderDistance = this.optionsRenderState.renderDistance * 16;
        FramePass pass = frame.addPass("weather");
        if (this.targets.weather != null) {
            this.targets.weather = pass.readsAndWrites(this.targets.weather);
        } else {
            this.targets.main = pass.readsAndWrites(this.targets.main);
        }

        pass.executes(
            () -> {
                RenderSystem.setShaderFog(fog);
                CameraRenderState cameraState = this.levelRenderState.cameraRenderState;
                if (Config.isShaders()) {
                    Shaders.beginWeather();
                }

                this.weatherEffectRenderer.render(cameraState.pos, this.levelRenderState.weatherRenderState);
                if (Config.isShaders()) {
                    Shaders.endWeather();
                }

                this.worldBorderRenderer
                    .render(this.levelRenderState.worldBorderRenderState, cameraState.pos, renderDistance, this.levelRenderState.cameraRenderState.depthFar);
            }
        );
    }

    private void addAlwaysOnTopPass(FrameGraphBuilder frame, FeatureRenderDispatcher.PreparedFrame featureFrame, GpuBufferSlice fog) {
        if (featureFrame.hasAnyAlwaysOnTop()) {
            FramePass pass = frame.addPass("always_on_top");
            this.targets.main = pass.readsAndWrites(this.targets.main);
            if (this.targets.itemEntity != null) {
                this.targets.itemEntity = pass.readsAndWrites(this.targets.itemEntity);
            }

            ResourceHandle<RenderTarget> mainTarget = this.targets.main;
            pass.executes(() -> {
                RenderSystem.setShaderFog(fog);
                PoseStack poseStack = new PoseStack();
                RenderTarget mainRenderTarget = mainTarget.get();
                RenderSystem.outputColorTextureOverride = mainRenderTarget.getColorTextureView();
                RenderSystem.outputDepthTextureOverride = mainRenderTarget.getDepthTextureView();
                if (!Config.isShaders()) {
                    RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(mainRenderTarget.getDepthTexture(), 0.0);
                }

                featureFrame.executeAlwaysOnTop();
                RenderSystem.outputColorTextureOverride = null;
                RenderSystem.outputDepthTextureOverride = null;
                this.checkPoseStack(poseStack);
            });
        }
    }

    public ChunkSectionsToRender prepareChunkRenders(Matrix4fc modelViewMatrix) {
        ObjectListIterator<SectionRenderDispatcher.RenderSection> iterator = this.renderInfosTerrain.listIterator(0);
        EnumMap<ChunkSectionLayer, Int2ObjectOpenHashMap<List<RenderPass.Draw<GpuBufferSlice[]>>>> drawGroups = this.enumMapLayerDraws;
        int largestIndexCount = 0;

        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            Int2ObjectOpenHashMap<List<RenderPass.Draw<GpuBufferSlice[]>>> draws = drawGroups.get(layer);
            if (draws != null) {
                for (List list : draws.values()) {
                    list.clear();
                }
            } else {
                drawGroups.put(layer, new Int2ObjectOpenHashMap<>());
            }
        }

        List<DynamicUniforms.ChunkSectionInfo> sectionInfos = new ArrayList<>();
        GpuTextureView blockAtlas = this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        int textureAtlasWidth = blockAtlas.getWidth(0);
        int textureAtlasHeight = blockAtlas.getHeight(0);
        boolean renderRegions = Config.isRenderRegions();
        if (renderRegions) {
            this.regionLayerRenderer.reset();
        }

        boolean smartAnimations = SmartAnimations.isActive();
        Vec3 camPos = Config.getMainCamera().position();
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.lock();

            try {
                while (iterator.hasNext()) {
                    SectionRenderDispatcher.RenderSection section = iterator.next();
                    SectionMesh sectionMesh = section.getSectionMesh();
                    BlockPos renderOffset = section.getRenderOrigin();
                    long now = Util.getMillis();
                    int uboIndex = -1;

                    for (ChunkSectionLayer layer : ChunkSectionLayer.VALUES) {
                        SectionMesh.SectionDraw draw = sectionMesh.getSectionDraw(layer);
                        SectionRenderDispatcher.RenderSectionBufferSlice slice = this.sectionRenderDispatcher.getRenderSectionSlice(sectionMesh, layer);
                        if (slice != null && draw != null && (!draw.hasCustomIndexBuffer() || slice.indexBuffer() != null)) {
                            if (smartAnimations) {
                                BitSet animatedSprites = sectionMesh.getAnimatedSprites(layer);
                                if (animatedSprites != null) {
                                    SmartAnimations.spritesRendered(animatedSprites);
                                }
                            }

                            if (renderRegions && draw.data().vboRegion != null) {
                                this.regionLayerRenderer.addSection(layer, section.regionX, section.regionZ, draw);
                            } else {
                                if (uboIndex == -1) {
                                    uboIndex = sectionInfos.size();
                                    sectionInfos.add(
                                        new DynamicUniforms.ChunkSectionInfo(
                                            new Matrix4f(modelViewMatrix),
                                            renderOffset.getX(),
                                            renderOffset.getY(),
                                            renderOffset.getZ(),
                                            section.getVisibility(now),
                                            textureAtlasWidth,
                                            textureAtlasHeight,
                                            camPos.x,
                                            camPos.y,
                                            camPos.z
                                        )
                                    );
                                }

                                int combinedHash = 173;
                                VertexFormat vertexFormat = layer.pipeline().getVertexFormatBinding(0);
                                GpuBuffer vertexBuffer = slice.vertexBuffer();
                                if (layer != ChunkSectionLayer.TRANSLUCENT) {
                                    combinedHash = 31 * combinedHash + vertexBuffer.hashCode();
                                }

                                int firstIndex = 0;
                                GpuBuffer indexBuffer;
                                IndexType indexType;
                                if (!draw.hasCustomIndexBuffer()) {
                                    if (draw.indexCount() > largestIndexCount) {
                                        largestIndexCount = draw.indexCount();
                                    }

                                    indexBuffer = null;
                                    indexType = null;
                                } else {
                                    indexBuffer = slice.indexBuffer();
                                    indexType = draw.indexType();
                                    if (layer != ChunkSectionLayer.TRANSLUCENT) {
                                        combinedHash = 31 * combinedHash + indexBuffer.hashCode();
                                        combinedHash = 31 * combinedHash + indexType.hashCode();
                                    }

                                    firstIndex = (int)(slice.indexBufferOffset() / indexType.bytes);
                                }

                                int finalUboIndex = uboIndex;
                                int baseVertex = (int)(slice.vertexBufferOffset() / vertexFormat.getVertexSize());
                                List<RenderPass.Draw<GpuBufferSlice[]>> draws = drawGroups.get(layer).computeIfAbsent(combinedHash, var0 -> new ArrayList<>());
                                draws.add(
                                    new RenderPass.Draw<>(
                                        0,
                                        vertexBuffer,
                                        indexBuffer,
                                        indexType,
                                        firstIndex,
                                        draw.indexCount(),
                                        baseVertex,
                                        (sectionUbos, uploader) -> uploader.upload("ChunkSection", sectionUbos[finalUboIndex]),
                                        null,
                                        draw.data().multiTextureData
                                    )
                                );
                            }
                        }
                    }
                }
            } finally {
                this.sectionRenderDispatcher.unlock();
            }
        }

        if (renderRegions) {
            this.regionLayerRenderer.finishPrepare(modelViewMatrix, camPos.x, camPos.y, camPos.z, sectionInfos, drawGroups);
        }

        GpuBufferSlice[] chunkSectionInfos = RenderSystem.getDynamicUniforms()
            .writeChunkSections(sectionInfos.toArray(new DynamicUniforms.ChunkSectionInfo[0]));
        return new ChunkSectionsToRender(blockAtlas, drawGroups, largestIndexCount, chunkSectionInfos);
    }

    private void compileSections(CameraRenderState camera) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("populateSectionsToCompile");
        BlockPos cameraPosition = camera.blockPos;
        long fadeDuration = Mth.floor(this.optionsRenderState.chunkSectionFadeInTime * 1000.0);
        Lagometer.timerChunkUpdate.start();

        for (SectionUpdateRenderState state : this.levelRenderState.sectionUpdateRenderStates) {
            BlockPos center = SectionPos.of(state.sectionNode()).center();
            double distSqr = center.distSqr(cameraPosition);
            boolean isNearby = distSqr < 768.0;
            boolean rebuildSync = false;
            if (this.optionsRenderState.prioritizeChunkUpdates == PrioritizeChunkUpdates.NEARBY) {
                rebuildSync = isNearby || state.playerChanged();
            } else if (this.optionsRenderState.prioritizeChunkUpdates == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                rebuildSync = state.playerChanged();
            }

            SectionRenderDispatcher.RenderSection section = this.viewArea.getRenderSection(state.sectionNode());
            if (section.needsBackgroundPriorityUpdate()) {
                section.compileAsync(state.region());
            } else {
                if (!isNearby && !section.wasPreviouslyEmpty()) {
                    section.setFadeDuration(fadeDuration);
                } else {
                    section.setFadeDuration(0L);
                }

                section.setWasPreviouslyEmpty(false);
                if (rebuildSync) {
                    profiler.push("compileSectionSynchronously");
                    section.compileSync(state.region());
                    profiler.pop();
                } else {
                    section.compileAsync(state.region());
                }
            }
        }

        Lagometer.timerChunkUpdate.end();
        Lagometer.timerChunkUpload.start();
        Lagometer.timerChunkUpload.end();
        profiler.popPush("scheduleTranslucentResort");
        this.scheduleTranslucentSectionResort(camera.pos);
        profiler.pop();
    }

    public void checkPoseStack(PoseStack poseStack) {
        if (!poseStack.isEmpty()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    public void submitEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector output) {
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        for (EntityRenderState state : levelRenderState.entityRenderStates) {
            this.entityRenderDispatcher.submit(state, levelRenderState.cameraRenderState, state.x - camX, state.y - camY, state.z - camZ, poseStack, output);
        }
    }

    public void submitBlockEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        for (BlockEntityRenderState renderState : levelRenderState.blockEntityRenderStates) {
            BlockPos blockPos = renderState.blockPos;
            poseStack.pushPose();
            poseStack.translate(blockPos.getX() - camX, blockPos.getY() - camY, blockPos.getZ() - camZ);
            this.blockEntityRenderDispatcher.submit(renderState, poseStack, submitNodeCollector, levelRenderState.cameraRenderState);
            poseStack.popPose();
        }
    }

    private void submitBlockDestroyAnimation(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState) {
        if (!levelRenderState.blockBreakingRenderStates.isEmpty()) {
            Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
            double camX = cameraPos.x();
            double camY = cameraPos.y();
            double camZ = cameraPos.z();
            List<BlockStateModelPart> parts = new ArrayList<>();
            RandomSource random = RandomSource.createThreadLocalInstance();

            for (BlockBreakingRenderState state : levelRenderState.blockBreakingRenderStates) {
                if (state.blockState().getRenderShape() == RenderShape.MODEL) {
                    BlockPos pos = state.blockPos();
                    poseStack.pushPose();
                    poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);
                    poseStack.translate(state.blockState().getOffset(pos));
                    BlockStateModel model = this.modelManager.getBlockStateModelSet().get(state.blockState());
                    random.setSeed(state.blockState().getSeed(pos));
                    model.collectParts(random, parts);
                    submitNodeCollector.submitBreakingBlockModel(poseStack, List.copyOf(parts), state.progress());
                    parts.clear();
                    poseStack.popPose();
                }
            }
        }
    }

    private void submitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState) {
        BlockOutlineRenderState state = levelRenderState.blockOutlineRenderState;
        if (state != null) {
            if (state.customRenderer() != null) {
                state.customRenderer().render(submitNodeCollector, poseStack, levelRenderState);
                return;
            }

            Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
            BlockPos pos = state.pos();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
            if (state.highContrast()) {
                this.submitHitOutline(poseStack, submitNodeCollector, RenderTypes.secondaryBlockOutline(), state, -16777216, 7.0F, state.isTranslucent());
            }

            int outlineColor = state.highContrast() ? -11010079 : ARGB.black(102);
            this.submitHitOutline(
                poseStack,
                submitNodeCollector,
                RenderTypes.lines(),
                state,
                outlineColor,
                this.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth,
                state.isTranslucent()
            );
            poseStack.popPose();
        }
    }

    private void submitHitOutline(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        RenderType renderType,
        BlockOutlineRenderState state,
        int color,
        float width,
        boolean afterTerrain
    ) {
        if (!Config.isCustomEntityModels() || !CustomEntityModels.isCustomModel(state.blockState())) {
            if (SharedConstants.DEBUG_SHAPES) {
                submitNodeCollector.submitShapeOutline(poseStack, state.shape(), renderType, -1, width, afterTerrain);
                if (state.collisionShape() != null) {
                    submitNodeCollector.submitShapeOutline(
                        poseStack, state.collisionShape(), renderType, ARGB.colorFromFloat(0.4F, 0.0F, 0.0F, 0.0F), width, afterTerrain
                    );
                }

                if (state.occlusionShape() != null) {
                    submitNodeCollector.submitShapeOutline(
                        poseStack, state.occlusionShape(), renderType, ARGB.colorFromFloat(0.4F, 0.0F, 1.0F, 0.0F), width, afterTerrain
                    );
                }

                if (state.interactionShape() != null) {
                    submitNodeCollector.submitShapeOutline(
                        poseStack, state.interactionShape(), renderType, ARGB.colorFromFloat(0.4F, 0.0F, 0.0F, 1.0F), width, afterTerrain
                    );
                }
            } else {
                submitNodeCollector.submitShapeOutline(poseStack, state.shape(), renderType, color, width, afterTerrain);
            }
        }
    }

    public void resize(int width, int height) {
        this.sectionOcclusionGraph.invalidate();
        this.entityOutlineTarget.resize(width, height);
    }

    public void endFrame() {
        GpuFrameTimer.finishRender();
        this.cloudRenderer.endFrame();
    }

    @Override
    public void close() {
        this.resetLevelRenderData();
        this.entityOutlineTarget.destroyBuffers();
        if (this.skyRenderer != null) {
            this.skyRenderer.close();
        }

        if (this.chunkLayerSampler != null) {
            this.chunkLayerSampler.close();
        }

        this.worldBorderRenderer.close();
        this.cloudRenderer.close();
        this.weatherEffectRenderer.close();
    }

    public void doEntityOutline() {
        if (this.levelRenderState.shouldShowEntityOutlines) {
            this.entityOutlineTarget
                .blitAndBlendToTexture(this.gameRenderer.mainRenderTarget().getColorTextureView(), this.gameRenderer.mainRenderTarget().getDepthTextureView());
        }
    }

    public void invalidateCompiledGeometry(ClientLevel level, Options options, Camera camera, BlockColors blockColors) {
        SectionCompiler sectionCompiler = new SectionCompiler(
            options.ambientOcclusion().get(),
            options.cutoutLeaves().get(),
            this.modelManager.getBlockStateModelSet(),
            this.modelManager.getFluidStateModelSet(),
            blockColors
        );
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.dispose();
            this.sectionRenderDispatcher = null;
        }

        if (this.sectionRenderDispatcher == null) {
            this.sectionRenderDispatcher = new SectionRenderDispatcher(
                Util.backgroundExecutor(), this.renderBuffers, sectionCompiler, this.sectionOcclusionGraph::schedulePropagationFrom
            );
        } else {
            this.sectionRenderDispatcher.setCompiler(sectionCompiler);
        }

        this.cloudRenderer().markForRebuild();
        LeavesBlock.setCutoutLeaves(Config.isTreesFancy());
        ModelBlockRenderer.updateAoLightValue();
        if (Config.isDynamicLights()) {
            DynamicLights.clear();
        }

        SmartAnimations.update();
        ambientOcclusion = Config.getGameSettings().ambientOcclusion().get();
        if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
        }

        GpuMemory.bufferFreed(GpuMemory.getBufferAllocated());
        this.sectionRenderDispatcher.clearCompileQueue();
        this.viewArea = new ViewArea(
            this.sectionRenderDispatcher,
            level.getMinY(),
            level.getMaxY(),
            level.getMinSectionY(),
            level.getMaxSectionY(),
            options.getEffectiveRenderDistance(),
            this.sectionOcclusionGraph
        );
        this.sectionOcclusionGraph().waitAndReset(this.viewArea);
        this.clearVisibleSections();
        this.clearRenderInfos();
        camera.killFrustum();
        SectionPos cameraSectionPos = SectionPos.of(camera.position());
        this.viewArea.repositionCamera(cameraSectionPos);
    }

    private PostChain getTransparencyChain() {
        return !this.gameRenderer.gameRenderState().useShaderTransparency()
            ? null
            : this.shaderManager.getPostChain(TRANSPARENCY_POST_CHAIN_ID, LevelTargetBundle.SORTING_TARGETS);
    }

    private void scheduleTranslucentSectionResort(Vec3 cameraPos) {
        if (!this.visibleSections.isEmpty()) {
            BlockPos cameraBlockPos = BlockPos.containing(cameraPos);
            boolean blockPosChanged = !cameraBlockPos.equals(this.lastTranslucentSortBlockPos);
            TranslucencyPointOfView pointOfView = new TranslucencyPointOfView();

            for (SectionRenderDispatcher.RenderSection section : this.nearbyVisibleSections) {
                this.scheduleResort(section, pointOfView, cameraPos, blockPosChanged, true);
            }

            this.translucencyResortIterationIndex = this.translucencyResortIterationIndex % this.visibleSections.size();
            int resortsLeft = Math.max(this.visibleSections.size() / 8, 15);

            while (resortsLeft-- > 0) {
                int index = this.translucencyResortIterationIndex++ % this.visibleSections.size();
                this.scheduleResort(this.visibleSections.get(index), pointOfView, cameraPos, blockPosChanged, false);
            }

            this.lastTranslucentSortBlockPos = cameraBlockPos;
        }
    }

    private void scheduleResort(
        SectionRenderDispatcher.RenderSection section, TranslucencyPointOfView pointOfView, Vec3 cameraPos, boolean blockPosChanged, boolean isNearby
    ) {
        pointOfView.set(cameraPos, section.getSectionNode());
        boolean pointOfViewChanged = section.getSectionMesh().isDifferentPointOfView(pointOfView);
        boolean resortBecauseBlockPosChanged = blockPosChanged && (pointOfView.isAxisAligned() || isNearby);
        if ((resortBecauseBlockPosChanged || pointOfViewChanged) && !section.transparencyResortingScheduled() && section.hasTranslucentGeometry()) {
            section.resortTransparency();
        }
    }

    public void clearVisibleSections() {
        this.visibleSections.clear();
        this.nearbyVisibleSections.clear();
        this.clearRenderInfos();
    }

    public void resetLevelRenderData() {
        if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
            this.viewArea = null;
        }

        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.dispose();
        }

        this.sectionRenderDispatcher = null;
        this.sectionOcclusionGraph.waitAndReset(null);
        this.clearVisibleSections();
    }

    public boolean hasRenderedAllSections() {
        return this.sectionRenderDispatcher == null || this.sectionRenderDispatcher.isQueueEmpty();
    }

    public boolean isSectionCompiledAndVisible(BlockPos blockPos) {
        if (this.viewArea == null) {
            return false;
        }

        SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSectionAt(blockPos);
        return renderSection != null && renderSection.sectionMesh.get() != CompiledSectionMesh.UNCOMPILED
            ? renderSection.getVisibility(Util.getMillis()) >= 0.3F
            : false;
    }

    public int getCountRenderers() {
        return this.viewArea.size();
    }

    public int getCountEntitiesRendered() {
        return this.levelRenderState.entityRenderStates.size();
    }

    public int getCountTileEntitiesRendered() {
        return this.levelRenderState.blockEntityRenderStates.size();
    }

    public int getCountLoadedChunks() {
        if (this.getWorld() == null) {
            return 0;
        }

        ClientChunkCache chunkProvider = this.getWorld().getChunkSource();
        return chunkProvider == null ? 0 : chunkProvider.getLoadedChunksCount();
    }

    public int getCountChunksToUpdate() {
        return this.countChunksToUpdate;
    }

    public SectionRenderDispatcher.RenderSection getRenderChunk(BlockPos pos) {
        return this.viewArea.getRenderSectionAt(pos);
    }

    public SectionRenderDispatcher.RenderSection getRenderChunk(long pos) {
        return this.viewArea.getRenderSection(pos);
    }

    public ClientLevel getWorld() {
        return this.minecraft.level;
    }

    private void clearRenderInfos() {
        this.clearRenderInfosTerrain();
        this.clearRenderInfosEntities();
    }

    public void clearRenderInfosTerrain() {
        if (renderEntitiesCounter > 0) {
            this.renderInfosTerrain = new ObjectArrayList<>(this.renderInfosTerrain.size() + 16);
            this.renderInfosTileEntities = new ArrayList<>(this.renderInfosTileEntities.size() + 16);
        } else {
            this.renderInfosTerrain.clear();
            this.renderInfosTileEntities.clear();
        }
    }

    private void clearRenderInfosEntities() {
        if (renderEntitiesCounter > 0) {
            this.renderInfosEntities = new LongOpenHashSet(this.renderInfosEntities.size() + 16);
        } else {
            this.renderInfosEntities.clear();
        }
    }

    public void onPlayerPositionSet() {
        if (this.firstWorldLoad) {
            Minecraft.getInstance().levelExtractor.allChanged();
            this.firstWorldLoad = false;
        }
    }

    public void pauseChunkUpdates() {
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.pauseChunkUpdates();
        }
    }

    public void resumeChunkUpdates() {
        if (this.sectionRenderDispatcher != null) {
            this.sectionRenderDispatcher.resumeChunkUpdates();
        }
    }

    public int getFrameCount() {
        return this.frameId;
    }

    public RenderBuffers getRenderTypeTextures() {
        return this.renderBuffers;
    }

    public LongOpenHashSet getRenderChunksEntities() {
        return this.renderInfosEntities;
    }

    private void addEntitySection(LongOpenHashSet set, EntitySectionStorage storage, BlockPos pos) {
        long sectionPos = SectionPos.asLong(pos);
        EntitySection es = storage.getSection(sectionPos);
        if (es != null) {
            set.add(sectionPos);
        }
    }

    private boolean hasEntitySection(EntitySectionStorage storage, BlockPos pos) {
        long sectionPos = SectionPos.asLong(pos);
        EntitySection es = storage.getSection(sectionPos);
        return es != null;
    }

    public List<SectionRenderDispatcher.RenderSection> getRenderInfos() {
        return this.visibleSections;
    }

    public List<SectionRenderDispatcher.RenderSection> getRenderInfosTerrain() {
        return this.renderInfosTerrain;
    }

    public LongOpenHashSet getRenderInfosEntities() {
        return this.renderInfosEntities;
    }

    public List<SectionRenderDispatcher.RenderSection> getRenderInfosTileEntities() {
        return this.renderInfosTileEntities;
    }

    private void checkLoadVisibleChunks(CameraRenderState cameraState, Frustum icamera, boolean spectator) {
        if (this.loadVisibleChunksCounter == 0) {
            this.loadAllVisibleChunks(cameraState, icamera, spectator);
            this.minecraft.gui.hud.getChat().deleteMessage(loadVisibleChunksMessageId);
        }

        if (this.loadVisibleChunksCounter >= 0) {
            this.loadVisibleChunksCounter--;
        }
    }

    private void loadAllVisibleChunks(CameraRenderState cameraState, Frustum icamera, boolean spectator) {
        int chunkUpdatesConfig = this.minecraft.options.ofChunkUpdates;
        boolean lazyChunkLoadingConfig = this.minecraft.options.ofLazyChunkLoading;

        try {
            this.minecraft.options.ofChunkUpdates = 1000;
            this.minecraft.options.ofLazyChunkLoading = false;
            LevelRenderer renderGlobal = Config.getRenderGlobal();
            int countLoadedChunks = renderGlobal.getCountLoadedChunks();
            long timeStart = System.currentTimeMillis();
            Config.dbg("Loading visible chunks");
            long timeLog = System.currentTimeMillis() + 5000L;
            int chunksUpdated = 0;
            boolean hasUpdates = false;

            do {
                hasUpdates = false;

                for (int i = 0; i < 100; i++) {
                    renderGlobal.needsUpdate();
                    Config.getLevelExtractor().extract(this.minecraft.getDeltaTracker(), Config.getMainCamera(), 0.0F);
                    Config.sleep(1L);
                    this.compileSections(cameraState);
                    if (renderGlobal.getCountChunksToUpdate() > 0) {
                        hasUpdates = true;
                    }

                    if (!renderGlobal.hasRenderedAllSections()) {
                        hasUpdates = true;
                    }

                    chunksUpdated += renderGlobal.getCountChunksToUpdate();

                    while (!renderGlobal.hasRenderedAllSections()) {
                        int countUpdates = renderGlobal.getCountChunksToUpdate();
                        this.compileSections(cameraState);
                        if (countUpdates == renderGlobal.getCountChunksToUpdate()) {
                            break;
                        }
                    }

                    chunksUpdated -= renderGlobal.getCountChunksToUpdate();
                    if (!hasUpdates) {
                        break;
                    }
                }

                if (renderGlobal.getCountLoadedChunks() != countLoadedChunks) {
                    hasUpdates = true;
                    countLoadedChunks = renderGlobal.getCountLoadedChunks();
                }

                if (System.currentTimeMillis() > timeLog) {
                    Config.log("Chunks loaded: " + chunksUpdated);
                    timeLog = System.currentTimeMillis() + 5000L;
                }
            } while (hasUpdates);

            Config.log("Chunks loaded: " + chunksUpdated);
            Config.log("Finished loading visible chunks");
            SectionRenderDispatcher.renderChunksUpdated = 0;
        } finally {
            this.minecraft.options.ofChunkUpdates = chunkUpdatesConfig;
            this.minecraft.options.ofLazyChunkLoading = lazyChunkLoadingConfig;
        }
    }

    public void setShadowRenderInfos(boolean shadowInfos) {
        if (shadowInfos) {
            this.renderInfosTerrain = this.renderInfosTerrainShadow;
            this.renderInfosEntities = this.renderInfosEntitiesShadow;
            this.renderInfosTileEntities = this.renderInfosTileEntitiesShadow;
        } else {
            this.renderInfosTerrain = this.renderInfosTerrainNormal;
            this.renderInfosEntities = this.renderInfosEntitiesNormal;
            this.renderInfosTileEntities = this.renderInfosTileEntitiesNormal;
        }
    }

    public int getRenderedChunksShadow() {
        return !Config.isShadersShadows() ? -1 : this.renderInfosTerrainShadow.size();
    }

    public int getCountEntitiesRenderedShadow() {
        return !Config.isShadersShadows() ? -1 : ShadersRender.countEntitiesRenderedShadow;
    }

    public int getCountTileEntitiesRenderedShadow() {
        if (!Config.isShaders()) {
            return -1;
        } else {
            return !Shaders.hasShadowMap ? -1 : ShadersRender.countTileEntitiesRenderedShadow;
        }
    }

    public void onChunkRenderNeedsUpdate(SectionRenderDispatcher.RenderSection renderChunk) {
        if (!renderChunk.getSectionMesh().hasTerrainBlockEntities()) {
            ;
        }
    }

    public boolean needsFrustumUpdate() {
        return this.sectionOcclusionGraph.needsFrustumUpdate();
    }

    public void needsUpdate() {
        this.sectionOcclusionGraph.invalidate();
        this.cloudRenderer.markForRebuild();
    }

    public boolean shouldRenderEntity(Entity entity, int minWorldY, int maxWorldY) {
        if (entity instanceof Display) {
            return true;
        } else if (entity == this.minecraft.getCameraEntity()) {
            return true;
        } else if (entity instanceof LivingEntity le && le.getScale() > 1.0F) {
            return true;
        } else {
            BlockPos posEntity = entity.blockPosition();
            return this.renderInfosEntities.contains(SectionPos.asLong(posEntity)) || posEntity.getY() <= minWorldY || posEntity.getY() >= maxWorldY;
        }
    }

    public LevelRenderState getLevelRenderState() {
        return this.levelRenderState;
    }

    public FeatureRenderDispatcher getFeatureRenderDispatcher() {
        return this.featureRenderDispatcher;
    }

    public SubmitNodeStorage getSubmitNodeStorage() {
        return this.submitNodeStorage;
    }

    public GpuSampler getChunkLayerSampler() {
        return this.chunkLayerSampler;
    }

    public WeatherEffectRenderer getWeatherEffects() {
        return this.weatherEffectRenderer;
    }

    public void setWeatherEffects(WeatherEffectRenderer value) {
        this.weatherEffectRenderer = value;
    }

    public void setLevel(ClientLevel level) {
        if (Config.isDynamicLights()) {
            DynamicLights.clear();
        }

        ChunkVisibility.reset();
        this.renderEnv.reset(null, null);
        BiomeUtils.onWorldChanged(level);
        Shaders.checkWorldChanged(level);
    }

    public void customTick(long gameTime) {
        if (Config.isRenderRegions() && gameTime % 60L == 0L) {
            this.regionLayerRenderer.clear();
        }
    }

    public static boolean isAmbientOcclusion() {
        return ambientOcclusion;
    }

    public SectionRenderDispatcher sectionRenderDispatcher() {
        return this.sectionRenderDispatcher;
    }

    public EntityRenderDispatcher entityRenderDispatcher() {
        return this.entityRenderDispatcher;
    }

    public BlockEntityRenderDispatcher blockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    public RenderTarget entityOutlineTarget() {
        return this.targets.entityOutline != null ? this.targets.entityOutline.get() : null;
    }

    public RenderTarget translucentTarget() {
        return this.targets.translucent != null ? this.targets.translucent.get() : null;
    }

    public RenderTarget itemEntityTarget() {
        return this.targets.itemEntity != null ? this.targets.itemEntity.get() : null;
    }

    public RenderTarget particlesTarget() {
        return this.targets.particles != null ? this.targets.particles.get() : null;
    }

    public RenderTarget weatherTarget() {
        return this.targets.weather != null ? this.targets.weather.get() : null;
    }

    public RenderTarget cloudsTarget() {
        return this.targets.clouds != null ? this.targets.clouds.get() : null;
    }

    public CloudRenderer cloudRenderer() {
        return this.cloudRenderer;
    }

    public SkyRenderer skyRenderer() {
        return this.skyRenderer;
    }

    public WeatherEffectRenderer weatherEffectRenderer() {
        return this.weatherEffectRenderer;
    }

    public WorldBorderRenderer worldBorderRenderer() {
        return this.worldBorderRenderer;
    }

    public ViewArea viewArea() {
        return this.viewArea;
    }

    public ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections() {
        return this.visibleSections;
    }

    public ObjectArrayList<SectionRenderDispatcher.RenderSection> nearbyVisibleSections() {
        return this.nearbyVisibleSections;
    }

    public LongCollection expectedChunks() {
        return this.sectionOcclusionGraph.expectedChunks();
    }

    public SectionOcclusionGraph sectionOcclusionGraph() {
        return this.sectionOcclusionGraph;
    }

    public Gizmos.TemporaryCollection collectPerFrameRenderThreadGizmos() {
        return Gizmos.withCollector(this.renderThreadGizmos);
    }

    private void finalizeGizmoCollection() {
        DrawableGizmoPrimitives standardPrimitives = new DrawableGizmoPrimitives();
        DrawableGizmoPrimitives alwaysOnTopPrimitives = new DrawableGizmoPrimitives();
        long currentMillis = Util.getMillis();

        for (SimpleGizmoCollector.GizmoInstance instance : this.renderThreadGizmos.drainGizmos()) {
            instance.gizmo().emit(instance.isAlwaysOnTop() ? alwaysOnTopPrimitives : standardPrimitives, instance.getAlphaMultiplier(currentMillis));
        }

        this.finalizedGizmos = new LevelRenderer.FinalizedGizmos(standardPrimitives, alwaysOnTopPrimitives);
    }

    public void addMainThreadGizmos(List<SimpleGizmoCollector.GizmoInstance> mainThreadGizmos) {
        this.renderThreadGizmos.addTemporaryGizmos(mainThreadGizmos);
    }

    private record FinalizedGizmos(DrawableGizmoPrimitives standardPrimitives, DrawableGizmoPrimitives alwaysOnTopPrimitives) {
    }
}
