package dev.fesly.client.module.impl.movement;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.util.rotation.RotationUtil;
import dev.fesly.impl.util.time.TimerUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.world.item.Items;
import net.minecraft.network.play.client.*;
import net.minecraft.util.Hand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
@ModuleInfo(name = "Spider", description = "", category = Category.MOVEMENT)
public class Spider extends Module {
    @EventLink
    public final Listener<PacketSendEvent> onSendPacket = event -> {
        if (mc.player == null) return;
    };
    @Override
    protected void onDisable() {
        super.onDisable();
        mc.gameSettings.keyBindUseItem.setPressed(false);
        use = false;
    }
    private TimerUtil timer = new TimerUtil();
    private boolean use = false;
    @EventLink
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        if (mc.player == null) return;
        if (Minecraft.getInstance().world.getBlockState(new BlockPos(Minecraft.getInstance().player.getPosX() + 0.4, mc.player.getPosY(), mc.player.getPosZ() + 0.4)).getBlock() != Blocks.AIR) {
            int slot = mc.player.inventory.currentItem;
            int block = InventoryHelper.getBlockItem();
            if (slot == -1 || block == -1) return;
            if (InventoryHelper.doesHotbarHaveBlock()) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(block));
                if (mc.player.getHeldItemMainhand() != null) {
                    use = true;
                    mc.player.rotationPitch = 81;
                    mc.player.rotationYaw = RotationUtil.getVectorDefault(mc.player, new Vector3d(Minecraft.getInstance().player.getPosX() + 0.4, mc.player.getPosY(), mc.player.getPosZ() + 0.4), 1, false)[0];
                    mc.player.inventory.currentItem = block;
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                    if (mc.player.ticksExisted % 6 == 0) {
                        mc.player.jump();
                    }
                } else
                if (use) {
                    mc.gameSettings.keyBindUseItem.setPressed(false);
                    use = false;
                }
            }
        }
        if (Minecraft.getInstance().world.getBlockState(new BlockPos(Minecraft.getInstance().player.getPosX() - 0.4, mc.player.getPosY(), mc.player.getPosZ() - 0.4)).getBlock() != Blocks.AIR) {
            int slot = mc.player.inventory.currentItem;
            int block = InventoryHelper.getBlockItem();
            if (slot == -1 || block == -1) return;
            if (InventoryHelper.doesHotbarHaveBlock()) {
                if (mc.player.getHeldItemMainhand() != null) {
                    use = true;
                    mc.player.rotationPitch = 81;
                    mc.player.rotationYaw = RotationUtil.getVectorDefault(mc.player, new Vector3d(Minecraft.getInstance().player.getPosX() - 0.4, mc.player.getPosY(), mc.player.getPosZ() - 0.4), 1, false)[0];
                    mc.player.inventory.currentItem = block;
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                    if (mc.player.ticksExisted % 6 == 0) {
                        mc.player.jump();
                    }
                } else
                if (use) {
                    mc.gameSettings.keyBindUseItem.setPressed(false);
                    use = false;
                }
            }
        }
    };
    @java.lang.SuppressWarnings("all")
    public Listener<PacketSendEvent> getOnSendPacket() {
        return this.onSendPacket;
    }
    @java.lang.SuppressWarnings("all")
    public TimerUtil getTimer() {
        return this.timer;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isUse() {
        return this.use;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<MotionEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public void setTimer(final TimerUtil timer) {
        this.timer = timer;
    }
    @java.lang.SuppressWarnings("all")
    public void setUse(final boolean use) {
        this.use = use;
    }
}
