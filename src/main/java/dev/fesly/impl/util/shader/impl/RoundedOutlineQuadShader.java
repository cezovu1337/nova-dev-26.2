package dev.fesly.impl.util.shader.impl;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.impl.util.shader.ShaderUniforms;
import dev.fesly.impl.util.shader.ShaderUtil;
import org.lwjgl.opengl.GL11;
import java.awt.*;
public class RoundedOutlineQuadShader {
    private final ShaderUtil program = new ShaderUtil("roundedoutline.glsl");
    public void draw(float x, float y, float width, float height, float radius, float borderSize, Color color) {
        int programId = this.program.getProgramID();
        program.init();
        ShaderUniforms.uniform2f(programId, "u_size", width, height);
        ShaderUniforms.uniform1f(programId, "u_radius", radius);
        ShaderUniforms.uniform1f(programId, "u_border_size", borderSize);
        ShaderUniforms.uniform4f(programId, "u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        ShaderUtil.drawQuads(x, y, width, height);
        GlStateManager.disableBlend();
        program.unload();
    }
    public void draw(double x, double y, double width, double height, double radius, double borderSize, Color color) {
        draw((float) x, (float) y, (float) width, (float) height, (float) radius, (float) borderSize, color);
    }
}
