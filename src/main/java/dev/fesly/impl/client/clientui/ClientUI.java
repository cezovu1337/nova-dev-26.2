package dev.fesly.impl.client.clientui;
import com.google.gson.JsonObject;
import dev.fesly.Nova;
import dev.fesly.client.component.draggable.impl.WaterMark;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.KeyboardInputEvent;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.impl.ElasticAnimation;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.Colors;
import dev.fesly.impl.value.clientValues.*;
import net.optifine.shaders.Shaders;
import org.lwjgl.glfw.GLFW;
import java.awt.*;
import java.util.*;
import java.util.List;
import static dev.fesly.impl.interfaces.game.IMinecraft.mc;
public class ClientUI {
    private boolean customScoreBoard;
    private boolean customChat;
    private boolean Notifications = false;
    private boolean keyBinds = false;
    private boolean customHotbar;
    private boolean newTextures;
    private boolean musicPlayer;
    private boolean EnableHue;
    private ColorEnum colorEnum = ColorEnum.Exquisite_Blue;
    public static EnumSet<HudElementsEnum> hudElements = EnumSet.of(HudElementsEnum.ArrayList, HudElementsEnum.WaterMark);
    private WaterMarkModeEnum waterMarkMode = WaterMarkModeEnum.Type2;
    private BlurMode blurMode = BlurMode.Fast;
    private TextMode keyBindsMode = TextMode.LowerCase;
    public enum BlurMode {
        Fancy, Fast;
    }
    public enum TextMode {
        Default, LowerCase, UpperCase;
    }
    private boolean darkMode = true;
    private boolean blur = true;
    private boolean render3DWithShaders = false;
    private boolean reverseBlurImage = false;
    private boolean blurHand = false;
    private boolean staffList = false;
    private boolean shadow = true;
    private boolean bloomRender = true;
    private boolean outline = true;
    private boolean colorPlus = true;
    private boolean colorful = true;
    private boolean colorfulOutline = true;
    private boolean colorfulGlow = true;
    private boolean colorfulRectangles = true;
    private boolean customFog = true;
    private boolean colorfulFog = true;
    private boolean testScriptOption;
    private float colorSpeed = 35;
    private float shadowRadius = 11.0F;
    private float shadowCompress = 0.3F;
    private float shadowAlpha = 1.0F;
    private float colorfulBlurAlpha = 0.33F;
    private float blurRadius = 16;
    private float gradientRange = 10;
    private boolean customColor = false;
    private boolean noHurtCamera = true;
    private boolean thinFonts = true;
    public Color customColor2 = new Color(5867493);
    public Color customColor1 = new Color(2945023);
    private final Map<String, ClientSetting> SETTINGS;
    private final Map<ClientUICategory, List<ClientSetting>> CATEGORY_TO_SETTINGS;
    public static Animation scaleAnimationHelp = new ElasticAnimation(650, 100, 8.5F, 1, false);
    public ClientUI() {
        Map<String, ClientSetting> settings = new LinkedHashMap<>();
        settings.put("hudElements", new ClientMultiEnumSetting<>("Hud Elements:", ClientUICategory.CLIENT, ClientUI::getHudElements, ClientUI::setHudElements, HudElementsEnum.class));
        settings.put("keybindsMode", new ClientEnumSetting<>("Keybinds Mode:", ClientUICategory.CLIENT, this::getKeyBindsMode, this::setKeyBindsMode, ClientUI.TextMode.class));
        settings.put("blurMode", new ClientEnumSetting<>("Blur Mode:", ClientUICategory.COLOR, this::getBlurMode, this::setBlurMode, ClientUI.BlurMode.class));
        settings.put("notifications", new ClientBooleanSetting("Notifications", ClientUICategory.CLIENT, this::isNotifications, this::setNotifications));
        {
            settings.put("color", new ClientEnumSetting<>("Color:", ClientUICategory.COLOR, this::getColorEnum, this::setColorEnum, ClientUI.ColorEnum.class));
            settings.put("customColor", new ClientColorSetting("Color", () -> getColorEnum().equals(ColorEnum.Custom), ClientUICategory.COLOR, this::getCustomColor1, this::setCustomColor1));
            settings.put("customColor2", new ClientColorSetting("Color2", () -> getColorEnum().equals(ColorEnum.Custom), ClientUICategory.COLOR, this::getCustomColor2, this::setCustomColor2));
            settings.put("blurMode", new ClientEnumSetting<>("Blur Mode:", ClientUICategory.COLOR, this::getBlurMode, this::setBlurMode, ClientUI.BlurMode.class));
            settings.put("colorSpeed", new ClientNumberSetting("Color Speed", ClientUICategory.COLOR, this::getColorSpeed, this::setColorSpeed, "%.1f", 1, 35));
            settings.put("colorPlus", new ClientBooleanSetting("Color Plus", ClientUICategory.COLOR, this::isColorPlus, this::setColorPlus));
            settings.put("render3DWithShaders", new ClientBooleanSetting("Render 3D With Shaders", () -> Shaders.shaderPackLoaded, ClientUICategory.COLOR, this::isRender3DWithShaders, this::setRender3DWithShaders));
            settings.put("thinFonts", new ClientBooleanSetting("Thin Fonts", ClientUICategory.COLOR, this::isThinFonts, this::setThinFonts));
            settings.put("blur", new ClientBooleanSetting("Blur", ClientUICategory.COLOR, this::isBlur, this::setBlur));
            settings.put("reverseBlurImage", new ClientBooleanSetting("Reverse Blur Image", ClientUICategory.COLOR, this::isReverseBlurImage, this::setReverseBlurImage));
            settings.put("noHurtCamera", new ClientBooleanSetting("No Hurt Camera", ClientUICategory.COLOR, this::isNoHurtCamera, this::setNoHurtCamera));
            settings.put("outline", new ClientBooleanSetting("Outline", ClientUICategory.COLOR, this::isOutline, this::setOutline));
            settings.put("shadow", new ClientBooleanSetting("Glow", ClientUICategory.COLOR, this::isShadow, this::setShadow));
            settings.put("removeBloomRender", new ClientBooleanSetting("Bloom", ClientUICategory.COLOR, this::isBloomRender, this::setBloomRender));
            settings.put("shadowRadius", new ClientNumberSetting("Shadow Radius", this::isShadow, ClientUICategory.COLOR, this::getShadowRadius, this::setShadowRadius, "%.1f", 0, 20));
            settings.put("shadowCompress", new ClientNumberSetting("Shadow Compress", this::isShadow, ClientUICategory.COLOR, this::getShadowCompress, this::setShadowCompress, "%.2f", 0.15F, 1.5F));
            settings.put("shadowAlpha", new ClientNumberSetting("Shadow Alpha", this::isShadow, ClientUICategory.COLOR, this::getShadowAlpha, this::setShadowAlpha, "%.1f", 0.75F, 0.9F));
            settings.put("blurRadius", new ClientNumberSetting("Blur Radius", this::isBlur, ClientUICategory.COLOR, this::getBlurRadius, this::setBlurRadius, "%.1f", 2, 20));
            settings.put("colorful", new ClientBooleanSetting("Colorful", ClientUICategory.COLOR, this::isColorful, this::setColorful));
            settings.put("gradientRange", new ClientNumberSetting("Colorful Gradient Range", this::isColorful, ClientUICategory.COLOR, this::getGradientRange, this::setGradientRange, "%.0f", 6, 80));
            settings.put("colorfulAlpha", new ClientNumberSetting("Colorful Rect Alpha", () -> this.isColorful() && this.isBlur() && this.isColorfulRectangles(), ClientUICategory.COLOR, this::getColorfulBlurAlpha, this::setColorfulBlurAlpha, "%.1f", 0.1F, 1.0F));
            settings.put("colorfulRectangles", new ClientBooleanSetting("Colorful Rect", this::isColorful, ClientUICategory.COLOR, this::isColorfulRectangles, this::setColorfulRectangles));
            settings.put("colorfulShadows", new ClientBooleanSetting("Colorful Shadows", () -> this.isColorful() && this.isShadow(), ClientUICategory.COLOR, this::isColorfulGlow, this::setColorfulGlow));
            settings.put("colorfulOutline", new ClientBooleanSetting("Colorful Outline", () -> this.isColorful() && this.isOutline(), ClientUICategory.COLOR, this::isColorfulOutline, this::setColorfulOutline));
        }
        SETTINGS = Map.copyOf(settings);
        Map<ClientUICategory, List<ClientSetting>> categoryToSettings = new LinkedHashMap<>();
        for (ClientSetting setting : settings.values()) {
            categoryToSettings.computeIfAbsent(setting.getCategory(), k -> new ArrayList<>()).add(setting);
        }
        CATEGORY_TO_SETTINGS = Map.copyOf(categoryToSettings);
    }
    public Map<String, ClientSetting> getSettings() {
        return SETTINGS;
    }
    public List<ClientSetting> getSettings(ClientUICategory category) {
        return CATEGORY_TO_SETTINGS.get(category);
    }
    public void init() {
        Nova.getInstance().getEventBus().register(this);
    }
    public void load(JsonObject json) {
        try {
            for (Map.Entry<String, ClientSetting> en : SETTINGS.entrySet()) {
                String id = en.getKey();
                if (!json.has(id)) continue;
                en.getValue().load(json.get(id));
            }
        } catch (Exception ignored) {
        }
    }
    public JsonObject save() {
        JsonObject json = new JsonObject();
        try {
            for (Map.Entry<String, ClientSetting> en : SETTINGS.entrySet()) {
                json.add(en.getKey(), en.getValue().save());
            }
        } catch (Exception ignored) {
        }
        return json;
    }
    public enum ColorEnum {
        White, Exquisite_Blue, Exquisite_Purple, Exquisite_Orange, Exquisite_Lime, Blue_Purple, Orange_White, Astolfo, Rainbow, Custom;
    }
    public enum HudElementsEnum {
        WaterMark, ArrayList, KeyBinds, PotionHud, StaffList, CustomChat, CustomHotBar;
    }
    public enum WaterMarkModeEnum {
        Type1, Type2;
    }
    @EventLink(Priorities.VERY_HIGH)
    public final Listener<KeyboardInputEvent> onKey = evt -> {
        if (evt.getKeyCode() == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            mc.displayScreen(Nova.getInstance().getClientScreen());
        }
    };
    @java.lang.SuppressWarnings("all")
    public Map<ClientUICategory, List<ClientSetting>> getCATEGORY_TO_SETTINGS() {
        return this.CATEGORY_TO_SETTINGS;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<KeyboardInputEvent> getOnKey() {
        return this.onKey;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isCustomScoreBoard() {
        return this.customScoreBoard;
    }
    @java.lang.SuppressWarnings("all")
    public void setCustomScoreBoard(final boolean customScoreBoard) {
        this.customScoreBoard = customScoreBoard;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isCustomChat() {
        return this.customChat || (getHudElements() != null && getHudElements().contains(HudElementsEnum.CustomChat));
    }
    @java.lang.SuppressWarnings("all")
    public void setCustomChat(final boolean customChat) {
        this.customChat = customChat;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isNotifications() {
        return this.Notifications;
    }
    @java.lang.SuppressWarnings("all")
    public void setNotifications(final boolean Notifications) {
        this.Notifications = Notifications;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isKeyBinds() {
        return this.keyBinds;
    }
    @java.lang.SuppressWarnings("all")
    public void setKeyBinds(final boolean keyBinds) {
        this.keyBinds = keyBinds;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isCustomHotbar() {
        return this.customHotbar || (getHudElements() != null && getHudElements().contains(HudElementsEnum.CustomHotBar));
    }
    @java.lang.SuppressWarnings("all")
    public void setCustomHotbar(final boolean customHotbar) {
        this.customHotbar = customHotbar;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isNewTextures() {
        return this.newTextures;
    }
    @java.lang.SuppressWarnings("all")
    public void setNewTextures(final boolean newTextures) {
        this.newTextures = newTextures;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isMusicPlayer() {
        return this.musicPlayer;
    }
    @java.lang.SuppressWarnings("all")
    public void setMusicPlayer(final boolean musicPlayer) {
        this.musicPlayer = musicPlayer;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isEnableHue() {
        return this.EnableHue;
    }
    @java.lang.SuppressWarnings("all")
    public void setEnableHue(final boolean EnableHue) {
        this.EnableHue = EnableHue;
    }
    @java.lang.SuppressWarnings("all")
    public ColorEnum getColorEnum() {
        return this.colorEnum;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorEnum(final ColorEnum colorEnum) {
        this.colorEnum = colorEnum;
    }
    @java.lang.SuppressWarnings("all")
    public static EnumSet<HudElementsEnum> getHudElements() {
        return ClientUI.hudElements;
    }
    @java.lang.SuppressWarnings("all")
    public static void setHudElements(final EnumSet<HudElementsEnum> hudElements) {
        ClientUI.hudElements = hudElements;
    }
    @java.lang.SuppressWarnings("all")
    public WaterMarkModeEnum getWaterMarkMode() {
        return this.waterMarkMode;
    }
    @java.lang.SuppressWarnings("all")
    public void setWaterMarkMode(final WaterMarkModeEnum waterMarkMode) {
        this.waterMarkMode = waterMarkMode;
    }
    @java.lang.SuppressWarnings("all")
    public BlurMode getBlurMode() {
        return this.blurMode;
    }
    @java.lang.SuppressWarnings("all")
    public void setBlurMode(final BlurMode blurMode) {
        this.blurMode = blurMode;
    }
    @java.lang.SuppressWarnings("all")
    public TextMode getKeyBindsMode() {
        return this.keyBindsMode;
    }
    @java.lang.SuppressWarnings("all")
    public void setKeyBindsMode(final TextMode keyBindsMode) {
        this.keyBindsMode = keyBindsMode;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isDarkMode() {
        return this.darkMode;
    }
    @java.lang.SuppressWarnings("all")
    public void setDarkMode(final boolean darkMode) {
        this.darkMode = darkMode;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isBlur() {
        return this.blur;
    }
    @java.lang.SuppressWarnings("all")
    public void setBlur(final boolean blur) {
        this.blur = blur;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isRender3DWithShaders() {
        return this.render3DWithShaders;
    }
    @java.lang.SuppressWarnings("all")
    public void setRender3DWithShaders(final boolean render3DWithShaders) {
        this.render3DWithShaders = render3DWithShaders;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isReverseBlurImage() {
        return this.reverseBlurImage;
    }
    @java.lang.SuppressWarnings("all")
    public void setReverseBlurImage(final boolean reverseBlurImage) {
        this.reverseBlurImage = reverseBlurImage;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isBlurHand() {
        return this.blurHand;
    }
    @java.lang.SuppressWarnings("all")
    public void setBlurHand(final boolean blurHand) {
        this.blurHand = blurHand;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isStaffList() {
        return this.staffList;
    }
    @java.lang.SuppressWarnings("all")
    public void setStaffList(final boolean staffList) {
        this.staffList = staffList;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isShadow() {
        return this.shadow;
    }
    @java.lang.SuppressWarnings("all")
    public void setShadow(final boolean shadow) {
        this.shadow = shadow;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isBloomRender() {
        return this.bloomRender;
    }
    @java.lang.SuppressWarnings("all")
    public void setBloomRender(final boolean bloomRender) {
        this.bloomRender = bloomRender;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isOutline() {
        return this.outline;
    }
    @java.lang.SuppressWarnings("all")
    public void setOutline(final boolean outline) {
        this.outline = outline;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isColorPlus() {
        return this.colorPlus;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorPlus(final boolean colorPlus) {
        this.colorPlus = colorPlus;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isColorful() {
        return this.colorful;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorful(final boolean colorful) {
        this.colorful = colorful;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isColorfulOutline() {
        return this.colorfulOutline;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorfulOutline(final boolean colorfulOutline) {
        this.colorfulOutline = colorfulOutline;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isColorfulGlow() {
        return this.colorfulGlow;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorfulGlow(final boolean colorfulGlow) {
        this.colorfulGlow = colorfulGlow;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isColorfulRectangles() {
        return this.colorfulRectangles;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorfulRectangles(final boolean colorfulRectangles) {
        this.colorfulRectangles = colorfulRectangles;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isCustomFog() {
        return this.customFog;
    }
    @java.lang.SuppressWarnings("all")
    public void setCustomFog(final boolean customFog) {
        this.customFog = customFog;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isColorfulFog() {
        return this.colorfulFog;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorfulFog(final boolean colorfulFog) {
        this.colorfulFog = colorfulFog;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isTestScriptOption() {
        return this.testScriptOption;
    }
    @java.lang.SuppressWarnings("all")
    public void setTestScriptOption(final boolean testScriptOption) {
        this.testScriptOption = testScriptOption;
    }
    @java.lang.SuppressWarnings("all")
    public float getColorSpeed() {
        return this.colorSpeed;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorSpeed(final float colorSpeed) {
        this.colorSpeed = colorSpeed;
    }
    @java.lang.SuppressWarnings("all")
    public float getShadowRadius() {
        return this.shadowRadius;
    }
    @java.lang.SuppressWarnings("all")
    public void setShadowRadius(final float shadowRadius) {
        this.shadowRadius = shadowRadius;
    }
    @java.lang.SuppressWarnings("all")
    public float getShadowCompress() {
        return this.shadowCompress;
    }
    @java.lang.SuppressWarnings("all")
    public void setShadowCompress(final float shadowCompress) {
        this.shadowCompress = shadowCompress;
    }
    @java.lang.SuppressWarnings("all")
    public float getShadowAlpha() {
        return this.shadowAlpha;
    }
    @java.lang.SuppressWarnings("all")
    public void setShadowAlpha(final float shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
    }
    @java.lang.SuppressWarnings("all")
    public float getColorfulBlurAlpha() {
        return this.colorfulBlurAlpha;
    }
    @java.lang.SuppressWarnings("all")
    public void setColorfulBlurAlpha(final float colorfulBlurAlpha) {
        this.colorfulBlurAlpha = colorfulBlurAlpha;
    }
    @java.lang.SuppressWarnings("all")
    public float getBlurRadius() {
        return this.blurRadius;
    }
    @java.lang.SuppressWarnings("all")
    public void setBlurRadius(final float blurRadius) {
        this.blurRadius = blurRadius;
    }
    @java.lang.SuppressWarnings("all")
    public float getGradientRange() {
        return this.gradientRange;
    }
    @java.lang.SuppressWarnings("all")
    public void setGradientRange(final float gradientRange) {
        this.gradientRange = gradientRange;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isCustomColor() {
        return this.customColor;
    }
    @java.lang.SuppressWarnings("all")
    public void setCustomColor(final boolean customColor) {
        this.customColor = customColor;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isNoHurtCamera() {
        return this.noHurtCamera;
    }
    @java.lang.SuppressWarnings("all")
    public void setNoHurtCamera(final boolean noHurtCamera) {
        this.noHurtCamera = noHurtCamera;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isThinFonts() {
        return this.thinFonts;
    }
    @java.lang.SuppressWarnings("all")
    public void setThinFonts(final boolean thinFonts) {
        this.thinFonts = thinFonts;
    }
    @java.lang.SuppressWarnings("all")
    public Color getCustomColor2() {
        return this.customColor2;
    }
    @java.lang.SuppressWarnings("all")
    public void setCustomColor2(final Color customColor2) {
        this.customColor2 = customColor2;
    }
    @java.lang.SuppressWarnings("all")
    public Color getCustomColor1() {
        return this.customColor1;
    }
    @java.lang.SuppressWarnings("all")
    public void setCustomColor1(final Color customColor1) {
        this.customColor1 = customColor1;
    }
}
