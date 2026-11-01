package dev.fesly.impl.value.clientValues;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientUiScreen;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.DecelerateAnimation;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
public class ClientBooleanSetting extends ClientSetting {
    private final BooleanSupplier getter;
    private final BooleanConsumer setter;

    public ClientBooleanSetting(String name, ClientUICategory category, BooleanSupplier getter, BooleanConsumer setter) {
        super(name, ()-> true,19, category);
        this.getter = getter;
        this.setter = setter;
    }
    public ClientBooleanSetting(String name,BooleanSupplier visible, ClientUICategory category, BooleanSupplier getter, BooleanConsumer setter) {
        super(name, visible,19, category);
        this.getter = getter;
        this.setter = setter;
    }
    public final Animation animation = new DecelerateAnimation(120, 1);

    public final Animation descriptionAnim = new DecelerateAnimation(220, 254);

    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {
        boolean enabled = getter.getAsBoolean();
        animation.setDirection(enabled);
        float rX = x + width - 38;
        float rectY = y + 1;
        int alpha = (int)( (visibleAnimation.getNumberValue().floatValue()) * 235);
        //if (alpha < 0.05f) return;
        //if (alpha > 254) alpha = 254;
        drawWithOutline(x + width - 26 + 6, y + 1, 19.094858f, 10.379752f, 4.8f, true,alpha);
        drawWithOutlineDefault((float) (rX - (enabled ? 0 : 1) + 3 + (animation.getOutput() * (19.094858f / 2.7f))) + 19.094858f / 2f + 8,
                rectY + 1f, 8.379752f, 8.579752f, 4f, false,alpha);
        Fonts.ROBOTO_REGULAR.get(15).draw(name, x,
                (long)y + 1.15f +
                        Fonts.ROBOTO_REGULAR.get(15).getMiddleOfBox(10.379752f),
                new Color(255,255,255,alpha).getRGB());


        Font font = Fonts.ROBOTO_REGULAR.get(15);
        if (!description.equals("")) {
            drawWithOutline(x + width - 26 + 6 - 11, y + 1 + 1, 10.379752f - 2, 10.379752f - 2, 3.7f, true,alpha);

            RenderUtil.drawImage(new ResourceLocation("nova","icons/ui/questionmark12.png"), x + width - 26 + 6 - 11 + 1, y + 1 + 1 + 1, 10.379752f - 4, 10.379752f - 4,
                    new Color(255,255,255,((int) (visibleAnimation.getNumberValue().floatValue() / getHeight())) * 255).getRGB());

            ClientUiScreen.drawWithOutlineDefault((float) MathUtil.clamp(x,x + width, mouseX - 2),
                    (float) MathUtil.clamp(y, y + height, mouseY - 5),font.getWidth(description) + 3,font.getHeight() + 2, 2,true,descriptionAnim.getOutput().intValue());

            if (descriptionAnim.getOutput() > 10) {
                font.draw(description, (float) MathUtil.clamp(x, x + width, mouseX - 1),
                        (float) MathUtil.clamp(y, y + height, mouseY - 4), new Color(255, 255, 255,
                                (int) MathUtil.clamp(1, 224, descriptionAnim.getOutput().intValue())).getRGB());
            }
        }

        boolean hovered = MathUtil.isHovered(x + width - 26 + 6 - 11, y + 1 + 1, 10.379752f - 2, 10.379752f - 2,mouseX,mouseY);

        descriptionAnim.setDirection(!description.equals("") && hovered && getVisible().getAsBoolean());
    }
    public void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline, int alpha) {
        Color color1 = new Color(255, 255, 255, alpha);
        Color colorb = new Color(239, 252, 255, alpha);
        Color color22 = new Color(242, 250, 255, alpha);

        Color color1N = new Color(255, 255, 255, alpha);

        Color color2 = new Color(255, 255, 255, alpha);

        Color color2B = new Color(255, 255, 255, alpha);


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

    public void drawWithOutline(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline, int alpha) {
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color color1 = colorEnum.getSettingBackgroundColor();
        Color color2 = colorEnum.getSettingBackgroundColor2();

        Color c1n = colorEnum.getSettingBackgroundColor();
        Color color1N = new Color(c1n.getRed(),c1n.getGreen(),c1n.getBlue(),alpha);
        Color c2b = colorEnum.getSettingBackgroundColor2();
        Color color2B = new Color(c2b.getRed(),c2b.getGreen(),c2b.getBlue(),alpha);
        if (outline) {
            Color color1Brighter = color1N.brighter().brighter().brighter();

            Color c2bb = color2.brighter().brighter().brighter();
            Color color2Brighter = new Color(c2bb.getRed(),c2bb.getGreen(),c2bb.getBlue(),alpha);

            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.5f
                    , color1Brighter, color2B
                    , color1Brighter, color2Brighter);
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5f, rectY + 0.5f, rectWidth - 1, rectHeight - 1, radius
                , new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha)
                , new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha)
                , new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha)
                , new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), alpha));
    }

    @Override
    public void mousePressed(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
        setter.accept(!getter.getAsBoolean());
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void load(JsonElement json) {
        setter.accept(json.getAsBoolean());
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getter.getAsBoolean());
    }
}
