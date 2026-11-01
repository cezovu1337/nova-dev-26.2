package dev.fesly.impl.value.secondValues;

import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.math.StringConsumer;
import dev.fesly.impl.util.math.StringSupplier;
import dev.fesly.impl.util.render.RenderUtil;
import lombok.Getter;
import lombok.Setter;
import nd.qunamy.nova.api.NovaApi;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public class NovaTextField implements IAccess {

    @Getter
    @Setter
    public Font font;

    @Getter
    @Setter
    public boolean centered = false;

    @Getter
    @Setter
    public String absolutlyNotUsable = "";

    private final StringSupplier getter;
    private final StringConsumer setter;

    private Animation draggedLine =  new Animation(Easing.EASE_OUT_CUBIC, 75);
    private Animation rgb =  new Animation(Easing.EASE_OUT_CUBIC, 500);

    private Animation alpha =  new Animation(Easing.EASE_OUT_CUBIC, 500);

    private int selectedSymbol = 0; // INDEX of the selected symbol
    public ArrayList<String> charArray = new ArrayList<String>();

    public boolean dragged = false;


    public NovaTextField(Font font,boolean centered) {
        this.font = font;
        this.centered = centered;
        this.getter = this::getAbsolutlyNotUsable;
        this.setter = this::setAbsolutlyNotUsable;
    }

    public void defaultRenderText(int x, int y, int mouseX, int mouseY, float partialTicks,float alphaValue) {
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
        rgb.run(!dragged ? alphaValue : 255);
        if (!dragged) {
            alpha.run(0);
        }else {
            if (alpha.getNumberValue().intValue() == 255) {
                alpha.run(0);
            }else
                alpha.run(255);
        }

        String value = empty ? "Введите текст.." : getter.get();

        y-=3;
        int w = 65;

        int rgbValue = alpha.getNumberValue().intValue();
        x = (int) (x - font.getWidth(value) / 2f);
        if (rgbValue > 10) {
            font.draw(value, x, y, new Color(255, 255, 255, rgbValue).getRGB());
        }

        if (dragged) {
            if (draggedLine.getNumberValue().intValue() < 2) {
                draggedLine.setValue(x);
            }


            StringBuilder builder = new StringBuilder();
            String orEmpty = "Введите текст..";
            char[] chars = getter.get().toCharArray();
            for (int i = 0; i < selectedSymbol; i++) {
                int charIndex = (int) MathUtil.clamp(0, chars.length - 1, i);
                builder.append(charArray.get(i));
            }

            float textWidth =
                    charArray.isEmpty() ?
                    font.getWidth(orEmpty)
                    : font.getWidth(builder.toString());
            float draggedLineX = (float) (x + textWidth + 1);

            draggedLine.run(draggedLineX);
        }
        RenderUtil.roundedRectangle(draggedLine.getNumberValue().floatValue(), y, 0.6f, font.getHeight(), 0,
                new Color(255, 255, 255,
                alpha.getNumberValue().intValue()));

    }

    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        dragged = true;
    }


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
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (String s : charArray) {
                stringBuilder.append(s);
            }
            setter.accept(stringBuilder.toString());
        }
    }


}
