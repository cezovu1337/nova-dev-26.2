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
    public static boolean notCustomRange = true;
    private static Color customColor = Color.BLACK;
    private static float customRng = 1.0F;
    public static float customAlpha = 0.7F;
    public static void createFrameBuffers() {
        try {
            framebuffer = RenderUtil.createFrameBuffer(framebuffer);
            framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            outlineShader.framebuffer = RenderUtil.createFrameBuffer(outlineShader.framebuffer);
            outlineShader.framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static boolean unscaled;
    private static boolean gradient;
    public static Color color1 = Color.WHITE;
    public static Color color2 = Color.WHITE;
    public static Color color3 = Color.WHITE;
    public static Color color4 = Color.WHITE;
    public static float x = -1;
    public static float y = -1;
    public static float height = -1;
    public static void renderOutlines(Runnable textura) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        texture = textura;
        init(textura);
        ESP.render2DForHud(framebuffer, 0.2F, ClientUI.getShadowAlpha() * 2, ClientUI.getShadowAlpha() * 2 / 3.3F, ClientUI.isOutline(), false, false, false);
    }
    public static void renderGlow(Runnable textura, boolean full) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        texture = textura;
        init(textura);
        ESP.render2DForHud(framebuffer, ClientUI.getShadowRadius() * 2, ClientUI.getShadowAlpha() * 2 + ClientUI.getShadowCompress() / 4, ClientUI.getShadowAlpha() * 2 / 3.3F, false, ClientUI.isShadow(), false, full);
    }
    public static void renderGlowCustomExposure(Runnable textura, boolean full, float exposure) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        init(textura);
        ESP.render2DForHud(framebuffer, ClientUI.getShadowRadius() * 2, exposure * 1.7F + ClientUI.getShadowCompress() / 4, exposure * 1.7F / 3.3F, false, ClientUI.isShadow(), false, full);
    }
    public static void renderOutlinesWithout(boolean draw) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        ESP.render2DForHud(framebuffer, draw ? 0.2F : 0.0F, draw ? ClientUI.getShadowAlpha() * 2 : 0, (draw ? ClientUI.getShadowAlpha() * 2 : 0) / 3.3F, draw && ClientUI.isOutline(), false, false, false);
    }
    public static void renderGlowWithout(boolean full) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        ESP.render2DForHud(framebuffer, ClientUI.getShadowRadius() * 2, ClientUI.getShadowAlpha() + ClientUI.getShadowCompress() / 4, ClientUI.getShadowAlpha() / 3.3F, false, ClientUI.isShadow(), false, full);
        framebuffer.framebufferClear();
    }
    public static void renderGlowCustomExposureWithout(boolean full, float exposure) {
        createFrameBuffers();
        unscaled = false;
        gradient = false;
        ESP.render2DForHud(framebuffer, ClientUI.getShadowRadius() * 2, exposure * 1.7F + ClientUI.getShadowCompress() / 4, exposure * 1.7F / 3.3F, false, ClientUI.isShadow(), false, full);
    }
    public static void init(Runnable texture) {
        unscaled = false;
        gradient = false;
        createFrameBuffers();
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        if (texture != null) texture.run();
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        RenderSystem.disableLighting();
        RenderSystem.resetColor();
    }
    private static void init(Runnable texture, boolean unScaled) {
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
    private static void init(Runnable texture, boolean unScaled, boolean gradiented) {
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
    @java.lang.SuppressWarnings("all")
    public static void setCustomColor(final Color customColor) {
        HudEffects.customColor = customColor;
    }
    @java.lang.SuppressWarnings("all")
    public static void setCustomRng(final float customRng) {
        HudEffects.customRng = customRng;
    }
    @java.lang.SuppressWarnings("all")
    public static void setCustomAlpha(final float customAlpha) {
        HudEffects.customAlpha = customAlpha;
    }
    @java.lang.SuppressWarnings("all")
    public static void setColor1(final Color color1) {
        HudEffects.color1 = color1;
    }
    @java.lang.SuppressWarnings("all")
    public static void setColor2(final Color color2) {
        HudEffects.color2 = color2;
    }
    @java.lang.SuppressWarnings("all")
    public static void setColor3(final Color color3) {
        HudEffects.color3 = color3;
    }
    @java.lang.SuppressWarnings("all")
    public static void setColor4(final Color color4) {
        HudEffects.color4 = color4;
    }
    @java.lang.SuppressWarnings("all")
    public static void setX(final float x) {
        HudEffects.x = x;
    }
    @java.lang.SuppressWarnings("all")
    public static void setY(final float y) {
        HudEffects.y = y;
    }
    @java.lang.SuppressWarnings("all")
    public static void setHeight(final float height) {
        HudEffects.height = height;
    }
}
