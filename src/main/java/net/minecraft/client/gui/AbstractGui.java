package net.minecraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.util.render.RoundedUtils;
import net.minecraft.util.ResourceLocation;

public class AbstractGui {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/menu_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

    public static void fill(MatrixStack ms, int minX, int minY, int maxX, int maxY, int color) {
        RoundedUtils.drawRoundedRect(minX, minY, maxX, maxY, 0, color);
    }

    public static void fill(MatrixStack ms, float minX, float minY, float maxX, float maxY, int color) {
        RoundedUtils.drawRoundedRect(minX, minY, maxX, maxY, 0, color);
    }
}
