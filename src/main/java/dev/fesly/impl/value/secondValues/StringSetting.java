package dev.fesly.impl.value.secondValues;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fesly.client.module.api.Module;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.client.screen.mainmenu.AltManager;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.math.FloatSupplier;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.math.StringConsumer;
import dev.fesly.impl.util.math.StringSupplier;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.RoundedUtils;
import dev.fesly.impl.value.defaultValues.impl.NumberValue;
import dev.fesly.impl.value.defaultValues.impl.StringValue;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
public class StringSetting extends Setting implements IAccess {
    private final StringSupplier getter;
    private StringConsumer setter;
    private boolean dragged;
    private final Module module;
    public StringSetting(String name, Module parent, BooleanSupplier visible, StringSupplier getter, StringConsumer setter) {
        super(name, 19, visible);
        this.getter = getter;
        this.setter = setter;
        this.module = parent;
        selectedSymbol = getter.get().length() - 1;
        char[] chars = getter.get().toCharArray();
        for (char aChar : chars) {
            charArray.add(String.valueOf(aChar));
        }
    }
    public StringSetting(String name, Module parent, StringSupplier getter, StringConsumer setter) {
        super(name, 19, () -> true);
        this.getter = getter;
        this.setter = setter;
        this.module = parent;
    }
    private String value = "";
    public StringSetting(String name, Module parent, String defaultValue) {
        super(name, 19, () -> true);
        value = defaultValue;
        this.getter = () -> value;
        this.setter = this::setValue;
        this.module = parent;
        if (getter.get() != null) {
            char[] chars = getter.get().toCharArray();
            for (char aChar : chars) {
                charArray.add(String.valueOf(aChar));
            }
        }
    }
    public StringSetting(StringValue value) {
        super(value.getName(), 19, () -> true);
        this.getter = value::getValue;
        this.setter = value::setValue;
        this.module = value.getParent();
        if (getter.get() != null) {
            char[] chars = getter.get().toCharArray();
            for (char aChar : chars) {
                getCharArray().add(String.valueOf(aChar));
            }
        }
    }
    public float getMiddleOfBox(float boxHeight, float fullheight) {
        return boxHeight / 2.0F - fullheight / 2.0F;
    }
    public String getValue() {
        return getter.get();
    }
    private Animation draggedLine = new Animation(Easing.EASE_OUT_CUBIC, 75);
    private Animation rgb = new Animation(Easing.EASE_OUT_CUBIC, 500);
    private Animation alpha = new Animation(Easing.EASE_OUT_CUBIC, 500);
    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        {
            if (charArray.isEmpty()) {
                selectedSymbol = getter.get().length();
                char[] chars = getter.get().toCharArray();
                for (char aChar : chars) {
                    charArray.add(String.valueOf(aChar));
                }
            }
        }
        boolean empty = getter.get().equalsIgnoreCase("");
        if (rgb.getNumberValue().intValue() < 5) rgb.setValue(195);
        rgb.run(!dragged ? 165 : 255);
        if (!dragged) {
            alpha.run(0);
        } else {
            if (alpha.getNumberValue().intValue() == 255) {
                alpha.run(0);
            } else alpha.run(255);
        }
        String value = empty ? "Введите текст.." : getter.get();
        Color textColor = new Color(-1);
        Font font1 = Fonts.ROBOTO_REGULAR.get(15);
        Font font2 = Fonts.ROBOTO_MEDIUM.get(13.0F);
        y -= 3;
        font1.draw(name, x, y + 1 + font1.getMiddleOfBox(height - 4), textColor.getRGB());
        int w = 65;
        drawWithOutline(x + width - w, y + 1, w, height - 4, 3, true);
        int rgbValue = rgb.getNumberValue().intValue();
        font2.draw(value, x + width - (w - 2), y + 1 + font2.getMiddleOfBox(height - 4), new Color(255, 255, 255, rgbValue).getRGB());
        if (dragged) {
            if (draggedLine.getNumberValue().intValue() < 2) {
                draggedLine.setValue(x + width - (w - 2));
            }
            StringBuilder builder = new StringBuilder();
            String orEmpty = "введитеТекст";
            char[] chars = getter.get().replace(" ", "i").toCharArray();
            for (int i = 0; i < selectedSymbol; i++) {
                int charIndex = (int) MathUtil.clamp(0, chars.length - 1, i);
                builder.append(chars[charIndex]);
            }
            float textWidth = charArray.isEmpty() ? font2.getWidth(orEmpty) : font2.getWidth(builder.toString());
            float draggedLineX = (float) (x + width - (w - 2) + (textWidth) + (charArray.isEmpty() ? 8 : 1));
            draggedLine.run(draggedLineX);
        }
        RenderUtil.roundedRectangle(draggedLine.getNumberValue().floatValue(), y + 3, 0.6F, height - 8, 0, new Color(255, 255, 255, alpha.getNumberValue().intValue()));
    }
    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline) {
        Color color1 = new Color(255, 255, 255, 65);
        Color color2 = new Color(255, 255, 255, 0);
        Color color1A = new Color(255, 255, 255, 245);
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5F, color1A.brighter().brighter(), color2.brighter().brighter(), color1A.brighter().brighter(), color1A.brighter().brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, color1, color1, color2, color1);
    }
    public void drawWithOutlineDefaultB(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline) {
        Color color1 = new Color(255, 255, 255, 65);
        Color colorb = new Color(239, 252, 255, 106);
        Color color22 = new Color(242, 250, 255, 101);
        Color color1N = new Color(255, 255, 255, 255);
        Color color2 = new Color(255, 255, 255, 60);
        Color color2B = new Color(255, 255, 255, 120);
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, colorb, colorb, color22, colorb);
        if (outline) {
            Color color1Brighter = color1N.brighter().brighter().brighter();
            Color color2Brighter = color2.brighter().brighter().brighter();
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5F, color1Brighter, color2B, color1Brighter, color2Brighter);
        }
    }
    public void drawWithOutlineDefaultFullAlpha(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline) {
        Color color1 = new Color(233, 255, 235, 255);
        Color color2 = new Color(255, 255, 255, 0);
        Color color1A = new Color(236, 255, 253, 245);
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5F, color1A.brighter().brighter(), color2.brighter().brighter(), color1A.brighter().brighter(), color1A.brighter().brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, color1, color1, color2, color1);
    }
    public void drawWithOutline(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline) {
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color color1 = colorEnum.getSettingBackgroundColor();
        Color color2 = colorEnum.getSettingBackgroundColor2();
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.6F, new Color(1, 1, 1, 0), color1.brighter(), color1.brighter(), color2.brighter(), color2.brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, color1, color1, color2, color2);
    }
    @Override
    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        dragged = true;
    }
    private int selectedSymbol = 0;
    private ArrayList<String> charArray = new ArrayList<String>();
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (dragged) {
            if (keyCode == 256) dragged = false;
            String buffer = mc.keyboardListener.getClipboardString();
            boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            boolean isControlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
            boolean isCapsLockOn = (GLFW.GLFW_MOD_CAPS_LOCK & modifiers) != 0;
            int charArrayLength = getter.get().toCharArray().length;
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                dragged = false;
                selectedSymbol = Math.min(Math.max(selectedSymbol, 0), charArray.size());
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                selectedSymbol = Math.min(selectedSymbol + 1, charArray.size());
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                selectedSymbol = Math.max(selectedSymbol - 1, 0);
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!charArray.isEmpty() && selectedSymbol > 0 && selectedSymbol <= charArray.size()) {
                    charArray.remove(selectedSymbol - 1);
                    selectedSymbol = Math.max(selectedSymbol - 1, 0);
                }
            } else {
                String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
                if (keyName != null) {
                    keyName = isShiftPressed || isCapsLockOn ? keyName.toUpperCase() : keyName.toLowerCase();
                    if (keyName.equals("-") && isShiftPressed) {
                        keyName = "_";
                    }
                    if (keyName.equals("1") && isShiftPressed) {
                        keyName = "!";
                    }
                    if (keyName.equals("0") && isShiftPressed) {
                        keyName = ")";
                    }
                    if ((keyName.equalsIgnoreCase("v") || keyName.equalsIgnoreCase("м")) && isControlPressed) {
                        ArrayList<String> textToPaste = new ArrayList<>();
                        char[] chars = buffer.toCharArray();
                        for (char c : chars) {
                            if (c != ' ') {
                                textToPaste.add(String.valueOf(c));
                            }
                        }
                        charArray.addAll(selectedSymbol, textToPaste);
                        selectedSymbol += textToPaste.size();
                        selectedSymbol = Math.min(Math.max(selectedSymbol, 0), charArray.size());
                    } else {
                        selectedSymbol = Math.min(Math.max(selectedSymbol, 0), charArray.size());
                        charArray.add(selectedSymbol, keyName);
                        selectedSymbol++;
                    }
                } else if (keyCode == GLFW.GLFW_KEY_SPACE) {
                    selectedSymbol = Math.min(Math.max(selectedSymbol, 0), charArray.size());
                    charArray.add(selectedSymbol, " ");
                    selectedSymbol++;
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : charArray) {
                stringBuilder.append(s);
            }
            setter.accept(stringBuilder.toString());
        }
    }
    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }
    @Override
    public void load(JsonElement json) {
        setter.accept(json.getAsString());
    }
    @Override
    public JsonElement save() {
        return new JsonPrimitive(getter.get());
    }
    @java.lang.SuppressWarnings("all")
    public StringSupplier getGetter() {
        return this.getter;
    }
    @java.lang.SuppressWarnings("all")
    public StringConsumer getSetter() {
        return this.setter;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isDragged() {
        return this.dragged;
    }
    @java.lang.SuppressWarnings("all")
    public Module getModule() {
        return this.module;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getDraggedLine() {
        return this.draggedLine;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getRgb() {
        return this.rgb;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getAlpha() {
        return this.alpha;
    }
    @java.lang.SuppressWarnings("all")
    public int getSelectedSymbol() {
        return this.selectedSymbol;
    }
    @java.lang.SuppressWarnings("all")
    public ArrayList<String> getCharArray() {
        return this.charArray;
    }
    @java.lang.SuppressWarnings("all")
    public void setValue(final String value) {
        this.value = value;
    }
}
