package dev.fesly.impl.value.secondValues;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
public class MultiEnumSetting<E extends Enum<E>> extends Setting {
    private final Supplier<EnumSet<E>> getter;
    private final Consumer<EnumSet<E>> setter;
    private final Class<E> enumClass;
    private final E[] values;
    private int bgalpha;
    private int rotateAnim;
    private int expandedx;
    private int expandedy;
    private String maxString = "ы";
    private boolean expanded = false;
    public int heightAnim = 15;
    public String selected = "selected..";
    private final Module module;
    public MultiEnumSetting(String name, Module parent, BooleanSupplier visible, Supplier<EnumSet<E>> getter, Consumer<EnumSet<E>> setter, Class<E> enumClass) {
        super(name, enumClass.getEnumConstants().length * 10 + 15, visible);
        this.getter = getter;
        this.setter = setter;
        this.enumClass = enumClass;
        values = enumClass.getEnumConstants();
        this.module = parent;
    }
    public MultiEnumSetting(String name, Module parent, Supplier<EnumSet<E>> getter, Consumer<EnumSet<E>> setter, Class<E> enumClass) {
        super(name, enumClass.getEnumConstants().length * 10 + 15, () -> true);
        this.getter = getter;
        this.setter = setter;
        this.enumClass = enumClass;
        values = enumClass.getEnumConstants();
        this.module = parent;
    }
    public final Animation animation = new DecelerateAnimation(120, 180);
    public final Animation offset = new DecelerateAnimation(120, 1);
    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        dev.fesly.font.Font namefont = Fonts.ROBOTO_REGULAR.get(15);
        dev.fesly.font.Font selectionsfont = Fonts.ROBOTO_REGULAR.get(15);
        EnumSet<E> set = getter.get();
        String ofname = set.size() > 0 ? set.toArray()[0].toString() : "empty";
        for (int i = 0; i < enumClass.getEnumConstants().length; i++) {
            String name = enumClass.getEnumConstants()[i].toString();
            if (name.length() > maxString.length()) {
                maxString = name;
            }
        }
        String aa = " ";
        for (int i = 0; i < ofname.length(); i++) {
            aa += ofname.charAt(i);
        }
        maxString = aa;
        int valueheight = !expanded ? selectionsfont.getHeight() + 4 : (int) (enumClass.getEnumConstants().length * ((selectionsfont.getHeight())) + 21);
        this.heightAnim = (int) MathUtil.lerp(heightAnim, valueheight, 2.0F / 10);
        float wd = (float) MathUtil.clamp(45, 240, selectionsfont.getWidth(maxString));
        float roundx = x + width - wd - 17;
        float roundy = y - 2;
        float roundwidth = (float) (wd + 16);
        float roundheight = this.heightAnim;
        boolean roud = (mouseX > roundx && mouseX < roundx + roundwidth && mouseY > roundy && mouseY < roundy + roundheight) || expanded;
        Runnable round = new Runnable() {
            @Override
            public void run() {
                drawWithOutlineDefault(roundx, roundy, roundwidth, roundheight, 3.0F, true);
            }
        };
        float alpha = roud ? 140 : 100;
        this.bgalpha = (int) MathUtil.animation(this.bgalpha, alpha, 2);
        round.run();
        if (expanded) {
            Color c = new Color(254, 254, 254, heightAnim);
            AbstractGui.fill(new MatrixStack(), (int) (roundx + 2), (int) (roundy + 10), (int) (roundx + roundwidth - 4), (int) (roundy + 10.5), c.getRGB());
        }
        Color textColor = new Color(255, 255, 255, (int) (offset.getOutput().floatValue() * 250));
        Color textColorOne = new Color(-1);
        namefont.draw(name, x, y + namefont.getMiddleOfBox(selectionsfont.getHeight() + 2), textColorOne.getRGB());
        Color alphab = new Color(255, 255, 255, 1);
        Color noalpha = new Color(255, 255, 255, 255);
        String finalAa = aa;
        GradientUtil.applyGradient(roundx, roundy, 2, 10, 1.0F, noalpha, noalpha, alphab, alphab, () -> namefont.draw(finalAa, roundx, y + namefont.getMiddleOfBox(selectionsfont.getHeight()), textColorOne.getRGB()));
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color cb = colorEnum.getBackgroundColor2().brighter();
        if (expanded) {
            StencilUtil.initStencil();
            RenderUtil.roundedRectangle(roundx, roundy, roundwidth, roundheight, 1.0F, new Color(31, 31, 31, bgalpha));
            StencilUtil.bindReadStencilBuffer(1);
            for (int i = 0; i < values.length; i++) {
                selectionsfont.draw(values[i].toString(), roundx + 2, y + i * (selectionsfont.getHeight()) + 12, set.contains(values[i]) ? new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), (int) (offset.getOutput().floatValue() * 250)).getRGB() : textColor.getRGB());
            }
            StencilUtil.uninitStencilBuffer();
        }
        if (expanded) animation.setDirection(Direction.FORWARDS);
         else animation.setDirection(Direction.BACKWARDS);
        offset.setDirection(expanded);
        Runnable rotateComponent = new Runnable() {
            @Override
            public void run() {
                RenderUtil.drawImage(new ResourceLocation("nova", "icons/ui/arrowm.png"), (int) (roundx + roundwidth - 9), (int) (roundy + 4), 5, 5, new Color(240, 240, 240).getRGB());
            }
        };
        if (expanded) animation.setDirection(Direction.FORWARDS);
         else animation.setDirection(Direction.BACKWARDS);
        Runnable rotated = () -> GLUtil.rotate((roundx + roundwidth - 6.5F), (int) (roundy + 6.5F), animation.getOutput().floatValue(), rotateComponent);
        {
            rotated.run();
        }
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
    @Override
    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        dev.fesly.font.Font namefont = Fonts.ROBOTO_REGULAR.get(15);
        Font selectionsfont = Fonts.ROBOTO_REGULAR.get(15);
        float wd = (float) MathUtil.clamp(45, 240, selectionsfont.getWidth(maxString));
        float roundx = x + width - wd - 17;
        float roundy = y - 2;
        float roundwidth = (float) (wd + 16);
        if (expanded) {
            for (int i = 0; i < values.length; i++) {
                if (MathUtil.isHovered(roundx, y + i * (selectionsfont.getHeight()) + 12, roundwidth, selectionsfont.getHeight(), mouseX, mouseY)) {
                    E hovered = values[i];
                    EnumSet<E> set = getter.get();
                    if (set.contains(hovered)) {
                        set.remove(hovered);
                    } else {
                        set.add(hovered);
                    }
                    setter.accept(set);
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
    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }
    @Override
    public void load(JsonElement json) {
        EnumSet<E> set = EnumSet.noneOf(enumClass);
        for (JsonElement entry : json.getAsJsonArray()) set.add(Enum.valueOf(enumClass, entry.getAsString()));
        setter.accept(set);
    }
    @Override
    public JsonElement save() {
        EnumSet<E> set = getter.get();
        JsonArray json = new JsonArray();
        for (Enum<?> entry : set) json.add(entry.name());
        return json;
    }
    @Override
    public int getHeight() {
        return heightAnim + 5;
    }
    @java.lang.SuppressWarnings("all")
    public Supplier<EnumSet<E>> getGetter() {
        return this.getter;
    }
    @java.lang.SuppressWarnings("all")
    public Consumer<EnumSet<E>> getSetter() {
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
    public Animation getAnimation() {
        return this.animation;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getOffset() {
        return this.offset;
    }
}
