package dev.fesly.impl.interfaces.client;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.theme.Themes;
import dev.fesly.impl.client.screen.clickui.BaseScreen;
import dev.fesly.impl.interfaces.game.IMinecraft;
import dev.fesly.impl.interfaces.game.IWindow;
import dev.fesly.impl.util.render.AntiAliasing;
import dev.fesly.impl.util.render.HudEffects;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.util.shader.impl.GaussianInstance;
import dev.fesly.impl.util.shader.impl.KawaseBlur;
import net.minecraft.client.shader.Framebuffer;
import java.util.ArrayList;
import java.util.List;
public interface IAccess extends IMinecraft, IWindow {
    Nova nova = Nova.getInstance();
    List<Runnable> BLUR_RUNNABLES = new ArrayList<>();
    List<Runnable> BLOOM_RUNNABLES = new ArrayList<>();
    List<Runnable>  GLOW_RUNNABLES = new ArrayList<>();
    List<Runnable>  OUTLINE_RUNNABLES = new ArrayList<>();
    List<Runnable>  POST_RUNNABLES = new ArrayList<>();
    List<Runnable>  PRE_RUNNABLES = new ArrayList<>();
    List<Runnable>  PRE_COLORFUL_MASKED = new ArrayList<>();
    List<Runnable>  OUT_OF_TEXTURE_RUNNABLES = new ArrayList<>();
    static void addRunnableWithChecks(Runnable texture) {
        BLUR_RUNNABLES.add(texture);
    }
    static void renderHudRunnables() {
        AntiAliasing.stop(true, true, true);
        PRE_RUNNABLES.forEach(Runnable::run);
        if (!BLUR_RUNNABLES.isEmpty()) {
            Runnable b = () -> BLUR_RUNNABLES.forEach(Runnable::run);
            HudEffects.init(b);
            if (nova.getClientUI().isBlur()) {
                KawaseBlur.renderBlur(new MatrixStack(), HudEffects.framebuffer.framebufferTexture, 3, (int) Math.max(1, nova.getClientUI().getBlurRadius() / 4));
                GaussianInstance.renderClientBlur(1);
            }
            if (nova.getClientUI().isOutline()) {
                HudEffects.renderOutlinesWithout(true);
            }
            if (nova.getClientUI().isShadow()) {
                HudEffects.renderGlowWithout(true);
            }
        }
        POST_RUNNABLES.forEach(Runnable::run);
        if (!BLOOM_RUNNABLES.isEmpty() && (Nova.getInstance().getClientUI().isBloomRender() || Nova.getInstance().getClientUI().isShadow())) {
            RenderUtil.applyBloom(
                3, 1,
                () -> BLOOM_RUNNABLES.forEach(Runnable::run));
        }
        clearHudRunnables();
        AntiAliasing.stop(true, true, true);
    }
    static void clearHudRunnables() {
        BLUR_RUNNABLES.clear();
        BLOOM_RUNNABLES.clear();
        POST_RUNNABLES.clear();
        GLOW_RUNNABLES.clear();
        OUTLINE_RUNNABLES.clear();
        PRE_RUNNABLES.clear();
    }
    default BaseScreen getClickGui() {
        return nova.getClickGui();
    }
    default <T extends Module> T getModule(final Class<T> clazz) {
        return nova.getModuleManager().get(clazz);
    }
    default Themes getTheme() {
        return nova.getThemeManager().getTheme();
    }
    default void setTheme(Themes theme) {
        nova.getThemeManager().setTheme(theme);
    }
}
