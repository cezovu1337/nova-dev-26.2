package dev.fesly.impl.value.secondValues;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.fesly.client.module.api.Module;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.FloatSupplier;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.value.defaultValues.impl.NumberValue;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
public class NumberSetting extends Setting {
    private final FloatSupplier getter;
    private FloatConsumer setter;
    private final String format;
    private float min;
    private float max;
    private boolean dragged;
    private boolean draggedField;


    private final Module module;
    public NumberSetting(String name, Module parent, BooleanSupplier visible, FloatSupplier getter, FloatConsumer setter, String format, float min, float max) {
        super(name, 19,visible);
        this.getter = getter;
        this.setter = setter;
        this.format = format;
        this.min = min;
        this.max = max;
        this.module = parent;
    }
    public NumberSetting(String name, Module parent, FloatSupplier getter, FloatConsumer setter, String format, float min, float max) {
        super(name, 19,() -> true);
        this.getter = getter;
        this.setter = setter;
        this.format = format;
        this.min = min;
        this.max = max;
        this.module = parent;
    }
    @Setter
    private float value = -1;
    public NumberSetting(String name, Module parent, float defaultValue, String format, float min, float max) {
        super(name, 19,() -> true);
        value = defaultValue;
        this.getter = () -> value;
        this.setter = this::setValue;
        this.format = format;
        this.min = min;
        this.max = max;
        this.module = parent;
    }
    public NumberSetting(NumberValue value) {
        super(value.getName(), 19,() -> true);
        this.getter = value::getFloatValue;
        this.setter = value::setValue;
        this.format = "%." + value.getDecimalPlaces() + "f";
        this.min = value.getMin().floatValue();
        this.max = value.getMax().floatValue();
        this.module = value.getParent();
    }
    @Override
    public void load(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("value")) {
            setter.accept(jsonObject.get("value").getAsFloat());
        }
        if (jsonObject.has("min")) {
            this.min = jsonObject.get("min").getAsFloat();
        }
        if (jsonObject.has("max")) {
            this.max = jsonObject.get("max").getAsFloat();
        }
    }

    @Override
    public JsonElement save() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", getter.get());
        jsonObject.addProperty("min", min);
        jsonObject.addProperty("max", max);
        return jsonObject;
    }
    public float getMiddleOfBox(float boxHeight,float fullheight) {
        return boxHeight / 2f - fullheight / 2f;
    }

    public Number getValue() {
        return getter.get();
    }

    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        float value = getter.get();
        int alpha = (int)( (visibleAnimation.getNumberValue().floatValue()) * 235);

        Color textColor = new Color(255,255,255,alpha);


        dev.fesly.font.Font font1 = Fonts.ROBOTO_REGULAR.get(15);
        Font font2 = Fonts.ROBOTO_MEDIUM.get(13f);
        y-=3;
        font1.draw(name, x, y + 1 + font1.getMiddleOfBox(height - 4), textColor.getRGB());
        String formattedValue =  String.format(format, value).replace(",",".");
        float wdth = font2.getWidth("2.5");
        int xb = (int) (x + width - wdth - 3);
        int xx = (int) (x + width - 60 - wdth);


        int yy = (int) (y - 1 + getMiddleOfBox(height,3));
        int wwidth = width - 91;
        float sliderPos = MathUtil.denormalize(MathUtil.normalize(value, min, max), xx + 1, xx + wwidth - 1);
        drawWithOutline(xx + 1.5f,yy, sliderPos - xx + 1, 3,1, true,alpha);

        RenderUtil.roundedRectangle(sliderPos + 0.5f,yy, xx + wwidth - sliderPos + 1, 3, 2, new Color(224, 224, 224,alpha));

        UIColors colorEnum = UIColorUtil.getColorEnum();

        drawWithOutlineDefaultB(xb - wdth,y + 3,2 + wdth * 2,height - 8, 2.55f,true,alpha);


        RenderUtil.roundedRectangle(sliderPos - 3, yy - 1.25f - 0.5f, 5.5f,6f,3f, textColor);
        Color c = colorEnum.getColor1N();
        RenderUtil.roundedOutlineRectangle(sliderPos - 3, yy - 1.25f - 0.5f, 5.5f,6f,2.15f,0.6f,
                new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));


        font2.drawCenterX(TextFormatting.BOLD +  formattedValue, xb - 1 - wdth + (2 + wdth * 2) / 2f, y + 3 + font2.getMiddleOfBox(height - 7f) - 0.5f ,
                textColor.getRGB());



        if (dragged) {
            setter.accept(MathHelper.clamp(MathUtil.denormalize(MathUtil.normalize(mouseX, xx + 1, xx + wwidth - 1), min, max), min, max));
        }
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
                new Color(colorb.getRed(), colorb.getGreen(), colorb.getBlue(), Math.min(alpha, 106)),
                new Color(colorb.getRed(), colorb.getGreen(), colorb.getBlue(), Math.min(alpha, 106)),
                new Color(color22.getRed(), color22.getGreen(), color22.getBlue(), Math.min(alpha, 101)),
                new Color(colorb.getRed(), colorb.getGreen(), colorb.getBlue(), Math.min(alpha, 106)));

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

}
