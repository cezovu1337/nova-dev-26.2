package org.lwjgl.input;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Mouse {
    public static int dWheel = 0;

    public static int getDWheel() {
        int w = dWheel;
        dWheel = 0;
        return w;
    }

    public static boolean hasWheel() {
        return true;
    }

    public static int getX() {
        try {
            double[] xpos = new double[1];
            long window = Minecraft.getInstance().getWindow().handle();
            GLFW.glfwGetCursorPos(window, xpos, null);
            return (int) xpos[0];
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getY() {
        try {
            double[] ypos = new double[1];
            long window = Minecraft.getInstance().getWindow().handle();
            GLFW.glfwGetCursorPos(window, null, ypos);
            return Minecraft.getInstance().getWindow().getHeight() - (int) ypos[0];
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isButtonDown(int button) {
        try {
            long window = Minecraft.getInstance().getWindow().handle();
            return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
        } catch (Exception e) {
            return false;
        }
    }
}
