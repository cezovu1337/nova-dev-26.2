package dev.fesly.impl.event;

public class CancellableEvent implements Event {
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isCanceled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setCanceled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setCancelled() {
        this.cancelled = true;
    }
}
