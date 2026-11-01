package net.minecraft.network.login.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CEncryptionResponsePacket implements IPacket {
    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener listener) {
    }
}
