package dev.fesly.client.module.impl.render;

import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.ColorSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@ModuleInfo(
        name = "Custom Fog",
        category = Category.RENDER,
        description = "Изменяет туман под ваш вкус и цвет :) "
)
public class CustomFog extends Module {

    @Getter @Setter
    public static float fogRadius = 0.15f;
    @Getter @Setter
    public static boolean customColor = true;
    @Getter @Setter
    public static boolean animated = true;
    @Getter @Setter
    public static Color color = ClientUtil.getThreeClientColor();

    @Override
    public void init() {
        super.init();
        addSetting("fogRadius", new NumberSetting("Fog Range",this, CustomFog::getFogRadius,CustomFog::setFogRadius,"%.2f",0.01f,0.3f));
        addSetting("animated",new BooleanSetting("Animated", this,() -> !CustomFog.isCustomColor(),CustomFog::isAnimated, CustomFog::setAnimated));
        addSetting("customColorBoolean",new BooleanSetting("Custom Color", this,CustomFog::isCustomColor, CustomFog::setCustomColor));
        addSetting("customColor",new ColorSetting("Color",this,CustomFog::isCustomColor,CustomFog::getColor,CustomFog::setColor));
    }

    public static boolean enabled() {
        return Nova.getInstance().getModuleManager().get(CustomFog.class).isEnabled();
    }
    public static float getFogDistance() {
        return getFogRadius();
    }
}
