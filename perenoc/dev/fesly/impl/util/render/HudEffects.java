package dev.fesly.impl.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.Nova;
import dev.fesly.client.module.impl.render.ESP;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.Colors;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.shader.ShaderUtil;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.nio.FloatBuffer;

import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;

public class HudEffects implements IAccess {
    private static final ShaderUtil outlineShader = new ShaderUtil("outline");
    private static final ShaderUtil glowShader = new ShaderUtil("clientbloom");
    private static final ShaderUtil glowOutlineShader = new ShaderUtil("clientoutline");


    public static Framebuffer framebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), false);

    public static Runnable texture;
    private static Minecraft mc = Minecraft.getInstance();
    public static boolean notCustomRange = true; // true - client colors ; false - custom colors;
    @Setter private static Color customColor = Color.BLACK;
    @Setter private static float customRng = 1f;
    @Setter public static float customAlpha = 0.7f;


    public static void createFrameBuffers() {
        try {
            framebuffer = RenderUtil.createFrameBuffer(framebuffer);
            outlineShader.framebuffer = RenderUtil.createFrameBuffer(outlineShader.framebuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static boolean unscaled;
    private static boolean gradient;

    @Setter
    public  static
    Color color1 = Color.WHITE;
    @Setter
    public static
    Color color2 = Color.WHITE;
    @Setter
    public static
    Color color3 = Color.WHITE;
    @Setter
    public static
    Color color4 = Color.WHITE;
    @Setter
    public static
    float x = -1;
    @Setter
    public static
    float y = -1;
    @Setter
    public static
    float height = -1;

    public static void renderOutlines(Runnable textura) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        texture = textura;
        init(textura);
        ESP.render2D(framebuffer,0.2f, ClientUI.getShadowAlpha() * 2, true, false,false);
    }
    public static void renderGlow(Runnable textura,boolean full) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        texture = textura;
        init(textura);
        ESP.render2D(framebuffer,ClientUI.getShadowRadius() * 2 ,ClientUI.getShadowAlpha() * 2, false, false,full);
    }
    public static void renderGlowCustomExposure(Runnable textura,boolean full,float exposure) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        init(textura);
        ESP.render2D(framebuffer,ClientUI.getShadowRadius() * 2, exposure * 1.7f, false, false,full);
    }
    public static void renderOutlinesWithout(boolean draw) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        ESP.render2D(framebuffer,draw ? 0.2f : 0f, draw ?ClientUI.getShadowAlpha() * 2 : 0, true, false,false);
    }
    public static void renderGlowWithout(boolean full) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        ESP.render2D(framebuffer,ClientUI.getShadowRadius() * 2 ,ClientUI.getShadowAlpha(), false, false,full);
        framebuffer.framebufferClear();
    }
    public static void renderGlowCustomExposureWithout(boolean full,float exposure) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        ESP.render2D(framebuffer,ClientUI.getShadowRadius() * 2, exposure * 1.7f, false, false,full);
    }
    public static void init(Runnable texture) {
        unscaled = false;
        gradient = false;
        createFrameBuffers();

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        if (texture != null)
            texture.run();
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        RenderSystem.disableLighting();
        RenderSystem.resetColor();
    }

    private static void init(Runnable texture,boolean unScaled) {
        unscaled = unScaled;
        gradient = false;
        createFrameBuffers();

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        texture.run();
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        RenderSystem.disableLighting();
        RenderSystem.resetColor();
    }
    private static void init(Runnable texture,boolean unScaled,boolean gradiented) {
        unscaled = unScaled;
        gradient = gradiented;
        createFrameBuffers();

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        texture.run();
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        RenderSystem.disableLighting();
        RenderSystem.resetColor();
    }
    public static final long detime = System.currentTimeMillis();
    private static ClientUI ClientUI = Nova.getInstance().getClientUI();
}
