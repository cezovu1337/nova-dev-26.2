package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CHeldItemChangePacket implements IPacket {
    private final int slotId;

    public CHeldItemChangePacket(int slotId) {
        this.slotId = slotId;
    }

    public int getSlotId() {
        return this.slotId;
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
