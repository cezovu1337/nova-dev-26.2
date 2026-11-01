package dev.fesly.impl.value.clientValues;

import dev.fesly.impl.value.secondValues.Setting;
import lombok.Getter;

import java.util.function.BooleanSupplier;

@Getter
public abstract class ClientSetting extends Setting {
    private final ClientUICategory category;
    private final BooleanSupplier visible;

    public ClientSetting(String name, int height, ClientUICategory category) {
        super(name, height,() -> true);
        this.visible = () -> true;
        this.category = category;
    }
    public ClientSetting(String name, BooleanSupplier visible, int height, ClientUICategory category) {
        super(name, height,visible);
        this.visible = visible;

        this.category = category;
    }

    public abstract void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks);
}
