package net.minecraft.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketType;

public interface IPacket<T extends PacketListener> extends Packet<T> {
    @Override
    default PacketType<? extends Packet<T>> type() {
        return null;
    }

    @Override
    default void handle(T listener) {
    }
}
