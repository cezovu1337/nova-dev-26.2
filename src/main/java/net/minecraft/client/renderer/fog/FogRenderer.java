package net.minecraft.client.renderer.fog;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.client.renderer.fog.environment.PowderedSnowFogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.render.FogParameters;
import net.optifine.shaders.Shaders;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class FogRenderer implements AutoCloseable {
    public static final int FOG_UBO_SIZE = new Std140SizeCalculator().putVec4().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().get();
    private static final List<FogEnvironment> FOG_ENVIRONMENTS = Lists.newArrayList(
        new LavaFogEnvironment(),
        new PowderedSnowFogEnvironment(),
        new BlindnessFogEnvironment(),
        new DarknessFogEnvironment(),
        new WaterFogEnvironment(),
        new AtmosphericFogEnvironment()
    );
    private static boolean fogEnabled = true;
    private final GpuBuffer emptyBuffer;
    private final MappableRingBuffer regularBuffer;
    private FogParameters lastFogParams;

    public FogRenderer() {
        GpuDevice device = RenderSystem.getDevice();
        this.regularBuffer = new MappableRingBuffer(() -> "Fog UBO", 130, FOG_UBO_SIZE);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(FOG_UBO_SIZE);
            this.updateBuffer(
                buffer, 0, new Vector4f(0.0F), Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE
            );
            this.emptyBuffer = device.createBuffer(() -> "Empty fog", 128, buffer.flip());
        }

        RenderSystem.setShaderFog(this.getBuffer(FogRenderer.FogMode.NONE));
    }

    @Override
    public void close() {
        this.emptyBuffer.close();
        this.regularBuffer.close();
    }

    public void endFrame() {
        this.regularBuffer.rotate();
    }

    public GpuBufferSlice getBuffer(FogRenderer.FogMode mode) {
        if (!fogEnabled) {
            return this.emptyBuffer.slice(0L, FOG_UBO_SIZE);
        }

        return switch (mode) {
            case NONE -> this.emptyBuffer.slice(0L, FOG_UBO_SIZE);
            case WORLD -> this.regularBuffer.currentBuffer().slice(0L, FOG_UBO_SIZE, this.lastFogParams);
        };
    }

    private void computeFogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f dest) {
        FogType fogType = this.getFogType(camera);
        Entity entity = camera.entity();
        FogEnvironment colorSourceEnvironment = null;
        FogEnvironment darknessModifyingEnvironment = null;

        for (FogEnvironment fogEnvironment : FOG_ENVIRONMENTS) {
            if (fogEnvironment.isApplicable(fogType, entity)) {
                if (colorSourceEnvironment == null && fogEnvironment.providesColor()) {
                    colorSourceEnvironment = fogEnvironment;
                }

                if (darknessModifyingEnvironment == null && fogEnvironment.modifiesDarkness()) {
                    darknessModifyingEnvironment = fogEnvironment;
                }
            }
        }

        if (colorSourceEnvironment == null) {
            throw new IllegalStateException("No color source environment found");
        }

        int color = colorSourceEnvironment.getBaseColor(level, camera, renderDistance, partialTicks);
        float voidDarknessOnsetRange = level.getLevelData().voidDarknessOnsetRange();
        float darkness = Mth.clamp((voidDarknessOnsetRange + level.getMinY() - (float)camera.position().y) / voidDarknessOnsetRange, 0.0F, 1.0F);
        if (darknessModifyingEnvironment != null) {
            LivingEntity livingEntity = (LivingEntity)entity;
            darkness = darknessModifyingEnvironment.getModifiedDarkness(livingEntity, darkness, partialTicks);
        }

        float fogRed = ARGB.redFloat(color);
        float fogGreen = ARGB.greenFloat(color);
        float fogBlue = ARGB.blueFloat(color);
        if (darkness > 0.0F && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
            float brightness = Mth.square(1.0F - darkness);
            fogRed *= brightness;
            fogGreen *= brightness;
            fogBlue *= brightness;
        }

        if (darkenWorldAmount > 0.0F) {
            fogRed = Mth.lerp(darkenWorldAmount, fogRed, fogRed * 0.7F);
            fogGreen = Mth.lerp(darkenWorldAmount, fogGreen, fogGreen * 0.6F);
            fogBlue = Mth.lerp(darkenWorldAmount, fogBlue, fogBlue * 0.6F);
        }

        float brightenFactor;
        if (fogType == FogType.WATER) {
            if (entity instanceof LocalPlayer localPlayer) {
                brightenFactor = localPlayer.getWaterVision();
            } else {
                brightenFactor = 1.0F;
            }
        } else if (entity instanceof LivingEntity livingEntity
            && livingEntity.hasEffect(MobEffects.NIGHT_VISION)
            && !livingEntity.hasEffect(MobEffects.DARKNESS)) {
            brightenFactor = GameRenderer.nightVisionScale(livingEntity, partialTicks);
        } else {
            brightenFactor = 0.0F;
        }

        if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F) {
            float maxColor = Math.max(fogRed, Math.max(fogGreen, fogBlue));
            float targetScale = 1.0F / Math.clamp(maxColor, 0.07F, 1.0F);
            float targetScaleMax = 1.0F / maxColor;
            float scale = maxColor != fogRed ? targetScale : targetScaleMax;
            fogRed = Mth.lerp(brightenFactor, fogRed, fogRed * scale);
            scale = maxColor != fogGreen ? targetScale : targetScaleMax;
            fogGreen = Mth.lerp(brightenFactor, fogGreen, fogGreen * scale);
            scale = maxColor != fogBlue ? targetScale : targetScaleMax;
            fogBlue = Mth.lerp(brightenFactor, fogBlue, fogBlue * scale);
        }

        if (fogType == FogType.WATER) {
            Entity renderViewEntity = camera.entity();
            Vec3 colUnderwater = CustomColors.getUnderwaterColor(level, renderViewEntity.getX(), renderViewEntity.getY() + 1.0, renderViewEntity.getZ());
            if (colUnderwater != null) {
                fogRed = (float)colUnderwater.x;
                fogGreen = (float)colUnderwater.y;
                fogBlue = (float)colUnderwater.z;
            }
        } else if (fogType == FogType.LAVA) {
            Entity renderViewEntity = camera.entity();
            Vec3 colUnderlava = CustomColors.getUnderlavaColor(level, renderViewEntity.getX(), renderViewEntity.getY() + 1.0, renderViewEntity.getZ());
            if (colUnderlava != null) {
                fogRed = (float)colUnderlava.x;
                fogGreen = (float)colUnderlava.y;
                fogBlue = (float)colUnderlava.z;
            }
        }

        if (Reflector.ForgeHooksClient_getFogColor.exists()) {
            Vector3f fogColor = (Vector3f)Reflector.ForgeHooksClient_getFogColor
                .call(camera, partialTicks, level, renderDistance, darkenWorldAmount, fogRed, fogGreen, fogBlue);
            fogRed = fogColor.x();
            fogGreen = fogColor.y();
            fogBlue = fogColor.z();
        }

        Shaders.setClearColor(fogRed, fogGreen, fogBlue, 0.0F);
        dest.set(fogRed, fogGreen, fogBlue, 1.0F);
    }

    public static boolean toggleFog() {
        return fogEnabled = !fogEnabled;
    }

    public FogData setupFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level) {
        float partialTickTime = deltaTracker.getGameTimeDeltaPartialTick(false);
        float renderDistanceInBlocks = renderDistanceInChunks * 16;
        FogType fogType = this.getFogType(camera);
        Entity entity = camera.entity();
        FogData fog = new FogData();
        this.computeFogColor(camera, partialTickTime, level, renderDistanceInChunks, darkenWorldAmount, fog.color);

        for (FogEnvironment fogEnvironment : FOG_ENVIRONMENTS) {
            if (fogEnvironment.isApplicable(fogType, entity)) {
                fogEnvironment.setupFog(fog, camera, level, renderDistanceInBlocks, deltaTracker);
                break;
            }
        }

        float renderDistanceFogSpan = Mth.clamp(renderDistanceInBlocks / 10.0F, 4.0F, 64.0F);
        fog.renderDistanceStart = renderDistanceInBlocks - renderDistanceFogSpan;
        fog.renderDistanceEnd = renderDistanceInBlocks;
        boolean fogStandard = false;
        float farPlaneDistance = renderDistanceInChunks * 16;
        if (fogType == FogType.ATMOSPHERIC) {
            fogStandard = true;
            fog.renderDistanceStart = farPlaneDistance * Config.getFogStart();
        }

        if (Config.isFogOff() && fogStandard) {
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
            fog.renderDistanceStart = Float.MAX_VALUE;
            fog.renderDistanceEnd = Float.MAX_VALUE;
        }

        if (Reflector.ForgeHooksClient_setupFog.exists()) {
            fog.color = (Vector4f)Reflector.ForgeHooksClient_setupFog.call(fogType, camera, deltaTracker, fog, fog.color);
        }

        if (dev.fesly.client.module.impl.render.CustomFog.enabled()) {
            java.awt.Color customColor = dev.fesly.impl.util.color.ColorUtil.interpolateColor(dev.fesly.impl.util.color.ClientUtil.getTwoClientColor(), dev.fesly.impl.util.color.ClientUtil.getThreeClientColor(), (float) Math.sin((System.currentTimeMillis() - 1000L) / 200.0));
            java.awt.Color customColor2 = dev.fesly.impl.util.color.ColorUtil.interpolateColor(dev.fesly.impl.util.color.ClientUtil.getOneClientColor(), dev.fesly.impl.util.color.ClientUtil.getFourClientColor(), (float) Math.sin((System.currentTimeMillis() - 1000L) / 400.0));
            customColor = dev.fesly.impl.util.color.ColorUtil.interpolateColor(customColor, customColor2, (float) Math.cos((System.currentTimeMillis() - 1000L) / 1000.0));
            if (!dev.fesly.client.module.impl.render.CustomFog.isAnimated()) {
                customColor = dev.fesly.impl.util.color.ClientUtil.getThreeClientColor();
            }
            if (dev.fesly.client.module.impl.render.CustomFog.isCustomColor()) {
                customColor = dev.fesly.client.module.impl.render.CustomFog.getColor();
            }
            fog.color = new org.joml.Vector4f(customColor.getRed() / 255.0F, customColor.getGreen() / 255.0F, customColor.getBlue() / 255.0F, 1.0F);
            fog.renderDistanceStart = farPlaneDistance * dev.fesly.client.module.impl.render.CustomFog.getFogDistance();
            fog.renderDistanceEnd = farPlaneDistance;
        }

        this.lastFogParams = Config.isShaders() ? new FogParameters(fog.color, fog) : null;
        return fog;
    }

    public void updateBuffer(FogData fog) {
        try (GpuBufferSlice.MappedView view = this.regularBuffer.currentBuffer().map(false, true)) {
            this.updateBuffer(
                view.data(),
                0,
                fog.color,
                fog.environmentalStart,
                fog.environmentalEnd,
                fog.renderDistanceStart,
                fog.renderDistanceEnd,
                fog.skyEnd,
                fog.cloudEnd
            );
        }
    }

    private FogType getFogType(Camera camera) {
        FogType blockFogType = camera.getFluidInCamera();
        return blockFogType == FogType.NONE ? FogType.ATMOSPHERIC : blockFogType;
    }

    private void updateBuffer(
        ByteBuffer byteBuffer,
        int offset,
        Vector4f fogColor,
        float environmentalStart,
        float environmentalEnd,
        float renderDistanceStart,
        float renderDistanceEnd,
        float skyEnd,
        float endClouds
    ) {
        byteBuffer.position(offset);
        Std140Builder.intoBuffer(byteBuffer)
            .putVec4(fogColor)
            .putFloat(environmentalStart)
            .putFloat(environmentalEnd)
            .putFloat(renderDistanceStart)
            .putFloat(renderDistanceEnd)
            .putFloat(skyEnd)
            .putFloat(endClouds);
    }

    public static void setShadersFog(GpuBufferSlice sliceIn) {
        Object data = sliceIn.getData();
        if (data == null) {
            Shaders.setFogParameters(null, null);
        } else if (data instanceof FogParameters params) {
            Shaders.setFogParameters(params.getFogColor(), params.getFogData());
        }
    }

    public enum FogMode {
        NONE,
        WORLD;
    }
}
