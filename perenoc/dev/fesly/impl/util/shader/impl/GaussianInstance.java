package dev.fesly.impl.util.shader.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.client.module.impl.render.ESP;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.shader.ShaderUtil;
import dev.fesly.impl.util.time.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.FloatBuffer;

import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;
import static org.lwjgl.opengl.GL11C.*;

public class GaussianInstance implements IAccess {

    public static ShaderUtil blurShader = new ShaderUtil("gaussianultrafast.frag");


    public static ShaderUtil ClientblurShader = new ShaderUtil("clientgaussian.frag");



    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    public static void setupUniforms(float dir1, float dir2, float radius) {
        blurShader.setUniformi("textureIn", 0);
        blurShader.setUniformf("texelSize", 1.0F / (float) mw.getWidth(), 1.0F / (float) mw.getHeight());
        blurShader.setUniformf("direction", dir1, dir2);
        blurShader.setUniformf("radius", radius);
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        for (int i = 1; i <= radius * 2; i++) {
            buffer.put(MathUtil.calculateGaussianValue(i, radius / 2));
        }
        buffer.rewind();

        glUniform1(blurShader.getUniform("weights"), buffer);
    }

    public static float gauss(float x, float sigma) {
        float pow = x / sigma;
        return (float) (1.0 / (Math.abs(sigma) * 2.50662827463) * Math.exp(-0.5 * pow * pow));
    }
    private static final long detime = System.currentTimeMillis();
    public static void setupClientUniforms(float dir1, float dir2, float radius) {


        ClientblurShader.setUniformi("u_diffuse_sampler", 0);
        ClientblurShader.setUniformf("u_texelSize", 1.0F / (float) mw.getWidth(), 1.0F / (float) mw.getHeight());
        ClientblurShader.setUniformf("resa", (float) (200 * mw.getScaleFactor()), (float) ((mw.getHeight() - (120 * mw.getScaleFactor())) - (200 * mw.getScaleFactor())));

        ClientblurShader.setUniformf("noColorfulColor", 0.12f,0.12f,0.12f);

        ClientblurShader.setUniformi("colorful",  nova.getClientUI().isColorful() && nova.getClientUI().isColorfulRectangles() ? 1 : 0);

        ClientblurShader.setUniformf("u_direction", dir1, dir2);
        //ClientblurShader.setUniformi("colorful", nova.getClientUI().isColorfulRectangles() ? 1 : 0);
        ClientblurShader.setUniformf("u_radius", radius);
        Color color1 = ClientUtil.getOneClientColor().brighter();
        Color color2 = ClientUtil.getTwoClientColor().brighter();
        Color color3 = ClientUtil.getThreeClientColor().brighter();
        Color color4 = ClientUtil.getFourClientColor().brighter();

        if (!nova.getClientUI().isColorful()) {
            color1 = new Color(0);
            color2 = new Color(0x1C1C1C);
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

        ClientblurShader.setUniformf("color1", r, g, b);
        ClientblurShader.setUniformf("color2", rr, gg, bb);
        ClientblurShader.setUniformf("color3", rrr, ggg, bbb);
        ClientblurShader.setUniformf("color4", rrrr, gggg, bbbb);


        ClientblurShader.setUniformf("time", (System.currentTimeMillis() - ESP.detime) / 1000F);
        ClientblurShader.setUniformf("colorSpeed", nova.getClientUI().getColorSpeed());
        ClientblurShader.setUniformf("gradientRange", nova.getClientUI().getGradientRange());
        ClientblurShader.setUniformf("exposure", nova.getClientUI().getColorfulBlurAlpha());
        ClientblurShader.setUniformf("deval", 12);
        ClientblurShader.setUniformi("rainbow",0);
    }


    public static TimerUtil timerUtil = new TimerUtil();
    public static void renderBlur(float radius) {
        if (Minecraft.getInstance().player != null) {

            if (timerUtil.hasReached(200000)) {
                //blurShader.cleanUp();
                if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fast)) {
                    blurShader = new ShaderUtil("gaussianultrafast.frag");
                } else if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fancy)) {
                    blurShader = new ShaderUtil("gaussian.frag");
                }
                timerUtil.reset();
            }
        }
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

        framebuffer = RenderUtil.createFrameBuffer(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        blurShader.init();

        setupUniforms(1, 0, radius);

        RenderUtil.bindTexture(Minecraft.getInstance().getFramebuffer().framebufferTexture);
        if (nova.getClientUI().isReverseBlurImage()) ShaderUtil.drawQuadReverse();
        else ShaderUtil.drawScaledQuads();

        framebuffer.unbindFramebuffer();
        blurShader.unload();

        Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
        blurShader.init();
        setupUniforms(0, 1, radius);

        RenderUtil.bindTexture(framebuffer.framebufferTexture);
        ShaderUtil.drawScaledQuads();
        blurShader.unload();

        RenderUtil.resetColor();
        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
    }

    public static void renderBlur(float radius,int texture) {
        if (Minecraft.getInstance().player != null) {

            if (timerUtil.hasReached(200000)) {
                //blurShader.cleanUp();
                if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fast)) {
                    blurShader = new ShaderUtil("gaussianultrafast.frag");
                } else if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fancy)) {
                    blurShader = new ShaderUtil("gaussian.frag");
                }
                timerUtil.reset();
            }
        }
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

        framebuffer = RenderUtil.createFrameBuffer(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        blurShader.init();

        setupUniforms(1, 0, radius);

        RenderUtil.bindTexture(Minecraft.getInstance().getFramebuffer().framebufferTexture);
        if (nova.getClientUI().isReverseBlurImage()) ShaderUtil.drawQuadReverse();
        else ShaderUtil.drawScaledQuads();

        framebuffer.unbindFramebuffer();
        blurShader.unload();

        Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
        blurShader.init();
        setupUniforms(0, 1, radius);

        RenderUtil.bindTexture(texture);
        ShaderUtil.drawScaledQuads();
        blurShader.unload();

        RenderUtil.resetColor();
        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
    }

    public static void renderClientBlur(float radius) {
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);


        framebuffer = RenderUtil.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        ClientblurShader.init();
        setupClientUniforms(1, 0, radius);

        RenderUtil.bindTexture(Minecraft.getInstance().getFramebuffer().framebufferTexture);

        ShaderUtil.drawScaledQuads();
        framebuffer.unbindFramebuffer();
        ClientblurShader.unload();

        Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
        ClientblurShader.init();
        setupClientUniforms(0, 1, radius);

        RenderUtil.bindTexture(framebuffer.framebufferTexture);
        ShaderUtil.drawScaledQuads();
        ClientblurShader.unload();

        RenderUtil.resetColor();
        GlStateManager.bindTexture(0);
    }

    public static void renderClientBlur(float radius,int texture) {
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);


        framebuffer = RenderUtil.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        ClientblurShader.init();
        setupClientUniforms(1, 0, radius);

        RenderUtil.bindTexture(Minecraft.getInstance().getFramebuffer().framebufferTexture);

        ShaderUtil.drawScaledQuads();

        framebuffer.unbindFramebuffer();
        ClientblurShader.unload();

        Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
        ClientblurShader.init();
        setupClientUniforms(0, 1, radius);

        RenderUtil.bindTexture(texture);
        ShaderUtil.drawScaledQuads();
        ClientblurShader.unload();

        RenderUtil.resetColor();
        GlStateManager.bindTexture(0);
    }

}
