package net.minecraft.network.play.server;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class SEntityVelocityPacket implements IPacket {
    public int entityID;
    public int motionX;
    public int motionY;
    public int motionZ;

    public int getEntityID() {
        return entityID;
    }

    public int getMotionX() {
        return motionX;
    }

    public int getMotionY() {
        return motionY;
    }

    public int getMotionZ() {
        return motionZ;
    }

    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener listener) {
    }
}
