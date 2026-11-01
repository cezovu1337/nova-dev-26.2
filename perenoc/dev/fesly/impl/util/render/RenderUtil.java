package dev.fesly.impl.util.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.Nova;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.math.StringSupplier;
import dev.fesly.impl.util.shader.ShaderUtil;
import dev.fesly.impl.util.shader.impl.*;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.lang.reflect.Method;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static org.lwjgl.opengl.GL11.*;


public class RenderUtil implements IAccess {

    public static void offsetRender() {
        Vector3d camPos = Minecraft.getInstance().renderViewEntity.getPositionVec();
        GL11.glRotated(MathHelper.wrapDegrees(Minecraft.getInstance().renderViewEntity.getPitchYaw().x), 1, 0, 0);
        GL11.glRotated(MathHelper.wrapDegrees(Minecraft.getInstance().renderViewEntity.getPitchYaw().y + 180.0), 0, 1, 0);
        GL11.glTranslated(-camPos.x, -camPos.y, -camPos.z);
    }

    public static void gl11Setup() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glLineWidth(2.5F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        offsetRender();
    }

    public static void gl11Cleanup() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        glPopMatrix();
    }

    public static void drawFilledBox(AxisAlignedBB box, float r, float g, float b, float a) {
        gl11Setup();
        // Fill
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);
        WorldRenderer.drawBoundingBox(new MatrixStack(), buffer,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ, r, g, b, a / 2f);
        tessellator.draw();

        // Outline
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(r, g, b, 0f).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, 0f).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(r, g, b, 0f).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        tessellator.draw();

        gl11Cleanup();
    }

    public static void drawOutlineBox(AxisAlignedBB bb, float r, float g, float b, float a) {
        gl11Setup();
        AxisAlignedBB box = new AxisAlignedBB(bb.minX - 0.10, bb.minY - 1.8, bb.minZ - 0.10, bb.maxX + 0.1, bb.maxY - 1.5, bb.maxZ + 0.1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        // Outline
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(r, g, b, 0f).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, 0f).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(r, g, b, 0f).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        tessellator.draw();
        gl11Cleanup();
    }

    /**
     * Better to use gl state manager to avoid bugs
     */
    public static void start() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture();
        GlStateManager.disableCull();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableDepthTest();
    }
    public static void setupRender() {
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.enableAlphaTest();
        enableBlend();
        blendFunc(GL_SRC_ALPHA,GL_ONE);
        RenderSystem.shadeModel(GL_SMOOTH);
    }
    public static void endRender() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        disableBlend();
        RenderSystem.color4f(1f,1f,1f,1f);
        RenderSystem.enableAlphaTest();
        blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Better to use gl state manager to avoid bugs
     */
    public static void stop() {
        GlStateManager.enableDepthTest();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableCull();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
    }

    public static void drawTexture(ResourceLocation identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        drawTexture(getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight);
    }

    public static void drawTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        resetColor();

        bindTexture(texId);

        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        y -= height;
        texX = texX / iWidth;
        texY = texY / iHeight;
        texWidth = texWidth / iWidth;
        texHeight = texHeight / iHeight;

        glBegin(GL_QUADS);
        glTexCoord2d(texX, texY);
        glVertex2d(x, y);
        glTexCoord2d(texX, texY + texHeight);
        glVertex2d(x, y + height);
        glTexCoord2d(texX + texWidth, texY + texHeight);
        glVertex2d(x + width, y + height);
        glTexCoord2d(texX + texWidth, texY);
        glVertex2d(x + width, y);
        glEnd();

        bindTexture(0);
        disableBlend();
    }

    private static int getTextureId(ResourceLocation identifier) {
        Texture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
        if(abstractTexture == null) {
            abstractTexture = new SimpleTexture(identifier);
            Minecraft.getInstance().getTextureManager().loadTexture(identifier, abstractTexture);
        }
        return abstractTexture.getGlTextureId();
    }
    public static void setColor(Color color) {
        if (color == null) color = Color.white;
        GL11.glColor4d(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d, color.getAlpha() / 255d);
    }

    public static void setColor(int colorb) {
        Color color = new Color(colorb);
        if (color == null) color = Color.white;
        GL11.glColor4d(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d, color.getAlpha() / 255d);
    }

    public static void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    public static void color(Color color, float alpha) {
        if (color == null) color = Color.white;
        color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f);
    }


    public static double[] worldToScreen(double x, double y, double z) {
        if (Minecraft.getInstance().currentScreen != null && !(Minecraft.getInstance().currentScreen instanceof ChatScreen) && (Minecraft.getInstance().currentScreen == null)) {
            return null;
        } else {
            try {
                Vector3d camera_pos = Minecraft.getInstance().getRenderManager().info.getProjectedView();
                Quaternion camera_rotation_conj = Minecraft.getInstance().getRenderManager().getCameraOrientation().copy();
                camera_rotation_conj.conjugate();
                Vector3f result3f = new Vector3f((float)(camera_pos.x - x), (float)(camera_pos.y - y), (float)(camera_pos.z - z));
                result3f.transform(camera_rotation_conj);
                GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
                Method method = null;
                Method[] var11 = GameRenderer.class.getDeclaredMethods();
                int var12 = var11.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    Method m = var11[var13];
                    if (m.getParameterCount() == 3 && m.getParameterTypes()[2] == Boolean.TYPE && m.getParameterTypes()[1] == Float.TYPE && m.getParameterTypes()[0] == ActiveRenderInfo.class && m.getReturnType() == Double.TYPE) {
                        method = m;
                    }
                }

                method.setAccessible(true);
                float fov = ((Double)method.invoke(gameRenderer, Minecraft.getInstance().getRenderManager().info, 1, true)).floatValue();
                float half_height = (float)Minecraft.getInstance().getMainWindow().getScaledHeight() / 2.0F;
                float scale_factor = half_height / (result3f.getZ() * (float)Math.tan(Math.toRadians((double)(fov / 2.0F))));
                if (result3f.getZ() < 0.0F) {
                    return new double[]{(double)(-result3f.getX() * scale_factor + (float)(Minecraft.getInstance().getMainWindow().getScaledWidth() / 2)), (double)((float)(Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - result3f.getY() * scale_factor)};
                }
            } catch (Exception var15) {
                var15.printStackTrace();
            }

            return null;
        }
    }
    public static double[] projectedView(double x, double y, double z) {
        if (Minecraft.getInstance().currentScreen != null && !(Minecraft.getInstance().currentScreen instanceof ChatScreen) && (Minecraft.getInstance().currentScreen == null)) {
            return null;
        } else {
            try {
                Vector3d camera_pos = Minecraft.getInstance().getRenderManager().info.getProjectedView();
                Quaternion camera_rotation_conj = Minecraft.getInstance().getRenderManager().getCameraOrientation().copy();
                camera_rotation_conj.conjugate();
                Vector3f result3f = new Vector3f((float)(camera_pos.x - x), (float)(camera_pos.y - y), (float)(camera_pos.z - z));
                result3f.transform(camera_rotation_conj);
                GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
                Method method = null;
                Method[] var11 = GameRenderer.class.getDeclaredMethods();
                int var12 = var11.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    Method m = var11[var13];
                    if (m.getParameterCount() == 3 && m.getParameterTypes()[2] == Boolean.TYPE && m.getParameterTypes()[1] == Float.TYPE && m.getParameterTypes()[0] == ActiveRenderInfo.class && m.getReturnType() == Double.TYPE) {
                        method = m;
                    }
                }

                method.setAccessible(true);
                float fov = ((Double)method.invoke(gameRenderer, Minecraft.getInstance().getRenderManager().info, 1, true)).floatValue();
                float half_height = (float)Minecraft.getInstance().getMainWindow().getScaledHeight() / 2.0F;
                float scale_factor = half_height / (result3f.getZ() * (float)Math.tan(Math.toRadians((double)(fov / 2.0F))));
                return new double[]{(double)(-result3f.getX() * scale_factor + (float)(Minecraft.getInstance().getMainWindow().getScaledWidth() / 2)), (double)((float)(Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - result3f.getY() * scale_factor),  (double)((float)(Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - result3f.getZ() * scale_factor)};
            } catch (Exception var15) {
                var15.printStackTrace();
            }

            return null;
        }
    }


    public static double interporate(double p_219803_0_, double p_219803_2_, double p_219803_4_) {
        return p_219803_2_ + p_219803_0_ * (p_219803_4_ - p_219803_2_);
    }

    public static void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255f);
    }

    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255f;
        float g = (float) (color >> 8 & 255) / 255f;
        float b = (float) (color & 255) / 255f;
        GlStateManager.color4f(r, g, b, alpha / 255f);
    }

    public static void drawLine(double x1, double y1, double x2, double y2, Color color) {
        GlStateManager.pushMatrix();
        RenderUtil.start();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderUtil.stop();
        GlStateManager.popMatrix();
    }

    public static void drawPoint(float x, float y, Color color) {
        GlStateManager.pushMatrix();
        RenderUtil.start();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderUtil.stop();
        GlStateManager.popMatrix();
    }

    public static void rectangle(final double x, final double y, final double width, final double height, final Color color) {
        start();

        if (color != null) {
            ColorUtil.glColor(color);
        }

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();

        stop();
    }

    public static void roundedRectangleNoGl(double x, double y, double width, double height, double radius, Color color) {
        start();

        if (color != null) {
            ColorUtil.glColor(color);
        }

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();

        stop();
    }

    public static void dropShadow(final int loops, final double x, final double y, final double width, final double height, final double opacity, final double edgeRadius) {
        GlStateManager.alphaFunc(516, 0);
        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();

        for (int margin = 1; margin <= loops; ++margin) {
            final double mariner = margin * 2;
            roundedRectangle(x - mariner / 2f, y - mariner / 2f,
                    width + mariner, height + mariner, edgeRadius,
                    new Color(0, 0, 0, (int) Math.max(0.5f, (opacity - mariner * 1.2) / 5.5f)));
        }
    }

    public static void drawImage(ResourceLocation image, double x, double y, double width, double height, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.enableDepthTest();
        minecraft.getTextureManager().bindTexture(image);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        bufferBuilder.pos(x, y + height, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(x + width, y + height, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(x + width, y, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).tex(1, 0).lightmap(0, 240).endVertex();
        bufferBuilder.pos(x, y, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).tex(0, 0).lightmap(0, 240).endVertex();
        tessellator.draw();
        RenderSystem.disableDepthTest();
        disableBlend();
    }


    @AllArgsConstructor
    public static class ImageRenderConstructor {
        public final BufferBuilder builder;
        public final Tessellator tessellator;
    }

    public static ImageRenderConstructor startBufferImage(ResourceLocation image) {
        Minecraft minecraft = Minecraft.getInstance();
        enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.enableDepthTest();
        minecraft.getTextureManager().bindTexture(image);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        return new ImageRenderConstructor(bufferBuilder,tessellator);
    }

    public static void endBufferImage(Tessellator tessellator) {
        tessellator.draw();
        RenderSystem.disableDepthTest();
        disableBlend();
    }

    public static void drawArrows(float x, float y, float size, float lineWidth, Color color, boolean isLeftArrow) {
        float arrowHalfWidth = size / 8;
        float arrowHalfHeight = size / 2;
        float arrowOffset = size / 2 + arrowHalfWidth;
        float scale = isLeftArrow ? -1 : 1;
        float rotation = isLeftArrow ? -45 : 135; //добавлено

        RenderSystem.translated(x, y, 0);
        RenderSystem.rotatef(180, 0, 1, 0);
        RenderSystem.rotatef(rotation, 0, 0, 1); //добавлено

        RenderSystem.disableTexture();
        enableBlend();

        blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        if (isLeftArrow) RenderSystem.translated(0, 7, 0);
        roundedRectangle(-arrowOffset - 0.5f * lineWidth, -arrowHalfHeight - 0.5f * lineWidth, arrowHalfWidth * 2 + lineWidth, arrowHalfHeight * 2, arrowHalfWidth, color);

        RenderSystem.rotatef(180, 0, 1, 0);
        roundedRectangle(arrowOffset - arrowHalfWidth * 2 - 0.5f * lineWidth, -arrowHalfHeight - 0.5f * lineWidth - (!isLeftArrow ? 7.5f : 0), arrowHalfWidth * 2 + lineWidth, arrowHalfHeight * 2 + lineWidth - (isLeftArrow ? 2.5f : 0f), arrowHalfWidth, color);

        RenderSystem.rotatef(-90, 0, 0, 1);
        RenderSystem.translated(scale * arrowHalfWidth, -arrowOffset, 0);
        RenderSystem.rotatef(180, 0, 1, 0);

        roundedRectangle(-arrowHalfWidth - 0.5f * lineWidth, -arrowOffset - arrowHalfWidth * 2 - 0.5f * lineWidth, arrowHalfWidth * 2 + lineWidth, arrowHalfHeight * 2 + lineWidth, arrowHalfWidth, color);

        RenderSystem.rotatef(180, 0, 1, 0);
        roundedRectangle(arrowHalfWidth - 0.5f * lineWidth + (!isLeftArrow ? 7f : 7), arrowOffset - arrowHalfWidth * 2 - 0.5f * lineWidth - (!isLeftArrow ? 2f : 2), arrowHalfWidth * 2 + lineWidth, arrowHalfHeight * 2 + lineWidth, arrowHalfWidth, color);

        disableBlend();
        RenderSystem.enableTexture();

    }

    private static final RoundedGradientQuadShader ROUNDED_GRADIENT_QUAD_SHADER = new RoundedGradientQuadShader();
    private static final RoundedGradientOutlineQuadShader ROUNDED_OUTLINE_GRADIENT_QUAD_SHADER = new RoundedGradientOutlineQuadShader();


    public static void roundedGradientRectangle(double x, double y, double width, double height, double radius, Color color1, Color color2, Color color3, Color color4) {
        ROUNDED_GRADIENT_QUAD_SHADER.draw(x, y, width, height, radius, color1, color2, color3, color4);
    }

    private static final ShaderUtil HUE_FRAG = new ShaderUtil("hue.frag");

    public static void drawHueRotation(float x, float y, float width, float height, float radius) {
        RenderUtil.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        HUE_FRAG.init();
        HUE_FRAG.setUniformf("alpha",1f);
        setupRoundedRectUniforms(x, y, width, height, radius, HUE_FRAG);
        ShaderUtil.drawIQuads(x - 1, y - 1, width + 2, height + 2);
        HUE_FRAG.unload();
        GlStateManager.disableBlend();
    }

    public static void drawHueRotation(float x, float y, float width, float height, float radius,float alpha) {
        RenderUtil.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        HUE_FRAG.init();
        HUE_FRAG.setUniformf("alpha",alpha);
        setupRoundedRectUniforms(x, y, width, height, radius, HUE_FRAG);
        ShaderUtil.drawIQuads(x - 1, y - 1, width + 2, height + 2);
        HUE_FRAG.unload();
        GlStateManager.disableBlend();
    }

    public static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtil roundedTexturedShader) {
        roundedTexturedShader.setUniformf("location", (float) (x * mw.getScaleFactor()),
                (float) ((mw.getHeight() - (height * mw.getScaleFactor())) - (y * mw.getScaleFactor())));
        roundedTexturedShader.setUniformf("rectSize", (float) (width * mw.getScaleFactor()), (float) (height * mw.getScaleFactor()));
        roundedTexturedShader.setUniformf("radius", (float) (radius * mw.getScaleFactor()));
    }

    public static void roundedOutlineGradientRectangle(double x, double y, double width, double height, double radius, double outlineThickness, Color color, Color color1, Color color2, Color color3, Color color4) {
        ROUNDED_OUTLINE_GRADIENT_QUAD_SHADER.draw((float) x, (float) y, (float) width, (float) height, (float) radius, (float) outlineThickness, color1, color2, color3, color4);
    }

    public static void roundedOutlineGradientRectangle(double x, double y, double width, double height, double radius, double outlineThickness, Color color1, Color color2, Color color3, Color color4) {
        ROUNDED_OUTLINE_GRADIENT_QUAD_SHADER.draw((float) x, (float) y, (float) width, (float) height, (float) radius, (float) outlineThickness, color1, color2, color3, color4);
    }

    private static final RoundedQuadShader ROUNDED_QUAD_SHADER = new RoundedQuadShader();
    private static final RoundedClientQuadShader ROUNDED_CLIENT_QUAD_SHADER = new RoundedClientQuadShader();


    public static void roundedRectangle(double x, double y, double width, double height, double radius, Color color) {
        ROUNDED_QUAD_SHADER.draw(x, y, width, height, radius, color);
        //RoundedUtils.drawSmoothRoundedRectWH((float) x, (float) y, (float) width, (float) height, (float) radius, color);
    }
    public static void roundedTextured(double x, double y, double width, double height, double radius, Runnable texture) {
        ROUNDED_QUAD_SHADER.drawTextured((float) x, (float) y, (float) width, (float) height, (float) radius, texture);
    }

    private static final ShaderUtil ROUNDED_RECT_MASK = new ShaderUtil("roundedmask.frag");

    public static void setupRoundedMaskUniforms(float x, float y, float width, float height, float radius, ShaderUtil roundedTexturedShader) {
        roundedTexturedShader.setUniformf("location", (float) (x * mw.getScaleFactor()),
                (float) ((mw.getFramebufferHeight() - (height * mw.getScaleFactor())) - (y * mw.getScaleFactor())));
        roundedTexturedShader.setUniformf("rectSize", (float) (width * mw.getScaleFactor()), (float) (height * mw.getScaleFactor()));
        roundedTexturedShader.setUniformf("radius", (float) (radius * mw.getScaleFactor()));
    }

    public static void drawMaskedRounded(float x, float y, float width, float height, float radius, Runnable content) {
        RenderUtil.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ROUNDED_RECT_MASK.init();
        setupRoundedMaskUniforms(x, y, width, height, radius, ROUNDED_RECT_MASK);

        ROUNDED_RECT_MASK.setUniformi("texture", 0);

        content.run();

        ShaderUtil.drawIQuads(x - 1, y - 1, width + 2, height + 2);
        ROUNDED_RECT_MASK.unload();
        GlStateManager.disableBlend();
    }
    public static void roundedRectangle(double x, double y, double width, double height, Vector4f radius, Color color) {
        ROUNDED_QUAD_SHADER.draw((float) x, (float) y, (float) width, (float) height, radius, color);
    }
    public static void roundedClientRectangle(double x, double y, double width, double height, double radius, double alpha) {
        StencilUtil.initStencil();
        ROUNDED_CLIENT_QUAD_SHADER.draw(x, y, width, height, radius, 1f);
        StencilUtil.bindReadStencilBuffer(1);
        ROUNDED_CLIENT_QUAD_SHADER.draw(0, 0, mw.getScaledWidth(), mw.getScaledHeight(), radius, alpha);
        StencilUtil.uninitStencilBuffer();
    }

    public static void roundedClientRectangleNoStencil(double x, double y, double width, double height, double radius, double alpha) {
        /*   StencilUtil.initStencil();*/
        ROUNDED_CLIENT_QUAD_SHADER.draw(x, y, width, height, radius, 1f);
        /*StencilUtil.bindReadStencilBuffer(1);
        ROUNDED_CLIENT_QUAD_SHADER.draw(0,0, mw.getScaledWidth(), mw.getScaledHeight(), radius,alpha);
        StencilUtil.uninitStencilBuffer();*/
    }

    public static void glowRoundedClientRectangle(double x, double y, double width, double height, double radius, double alpha, int iterations, int offset) {
        MatrixStack matrix = new MatrixStack();
        matrix.push();
        roundedClientRectangle(x, y, width, height, radius, alpha);
        int clampedIterations = Math.max(1, Math.min(10, iterations));
        int clampedOffset = Math.max(1, Math.min(10, offset));
        stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(true);
        roundedClientRectangle(x, y, width, height, radius, alpha);
        stencilFramebuffer.unbindFramebuffer();
        KawaseBloom.renderBlur(matrix, stencilFramebuffer.framebufferTexture, clampedIterations, clampedOffset);
        matrix.pop();
    }

    private static Framebuffer stencilFramebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);

    public static void glowRoundedRectangle(MatrixStack matrix, double x, double y, double width, double height, double radius, Color color, int iterations, int offset) {
        matrix.push();
        roundedRectangle(x, y, width, height, radius, color);
        int clampedIterations = Math.max(1, Math.min(10, iterations));
        int clampedOffset = Math.max(1, Math.min(10, offset));
        stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(true);
        roundedRectangle(x, y, width, height, radius, new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
        stencilFramebuffer.unbindFramebuffer();
        KawaseBloom.renderBlur(matrix, stencilFramebuffer.framebufferTexture, clampedIterations, clampedOffset);
        matrix.pop();
    }

    public static void applyBloom(int iterations, int offset, Runnable runnable) {
        if (!Nova.getInstance().getClientUI().isShadow()) return;
        MatrixStack matrix = new MatrixStack();
        matrix.push();
        int clampedIterations = Math.max(1, Math.min(10, iterations));
        int clampedOffset = Math.max(1, Math.min(10, offset));
        stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(true);
        runnable.run();
        stencilFramebuffer.unbindFramebuffer();
        KawaseBloom.renderBlur(matrix, stencilFramebuffer.framebufferTexture, clampedIterations, clampedOffset);
        matrix.pop();

    }
    public static void applyBloom(int iterations, int offset, Framebuffer stencilFramebuffer) {
        MatrixStack matrix = new MatrixStack();
        matrix.push();
        int clampedIterations = Math.max(1, Math.min(10, iterations));
        int clampedOffset = Math.max(1, Math.min(10, offset));
        KawaseBloom.renderBlur(matrix, stencilFramebuffer.framebufferTexture, clampedIterations, clampedOffset);
        matrix.pop();
    }

    public static void applyBlur(int iterations, int offset, Runnable runnable) {
        MatrixStack matrix = new MatrixStack();
        matrix.push();
        int clampedIterations = Math.max(1, Math.min(10, iterations));
        int clampedOffset = Math.max(1, Math.min(10, offset));
        stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(true);
        runnable.run();
        stencilFramebuffer.unbindFramebuffer();
        KawaseBlur.renderBlur(matrix, stencilFramebuffer.framebufferTexture, clampedIterations, clampedOffset);
        matrix.pop();
    }
    public static void applyClientBlur(int iterations, int offset, Runnable runnable) {

        MatrixStack matrix = new MatrixStack();
        matrix.push();
        int clampedIterations = Math.max(1, Math.min(10, iterations));
        int clampedOffset = Math.max(1, Math.min(10, offset));
        stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(true);
        runnable.run();
        stencilFramebuffer.unbindFramebuffer();
        KawaseBlur.renderClientBlur(matrix, stencilFramebuffer.framebufferTexture, clampedIterations, clampedOffset);
        matrix.pop();
    }

    private static final RoundedOutlineQuadShader ROUNDED_OUTLINE_QUAD_SHADER = new RoundedOutlineQuadShader();

    public static void roundedOutlineRectangle(double x, double y, double width, double height, double radius, double borderSize, Color color) {
        ROUNDED_OUTLINE_QUAD_SHADER.draw(x, y, width, height, radius, borderSize, color);
    }

    public static void rainbowRectangle(final double x, final double y, final double width, final double height) {
        start();

        GL11.glBegin(GL11.GL_QUADS);

        for (double position = x; position <= x + width; position += 0.5) {
            setColor(Color.getHSBColor((float) ((position - x) / width), 1, 1));

            GL11.glVertex2d(position, y);
            GL11.glVertex2d(position + 0.5f, y);
            GL11.glVertex2d(position + 0.5f, y + height);
            GL11.glVertex2d(position, y + height);
        }

        GL11.glEnd();

        stop();
    }

    public static void drawLine(double x, double y, double z, double x1, double y1, double z1, final Color color, final float width) {

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(width);

        setColor(color);
        GL11.glBegin(2);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }


    public static void renderBox(double x, double y, double z, LivingEntity entity, Color color) {
        double width = (entity.getRenderBoundingBox().maxX - entity.getRenderBoundingBox().minX + 0.2) / 2;
        double height = (entity.getRenderBoundingBox().maxY - entity.getRenderBoundingBox().minY + 0.2);

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1);
        RenderUtil.color(entity.hurtTime > 0 ? new Color(1.0f, 0.0f, 0.0f).getRGB() : color.getRGB(), 1);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }
    public static void renderBox(double x, double y, double z, AxisAlignedBB boundingBox, Color color) {
        double width = (boundingBox.maxX - boundingBox.minX + 0.2) / 2;
        double height = (boundingBox.maxY - boundingBox.minY + 0.2);

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1);
        RenderUtil.color(color.getRGB(), 1);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void renderFilledBox(double x, double y, double z, LivingEntity entity, Color color) {
        double width = (entity.getRenderBoundingBox().maxX - entity.getRenderBoundingBox().minX + 0.2) / 2;
        double height = (entity.getRenderBoundingBox().maxY - entity.getRenderBoundingBox().minY + 0.2);

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1);
        RenderUtil.color(entity.hurtTime > 0 ? new Color(1.0f, 0.0f, 0.0f).getRGB() : color.getRGB());
        RenderUtil.drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        bufferBuilder.begin(1, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void polygon(final double x, final double y, double radius, final double sides,
                               final boolean filled, final Color color) {
        radius /= 2;
        start();
        if (color != null)
            setColor(color);
        if (!filled) GL11.glLineWidth(2);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(filled ? GL11.GL_TRIANGLE_FAN : GL11.GL_LINE_STRIP);
        {
            for (double i = 0; i <= sides; i++) {
                final double angle = i * (Math.PI * 2) / sides;
                GL11.glVertex2d(x + (radius * Math.cos(angle)) + radius, y + (radius * Math.sin(angle)) + radius);
            }
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        stop();
    }

    public static void circle(final double x, final double y, final double radius, final boolean filled,
                              final Color color) {
        polygon(x, y, radius, 360, filled, color);
    }

    public static void triangle(final double x, final double y, final double sideLength, final boolean filled,
                                final Color color) {
        polygon(x, y, sideLength, 3, filled, color);
    }

    public static void drawCircle(final Entity entity, final double rad, final int color, final boolean shade) {
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glHint(3153, 4354);
        GL11.glDepthMask(false);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        if (shade) GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        final double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX);
        final double y = (entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) + Math.sin(System.currentTimeMillis() / 2E+2) + 1);
        final double z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ);

        Color c;

        for (float i = 0; i < Math.PI * 2; i += Math.PI * 2 / 64.F) {
            final double vecX = x + rad * Math.cos(i);
            final double vecZ = z + rad * Math.sin(i);

            c = new Color(255, 255, 255);

            if (shade) {
                GL11.glColor4f(c.getRed() / 255.F,
                        c.getGreen() / 255.F,
                        c.getBlue() / 255.F,
                        0
                );
                GL11.glVertex3d(vecX, y - Math.cos(System.currentTimeMillis() / 2E+2) / 2.0F, vecZ);
                GL11.glColor4f(c.getRed() / 255.F,
                        c.getGreen() / 255.F,
                        c.getBlue() / 255.F,
                        0.85F
                );
            }
            GL11.glVertex3d(vecX, y, vecZ);
        }

        GL11.glEnd();
        if (shade) GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableCull();
        GL11.glDisable(2848);
        GL11.glDisable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
        GL11.glColor3f(255, 255, 255);
    }

    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public static void resetColor() {
        GL11.glColor4f(1, 1, 1, 1);
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mw.getFramebufferWidth() || framebuffer.framebufferHeight != mw.getFramebufferHeight();
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        bufferBuilder.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }
}
