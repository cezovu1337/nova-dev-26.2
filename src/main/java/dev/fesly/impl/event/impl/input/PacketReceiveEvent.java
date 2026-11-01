package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.CancellableEvent;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.IPacket;
public class PacketReceiveEvent extends CancellableEvent {
    public IPacket<?> packet;
    public PacketReceiveEvent(IPacket<?> packet) {
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
}
