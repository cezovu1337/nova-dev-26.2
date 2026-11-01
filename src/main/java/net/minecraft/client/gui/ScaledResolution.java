package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;

public class ScaledResolution {
    private final int scaledWidth;
    private final int scaledHeight;
    private final int scaleFactor;

    public ScaledResolution(Minecraft mc) {
        if (mc == null) mc = Minecraft.getInstance();
        this.scaledWidth = mc.getWindow().getGuiScaledWidth();
        this.scaledHeight = mc.getWindow().getGuiScaledHeight();
        this.scaleFactor = Math.max(1, (int) mc.getWindow().getGuiScale());
    }

    public int getScaledWidth() {
        return scaledWidth;
    }

    public int getScaledHeight() {
        return scaledHeight;
    }

    public double getScaledWidth_double() {
        return scaledWidth;
    }

    public double getScaledHeight_double() {
        return scaledHeight;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }
}
