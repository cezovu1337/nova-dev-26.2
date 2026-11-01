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
import lombok.Getter;
import lombok.Setter;
import net.optifine.shaders.Shaders;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.List;

import static dev.fesly.impl.interfaces.game.IMinecraft.mc;

@Getter()
@Setter
public class ClientUI {
    @Getter
    @Setter
    private boolean customScoreBoard;
    @Getter
    @Setter
    private boolean customChat;
    @Getter
    @Setter
    private boolean Notifications = false;
    @Getter
    @Setter
    private boolean keyBinds = false;
    @Getter
    @Setter
    private boolean customHotbar;
    @Getter
    @Setter
    private boolean newTextures;

    @Getter
    @Setter
    private boolean musicPlayer;
    @Getter
    @Setter
    private boolean EnableHue;
    @Getter
    @Setter
    private ColorEnum colorEnum = ColorEnum.Exquisite_Blue;
    @Getter
    @Setter
    public static EnumSet<HudElementsEnum> hudElements = EnumSet.of(HudElementsEnum.ArrayList,HudElementsEnum.WaterMark);

    @Getter
    @Setter
    private WaterMarkModeEnum waterMarkMode = WaterMarkModeEnum.Type2;

    @Getter
    @Setter
    private BlurMode blurMode = BlurMode.Fast;

    @Getter
    @Setter
    private TextMode keyBindsMode = TextMode.LowerCase;

    public enum BlurMode {
        Fancy,Fast;
    }

    public enum TextMode {
        Default,LowerCase,UpperCase
    }

    @Getter
    @Setter
    private boolean darkMode = true;
    @Getter
    @Setter
    private boolean blur = true;
    @Getter
    @Setter
    private boolean render3DWithShaders = false;


    @Getter
    @Setter
    private boolean reverseBlurImage = false;

    @Getter
    @Setter
    private boolean blurHand = false;
    @Getter
    @Setter
    private boolean staffList = false;

    @Getter
    @Setter
    private boolean shadow = true;
    @Getter
    @Setter
    private boolean bloomRender = true;

    @Getter
    @Setter
    private boolean outline = true;
    @Getter
    @Setter
    private boolean colorPlus = true;



    @Getter
    @Setter
    private boolean colorful = true;

    @Getter
    @Setter
    private boolean colorfulOutline = true;
    @Getter
    @Setter
    private boolean colorfulGlow = true;



    @Getter
    @Setter
    private boolean colorfulRectangles = true;
    @Getter
    @Setter
    private boolean customFog = true;
    @Getter
    @Setter
    private boolean colorfulFog = true;


    @Getter
    @Setter
    private boolean testScriptOption;
    @Getter
    @Setter
    private float colorSpeed = 35;
    @Getter
    @Setter
    private float shadowRadius = 11.0F;
    @Getter
    @Setter
    private float shadowCompress = 0.3F;

    @Getter
    @Setter
    private float shadowAlpha = 1F;
    @Getter
    @Setter
    private float colorfulBlurAlpha = 0.33f;
    @Getter
    @Setter
    private float blurRadius = 16;
    @Getter
    @Setter
    private float gradientRange = 10;


    @Getter
    @Setter
    private boolean customColor = false;

    @Getter
    @Setter
    private boolean noHurtCamera = true;

    @Getter
    @Setter
    private boolean thinFonts = true;

    @Getter @Setter
    public Color customColor2 = new Color(0x5987E5);
    @Getter
    @Setter
    public Color customColor1 = new Color(0x2CEFFF);

    private final Map<String, ClientSetting> SETTINGS;
    private final Map<ClientUICategory, List<ClientSetting>> CATEGORY_TO_SETTINGS;

    public static Animation scaleAnimationHelp = new ElasticAnimation(650, 100, 8.5f, 1, false);




    public ClientUI() {
        Map<String, ClientSetting> settings = new LinkedHashMap<>();
        // Client Settings

        settings.put("hudElements", new ClientMultiEnumSetting<>("Hud Elements:", ClientUICategory.CLIENT, ClientUI::getHudElements, ClientUI::setHudElements, HudElementsEnum.class));
        settings.put("keybindsMode", new ClientEnumSetting<>("Keybinds Mode:", ClientUICategory.CLIENT, this::getKeyBindsMode, this::setKeyBindsMode, ClientUI.TextMode.class));

        settings.put("blurMode", new ClientEnumSetting<>("Blur Mode:", ClientUICategory.COLOR, this::getBlurMode, this::setBlurMode, ClientUI.BlurMode.class));

        //settings.put("waterMarkMode", new ClientEnumSetting<>("WaterMark Mode",() -> getHudElements().contains(HudElementsEnum.WaterMark), ClientUICategory.CLIENT, this::getWaterMarkMode, this::setWaterMarkMode, ClientUI.WaterMarkModeEnum.class));

        //settings.put("musicPlayer", new ClientBooleanSetting("Music player", ClientUICategory.CLIENT, this::isMusicPlayer, this::setMusicPlayer));
        //settings.put("customScoreboard", new ClientBooleanSetting("Custom Scoreboard", ClientUICategory.CLIENT, this::isCustomScoreBoard, this::setCustomScoreBoard));
       // settings.put("customChat", new ClientBooleanSetting("Custom Chat", ClientUICategory.CLIENT, this::isCustomChat, this::setCustomChat));
        settings.put("notifications", new ClientBooleanSetting("Notifications", ClientUICategory.CLIENT, this::isNotifications, this::setNotifications));

        // Color Settings
        {
            settings.put("color", new ClientEnumSetting<>("Color:", ClientUICategory.COLOR, this::getColorEnum, this::setColorEnum, ClientUI.ColorEnum.class));
            settings.put("customColor", new ClientColorSetting("Color",() -> getColorEnum().equals(ColorEnum.Custom), ClientUICategory.COLOR, this::getCustomColor1, this::setCustomColor1));
            settings.put("customColor2", new ClientColorSetting("Color2",() -> getColorEnum().equals(ColorEnum.Custom), ClientUICategory.COLOR, this::getCustomColor2, this::setCustomColor2));

            settings.put("blurMode", new ClientEnumSetting<>("Blur Mode:", ClientUICategory.COLOR, this::getBlurMode, this::setBlurMode, ClientUI.BlurMode.class));

            settings.put("colorSpeed", new ClientNumberSetting("Color Speed", ClientUICategory.COLOR, this::getColorSpeed, this::setColorSpeed, "%.1f", 1, 35));
            settings.put("colorPlus", new ClientBooleanSetting("Color Plus", ClientUICategory.COLOR, this::isColorPlus, this::setColorPlus));

            settings.put("render3DWithShaders", new ClientBooleanSetting("Render 3D With Shaders",() -> Shaders.shaderPackLoaded, ClientUICategory.COLOR, this::isRender3DWithShaders, this::setRender3DWithShaders));

            settings.put("thinFonts", new ClientBooleanSetting("Thin Fonts", ClientUICategory.COLOR, this::isThinFonts, this::setThinFonts));

            settings.put("blur", new ClientBooleanSetting("Blur", ClientUICategory.COLOR, this::isBlur, this::setBlur));

            settings.put("reverseBlurImage", new ClientBooleanSetting("Reverse Blur Image", ClientUICategory.COLOR, this::isReverseBlurImage, this::setReverseBlurImage));
            settings.put("noHurtCamera", new ClientBooleanSetting("No Hurt Camera", ClientUICategory.COLOR, this::isNoHurtCamera, this::setNoHurtCamera));

           // settings.put("blurHand", new ClientBooleanSetting("Blur Hand", ClientUICategory.COLOR, this::isBlurHand, this::setBlurHand));

            //settings.put("darkMode", new ClientBooleanSetting("Dark Mode", ClientUICategory.COLOR, this::isDarkMode, this::setDarkMode));
            //settings.put("toasterMode", new ClientBooleanSetting("Toaster Mode", ClientUICategory.COLOR, this::isT, this::setDarkMode));

            settings.put("outline", new ClientBooleanSetting("Outline", ClientUICategory.COLOR, this::isOutline, this::setOutline));

            settings.put("shadow", new ClientBooleanSetting("Glow", ClientUICategory.COLOR, this::isShadow, this::setShadow));

            settings.put("removeBloomRender", new ClientBooleanSetting("Bloom", ClientUICategory.COLOR, this::isBloomRender, this::setBloomRender));

            settings.put("shadowRadius", new ClientNumberSetting("Shadow Radius", this::isShadow, ClientUICategory.COLOR, this::getShadowRadius, this::setShadowRadius, "%.1f", 0, 20));
            settings.put("shadowCompress", new ClientNumberSetting("Shadow Compress", this::isShadow, ClientUICategory.COLOR, this::getShadowCompress, this::setShadowCompress, "%.2f", 0.15f, 1.5f));

            settings.put("shadowAlpha", new ClientNumberSetting("Shadow Alpha", this::isShadow, ClientUICategory.COLOR, this::getShadowAlpha, this::setShadowAlpha, "%.1f", 0.75f, 0.9f));
            settings.put("blurRadius", new ClientNumberSetting("Blur Radius", this::isBlur, ClientUICategory.COLOR, this::getBlurRadius, this::setBlurRadius, "%.1f", 2, 20));

            settings.put("colorful", new ClientBooleanSetting("Colorful", ClientUICategory.COLOR, this::isColorful, this::setColorful));
            settings.put("gradientRange", new ClientNumberSetting("Colorful Gradient Range", this::isColorful, ClientUICategory.COLOR, this::getGradientRange, this::setGradientRange, "%.0f", 6, 80));
            settings.put("colorfulAlpha", new ClientNumberSetting("Colorful Rect Alpha", () -> this.isColorful() && this.isBlur() && this.isColorfulRectangles(), ClientUICategory.COLOR, this::getColorfulBlurAlpha, this::setColorfulBlurAlpha, "%.1f", 0.1f, 1.0f));

            settings.put("colorfulRectangles", new ClientBooleanSetting("Colorful Rect",
                    this::isColorful,
                    ClientUICategory.COLOR, this::isColorfulRectangles, this::setColorfulRectangles));

            settings.put("colorfulShadows", new ClientBooleanSetting("Colorful Shadows",() -> this.isColorful() && this.isShadow(),  ClientUICategory.COLOR, this::isColorfulGlow, this::setColorfulGlow));

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
        } catch (Exception ignored) {}
    }

    public JsonObject save() {
        JsonObject json = new JsonObject();
        try {
            for (Map.Entry<String, ClientSetting> en : SETTINGS.entrySet()) {
                json.add(en.getKey(), en.getValue().save());
            }
        } catch (Exception ignored) {}
        return json;
    }
    public enum ColorEnum {
        White,
        Exquisite_Blue,
        Exquisite_Purple,
        Exquisite_Orange,
        Exquisite_Lime,
        Blue_Purple,Orange_White,Astolfo,
        Rainbow,
        Custom
    }

    public enum HudElementsEnum {
        WaterMark,
        ArrayList,
        KeyBinds,
        PotionHud,
        StaffList,
       CustomChat,
        CustomHotBar
    }

    public enum WaterMarkModeEnum {
        Type1,
        Type2
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<KeyboardInputEvent> onKey = evt -> {
        if (evt.getKeyCode() == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            mc.displayScreen(Nova.getInstance().getClientScreen());
        }
    };
}
