package dev.fesly.impl.util.shader.impl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.client.module.impl.render.ESP;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.HudEffects;
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
        int maxWeights = nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fast) ? 16 : 256;
        int count = Math.min(maxWeights, Math.max(1, (int) (radius * 2)));
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(count);
        for (int i = 1; i <= count; i++) {
            buffer.put(MathUtil.calculateGaussianValue(i, radius / 2));
        }
        buffer.rewind();
        int weightsLoc = blurShader.getUniform("weights");
        if (weightsLoc > -1) {
            glUniform1(weightsLoc, buffer);
        }
    }
    public static float gauss(float x, float sigma) {
        float pow = x / sigma;
        return (float) (1.0 / (Math.abs(sigma) * 2.50662827463) * Math.exp(-0.5 * pow * pow));
    }
    private static final long detime = System.currentTimeMillis();
    public static void setupClientUniforms(float dir1, float dir2, float radius) {
        ClientblurShader.setUniformi("diffSampler", 0);
        ClientblurShader.setUniformi("u_diffuse_sampler", 0);
        ClientblurShader.setUniformi("u_other_sampler", 16);
        ClientblurShader.setUniformf("u_texel_size", 1.0F / (float) mw.getWidth(), 1.0F / (float) mw.getHeight());
        ClientblurShader.setUniformf("u_texelSize", 1.0F / (float) mw.getWidth(), 1.0F / (float) mw.getHeight());
        ClientblurShader.setUniformf("resa", (float) (200 * mw.getScaleFactor()), (float) ((mw.getHeight() - (120 * mw.getScaleFactor())) - (200 * mw.getScaleFactor())));
        ClientblurShader.setUniformf("noColorfulColor", 0.12f,0.12f,0.12f);
        ClientblurShader.setUniformi("colorful",  nova.getClientUI().isColorful() && nova.getClientUI().isColorfulRectangles() ? 1 : 0);
        ClientblurShader.setUniformf("u_direction", dir1, dir2);
        ClientblurShader.setUniformf("u_radius", radius);
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(128);
        int count = Math.min(128, Math.max(1, (int) radius + 1));
        for (int i = 0; i < count; i++) {
            buffer.put(MathUtil.calculateGaussianValue(i, Math.max(radius / 2f, 0.5f)));
        }
        for (int i = count; i < 128; i++) {
            buffer.put(0.0F);
        }
        buffer.rewind();
        int kernelLoc = ClientblurShader.getUniform("kernel");
        if (kernelLoc > -1) {
            glUniform1(kernelLoc, buffer);
        }
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
                if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fast)) {
                    blurShader = new ShaderUtil("gaussianultrafast.frag");
                } else if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fancy)) {
                    blurShader = new ShaderUtil("gaussian.frag");
                }
                timerUtil.reset();
            }
        }
        boolean stencil = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
        if (stencil) org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
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
        if (stencil) org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
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
                if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fast)) {
                    blurShader = new ShaderUtil("gaussianultrafast.frag");
                } else if (nova.getClientUI().getBlurMode().equals(ClientUI.BlurMode.Fancy)) {
                    blurShader = new ShaderUtil("gaussian.frag");
                }
                timerUtil.reset();
            }
        }
        boolean stencil = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
        if (stencil) org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
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
        if (stencil) org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
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
        if (HudEffects.framebuffer != null && HudEffects.framebuffer.framebufferTexture > 0) {
            renderClientBlur(radius, HudEffects.framebuffer.framebufferTexture);
        }
    }
    public static void renderClientBlur(float radius,int texture) {
        boolean stencil = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
        if (stencil) org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
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
        if (stencil) org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
        ClientblurShader.init();
        setupClientUniforms(0, 1, radius);
        org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE16);
        RenderUtil.bindTexture(texture);
        org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        RenderUtil.bindTexture(framebuffer.framebufferTexture);
        ShaderUtil.drawScaledQuads();
        ClientblurShader.unload();
        org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE16);
        org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0);
        org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        RenderUtil.resetColor();
        GlStateManager.bindTexture(0);
    }
}
