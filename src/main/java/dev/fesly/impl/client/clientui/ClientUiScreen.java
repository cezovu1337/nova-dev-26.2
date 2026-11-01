package dev.fesly.impl.client.clientui;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.Nova;
import dev.fesly.client.theme.Themes;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.client.screen.mainmenu.AltManager;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.ElasticAnimation;
import dev.fesly.impl.util.chat.ChatUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.clientValues.ClientUICategory;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
public class ClientUiScreen extends Screen implements IAccess {
    public ClientUiScreen() {
        super(NarratorChatListener.EMPTY);
    }
    public static int alphaAnim;
    private final float STANDART_X_OFFSET = 0.304f;
    private final float STANDART_Y_OFFSET = 0.278f;
    public float x = -1;
    public float y = -1;
    public float uiWidth = -1;
    public float uiHeight = -1;
    @Override
    public void init() {
        super.init();
        uiWidth = 752 * 0.5f;
        uiHeight = 478 * 0.5f;
        x = width / 2f - uiWidth / 2;
        y = height / 2f - uiHeight / 2;
        alphaAnim = 65;
        ClientUI.scaleAnimationHelp.reset();
        ClientUI.scaleAnimationHelp.setDirection(Direction.FORWARDS);
        int x = (int) (this.x + 20);
        float y = this.y + uiHeight / 6f;
        buttons.clear();
        for (int i = 0; i < ClientUICategory.values().length; i++) {
            buttons.add(new ClientCategoryWidget(ClientUICategory.values()[i], x + (i * (60 + ClientCategoryWidget.WIDTH)), (int) y));
        }
    }
    private int xOfMouse = -1;
    private int yOfMouse = -1;
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.xOfMouse = mouseX;
        this.yOfMouse = mouseY;
        alphaAnim = (int) MathUtil.lerp(alphaAnim, 253, 0.2f);
        drawUI(mouseX, mouseY, partialTicks,-400 + ClientUI.scaleAnimationHelp.getOutput().floatValue() * 4);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Widget widget : buttons) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Widget widget : buttons) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (Widget widget : buttons) {
            if (widget.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (Widget widget : buttons) {
            if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }
    public static Runnable background = null;
    public final Animation animation = new ElasticAnimation(350, 1, 15, 1, false, Direction.BACKWARDS);
    public static void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline, int alpha) {
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color c1 = colorEnum.getBackgroundColor();
        Color c2 = colorEnum.getBackgroundColor2();
        Color color1 = Color.WHITE;
        Color color2 = Color.WHITE;
        if (alpha == 365) {
            color1 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 255);
            color2 = new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 255);
        } else {
            color1 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int) (163 * (alpha / 255f)));
            color2 = new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int) (107 * (alpha / 255f)));
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius
                , color1, color1
                , color2, color2);
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.4f
                    , color1.brighter().brighter(), color1.brighter().brighter()
                    , color2.brighter().brighter(), color2.brighter().brighter());
        }
    }
    private int clipboardMode = -1;
    private String successPasted = "";
    private TimerUtil timerUtil = new TimerUtil();
    public void drawUI(int mouseX, int mouseY, float partialTicks, float yTranslate) {
        float y = this.y + yTranslate;
        if (alphaAnim < 42) alphaAnim = 253;
        background = (() -> {
            drawWithOutlineDefault(x + 1, y + 1, uiWidth - 2, uiHeight - 2, 9f, false, 365);
        });
        RenderUtil.applyBloom(3, 1, () -> drawWithOutlineDefault(x, y, uiWidth - 0.5f, uiHeight - 1, 8.5f, false, 365));
        drawWithOutlineDefault(x, y, uiWidth, uiHeight, 9, true, alphaAnim - 40);
        Runnable image = () -> {
            RenderUtil.drawImage(new ResourceLocation("nova", "icons/nova64px.png"), (int) (x + uiWidth / 2f - 12), (int) (y + 5), 32, 32, new Color(255, 255, 255, alphaAnim - 60).getRGB());
        };
        image.run();
        if (timerUtil.hasReached(20000)) {
            timerUtil.reset();
            successPasted = "";
        }
        String s = mc.keyboardListener.getClipboardString();
        boolean paste = s.contains("gradientRange") && s.contains("shadowAlpha");
        String clipboardString = successPasted;
        Font font = Fonts.ROBOTO_MEDIUM.get(17);
        font.drawCenterX(clipboardString, x + uiWidth / 2f, y + uiHeight - 5 - font.getHeight(), -1);
        clipboardMode = paste ? 1 : 0;
        MatrixStack stack = new MatrixStack();
        for (Widget widget : buttons) {
            if (!(widget instanceof ClientCategoryWidget)) continue;
            ((ClientCategoryWidget) widget).renderWidget(stack,mouseX, mouseY, partialTicks,yTranslate);
        }
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : buttons) {
            widget.keyPressed(keyCode, scanCode, modifiers);
        }
        String clipboard = mc.keyboardListener.getClipboardString();
        boolean isControlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
        if (keyName != null) {
            if ((keyName.equalsIgnoreCase("v") || keyName.equalsIgnoreCase("м"))
                    && isControlPressed) {
                try {
                    JsonObject jsonObject = (JsonObject) JsonParser.parseString(clipboard);
                    if (!clipboard.contains("draggables")) {
                        Nova.getInstance().getClientUI().load(jsonObject);
                        return false;
                    }
                    if (jsonObject == null) {
                        return false;
                    }
                    if (jsonObject.has("theme")) {
                        Optional<Themes> theme = Arrays.stream(Themes.values())
                                .filter(x -> x.name().equalsIgnoreCase(jsonObject.get("theme").getAsString()))
                                .findFirst();
                        theme.ifPresent(configTheme -> Nova.getInstance().getThemeManager().setTheme(configTheme));
                    }
                    Nova.getInstance().getDraggableManager().load(jsonObject.getAsJsonObject("draggables"));
                    Nova.getInstance().getModuleManager().load(jsonObject.getAsJsonObject("modules"));
                    Nova.getInstance().getClientUI().load(jsonObject.getAsJsonObject("clientUi"));
                    successPasted = "Успешно вставлены настройки!";
                } catch (Exception ex) {
                    try {
                        Nova.getInstance().getClientUI().load((JsonObject) JsonParser.parseString(clipboard));
                        successPasted = "Успешно вставлены настройки";
                    }catch (Exception FATAL) {
                        successPasted = "Фатальная ошибка. Перезайдите в гуи и попробуйте снова.";
                        ChatUtil.addText("Фатальная ошибка. Перезайдите в гуи и попробуйте снова.");
                    }
                }
            }
            if ((keyName.equalsIgnoreCase("c") || keyName.equalsIgnoreCase("с"))
                    && isControlPressed && !isShiftPressed) {
                try {
                    mc.keyboardListener.setClipboardString(nova.getClientUI().save().toString());
                    successPasted = "Успешно скопированы настройки!";
                } catch (Exception ex) {
                    successPasted = "Успешно скопированы настройки!";
                }
            } else if ((keyName.equalsIgnoreCase("c") || keyName.equalsIgnoreCase("с"))
                    && isControlPressed) {
                try {
                    final JsonObject jsonObject = new JsonObject();
                    final JsonObject metadataJsonObject = new JsonObject();
                    metadataJsonObject.addProperty("version", Nova.getInstance().getClientInfo().getVersion());
                    jsonObject.add("Metadata", metadataJsonObject);
                    jsonObject.add("draggables", Nova.getInstance().getDraggableManager().save());
                    jsonObject.add("clientUi", Nova.getInstance().getClientUI().save());
                    jsonObject.add("modules", Nova.getInstance().getModuleManager().save());
                    jsonObject.addProperty("theme", Nova.getInstance().getThemeManager().getTheme().name());
                    mc.keyboardListener.setClipboardString(jsonObject.toString());
                } catch (Exception ex) {
                }
            }
        }
        if (keyCode == 256) {
            this.closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
