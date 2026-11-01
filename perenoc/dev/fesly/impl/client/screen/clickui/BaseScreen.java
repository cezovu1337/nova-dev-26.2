package dev.fesly.impl.client.screen.clickui;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.notification.Notification;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.client.notification.NotificationType;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientCategoryWidget;
import dev.fesly.impl.client.clientui.ClientUiScreen;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.client.screen.mainmenu.AltManager;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.ElasticAnimation;
import dev.fesly.impl.util.chat.ChatUtil;
import dev.fesly.impl.util.color.Colors;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.other.KeyUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.value.clientValues.ClientUICategory;
import dev.fesly.impl.value.secondValues.NovaTextField;
import dev.fesly.impl.value.secondValues.Setting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.glScissor;

public class BaseScreen extends Screen implements IAccess {
    public java.util.List<Module> selectedModules = new ArrayList<>();
    public Category selected = Category.COMBAT;
    public Runnable background;
    public static Animation scaleAnimation = new ElasticAnimation(300, 1, 12, 1, false);
    public static Animation scaleAnimationHelp = new ElasticAnimation(650, 100, 22, 1, false);

    public NovaTextField textField = new NovaTextField(Fonts.ROBOTO_REGULAR.get(26),true);

    //public static Animation plusAnimation = new ElasticAnimation(200,200,25,1,false,Direction.FORWARDS);


    public int GUI_WIDTH = 270;
    public final int GUI_HEIGHT = 160;
    public static final int DRAG_WIDTH = 96;
    public static final int DRAG_HEIGHT = 25;

    private float widthGui;
    private float heightGui;

    public int x = -1;
    public int y = -1;
    public int mouseHoldX = -1;
    public int mouseHoldY = -1;

    public boolean exit;


    public static Color one = Color.WHITE;
    public static Color two = new Color(255, 255, 255, 0);

    private final float sizer = 1.33333333333333333333f;

    public static java.util.List<Module> getOpenedModules(java.util.List<Module> modules) {
        return modules.stream().filter(Module::isOpened).collect(Collectors.toList());
    }

    private AltManager altManager = new AltManager();

    public BaseScreen() {
        super(NarratorChatListener.EMPTY);
        exit = false;
    }

    public int getGuiWidth() {
        widthGui = 754f * 0.5f;
        return (int) ((int) widthGui);
    }

    public int getGuiHeight() {
        heightGui = 478f * 0.5f;
        return (int) ((int) heightGui);
    }

    private boolean openUi = false;

    @Override
    public void init() {
        exit = false;
        openUi = false;
        altManager.animation.setDirection(true);

        if (x == -1 || y == -1) { // initialize variables
            x = width / 2 - getGuiWidth() / 2;
            y = height / 2 - getGuiHeight() / 2;
        }

        scaleAnimation.setDirection(Direction.FORWARDS);
        scaleAnimationHelp.setDirection(Direction.FORWARDS);

    }

    public void holdVariables() {
        if (x == -1 || y == -1) { // initialize variables
            x = width / 2 - getGuiWidth() / 2;
            y = height / 2 - getGuiHeight() / 2;
        }
        float xr = x - getGuiWidth() / 2f;
        /*This prevents you from going off screen when dragging.*/
        x = MathHelper.clamp(x, 0, width - getGuiWidth());
        y = MathHelper.clamp(y, 0, height - getGuiHeight());
        /*==============================================*/
    }

    boolean isSearching = false;

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        altManager.keyPressed(keyCode,scanCode,modifiers);

        boolean isControlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean cf = isControlPressed && keyCode == GLFW.GLFW_KEY_F;

        if (isSearching) {
            textField.keyPressed(keyCode,scanCode,modifiers);
        }

        if (cf) {
            isSearching = !isSearching;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) isSearching = false;
        textField.dragged = isSearching;

        for (Module selectedModule : selectedModules) {
            for (Map.Entry<String, Setting> setting : selectedModule.getSettings().entrySet()) {
                if (!setting.getValue().getVisible().getAsBoolean()) continue;
                setting.getValue().keyPressed(keyCode,scanCode,modifiers);
            }
        }
        if (keyCode == 256) {

            if (isSearching) {
                isSearching = false;
                textField.absolutlyNotUsable = "";
                textField.charArray.clear();
            } else {
                boolean binding = false;
                for (Module module : Nova.getInstance().getModuleManager().get(selected)) {
                    if (module.isBinding()) {
                        module.setKeyCode(0);

                        if (!binding) {
                            binding = module.isBinding();
                        }
                        module.setBinding(false);
                    }
                }
                if (!binding) {
                    exit = true;
                    scaleAnimation.setDirection(Direction.BACKWARDS);
                    scaleAnimationHelp.setDirection(Direction.FORWARDS);

                }
            }

        }

        String clipboard = mc.keyboardListener.getClipboardString();

        //boolean isControlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
        if (keyName != null) {

            if ((keyName.equalsIgnoreCase("v") || keyName.equalsIgnoreCase("м"))
                    && isControlPressed) {
                try {
                    if (!clipboard.contains("draggables")) {
                        try {
                            nova.getModuleManager().load((JsonObject) JsonParser.parseString(clipboard));
                        }catch (Exception FATAL) {
                            ChatUtil.addText("Фатальная ошибка. Перезайдите в гуи и попробуйте снова.");
                        }
                        return false;
                    }

                    JsonObject jsonObject = (JsonObject) JsonParser.parseString(clipboard);
                    // checks if there was data read
                    if (jsonObject == null) {
                        return false;
                    }

                    Nova.getInstance().getDraggableManager().load(jsonObject.getAsJsonObject("draggables"));
                    Nova.getInstance().getModuleManager().load(jsonObject.getAsJsonObject("modules"));
                    Nova.getInstance().getClientUI().load(jsonObject.getAsJsonObject("clientUi"));

                    //nova.getModuleManager().load((JsonObject) JsonParser.parseString(clipboard));
                } catch (Exception ex) {
                    try {
                        nova.getModuleManager().load((JsonObject) JsonParser.parseString(clipboard));
                    }catch (Exception FATAL) {
                        ChatUtil.addText("Фатальная ошибка. Перезайдите в гуи и попробуйте снова.");
                    }
                }


            }
            if ((keyName.equalsIgnoreCase("c") || keyName.equalsIgnoreCase("с"))
                    && isControlPressed && !isShiftPressed) {

                try {
                    mc.keyboardListener.setClipboardString(nova.getModuleManager().save().toString());
                } catch (Exception ex) {
                }
            } else if ((keyName.equalsIgnoreCase("c") || keyName.equalsIgnoreCase("с"))
                    && isControlPressed) {
                try {
                    // creates a new json object where all data is stored in
                    final JsonObject jsonObject = new JsonObject();

                    // Add some extra information to the config
                    final JsonObject metadataJsonObject = new JsonObject();
                    metadataJsonObject.addProperty("version", Nova.getInstance().getClientInfo().getVersion());
                    jsonObject.add("Metadata", metadataJsonObject);

                    jsonObject.add("draggables", Nova.getInstance().getDraggableManager().save());

                    jsonObject.add("clientUi", Nova.getInstance().getClientUI().save());

                    jsonObject.add("modules", Nova.getInstance().getModuleManager().save());

                    mc.keyboardListener.setClipboardString(jsonObject.toString());
                } catch (Exception ex) {
                }
            }
        }

        if (keyCode == 256 && this.shouldCloseOnEsc() && scaleAnimation.getDirection().backwards() && scaleAnimation.getOutput().floatValue() < 0.1f) {
            this.closeScreen();
            return true;
        }
        return false;
    }



    @Override
    public void onClose() {
        super.onClose();
        altManager.animation.setDirection(false);
        scaleAnimation.setDirection(Direction.BACKWARDS);
        scaleAnimationHelp.setDirection(Direction.FORWARDS);

        NotificationManager.sendNotification(new Notification("ClickGUI Закрыто!", NotificationType.INFO));
    }



    private float scrollBar = 0;
    private float ScrollBar = 0;
    private float modulesScrollBar = 0;
    private float scrollModules = 0;


    private int categoryBar;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        //altManager.draw(mouseX,mouseY);
        background = () -> drawMethod(mouseX, mouseY, partialTicks, true,false);

        textField.defaultRenderText((int) (mw.getScaledWidth() / 2f), (int) (mw.getScaledHeight() - mw.getScaledHeight() / 6f),
                mouseX,mouseY,partialTicks,scaleAnimationHelp.getOutput().floatValue() / 100);
        //ShaderRender.drawBlur((float) 15f,() -> drawMethod(mouseX,mouseY,partialTicks,true));
        if (openUi) {
            ClientUiScreen.alphaAnim = 253;
            Nova.getInstance().getClientScreen().drawUI(
                    mouseX, mouseY, partialTicks, -400 + scaleAnimationHelp.getOutput().floatValue() * 4);
        }
        StencilUtil.initStencil();
        drawMethod(mouseX, mouseY, partialTicks, true,false);
        StencilUtil.bindReadStencilBuffer(2);
        RenderUtil.applyBloom(3, 5, () -> drawMethod(mouseX, mouseY, partialTicks, true,true));
        StencilUtil.uninitStencilBuffer();
        drawMethod(mouseX, mouseY, partialTicks, false,false);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
//    public ScrollUtil scroll = new ScrollUtil();
//    public ScrollUtil scrollBarB = new ScrollUtil();

    private final dev.fesly.impl.util.animation.compact.Animation
            animationS = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 200);


    public void drawMethod(int mouseX, int mouseY, float partialTicks, boolean shaders,boolean bloom) {

        UIColors colorEnum = UIColorUtil.getColorEnum();
        boolean hoverSett = MathUtil.isHovered((float) (5), mw.getScaledHeight() - 25, 20,20,mouseX,mouseY);
        animationS.run(hoverSett ? -0.2f : 0);
        float hoverSettVal = scaleAnimation.getOutput().floatValue() + animationS.getNumberValue().floatValue();
        hoverSettVal  = hoverSettVal > 1 ? 1 : hoverSettVal < 0 ? 0 : hoverSettVal;
        ClientUiScreen.drawWithOutlineDefault((float) (5),
                mw.getScaledHeight() - 25, 20,20,10, true,
                (int) (hoverSettVal * 254)
        );
        RenderUtil.drawImage(new ResourceLocation("nova","icons/ui/settings28.png"),
                (float) (8),
                mw.getScaledHeight() - 22, 14,14,new Color(255,255,255,(int)((hoverSettVal * 254))).getRGB());
        if (mouseHoldX != -1 && mouseHoldY != -1) {
            x = mouseX - mouseHoldX;
            y = mouseY - mouseHoldY;
            holdVariables();
        }



        if (!openUi) {
            if (scaleAnimation.getDirection() == Direction.BACKWARDS && scaleAnimation.getOutput() < scaleAnimation.getEndPoint() / 5 && exit) {
                if (scaleAnimation.getOutput() < 0.1f) {
                    closeScreen();
                }
            }
        }else {
            if (scaleAnimation.getDirection() == Direction.BACKWARDS) {

                closeScreen();

                mc.displayScreen(Nova.getInstance().getClientScreen());
                ClientUiScreen.alphaAnim = 253;
            }
        }


        if (shaders) {
            startScale();
            Color c = colorEnum.getBackgroundColor();
            Color c2 = colorEnum.getBackgroundColor2();
            Color color1 = new Color(c.getRed(), c.getGreen(),c.getBlue(), 255);
            Color color2 = new Color(c2.getRed(), c2.getGreen(),c2.getBlue(),255);
            RenderUtil.roundedGradientRectangle(x + 0.5f, y + 0.5f, widthGui - 1, heightGui - 1 - (bloom ? 2 :0), 5.2f
                    , color1, color1
                    , color2, color1);
            endScale();
        } else {
            startScale();
            Color color1 = colorEnum.getBackgroundColor();
            Color color2 = colorEnum.getBackgroundColor2();
            Color color1N = colorEnum.getColor1N();
            Color color2N = colorEnum.getColor2N();

            RenderUtil.roundedGradientRectangle(x + 0.5f, y + 0.5f, widthGui - 1, heightGui - 1, 5.2f
                    , color1, color1
                    , color2, color1);
            RenderUtil.roundedOutlineGradientRectangle(x, y, widthGui, heightGui, 5.2f, 0.7f
                    , new Color(1, 1, 1, 0)
                    , color1.brighter(), color1.brighter()
                    , color2.brighter(), color1.brighter());


            float leftBarFromWidth = 0.12068f;
            float leftBarWidth = (widthGui - 1) * leftBarFromWidth;
            //TODO:  Draw leftBar
            {
                StencilUtil.initStencil();
                RenderUtil.roundedRectangle(x + 0.5f, y + 0.5f, leftBarWidth, heightGui - 1, 0, Color.BLACK);
                StencilUtil.bindReadStencilBuffer(1);
                RenderUtil.roundedGradientRectangle(x + 0.5f, y + 0.5f, widthGui - 1, heightGui - 1, 5.2f
                        , color1, color1
                        , color2, color1);

                { //TODO: DEUS WARNING
                    endScale();
                    startScale();
                    RenderUtil.drawImage(new ResourceLocation("nova", "icons/clickgui/deus_avatar44.png"),
                            (int) (x + 0.5f + leftBarWidth / 2f - 11), (int) (y + heightGui - 38), 22, 22, -1);
                    RenderUtil.drawImage(new ResourceLocation("nova", "icons/nova64px.png"), (int) (x + 0.5f + leftBarWidth / 2f - 16), y + 2, 32, 32, new Color(255, 255, 255, 255).getRGB());

                }
                StencilUtil.uninitStencilBuffer();
                endScale();

                float offset = 0;
                for (int i = 0; i < Category.values().length; i++) {
                    Category category = Category.values()[i];
                    String pathIn = "icons/clickgui/" + category.getName().toLowerCase() + ".png";

                    float width = 24;
                    float height = 24;
                    if (category == Category.COMBAT) {
                        width = 22f;
                        height = 22f;
                    } else if (category == Category.MOVEMENT) {
                        width = 20;
                    } else if (category == Category.RENDER) {
                        height = 18;
                    }
                    height /= 2f;
                    width /= 2f;
                    if (category == selected) {
                        float wwidth = 12 * 2f;
                        int xx = (int) (x + 1 + leftBarWidth / 2f - ((wwidth) / 2));
                        if (category != Category.COMBAT) {
                            xx -= 1;
                        }
                        int hheight = (int) (12 * 2f);
                        if (categoryBar < y)
                            this.categoryBar = (int) (y + 45 + (offset) - (hheight / 4f));
                        this.categoryBar = (int) MathUtil.animation(this.categoryBar, (y + 45 + (offset) - (hheight / 4f)), 10);

                        int yy = categoryBar;

                        {
                            RenderUtil.roundedOutlineGradientRectangle(xx, yy, wwidth, hheight, 12f,
                                    0.01f,
                                    new Color(1, 1, 1, 0)
                                    , color1.brighter(), color1.brighter()
                                    , color2.brighter(), color1.brighter());
                        }
                        startScale();
                        RenderUtil.roundedGradientRectangle(xx, yy, wwidth - 1, hheight - 1, 12f
                                , color1, color1
                                , color2, color1);

                        int finalXx = xx;
                        RenderUtil.applyBloom(3, 1, () -> RenderUtil.roundedGradientRectangle(finalXx, yy, wwidth - 1, hheight - 1, 12f
                                , color1, color1
                                , color2, color1));
                        RenderUtil.roundedOutlineGradientRectangle(xx, yy, wwidth - 1, hheight - 1, 12f, 0.6f
                                , color1N, color1N
                                , color2N, color1N);
                        endScale();
                    }
                    int y = (int) (this.y + 46 + (offset));
                    int x = (int) (this.x + leftBarWidth / 2f - (width / 2));
                    if (category == Category.MISC || category == Category.MOVEMENT) y -= 1;
                    if (category == Category.DISPLAY) y -= 1;
                    float finalWidth = width;
                    float finalHeight = height;
                    int finalY = y;
                    int finalX = x;
                    startScale();
                    RenderUtil.drawImage(new ResourceLocation("nova", pathIn),
                            finalX, finalY, (int) finalWidth, (int) finalHeight, -1);
                    endScale();
                    offset += height + 20;
                }
            }
            // TODO: Draw scrollBar
            {
                float standartOffset = 10;
                float fromHeight = 0.0753f;
                float standartX = x + leftBarWidth + standartOffset;
                float standartY = y + standartOffset;
                float standartWidth = widthGui - leftBarWidth - standartOffset * 2f;
                float standartHeight = heightGui * fromHeight;

                /** Background */
                {
                    startScale();
                    drawWithOutlineDefault(standartX + 0.5f, standartY + 0.5f, standartWidth - 1, standartHeight - 1,
                            color1, color2, 8.2f, true);
                    endScale();
                }

                {/** Modules */
                    Font font = Fonts.ROBOTO_MEDIUM.get(12.85f);
                    java.util.List<Module> allModules = Nova.getInstance().getModuleManager().getAll();
                    float modulesOffset = 0;
                    int id = 0;
                    int idb = 0;

                    int leftIdOffset = 0;
                    int rightIdOffset = 0;

                    for (int i = 0; i < allModules.size(); i++) {
                        Module module = allModules.get(i);
                        if (isSearching) {
                            if (textField.getAbsolutlyNotUsable().equals("")) {
                                if (!(module.getModuleInfo().category() == selected || selectedModules.contains(module))) continue;

                            }else
                                if (!module.getName().toLowerCase().contains(textField.getAbsolutlyNotUsable().toLowerCase())) continue;

                        }else
                            if (!(module.getModuleInfo().category() == selected)) continue;

                        if (MathUtil.isHovered(standartX, standartY, standartWidth, standartHeight, mouseX, mouseY)) { // TODO: handle ScrollBar ( Up )
//                            scrollBarB.onRender();
//                            scrollBar = (float) scrollBarB.getScroll();
                        }

                        modulesScrollBar = MathUtil.lerp(modulesScrollBar, scrollBar, 0.015f);
                        if ((standartX + (standartOffset * 1.5f) + modulesOffset + modulesScrollBar + font.getWidth(module.getName()) + standartOffset) > standartX + standartWidth) {
                            module.inScrollBar.setDirection(Direction.BACKWARDS);
                        } else if ((standartX + (standartOffset * 1.5f) + modulesOffset + modulesScrollBar - standartOffset) < standartX) {
                            module.inScrollBar.setDirection(Direction.BACKWARDS);
                        } else module.inScrollBar.setDirection(Direction.FORWARDS);
                        { // TODO: Background ( If Module Selected, Draw )
                            if (selectedModules.contains(module)) module.selectionAnim.setDirection(Direction.FORWARDS);
                            else module.selectionAnim.setDirection(Direction.BACKWARDS);

                            float alpha1 = (float) (Math.max(module.selectionAnim.getOutput(), 0.01)) * 0.55f;
                            float alpha2 = module.inScrollBar.getOutput().floatValue() + 0.5f;
                            Color c11 = new Color(color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, alpha1);
                            Color c22 = new Color(color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, alpha1);

                            startScale();
                            StencilUtil.initStencil();
                            RenderUtil.roundedGradientRectangle(standartX + 2.5f, standartY + 0.5f, standartWidth - 5, standartHeight - 1, 8.2f
                                    , color1, color1
                                    , color2, color1);
                            StencilUtil.bindReadStencilBuffer(1);
                            drawWithOutlineDefaultNoStencil(standartX + (standartOffset * 1.5f) + modulesOffset + modulesScrollBar - 3 - 1.1f, standartY + 5 - 1, font.getWidth(module.getName()) + 7.5f, standartHeight - 10 + 2.2f
                                    , c11, c22, 4.2f, true);
                            StencilUtil.uninitStencilBuffer();
                            endScale();
                        }
                        startScale();
                        StencilUtil.initStencil();
                        RenderUtil.roundedGradientRectangle(standartX + 2.5f, standartY + 0.5f, standartWidth - 5, standartHeight - 1, 8.2f
                                , color1, color1
                                , color2, color1);
                        StencilUtil.bindReadStencilBuffer(1);
                        font.draw(module.getName(), standartX + (standartOffset * 1.4f) + modulesOffset + modulesScrollBar, standartY + font.getMiddleOfBox(standartHeight) + 1, new Color(1f, 1f, 1f, module.inScrollBar.getOutput().floatValue() + 0.5f).getRGB());
                        StencilUtil.uninitStencilBuffer();
                        modulesOffset += font.getWidth(module.getName()) + 30;
                        endScale();
                        {
                            if (selectedModules.contains(module))
                                module.inListAnimation.setDirection(Direction.FORWARDS);
                            else module.inListAnimation.setDirection(Direction.BACKWARDS);
                            if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                id++;
                            }
                            if (Math.max(Math.min( module.inListAnimation.getNonNullOutput(),0.995),0.005) > 0.0) {
                                idb++;
                            }
                            boolean left = idb % 2 == 1;
                            float fromModuleWidth = 0.3899204244031f;
                            float moduleWidth = widthGui * fromModuleWidth;
                            float moduleXOffset = standartWidth - (moduleWidth * 2);
                            float moduleNewY = standartY + standartHeight + standartOffset + (!left ? rightIdOffset : leftIdOffset);
                            float moduleStandartHeight = 0;
                            float moduleStandartHeightMega = standartOffset * 2f;
                            float moduleStandartHeightConst = standartHeight * 1.5f;

                            //moduleStandartHeight += standartOffset;
                            for (Map.Entry<String, Setting> setting : module.getSettings().entrySet()) {
                                //if (!setting.getValue().getVisible().getAsBoolean()) continue;
                                setting.getValue().visibleAnimation.run(setting.getValue().getVisible().getAsBoolean() ? 1 : 0);
                                moduleStandartHeight += setting.getValue().visibleAnimation.getValue() * setting.getValue().getHeight();
                            }
                            module.setY((int) moduleNewY);
                            module.setX((int) (left ? standartX : standartX + moduleWidth + moduleXOffset));
                            float rectX = module.getX();
                            float rectY = module.getY() + (left ? leftIdOffset : rightIdOffset) + scrollModules;
                            float rectWidth = moduleWidth;
                            module.openAnimation.setEndPoint(moduleStandartHeight);
                            if (module.isOpened()) module.openAnimation.setDirection(Direction.FORWARDS);
                            else module.openAnimation.setDirection(Direction.BACKWARDS);
                            float rectHeight = (float) (module.openAnimation.getOutput() + moduleStandartHeightMega);

                            float rectHeightConst = moduleStandartHeightConst + standartOffset;


                            startScale();
                            ScaleUtil.scaleStart((rectX) + (moduleWidth / 2f), rectY + (moduleStandartHeight / 2f), module.inListAnimation.getOutput().floatValue());
                            { // TODO: RenderModule |  Handle Scroll (  Down )
                                if (!MathUtil.isHovered(standartX, standartY, standartWidth, standartHeight, mouseX, mouseY)) {
//                                    scroll.onRender();
//                                    ScrollBar += (float) scroll.getScroll();
                                }
                                if (ScrollBar > 0) ScrollBar = 0;

                                scrollModules = MathUtil.lerp(scrollModules, (float) ScrollBar, 0.015f);
                                GlStateManager.pushMatrix();
                                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                                scissor_rect(0, standartY + standartHeight, mw.getScaledWidth(), y + heightGui);
                                drawWithOutlineDefault(rectX, rectY, rectWidth, rectHeight, color1, color2, 8.2f, true);
                                Fonts.ROBOTO_REGULAR.get(15).draw(module.getName(), rectX + 8, rectY + Fonts.ROBOTO_REGULAR.get(15).getMiddleOfBox(moduleStandartHeightMega), -1);
                                if (module.getModuleInfo().allowDisable()) { // TODO: Draw Key
                                    String keyName = KeyUtil.getKeyboardKey(module.getKeyCode());
                                    float megaOffer = (keyName.length() <= 1 || module.getKeyCode() == 0 || module.isBinding()) ? 0.149319f : 0.169319f;
                                    float wdth = rectWidth * megaOffer;
                                    float megaOffer2 = 0.2805714f;
                                    float hght = rectHeightConst * megaOffer2;
                                    Color color1z = new Color(255, 255, 255, 65);
                                    Color color2b = new Color(255, 255, 255, 0);

                                    drawWithOutline(rectX + rectWidth - 8 - wdth, rectY + 4.6f, wdth, hght, color1z, color2b, 4.5f, true);
                                    Font fontik = Fonts.ROBOTO_REGULAR.get(15);

                                    String bind;
                                    if (module.isBinding()) {
                                        bind = "...";
                                    } else if (module.getKeyCode() == 0) {
                                        bind = "...";
                                    } else {
                                        bind = (keyName != "") ? keyName.replace(" ", "") : "...";
                                    }
                                    bind = bind.toLowerCase();
                                    fontik.drawCenterXY(bind, rectX + rectWidth - 8 - wdth + wdth / 2f - 1, rectY + 9.3f + fontik.getMiddleOfBox(hght), -1);

                                }
                                if (module.getModuleInfo().allowDisable()) { // TODO: Draw toggled
                                    float megaOffer = 0.129897f;
                                    float wdth = rectWidth * megaOffer;
                                    float megaOfferz = 0.149319f;
                                    float wdthz = rectWidth * megaOfferz;
                                    float megaOffer2 = 0.2805714f;
                                    float hght = rectHeightConst * megaOffer2;


                                    float rX = rectX + rectWidth - 8 - wdthz - 5 - wdth;
                                    Color color1z = new Color(255, 255, 255, 65);
                                    Color color2b = new Color(255, 255, 255, 0);

                                    drawWithOutline(rX, rectY + 4.3f, wdth, hght, color1z, color2b, 5f, true);
                                    if (module.isEnabled()) module.deanimation.setDirection(Direction.FORWARDS);
                                    else module.deanimation.setDirection(Direction.BACKWARDS);
                                    drawWithOutlineDefault((float) (rX + 2 + (module.deanimation.getOutput() * (wdth - 12))), rectY + 5.5f, 8, 8,
                                            3f, false);
                                    RenderUtil.roundedOutlineGradientRectangle((float) (rX + 3 + (module.deanimation.getOutput() * (wdth - 12))), rectY + 6f, 7, 7,
                                            3f, 0.5f, color2b.darker(), color1z.brighter().brighter(), color1z.brighter(), color1z.brighter());
                                }
                                int offset = (int) standartOffset * 2;
                                if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                    for (Map.Entry<String, Setting> setting : module.getSettings().entrySet()) {
                                        //if (!setting.getValue().getVisible().getAsBoolean()) continue;
                                        if ((rectY + offset + 5) < (rectY + rectHeight))
                                            setting.getValue().draw((int) rectX + 8, (int) rectY + offset, (int) rectWidth - 14, mouseX, mouseY, partialTicks);
                                        offset += setting.getValue().visibleAnimation.getNumberValue().floatValue() * setting.getValue().getHeight();
                                    }
                                }
                                end_scissor();
                                GlStateManager.popMatrix();
                                if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                    if (left) leftIdOffset += rectHeight / 2f + 5;
                                    else rightIdOffset += rectHeight / 2f + 5;
                                }
                            }
                            ScaleUtil.scaleEnd();
                            endScale();
                        }
                    }
                    float maxY = Math.max(leftIdOffset, rightIdOffset);
                    //scrollBar = (float) MathUtil.clamp(0,maxY,scrollBar);
                    //ScrollBar = (float) MathUtil.clamp(0,modulesOffset,ScrollBar);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {

        float fromLeftBarWidth = 0.12068f;
        float leftBarWidth = (widthGui - 1) * fromLeftBarWidth;
        float standartOffset = 10;
        float fromHeight = 0.0753f;
        float standartX = x + leftBarWidth + standartOffset;
        float standartY = y + standartOffset;
        float standartWidth = widthGui - leftBarWidth - standartOffset * 2f;
        float standartHeight = heightGui * fromHeight;

        if( MathUtil.isHovered(standartX + 0.5f, standartY + 0.5f, standartWidth - 1, standartHeight - 1,mouseX,mouseY) )
            scrollBar += delta * 15;
        else
            ScrollBar += delta * 15;
        if (scrollBar > 0) scrollBar = 0;
        if (ScrollBar > 0) ScrollBar = 0;

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline) {
        Color color1 = new Color(255, 255, 255, 65);
        Color colorb = new Color(239, 252, 255, 106);
        Color color22 = new Color(242, 250, 255, 101);

        Color color1N = new Color(255, 255, 255, 255);

        Color color2 = new Color(255, 255, 255, 60);

        Color color2B = new Color(255, 255, 255, 120);


        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius
                , colorb, colorb
                , color22, colorb);
        if (outline) {
            Color color1Brighter = color1N.brighter().brighter().brighter();
            Color color2Brighter = color2.brighter().brighter().brighter();
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5f
                    , color1Brighter, color2B
                    , color1Brighter, color2Brighter);
        }

    }

    public void drawWithOutline(float rectX, float rectY, float rectWidth, float rectHeight, Color color1, Color color2, float radius, boolean outline) {
        Color br1 = new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 156);
        Color br2 = new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 156);

        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.6f
                    , new Color(1, 1, 1, 0)
                    , br1, br1
                    , br2, br2);
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius
                , color1, color1
                , color2, color2);
    }

    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, Color color1, Color color2, float radius, boolean outline) {
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.6f
                    , new Color(1, 1, 1, 0)
                    , color1.brighter().brighter(), color1.brighter().brighter()
                    , color2.brighter().brighter(), color1.brighter().brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius
                , color1, color1
                , color2, color1);
    }

    public void drawWithOutlineDefaultNoStencil(float rectX, float rectY, float rectWidth, float rectHeight, Color color1, Color color2, float radius, boolean outline) {
        Color color1B = new Color(color1.getRed(), color1.getGreen(), color1.getBlue());
        Color color2B = new Color(color2.getRed(), color2.getGreen(), color1.getBlue());

        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.6f
                    , color1.brighter().brighter(), color1.brighter().brighter()
                    , color2.brighter().brighter(), color1.brighter().brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius
                , color1, color1
                , color2, color1);
    }

    public void scissor_rect(float x, float y, float width, float height) {
        int dwidth = mw.getScaledWidth();
        int dheight = mw.getScaledHeight();

        int factor = (int) mw.getScaleFactor();
        glScissor((int) (x * (float) factor), (int) (((double) dheight - height) * (double) factor), (int) ((width - x) * (float) factor), (int) ((height - (double) y) * (double) factor));
    }

    public void end_scissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }


    public void startScale() {
        ScaleUtil.scaleStart(x + widthGui / 2f, y + heightGui / 2f, openUi ? 0 : scaleAnimation.getOutput().floatValue());
    }

    public void endScale() {
        ScaleUtil.scaleEnd();
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        ClientUiScreen.drawWithOutlineDefault((float) (5), mw.getScaledHeight() - 25, 20,20,10, true,
                scaleAnimation.getOutput().floatValue() > 1 ?
                        254 :
                        scaleAnimation.getOutput().floatValue() < 0.01f ? 1 : (int) (scaleAnimation.getOutput().floatValue() * 254)
        );
        if (MathUtil.isHovered((float) (5), mw.getScaledHeight() - 25, 20,20,mouseX,mouseY)) {
            //onClose();
            ClientUiScreen screen = Nova.getInstance().getClientScreen();
            Nova.getInstance().getClientScreen().init();

            screen.uiWidth = 752 * 0.5f;
            screen.uiHeight = 478 * 0.5f;

            screen.x = width / 2f - screen.uiWidth / 2;
            screen.y = height / 2f - screen.uiHeight / 2;

            int x = (int) (screen.x + 20);
            float y = screen.y + screen.uiHeight / 6f;
            for (int i = 0; i < ClientUICategory.values().length; i++) {
                screen.buttons.add(new ClientCategoryWidget(ClientUICategory.values()[i], x + (i * (60 + ClientCategoryWidget.WIDTH)), (int) y));
            }

            scaleAnimation.setDirection(false);
            scaleAnimationHelp.reset();
            scaleAnimationHelp.setDirection(true);

            exit = true;
            openUi = true;
        }

        //altManager.mouseClicked(mouseX,mouseY,button);
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && mouseX >= x && mouseY >= y && mouseX <= x + getGuiWidth() && mouseY <= y + 40) { //determine the ability to launch the drag and run it
            mouseHoldX = (int) (mouseX - x);
            mouseHoldY = (int) (mouseY - y);
        }
        float fromLeftBarWidth = 0.12068f;
        float leftBarWidth = (widthGui - 1) * fromLeftBarWidth;
        { //TODO : handle leftBar

            float offset = 0;
            for (int i = 0; i < Category.values().length; i++) {
                Category category = Category.values()[i];
                float width = 24;
                float height = 24;
                if (category == Category.COMBAT) {
                    width = 22f;
                    height = 22f;
                } else if (category == Category.MOVEMENT) {
                    width = 20;
                    height = 24;
                } else if (category == Category.RENDER) {
                    width = 24;
                    height = 18;
                }
                height /= 2f;
                width /= 2f;

                float wwidth = width * 2f;
                int xx = (int) (x + 1 + leftBarWidth / 2f - ((wwidth) / 2));
                int hheight = (int) (height * 2f);
                int yy = (int) (y + 45 + (offset) - (hheight / 4f));

                if (MathUtil.isHovered(xx, yy, wwidth, hheight, mouseX, mouseY)) {
                    if (category != selected) {
                        scrollModules = -250;
                        modulesScrollBar = -250;
                        scrollBar = 0;
                        ScrollBar = 0;
                    }

                    selected = category;


                    return false;
                }

                offset += height + 20;
            }
        }
        {//TODO : Handle ScrollBar
            float standartOffset = 10;
            float fromHeight = 0.0753f;
            float standartX = x + leftBarWidth + standartOffset;
            float standartY = y + standartOffset;
            float standartWidth = widthGui - leftBarWidth - standartOffset * 2f;
            float standartHeight = heightGui * fromHeight;
            {
                {
                    if (MathUtil.isHovered(0, y, mw.getScaledWidth(), getGuiHeight(), mouseX, mouseY)) {
                        Font font = Fonts.ROBOTO_MEDIUM.get(12.85f);
                        java.util.List<Module> allModules = Nova.getInstance().getModuleManager().getAll();
                        float modulesOffset = 0;
                        int id = 0;
                        int idb = 0;

                        int leftIdOffset = 0;
                        int rightIdOffset = 0;

                        for (int i = 0; i < allModules.size(); i++) {
                            Module module = allModules.get(i);
                            if (isSearching) {
                                if (textField.getAbsolutlyNotUsable().equals("")) {
                                    if (!(module.getModuleInfo().category() == selected)) continue;
                                } else if (!module.getName().toLowerCase().contains(textField.getAbsolutlyNotUsable().toLowerCase()))
                                    continue;

                            } else if (!(module.getModuleInfo().category() == selected))
                                continue;

                            {// TODO: Background ( If Module Selected, Draw )
                                if (MathUtil.isHovered(standartX, standartY, standartWidth, standartHeight, mouseX, mouseY)) {
                                    if (MathUtil.isHovered(standartX + (standartOffset * 1.5f) + modulesOffset + modulesScrollBar - 3 - 1, standartY + 5 - 1, font.getWidth(module.getName()) + 6, standartHeight - 10 + 2,
                                            mouseX, mouseY)) {
                                        if (selectedModules.contains(module)) selectedModules.remove(module);
                                        else selectedModules.add(module);
                                        //return true;
                                    }
                                }
                                modulesOffset += font.getWidth(module.getName()) + 30;
                            }
                            {
                                if (!MathUtil.isHovered(standartX, standartY, standartWidth, standartHeight, mouseX, mouseY)) {
                                    if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                        id++;
                                    }
                                    if (Math.max(Math.min(module.inListAnimation.getNonNullOutput(), 0.995), 0.005) > 0.0) {
                                        idb++;
                                    }
                                    boolean left = idb % 2 == 1;
                                    float fromModuleWidth = 0.3899204244031f;
                                    float moduleWidth = widthGui * fromModuleWidth;
                                    float moduleXOffset = standartWidth - (moduleWidth * 2);
                                    float moduleNewY = standartY + standartHeight + standartOffset + (!left ? rightIdOffset : leftIdOffset);
                                    float moduleStandartHeight = standartOffset * 2f;
                                    float moduleStandartHeightMega = standartOffset * 2f;

                                    float moduleStandartHeightConst = standartHeight * 1.5f;

                                    if (!module.openAnimation.finished(Direction.BACKWARDS)) {
                                        for (Map.Entry<String, Setting> setting : module.getSettings().entrySet()) {
                                            if (!setting.getValue().getVisible().getAsBoolean()) continue;
                                            moduleStandartHeight += setting.getValue().getHeight();
                                        }
                                    }
                                    float rectX = module.getX();
                                    float rectY = module.getY() + (left ? leftIdOffset : rightIdOffset) + scrollModules;
                                    float rectWidth = moduleWidth;
                                    float rectHeight = (float) (module.openAnimation.getOutput() + moduleStandartHeightMega);


                                    float rectHeightConst = moduleStandartHeightConst + standartOffset;

                                    {
                                        if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                            if (MathUtil.isHovered(rectX, rectY, rectWidth, standartHeight, mouseX, mouseY)) {
                                                if (button == 0) {
                                                    module.toggle();
                                                    return true;
                                                }
                                                if (button == 1) {
                                                    module.setOpened(!module.isOpened());
                                                    return true;
                                                }
                                                if (button == 2) {
                                                    module.setBinding(!module.isBinding());
                                                    return true;
                                                }
                                            }
                                        }

                                        int offset = (int) standartOffset * 2;
                                        if (!module.inListAnimation.finished(Direction.BACKWARDS) && module.isOpened()) {
                                            for (Map.Entry<String, Setting> setting : module.getSettings().entrySet()) {
                                                if (!setting.getValue().getVisible().getAsBoolean()) continue;
                                                if (MathUtil.isHovered(rectX + 8, rectY + offset, rectWidth - 14, setting.getValue().getHeight(), mouseX, mouseY)) {
                                                    setting.getValue().mousePressed((int) rectX + 8, (int) rectY + offset, (int) rectWidth - 14, (int) mouseX, (int) mouseY, button);
                                                    return true;
                                                }
                                                offset += setting.getValue().getHeight();
                                            }
                                        }
                                        if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                            if (left) leftIdOffset += rectHeight / 2f + 5;
                                            else rightIdOffset += rectHeight / 2f + 5;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {

        for (Module module : selectedModules) {
            if (module.isBinding()) {
                module.setKeyCode(keyCode);
                module.setBinding(false);
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        altManager.mouseDragged(mouseX,mouseY,button,dragX,dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        altManager.mouseReleased(mouseX,mouseY,button);
        super.mouseReleased(mouseX, mouseY, button);
        if (button == 0) {
            mouseHoldX = mouseHoldY = -1; //give the ability to stop the drag
        }
        float fromLeftBarWidth = 0.12068f;
        float leftBarWidth = (widthGui - 1) * fromLeftBarWidth;

        {//TODO : Handle ScrollBar
            float standartOffset = 10;
            float fromHeight = 0.0753f;
            float standartX = x + leftBarWidth + standartOffset;
            float standartY = y + standartOffset;
            float standartWidth = widthGui - leftBarWidth - standartOffset * 2f;
            float standartHeight = heightGui * fromHeight;
            {
                {
                    Font font = Fonts.ROBOTO_MEDIUM.get(12.85f);
                    java.util.List<Module> allModules = Nova.getInstance().getModuleManager().getAll();
                    float modulesOffset = 0;
                    int id = 0;
                    int idb = 0;

                    int leftIdOffset = 0;
                    int rightIdOffset = 0;

                    for (int i = 0; i < allModules.size(); i++) {
                        Module module = allModules.get(i);
                        if (isSearching) {
                            if (textField.getAbsolutlyNotUsable().equals("")) {
                                if (!(module.getModuleInfo().category() == selected)) continue;
                            }else
                            if (!module.getName().toLowerCase().contains(textField.getAbsolutlyNotUsable().toLowerCase())) continue;

                        }else
                        if (!(module.getModuleInfo().category() == selected || selectedModules.contains(module))) continue;

                        {
                            if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                id++;
                            }
                            if (module.inListAnimation.getOutput() > 0.2) {
                                idb++;
                            }
                            boolean left = idb % 2 == 1;
                            float fromModuleWidth = 0.3899204244031f;
                            float moduleWidth = widthGui * fromModuleWidth;
                            float moduleXOffset = standartWidth - (moduleWidth * 2);
                            float moduleNewY = standartY + standartHeight + standartOffset + (!left ? rightIdOffset : leftIdOffset);
                            float moduleStandartHeight = standartOffset * 2f;
                            float moduleStandartHeightConst = standartHeight * 1.5f;

                            if (module.isOpened()) {
                                for (Map.Entry<String, Setting> setting : module.getSettings().entrySet()) {
                                    moduleStandartHeight += setting.getValue().getHeight();
                                }
                            }
                            float rectX = module.getX();
                            float rectY = module.getY() + (left ? leftIdOffset : rightIdOffset) + scrollModules;
                            float rectWidth = moduleWidth;
                            float rectHeight = moduleStandartHeight;

                            float rectHeightConst = moduleStandartHeightConst + standartOffset;

                            {


                                int offset = (int) standartOffset * 2;
                                {
                                    for (Map.Entry<String, Setting> setting : module.getSettings().entrySet()) {
                                        setting.getValue().mouseReleased((int) rectX + 8, (int) rectY + offset, (int) mouseX, (int) mouseY, button);
                                        offset += setting.getValue().getHeight();
                                    }
                                }
                                if (!module.inListAnimation.finished(Direction.BACKWARDS)) {
                                    if (left) leftIdOffset += offset / 2f + 5;
                                    else rightIdOffset += offset / 2f + 5;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
