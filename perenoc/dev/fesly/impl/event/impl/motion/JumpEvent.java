package dev.fesly.impl.event.impl.motion;

import dev.fesly.impl.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class JumpEvent extends CancellableEvent {
    private float yaw;
}