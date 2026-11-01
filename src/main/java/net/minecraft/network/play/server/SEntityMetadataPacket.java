package net.minecraft.network.play.server;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class SEntityMetadataPacket implements IPacket {
    private int entityId;

    public int getEntityId() {
        return entityId;
    }

    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener listener) {
    }
}
