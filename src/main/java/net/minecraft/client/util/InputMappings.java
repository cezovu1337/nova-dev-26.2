package net.minecraft.client.util;

import com.mojang.blaze3d.platform.InputConstants;

public class InputMappings {
    public static class Input {
        private final InputConstants.Key key;

        public Input(InputConstants.Key key) {
            this.key = key;
        }

        public int getKeyCode() {
            return this.key != null ? this.key.getValue() : -1;
        }
    }

    public static boolean isKeyDown(long window, int key) {
        return org.lwjgl.glfw.GLFW.glfwGetKey(window, key) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    public static Input getInputByName(String name) {
        try {
            return new Input(InputConstants.getKey(name));
        } catch (Exception e) {
            return new Input(null);
        }
    }
}
