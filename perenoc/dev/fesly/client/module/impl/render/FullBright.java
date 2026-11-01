package dev.fesly.client.module.impl.render;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.UpdateEvent;

@ModuleInfo(
        name = "FullBright",
        category = Category.RENDER,
        description = "Устанавливает огромную гамму."
)
public class FullBright extends Module {

    @Override
    protected void onEnable() {
        super.onEnable();
        mc.gameSettings.gamma = 1000;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        mc.gameSettings.gamma = 1;
    }
}
