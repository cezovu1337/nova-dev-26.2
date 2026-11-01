package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@AllArgsConstructor
@Getter
@Setter
public class VisualRotationEvent implements Event {
    private float yawOffset;
    private float yawHead;
    private float pitch;
}
