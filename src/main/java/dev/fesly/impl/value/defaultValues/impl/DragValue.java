package dev.fesly.impl.value.defaultValues.impl;
import dev.fesly.client.module.api.Module;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.value.defaultValues.Mode;
import dev.fesly.impl.value.defaultValues.Value;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import org.joml.Vector2d;
import java.util.List;
import java.util.function.BooleanSupplier;
import static dev.fesly.impl.util.animation.compact.Easing.EASE_OUT_EXPO;
import static dev.fesly.impl.util.animation.compact.Easing.LINEAR;
public class DragValue extends Value<Vector2d> {
    public Vector2d position = new Vector2d(100, 100);
    public Vector2d targetPosition = new Vector2d(100, 100);
    public Vector2d scale = new Vector2d(100, 100);
    public Vector2d lastScale = new Vector2d(-1, -1);
    public Animation animationPosition = new Animation(LINEAR, 600);
    public Animation smoothAnimation = new Animation(EASE_OUT_EXPO, 300);
    public boolean render = true;
    public boolean structure;
    public DragValue(final String name, final Module parent, final Vector2d defaultValue) {
        super(name, parent, defaultValue);
    }
    public DragValue(final String name, final Module parent, final Vector2d defaultValue, final boolean render) {
        super(name, parent, defaultValue);
        this.render = render;
    }
    public DragValue(final String name, final Module parent, final Vector2d defaultValue, final boolean render, final boolean structure) {
        super(name, parent, defaultValue);
        this.render = render && !structure;
        this.structure = structure;
    }
    public DragValue(final String name, final Mode<?> parent, final Vector2d defaultValue) {
        super(name, parent, defaultValue);
    }
    public DragValue(final String name, final Module parent, final Vector2d defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }
    public DragValue(final String name, final Mode<?> parent, final Vector2d defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }
    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }
    public void setScale(Vector2d scale) {
        this.scale = scale;
        if (lastScale.x == -1 && lastScale.y == -1) {
            this.lastScale = this.scale;
        }
        MainWindow mw = Minecraft.getInstance().getMainWindow();
        int width = mw.getScaledWidth();
        int height = mw.getScaledHeight();
        if (this.position.x > width / 2.0F) {
            this.targetPosition.x += this.lastScale.x - this.scale.x;
            this.position = targetPosition;
        }
        if (this.position.y > height / 2.0F) {
            this.targetPosition.y += this.lastScale.y - this.scale.y;
            this.position = targetPosition;
        }
        this.lastScale = scale;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2d getPosition() {
        return this.position;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2d getTargetPosition() {
        return this.targetPosition;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2d getScale() {
        return this.scale;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2d getLastScale() {
        return this.lastScale;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getAnimationPosition() {
        return this.animationPosition;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getSmoothAnimation() {
        return this.smoothAnimation;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isRender() {
        return this.render;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isStructure() {
        return this.structure;
    }
    @java.lang.SuppressWarnings("all")
    public void setPosition(final Vector2d position) {
        this.position = position;
    }
    @java.lang.SuppressWarnings("all")
    public void setTargetPosition(final Vector2d targetPosition) {
        this.targetPosition = targetPosition;
    }
    @java.lang.SuppressWarnings("all")
    public void setLastScale(final Vector2d lastScale) {
        this.lastScale = lastScale;
    }
    @java.lang.SuppressWarnings("all")
    public void setAnimationPosition(final Animation animationPosition) {
        this.animationPosition = animationPosition;
    }
    @java.lang.SuppressWarnings("all")
    public void setSmoothAnimation(final Animation smoothAnimation) {
        this.smoothAnimation = smoothAnimation;
    }
    @java.lang.SuppressWarnings("all")
    public void setRender(final boolean render) {
        this.render = render;
    }
    @java.lang.SuppressWarnings("all")
    public void setStructure(final boolean structure) {
        this.structure = structure;
    }
}
