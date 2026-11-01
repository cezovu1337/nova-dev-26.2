package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.Event;
public class MouseReleaseEvent implements Event {
	@java.lang.SuppressWarnings("all")
	public MouseReleaseEvent() {
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof MouseReleaseEvent)) return false;
		final MouseReleaseEvent other = (MouseReleaseEvent) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof MouseReleaseEvent;
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
		return "MouseReleaseEvent()";
	}
}
