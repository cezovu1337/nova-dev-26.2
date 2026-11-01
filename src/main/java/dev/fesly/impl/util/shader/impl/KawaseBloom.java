package dev.fesly.impl.util.shader.impl;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.render.GLUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.shader.ShaderUtil;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
public class KawaseBloom implements IAccess {
    public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDownBloom");
    public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUpBloom");
    public static Framebuffer framebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    private static int currentIterations;
    private static final List<Framebuffer> framebufferList = new ArrayList<>();
    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();
        framebuffer = RenderUtil.createFrameBuffer(null, true);
        framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        framebufferList.add(framebuffer);
        for (int i = 0; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (mw.getFramebufferWidth() / Math.pow(2, i)), (int) (mw.getFramebufferHeight() / Math.pow(2, i)), true);
            currentBuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            currentBuffer.setFramebufferFilter(GL_LINEAR);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);
            framebufferList.add(currentBuffer);
        }
    }
    public static void renderBlur(MatrixStack matrix, int framebufferTexture, int iterations, int offset) {
        matrix.push();
        if (currentIterations != iterations || (framebuffer.framebufferWidth != mw.getFramebufferWidth() || framebuffer.framebufferHeight != mw.getFramebufferHeight())) {
            initFramebuffers(iterations);
            currentIterations = iterations;
        }
        RenderUtil.setAlphaLimit(0);
        renderFBO(framebufferList.get(1), framebufferTexture, kawaseDown, offset);
        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }
        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUp, offset);
        }
        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(true);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferTextureWidth, 1.0f / lastBuffer.framebufferTextureHeight);
        kawaseUp.setUniformf("iResolution", lastBuffer.framebufferTextureWidth, lastBuffer.framebufferTextureHeight);
        GlStateManager.activeTexture(GL13.GL_TEXTURE16);
        RenderUtil.bindTexture(framebufferTexture);
        GlStateManager.activeTexture(GL13.GL_TEXTURE0);
        RenderUtil.bindTexture(framebufferList.get(1).framebufferTexture);
        ShaderUtil.drawQuads();
        kawaseUp.unload();
        GlStateManager.activeTexture(GL13.GL_TEXTURE16);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GlStateManager.activeTexture(GL13.GL_TEXTURE0);
        mc.getFramebuffer().bindFramebuffer(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableTexture();
        org.lwjgl.opengl.GL33.glBindSampler(0, 0);
        RenderUtil.bindTexture(framebufferList.get(0).framebufferTexture);
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0);
        GLUtil.startBlend();
        ShaderUtil.drawQuads();
        GlStateManager.bindTexture(0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        RenderUtil.setAlphaLimit(0);
        GLUtil.endBlend();
        matrix.pop();
    }
    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtil shader, float offset) {
        framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        shader.init();
        GlStateManager.blendFunc(1, 1);
        RenderUtil.bindTexture(framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        shader.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        ShaderUtil.drawQuads();
        GlStateManager.disableBlend();
        shader.unload();
    }
}
