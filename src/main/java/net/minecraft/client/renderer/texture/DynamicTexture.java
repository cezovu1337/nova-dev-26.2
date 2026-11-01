package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.optifine.Config;
import net.optifine.shaders.ShadersTex;
import org.slf4j.Logger;

public class DynamicTexture extends AbstractTexture implements Dumpable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private NativeImage pixels;
    private boolean capeTexture;
    private boolean elytraCapeTexture;

    public DynamicTexture(Supplier<String> label, NativeImage image) {
        this.pixels = image;
        this.createTexture(label);
        this.upload();
    }

    public DynamicTexture(NativeImage image) {
        this(() -> "Dynamic Texture", image);
    }

    public DynamicTexture(java.awt.image.BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, w, h, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                if (a == 0) {
                    nativeImage.setPixel(x, y, 0x00FFFFFF);
                } else {
                    nativeImage.setPixel(x, y, rgb);
                }
            }
        }
        this.pixels = nativeImage;
        this.createTexture(() -> "dynamic_texture");
        this.setLinear(true);
        this.upload();
    }

    public DynamicTexture(String label, int width, int height, boolean zero) {
        this.pixels = new NativeImage(width, height, zero);
        this.createTexture(label);
    }

    public DynamicTexture(Supplier<String> label, int width, int height, boolean zero) {
        this.pixels = new NativeImage(width, height, zero);
        this.createTexture(label);
    }

    private void createTexture(Supplier<String> label) {
        GpuDevice device = RenderSystem.getDevice();
        this.texture = device.createTexture(label, 5, GpuFormat.RGBA8_UNORM, this.pixels.getWidth(), this.pixels.getHeight(), 1, 1);
        this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        this.textureView = device.createTextureView(this.texture);
        this.texture.setParentTexture(this);
        if (Config.isShaders()) {
            ShadersTex.initDynamicTextureNS(this);
        }
    }

    private void createTexture(String label) {
        GpuDevice device = RenderSystem.getDevice();
        this.texture = device.createTexture(label, 13, GpuFormat.RGBA8_UNORM, this.pixels.getWidth(), this.pixels.getHeight(), 1, 1);
        this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        this.textureView = device.createTextureView(this.texture);
        this.texture.setParentTexture(this);
        if (Config.isShaders()) {
            ShadersTex.initDynamicTextureNS(this);
        }
    }

    public void upload() {
        if (this.texture != null) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.pixels);
        } else {
            LOGGER.warn("Trying to upload disposed texture {}", this.getTexture().getLabel());
        }
    }

    public NativeImage getPixels() {
        return this.pixels;
    }

    public void setPixels(NativeImage pixels) {
        this.pixels.close();
        this.pixels = pixels;
    }

    @Override
    public void close() {
        this.pixels.close();
        super.close();
    }

    @Override
    public void dumpContents(Identifier selfId, Path dir) throws IOException {
        if (!this.pixels.isClosed()) {
            String outputId = selfId.toDebugFileName() + ".png";
            Path path = dir.resolve(outputId);
            this.pixels.writeToFile(path);
        }
    }

    public boolean isCapeTexture() {
        return this.capeTexture;
    }

    public void setCapeTexture(boolean capeTexture) {
        this.capeTexture = capeTexture;
    }

    public boolean isElytraCapeTexture() {
        return this.elytraCapeTexture;
    }

    public void setElytraCapeTexture(boolean elytraCapeTexture) {
        this.elytraCapeTexture = elytraCapeTexture;
    }
}
