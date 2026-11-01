package dev.fesly.client.component.draggable;
import dev.fesly.impl.interfaces.client.IAccess;
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
    @java.lang.SuppressWarnings("all")
    public String getId() {
        return this.id;
    }
    @java.lang.SuppressWarnings("all")
    public int getX() {
        return this.x;
    }
    @java.lang.SuppressWarnings("all")
    public int getY() {
        return this.y;
    }
    @java.lang.SuppressWarnings("all")
    public float getWidth() {
        return this.width;
    }
    @java.lang.SuppressWarnings("all")
    public float getHeight() {
        return this.height;
    }
    @java.lang.SuppressWarnings("all")
    public void setX(final int x) {
        this.x = x;
    }
    @java.lang.SuppressWarnings("all")
    public void setY(final int y) {
        this.y = y;
    }
    @java.lang.SuppressWarnings("all")
    public void setWidth(final float width) {
        this.width = width;
    }
    @java.lang.SuppressWarnings("all")
    public void setHeight(final float height) {
        this.height = height;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Draggable)) return false;
        final Draggable other = (Draggable) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.getX() != other.getX()) return false;
        if (this.getY() != other.getY()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Draggable;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "Draggable(id=" + this.getId() + ", x=" + this.getX() + ", y=" + this.getY() + ", width=" + this.getWidth() + ", height=" + this.getHeight() + ")";
    }
}
