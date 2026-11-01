package dev.fesly.client.module.impl.misc;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
@ModuleInfo(
        name = "NoJumpDelay",
        category = Category.MISC,
        description = "Какая то хуета под ногами"
)
public class NoJumpDelay extends Module {
    @Override
    public void init() {
        super.init();
    }
    @EventLink()
    public final Listener<UpdateEvent> onPlayerUpdate = event -> {
        mc.player.jumpTicks = 0;
    };
    public static NoJumpDelay getInstance() {
        return nova.getModuleManager().get(NoJumpDelay.class);
    }
}
