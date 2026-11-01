package dev.fesly.client.module.impl.misc;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Getter;
import lombok.Setter;

@ModuleInfo(
        name = "BetterTab",
        category = Category.MISC,
        description = "Какая то хуета под ногами"
)
@Getter
@Setter
public class BetterTab extends Module {

    @Getter
    @Setter
    public static boolean renderHeads = true;
    @Getter
    @Setter
    public static boolean customLimit = true;
    @Getter
    @Setter
    public static float limit = 240;

    @Override
    public void init() {
        super.init();
        addSetting("renderHeads",new BooleanSetting("Render Heads",this,BetterTab::isRenderHeads,BetterTab::setRenderHeads));
        addSetting("customLimit",new BooleanSetting("Custom Limit",this,BetterTab::isCustomLimit,BetterTab::setCustomLimit));
        addSetting("limit",new NumberSetting("Limit",this,BetterTab::isCustomLimit,BetterTab::getLimit,BetterTab::setLimit,"%.0f",80,1000));

    }

    public static boolean enabled() {
        return nova.getModuleManager().get(BetterTab.class).isEnabled();
    }
}
