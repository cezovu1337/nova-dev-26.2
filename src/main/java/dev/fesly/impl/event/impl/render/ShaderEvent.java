package dev.fesly.impl.event.impl.render;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.event.Event;
public class ShaderEvent implements Event {
    private final MatrixStack matrix;
    private final boolean bloom;
    @java.lang.SuppressWarnings("all")
    public MatrixStack getMatrix() {
        return this.matrix;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isBloom() {
        return this.bloom;
    }
    @java.lang.SuppressWarnings("all")
    public ShaderEvent(final MatrixStack matrix, final boolean bloom) {
        this.matrix = matrix;
        this.bloom = bloom;
    }
}
