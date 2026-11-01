package dev.fesly.client.module.impl.render;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.render.DrawHudPostEvent;
import dev.fesly.impl.event.impl.render.DrawHudPreEvent;
import dev.fesly.impl.event.impl.render.ShaderEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.util.render.HudEffects;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.shader.GradientUtil;
import dev.fesly.impl.value.defaultValues.impl.BooleanValue;
import dev.fesly.impl.value.secondValues.EnumSetting;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Text;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
@ModuleInfo(name = "Interface", description = "", category = Category.DISPLAY, autoEnabled = true, allowDisable = false)
public class Interface extends Module {
    public BooleanValue moduleCase = new BooleanValue("Module Lower Case", this, false);
    public BooleanValue moduleSplit = new BooleanValue("Split Module Name", this, false);
    private final ArrayList<ModuleComponent> allModuleComponent = new ArrayList<>();
    public static ArrayList<ModuleComponent> activeModuleComponents = new ArrayList<>();
    public static final Animation animation = new Animation(Easing.EASE_OUT_CUBIC, 150);
    private final Framebuffer stencilFramebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    public static AnimationEnum animationEnum = AnimationEnum.Scaling;
    @Override
    public void init() {
        createArrayList();
        addSetting("animationMode", new EnumSetting<>("Animation Mode", this, Interface::getAnimationEnum, Interface::setAnimationEnum, AnimationEnum.class));
    }
    public static enum AnimationEnum {
        Scaling, Height;
    }
    @EventLink
    public Listener<ShaderEvent> onShader = event -> {
    };
    @EventLink
    public Listener<DrawHudPreEvent> onHudPre = event -> {
    };
    @EventLink
    public Listener<DrawHudPostEvent> onRender = event -> {
        if (mc.player.abilities.isFlying) {
        }
        allModuleComponent.forEach(module -> {
            module.getModule().animation.setDirection(module.getModule().isEnabled() && module.getModule().isVisible());
        });
        sortArrayList();
        if (mc.gameSettings.showDebugInfo) return;
        if (mc.currentScreen instanceof ChatScreen) {
            animation.run(14);
        } else {
            animation.run(0);
        }
        Nova client = Nova.getInstance();
        String user = client.getUser().username() + " §7|§f uid: " + client.getUser().uid() + " §7|§f rank: " + client.getUser().rank();
        Font font = Fonts.ROBOTO_REGULAR.get(13);
        float x = mw.getScaledWidth() - font.getWidth(user) - 4;
        float anim = animation.getNumberValue().floatValue();
        assert mc.player != null;
        String coords = "X: " + String.format("%.0f", mc.player.getPosX()) + " Y: " + String.format("%.0f", mc.player.getPosY()) + " Z: " + String.format("%.0f", mc.player.getPosZ());
        String coordsAd = "X: " + String.format("%.0f", mc.player.getPosX() / 8) + " Y: " + String.format("%.0f", mc.player.getPosY()) + " Z: " + String.format("%.0f", mc.player.getPosZ() / 8);
        font.draw(user, 2, mw.getScaledHeight() - 2 - font.getHeight() - anim, -1);
        font.draw("Coords : " + TextFormatting.GREEN + coords,
        2, mw.getScaledHeight() - 2 - font.getHeight() - anim - font.getHeight(), -1);
    };
    public void createArrayList() {
        if (allModuleComponent == null) return;
        Font font = Nova.getInstance().getClientUI().isThinFonts() ? Fonts.ROBOTO_REGULAR.get(16) : Fonts.ROBOTO_MEDIUM.get(16);
        allModuleComponent.clear();
        Nova.getInstance().getModuleManager().getAll().stream().sorted(Comparator.comparingDouble(module -> -font.getWidth(moduleSplit.getValue() ? moduleCase.getValue() ? module.getModuleInfo().name().toLowerCase().replace(" ", "") : module.getModuleInfo().name().replace(" ", "") : moduleCase.getValue() ? module.getModuleInfo().name().toLowerCase() : module.getModuleInfo().name() + (module.prefix != null ? " -> " + module.getPrefix() : " ")))).forEach(module -> {
            module.animation.setDirection(module.isEnabled() && module.isVisible());
            module.alpha.setDirection(module.isEnabled() && module.isVisible());
            allModuleComponent.add(new ModuleComponent(module));
        });
    }
    public void sortArrayList() {
        Font font = Fonts.ROBOTO_MEDIUM.get(16);
        activeModuleComponents = allModuleComponent.stream().filter(moduleComponent -> !moduleComponent.getModule().animation.finished(Direction.BACKWARDS)).sorted(Comparator.comparingDouble(module -> -font.getWidth((moduleSplit.getValue() ? moduleCase.getValue() ? module.getModule().getModuleInfo().name().toLowerCase().replace(" ", "") : module.getModule().getModuleInfo().name().replace(" ", "") : moduleCase.getValue() ? module.getModule().getModuleInfo().name().toLowerCase() : module.getModule().getModuleInfo().name() + (module.getModule().prefix != null ? " -> " + module.getModule().getPrefix() : " ")).replace(" ", "")))).collect(Collectors.toCollection(ArrayList::new));
    }
    public static class ModuleComponent {
        public Module module;
        public ModuleComponent(final Module module) {
            this.module = module;
        }
        @java.lang.SuppressWarnings("all")
        public Module getModule() {
            return this.module;
        }
        @java.lang.SuppressWarnings("all")
        public void setModule(final Module module) {
            this.module = module;
        }
        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Interface.ModuleComponent)) return false;
            final Interface.ModuleComponent other = (Interface.ModuleComponent) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$module = this.getModule();
            final java.lang.Object other$module = other.getModule();
            if (this$module == null ? other$module != null : !this$module.equals(other$module)) return false;
            return true;
        }
        @java.lang.SuppressWarnings("all")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof Interface.ModuleComponent;
        }
        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $module = this.getModule();
            result = result * PRIME + ($module == null ? 43 : $module.hashCode());
            return result;
        }
        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public java.lang.String toString() {
            return "Interface.ModuleComponent(module=" + this.getModule() + ")";
        }
    }
    @java.lang.SuppressWarnings("all")
    public static AnimationEnum getAnimationEnum() {
        return Interface.animationEnum;
    }
    @java.lang.SuppressWarnings("all")
    public static void setAnimationEnum(final AnimationEnum animationEnum) {
        Interface.animationEnum = animationEnum;
    }
}
