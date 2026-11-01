package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CPlayerTryUseItemPacket implements IPacket {
    private Object hand;

    public CPlayerTryUseItemPacket(Object hand) {
        this.hand = hand;
    }

    public Object getHand() {
        return this.hand;
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
