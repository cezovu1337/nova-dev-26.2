package dev.fesly.impl.client.screen.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.Color;
import java.util.Random;

import static dev.fesly.impl.util.render.HudEffects.detime;

public class MainMenu extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("nova", "icons/loadscreen/background.png");
    private static final ResourceLocation SMILE_FACE_TEXTURE = new ResourceLocation("nova", "icons/loadscreen/smile.png");
    private static final ResourceLocation SMILE_FACE2_TEXTURE = new ResourceLocation("nova", "icons/loadscreen/smileface2.png");
    private static final ResourceLocation SMILE_FACE_BG = new ResourceLocation("nova", "icons/loadscreen/applybg.png");

    public static final float random = new Random().nextFloat();

    private final boolean showFadeInAnimation;
    private long firstRenderTime;
    public AltManager altManager = new AltManager();

    public MainMenu() {
        this(false);
    }

    public MainMenu(boolean fadeIn) {
        super(new TranslationTextComponent("narrator.screen.title"));
        this.showFadeInAnimation = fadeIn;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        altManager.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void init() {
        this.buttons.clear();
        MainWindow mw = Minecraft.getInstance().getMainWindow();

        float w = 144;
        float h = 153;

        float x = mw.getScaledWidth() / 2f - w / 2f;
        float y = mw.getScaledHeight() / 2f - h / 2f;

        float bw = w * 0.8f;
        float by = h * 0.515f;
        int height = 17;

        this.addButton(new MainMenuButton(
                (int) (x + w / 2f - bw / 2f), (int) (y + by), (int) bw, height,
                new TranslationTextComponent("menu.singleplayer"),
                p -> this.minecraft.displayScreen(new SelectWorldScreen(this))
        ));

        this.addButton(new MainMenuButton(
                (int) (x + w / 2f - bw / 2f), (int) (y + by + height + 5), (int) bw, height,
                new TranslationTextComponent("menu.multiplayer"),
                p -> this.minecraft.displayScreen(new JoinMultiplayerScreen(this))
        ));

        this.addButton(new MainMenuButton(
                (int) (x + w / 2f - bw / 2f), (int) (y + by + height * 2 + 10), (int) bw, height,
                new TranslationTextComponent("menu.altmanager"),
                p -> altManager.check()
        ));

        this.addButton(new MainMenuButton(
                5, 5, 65, height,
                new TranslationTextComponent("menu.options"),
                p -> this.minecraft.displayScreen(new net.minecraft.client.gui.screens.options.OptionsScreen(this, this.minecraft.options, false))
        ));

        this.addButton(new MainMenuButton(
                mw.getScaledWidth() - 55, 5, 50, height,
                new TranslationTextComponent("menu.quit"),
                p -> this.minecraft.stop()
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.firstRenderTime == 0L && this.showFadeInAnimation) {
            this.firstRenderTime = Util.getMillis();
        }

        float f = this.showFadeInAnimation ? (float) (Util.getMillis() - this.firstRenderTime) / 2000.0F : 1.0F;
        MainWindow mw = Minecraft.getInstance().getMainWindow();

        RenderUtil.drawRect(0, 0, mw.getScaledWidth() + 1, mw.getScaledHeight(), 0xFF000000);
        RenderUtil.drawImage(BACKGROUND_TEXTURE, 0, 0, mw.getScaledWidth() + 1, mw.getScaledHeight(), -1);

        float w = 144;
        float h = 153;
        float x = mw.getScaledWidth() / 2f - w / 2f;
        float y = mw.getScaledHeight() / 2f - h / 2f;

        RenderUtil.roundedRectangle(x, y, w, h, 10, Color.decode("#D9D9D9"));

        StencilUtil.initStencil();
        RenderUtil.roundedRectangle(x, y, w, h, 10, Color.decode("#D9D9D9"));
        StencilUtil.bindReadStencilBuffer(1);
        RenderUtil.drawImage(SMILE_FACE_BG, x - 10, y - 10, w + 20, h + 20, -1);
        StencilUtil.uninitStencilBuffer();

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        double sin = Math.cos(((System.currentTimeMillis() - detime) / 1200f));
        float fWOffer = 127 / 648f;
        float smileW = w * fWOffer;
        Font loadFont = Fonts.ROBOTO_REGULAR.get(19f);
        Font loadFont2 = Fonts.ROBOTO_REGULAR.get(18f);
        Font descriptionFont = Fonts.ROBOTO_LIGHT.get(22f);

        RenderUtil.drawImage(random > 0.5 ? SMILE_FACE_TEXTURE : SMILE_FACE2_TEXTURE,
                x + (w / 2f) - smileW / 2f,
                y + (h * 0.1f) - (float) (sin * 1.5f),
                smileW, smileW, -1);

        loadFont.drawCenterXY("Приятной игры", x + (w / 2f), y + (h * 0.35f), Color.decode("#222222").getRGB());
        loadFont2.drawCenterXY("С Nova Client!", x + (w / 2f), y + (h * 0.42f), Color.decode("#222222").getRGB());

        String description = "сделано с любовью ; by haramdev";
        StencilUtil.initStencil();
        descriptionFont.drawCenterXY(description, x + w / 2f, mw.getScaledHeight() * 0.95f, -1);
        StencilUtil.bindReadStencilBuffer(1);
        RenderUtil.drawImage(SMILE_FACE_BG, x + w / 2f - descriptionFont.getWidth(description) / 2f,
                mw.getScaledHeight() * 0.9f, descriptionFont.getWidth(description), mw.getScaledHeight() * 0.1f,
                new Color(255, 255, 255, 40).getRGB());
        StencilUtil.uninitStencilBuffer();

        altManager.draw(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        altManager.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        altManager.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!altManager.mouseClicked(mouseX, mouseY, button)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
}
