package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.CancellableEvent;
import net.minecraft.network.IPacket;
public class PacketSendEvent extends CancellableEvent {
    private IPacket<?> packet;
    public PacketSendEvent(IPacket<?> packet) {
        this.packet = packet;
    }
    @java.lang.SuppressWarnings("all")
    public IPacket<?> getPacket() {
        return this.packet;
    }
    @java.lang.SuppressWarnings("all")
    public void setPacket(final IPacket<?> packet) {
        this.packet = packet;
    }
    @java.lang.SuppressWarnings("all")
    public PacketSendEvent() {
    }
}
