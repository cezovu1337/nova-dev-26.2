package dev.fesly.impl.event.impl.render;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.event.Event;
import net.minecraft.client.MainWindow;
public final class DrawHudPreEvent implements Event {
    private final MatrixStack matrix;
    private final MainWindow mainWindow;
    private final float partialTicks;
    @java.lang.SuppressWarnings("all")
    public MatrixStack getMatrix() {
        return this.matrix;
    }
    @java.lang.SuppressWarnings("all")
    public MainWindow getMainWindow() {
        return this.mainWindow;
    }
    @java.lang.SuppressWarnings("all")
    public float getPartialTicks() {
        return this.partialTicks;
    }
    @java.lang.SuppressWarnings("all")
    public DrawHudPreEvent(final MatrixStack matrix, final MainWindow mainWindow, final float partialTicks) {
        this.matrix = matrix;
        this.mainWindow = mainWindow;
        this.partialTicks = partialTicks;
    }
}
