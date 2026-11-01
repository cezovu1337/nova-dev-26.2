package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CMoveVehiclePacket implements IPacket {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public CMoveVehiclePacket() {
    }

    public CMoveVehiclePacket(Object entity) {
    }

    @Override
    public PacketType type() { return null; }
    @Override
    public void handle(net.minecraft.network.PacketListener listener) {}
}
