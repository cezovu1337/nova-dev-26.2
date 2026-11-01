package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.CancellableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.IPacket;

@Getter
@Setter
@RequiredArgsConstructor
public class PacketSendEvent extends CancellableEvent {
    private IPacket<?> packet;
    public PacketSendEvent(IPacket<?> packet) {
        this.packet = packet;
    }
}
