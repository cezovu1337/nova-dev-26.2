package dev.fesly.client.screen.clientmenu.impl;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.client.screen.IScreen;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import org.joml.Vector2d;
public class CategoryScreen implements IScreen, IAccess {
    public Vector2d position;
    public Vector2d size;
    @Override
    public void init() {
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        final var clientMenu = nova.getClientMenu();
        position = clientMenu.position;
        size = clientMenu.size;
        double x = position.x;
        double y = position.y;
        double width = size.x;
        double height = size.y;
        Fonts.ROBOTO_SEMI.get(20).draw(matrixStack, "Settings", x + 10, y + 10, -1);
    }
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
    }
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }
    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }
    @Override
    public void update() {
    }
    @Override
    public void bloom() {
    }
    @Override
    public void onClose() {
    }
    @java.lang.SuppressWarnings("all")
    public CategoryScreen() {
    }
    @java.lang.SuppressWarnings("all")
    public Vector2d getPosition() {
        return this.position;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2d getSize() {
        return this.size;
    }
    @java.lang.SuppressWarnings("all")
    public void setPosition(final Vector2d position) {
        this.position = position;
    }
    @java.lang.SuppressWarnings("all")
    public void setSize(final Vector2d size) {
        this.size = size;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof CategoryScreen)) return false;
        final CategoryScreen other = (CategoryScreen) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$position = this.getPosition();
        final java.lang.Object other$position = other.getPosition();
        if (this$position == null ? other$position != null : !this$position.equals(other$position)) return false;
        final java.lang.Object this$size = this.getSize();
        final java.lang.Object other$size = other.getSize();
        if (this$size == null ? other$size != null : !this$size.equals(other$size)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof CategoryScreen;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $position = this.getPosition();
        result = result * PRIME + ($position == null ? 43 : $position.hashCode());
        final java.lang.Object $size = this.getSize();
        result = result * PRIME + ($size == null ? 43 : $size.hashCode());
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "CategoryScreen(position=" + this.getPosition() + ", size=" + this.getSize() + ")";
    }
}
