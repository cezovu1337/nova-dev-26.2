package dev.fesly.client.module.impl.misc;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.util.file.FileUtils;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.util.text.ITextComponent;
@ModuleInfo(name = "Test", description = "", category = Category.MOVEMENT)
public class Test extends Module {
    @EventLink
    public final Listener<PacketSendEvent> onPlayerUpdate = event -> {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof CConfirmTransactionPacket) {
            mc.ingameGUI.setOverlayMessage(ITextComponent.getTextComponentOrEmpty("PACKET SEND ; UID" + ((CConfirmTransactionPacket) event.getPacket()).getUid()), true);
        }
    };
    @java.lang.SuppressWarnings("all")
    public Listener<PacketSendEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
}
