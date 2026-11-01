package dev.fesly.client.module.impl.misc;

import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.render.CustomFog;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.EnumSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Getter;
import lombok.Setter;

@ModuleInfo(
        name = "Swing Animations",
        category = Category.MISC,
        description = "Анимация на русках"
)
@Getter
@Setter
public class SwingAnimations  extends Module {

    @Getter @Setter
    public static   float right_x = 0f;
    @Getter @Setter
    public static  float right_y = 0f;
    @Getter @Setter
    public static  float right_z = 0f;

    @Getter @Setter
    public static  float left_x = 0f;
    @Getter @Setter
    public static  float left_y = 0f;
    @Getter @Setter
    public static float left_z = 0f;

    public static boolean onlyKillAura;

    @Getter @Setter
    public static SwingMods swingMod = SwingMods.Default;
    @Getter @Setter
    public static boolean animations = false;

    @Override
    public void init() {
        super.init();
        addSetting("animations", new BooleanSetting("Animations", this, SwingAnimations::isAnimations, SwingAnimations::setAnimations));
        addSetting("swingMode", new EnumSetting<>("Animation Mode", this,SwingAnimations::isAnimations, SwingAnimations::getSwingMod,
                SwingAnimations::setSwingMod, SwingMods.class));
        addSetting("right_x", new NumberSetting("Right X", this, SwingAnimations::getRight_x,SwingAnimations::setRight_x,"%.1f",-2, 2));
        addSetting("right_y", new NumberSetting("Right Y", this, SwingAnimations::getRight_y,SwingAnimations::setRight_y,"%.1f",-2, 2));
        addSetting("right_z", new NumberSetting("Right Z", this, SwingAnimations::getRight_z,SwingAnimations::setRight_z,"%.1f",-2, 2));
        addSetting("left_x", new NumberSetting("Left X", this, SwingAnimations::getLeft_x,SwingAnimations::setLeft_x,"%.1f",-2, 2));
        addSetting("left_y", new NumberSetting("Left Y", this, SwingAnimations::getLeft_y,SwingAnimations::setLeft_y,"%.1f",-2, 2));
        addSetting("left_z", new NumberSetting("Left Z", this, SwingAnimations::getLeft_z,SwingAnimations::setLeft_z,"%.1f",-2, 2));
    }

    public static boolean enabled() {
        return Nova.getInstance().getModuleManager().get(SwingAnimations.class).isEnabled();
    }

    public enum SwingMods {
        Default,
        Rotate
    }
}
