package net.minecraft.network.play.server;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class SConfirmTransactionPacket implements IPacket {
    private int windowId;
    private short actionNumber;
    private boolean accepted;

    public int getWindowId() {
        return windowId;
    }

    public short getActionNumber() {
        return actionNumber;
    }

    public boolean wasAccepted() {
        return accepted;
    }

    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener listener) {
    }
}
