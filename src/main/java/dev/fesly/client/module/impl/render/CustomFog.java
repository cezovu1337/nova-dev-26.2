package dev.fesly.client.module.impl.render;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.ColorSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import java.awt.*;
@ModuleInfo(name = "Custom Fog", category = Category.RENDER, description = "Изменяет туман под ваш вкус и цвет :) ")
public class CustomFog extends Module {
    public static float fogRadius = 0.15F;
    public static boolean customColor = true;
    public static boolean animated = true;
    public static Color color = ClientUtil.getThreeClientColor();
    @Override
    public void init() {
        super.init();
        addSetting("fogRadius", new NumberSetting("Fog Range", this, CustomFog::getFogRadius, CustomFog::setFogRadius, "%.2f", 0.01F, 0.3F));
        addSetting("animated", new BooleanSetting("Animated", this, () -> !CustomFog.isCustomColor(), CustomFog::isAnimated, CustomFog::setAnimated));
        addSetting("customColorBoolean", new BooleanSetting("Custom Color", this, CustomFog::isCustomColor, CustomFog::setCustomColor));
        addSetting("customColor", new ColorSetting("Color", this, CustomFog::isCustomColor, CustomFog::getColor, CustomFog::setColor));
    }
    public static boolean enabled() {
        return Nova.getInstance().getModuleManager().get(CustomFog.class).isEnabled();
    }
    public static float getFogDistance() {
        return getFogRadius();
    }
    @java.lang.SuppressWarnings("all")
    public static float getFogRadius() {
        return CustomFog.fogRadius;
    }
    @java.lang.SuppressWarnings("all")
    public static void setFogRadius(final float fogRadius) {
        CustomFog.fogRadius = fogRadius;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isCustomColor() {
        return CustomFog.customColor;
    }
    @java.lang.SuppressWarnings("all")
    public static void setCustomColor(final boolean customColor) {
        CustomFog.customColor = customColor;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isAnimated() {
        return CustomFog.animated;
    }
    @java.lang.SuppressWarnings("all")
    public static void setAnimated(final boolean animated) {
        CustomFog.animated = animated;
    }
    @java.lang.SuppressWarnings("all")
    public static Color getColor() {
        return CustomFog.color;
    }
    @java.lang.SuppressWarnings("all")
    public static void setColor(final Color color) {
        CustomFog.color = color;
    }
}
