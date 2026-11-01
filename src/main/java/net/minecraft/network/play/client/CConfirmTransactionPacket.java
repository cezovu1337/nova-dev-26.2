package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CConfirmTransactionPacket implements IPacket {
    private int windowId;
    private short uid;
    private boolean accepted;

    public CConfirmTransactionPacket() {
    }

    public CConfirmTransactionPacket(int windowId, short uid, boolean accepted) {
        this.windowId = windowId;
        this.uid = uid;
        this.accepted = accepted;
    }

    public int getWindowId() {
        return this.windowId;
    }

    public short getUid() {
        return this.uid;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
