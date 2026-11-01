package dev.fesly.impl.event.impl.other;

import dev.fesly.impl.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.entity.Entity;

@Data
@AllArgsConstructor
public final class AttackEvent extends CancellableEvent {
    private Entity target;
}