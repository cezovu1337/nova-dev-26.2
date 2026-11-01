package dev.fesly.impl.event.impl.render;

import dev.fesly.impl.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.ActiveRenderInfo;

@Getter
@AllArgsConstructor
public final class BossHealthRenderEvent implements Event {
    private final float partialTicks;
}
