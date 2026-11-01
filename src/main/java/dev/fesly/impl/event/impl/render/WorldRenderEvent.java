package dev.fesly.impl.event.impl.render;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.event.Event;
import net.minecraft.client.renderer.ActiveRenderInfo;
public final class WorldRenderEvent implements Event {
    private final MatrixStack matrixStack;
    private final ActiveRenderInfo activeRenderInfo;
    private final float partialTicks;
    @java.lang.SuppressWarnings("all")
    public MatrixStack getMatrixStack() {
        return this.matrixStack;
    }
    @java.lang.SuppressWarnings("all")
    public ActiveRenderInfo getActiveRenderInfo() {
        return this.activeRenderInfo;
    }
    @java.lang.SuppressWarnings("all")
    public float getPartialTicks() {
        return this.partialTicks;
    }
    @java.lang.SuppressWarnings("all")
    public WorldRenderEvent(final MatrixStack matrixStack, final ActiveRenderInfo activeRenderInfo, final float partialTicks) {
        this.matrixStack = matrixStack;
        this.activeRenderInfo = activeRenderInfo;
        this.partialTicks = partialTicks;
    }
}
