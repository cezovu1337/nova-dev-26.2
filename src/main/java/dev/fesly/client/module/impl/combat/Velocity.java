package dev.fesly.client.module.impl.combat;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.MultiEnumSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.vector.Vector3d;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
@ModuleInfo(name = "Velocity", description = "", category = Category.COMBAT)
public class Velocity extends Module {
    private float velocityChance = 80;
    public enum VelocityValues {
        OtherDamages, Explosion;
    }
    public EnumSet<VelocityValues> velocityValues = EnumSet.of(VelocityValues.Explosion, VelocityValues.OtherDamages);
    @Override
    public void init() {
        super.init();
        addSetting("velocityChance", new NumberSetting("Velocity Chance", this, this::getVelocityChance, this::setVelocityChance, "%.0f", 0, 100.0F));
        addSetting("velocityValues", new MultiEnumSetting<>("Cancel Velocities", this, this::getVelocityValues, this::setVelocityValues, VelocityValues.class));
    }
    private boolean ground;
    private int packetCanceled = 6;
    private int resetPersec = 8;
    private int canceled = 0;
    private int updates = 0;
    private List<SEntityVelocityPacket> canceledPackets = new LinkedList<>();
    private TimerUtil timerUtil = new TimerUtil();
    @Override
    protected void onEnable() {
        super.onEnable();
        canceled = 0;
    }
    public Vector3d lastRelative;
    private int velocityChanceValues = 1;
    private TimerUtil flagTimer;
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = evt -> {
        if (flagTimer == null) flagTimer = new TimerUtil();
        IPacket<?> packet = evt.getPacket();
        if (mc.player == null || mc.world == null) return;
        if (packet instanceof SPlayerPositionLookPacket) {
            flagTimer.reset();
        }
        if (flagTimer.hasReached(200)) {
            if (velocityValues.contains(VelocityValues.OtherDamages)) {
                if (packet instanceof SEntityVelocityPacket && ((SEntityVelocityPacket) packet).getEntityID() == mc.player.getEntityId()) {
                    {
                        if (((int) ((100 - velocityChance) / 10)) != 0) {
                            if (velocityChanceValues % ((int) ((100 - velocityChance) / 10)) == 0) {
                                evt.setCancelled();
                                canceled = packetCanceled;
                                lastRelative = new Vector3d(((SEntityVelocityPacket) packet).motionX, ((SEntityVelocityPacket) packet).motionY, ((SEntityVelocityPacket) packet).motionZ);
                            }
                        } else {
                            evt.setCancelled();
                            canceled = packetCanceled;
                            lastRelative = new Vector3d(((SEntityVelocityPacket) packet).motionX, ((SEntityVelocityPacket) packet).motionY, ((SEntityVelocityPacket) packet).motionZ);
                        }
                    }
                    velocityChanceValues++;
                }
            }
            if (velocityValues.contains(VelocityValues.Explosion)) {
                if (packet instanceof SExplosionPacket) {
                    {
                        if (((int) ((100 - velocityChance) / 10)) != 0) {
                            if (velocityChanceValues % ((int) ((100 - velocityChance) / 10)) == 0) {
                                evt.setCancelled();
                                lastRelative = new Vector3d(((SExplosionPacket) packet).motionX, ((SExplosionPacket) packet).motionY, ((SExplosionPacket) packet).motionZ);
                            }
                        } else {
                            evt.setCancelled();
                            lastRelative = new Vector3d(((SExplosionPacket) packet).motionX, ((SExplosionPacket) packet).motionY, ((SExplosionPacket) packet).motionZ);
                        }
                    }
                    velocityChanceValues++;
                }
            }
            if (packet instanceof SConfirmTransactionPacket && canceled > 0) {
                assert mc.getCurrentServerData() != null;
                if (mc.getCurrentServerData().serverIP != null) {
                    evt.setCancelled();
                    canceled--;
                }
            }
        }
    };
    @EventLink
    public final Listener<UpdateEvent> onPlayerUpdate = event -> {
        updates++;
        if (resetPersec > 0) {
            if (updates > 5) {
                updates = 0;
                if (canceled > 0) {
                    canceled--;
                }
            }
        }
    };
    @java.lang.SuppressWarnings("all")
    public float getVelocityChance() {
        return this.velocityChance;
    }
    @java.lang.SuppressWarnings("all")
    public EnumSet<VelocityValues> getVelocityValues() {
        return this.velocityValues;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isGround() {
        return this.ground;
    }
    @java.lang.SuppressWarnings("all")
    public int getPacketCanceled() {
        return this.packetCanceled;
    }
    @java.lang.SuppressWarnings("all")
    public int getResetPersec() {
        return this.resetPersec;
    }
    @java.lang.SuppressWarnings("all")
    public int getCanceled() {
        return this.canceled;
    }
    @java.lang.SuppressWarnings("all")
    public int getUpdates() {
        return this.updates;
    }
    @java.lang.SuppressWarnings("all")
    public List<SEntityVelocityPacket> getCanceledPackets() {
        return this.canceledPackets;
    }
    @java.lang.SuppressWarnings("all")
    public TimerUtil getTimerUtil() {
        return this.timerUtil;
    }
    @java.lang.SuppressWarnings("all")
    public Vector3d getLastRelative() {
        return this.lastRelative;
    }
    @java.lang.SuppressWarnings("all")
    public int getVelocityChanceValues() {
        return this.velocityChanceValues;
    }
    @java.lang.SuppressWarnings("all")
    public TimerUtil getFlagTimer() {
        return this.flagTimer;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketReceiveEvent> getOnPacketReceive() {
        return this.onPacketReceive;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<UpdateEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public void setVelocityChance(final float velocityChance) {
        this.velocityChance = velocityChance;
    }
    @java.lang.SuppressWarnings("all")
    public void setVelocityValues(final EnumSet<VelocityValues> velocityValues) {
        this.velocityValues = velocityValues;
    }
    @java.lang.SuppressWarnings("all")
    public void setGround(final boolean ground) {
        this.ground = ground;
    }
    @java.lang.SuppressWarnings("all")
    public void setPacketCanceled(final int packetCanceled) {
        this.packetCanceled = packetCanceled;
    }
    @java.lang.SuppressWarnings("all")
    public void setResetPersec(final int resetPersec) {
        this.resetPersec = resetPersec;
    }
    @java.lang.SuppressWarnings("all")
    public void setCanceled(final int canceled) {
        this.canceled = canceled;
    }
    @java.lang.SuppressWarnings("all")
    public void setUpdates(final int updates) {
        this.updates = updates;
    }
    @java.lang.SuppressWarnings("all")
    public void setCanceledPackets(final List<SEntityVelocityPacket> canceledPackets) {
        this.canceledPackets = canceledPackets;
    }
    @java.lang.SuppressWarnings("all")
    public void setTimerUtil(final TimerUtil timerUtil) {
        this.timerUtil = timerUtil;
    }
    @java.lang.SuppressWarnings("all")
    public void setLastRelative(final Vector3d lastRelative) {
        this.lastRelative = lastRelative;
    }
    @java.lang.SuppressWarnings("all")
    public void setVelocityChanceValues(final int velocityChanceValues) {
        this.velocityChanceValues = velocityChanceValues;
    }
    @java.lang.SuppressWarnings("all")
    public void setFlagTimer(final TimerUtil flagTimer) {
        this.flagTimer = flagTimer;
    }
}
