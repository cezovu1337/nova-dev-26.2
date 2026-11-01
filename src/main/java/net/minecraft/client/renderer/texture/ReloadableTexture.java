package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.io.IOException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.Config;
import net.optifine.EmissiveTextures;
import net.optifine.shaders.ShadersTex;

public abstract class ReloadableTexture extends AbstractTexture {
    private final Identifier resourceId;
    private ResourceManager resourceManager = Config.getResourceManager();
    public Identifier locationEmissive;
    public boolean isEmissive;
    public long size;

    public ReloadableTexture(Identifier resourceId) {
        this.resourceId = resourceId;
    }

    public Identifier resourceId() {
        return this.resourceId;
    }

    public void apply(TextureContents contents) {
        boolean clamp = contents.clamp();
        boolean blur = contents.blur();
        AddressMode addressMode = clamp ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
        FilterMode minMag = blur ? FilterMode.LINEAR : FilterMode.NEAREST;
        this.sampler = RenderSystem.getSamplerCache().getSampler(addressMode, addressMode, minMag, minMag, false);

        try (NativeImage image = contents.image()) {
            this.doLoad(image);
        }
    }

    protected void doLoad(NativeImage image) {
        GpuDevice device = RenderSystem.getDevice();
        this.close();
        this.texture = device.createTexture(this.resourceId::toString, 5, GpuFormat.RGBA8_UNORM, image.getWidth(), image.getHeight(), 1, 1);
        this.textureView = device.createTextureView(this.texture);
        device.createCommandEncoder().writeToTexture(this.texture, image);
        device.createCommandEncoder().submit();
        this.texture.setParentTexture(this);
        if (Config.isShaders()) {
            ShadersTex.loadSimpleTextureNS(this.getGlTextureId(), image, this.sampler, this.resourceManager, this.resourceId, this.getMultiTexID());
        }

        if (EmissiveTextures.isActive()) {
            EmissiveTextures.loadTexture(this.resourceId, this);
        }

        this.size = image.getSize();
    }

    public abstract TextureContents loadContents(ResourceManager var1) throws IOException;
}
