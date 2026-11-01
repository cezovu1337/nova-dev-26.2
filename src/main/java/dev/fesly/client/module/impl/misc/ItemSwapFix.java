package dev.fesly.client.module.impl.misc;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
@ModuleInfo(name = "ItemSwapFix", description = "", category = Category.MISC)
public class ItemSwapFix extends Module {
    @EventLink
    public final Listener<PacketReceiveEvent> onPlayerUpdate = event -> {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof SHeldItemChangePacket) {
            final SHeldItemChangePacket packetHeldItemChange = (SHeldItemChangePacket) event.getPacket();
            final int heldItemHotbarIndex = packetHeldItemChange.getHeldItemHotbarIndex();
            if (heldItemHotbarIndex != mc.player.inventory.currentItem) {
                final ClientPlayNetHandler connection = mc.player.connection;
                connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                event.setCancelled();
            }
        }
    };
    @java.lang.SuppressWarnings("all")
    public Listener<PacketReceiveEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
}
