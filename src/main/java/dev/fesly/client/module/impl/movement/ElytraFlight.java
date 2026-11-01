package dev.fesly.client.module.impl.movement;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.player.BlockUtils;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.secondValues.NumberSetting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import static org.lwjgl.glfw.GLFW.*;
@ModuleInfo(name = "ElytraFlight", description = "", category = Category.MOVEMENT)
public class ElytraFlight extends Module {
    private float speed = 1.0F;
    private float motionY = 0.3F;
    private float ss = 0.1F;
    @Override
    public void init() {
        super.init();
        addSetting("speed", new NumberSetting("Speed", this, this::getSpeed, this::setSpeed, "%.2f", 0.1F, 2.5F));
        addSetting("motiony", new NumberSetting("Motion Y", this, this::getMotionY, this::setMotionY, "%.1f", 0.1F, 1.0F));
    }
    @EventLink
    public final Listener<PacketReceiveEvent> onSendPacket = evt -> {
        if (mc.player == null) return;
        if (evt.getPacket() instanceof SEntityMetadataPacket && mc.player != null && ((SEntityMetadataPacket) evt.getPacket()).getEntityId() == mc.player.getEntityId()) {
            evt.setCancelled(true);
        }
    };
    @Override
    protected void onDisable() {
        super.onDisable();
    }
    private TimerUtil timer = new TimerUtil();
    @EventLink
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        if (mc.player == null) return;
        boolean sunrise = false;
        float speed = (this.speed - 0.2F);
        int ticks = sunrise ? 3 : 6;
        if (mc.player.onGround) mc.player.jump();
        if (mc.player.fallDistance < 0.1) return;
        if (InventoryHelper.getItemIndex(Items.ELYTRA) == -1) {
        }
        int emptySlot = InventoryHelper.getItemIndex(Item.getItemFromBlock(Blocks.AIR));
        if (emptySlot != -1 && mc.player.inventory.getItemStack().getItem() == Items.ELYTRA && InventoryHelper.getItemIndex(Items.ELYTRA) == -1) {
            mc.playerController.windowClick(0, (emptySlot < 9) ? (emptySlot + 36) : emptySlot, 1, ClickType.SWAP, mc.player);
        }
        if (mc.player.inventory.getItemStack().getItem() instanceof ArmorItem && mc.player.inventory.armorItemInSlot(2).isEmpty()) {
            ArmorItem armor = (ArmorItem) mc.player.inventory.getItemStack().getItem();
            if (armor.slot == EquipmentSlotType.CHEST) {
                mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
            }
        }
        if (mc.player.inventory.getItemStack().getItem() instanceof ArmorItem && !mc.player.inventory.armorItemInSlot(2).isEmpty()) {
            mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
            int emptySlot2 = InventoryHelper.getItemIndex(Item.getItemFromBlock(Blocks.AIR));
            if (emptySlot2 != -1 && mc.player.inventory.getItemStack().getItem() == Items.ELYTRA && InventoryHelper.getItemIndex(Items.ELYTRA) == -1) {
                mc.playerController.windowClick(0, (emptySlot2 < 9) ? (emptySlot2 + 36) : emptySlot2, 1, ClickType.SWAP, mc.player);
            }
        }
        if (!(mc.player.onGround) && (mc.player.ticksExisted % ticks == 0)) {
            int elytra = InventoryHelper.getItemFromIndex(Items.ELYTRA, false);
            if (mc.player.inventory.getItemStack().getItem() != Items.ELYTRA) {
                mc.playerController.windowClick(0, (elytra < 9) ? (elytra + 36) : elytra, 1, ClickType.SWAP, mc.player);
            }
            mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            mc.player.fallDistance = 1;
            mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
            mc.playerController.windowClick(0, (elytra < 9) ? (elytra + 36) : elytra, 1, ClickType.SWAP, mc.player);
        }
        double bps = ClientUtil.calculateBPS();
        mc.ingameGUI.setOverlayMessage(ITextComponent.getTextComponentOrEmpty("" + String.format("%.2f", ClientUtil.calculateBPS()) + "bps"), false);
        double prevX = mc.player.getPosX() - mc.player.prevPosX;
        double prevZ = mc.player.getPosZ() - mc.player.prevPosZ;
        double lastDist = Math.sqrt(prevX * prevX + prevZ * prevZ);
        double currSpeed = lastDist * 15.3571428571;
        mc.gameSettings.keyBindSneak.setPressed(false);
        if (!MoveUtil.hasBlocks(-2)) ss = MathUtil.lerp(ss, MoveUtil.isMoving() ? speed : 0.1F, 0.05F);
         else ss = MathUtil.lerp(ss, MoveUtil.isMoving() ? 1 : 0.1F, 0.05F);
        if (!MoveUtil.isMoving()) ss = 0.1F;
        if (!(mc.player.onGround) && mc.player.ticksExisted % (sunrise ? ticks : 10) == 0 && !MoveUtil.hasBlocks(-0.1)) {
            mc.gameSettings.keyBindSneak.setPressed(false);
            if (bps < 35) {
                mc.gameSettings.keyBindSneak.setPressed(false);
                double speedMulc = sunrise ? 19.81 : 15;
                long window = mc.getMainWindow().getHandle();
                boolean isPressed = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
                boolean upDown = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
                boolean spaceDown = InputMappings.isKeyDown(window, GLFW_KEY_SPACE);
                boolean shiftDown = InputMappings.isKeyDown(window, GLFW_KEY_LEFT_SHIFT);
                if (!upDown) mc.player.addVelocity(isPressed ? -Math.sin(MoveUtil.getDirection()) * 9.11 / speedMulc : 0, 0.15, isPressed ? Math.cos(MoveUtil.getDirection()) * 9.11 / speedMulc : 0);
                 else {
                    mc.player.addVelocity(isPressed ? -Math.sin(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0, 0.15, isPressed ? Math.cos(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0);
                    mc.player.addVelocity(isPressed ? -Math.sin(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0, 0.15, isPressed ? Math.cos(MoveUtil.getDirection()) * 9.11 / (speedMulc * 2) : 0);
                }
                if (InputMappings.isKeyDown(window, GLFW_KEY_SPACE)) {
                    mc.player.addVelocity(-Math.sin(MoveUtil.getDirection()) * 0.005 * MoveUtil.getSpeed(), motionY, Math.cos(MoveUtil.getDirection()) * 0.005 * MoveUtil.getSpeed());
                } else if (InputMappings.isKeyDown(window, GLFW_KEY_LEFT_SHIFT)) {
                    mc.player.addVelocity(-Math.sin(MoveUtil.getDirection()) * 0.005 * MoveUtil.getSpeed(), -motionY, Math.cos(MoveUtil.getDirection()) * 0.005 * MoveUtil.getSpeed());
                } else {
                }
            }
        } else
        if (!(mc.player.fallDistance >= 0.1) && mc.player.onGround && MoveUtil.hasBlocks(-0.1) && !(MoveUtil.isBlockAboveHead())) {
        }
    };
    @java.lang.SuppressWarnings("all")
    public float getSpeed() {
        return this.speed;
    }
    @java.lang.SuppressWarnings("all")
    public float getMotionY() {
        return this.motionY;
    }
    @java.lang.SuppressWarnings("all")
    public float getSs() {
        return this.ss;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketReceiveEvent> getOnSendPacket() {
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
    public void setSpeed(final float speed) {
        this.speed = speed;
    }
    @java.lang.SuppressWarnings("all")
    public void setMotionY(final float motionY) {
        this.motionY = motionY;
    }
    @java.lang.SuppressWarnings("all")
    public void setSs(final float ss) {
        this.ss = ss;
    }
    @java.lang.SuppressWarnings("all")
    public void setTimer(final TimerUtil timer) {
        this.timer = timer;
    }
}
