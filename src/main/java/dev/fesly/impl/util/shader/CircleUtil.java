package dev.fesly.impl.util.shader;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.render.GLUtil;
import java.awt.*;
public class CircleUtil implements IAccess {
    private static final ShaderUtil circleShader = new ShaderUtil("circle.frag");
    public static void drawCircle(float x, float y, float radius, float progress, int change, Color color, float smoothness) {
        GLUtil.startBlend();
        float borderThickness = 1;
        circleShader.init();
        circleShader.setUniformf("radialSmoothness", smoothness);
        circleShader.setUniformf("radius", radius);
        circleShader.setUniformf("borderThickness", borderThickness);
        circleShader.setUniformf("progress", 1 - (progress / 100));
        circleShader.setUniformi("change", change);
        circleShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        float wh = radius + 10;
        float v = (wh / 2f) - ((radius + borderThickness) / 2f);
        circleShader.setUniformf("pos", (float) ((x + v) * mw.getScaleFactor()),
                (float) ((mw.getHeight() - ((radius + borderThickness) * mw.getScaleFactor())) - ((y + v) * mw.getScaleFactor())));
        ShaderUtil.drawQuads(x, y, wh, wh);
        circleShader.unload();
        GLUtil.endBlend();
    }
}
