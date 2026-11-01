package net.minecraft.client;

import com.mojang.blaze3d.platform.Window;

public class MainWindow {
    private final Window window;

    public MainWindow(Window window) {
        this.window = window;
    }

    public int getScaledWidth() {
        return window != null ? window.getGuiScaledWidth() : 854;
    }

    public int getScaledHeight() {
        return window != null ? window.getGuiScaledHeight() : 480;
    }

    public double getScaleFactor() {
        return window != null ? window.getGuiScale() : 2.0;
    }

    public int getFramebufferWidth() {
        return window != null ? window.getWidth() : 854;
    }

    public int getFramebufferHeight() {
        return window != null ? window.getHeight() : 480;
    }

    public int getWidth() {
        return getFramebufferWidth();
    }

    public int getHeight() {
        return getFramebufferHeight();
    }

    public long getHandle() {
        return window != null ? window.handle() : 0L;
    }
}
