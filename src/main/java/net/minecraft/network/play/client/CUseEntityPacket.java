package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CUseEntityPacket implements IPacket {
    public enum Action {
        INTERACT, ATTACK, INTERACT_AT
    }

    private final Object entity;
    private final boolean isSneaking;

    public CUseEntityPacket(Object entity, boolean isSneaking) {
        this.entity = entity;
        this.isSneaking = isSneaking;
    }

    public Object getEntity() {
        return this.entity;
    }

    public boolean isSneaking() {
        return this.isSneaking;
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
