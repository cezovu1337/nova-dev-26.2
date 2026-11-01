package net.minecraft.client.gui.screens;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProgressListener;
import net.optifine.CustomLoadingScreen;
import net.optifine.CustomLoadingScreens;

public class ProgressScreen extends Screen implements ProgressListener {
    private Component header;
    private Component stage;
    private int progress;
    private boolean stop;
    private final boolean clearScreenAfterStop;
    private CustomLoadingScreen customLoadingScreen;

    public ProgressScreen(boolean clearScreenAfterStop) {
        super(GameNarrator.NO_TITLE);
        this.clearScreenAfterStop = clearScreenAfterStop;
        this.customLoadingScreen = CustomLoadingScreens.getCustomLoadingScreen();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void progressStartNoAbort(Component string) {
        this.progressStart(string);
    }

    @Override
    public void progressStart(Component string) {
        this.header = string;
        this.progressStage(Component.translatable("menu.working"));
    }

    @Override
    public void progressStage(Component string) {
        this.stage = string;
        this.progressStagePercentage(0);
    }

    @Override
    public void progressStagePercentage(int i) {
        this.progress = i;
    }

    @Override
    public void stop() {
        this.stop = true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.stop) {
            if (this.clearScreenAfterStop) {
                this.minecraft.gui.setScreen(null);
            }
        } else {
            super.extractRenderState(graphics, mouseX, mouseY, a);
            if (this.progress > 0) {
                if (this.header != null) {
                    graphics.centeredText(this.font, this.header, this.width / 2, 70, -1);
                }

                if (this.stage != null && this.progress != 0) {
                    graphics.centeredText(this.font, Component.empty().append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, -1);
                }
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
        if (this.customLoadingScreen != null && this.minecraft.level == null) {
            this.customLoadingScreen.drawBackground(this.width, this.height);
        } else {
            super.extractBackground(graphicsIn, mouseX, mouseY, partialTicks);
        }
    }
}
