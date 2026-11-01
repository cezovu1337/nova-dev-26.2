package dev.fesly.impl.value.clientValues;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.DecelerateAnimation;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.GLUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.shader.GradientUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class ClientEnumSetting<E extends Enum<E>> extends ClientSetting {
    private final Supplier<E> getter;
    private final Consumer<E> setter;

    private final Class<E> enumClass;
    private final E[] values;
    private boolean expanded = false;
    public int heightAnim = 15;

    public String selected = "selected..";
    @Getter
    @Setter
    public String old = "selected..";


    public ClientEnumSetting(String name, ClientUICategory category, Supplier<E> getter, Consumer<E> setter, Class<E> enumClass) {
        super(name, () -> true, enumClass.getEnumConstants().length * 10 + 15, category);
        this.getter = getter;
        this.setter = setter;
        this.enumClass = enumClass;
        values = enumClass.getEnumConstants();
    }

    public ClientEnumSetting(String name, BooleanSupplier visible, ClientUICategory category, Supplier<E> getter, Consumer<E> setter, Class<E> enumClass) {
        super(name, visible, enumClass.getEnumConstants().length * 10 + 15, category);
        this.getter = getter;
        this.setter = setter;
        this.enumClass = enumClass;
        values = enumClass.getEnumConstants();
    }
    private int bgalpha;

    /**Arrow rotate */
    private int rotateAnim;
    private int expandedx;
    private int expandedy;
    /**Arrow rotate */

    private String maxString = "ы";
    public final Animation animation = new DecelerateAnimation(120, 180);
    public final Animation offset = new DecelerateAnimation(120, 1);

    public int rOld,rOld2 = 255;
    public int gOld,gOld2 = 255;
    public int bOld,bOld2 = 255;
    public int rNow,rNow2 = 255;
    public int gNow,gNow2 = 255;
    public int bNow,bNow2 = 255;



    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        dev.fesly.font.Font namefont = Fonts.ROBOTO_REGULAR.get(15);
        dev.fesly.font.Font selectionsfont = Fonts.ROBOTO_REGULAR.get(15);
        E value = getter.get();
        for (int i = 0; i < values.length; i++) {
            String name = values[i].toString();
            if (name.length() > maxString.length()) {
                maxString = name;
            }
        }
        int valueheight = !expanded ? selectionsfont.getHeight() + 4 : (int) (enumClass.getEnumConstants().length * ((selectionsfont.getHeight())) + 21);
        this.heightAnim = (int) MathUtil.lerp(heightAnim, valueheight, 2f/10);


        String ofname = Objects.requireNonNull(getter.get().name());
        float roundx = x + width - selectionsfont.getWidth(maxString) - 17;
        float roundy = y - 2;
        float roundwidth = selectionsfont.getWidth(maxString) + 19;
        float roundheight = this.heightAnim;
        boolean roud = (mouseX > roundx && mouseX < roundx + roundwidth && mouseY > roundy && mouseY < roundy + roundheight) || expanded;
        drawWithOutlineDefault(roundx,roundy, roundwidth, roundheight,3F,true);
        float alpha = roud ? 140 : 100;
        this.bgalpha= (int) MathUtil.animation(this.bgalpha, alpha, 2);
        offset.setDirection(expanded);
        if (expanded) {
            Color c = new Color(254, 254, 254,heightAnim);
            AbstractGui.fill(new MatrixStack(), (int) (roundx + 2), (int) (roundy + 10), (int) (roundx + roundwidth - 4), (int) (roundy + 10.5),c.getRGB());
        }
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color c = colorEnum.getBackgroundColor2();
        Color c2 = colorEnum.getBackgroundColor();

        Color textColor = new Color(255,255,255, (int) (offset.getOutput().floatValue() * 250));
        Color textColorOne = new Color(-1);

        namefont.draw(name, x, y, -1);
        Color alphab = new Color(255,255,255,1);
        Color noalpha = new Color(255,255,255,255);
        GradientUtil.applyGradient(roundx, roundy, roundwidth - 8,10,1f,
                noalpha,noalpha,
                alphab,alphab,
                () -> namefont.draw(ofname,roundx + 2,roundy +  namefont.getMiddleOfBox(selectionsfont.getHeight() + 4),-1)
        );

        Color w = Color.WHITE;

        rNow = MathUtil.lerp(rNow,c.getRed(),0.3f);
        gNow = MathUtil.lerp(gNow,c.getGreen(),0.3f);
        bNow = MathUtil.lerp(bNow,c.getBlue(),0.3f);
        rNow2 = MathUtil.lerp(rNow2,c2.getRed(),0.3f);
        gNow2 = MathUtil.lerp(gNow2,c2.getGreen(),0.3f);
        bNow2 = MathUtil.lerp(bNow2,c2.getBlue(),0.3f);

        rOld = MathUtil.lerp(rOld,w.getRed(),0.02f);
        gOld = MathUtil.lerp(gOld,w.getGreen(),0.02f);
        bOld = MathUtil.lerp(bOld,w.getBlue(),0.02f);
        rOld2 = MathUtil.lerp(rOld2,w.getRed(),0.02f);
        gOld2 = MathUtil.lerp(gOld2,w.getGreen(),0.02f);
        bOld2 = MathUtil.lerp(bOld2,w.getBlue(),0.02f);




        Color nowC = new Color(rNow, gNow, bNow,255);
        Color nowC2 = new Color(rNow2, gNow2, bNow2,255);
        Color oldC = new Color(rOld, gOld, bOld,255);
        Color oldC2 = new Color(rOld2, gOld2, bOld2,255);


        if (roundheight > 25) {
            StencilUtil.initStencil();
            RenderUtil.roundedRectangle(roundx, roundy, roundwidth, roundheight, 1F, new Color(31, 31, 31, bgalpha));
            StencilUtil.bindReadStencilBuffer(1);
            for (int i = 0; i < values.length; i++) {
                int finalI = i;
                if (value == values[i]) {
                    GradientUtil.applyGradient(roundx + 2,
                            y - ((int) ((1f - offset.getOutput().floatValue()) * 5)) + i * (selectionsfont.getHeight() * (offset.getOutput().floatValue())) + 12,
                            selectionsfont.getWidth(values[i].toString()), selectionsfont.getHeight(),1f,
                            nowC   ,nowC ,
                            nowC2,nowC2,() ->
                    selectionsfont.draw(values[finalI].toString(), roundx + 2,
                            y - ((int) ((1f - offset.getOutput().floatValue()) * 5)) + finalI * (selectionsfont.getHeight() * (offset.getOutput().floatValue())) + 12,
                            value == values[finalI] ? new Color(rNow, gNow, bNow, (int) (offset.getOutput().floatValue() * 250)).getRGB() : textColor.getRGB())
                    );

                }else if (values[i].toString() == old) {
                    GradientUtil.applyGradient(roundx + 2,
                            y - ((int) ((1f - offset.getOutput().floatValue()) * 5)) + i * (selectionsfont.getHeight() * (offset.getOutput().floatValue())) + 12,
                            selectionsfont.getWidth(values[i].toString()), selectionsfont.getHeight(),1f,
                            oldC   ,oldC ,
                            oldC2,oldC2,() ->
                                    selectionsfont.draw(values[finalI].toString(), roundx + 2,
                                            y - ((int) ((1f - offset.getOutput().floatValue()) * 5)) + finalI * (selectionsfont.getHeight() * (offset.getOutput().floatValue())) + 12,
                                            value == values[finalI] ? new Color(rNow, gNow, bNow, (int) (offset.getOutput().floatValue() * 250)).getRGB() : textColor.getRGB())
                    );
                } else
                    selectionsfont.draw(values[i].toString(), roundx + 2,
                            y - ((int) ((1f - offset.getOutput().floatValue()) * 5)) + i * (selectionsfont.getHeight() * (offset.getOutput().floatValue())) + 12,
                            textColor.getRGB());
            }
            StencilUtil.uninitStencilBuffer();
        }

        int valuerotate = !expanded ? 0 : -180;
        int valuex = !expanded ? 0 : 5;
        int valuey = !expanded ? -40 + 30 : 2;

        this.rotateAnim = (int) MathUtil.lerp(rotateAnim, valuerotate, 2f/2);
        this.expandedx = (int) MathUtil.lerp(expandedx, valuex, 2f/2);
        this.expandedy = (int) MathUtil.lerp(expandedy, valuey, 2f/2);

        Color finalTextColor = textColor;
        Runnable rotateComponent = new Runnable() {
            @Override
            public void run() {
                RenderUtil.drawImage(new ResourceLocation("nova","icons/ui/arrowm.png"), (int) (roundx + roundwidth - 9), (int) (roundy + 4),5,5, new Color(240,240,240).getRGB());
            }
        };

        if (expanded) animation.setDirection(Direction.FORWARDS);
        else animation.setDirection(Direction.BACKWARDS);

        Runnable rotated = () -> GLUtil.rotate( (roundx + roundwidth - 6.5f), (int) (roundy + 6.8f),animation.getOutput().floatValue(),rotateComponent);
        {
            rotated.run();
        }
    }

    @Override
    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        dev.fesly.font.Font namefont = Fonts.ROBOTO_REGULAR.get(15);
        Font selectionsfont = Fonts.ROBOTO_REGULAR.get(15);
        if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + namefont.getHeight()) {
            expanded = !expanded;
        }
        if (expanded) {
            float roundx = x + width - selectionsfont.getWidth(maxString) - 20;
            float roundy = y - 2;
            float roundwidth = selectionsfont.getWidth(maxString) + 16;
            float roundheight = this.heightAnim;
            for (int i = 0; i < values.length; i++) {
                if (MathUtil.isHovered(roundx, y + i * (selectionsfont.getHeight()) + 12,roundwidth,selectionsfont.getHeight(),mouseX,mouseY)) {
                    setter.accept(values[i]);
                    rOld = rNow;
                    gOld = gNow;
                    bOld = bNow;
                    rOld2 = rNow2;
                    gOld2 = gNow2;
                    bOld2 = bNow2;
                    if (values[i].name() != selected) {
                        Color c = Color.WHITE;
                        rNow = c.getRed();
                        gNow = c.getGreen();
                        bNow = c.getBlue();
                        rNow2 = c.getRed();
                        gNow2 = c.getGreen();
                        bNow2 = c.getBlue();
                        old = selected;
                    }




                    selected = values[i].name();
                }
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius,boolean outline) {
        Color color1 = new Color(255, 255, 255, 65);
        Color color2 = new Color(255, 255, 255, 0);
        Color color1A = new Color(255, 255, 255, 245);
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX,rectY,rectWidth,rectHeight, radius, 0.5f
                    , color1A.brighter().brighter(), color2.brighter().brighter()
                    , color1A.brighter().brighter(), color1A.brighter().brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius
                , color1, color1
                , color2, color1);
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
}
