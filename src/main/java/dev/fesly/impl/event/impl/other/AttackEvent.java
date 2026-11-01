package dev.fesly.impl.event.impl.other;
import dev.fesly.impl.event.CancellableEvent;
import net.minecraft.world.entity.Entity;
public final class AttackEvent extends CancellableEvent {
    private Entity target;
    @java.lang.SuppressWarnings("all")
    public Entity getTarget() {
        return this.target;
    }
    @java.lang.SuppressWarnings("all")
    public void setTarget(final Entity target) {
        this.target = target;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof AttackEvent)) return false;
        final AttackEvent other = (AttackEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$target = this.getTarget();
        final java.lang.Object other$target = other.getTarget();
        if (this$target == null ? other$target != null : !this$target.equals(other$target)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof AttackEvent;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $target = this.getTarget();
        result = result * PRIME + ($target == null ? 43 : $target.hashCode());
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "AttackEvent(target=" + this.getTarget() + ")";
    }
    @java.lang.SuppressWarnings("all")
    public AttackEvent(final Entity target) {
        this.target = target;
    }
}
