package dev.fesly.client.module.impl.movement;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import net.minecraft.network.play.client.CEntityActionPacket;
@ModuleInfo(name = "AutoElytra", description = "", category = Category.MOVEMENT)
public class HighJump extends Module {
    public boolean packeted = false;
    public boolean jumped = false;
    @EventLink
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        if (mc.player == null) return;
        if (!mc.player.isSprinting() && mc.player.movementInput.isMovingForward()) {
            mc.player.setSprinting(true);
        }
        if (!packeted) {
            if (mc.player.fallDistance < 0) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                packeted = true;
                jumped = false;
            }
        }
        if (!jumped && packeted) {
            if (mc.player.onGround) {
                mc.player.jump();
                jumped = true;
                packeted = false;
            }
        }
    };
    @java.lang.SuppressWarnings("all")
    public boolean isPacketed() {
        return this.packeted;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isJumped() {
        return this.jumped;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<MotionEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public void setPacketed(final boolean packeted) {
        this.packeted = packeted;
    }
    @java.lang.SuppressWarnings("all")
    public void setJumped(final boolean jumped) {
        this.jumped = jumped;
    }
}
