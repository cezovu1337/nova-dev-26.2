package dev.fesly.impl.value.secondValues;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
public abstract class Setting {
    protected final String name;
    protected String description = "";


    public final Animation visibleAnimation = new Animation(Easing.EASE_OUT_CUBIC, 300);

    public Setting setDescription(String description) {
        this.description = description;
        return this;
    }

    protected final int height;
    private final BooleanSupplier visible;

    public Setting(String name,int height,BooleanSupplier visible) {
        this.name = name;
        this.height = height;
        this.visible = visible;
        visibleAnimation.setValue(1);
    }

    public abstract void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks);

    public abstract void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton);
    public abstract void keyPressed(int keyCode, int scanCode, int modifiers);
    public abstract void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton);

    public abstract void load(JsonElement json);

    public abstract JsonElement save();
    public int getHeight() {
        return height;
    }
}
