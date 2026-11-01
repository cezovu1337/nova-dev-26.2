package net.minecraft.network.play.client;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;

public class CPlayerPacket implements IPacket {
    protected double x;
    protected double y;
    protected double z;
    protected float yaw;
    protected float pitch;
    protected boolean onGround;
    protected boolean moving;
    protected boolean rotating;

    public CPlayerPacket() {
    }

    public CPlayerPacket(boolean onGroundIn) {
        this.onGround = onGroundIn;
    }

    public double getX(double defaultValue) {
        return this.moving ? this.x : defaultValue;
    }

    public double getY(double defaultValue) {
        return this.moving ? this.y : defaultValue;
    }

    public double getZ(double defaultValue) {
        return this.moving ? this.z : defaultValue;
    }

    public float getYaw(float defaultValue) {
        return this.rotating ? this.yaw : defaultValue;
    }

    public float getPitch(float defaultValue) {
        return this.rotating ? this.pitch : defaultValue;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener listener) {
    }

    public static class PositionPacket extends CPlayerPacket {
        public PositionPacket() {
            this.moving = true;
        }

        public PositionPacket(double xIn, double yIn, double zIn, boolean onGroundIn) {
            this.x = xIn;
            this.y = yIn;
            this.z = zIn;
            this.onGround = onGroundIn;
            this.moving = true;
        }
    }

    public static class PositionRotationPacket extends CPlayerPacket {
        public PositionRotationPacket() {
            this.moving = true;
            this.rotating = true;
        }

        public PositionRotationPacket(double xIn, double yIn, double zIn, float yawIn, float pitchIn, boolean onGroundIn) {
            this.x = xIn;
            this.y = yIn;
            this.z = zIn;
            this.yaw = yawIn;
            this.pitch = pitchIn;
            this.onGround = onGroundIn;
            this.moving = true;
            this.rotating = true;
        }
    }

    public static class RotationPacket extends CPlayerPacket {
        public RotationPacket() {
            this.rotating = true;
        }

        public RotationPacket(float yawIn, float pitchIn, boolean onGroundIn) {
            this.yaw = yawIn;
            this.pitch = pitchIn;
            this.onGround = onGroundIn;
            this.rotating = true;
        }
    }
}
