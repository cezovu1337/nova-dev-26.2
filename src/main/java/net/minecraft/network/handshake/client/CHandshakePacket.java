package net.minecraft.network.handshake.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CHandshakePacket implements IPacket {
    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener listener) {
    }
}
