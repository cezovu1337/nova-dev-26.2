package dev.fesly.impl.util.shader.impl;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.shader.ShaderUniforms;
import dev.fesly.impl.util.shader.ShaderUtil;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.nio.FloatBuffer;
import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;
public class RoundedQuadShader {
    private final ShaderUtil program = new ShaderUtil("roundedquad.frag");
    private final ShaderUtil programAllQuad = new ShaderUtil("roundedquadall.frag");
    private final ShaderUtil roundedTextured = new ShaderUtil("roundedtextured.frag");
    public void draw(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        int programId = this.program.getProgramID();
        program.init();
        ShaderUniforms.uniform2f(programId, "u_size", width, height);
        ShaderUniforms.uniform1f(programId, "u_radius", radius);
        ShaderUniforms.uniform4f(programId, "u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        ShaderUtil.drawQuads(x, y, width, height);
        GlStateManager.disableBlend();
        program.unload();
    }
    public void draw(final float x, final float y, final float width, final float height, final Vector4f radius, final Color color) {
        int programId = this.programAllQuad.getProgramID();
        programAllQuad.init();
        ShaderUniforms.uniform2f(programId, "u_size", width, height);
        ShaderUniforms.uniform1f(programId, "radius[0]", radius.getX());
        ShaderUniforms.uniform1f(programId, "radius[1]", radius.getY());
        ShaderUniforms.uniform1f(programId, "radius[2]", radius.getZ());
        ShaderUniforms.uniform1f(programId, "radius[3]", radius.getW());
        ShaderUniforms.uniform4f(programId, "u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        ShaderUtil.drawQuads(x, y, width, height);
        GlStateManager.disableBlend();
        programAllQuad.unload();
    }
    public void drawTextured(final float x, final float y, final float width, final float height, final float radius, final Runnable texture) {
        int programId = this.roundedTextured.getProgramID();
        RenderUtil.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        roundedTextured.init();
        ShaderUniforms.uniform2f(programId, "u_size", width, height);
        ShaderUniforms.uniform1f(programId, "u_radius", radius);
        roundedTextured.setUniformi("texture",0);
        texture.run();
        ShaderUtil.drawIQuads(x - 1, y - 1, width + 2, height + 2);
        roundedTextured.unload();
        GlStateManager.disableBlend();
    }
    public void draw(final double x, final double y, final double width, final double height, final double radius, final Color color) {
        draw((float) x, (float) y, (float) width, (float) height, (float) radius, color);
    }
}
