package dev.fesly.impl.util.render;
import org.joml.Vector2d;
public final class GUIUtil {
    public static boolean mouseOver(final double posX, final double posY, final double width, final double height, final double mouseX, final double mouseY) {
        return mouseX > posX && mouseX < posX + width && mouseY > posY && mouseY < posY + height;
    }
    public static boolean mouseOver(final Vector2d position, final Vector2d scale, final double mouseX, final double mouseY) {
        return mouseX > position.x && mouseX < position.x + scale.x && mouseY > position.y && mouseY < position.y + scale.y;
    }
    @java.lang.SuppressWarnings("all")
    private GUIUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
