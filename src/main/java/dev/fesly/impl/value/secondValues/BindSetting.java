package dev.fesly.impl.value.secondValues;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fesly.client.module.api.Module;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientUiScreen;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.KeyboardInputEvent;
import dev.fesly.impl.event.impl.input.MouseInputEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.impl.DecelerateAnimation;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.other.KeyUtil;
import dev.fesly.impl.util.render.RenderUtil;
import net.minecraft.util.ResourceLocation;
import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
public class BindSetting extends Setting implements IAccess {
    private IntSupplier getter;
    private IntConsumer setter;
    private final Module module;
    private boolean binding = false;
    public BindType bindType = null;
    public Runnable todo;
    public BindSetting(String name, Module parent, BooleanSupplier visible, IntSupplier getter, IntConsumer setter, Runnable todo) {
        super(name, 19, visible);
        this.getter = getter;
        this.setter = setter;
        this.module = parent;
        this.todo = todo;
    }
    public BindSetting(String name, Module parent, BooleanSupplier visible, int standartValue, Runnable todo) {
        super(name, 19, visible);
        this.getter = () -> standartValue;
        this.setter.accept(standartValue);
        this.module = parent;
        this.todo = todo;
    }
    private int value;
    public BindSetting(String name, Module parent, int standartValue, Runnable todo) {
        super(name, 19, () -> true);
        this.value = standartValue;
        this.getter = () -> value;
        this.setter = this::setValue;
        this.module = parent;
        this.todo = todo;
    }
    public BindSetting(String name, Module parent, IntSupplier getter, IntConsumer setter, Runnable todo) {
        super(name, 19, () -> true);
        this.getter = getter;
        this.setter = setter;
        this.module = parent;
        this.todo = todo;
    }
    public final Animation descriptionAnim = new DecelerateAnimation(220, 254);
    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        int bind = getter.getAsInt();
        String bindString = "";
        if (bindType != null) {
            if (bindType.equals(BindType.Keyboard)) {
                bindString = KeyUtil.getKeyboardKey(bind);
            } else if (bindType.equals(BindType.Mouse)) {
                bindString = String.valueOf(bind);
            }
        }
        if (bindString.equals("")) bindString = "...";
        int alpha = (int) (visibleAnimation.getNumberValue().floatValue() * 235);
        drawWithOutline(x + width - 26 + 6, y + 1, 19.094858F, 11.179752F, 4.8F, true, alpha);
        Font bindFont = Fonts.ROBOTO_REGULAR.get(13);
        Font defaultFont = Fonts.ROBOTO_REGULAR.get(15);
        bindFont.drawCenterX(bindString, x + width - 11.5, y + bindFont.getMiddleOfBox(12.37F), new Color(255, 255, 255, alpha).getRGB());
        defaultFont.draw(name, x, (long) y + 1.15F + defaultFont.getMiddleOfBox(10.379752F), new Color(255, 255, 255, alpha).getRGB());
        if (!description.equals("")) {
            drawWithOutline(x + width - 26 + 6 - 11, y + 1 + 1, 10.379752F - 2, 10.379752F - 2, 3.7F, true, alpha);
            RenderUtil.drawImage(new ResourceLocation("nova", "icons/ui/questionmark12.png"), x + width - 26 + 6 - 11 + 1, y + 1 + 1 + 1, 10.379752F - 4, 10.379752F - 4, new Color(255, 255, 255, alpha).getRGB());
            ClientUiScreen.drawWithOutlineDefault((float) MathUtil.clamp(x, x + width, mouseX - 2), (float) MathUtil.clamp(y, y + height, mouseY - 5), defaultFont.getWidth(description) + 3, defaultFont.getHeight() + 2, 2, true, alpha);
            if (descriptionAnim.getOutput() > 10) {
                defaultFont.draw(description, (float) MathUtil.clamp(x, x + width, mouseX - 1), (float) MathUtil.clamp(y, y + height, mouseY - 4), new Color(255, 255, 255, (int) MathUtil.clamp(1, 224, alpha)).getRGB());
            }
        }
        boolean hovered = MathUtil.isHovered(x + width - 26 + 6 - 11, y + 1 + 1, 10.379752F - 2, 10.379752F - 2, mouseX, mouseY);
        descriptionAnim.setDirection(!description.equals("") && hovered && getVisible().getAsBoolean());
    }
    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline, int alpha) {
        Color color1 = new Color(255, 255, 255, alpha);
        Color colorb = new Color(239, 252, 255, alpha);
        Color color22 = new Color(242, 250, 255, alpha);
        Color color1N = new Color(255, 255, 255, alpha);
        Color color2 = new Color(255, 255, 255, alpha);
        Color color2B = new Color(255, 255, 255, alpha);
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, colorb, colorb, color22, colorb);
        if (outline) {
            Color color1Brighter = color1N.brighter().brighter().brighter();
            Color color2Brighter = color2.brighter().brighter().brighter();
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5F, color1Brighter, color2B, color1Brighter, color2Brighter);
        }
    }
    public void drawWithOutline(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline, int alpha) {
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color color1 = colorEnum.getSettingBackgroundColor();
        Color color2 = colorEnum.getSettingBackgroundColor2();
        Color color1N = colorEnum.getSettingBackgroundColor();
        Color color2B = colorEnum.getSettingBackgroundColor2();
        if (outline) {
            Color color1Brighter = color1N.brighter().brighter().brighter();
            Color color2Brighter = color2.brighter().brighter().brighter();
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5F, color1Brighter, color2B, color1Brighter, color2Brighter);
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha), new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha), new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha), new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha));
    }
    @Override
    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        if (mc.currentScreen != null) {
            if (binding) {
                bindType = BindType.Mouse;
                setter.accept(mouseButton);
                binding = false;
            } else {
                {
                    binding = true;
                }
            }
        }
    }
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getter == null || bindType == null) return;
        if (mc.currentScreen != null) {
            if (binding) {
                bindType = BindType.Keyboard;
                setter.accept(keyCode);
                binding = false;
            }
        }
    }
    @EventLink
    public Listener<KeyboardInputEvent> onKeyboardInput = event -> {
        if (getter == null || bindType == null) return;
        if (mc.currentScreen == null) {
            binding = false;
            {
                if (bindType.equals(BindType.Keyboard)) if (event.getKeyCode() == getter.getAsInt()) {
                    todo.run();
                }
            }
        }
    };
    @EventLink
    public Listener<MouseInputEvent> onMouseInput = event -> {
        if (getter == null || bindType == null) return;
        if (mc.currentScreen == null) {
            binding = false;
            if (bindType.equals(BindType.Mouse)) {
                {
                    if (event.getMouseButton() == getter.getAsInt()) {
                        todo.run();
                    }
                }
            }
        }
    };
    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }
    @Override
    public void load(JsonElement json) {
        setter.accept(json.getAsInt());
    }
    @Override
    public JsonElement save() {
        return new JsonPrimitive(getter.getAsInt());
    }
    public enum BindType {
        Mouse, Keyboard;
    }
    @java.lang.SuppressWarnings("all")
    public IntSupplier getGetter() {
        return this.getter;
    }
    @java.lang.SuppressWarnings("all")
    public IntConsumer getSetter() {
        return this.setter;
    }
    @java.lang.SuppressWarnings("all")
    public Module getModule() {
        return this.module;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isBinding() {
        return this.binding;
    }
    @java.lang.SuppressWarnings("all")
    public BindType getBindType() {
        return this.bindType;
    }
    @java.lang.SuppressWarnings("all")
    public Runnable getTodo() {
        return this.todo;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getDescriptionAnim() {
        return this.descriptionAnim;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<KeyboardInputEvent> getOnKeyboardInput() {
        return this.onKeyboardInput;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<MouseInputEvent> getOnMouseInput() {
        return this.onMouseInput;
    }
    @java.lang.SuppressWarnings("all")
    public int getValue() {
        return this.value;
    }
    @java.lang.SuppressWarnings("all")
    public void setValue(final int value) {
        this.value = value;
    }
}
