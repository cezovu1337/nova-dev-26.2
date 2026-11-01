package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.CancellableEvent;
public class MoveInputEvent extends CancellableEvent {
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneaking;
    private double sneakSlowDownMultiplier;
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof MoveInputEvent)) return false;
        final MoveInputEvent other = (MoveInputEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        if (java.lang.Float.compare(this.getForward(), other.getForward()) != 0) return false;
        if (java.lang.Float.compare(this.getStrafe(), other.getStrafe()) != 0) return false;
        if (this.isJump() != other.isJump()) return false;
        if (this.isSneaking() != other.isSneaking()) return false;
        if (java.lang.Double.compare(this.getSneakSlowDownMultiplier(), other.getSneakSlowDownMultiplier()) != 0) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof MoveInputEvent;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + java.lang.Float.floatToIntBits(this.getForward());
        result = result * PRIME + java.lang.Float.floatToIntBits(this.getStrafe());
        result = result * PRIME + (this.isJump() ? 79 : 97);
        result = result * PRIME + (this.isSneaking() ? 79 : 97);
        final long $sneakSlowDownMultiplier = java.lang.Double.doubleToLongBits(this.getSneakSlowDownMultiplier());
        result = result * PRIME + (int) ($sneakSlowDownMultiplier >>> 32 ^ $sneakSlowDownMultiplier);
        return result;
    }
    @java.lang.SuppressWarnings("all")
    public float getForward() {
        return this.forward;
    }
    @java.lang.SuppressWarnings("all")
    public float getStrafe() {
        return this.strafe;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isJump() {
        return this.jump;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isSneaking() {
        return this.sneaking;
    }
    @java.lang.SuppressWarnings("all")
    public double getSneakSlowDownMultiplier() {
        return this.sneakSlowDownMultiplier;
    }
    @java.lang.SuppressWarnings("all")
    public void setForward(final float forward) {
        this.forward = forward;
    }
    @java.lang.SuppressWarnings("all")
    public void setStrafe(final float strafe) {
        this.strafe = strafe;
    }
    @java.lang.SuppressWarnings("all")
    public void setJump(final boolean jump) {
        this.jump = jump;
    }
    @java.lang.SuppressWarnings("all")
    public void setSneaking(final boolean sneaking) {
        this.sneaking = sneaking;
    }
    @java.lang.SuppressWarnings("all")
    public void setSneakSlowDownMultiplier(final double sneakSlowDownMultiplier) {
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "MoveInputEvent(forward=" + this.getForward() + ", strafe=" + this.getStrafe() + ", jump=" + this.isJump() + ", sneaking=" + this.isSneaking() + ", sneakSlowDownMultiplier=" + this.getSneakSlowDownMultiplier() + ")";
    }
    @java.lang.SuppressWarnings("all")
    public MoveInputEvent(final float forward, final float strafe, final boolean jump, final boolean sneaking, final double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneaking = sneaking;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }
}
