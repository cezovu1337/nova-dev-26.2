package dev.fesly.impl.value.clientValues;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.util.math.FloatSupplier;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
public class ClientNumberSetting extends ClientSetting {
    private final FloatSupplier getter;
    private final FloatConsumer setter;
    private final String format;
    private final float min;
    private final float max;
    private boolean dragged;

    public ClientNumberSetting(String name, ClientUICategory category, FloatSupplier getter, FloatConsumer setter, String format, float min, float max) {
        super(name,  ()-> true,17, category);
        this.getter = getter;
        this.setter = setter;
        this.format = format;
        this.min = min;
        this.max = max;
    }
    public ClientNumberSetting(String name,BooleanSupplier visible, ClientUICategory category, FloatSupplier getter, FloatConsumer setter, String format, float min, float max) {
        super(name, visible,17, category);
        this.getter = getter;
        this.setter = setter;
        this.format = format;
        this.min = min;
        this.max = max;
    }

    public float getMiddleOfBox(float boxHeight,float fullheight) {
        return boxHeight / 2f - fullheight / 2f;
    }

    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        float value = getter.get();
        int alpha = (int)( (visibleAnimation.getNumberValue().floatValue()) * 235);

        Color textColor = new Color(255,255,255,alpha);

        dev.fesly.font.Font font1 = Fonts.ROBOTO_REGULAR.get(15);
        Font font2 = Fonts.ROBOTO_MEDIUM.get(12);
        font1.draw(name, x, y + 1 + font1.getMiddleOfBox(10.379752f), textColor.getRGB());
        String formattedValue = String.format(format, value);
        int xx = x + width - 45;
        int yy = (int) (y - 1 + getMiddleOfBox(height,3));
        int wwidth = width - 91;
        float sliderPos = MathUtil.denormalize(MathUtil.normalize(value, min, max), xx + 1, xx + wwidth - 1);
        drawWithOutline(xx + 1.5f,yy - 1, sliderPos - xx + 1, 3,1, true,alpha);
        RenderUtil.roundedRectangle(sliderPos + 0.5f,yy -1, xx + wwidth - sliderPos + 1, 3, 1, new Color(224, 224, 224,alpha));
        RenderUtil.roundedRectangle(sliderPos - 3, yy - 1.8f, 4.5f,4.5f,2,textColor);
        font2.draw(TextFormatting.BOLD +  formattedValue,  Math.max(xx,sliderPos - 2 - font2.getWidth(formattedValue) * 0.8f), y - 3, textColor.getRGB());

        if (dragged) {
            setter.accept(MathHelper.clamp(MathUtil.denormalize(MathUtil.normalize(mouseX, xx + 1, xx + wwidth - 1), min, max), min, max));
        }
    }
    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline,int alpha) {
        Color color1 = new Color(255, 255, 255, 65);
        Color color2 = new Color(255, 255, 255, 0);
        Color color1A = new Color(255, 255, 255, 245);

        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5f,
                    new Color(color1A.getRed(), color1A.getGreen(), color1A.getBlue(), alpha),
                    new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha),
                    new Color(color1A.getRed(), color1A.getGreen(), color1A.getBlue(), alpha),
                    new Color(color1A.getRed(), color1A.getGreen(), color1A.getBlue(), alpha));
        }

        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius,
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha),
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha),
                new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha),
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha));
    }

    public void drawWithOutlineDefaultB(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline,int alpha) {
        Color color1 = new Color(255, 255, 255, 65);
        Color colorb = new Color(239, 252, 255, 106);
        Color color22 = new Color(242, 250, 255, 101);
        Color color1N = new Color(255, 255, 255, 255);
        Color color2 = new Color(255, 255, 255, 60);
        Color color2B = new Color(255, 255, 255, 120);

        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius,
                new Color(colorb.getRed(), colorb.getGreen(), colorb.getBlue(), alpha),
                new Color(colorb.getRed(), colorb.getGreen(), colorb.getBlue(), alpha),
                new Color(color22.getRed(), color22.getGreen(), color22.getBlue(), alpha),
                new Color(colorb.getRed(), colorb.getGreen(), colorb.getBlue(), alpha));

        if (outline) {
            Color color1Brighter = new Color(color1N.getRed(), color1N.getGreen(), color1N.getBlue(), alpha).brighter().brighter().brighter();
            Color color2Brighter = new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha).brighter().brighter().brighter();
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5f,
                    color1Brighter, new Color(color2B.getRed(), color2B.getGreen(), color2B.getBlue(), alpha),
                    color1Brighter, color2Brighter);
        }
    }

    public void drawWithOutlineDefaultFullAlpha(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline,int alpha) {
        Color color1 = new Color(233, 255, 235, 255);
        Color color2 = new Color(255, 255, 255, 0);
        Color color1A = new Color(236, 255, 253, 245);

        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5f,
                    new Color(color1A.getRed(), color1A.getGreen(), color1A.getBlue(), alpha).brighter().brighter(),
                    new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha).brighter().brighter(),
                    new Color(color1A.getRed(), color1A.getGreen(), color1A.getBlue(), alpha).brighter().brighter(),
                    new Color(color1A.getRed(), color1A.getGreen(), color1A.getBlue(), alpha).brighter().brighter());
        }

        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius,
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha),
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha),
                new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha),
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha));
    }

    public void drawWithOutline(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline,int alpha) {
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color color1 = colorEnum.getSettingBackgroundColor();
        Color color2 = colorEnum.getSettingBackgroundColor2();

        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.6f,
                    new Color(1, 1, 1, 0),
                    new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha).brighter(),
                    new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha).brighter(),
                    new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha).brighter(),
                    new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha).brighter());
        }

        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius,
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha),
                new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha),
                new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha),
                new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha));
    }


    @Override
    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        dragged = true;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
        dragged = false;
    }

    @Override
    public void load(JsonElement json) {
        setter.accept(json.getAsFloat());
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getter.get());
    }

}
