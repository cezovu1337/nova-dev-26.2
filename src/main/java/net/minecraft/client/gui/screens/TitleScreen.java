package net.minecraft.client.gui.screens;

import dev.fesly.impl.client.screen.mainmenu.MainMenu;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.texture.TextureManager;

public class TitleScreen extends MainMenu {
    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean fading) {
        super(fading);
    }

    public TitleScreen(boolean fading, LogoRenderer logoRenderer) {
        super(fading);
    }

    public static void registerTextures(TextureManager textureManager) {
    }
}
