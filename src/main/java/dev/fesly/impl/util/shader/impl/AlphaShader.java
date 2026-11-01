package dev.fesly.impl.util.shader.impl;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.Nova;
import dev.fesly.impl.event.impl.render.AlphaEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.shader.ShaderUniforms;
import dev.fesly.impl.util.shader.ShaderUtil;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
public class AlphaShader implements IAccess {
    private final ShaderUtil program = new ShaderUtil("alpha.frag");
    private Framebuffer inputFramebuffer = new Framebuffer(mw.getWidth(), mw.getHeight(), true);
    private float alpha;
    public void run() {
        this.update();
        this.inputFramebuffer.bindFramebuffer(true);
        Nova.getInstance().getEventBus().handle(new AlphaEvent());
        mc.getFramebuffer().bindFramebuffer(true);
        final int programId = this.program.getProgramID();
        program.init();
        ShaderUniforms.uniform1i(programId, "u_diffuse_sampler", 0);
        ShaderUniforms.uniform1f(programId, "u_alpha", alpha);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        this.inputFramebuffer.bindFramebufferTexture();
        ShaderUtil.drawQuads();
        GlStateManager.disableBlend();
        program.unload();
    }
    public void update() {
        if (mw.getWidth() != inputFramebuffer.framebufferWidth || mw.getHeight() != inputFramebuffer.framebufferHeight) {
            inputFramebuffer.deleteFramebuffer();
            inputFramebuffer = new Framebuffer(mw.getWidth(), mw.getHeight(), true);
        } else {
            inputFramebuffer.framebufferClear();
        }
        inputFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
    }
    @java.lang.SuppressWarnings("all")
    public void setAlpha(final float alpha) {
        this.alpha = alpha;
    }
}
