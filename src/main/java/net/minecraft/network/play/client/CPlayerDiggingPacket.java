package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CPlayerDiggingPacket implements IPacket {
    public enum Action {
        START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK, DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, SWAP_ITEM_WITH_OFFHAND
    }

    private Action action;
    private Object pos;
    private Object facing;

    public CPlayerDiggingPacket() {
    }

    public CPlayerDiggingPacket(Action action, Object pos, Object facing) {
        this.action = action;
        this.pos = pos;
        this.facing = facing;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
