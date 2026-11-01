package dev.fesly.client.module.impl.misc;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.render.CustomFog;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.EnumSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
@ModuleInfo(name = "Swing Animations", category = Category.MISC, description = "Анимация на русках")
public class SwingAnimations extends Module {
    public static float right_x = 0.0F;
    public static float right_y = 0.0F;
    public static float right_z = 0.0F;
    public static float left_x = 0.0F;
    public static float left_y = 0.0F;
    public static float left_z = 0.0F;
    public static boolean onlyKillAura;
    public static SwingMods swingMod = SwingMods.Default;
    public static boolean animations = false;
    @Override
    public void init() {
        super.init();
        addSetting("animations", new BooleanSetting("Animations", this, SwingAnimations::isAnimations, SwingAnimations::setAnimations));
        addSetting("swingMode", new EnumSetting<>("Animation Mode", this, SwingAnimations::isAnimations, SwingAnimations::getSwingMod, SwingAnimations::setSwingMod, SwingMods.class));
        addSetting("right_x", new NumberSetting("Right X", this, SwingAnimations::getRight_x, SwingAnimations::setRight_x, "%.1f", -2, 2));
        addSetting("right_y", new NumberSetting("Right Y", this, SwingAnimations::getRight_y, SwingAnimations::setRight_y, "%.1f", -2, 2));
        addSetting("right_z", new NumberSetting("Right Z", this, SwingAnimations::getRight_z, SwingAnimations::setRight_z, "%.1f", -2, 2));
        addSetting("left_x", new NumberSetting("Left X", this, SwingAnimations::getLeft_x, SwingAnimations::setLeft_x, "%.1f", -2, 2));
        addSetting("left_y", new NumberSetting("Left Y", this, SwingAnimations::getLeft_y, SwingAnimations::setLeft_y, "%.1f", -2, 2));
        addSetting("left_z", new NumberSetting("Left Z", this, SwingAnimations::getLeft_z, SwingAnimations::setLeft_z, "%.1f", -2, 2));
    }
    public static boolean enabled() {
        return Nova.getInstance().getModuleManager().get(SwingAnimations.class).isEnabled();
    }
    public enum SwingMods {
        Default, Rotate;
    }
    @java.lang.SuppressWarnings("all")
    public static float getRight_x() {
        return SwingAnimations.right_x;
    }
    @java.lang.SuppressWarnings("all")
    public static void setRight_x(final float right_x) {
        SwingAnimations.right_x = right_x;
    }
    @java.lang.SuppressWarnings("all")
    public static float getRight_y() {
        return SwingAnimations.right_y;
    }
    @java.lang.SuppressWarnings("all")
    public static void setRight_y(final float right_y) {
        SwingAnimations.right_y = right_y;
    }
    @java.lang.SuppressWarnings("all")
    public static float getRight_z() {
        return SwingAnimations.right_z;
    }
    @java.lang.SuppressWarnings("all")
    public static void setRight_z(final float right_z) {
        SwingAnimations.right_z = right_z;
    }
    @java.lang.SuppressWarnings("all")
    public static float getLeft_x() {
        return SwingAnimations.left_x;
    }
    @java.lang.SuppressWarnings("all")
    public static void setLeft_x(final float left_x) {
        SwingAnimations.left_x = left_x;
    }
    @java.lang.SuppressWarnings("all")
    public static float getLeft_y() {
        return SwingAnimations.left_y;
    }
    @java.lang.SuppressWarnings("all")
    public static void setLeft_y(final float left_y) {
        SwingAnimations.left_y = left_y;
    }
    @java.lang.SuppressWarnings("all")
    public static float getLeft_z() {
        return SwingAnimations.left_z;
    }
    @java.lang.SuppressWarnings("all")
    public static void setLeft_z(final float left_z) {
        SwingAnimations.left_z = left_z;
    }
    @java.lang.SuppressWarnings("all")
    public static SwingMods getSwingMod() {
        return SwingAnimations.swingMod;
    }
    @java.lang.SuppressWarnings("all")
    public static void setSwingMod(final SwingMods swingMod) {
        SwingAnimations.swingMod = swingMod;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isAnimations() {
        return SwingAnimations.animations;
    }
    @java.lang.SuppressWarnings("all")
    public static void setAnimations(final boolean animations) {
        SwingAnimations.animations = animations;
    }
}
