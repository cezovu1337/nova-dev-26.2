package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CEntityActionPacket implements IPacket {
    public enum Action {
        PRESS_SHIFT_KEY, RELEASE_SHIFT_KEY, STOP_SLEEPING, START_SPRINTING, STOP_SPRINTING, START_RIDING_JUMP, STOP_RIDING_JUMP, OPEN_INVENTORY, START_FALL_FLYING
    }

    private final Object entity;
    private final Action action;

    public CEntityActionPacket(Object entity, Action action) {
        this.entity = entity;
        this.action = action;
    }

    public Object getEntity() {
        return this.entity;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
