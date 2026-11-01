package dev.fesly.impl.value.clientValues;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import lombok.Getter;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class ClientColorSetting extends ClientSetting {
    private final Supplier<Color> getter;
    private final Consumer<Color> setter;
    private boolean draggedMain; // Main - central panel where you can adjust brightness and saturation
    private boolean draggedHue; // Hue - right panel where you can adjust hue

    private boolean expanded;

    public ClientColorSetting(String name, ClientUICategory category, Supplier<Color> getter, Consumer<Color> setter) {
        super(name,  ()-> true,80, category);
        this.getter = getter;
        this.setter = setter;
    }
    public ClientColorSetting(String name, BooleanSupplier visible, ClientUICategory category, Supplier<Color> getter, Consumer<Color> setter) {
        super(name, visible,80, category);
        this.getter = getter;
        this.setter = setter;
    }
    private final Animation expandedAnim = new Animation(Easing.EASE_OUT_CUBIC, 100);
    private final Animation stencilAnim = new Animation(Easing.EASE_OUT_CUBIC, 100);

    private final dev.fesly.impl.util.animation.extended.Animation rq = new EaseBackIn(200,1,1);

    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY, float partialTicks) {

        Color color = getter.get();
        Font font =  Fonts.ROBOTO_REGULAR.get(13);
        Font nfont =  Fonts.ROBOTO_REGULAR.get(15);


        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hP = width - 90;
        RenderSystem.disableTexture();
        nfont.draw(name, x, y, -1);

        float fuller = stencilAnim.getNumberValue().floatValue() / (super.getHeight() + 15);

        int alpha = (int)( (visibleAnimation.getNumberValue().floatValue()) * 235);

        float oldX = x;
        float oldWidth = width;
        y+=2;
        x += 32;
        width -=33;



        stencilAnim.run(expanded ? super.getHeight() + 15 : 25);

        if (expanded && stencilAnim.getNumberValue().floatValue() > (super.getHeight() + 13)) {
            expandedAnim.run(0);
        }
        if (!expanded) {
            expandedAnim.run(0.89f);
        }

        rq.setDirection(expanded);

        float rq = this.rq.getOutput().floatValue();

        float b = 18;
        float wb =(width * 0.91f);
        drawWithOutline(x - 3 + (wb) * expandedAnim.getNumberValue().floatValue(), y - 3,
                width + 3 - wb * expandedAnim.getNumberValue().floatValue(),
                (hP + 22) * fuller - (!expanded ? 2 : -2),
                5,true,alpha);

        StencilUtil.initStencil();
        drawWithOutline(x - 3 + (wb) * expandedAnim.getNumberValue().floatValue(),
                y - 3, width + 3 - wb * expandedAnim.getNumberValue().floatValue(),
                (hP + 22) * fuller - (!expanded ? 2 : -2),5,true,alpha);
        StencilUtil.bindReadStencilBuffer(1);
        //RenderUtil.roundedRectangle(x + width - 16, y, 12, 7, 3, color);

        RenderUtil.roundedRectangle(x + width - 37 + 19 + (2 * rq), y, 42 - 8 - 19 - (3 * rq ), 9, 3, new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha));

        Color black = new Color(0,0,0,alpha);
        Color white = new Color(255,255,255,alpha);

        Color z = Color.getHSBColor(hsb[0] , 1, 1);

        RenderUtil.roundedGradientRectangle(x, y + b - (b) * fuller + (!expanded ? 3 : 0), width - 20, hP + 18, 3,
                black,white
                , black , new Color(z.getRed(),z.getGreen(),z.getBlue(),alpha));
        RenderUtil.drawHueRotation(x + width - 14, y + b - 1, 8, hP, 3.5f,alpha / 255f);

        int px = x + (int) (hsb[1] * (width - 30F));
        int py = (int) (y + b - (b) * fuller + (!expanded ? 3 : 0) + (int) ((1F - hsb[2]) * (hP + 18)));

        //AbstractGui.fill(new MatrixStack(), px - 4, py - 4, px + 4, py + 4, -16777216);
        //AbstractGui.fill(new MatrixStack(), px - 3, py - 3, px + 3, py + 3, -1);

        RenderUtil.roundedRectangle( px - 3, py - 3,6,6,3f,Color.WHITE);

        RenderUtil.roundedOutlineRectangle( px - 3.3f, py - 3.3f,6.6f,6.6f,3f,0.7f,new Color(41,41,41));

        px = x + width - 13;
        py = (int) (y + b + (int) (hsb[0] * hP));

        /*AbstractGui.fill(new MatrixStack(), px - 10, py - 2, px + 11, py + 2, -16777216);
        AbstractGui.fill(new MatrixStack(), px - 9, py - 1, px + 10, py + 1, -1);*/

        /*StencilUtil.uninitStencilBuffer();
        StencilUtil.initStencil();
        RenderUtil.roundedRectangle(oldX + oldWidth * expandedAnim.getNumberValue().floatValue(),y - 4,
                oldWidth + 5,(hP + (expanded ? 47 : 18)) * fuller,0,Color.WHITE);
        StencilUtil.bindReadStencilBuffer(1);
*/
        RenderUtil.roundedRectangle(x + width - 15 - 0.5f, py - 1, 11, 2,0.7f, new Color(color.getRed(),color.getBlue(),color.getRed(),100));
        drawWithOutlineDefault(x + width - 15 - 0.5f, py - 1,
                11, 2,0.7f,true,alpha);

        GlStateManager.enableTexture();

        if (draggedMain) {
            hsb[1] = MathHelper.clamp(MathUtil.normalize(mouseX, x, x + width - 25), 0, 1);
            hsb[2] = 1F - MathHelper.clamp(MathUtil.normalize(mouseY, y + b - (b) * fuller + (!expanded ? 3 : 0),
                    y + b - (b) * fuller + (!expanded ? 3 : 0) + hP + 18), 0, 1);
        }

        if (draggedHue) {
            hsb[0] = MathHelper.clamp(MathUtil.normalize(mouseY, y + b, y + b + hP), 0, 1);
        }

        setter.accept(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
        color = getter.get();

        px = x;
        py = y + height - 10;
        if (expandedAnim.isFinished()) {
            drawWithOutline(x, py - 5, width - 3.5f, 20, 3, true,alpha);

            drawWithOutline(x + 5, py - 5 + 4, 42 - 8, 20 - 8, 3,
                    true,alpha);
            drawWithOutline(x + width - 42, py - 5 + 4, 42 - 8, 20 - 8, 3,
                    true,alpha);

            py += 4;

            font.drawCenterXY("Copy", x + 5 + 16, py, white.getRGB());
            font.drawCenterXY("Paste", x + width - 42 + 16, py, white.getRGB());
        }
        px = x + width - 40;
        StencilUtil.uninitStencilBuffer();

//Gui.drawRect(px + 2, py + 2, px + 13, py + 13, color.getRGB());

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
            RenderUtil.roundedOutlineGradientRectangle(rectX,rectY,rectWidth,rectHeight, radius, 0.5f
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
        if (mouseButton == 1) {
            if (MathUtil.isHovered(x, y, width, 15, mouseX, mouseY)) {
                expanded = !expanded;
            }
        }

        if (!expanded) return;
        if (mouseButton == 0) {
            if (MathUtil.isHovered(x + 5, y, width - 30, width - 75, mouseX, mouseY)) {
                draggedMain = true;
            }
            if (MathUtil.isHovered(x + width - 20, y + 15, 15, width - 90, mouseX, mouseY)) {
                draggedHue = true;
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
        draggedMain = draggedHue = false;
    }

    @Override
    public void load(JsonElement json) {
        setter.accept(new Color(json.getAsInt()));
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getter.get().getRGB());
    }

    @Override
    public int getHeight() {
        if (stencilAnim.getNumberValue().floatValue() < 1) {
            expandedAnim.setValue(1);
            stencilAnim.setValue(30);
        }
        return (int) ((int) stencilAnim.getNumberValue().floatValue() * (expanded ? 0.75f : 0.8f));
    }

}
