package dev.fesly.impl.event.impl.player;
import dev.fesly.impl.event.CancellableEvent;
public class PositionSendEvent extends CancellableEvent {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private boolean canceled;
    public PositionSendEvent(double x, double y, double z, float yaw, float pitch, Boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PositionSendEvent)) return false;
        final PositionSendEvent other = (PositionSendEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (java.lang.Double.compare(this.getX(), other.getX()) != 0) return false;
        if (java.lang.Double.compare(this.getY(), other.getY()) != 0) return false;
        if (java.lang.Double.compare(this.getZ(), other.getZ()) != 0) return false;
        if (java.lang.Float.compare(this.getYaw(), other.getYaw()) != 0) return false;
        if (java.lang.Float.compare(this.getPitch(), other.getPitch()) != 0) return false;
        if (this.isOnGround() != other.isOnGround()) return false;
        if (this.isCanceled() != other.isCanceled()) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof PositionSendEvent;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $x = java.lang.Double.doubleToLongBits(this.getX());
        result = result * PRIME + (int) ($x >>> 32 ^ $x);
        final long $y = java.lang.Double.doubleToLongBits(this.getY());
        result = result * PRIME + (int) ($y >>> 32 ^ $y);
        final long $z = java.lang.Double.doubleToLongBits(this.getZ());
        result = result * PRIME + (int) ($z >>> 32 ^ $z);
        result = result * PRIME + java.lang.Float.floatToIntBits(this.getYaw());
        result = result * PRIME + java.lang.Float.floatToIntBits(this.getPitch());
        result = result * PRIME + (this.isOnGround() ? 79 : 97);
        result = result * PRIME + (this.isCanceled() ? 79 : 97);
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "PositionSendEvent(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ", onGround=" + this.isOnGround() + ", canceled=" + this.isCanceled() + ")";
    }
    @java.lang.SuppressWarnings("all")
    public double getX() {
        return this.x;
    }
    @java.lang.SuppressWarnings("all")
    public double getY() {
        return this.y;
    }
    @java.lang.SuppressWarnings("all")
    public double getZ() {
        return this.z;
    }
    @java.lang.SuppressWarnings("all")
    public float getYaw() {
        return this.yaw;
    }
    @java.lang.SuppressWarnings("all")
    public float getPitch() {
        return this.pitch;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isOnGround() {
        return this.onGround;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isCanceled() {
        return this.canceled;
    }
    @java.lang.SuppressWarnings("all")
    public void setX(final double x) {
        this.x = x;
    }
    @java.lang.SuppressWarnings("all")
    public void setY(final double y) {
        this.y = y;
    }
    @java.lang.SuppressWarnings("all")
    public void setZ(final double z) {
        this.z = z;
    }
    @java.lang.SuppressWarnings("all")
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    @java.lang.SuppressWarnings("all")
    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }
    @java.lang.SuppressWarnings("all")
    public void setCanceled(final boolean canceled) {
        this.canceled = canceled;
    }
}
