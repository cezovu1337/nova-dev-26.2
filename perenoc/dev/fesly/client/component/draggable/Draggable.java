package dev.fesly.client.component.draggable;

import dev.fesly.impl.interfaces.client.IAccess;
import lombok.Data;

@Data
public abstract class Draggable implements IAccess {
    protected final String id;
    protected int x;
    protected int y;
    protected transient float width;
    protected transient float height;

    public Draggable(String id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract boolean isVisible();

    public abstract void draw(double mouseX, double mouseY, float partialTicks);
    public abstract void drawPre(double mouseX, double mouseY, float partialTicks);

}
