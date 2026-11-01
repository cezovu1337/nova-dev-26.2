package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class MoveInputEvent extends CancellableEvent {
    private float forward, strafe;
    private boolean jump, sneaking;
    private double sneakSlowDownMultiplier;
}
