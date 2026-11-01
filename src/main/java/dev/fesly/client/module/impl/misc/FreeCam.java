package dev.fesly.client.module.impl.misc;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.font.Fonts;
import dev.fesly.impl.event.CancellableEvent;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.event.impl.other.WorldChangeEvent;
import dev.fesly.impl.event.impl.player.ClipEvent;
import dev.fesly.impl.event.impl.player.EventFullCube;
import dev.fesly.impl.event.impl.render.DrawHudPostEvent;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.value.secondValues.NumberSetting;
import net.minecraft.client.entity.player.RemoteLocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.world.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Hand;
import net.minecraft.world.GameType;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
@ModuleInfo(name = "FreeCam", category = Category.MISC, description = "Свободный полет от вашей ебучей души.")
public class FreeCam extends Module {
    private float speed = 1.0F;
    double XF;
    double YF;
    double z;
    private static String world = "";
    @Override
    public void init() {
        super.init();
        addSetting("speed", new NumberSetting("Speed", this, this::getSpeed, this::setSpeed, "%.1f", 0.1F, 1.0F));
    }
    public static boolean enabled() {
        return nova.getModuleManager().get(FreeCam.class).isEnabled();
    }
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (mc.player == null) {
            toggle();
            return;
        }
        if (!mc.player.isAlive()) {
            toggle();
            return;
        }
        if (event.getPacket() instanceof SPlayerPositionLookPacket) {
            if (mc.player != null || mc.world != null) {
                event.setCancelled(true);
            }
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onSendReceive = event -> {
        if (mc.player == null) {
            toggle();
            return;
        }
        if (!mc.player.isAlive()) {
            toggle();
        }
        if (event.getPacket() instanceof CPlayerPacket) {
            event.setCancelled();
        }
    };
    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        if (isEnabled()) {
            toggle();
        }
    };
    @Override
    public void toggle() {
        super.toggle();
        if (mc.player == null || mc.world == null) {
            this.enabled = false;
            return;
        }
        if (isEnabled()) {
            if (!mc.player.isAlive()) {
                toggle();
            }
            world = String.valueOf(mc.world);
            XF = mc.player.getPosX();
            YF = mc.player.getPosY();
            z = mc.player.getPosZ();
            RemoteLocalPlayer ent = new RemoteLocalPlayer(mc.world, mc.player.getGameProfile());
            ent.inventory = mc.player.inventory;
            ent.setHealth(mc.player.getHealth());
            ent.setPositionAndRotation(this.XF, mc.player.getBoundingBox().minY, this.z, mc.player.rotationYaw, mc.player.rotationPitch);
            ent.rotationYawHead = mc.player.rotationYawHead;
            mc.world.addEntity(-1, ent);
            mc.player.setGameType(GameType.SPECTATOR);
            mc.player.setSneaking(true);
        } else {
            mc.player.setPosition(XF, YF, z);
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 0.01, mc.player.getPosZ(), mc.player.onGround));
            mc.player.abilities.isFlying = false;
            mc.player.noClip = false;
            mc.world.removeEntityFromWorld(-1);
            mc.player.setGameType(GameType.SURVIVAL);
            mc.player.setSneaking(false);
        }
    }
    @EventLink
    public final Listener<DrawHudPostEvent> onHud = event -> {
        String yCoord = "" + Math.round(mc.player.getPosY() - YF);
        String str = "Y: " + yCoord;
        Fonts.ROBOTO_MEDIUM.get(16).draw(str, (mw.getScaledWidth() / 2.0F), mw.getScaledHeight() / 4.0F + 20, -1);
    };
    @EventLink
    public final Listener<UpdateEvent> onClientUpdate = event -> {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
        if (!world.equals(String.valueOf(mc.world))) {
            toggle();
            return;
        }
        mc.player.motion.y = 0;
        double speedMulc = 32;
        long window = mc.getMainWindow().getHandle();
        boolean isPressed = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
        mc.player.addVelocity(isPressed ? -Math.sin(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0, 0, isPressed ? Math.cos(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0);
        mc.player.addVelocity(isPressed ? -Math.sin(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0, 0, isPressed ? Math.cos(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0);
        if (mc.player.movementInput.jump) {
            mc.player.motion.y = speed;
        }
        if (mc.player.movementInput.sneaking) {
            mc.player.motion.y = -speed;
        }
    };
    @EventLink
    public final Listener<EventFullCube> fullCubeListener = CancellableEvent::setCancelled;
    @EventLink
    public final Listener<ClipEvent> clipEventListener = CancellableEvent::setCancelled;
    @java.lang.SuppressWarnings("all")
    public float getSpeed() {
        return this.speed;
    }
    @java.lang.SuppressWarnings("all")
    public double getXF() {
        return this.XF;
    }
    @java.lang.SuppressWarnings("all")
    public double getYF() {
        return this.YF;
    }
    @java.lang.SuppressWarnings("all")
    public double getZ() {
        return this.z;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketReceiveEvent> getOnPacketReceive() {
        return this.onPacketReceive;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketSendEvent> getOnSendReceive() {
        return this.onSendReceive;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<WorldChangeEvent> getOnWorldChange() {
        return this.onWorldChange;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<DrawHudPostEvent> getOnHud() {
        return this.onHud;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<UpdateEvent> getOnClientUpdate() {
        return this.onClientUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<EventFullCube> getFullCubeListener() {
        return this.fullCubeListener;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<ClipEvent> getClipEventListener() {
        return this.clipEventListener;
    }
    @java.lang.SuppressWarnings("all")
    public void setSpeed(final float speed) {
        this.speed = speed;
    }
    @java.lang.SuppressWarnings("all")
    public void setXF(final double XF) {
        this.XF = XF;
    }
    @java.lang.SuppressWarnings("all")
    public void setYF(final double YF) {
        this.YF = YF;
    }
    @java.lang.SuppressWarnings("all")
    public void setZ(final double z) {
        this.z = z;
    }
}
