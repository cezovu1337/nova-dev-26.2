package dev.fesly.impl.util.shader;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.render.GLUtil;
import dev.fesly.impl.util.render.RenderUtil;
import java.awt.*;
public class GradientUtil implements IAccess {
    private static final ShaderUtil gradientMaskShader = new ShaderUtil("gradientMask");
    private static final ShaderUtil gradientShader = new ShaderUtil("gradient");
    public static void drawGradient(float x, float y, float width, float height, float alpha, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        RenderUtil.setAlphaLimit(0);
        RenderUtil.resetColor();
        GLUtil.startBlend();
        gradientShader.init();
        gradientShader.setUniformf("location", (float) (x * mw.getScaleFactor()), (float) ((mw.getHeight() - (height * mw.getScaleFactor())) - (y * mw.getScaleFactor())));
        gradientShader.setUniformf("rectSize", (float) (width * mw.getScaleFactor()), (float) (height * mw.getScaleFactor()));
        gradientShader.setUniformf("color1", bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f);
        gradientShader.setUniformf("color2", topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f);
        gradientShader.setUniformf("color3", bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f);
        gradientShader.setUniformf("color4", topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f);
        ShaderUtil.drawQuads(x, y, width, height);
        gradientShader.unload();
        GLUtil.endBlend();
    }
    public static void drawGradient(float x, float y, float width, float height, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        RenderUtil.resetColor();
        GLUtil.startBlend();
        gradientShader.init();
        gradientShader.setUniformf("location", (float) (x * mw.getScaleFactor()), (float) ((mw.getHeight() - (height * mw.getScaleFactor())) - (y * mw.getScaleFactor())));
        gradientShader.setUniformf("rectSize", (float) (width * mw.getScaleFactor()), (float) (height * mw.getScaleFactor()));
        gradientShader.setUniformf("color1", bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f, bottomLeft.getAlpha() / 255f);
        gradientShader.setUniformf("color2", topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f, topLeft.getAlpha() / 255f);
        gradientShader.setUniformf("color3", bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f, bottomRight.getAlpha() / 255f);
        gradientShader.setUniformf("color4", topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f, topRight.getAlpha() / 255f);
        ShaderUtil.drawQuads(x, y, width, height);
        gradientShader.unload();
        GLUtil.endBlend();
    }
    public static void drawGradientLR(float x, float y, float width, float height, float alpha, Color left, Color right) {
        drawGradient(x, y, width, height, alpha, left, left, right, right);
    }
    public static void drawGradientTB(float x, float y, float width, float height, float alpha, Color top, Color bottom) {
        drawGradient(x, y, width, height, alpha, bottom, top, bottom, top);
    }
    public static void applyGradientHorizontal(float x, float y, float width, float height, float alpha, Color left, Color right, Runnable content) {
        applyGradient(x, y, width, height, alpha, left, left, right, right, content);
    }
    public static void applyGradientVertical(float x, float y, float width, float height, float alpha, Color top, Color bottom, Runnable content) {
        applyGradient(x, y, width, height, alpha, bottom, top, bottom, top, content);
    }
    public static void applyGradientCornerRL(float x, float y, float width, float height, float alpha, Color bottomLeft, Color topRight, Runnable content) {
        Color mixedColor = ColorUtil.interpolateColor(topRight, bottomLeft, .5f);
        applyGradient(x, y, width, height, alpha, bottomLeft, mixedColor, mixedColor, topRight, content);
    }
    public static void applyGradientCornerLR(float x, float y, float width, float height, float alpha, Color bottomRight, Color topLeft, Runnable content) {
        Color mixedColor = ColorUtil.interpolateColor(bottomRight, topLeft, .5f);
        applyGradient(x, y, width, height, alpha, mixedColor, topLeft, bottomRight, mixedColor, content);
    }
    public static void applyGradient(float x, float y, float width, float height, float alpha, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight, Runnable content) {
        RenderUtil.resetColor();
        GLUtil.startBlend();
        gradientMaskShader.init();
        gradientMaskShader.setUniformf("location", (float) (x * mw.getScaleFactor()), (float) ((mw.getHeight() - (height * mw.getScaleFactor())) - (y * mw.getScaleFactor())));
        gradientMaskShader.setUniformf("rectSize", (float) (width * mw.getScaleFactor()), (float) (height * mw.getScaleFactor()));
        gradientMaskShader.setUniformf("alpha", alpha);
        gradientMaskShader.setUniformi("tex", 0);
        gradientMaskShader.setUniformf("color1", bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f);
        gradientMaskShader.setUniformf("color2", topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f);
        gradientMaskShader.setUniformf("color3", bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f);
        gradientMaskShader.setUniformf("color4", topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f);
        content.run();
        gradientMaskShader.unload();
        GLUtil.endBlend();
    }
}
