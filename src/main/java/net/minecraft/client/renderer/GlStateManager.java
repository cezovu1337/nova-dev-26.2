package net.minecraft.client.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

public class GlStateManager {

    public static void pushMatrix() {}
    public static void popMatrix() {}
    public static void enableBlend() {}
    public static void disableBlend() {}
    public static void enableAlpha() {}
    public static void disableAlpha() {}
    public static void enableTexture2D() {}
    public static void disableTexture2D() {}
    public static void resetColor() {}
    public static void color(float r, float g, float b, float a) {}
    public static void scale(double x, double y, double z) {}
    public static void rotate(float angle, float x, float y, float z) {}
    public static void translate(double x, double y, double z) {}
    public static void alphaFunc(int func, float ref) {}
    public static void blendFunc(int src, int dst) {}
    public static void tryBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {}
    public static void tryBlendFuncSeparate(SourceFactor srcRGB, DestFactor dstRGB, SourceFactor srcAlpha, DestFactor dstAlpha) {}
    public static void shadeModel(int mode) {}
    public static void bindTexture(int texture) {
        if (texture > 0) GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }
    public static void setActiveTexture(int texture) {
        GL13.glActiveTexture(texture);
    }

    public enum SourceFactor {
        SRC_ALPHA, ONE, ZERO
    }

    public enum DestFactor {
        ONE_MINUS_SRC_ALPHA, ZERO, ONE
    }
}
