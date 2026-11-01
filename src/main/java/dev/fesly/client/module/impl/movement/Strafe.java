package dev.fesly.client.module.impl.movement;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.util.player.MoveUtil;
@ModuleInfo(name = "Strafe", description = "", category = Category.MOVEMENT)
public class Strafe extends Module {
    @EventLink
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.onGround) {
            MoveUtil.setSpeed(MoveUtil.getSpeed());
        }
        if (MoveUtil.isMoving()) {
            if (mc.player.fallDistance > 1.0) {
                mc.timer.timerSpeed = 1.0F;
            } else {
                if (mc.player.ticksExisted % 2 == 0) MoveUtil.setSpeed(0.2F);
            }
        }
    };
    @java.lang.SuppressWarnings("all")
    public Listener<MotionEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
}
