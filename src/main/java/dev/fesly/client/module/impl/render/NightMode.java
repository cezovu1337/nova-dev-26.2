package dev.fesly.client.module.impl.render;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import net.minecraft.network.play.server.SUpdateTimePacket;
@ModuleInfo(
        name = "NightMode",
        category = Category.RENDER,
        description = "Устанавливает ночное время суток!"
)
public class NightMode extends Module {
    @EventLink
    public Listener<PacketReceiveEvent> onPacket = e -> {
        {
            if ((Object) e.getPacket() instanceof SUpdateTimePacket || (Object) e.getPacket() instanceof net.minecraft.network.protocol.game.ClientboundSetTimePacket) {
                e.setCancelled(true);
            }
        }
    };
    @EventLink
    public Listener<UpdateEvent> onLastWorldRender = evt -> {
        if (mc.world != null)
            mc.world.setDayTime(20000);
    };
}
