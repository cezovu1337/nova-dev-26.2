package com.mojang.blaze3d.textures;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlTexture;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraftforge.client.extensions.IForgeGpuTexture;
import net.optifine.shaders.MultiTexID;

public abstract class GpuTexture implements AutoCloseable, IForgeGpuTexture {
    public static final int USAGE_COPY_DST = 1;
    public static final int USAGE_COPY_SRC = 2;
    public static final int USAGE_TEXTURE_BINDING = 4;
    public static final int USAGE_RENDER_ATTACHMENT = 8;
    public static final int USAGE_CUBEMAP_COMPATIBLE = 16;
    private final GpuFormat format;
    private final int width;
    private final int height;
    private final int depthOrLayers;
    private final int mipLevels;
    private @GpuTexture.Usage int usage;
    private final String label;
    private AbstractTexture parentTexture;

    public GpuTexture(@GpuTexture.Usage int usage, String label, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        this.usage = usage;
        this.label = label;
        this.format = format;
        this.width = width;
        this.height = height;
        this.depthOrLayers = depthOrLayers;
        this.mipLevels = mipLevels;
    }

    public int getWidth(int mipLevel) {
        return this.width >> mipLevel;
    }

    public int getHeight(int mipLevel) {
        return this.height >> mipLevel;
    }

    public int getDepthOrLayers() {
        return this.depthOrLayers;
    }

    public int getMipLevels() {
        return this.mipLevels;
    }

    public GpuFormat getFormat() {
        return this.format;
    }

    public @GpuTexture.Usage int usage() {
        return this.usage;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public abstract void close();

    public abstract boolean isClosed();

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public MultiTexID getMultiTexID() {
        return null;
    }

    public int getGlTextureId() {
        return 0;
    }

    public void bindTexture() {
    }

    public void deleteGlTexture() {
    }

    public void setUpdateBlurMipmap(boolean updateBlurMipmap) {
    }

    public void setParentTexture(AbstractTexture parentTexture) {
        this.parentTexture = parentTexture;
    }

    public AbstractTexture getParentTexture() {
        return this.parentTexture;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public GlTexture getGlTexture() {
        return (GlTexture)this;
    }

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE_USE)
    public @interface Usage {
    }
}
