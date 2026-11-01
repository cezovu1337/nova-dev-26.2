package dev.fesly.impl.event.impl.player;
import dev.fesly.impl.event.CancellableEvent;
public class PlayerJumpEvent extends CancellableEvent {
	@java.lang.SuppressWarnings("all")
	public PlayerJumpEvent() {
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof PlayerJumpEvent)) return false;
		final PlayerJumpEvent other = (PlayerJumpEvent) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof PlayerJumpEvent;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int result = 1;
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "PlayerJumpEvent()";
	}
}
