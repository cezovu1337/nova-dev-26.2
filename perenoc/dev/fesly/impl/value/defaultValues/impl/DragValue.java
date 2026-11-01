package dev.fesly.impl.value.defaultValues.impl;

import dev.fesly.client.module.api.Module;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.value.defaultValues.Mode;
import dev.fesly.impl.value.defaultValues.Value;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import org.joml.Vector2d;

import java.util.List;
import java.util.function.BooleanSupplier;

import static dev.fesly.impl.util.animation.compact.Easing.EASE_OUT_EXPO;
import static dev.fesly.impl.util.animation.compact.Easing.LINEAR;

@Getter
@Setter
public class DragValue extends Value<Vector2d> {

    public Vector2d position = new Vector2d(100, 100), targetPosition = new Vector2d(100, 100), scale = new Vector2d(100, 100), lastScale = new Vector2d(-1, -1);
    public Animation animationPosition = new Animation(LINEAR, 600), smoothAnimation = new Animation(EASE_OUT_EXPO, 300);
    public boolean render = true, structure;

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

        if (this.position.x > width / 2f) {
            this.targetPosition.x += this.lastScale.x - this.scale.x;
            this.position = targetPosition;
        }

        if (this.position.y > height / 2f) {
            this.targetPosition.y += this.lastScale.y - this.scale.y;
            this.position = targetPosition;
        }

        this.lastScale = scale;
    }
}