package dev.fesly.impl.util.render;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.impl.interfaces.client.IAccess;
import org.lwjgl.opengl.GL11;
public class ScaleUtil implements IAccess {
    public static float size = 2;
    public static void scale_pre() {
        final double scale = mw.getScaleFactor() / Math.pow(mw.getScaleFactor(), 2);
        GL11.glPushMatrix();
        GL11.glScaled(scale * size, scale * size, scale * size);
    }
    public static void scale_post() {
        GL11.glScaled(size, size, size);
        GL11.glPopMatrix();
    }
    public static void scaleStart(float x, float y, float scale) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, 1);
        RenderSystem.translated(-x, -y, 0);
    }
    public static void scaleEnd() {
        RenderSystem.popMatrix();
    }
    public static void scaleNonMatrix(float x, float y, float scale) {
        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, 1);
        RenderSystem.translated(-x, -y, 0);
    }
    public static int calc(int value) {
        return (int) (value * mw.getScaleFactor() / size);
    }
    public static int calc(double value) {
        return (int) (value * mw.getScaleFactor() / size);
    }
    public static double[] calc(double mouseX, double mouseY) {
        mouseX = mouseX * mw.getScaleFactor() / size;
        mouseY = mouseY * mw.getScaleFactor() / size;
        return new double[]{mouseX, mouseY};
    }
    public static int[] calc(int mouseX, int mouseY) {
        mouseX = (int) (mouseX * mw.getScaleFactor() / size);
        mouseY = (int) (mouseY * mw.getScaleFactor() / size);
        return new int[]{mouseX, mouseY};
    }
}
