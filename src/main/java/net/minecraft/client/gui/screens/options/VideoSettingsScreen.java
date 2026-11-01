package net.minecraft.client.gui.screens.options;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.optifine.Config;
import net.optifine.Lang;
import net.optifine.config.Option;
import net.optifine.gui.GuiAnimationSettingsOF;
import net.optifine.gui.GuiButtonOF;
import net.optifine.gui.GuiDetailSettingsOF;
import net.optifine.gui.GuiOtherSettingsOF;
import net.optifine.gui.GuiPerformanceSettingsOF;
import net.optifine.gui.GuiQualitySettingsOF;
import net.optifine.gui.GuiQuickInfoOF;
import net.optifine.gui.GuiScreenButtonOF;
import net.optifine.gui.GuiVanillaSettingsOF;
import net.optifine.gui.IGuiScreenOF;
import net.optifine.gui.IOptionControl;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderOptions;
import net.optifine.shaders.gui.GuiShaders;
import net.optifine.util.GuiUtils;
import org.lwjgl.glfw.GLFW;

public class VideoSettingsScreen extends OptionsSubScreen implements IGuiScreenOF {
    private static final Component TITLE = Component.translatable("options.videoTitle");
    private static final Component IMPROVED_TRANSPARENCY = Component.translatable("options.improvedTransparency").withStyle(ChatFormatting.ITALIC);
    private static final Component WARNING_MESSAGE = Component.translatable("options.graphics.warning.message", IMPROVED_TRANSPARENCY, IMPROVED_TRANSPARENCY);
    private static final Component WARNING_TITLE = Component.translatable("options.graphics.warning.title").withStyle(ChatFormatting.RED);
    private static final Component BUTTON_ACCEPT = Component.translatable("options.graphics.warning.accept");
    private static final Component BUTTON_CANCEL = Component.translatable("options.graphics.warning.cancel");
    private static final Component DISPLAY_HEADER = Component.translatable("options.video.display.header");
    private static final Component QUALITY_HEADER = Component.translatable("options.video.quality.header");
    private static final Component PREFERENCES_HEADER = Component.translatable("options.video.preferences.header");
    private static final Component RESTART_REQUIRED = Component.translatable("options.restartRequired").withColor(-2142128);
    private final GpuWarnlistManager gpuWarnlistManager;
    private final int oldMipmaps;
    private final int oldAnisotropyBit;
    private final TextureFilteringMethod oldTextureFiltering;
    private final LinearLayout header = LinearLayout.vertical().spacing(2);
    private StringWidget restartWarning;
    private Screen parentScreen;
    private Options options;
    private Minecraft minecraft = Minecraft.getInstance();
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());
    private List<AbstractWidget> buttonList = GuiUtils.getButtonList(this);
    private AbstractWidget buttonGuiScale;
    protected boolean mousePressed = false;

    public VideoSettingsScreen(Screen lastScreen, Minecraft minecraft, Options options) {
        super(lastScreen, options, TITLE);
        this.gpuWarnlistManager = minecraft.getGpuWarnlistManager();
        this.gpuWarnlistManager.resetWarnings();
        if (options.improvedTransparency().get()) {
            this.gpuWarnlistManager.dismissWarning();
        }

        this.oldMipmaps = options.mipmapLevels().get();
        this.oldAnisotropyBit = options.maxAnisotropyBit().get();
        this.oldTextureFiltering = options.textureFiltering().get();
        this.parentScreen = lastScreen;
        this.minecraft = minecraft;
        this.options = options;
    }

    @Override
    public void init() {
        this.clearWidgets();
        OptionInstance[] videoOptions = new OptionInstance[]{
            this.options.GRAPHICS,
            this.options.RENDER_DISTANCE,
            this.options.AO,
            this.options.SIMULATION_DISTANCE,
            Option.AO_LEVEL,
            this.options.FRAMERATE_LIMIT,
            this.options.GUI_SCALE,
            this.options.ENTITY_SHADOWS,
            this.options.GAMMA,
            Option.DYNAMIC_LIGHTS
        };

        for (int i = 0; i < videoOptions.length; i++) {
            OptionInstance opt = videoOptions[i];
            if (opt != null) {
                int x = this.width / 2 - 155 + i % 2 * 160;
                int y = this.height / 6 + 21 * (i / 2) - 12;
                AbstractWidget guiElement = this.addButton(opt.createButton(this.minecraft.options, x, y, 150));
                guiElement.setTooltip(null);
                if (opt == this.options.GUI_SCALE) {
                    this.buttonGuiScale = guiElement;
                }
            }
        }

        int y = this.height / 6 + 21 * (videoOptions.length / 2) - 12;
        int x = 0;
        x = this.width / 2 - 155 + 0;
        this.addButton(new GuiScreenButtonOF(231, x, y, Lang.get("of.options.shaders")));
        x = this.width / 2 - 155 + 160;
        this.addButton(new GuiButtonOF(220, x, y, 150, 20, I18n.get("of.options.quickInfo")));
        y += 21;
        x = this.width / 2 - 155 + 0;
        this.addButton(new GuiScreenButtonOF(201, x, y, Lang.get("of.options.details")));
        x = this.width / 2 - 155 + 160;
        this.addButton(new GuiScreenButtonOF(202, x, y, Lang.get("of.options.quality")));
        y += 21;
        x = this.width / 2 - 155 + 0;
        this.addButton(new GuiScreenButtonOF(211, x, y, Lang.get("of.options.animations")));
        x = this.width / 2 - 155 + 160;
        this.addButton(new GuiScreenButtonOF(212, x, y, Lang.get("of.options.performance")));
        y += 21;
        x = this.width / 2 - 155 + 0;
        this.addButton(new GuiScreenButtonOF(223, x, y, Lang.get("of.options.vanilla")));
        x = this.width / 2 - 155 + 160;
        this.addButton(new GuiScreenButtonOF(222, x, y, Lang.get("of.options.other")));
        this.addButton(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.get("gui.done")));
        this.buttonList = GuiUtils.getButtonList(this);
    }

    @Override
    protected void addOptions() {
    }

    @Override
    public boolean isMousePressed() {
        return this.mousePressed;
    }

    @Override
    public void setMousePressed(boolean pressed) {
        this.mousePressed = pressed;
    }

    @Override
    public void actionPerformed(AbstractWidget button) {
        OptionInstance opt = button instanceof IOptionControl ioc ? ioc.getControlOption() : null;
        if (opt == this.options.GUI_SCALE) {
            this.guiScaleChanged();
        }

        this.checkFabulousWarning();
        if (button instanceof GuiButtonOF gbo) {
            this.actionPerformed(gbo, 1);
        }
    }

    private void checkFabulousWarning() {
        if (this.gpuWarnlistManager.isShowingWarning()) {
            List<Component> list = Lists.newArrayList(WARNING_MESSAGE, CommonComponents.NEW_LINE);
            String s = this.gpuWarnlistManager.getRendererWarnings();
            if (s != null) {
                list.add(CommonComponents.NEW_LINE);
                list.add(Component.translatable("options.graphics.warning.renderer", s).withStyle(ChatFormatting.GRAY));
            }

            String s1 = this.gpuWarnlistManager.getVendorWarnings();
            if (s1 != null) {
                list.add(CommonComponents.NEW_LINE);
                list.add(Component.translatable("options.graphics.warning.vendor", s1).withStyle(ChatFormatting.GRAY));
            }

            String s2 = this.gpuWarnlistManager.getVersionWarnings();
            if (s2 != null) {
                list.add(CommonComponents.NEW_LINE);
                list.add(Component.translatable("options.graphics.warning.version", s2).withStyle(ChatFormatting.GRAY));
            }

            this.minecraft
                .gui
                .setScreen(
                    new UnsupportedGraphicsWarningScreen(
                        WARNING_TITLE, list, ImmutableList.of(new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_ACCEPT, btn -> {
                            this.options.improvedTransparency().set(true);
                            Minecraft.getInstance().levelExtractor.allChanged();
                            this.gpuWarnlistManager.dismissWarning();
                            this.minecraft.gui.setScreen(this);
                        }), new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_CANCEL, btn -> {
                            this.gpuWarnlistManager.dismissWarning();
                            this.minecraft.gui.setScreen(this);
                        }))
                    )
                );
        }
    }

    @Override
    public void actionPerformedRightClick(AbstractWidget button) {
        OptionInstance opt = button instanceof IOptionControl ioc ? ioc.getControlOption() : null;
        if (opt == this.options.GUI_SCALE) {
            int newScale = this.options.guiScale().get() - 1;
            if (newScale < 0) {
                newScale = Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode());
            }

            this.options.GUI_SCALE.set(newScale);
            this.guiScaleChanged();
        }
    }

    private void guiScaleChanged() {
        int btnWidth = GuiUtils.getWidth(this.buttonGuiScale);
        int btnHeight = GuiUtils.getHeight(this.buttonGuiScale);
        int x = this.buttonGuiScale.getX() + (btnWidth - btnHeight);
        int y = this.buttonGuiScale.getY() + btnHeight / 2;
        Window mw = this.minecraft.getWindow();
        GLFW.glfwSetCursorPos(mw.handle(), x * mw.getGuiScale(), y * mw.getGuiScale());
    }

    @Override
    protected void repositionElements() {
        this.init();
    }

    private void actionPerformed(GuiButtonOF button, int val) {
        if (button.active) {
            if (button.id == 200) {
                this.minecraft.options.save();
                this.minecraft.gui.setScreen(this.parentScreen);
            }

            if (button.id == 201) {
                this.minecraft.options.save();
                GuiDetailSettingsOF scr = new GuiDetailSettingsOF(this, this.options);
                this.minecraft.gui.setScreen(scr);
            }

            if (button.id == 202) {
                this.minecraft.options.save();
                GuiQualitySettingsOF scr = new GuiQualitySettingsOF(this, this.options);
                this.minecraft.gui.setScreen(scr);
            }

            if (button.id == 211) {
                this.minecraft.options.save();
                GuiAnimationSettingsOF scr = new GuiAnimationSettingsOF(this, this.options);
                this.minecraft.gui.setScreen(scr);
            }

            if (button.id == 212) {
                this.minecraft.options.save();
                GuiPerformanceSettingsOF scr = new GuiPerformanceSettingsOF(this, this.options);
                this.minecraft.gui.setScreen(scr);
            }

            if (button.id == 220) {
                this.minecraft.options.save();
                GuiQuickInfoOF scr = new GuiQuickInfoOF(this);
                this.minecraft.gui.setScreen(scr);
            }

            if (button.id == 222) {
                this.minecraft.options.save();
                GuiOtherSettingsOF scr = new GuiOtherSettingsOF(this, this.options);
                this.minecraft.gui.setScreen(scr);
            }

            if (button.id == 223) {
                this.minecraft.options.save();
                GuiVanillaSettingsOF scr = new GuiVanillaSettingsOF(this, this.options);
                this.minecraft.gui.setScreen(scr);
            }

            if (button.id == 231) {
                if (Config.isAntialiasing() || Config.isAntialiasingConfigured()) {
                    Config.showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
                    return;
                }

                if (Config.isGraphicsFabulous()) {
                    Config.showGuiMessage(Lang.get("of.message.shaders.gf1"), Lang.get("of.message.shaders.gf2"));
                    return;
                }

                this.minecraft.options.save();
                GuiShaders scr = new GuiShaders(this, this.options);
                this.minecraft.gui.setScreen(scr);
            }
        }
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
        super.removed();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
        this.renderVersion(graphicsIn);
        super.extractRenderState(graphicsIn, mouseX, mouseY, partialTicks);
        graphicsIn.centeredText(this.minecraft.font, this.title, this.width / 2, 15, -1);
        this.tooltipManager.drawTooltips(graphicsIn, mouseX, mouseY, this.buttonList);
    }

    private void renderVersion(GuiGraphicsExtractor graphicsIn) {
        String ver = Config.getVersion();
        String ed = "HD_U";
        if (ed.equals("HD")) {
            ver = "OptiFine HD K2_pre5";
        }

        if (ed.equals("HD_U")) {
            ver = "OptiFine HD K2_pre5 Ultra";
        }

        if (ed.equals("L")) {
            ver = "OptiFine K2_pre5 Light";
        }

        graphicsIn.text(this.minecraft.font, ver, 2, this.height - 10, -6250336);
        String verMc = "Minecraft 26.2";
        int lenMc = this.minecraft.font.width(verMc);
        graphicsIn.text(this.minecraft.font, verMc, this.width - lenMc - 2, this.height - 10, -6250336);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphicsIn, mouseX, mouseY, partialTicks);
    }

    public void updateFullscreenButton(boolean valueIn) {
    }

    private AbstractWidget addButton(AbstractWidget btn) {
        return this.addRenderableWidget(btn);
    }
}
