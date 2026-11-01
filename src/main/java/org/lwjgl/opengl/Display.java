package org.lwjgl.opengl;

import net.minecraft.client.Minecraft;

public class Display {
    public static void setTitle(String title) {
        try {
            Minecraft.getInstance().getWindow().setTitle(title);
        } catch (Exception ignored) {}
    }

    public static int getWidth() {
        return Minecraft.getInstance().getWindow().getWidth();
    }

    public static int getHeight() {
        return Minecraft.getInstance().getWindow().getHeight();
    }

    public static void destroy() {
    }
}
