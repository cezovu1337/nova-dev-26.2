package net.minecraft.client.renderer;

import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public class BufferBuilder {
    private int mode;
    private VertexFormat format;
    private double nextX;
    private double nextY;
    private double nextZ;

    public BufferBuilder(int size) {
    }

    public void begin(int glMode, VertexFormat format) {
        this.mode = glMode;
        this.format = format;
        GL11.glBegin(glMode);
    }

    public BufferBuilder pos(double x, double y, double z) {
        this.nextX = x;
        this.nextY = y;
        this.nextZ = z;
        return this;
    }

    public BufferBuilder pos(org.joml.Matrix4f matrix, float x, float y, float z) {
        org.joml.Vector4f vec = matrix.transform(new org.joml.Vector4f(x, y, z, 1.0F));
        this.nextX = vec.x();
        this.nextY = vec.y();
        this.nextZ = vec.z();
        return this;
    }

    public BufferBuilder pos(net.minecraft.util.math.vector.Matrix4f matrix, float x, float y, float z) {
        net.minecraft.util.math.vector.Vector4f vec = new net.minecraft.util.math.vector.Vector4f(x, y, z, 1.0F);
        vec.transform(matrix);
        this.nextX = vec.getX();
        this.nextY = vec.getY();
        this.nextZ = vec.getZ();
        return this;
    }

    public BufferBuilder color(float red, float green, float blue, float alpha) {
        GL11.glColor4f(red, green, blue, alpha);
        return this;
    }

    public BufferBuilder color(int red, int green, int blue, int alpha) {
        GL11.glColor4ub((byte) red, (byte) green, (byte) blue, (byte) alpha);
        return this;
    }

    public BufferBuilder tex(float u, float v) {
        GL11.glTexCoord2f(u, v);
        return this;
    }

    public BufferBuilder lightmap(int lightmap) {
        return this;
    }

    public BufferBuilder lightmap(int sky, int block) {
        return this;
    }

    public BufferBuilder normal(float x, float y, float z) {
        GL11.glNormal3f(x, y, z);
        return this;
    }

    public void endVertex() {
        GL11.glVertex3d(this.nextX, this.nextY, this.nextZ);
    }

    public void finishDrawing() {
        GL11.glEnd();
    }
}
