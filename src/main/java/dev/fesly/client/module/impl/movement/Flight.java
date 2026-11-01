package dev.fesly.client.module.impl.movement;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.util.time.TimerUtil;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
@ModuleInfo(name = "Flight", description = "", category = Category.MOVEMENT)
public class Flight extends Module {
    private boolean shouldFly = false;
    private int jumpCount = 0;
    @EventLink
    public final Listener<PacketSendEvent> onSendPacket = event -> {
        if (mc.player == null) return;
        if (event.getPacket() instanceof CConfirmTransactionPacket) {
            if (shouldFly && jumpCount <= 5) {
                System.out.println(mc.player.motion.y);
                if (mc.player.motion.y == 0.16476250320672992) {
                    jumpCount++;
                }
            }
        }
    };
    @Override
    protected void onDisable() {
        super.onDisable();
        shouldFly = false;
        jumpCount = 0;
    }
    private TimerUtil timer = new TimerUtil();
    @EventLink
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        if (mc.player == null) return;
        if (true) {
            if (mc.player.motion.y == 0.24813599859094576 || mc.player.isOnLadder()) shouldFly = true;
            if (shouldFly) {
                mc.player.connection.sendPacketNoEvent(new CPlayerPacket.PositionPacket(Double.MAX_EXPONENT, Double.MIN_EXPONENT, Double.MAX_EXPONENT, false));
            }
        } else {
            mc.player.motion.y += 0.05;
        }
    };
    @java.lang.SuppressWarnings("all")
    public boolean isShouldFly() {
        return this.shouldFly;
    }
    @java.lang.SuppressWarnings("all")
    public int getJumpCount() {
        return this.jumpCount;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketSendEvent> getOnSendPacket() {
        return this.onSendPacket;
    }
    @java.lang.SuppressWarnings("all")
    public TimerUtil getTimer() {
        return this.timer;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<MotionEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public void setShouldFly(final boolean shouldFly) {
        this.shouldFly = shouldFly;
    }
    @java.lang.SuppressWarnings("all")
    public void setJumpCount(final int jumpCount) {
        this.jumpCount = jumpCount;
    }
    @java.lang.SuppressWarnings("all")
    public void setTimer(final TimerUtil timer) {
        this.timer = timer;
    }
}
