package net.minecraft.client.network.play;

import net.minecraft.network.protocol.Packet;

public interface ClientPlayNetHandler {
    void sendPacket(Packet<?> packet);
}
