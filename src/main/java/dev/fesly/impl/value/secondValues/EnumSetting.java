package dev.fesly.impl.value.secondValues;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.client.module.api.Module;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.DecelerateAnimation;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.GLUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.shader.GradientUtil;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import java.awt.*;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
public class EnumSetting<E extends Enum<E>> extends Setting {
    private final Supplier<E> getter;
    private final Consumer<E> setter;
    private final Class<E> enumClass;
    private final E[] values;
    private boolean expanded = false;
    public int heightAnim = 15;
    public String selected = "selected..";
    private final Module module;
    public EnumSetting(String name, Module parent, BooleanSupplier visible, Supplier<E> getter, Consumer<E> setter, Class<E> enumClass) {
        super(name, enumClass.getEnumConstants().length * 10 + 15, visible);
        this.getter = getter;
        this.setter = setter;
        this.enumClass = enumClass;
        values = enumClass.getEnumConstants();
        this.module = parent;
    }
    public EnumSetting(String name, Module parent, Supplier<E> getter, Consumer<E> setter, Class<E> enumClass) {
        super(name, enumClass.getEnumConstants().length * 10 + 15, () -> true);
        this.getter = getter;
        this.setter = setter;
        this.enumClass = enumClass;
        values = enumClass.getEnumConstants();
        this.module = parent;
    }
    private int bgalpha;
    private int rotateAnim;
    private int expandedx;
    private int expandedy;
    private String maxString = "ы";
    public final Animation animation = new DecelerateAnimation(120, 180);
    public final Animation offset = new DecelerateAnimation(120, 1);
    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        dev.fesly.font.Font namefont = Fonts.ROBOTO_REGULAR.get(15);
        dev.fesly.font.Font selectionsfont = Fonts.ROBOTO_REGULAR.get(15);
        int alphaV = (int) ((visibleAnimation.getNumberValue().floatValue()) * 235);
        E value = getter.get();
        for (int i = 0; i < values.length; i++) {
            String name = values[i].toString();
            if (name.length() > maxString.length()) {
                maxString = name;
            }
        }
        int valueheight = !expanded ? selectionsfont.getHeight() + 4 : (int) (enumClass.getEnumConstants().length * ((selectionsfont.getHeight())) + 21);
        this.heightAnim = (int) MathUtil.lerp(heightAnim, valueheight, 2.0F / 10);
        String ofname = Objects.requireNonNull(getter.get().name());
        float roundx = x + width - selectionsfont.getWidth(maxString) - 17;
        float roundy = y - 2;
        float roundwidth = selectionsfont.getWidth(maxString) + 16;
        float roundheight = this.heightAnim;
        boolean roud = (mouseX > roundx && mouseX < roundx + roundwidth && mouseY > roundy && mouseY < roundy + roundheight) || expanded;
        drawWithOutlineDefault(roundx, roundy, roundwidth, roundheight, 3.0F, true, alphaV);
        float alpha = roud ? 140 : 100;
        this.bgalpha = (int) MathUtil.animation(this.bgalpha, alpha, 2);
        offset.setDirection(expanded);
        if (expanded) {
            Color c = new Color(254, 254, 254, heightAnim);
            AbstractGui.fill(new MatrixStack(), (int) (roundx + 2), (int) (roundy + 10), (int) (roundx + roundwidth - 4), (int) (roundy + 10.5), c.getRGB());
        }
        Color textColor = new Color(255, 255, 255, alphaV);
        Color textColorOne = new Color(255, 255, 255, alphaV);
        if (alphaV > 5) namefont.draw(name, x, y, textColorOne.getRGB());
        Color alphab = new Color(255, 255, 255, 1);
        Color noalpha = new Color(255, 255, 255, alphaV);
        if (alphaV > 5) GradientUtil.applyGradient(roundx, roundy, roundwidth - 8, 10, 1.0F, noalpha, noalpha, alphab, alphab, () -> namefont.draw(ofname, roundx + 2, y + namefont.getMiddleOfBox(selectionsfont.getHeight()), new Color(255, 255, 255, alphaV).getRGB()));
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color cb = colorEnum.getBackgroundColor2().brighter();
        if (alphaV > 5) if (expanded) {
            StencilUtil.initStencil();
            RenderUtil.roundedRectangle(roundx, roundy, roundwidth, roundheight, 1.0F, new Color(31, 31, 31, bgalpha));
            StencilUtil.bindReadStencilBuffer(1);
            for (int i = 0; i < values.length; i++) {
                selectionsfont.draw(values[i].toString(), roundx + 2, y - ((int) ((1.0F - offset.getOutput().floatValue()) * 5)) + i * (selectionsfont.getHeight() * (offset.getOutput().floatValue())) + 12, value == values[i] ? new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), (int) ((alphaV / 255.0F) * (offset.getOutput().floatValue() * 250))).getRGB() : textColor.getRGB());
            }
            StencilUtil.uninitStencilBuffer();
        }
        int valuerotate = !expanded ? 0 : -180;
        int valuex = !expanded ? 0 : 5;
        int valuey = !expanded ? -40 + 30 : 2;
        this.rotateAnim = (int) MathUtil.lerp(rotateAnim, valuerotate, 2.0F / 2);
        this.expandedx = (int) MathUtil.lerp(expandedx, valuex, 2.0F / 2);
        this.expandedy = (int) MathUtil.lerp(expandedy, valuey, 2.0F / 2);
        Color finalTextColor = textColor;
        Color c = colorEnum.getBackgroundColor2();
        Color c2 = colorEnum.getBackgroundColor();
        Runnable rotateComponent = new Runnable() {
            @Override
            public void run() {
                RenderUtil.drawImage(new ResourceLocation("nova", "icons/ui/arrowm.png"), (int) (roundx + roundwidth - 9), (int) (roundy + 4), 5, 5, new Color(240, 240, 240, alphaV).getRGB());
            }
        };
        if (expanded) animation.setDirection(Direction.FORWARDS);
         else animation.setDirection(Direction.BACKWARDS);
        Runnable rotated = () -> GLUtil.rotate((roundx + roundwidth - 6.5F), (int) (roundy + 6.8F), animation.getOutput().floatValue(), rotateComponent);
        {
            rotated.run();
        }
    }
    @Override
    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        dev.fesly.font.Font namefont = Fonts.ROBOTO_REGULAR.get(15);
        Font selectionsfont = Fonts.ROBOTO_REGULAR.get(15);
        float roundx = x + width - selectionsfont.getWidth(maxString) - 17;
        float roundy = y - 2;
        float roundwidth = selectionsfont.getWidth(maxString) + 16;
        if (expanded) {
            for (int i = 0; i < values.length; i++) {
                if (MathUtil.isHovered(roundx, y + i * (selectionsfont.getHeight()) + 12, roundwidth, selectionsfont.getHeight(), mouseX, mouseY)) {
                    setter.accept(values[i]);
                    selected = values[i].name();
                    expanded = false;
                    return;
                }
            }
        }
        if (MathUtil.isHovered(x, y - 2, width, namefont.getHeight() + 4, mouseX, mouseY) || MathUtil.isHovered(roundx, roundy, roundwidth, selectionsfont.getHeight() + 4, mouseX, mouseY)) {
            expanded = !expanded;
        }
    }
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline, int alpha) {
        Color color1 = new Color(255, 255, 255, (int) (((alpha / 255.0F) * 65)));
        Color color2 = new Color(255, 255, 255, 0);
        Color color1A = new Color(255, 255, 255, (int) (((alpha / 255.0F) * 245)));
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5F, color1A.brighter().brighter(), color2.brighter().brighter(), color1A.brighter().brighter(), color1A.brighter().brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, color1, color1, color2, color1);
    }
    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }
    @Override
    public void load(JsonElement json) {
        setter.accept(Enum.valueOf(enumClass, json.getAsString()));
    }
    @Override
    public JsonElement save() {
        return new JsonPrimitive(getter.get().name());
    }
    @Override
    public int getHeight() {
        return heightAnim + 5;
    }
    @java.lang.SuppressWarnings("all")
    public Supplier<E> getGetter() {
        return this.getter;
    }
    @java.lang.SuppressWarnings("all")
    public Consumer<E> getSetter() {
        return this.setter;
    }
    @java.lang.SuppressWarnings("all")
    public Class<E> getEnumClass() {
        return this.enumClass;
    }
    @java.lang.SuppressWarnings("all")
    public E[] getValues() {
        return this.values;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isExpanded() {
        return this.expanded;
    }
    @java.lang.SuppressWarnings("all")
    public int getHeightAnim() {
        return this.heightAnim;
    }
    @java.lang.SuppressWarnings("all")
    public String getSelected() {
        return this.selected;
    }
    @java.lang.SuppressWarnings("all")
    public Module getModule() {
        return this.module;
    }
    @java.lang.SuppressWarnings("all")
    public int getBgalpha() {
        return this.bgalpha;
    }
    @java.lang.SuppressWarnings("all")
    public int getRotateAnim() {
        return this.rotateAnim;
    }
    @java.lang.SuppressWarnings("all")
    public int getExpandedx() {
        return this.expandedx;
    }
    @java.lang.SuppressWarnings("all")
    public int getExpandedy() {
        return this.expandedy;
    }
    @java.lang.SuppressWarnings("all")
    public String getMaxString() {
        return this.maxString;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getAnimation() {
        return this.animation;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getOffset() {
        return this.offset;
    }
}
