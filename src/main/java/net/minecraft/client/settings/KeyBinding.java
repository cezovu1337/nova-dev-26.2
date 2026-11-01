package net.minecraft.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyBinding extends KeyMapping {
    private KeyMapping wrapped;
    public KeyCodeWrapper keyCode = new KeyCodeWrapper();

    public static class KeyCodeWrapper {
        public int getKeyCode() {
            return 0;
        }
    }

    public KeyBinding(String description, int keyCodeVal, Category category) {
        super(description, keyCodeVal, category);
        this.keyCode = new KeyCodeWrapper() {
            @Override
            public int getKeyCode() {
                return getKey().getValue();
            }
        };
    }

    public KeyBinding(KeyMapping wrapped) {
        super(wrapped.getName(), wrapped.getKey().getValue(), Category.MOVEMENT);
        this.wrapped = wrapped;
        ALL.put(wrapped.getName(), wrapped);
        this.keyCode = new KeyCodeWrapper() {
            @Override
            public int getKeyCode() {
                return wrapped.getKey().getValue();
            }
        };
    }

    @Override
    public void setDown(boolean down) {
        super.setDown(down);
        if (wrapped != null) {
            wrapped.setDown(down);
        }
    }

    public void setPressed(boolean value) {
        this.setDown(value);
    }

    public boolean isKeyDown() {
        if (wrapped != null) {
            return wrapped.isDown();
        }
        return this.isDown();
    }

    @Override
    public boolean isDown() {
        if (wrapped != null) {
            return wrapped.isDown();
        }
        return super.isDown();
    }
}
