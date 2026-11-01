package dev.fesly.impl.util.render;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.impl.interfaces.client.IAccess;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL11.*;
public class StencilUtil implements IAccess {
    private static void recreate(final Framebuffer framebuffer) {
        if (framebuffer.depthBuffer > -1) {
            GL30.glDeleteRenderbuffers(framebuffer.depthBuffer);
        }
        final int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, framebuffer.framebufferTextureWidth, framebuffer.framebufferTextureHeight);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        framebuffer.depthBuffer = depthBuffer;
    }
    public static void checkSetupFBO(final Framebuffer framebuffer) {
        if (framebuffer != null) {
            if (framebuffer.framebufferObject > 0 && framebuffer.depthBuffer == -1) {
                recreate(framebuffer);
            }
        }
    }
    public static void initStencil() {
        mc.getFramebuffer().bindFramebuffer(false);
        checkSetupFBO(mc.getFramebuffer());
        glStencilMask(0xFF);
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glColorMask(false, false, false, false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
    }
    public static void bindWriteStencilBuffer() {
        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glColorMask(false, false, false, false);
    }
    public static void bindReadStencilBuffer(final int ref) {
        glColorMask(true, true, true, true);
        glStencilFunc(GL_EQUAL, ref, 1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }
    public static void uninitStencilBuffer() {
        glStencilMask(0xFF);
        glDisable(GL_STENCIL_TEST);
        glColorMask(true, true, true, true);
        GlStateManager.disableBlend();
        GlStateManager.disableAlphaTest();
    }
}
