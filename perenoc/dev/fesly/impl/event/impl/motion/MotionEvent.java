package dev.fesly.impl.event.impl.motion;

import dev.fesly.impl.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public final class MotionEvent extends CancellableEvent {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private boolean isCrouching;
    private boolean isSprinting;
}