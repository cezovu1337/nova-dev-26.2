package dev.fesly.client.module.impl.movement;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.client.notification.Notification;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.font.Fonts;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.render.DrawHudPostEvent;
import dev.fesly.impl.event.impl.render.DrawHudPreEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.animation.extended.impl.EaseOutSine;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.status.client.CServerQueryPacket;
import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
@ModuleInfo(name = "Timer", description = "", category = Category.MOVEMENT)
public class Timer extends Module {
    private boolean grimBypass = true;
    private float speed = 1.1F;
    private boolean smart = true;
    private int bindButton = -1;
    @Override
    public void init() {
        super.init();
        addSetting("grimBypass", new BooleanSetting("Grim Bypass", this, this::isGrimBypass, this::setGrimBypass));
        addSetting("speed", new NumberSetting("Timer Speed", this, this::getSpeed, this::setSpeed, "%.2f", 0, 10));
        addSetting("smart", new BooleanSetting("Smart Hud", this, this::isGrimBypass, this::isSmart, this::setSmart));
    }
    private Queue<IPacket<?>> packets = new LinkedList<>();
    public EaseOutSine openAnim = new EaseOutSine(650, 1);
    @Override
    public void onDisable() {
        if (grimBypass) {
            mc.timer.timerSpeed = 1.0F;
            packets.clear();
        } else {
            mc.timer.timerSpeed = 1.0F;
        }
        openAnim.setDirection(false);
    }
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    public String getInfo() {
        return String.valueOf(packets.size());
    }
    @EventLink
    public Listener<DrawHudPreEvent> onHudPre = event -> {
        float center = mw.getScaledWidth() / 2.0F;
        float y = mw.getScaledHeight() / 6.0F;
        if (smart && isGrimBypass()) {
            openAnim.setDirection(true);
            IAccess.addRunnableWithChecks(() -> RenderUtil.roundedRectangle(center - 50, y - 5 - (250 - (openAnim.getOutput().floatValue() * 250)), 100, 10, 3, new Color(-1)));
        }
    };
    @EventLink
    public Listener<DrawHudPostEvent> onHudPost = event -> {
        float center = mw.getScaledWidth() / 2.0F;
        float y = mw.getScaledHeight() / 6.0F;
        if (smart && isGrimBypass()) {
            RenderUtil.roundedRectangle(center - 50, y - 5 - (250 - (openAnim.getOutput().floatValue() * 250)), packets.size() / 100.0F * 20, 10, 3, ClientUtil.getOneClientColor());
        }
    };
    @EventLink
    public Listener<MotionEvent> onMotionUpdate = event -> {
        if (nullCheck()) return;
        {
            if ((!grimBypass || !packets.isEmpty()) && isMovingExtended()) {
                mc.timer.timerSpeed = this.speed;
                if (grimBypass) mc.player.connection.sendPacket(Objects.requireNonNull(packets.poll()));
            } else mc.timer.timerSpeed = 1.0F;
        }
    };
    @EventLink
    public Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
    };
    @EventLink
    public Listener<PacketSendEvent> packetSendEventListener = event -> {
        if (nullCheck() || !grimBypass) return;
        if (event.getPacket() instanceof CConfirmTransactionPacket packet) {
            if (!isMovingExtended() && packets.size() < 500) {
                event.setCancelled();
                packets.add(packet);
            }
            if (!packets.isEmpty()) event.setCancelled();
        }
    };
    private boolean isMovingExtended() {
        return mc.player.movementInput.rightKeyDown || mc.player.movementInput.leftKeyDown || mc.player.movementInput.backKeyDown || mc.player.movementInput.forwardKeyDown || ((mc.player.getPosX() - mc.player.prevPosX) != 0) || ((mc.player.getPosY() - mc.player.prevPosY) != 0) || ((mc.player.getPosZ() - mc.player.prevPosZ) != 0);
    }
    public boolean nullCheck() {
        return mc.player == null || mc.world == null;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isGrimBypass() {
        return this.grimBypass;
    }
    @java.lang.SuppressWarnings("all")
    public float getSpeed() {
        return this.speed;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isSmart() {
        return this.smart;
    }
    @java.lang.SuppressWarnings("all")
    public int getBindButton() {
        return this.bindButton;
    }
    @java.lang.SuppressWarnings("all")
    public Queue<IPacket<?>> getPackets() {
        return this.packets;
    }
    @java.lang.SuppressWarnings("all")
    public EaseOutSine getOpenAnim() {
        return this.openAnim;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<DrawHudPreEvent> getOnHudPre() {
        return this.onHudPre;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<DrawHudPostEvent> getOnHudPost() {
        return this.onHudPost;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<MotionEvent> getOnMotionUpdate() {
        return this.onMotionUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketReceiveEvent> getPacketReceiveEventListener() {
        return this.packetReceiveEventListener;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketSendEvent> getPacketSendEventListener() {
        return this.packetSendEventListener;
    }
    @java.lang.SuppressWarnings("all")
    public void setGrimBypass(final boolean grimBypass) {
        this.grimBypass = grimBypass;
    }
    @java.lang.SuppressWarnings("all")
    public void setSpeed(final float speed) {
        this.speed = speed;
    }
    @java.lang.SuppressWarnings("all")
    public void setSmart(final boolean smart) {
        this.smart = smart;
    }
    @java.lang.SuppressWarnings("all")
    public void setBindButton(final int bindButton) {
        this.bindButton = bindButton;
    }
    @java.lang.SuppressWarnings("all")
    public void setPackets(final Queue<IPacket<?>> packets) {
        this.packets = packets;
    }
    @java.lang.SuppressWarnings("all")
    public void setOpenAnim(final EaseOutSine openAnim) {
        this.openAnim = openAnim;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnHudPre(final Listener<DrawHudPreEvent> onHudPre) {
        this.onHudPre = onHudPre;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnHudPost(final Listener<DrawHudPostEvent> onHudPost) {
        this.onHudPost = onHudPost;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnMotionUpdate(final Listener<MotionEvent> onMotionUpdate) {
        this.onMotionUpdate = onMotionUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public void setPacketReceiveEventListener(final Listener<PacketReceiveEvent> packetReceiveEventListener) {
        this.packetReceiveEventListener = packetReceiveEventListener;
    }
    @java.lang.SuppressWarnings("all")
    public void setPacketSendEventListener(final Listener<PacketSendEvent> packetSendEventListener) {
        this.packetSendEventListener = packetSendEventListener;
    }
}
