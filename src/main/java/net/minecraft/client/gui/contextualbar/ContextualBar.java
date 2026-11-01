package net.minecraft.client.gui.contextualbar;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.optifine.Config;
import net.optifine.CustomColors;

public interface ContextualBar {
    int WIDTH = 182;
    int HEIGHT = 5;
    int MARGIN_BOTTOM = 24;
    ContextualBar EMPTY = new ContextualBar() {
        @Override
        public void extractBackground(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        }
    };

    default int left(Window window) {
        return (window.getGuiScaledWidth() - 182) / 2;
    }

    default int top(Window window) {
        return window.getGuiScaledHeight() - 24 - 5 - (dev.fesly.Nova.getInstance().getClientUI().isCustomHotbar() ? dev.fesly.client.module.impl.render.Interface.animation.getNumberValue().intValue() : 0);
    }

    void extractBackground(GuiGraphicsExtractor var1, DeltaTracker var2);

    void extractRenderState(GuiGraphicsExtractor var1, DeltaTracker var2);

    static void extractExperienceLevel(GuiGraphicsExtractor graphics, Font font, int experienceLevel) {
        Component str = Component.translatable("gui.experience.level", experienceLevel);
        int x = (graphics.guiWidth() - font.width(str)) / 2;
        int customShift = dev.fesly.Nova.getInstance().getClientUI().isCustomHotbar() ? dev.fesly.client.module.impl.render.Interface.animation.getNumberValue().intValue() : 0;
        int y = graphics.guiHeight() - 24 - 9 - 2 - customShift;
        graphics.text(font, str, x + 1, y, -16777216, false);
        graphics.text(font, str, x - 1, y, -16777216, false);
        graphics.text(font, str, x, y + 1, -16777216, false);
        graphics.text(font, str, x, y - 1, -16777216, false);
        int col = -8323296;
        if (Config.isCustomColors()) {
            col = CustomColors.getExpBarTextColor(col);
        }

        graphics.text(font, str, x, y, col, false);
    }
}
