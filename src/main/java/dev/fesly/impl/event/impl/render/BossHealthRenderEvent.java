package dev.fesly.impl.event.impl.render;
import dev.fesly.impl.event.Event;
public final class BossHealthRenderEvent implements Event {
    private final float partialTicks;
    @java.lang.SuppressWarnings("all")
    public float getPartialTicks() {
        return this.partialTicks;
    }
    @java.lang.SuppressWarnings("all")
    public BossHealthRenderEvent(final float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
