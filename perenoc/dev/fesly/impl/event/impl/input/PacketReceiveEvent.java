package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.CancellableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.IPacket;

@Getter
@Setter
public class PacketReceiveEvent extends CancellableEvent {
    public IPacket<?> packet;
    public PacketReceiveEvent(IPacket<?> packet) {
        this.packet = packet;
    }
}
