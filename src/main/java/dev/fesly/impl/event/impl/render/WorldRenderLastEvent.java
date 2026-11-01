package dev.fesly.impl.event.impl.render;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.event.Event;
import net.minecraft.client.renderer.ActiveRenderInfo;
public final class WorldRenderLastEvent implements Event {
    private boolean shadersRender;
    private final MatrixStack matrixStack;
    private final ActiveRenderInfo activeRenderInfo;
    private final float partialTicks;
    @java.lang.SuppressWarnings("all")
    public boolean isShadersRender() {
        return this.shadersRender;
    }
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
    public WorldRenderLastEvent(final boolean shadersRender, final MatrixStack matrixStack, final ActiveRenderInfo activeRenderInfo, final float partialTicks) {
        this.shadersRender = shadersRender;
        this.matrixStack = matrixStack;
        this.activeRenderInfo = activeRenderInfo;
        this.partialTicks = partialTicks;
    }
}
