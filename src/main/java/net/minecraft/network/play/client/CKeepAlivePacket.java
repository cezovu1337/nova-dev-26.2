package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CKeepAlivePacket implements IPacket {
    private long key;

    public CKeepAlivePacket() {
    }

    public CKeepAlivePacket(long key) {
        this.key = key;
    }

    public long getKey() {
        return this.key;
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
