package dev.fesly.impl.util.render;

import com.mojang.blaze3d.platform.GlStateManager;

import static org.lwjgl.opengl.GL11.*;

public class GLUtil {

    public static void enableDepth() {
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);
    }

    public static void disableDepth() {
        GlStateManager.disableDepthTest();
        GlStateManager.depthMask(false);
    }

    public static int[] enabledCaps = new int[32];

    public static void enableCaps(int... caps) {
        for (int cap : caps) glEnable(cap);
        enabledCaps = caps;
    }

    public static void disableCaps() {
        for (int cap : enabledCaps) glDisable(cap);
    }

    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }

    public static void setup2DRendering(boolean blend) {
        if (blend) {
            startBlend();
        }
        GlStateManager.disableTexture();
    }

    public static void setup2DRendering() {
        setup2DRendering(true);
    }

    public static void end2DRendering() {
        GlStateManager.enableTexture();
        endBlend();
    }

    public static void setup2DRendering(Runnable runnable) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        runnable.run();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    public static void startRotate(float x, float y, float rotate) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, 0);
        GlStateManager.rotatef(rotate, 0, 0, -1);
        GlStateManager.translatef(-x, -y, 0);
    }

    public static void endRotate() {
        GlStateManager.popMatrix();
    }

    public static void rotate(float x, float y, float rotate, Runnable runnable) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, 0);
        GlStateManager.rotatef(rotate, 0, 0, -1);
        GlStateManager.translatef(-x, -y, 0);
        runnable.run();
        GlStateManager.popMatrix();
    }

}