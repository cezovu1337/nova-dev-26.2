package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.Event;
import net.minecraft.world.entity.Entity;
public class VisualRotationEvent implements Event {
    private float yawOffset;
    private float yawHead;
    private float pitch;
    @java.lang.SuppressWarnings("all")
    public VisualRotationEvent(final float yawOffset, final float yawHead, final float pitch) {
        this.yawOffset = yawOffset;
        this.yawHead = yawHead;
        this.pitch = pitch;
    }
    @java.lang.SuppressWarnings("all")
    public float getYawOffset() {
        return this.yawOffset;
    }
    @java.lang.SuppressWarnings("all")
    public float getYawHead() {
        return this.yawHead;
    }
    @java.lang.SuppressWarnings("all")
    public float getPitch() {
        return this.pitch;
    }
    @java.lang.SuppressWarnings("all")
    public void setYawOffset(final float yawOffset) {
        this.yawOffset = yawOffset;
    }
    @java.lang.SuppressWarnings("all")
    public void setYawHead(final float yawHead) {
        this.yawHead = yawHead;
    }
    @java.lang.SuppressWarnings("all")
    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
}
