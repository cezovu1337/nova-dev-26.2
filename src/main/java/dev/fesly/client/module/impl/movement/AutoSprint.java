package dev.fesly.client.module.impl.movement;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.util.rotation.RotationUtil;
import dev.fesly.impl.util.time.TimerUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
@ModuleInfo(name = "AutoSprint", description = "", category = Category.MOVEMENT)
public class AutoSprint extends Module {
    @EventLink
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        if (mc.player == null || mc.world == null) return;
        {
            if (mc.player.ticksExisted % 2 == 0) mc.gameSettings.keyBindSprint.setPressed(!mc.player.isSprinting() && mc.player.movementInput.isMovingForward() && mc.gameSettings.keyBindForward.isKeyDown() && !mc.player.collidedHorizontally && !mc.player.isInWater());
        }
    };
    @java.lang.SuppressWarnings("all")
    public Listener<MotionEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
}
