package dev.fesly.impl.util.shader.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.render.GLUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.shader.ShaderUtil;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.fesly.impl.util.render.HudEffects.detime;
import static org.lwjgl.opengl.GL11.*;

public class KawaseBlur implements IAccess {

    public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDown");
    public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUp");

    public static ShaderUtil clientKawaseDown = new ShaderUtil("kawaseclientdown.frag");
    public static ShaderUtil clientKawaseUp = new ShaderUtil("kawaseclientup.frag");


    public static Framebuffer framebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);

    public static void setupUniforms(float offset) {
        kawaseDown.setUniformf("offset", offset, offset);
        kawaseUp.setUniformf("offset", offset, offset);
    }

    private static int currentIterations;

    private static final List<Framebuffer> framebufferList = new ArrayList<>();

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();

        //Have to make the framebuffer null so that it does not try to delete a framebuffer that has already been deleted
        framebufferList.add(framebuffer = RenderUtil.createFrameBuffer(null));

        for (int i = 1; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (mw.getFramebufferWidth() / Math.pow(2, i)), (int) (mw.getFramebufferHeight() / Math.pow(2, i)), true);
            currentBuffer.setFramebufferFilter(GL_LINEAR);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }


    public static void renderBlur(MatrixStack matrix, int stencilFrameBufferTexture, int iterations, int offset) {
        matrix.push();
        if (currentIterations != iterations || framebuffer.framebufferWidth != mw.getWidth() || framebuffer.framebufferHeight != mw.getHeight()) {
            initFramebuffers(iterations);
            currentIterations = iterations;
        }

        renderFBO(framebufferList.get(1), mc.getFramebuffer().framebufferTexture, kawaseDown, offset);

        //Downsample
        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }

        //Upsample
        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUp, offset);
        }


        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight);
        kawaseUp.setUniformf("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        RenderUtil.bindTexture(stencilFrameBufferTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderUtil.bindTexture(framebufferList.get(1).framebufferTexture);
        ShaderUtil.drawQuads();
        kawaseUp.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        RenderUtil.bindTexture(framebufferList.get(0).framebufferTexture);
        //RenderUtil.setAlphaLimit(0);
        GLUtil.startBlend();
        ShaderUtil.drawQuads();
        GlStateManager.bindTexture(0);
        GLUtil.endBlend();
        matrix.pop();
    }

    public static void renderClientBlur(MatrixStack matrix, int stencilFrameBufferTexture, int iterations, int offset) {
        matrix.push();
        if (currentIterations != iterations || framebuffer.framebufferTextureWidth != mw.getWidth() || framebuffer.framebufferTextureHeight != mw.getHeight()) {
            initFramebuffers(iterations);
            currentIterations = iterations;
        }

        renderFBO(framebufferList.get(1), mc.getFramebuffer().framebufferTexture, clientKawaseDown, offset);

        //Downsample
        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, clientKawaseDown, offset);
        }

        //Upsample
        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, clientKawaseUp, offset);
        }

        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(true);
        mc.getFramebuffer().bindFramebuffer(true);
        clientKawaseUp.init();
        clientKawaseUp.setUniformf("offset", offset, offset);
        clientKawaseUp.setUniformi("inTexture", 0);
        clientKawaseUp.setUniformi("check", 1);
        clientKawaseUp.setUniformi("textureToCheck", 16);
        clientKawaseUp.setUniformf("halfpixel", 1.0f / mw.getWidth(), 1.0f / mw.getHeight());
        clientKawaseUp.setUniformf("iResolution",mw.getWidth(), mw.getHeight());
        //GL13.glActiveTexture(GL13.GL_TEXTURE16);
        //RenderUtil.bindTexture(stencilFrameBufferTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderUtil.bindTexture(framebufferList.get(1).framebufferTexture);
        ShaderUtil.drawQuad();
        RenderSystem.color4f(1f,1f,1f,1f);
        clientKawaseUp.unload();
        RenderUtil.resetColor();
        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
        matrix.pop();
    }

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtil shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.init();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.bindTexture(framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        shader.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);



        Color color1 = ClientUtil.getOneClientColor().brighter();
        Color color2 = ClientUtil.getTwoClientColor().brighter();
        Color color3 = ClientUtil.getThreeClientColor().brighter();
        Color color4 = ClientUtil.getFourClientColor().brighter();

        if (!nova.getClientUI().isColorful()) {
            color1 = new Color(0);
            color2 = new Color(0x1C1C1C);
        }
        if (!nova.getClientUI().isColorful()) {
            color1 = color1.brighter().brighter().brighter().brighter();
            color2 = color2.brighter().brighter().brighter().brighter();
        }
        float r = color1.getRed() / 255f;
        float g = color1.getGreen() / 255f;
        float b = color1.getBlue() / 255f;
        float rr = color2.getRed() / 255f;
        float gg = color2.getGreen() / 255f;
        float bb = color2.getBlue() / 255f;
        float rrr = color3.getRed() / 255f;
        float ggg = color3.getGreen() / 255f;
        float bbb = color3.getBlue() / 255f;
        float rrrr = color4.getRed() / 255f;
        float gggg = color4.getGreen() / 255f;
        float bbbb = color4.getBlue() / 255f;

        shader.setUniformf("color1", r, g, b);
        shader.setUniformf("color2", rr, gg, bb);
        shader.setUniformf("color3", rrr, ggg, bbb);
        shader.setUniformf("color4", rrrr, gggg, bbbb);

        shader.setUniformf("exposure", nova.getClientUI().getColorfulBlurAlpha());
        shader.setUniformi("avoidTexture", 1);
        shader.setUniformf("time", (System.currentTimeMillis() - detime) / 1000F);
        shader.setUniformf("colorSpeed", nova.getClientUI().getColorSpeed());
        shader.setUniformi("blurAgain", 0);
        shader.setUniformi("rainbow", 0);
        shader.setUniformf("blurAgainRadius", 0.01f);
        shader.setUniformf("gradientRange", nova.getClientUI().getGradientRange());
        ShaderUtil.drawQuads();
        GLUtil.endBlend();
        shader.unload();
    }


}
