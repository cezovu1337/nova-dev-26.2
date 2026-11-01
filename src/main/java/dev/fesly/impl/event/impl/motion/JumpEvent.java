package dev.fesly.impl.event.impl.motion;
import dev.fesly.impl.event.CancellableEvent;
public class JumpEvent extends CancellableEvent {
    private float yaw;
    @java.lang.SuppressWarnings("all")
    public float getYaw() {
        return this.yaw;
    }
    @java.lang.SuppressWarnings("all")
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "JumpEvent(yaw=" + this.getYaw() + ")";
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof JumpEvent)) return false;
        final JumpEvent other = (JumpEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        if (java.lang.Float.compare(this.getYaw(), other.getYaw()) != 0) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof JumpEvent;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + java.lang.Float.floatToIntBits(this.getYaw());
        return result;
    }
    @java.lang.SuppressWarnings("all")
    public JumpEvent(final float yaw) {
        this.yaw = yaw;
    }
}
