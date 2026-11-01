package dev.fesly.impl.event.impl.render;


import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.ActiveRenderInfo;

@Getter
@AllArgsConstructor
public final class WorldRenderEvent implements Event {
    private final MatrixStack matrixStack;
    private final ActiveRenderInfo activeRenderInfo;
    private final float partialTicks;
}
