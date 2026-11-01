package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.optifine.SmartAnimations;
import net.optifine.render.VertexBuilderWrapper;

public class SpriteCoordinateExpander extends VertexBuilderWrapper implements VertexConsumer {
    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    public SpriteCoordinateExpander(VertexConsumer delegate, TextureAtlasSprite sprite) {
        super(delegate);
        if (SmartAnimations.isActive()) {
            SmartAnimations.spriteRendered(sprite);
        }

        this.delegate = delegate;
        this.sprite = sprite;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        this.delegate.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        this.delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(this.sprite.getU(u), this.sprite.getV(v));
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        this.delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        this.delegate.setLineWidth(width);
        return this;
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
        if (this.delegate.isMultiTexture()) {
            this.delegate.putSprite(this.sprite);
            this.delegate.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
        } else {
            this.delegate.addVertex(x, y, z, color, this.sprite.getU(u), this.sprite.getV(v), overlayCoords, lightCoords, nx, ny, nz);
        }
    }

    @Override
    public boolean canAddVertexFast() {
        return this.delegate.canAddVertexFast();
    }

    @Override
    public void addVertexFast(float x, float y, float z, int color, float texU, float texV, int overlayUV, int lightmapUV, int normals) {
        if (this.delegate.isMultiTexture()) {
            this.delegate.putSprite(this.sprite);
            this.delegate.addVertexFast(x, y, z, color, texU, texV, overlayUV, lightmapUV, normals);
        } else {
            float tu = this.sprite.getU(texU);
            float tv = this.sprite.getV(texV);
            this.delegate.addVertexFast(x, y, z, color, tu, tv, overlayUV, lightmapUV, normals);
        }
    }
}
