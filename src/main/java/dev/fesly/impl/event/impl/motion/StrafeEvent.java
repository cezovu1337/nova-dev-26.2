package dev.fesly.impl.event.impl.motion;
import dev.fesly.impl.event.CancellableEvent;
import dev.fesly.impl.util.player.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.math.vector.Vector3d;
public final class StrafeEvent extends CancellableEvent {
    private float friction;
    private Vector3d relative;
    private float yaw;
    public void setMotion(double speed, float strafeMotion) {
        float remainder = 1 - strafeMotion;
        LocalPlayer player = Minecraft.getInstance().player;
        strafeMotion *= 0.9;
        if (player.isOnGround()) {
            MoveUtil.setSpeed(speed);
        } else {
            double mx = player.getDeltaMovement().x * strafeMotion;
            double mz = player.getDeltaMovement().z * strafeMotion;
            player.setDeltaMovement(mx, player.getDeltaMovement().y, mz);
            friction = (float) speed * remainder;
        }
    }
    @java.lang.SuppressWarnings("all")
    public float getFriction() {
        return this.friction;
    }
    @java.lang.SuppressWarnings("all")
    public Vector3d getRelative() {
        return this.relative;
    }
    @java.lang.SuppressWarnings("all")
    public float getYaw() {
        return this.yaw;
    }
    @java.lang.SuppressWarnings("all")
    public void setFriction(final float friction) {
        this.friction = friction;
    }
    @java.lang.SuppressWarnings("all")
    public void setRelative(final Vector3d relative) {
        this.relative = relative;
    }
    @java.lang.SuppressWarnings("all")
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof StrafeEvent)) return false;
        final StrafeEvent other = (StrafeEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (java.lang.Float.compare(this.getFriction(), other.getFriction()) != 0) return false;
        if (java.lang.Float.compare(this.getYaw(), other.getYaw()) != 0) return false;
        final java.lang.Object this$relative = this.getRelative();
        final java.lang.Object other$relative = other.getRelative();
        if (this$relative == null ? other$relative != null : !this$relative.equals(other$relative)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof StrafeEvent;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + java.lang.Float.floatToIntBits(this.getFriction());
        result = result * PRIME + java.lang.Float.floatToIntBits(this.getYaw());
        final java.lang.Object $relative = this.getRelative();
        result = result * PRIME + ($relative == null ? 43 : $relative.hashCode());
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "StrafeEvent(friction=" + this.getFriction() + ", relative=" + this.getRelative() + ", yaw=" + this.getYaw() + ")";
    }
    @java.lang.SuppressWarnings("all")
    public StrafeEvent(final float friction, final Vector3d relative, final float yaw) {
        this.friction = friction;
        this.relative = relative;
        this.yaw = yaw;
    }
}
