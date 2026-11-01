package dev.fesly.impl.util.shader.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.impl.interfaces.game.IWindow;
import dev.fesly.impl.util.shader.ShaderUniforms;
import dev.fesly.impl.util.shader.ShaderUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RoundedGradientOutlineQuadShader implements IWindow {

    private final ShaderUtil program = new ShaderUtil("gradientoutline.glsl");

    /**
     * Draws a rounded rectangle at the given coordinates with the given lengths
     *
     * @param x      The top left x coordinate of the rectangle
     * @param y      The top y coordinate of the rectangle
     * @param width  The width which is used to determine the second x rectangle
     * @param height The height which is used to determine the second y rectangle
     * @param radius The radius for the corners of the rectangles (>0)
     * @param color1 Color 1
     * @param color2 Color 2
     * @param color3 Color 3
     * @param color4 Color 4
     */

    public void draw(float x, float y, float width, float height, float radius, float borderSize, Color color1, Color color2, Color color3, Color color4) {
        int programId = this.program.getProgramID();
        program.init();
        ShaderUniforms.uniform2f(programId, "u_size", width, height);
        ShaderUniforms.uniform1f(programId, "u_radius", radius);
        ShaderUniforms.uniform1f(programId, "u_border_size", borderSize);
        ShaderUniforms.uniform4f(programId, "color1", color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
        ShaderUniforms.uniform4f(programId, "color2", color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
        ShaderUniforms.uniform4f(programId, "color3", color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
        ShaderUniforms.uniform4f(programId, "color4", color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        ShaderUtil.drawQuads(x, y, width, height);
        GlStateManager.disableBlend();
        program.unload();
    }

}
