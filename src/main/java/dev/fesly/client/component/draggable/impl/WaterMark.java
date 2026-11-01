package dev.fesly.client.component.draggable.impl;
import dev.fesly.Nova;
import dev.fesly.client.component.draggable.Draggable;
import dev.fesly.client.component.draggable.DraggableManager;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.shader.impl.GaussianInstance;
import net.minecraft.util.ResourceLocation;
import java.awt.*;
public class WaterMark extends Draggable {
    public WaterMark() {
        super("watermark", 4, 4, 48, 48);
    }
    @Override
    public boolean isVisible() {
        return true;
    }
    private static final dev.fesly.impl.util.animation.compact.Animation tap = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 155);
    public static float getScaleAnim() {
        return Math.max(1f - tap.getNumberValue().floatValue(),0);
    }
    @Override
    public void drawPre(double mouseX, double mouseY, float partialTicks) {
        boolean type1 = nova.getClientUI().getWaterMarkMode().equals(ClientUI.WaterMarkModeEnum.Type1);
        if (type1) {
            setWidth(30);
            setHeight(30);
        }
        Runnable round = () -> {
            ScaleUtil.scaleStart(x + width / 2.0F, y + height / 2.0F, getScaleAnim());
            if (!type1) RenderUtil.roundedRectangle(x, y, width, height, 5.0F, Color.WHITE);
            else RenderUtil.drawImage(new ResourceLocation("nova", "icons/nova.png"), x - 12, y - 12, width + 18, height + 18, -1);
            ScaleUtil.scaleEnd();
        };
        IAccess.addRunnableWithChecks(round);
    }
    @Override
    public void draw(double mouseX, double mouseY, float partialTicks) {
        boolean type1 = nova.getClientUI().getWaterMarkMode().equals(ClientUI.WaterMarkModeEnum.Type1);
        Color c = ClientUtil.getOneClientColor();
        if (type1) {
            RenderUtil.drawImage(new ResourceLocation("nova","icons/nova.png"),x - 12,y - 12,width + 18,height + 18,
                    new Color(c.getRed(),c.getGreen(),c.getBlue(),255).getRGB());
            return;
        }
        if (type1) return;
        String str = "Nova  ";
        String name = str + "b ";
        if (x > mw.getScaledWidth() /1.8f && x < mw.getScaledWidth() / 2.5f) {
            x = (int) (mw.getScaledWidth() / 2f - getWidth() / 2f);
        }
        Font font = Nova.getInstance().getClientUI().isThinFonts() ? Fonts.MANROPE_REGULAR.get(19.8f) : Fonts.MANROPE_MEDIUM.get(20);
        setHeight(font.getHeight() + 1f);
        setWidth(font.getWidth(name) + 8.5f);
        boolean dragged = DraggableManager.dragged instanceof WaterMark;
        tap.run((dragged && !DraggableManager.draggedTimer.hasReached(150)) ? 0.04f : 0);
        ScaleUtil.scaleStart(x + width / 2f, y + height / 2f,getScaleAnim());
        font.draw(str, x + 15, y, -1);
        RenderUtil.drawImage(new ResourceLocation("nova","icons/ui/smile32.png"),x+ 2, y + 1, height - 2, height - 2, -1);
        ScaleUtil.scaleEnd();
    }
}
