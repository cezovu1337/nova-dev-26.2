package net.minecraft.network.play.server;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class SUpdateTimePacket implements IPacket<ClientGamePacketListener> {
    @Override
    public PacketType<? extends net.minecraft.network.protocol.Packet<ClientGamePacketListener>> type() {
        return null;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
    }
}
