package net.minecraft.network.play.server;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class SHeldItemChangePacket implements IPacket<ClientGamePacketListener> {
    private int slot;

    public SHeldItemChangePacket() {
    }

    public SHeldItemChangePacket(int slot) {
        this.slot = slot;
    }

    public int getHeldItemHotbarIndex() {
        return slot;
    }

    @Override
    public PacketType<? extends net.minecraft.network.protocol.Packet<ClientGamePacketListener>> type() {
        return null;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
    }
}
