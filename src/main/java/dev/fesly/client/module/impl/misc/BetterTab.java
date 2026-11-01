package dev.fesly.client.module.impl.misc;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
@ModuleInfo(name = "BetterTab", category = Category.MISC, description = "Какая то хуета под ногами")
public class BetterTab extends Module {
    public static boolean renderHeads = true;
    public static boolean customLimit = true;
    public static float limit = 240;
    @Override
    public void init() {
        super.init();
        addSetting("renderHeads", new BooleanSetting("Render Heads", this, BetterTab::isRenderHeads, BetterTab::setRenderHeads));
        addSetting("customLimit", new BooleanSetting("Custom Limit", this, BetterTab::isCustomLimit, BetterTab::setCustomLimit));
        addSetting("limit", new NumberSetting("Limit", this, BetterTab::isCustomLimit, BetterTab::getLimit, BetterTab::setLimit, "%.0f", 80, 1000));
    }
    public static boolean enabled() {
        return nova.getModuleManager().get(BetterTab.class).isEnabled();
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isRenderHeads() {
        return BetterTab.renderHeads;
    }
    @java.lang.SuppressWarnings("all")
    public static void setRenderHeads(final boolean renderHeads) {
        BetterTab.renderHeads = renderHeads;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isCustomLimit() {
        return BetterTab.customLimit;
    }
    @java.lang.SuppressWarnings("all")
    public static void setCustomLimit(final boolean customLimit) {
        BetterTab.customLimit = customLimit;
    }
    @java.lang.SuppressWarnings("all")
    public static float getLimit() {
        return BetterTab.limit;
    }
    @java.lang.SuppressWarnings("all")
    public static void setLimit(final float limit) {
        BetterTab.limit = limit;
    }
}
