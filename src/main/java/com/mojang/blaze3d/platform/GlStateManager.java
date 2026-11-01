package com.mojang.blaze3d.platform;

public class GlStateManager extends com.mojang.blaze3d.opengl.GlStateManager {
    public static void pushMatrix() {
        org.lwjgl.opengl.GL11.glPushMatrix();
    }

    public static void popMatrix() {
        org.lwjgl.opengl.GL11.glPopMatrix();
    }

    public static void scaled(double x, double y, double z) {
        org.lwjgl.opengl.GL11.glScaled(x, y, z);
    }

    public static void scale(float x, float y, float z) {
        org.lwjgl.opengl.GL11.glScalef(x, y, z);
    }

    public static void translated(double x, double y, double z) {
        org.lwjgl.opengl.GL11.glTranslated(x, y, z);
    }

    public static void translate(float x, float y, float z) {
        org.lwjgl.opengl.GL11.glTranslatef(x, y, z);
    }

    public static void color4f(float r, float g, float b, float a) {
        org.lwjgl.opengl.GL11.glColor4f(r, g, b, a);
    }

    public static void disableLighting() {
    }

    public static void enableLighting() {
    }

    public static void disableCull() {
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
    }

    public static void enableCull() {
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
    }

    public static void enableDepthTest() {
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
    }

    public static void disableDepthTest() {
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
    }

    public static void depthMask(boolean flag) {
        org.lwjgl.opengl.GL11.glDepthMask(flag);
    }

    public static void enableBlend() {
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
    }

    public static void disableBlend() {
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);
    }

    public static void translatef(float x, float y, float z) {
        org.lwjgl.opengl.GL11.glTranslatef(x, y, z);
    }

    public static void rotatef(float angle, float x, float y, float z) {
        org.lwjgl.opengl.GL11.glRotatef(angle, x, y, z);
    }

    public static void bindTexture(int texture) {
        org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, texture);
    }

    public static void activeTexture(int textureUnit) {
        org.lwjgl.opengl.GL13.glActiveTexture(textureUnit);
    }

    public static void blendFunc(int src, int dst) {
        org.lwjgl.opengl.GL11.glBlendFunc(src, dst);
    }

    public static void blendFunc(SourceFactor src, DestFactor dst) {
        blendFunc(src.value, dst.value);
    }

    public enum SourceFactor {
        SRC_ALPHA(770);
        public final int value;
        SourceFactor(int value) { this.value = value; }
    }

    public enum DestFactor {
        ONE_MINUS_SRC_ALPHA(771);
        public final int value;
        DestFactor(int value) { this.value = value; }
    }
}
