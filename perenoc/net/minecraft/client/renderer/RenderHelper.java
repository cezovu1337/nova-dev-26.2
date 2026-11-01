package net.minecraft.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Orientation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderHelper
{
    private static final Vector3f DEFAULT_LIGHTING = Util.make(new Vector3f(0.2F, 1.0F, -0.7F), Vector3f::normalize);
    private static final Vector3f DIFFUSE_LIGHTING = Util.make(new Vector3f(-0.2F, 1.0F, 0.7F), Vector3f::normalize);
    private static final Vector3f GUI_FLAT_DIFFUSE_LIGHTING = Util.make(new Vector3f(0.2F, 1.0F, -0.7F), Vector3f::normalize);
    private static final Vector3f GUI_3D_DIFFUSE_LIGHTING = Util.make(new Vector3f(-0.2F, -1.0F, 0.7F), Vector3f::normalize);

    public static BufferBuilder renderPrepare(Color color) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        //RenderSystem.setShader(GameRenderer::getPositionShader);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.color4f(red, green, blue, alpha);
        RenderSystem.lineWidth(2f);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES,
                DefaultVertexFormats.POSITION_COLOR);
        return buffer;
    }

    public static void renderOutline(Vector3d start, Vector3d dimensions, Color color, MatrixStack stack) {
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        BufferBuilder buffer = renderPrepare(color);
        Tessellator tessellator = Tessellator.getInstance();

        renderOutlineIntern(start, dimensions, stack, buffer);

        buffer.endVertex();

        tessellator.draw();

        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static void renderOutlineIntern(Vector3d start, Vector3d dimensions, MatrixStack stack, BufferBuilder buffer) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo c = mc.gameRenderer.getActiveRenderInfo();
        Vector3d camPos = c.getProjectedView();
        start = start.subtract(camPos);
        Vector3d end = start.add(dimensions);
        Matrix4f matrix = stack.getLast().getMatrix();
        float x1 = (float) start.x;
        float y1 = (float) start.y;
        float z1 = (float) start.z;
        float x2 = (float) end.x;
        float y2 = (float) end.y;
        float z2 = (float) end.z;

        buffer.begin(1,
                DefaultVertexFormats.POSITION_COLOR);

        buffer.pos(matrix, x1, y1, z1).endVertex();
        buffer.pos(matrix, x1, y1, z2).endVertex();
        buffer.pos(matrix, x1, y1, z2).endVertex();
        buffer.pos(matrix, x2, y1, z2).endVertex();
        buffer.pos(matrix, x2, y1, z2).endVertex();
        buffer.pos(matrix, x2, y1, z1).endVertex();
        buffer.pos(matrix, x2, y1, z1).endVertex();
        buffer.pos(matrix, x1, y1, z1).endVertex();

        buffer.pos(matrix, x1, y2, z1).endVertex();
        buffer.pos(matrix, x1, y2, z2).endVertex();
        buffer.pos(matrix, x1, y2, z2).endVertex();
        buffer.pos(matrix, x2, y2, z2).endVertex();
        buffer.pos(matrix, x2, y2, z2).endVertex();
        buffer.pos(matrix, x2, y2, z1).endVertex();
        buffer.pos(matrix, x2, y2, z1).endVertex();
        buffer.pos(matrix, x1, y2, z1).endVertex();

        buffer.pos(matrix, x1, y1, z1).endVertex();
        buffer.pos(matrix, x1, y2, z1).endVertex();

        buffer.pos(matrix, x2, y1, z1).endVertex();
        buffer.pos(matrix, x2, y2, z1).endVertex();

        buffer.pos(matrix, x2, y1, z2).endVertex();
        buffer.pos(matrix, x2, y2, z2).endVertex();

        buffer.pos(matrix, x1, y1, z2).endVertex();
        buffer.pos(matrix, x1, y2, z2).endVertex();
    }

    public static void renderFilled(Vector3d start, Vector3d dimensions, Color color, MatrixStack stack) {
        Minecraft mc = Minecraft.getInstance();
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        ActiveRenderInfo c = mc.gameRenderer.getActiveRenderInfo();
        Vector3d camPos = c.getProjectedView();
        start = start.subtract(camPos);
        Vector3d end = start.add(dimensions);
        Matrix4f matrix = stack.getLast().getMatrix();
        float x1 = (float) start.x;
        float y1 = (float) start.y;
        float z1 = (float) start.z;
        float x2 = (float) end.x;
        float y2 = (float) end.y;
        float z2 = (float) end.z;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Tessellator tessellator = Tessellator.getInstance();
        //RenderSystem.setupPo(GameRenderer::getPositionColorShader);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        buffer.begin(8,
                DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(matrix, x1, y2, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y2, z1).color(red, green, blue, alpha).endVertex();

        buffer.pos(matrix, x1, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y2, z2).color(red, green, blue, alpha).endVertex();

        buffer.pos(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y2, z1).color(red, green, blue, alpha).endVertex();

        buffer.pos(matrix, x2, y2, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y2, z1).color(red, green, blue, alpha).endVertex();

        buffer.pos(matrix, x1, y2, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y2, z2).color(red, green, blue, alpha).endVertex();

        buffer.pos(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x2, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x1, y1, z2).color(red, green, blue, alpha).endVertex();

        tessellator.draw();

        //BufferRenderer.draw(buffer);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static void enableStandardItemLighting()
    {
        RenderSystem.enableLighting();
        RenderSystem.enableColorMaterial();
        RenderSystem.colorMaterial(1032, 5634);
    }

    /**
     * Disables the OpenGL lighting properties enabled by enableStandardItemLighting
     */
    public static void disableStandardItemLighting()
    {
        RenderSystem.disableLighting();
        RenderSystem.disableColorMaterial();
    }

    public static void setupDiffuseGuiLighting(Matrix4f matrix)
    {
        RenderSystem.setupLevelDiffuseLighting(GUI_FLAT_DIFFUSE_LIGHTING, GUI_3D_DIFFUSE_LIGHTING, matrix);
    }

    public static void setupLevelDiffuseLighting(Matrix4f matrixIn)
    {
        RenderSystem.setupLevelDiffuseLighting(DEFAULT_LIGHTING, DIFFUSE_LIGHTING, matrixIn);
    }

    public static void setupGuiFlatDiffuseLighting()
    {
        RenderSystem.setupGuiFlatDiffuseLighting(DEFAULT_LIGHTING, DIFFUSE_LIGHTING);
    }

    public static void setupGui3DDiffuseLighting()
    {
        RenderSystem.setupGui3DDiffuseLighting(DEFAULT_LIGHTING, DIFFUSE_LIGHTING);
    }
}
