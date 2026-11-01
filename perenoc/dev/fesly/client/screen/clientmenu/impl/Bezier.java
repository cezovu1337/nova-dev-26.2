package dev.fesly.client.screen.clientmenu.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.client.screen.IScreen;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.SmoothStepAnimation;
import dev.fesly.impl.util.render.GUIUtil;
import dev.fesly.impl.util.render.RenderUtil;
import lombok.Data;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@Data
public class Bezier implements IAccess, IScreen {

    private Vector2f position;
    private Vector2f startPosition = new Vector2f();
    private Vector2f controlPosition1;
    private Vector2f controlPosition2;
    private Vector2f endPosition = new Vector2f();

    private boolean drag, dragC1, dragC2;
    private double prevX;
    private double prevY;
    private double prevCX1, prevCY1;
    private double prevCX2, prevCY2;

    private Animation animationC1 = new SmoothStepAnimation(250, 255);
    private Animation animationC2 = new SmoothStepAnimation(250, 255);

    public Bezier(Vector2f position) {
        this.position = position;

        this.controlPosition1 = new Vector2f(position.x + 55, position.y + 55 - 20);
        this.controlPosition2 = new Vector2f(position.x + 190 - 50, position.y + 190 - 50 + 20);
    }

    @Override
    public void init() {

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        // dragging
        dragging(mouseX, mouseY);

        float x = position.x;
        float y = position.y;

        float width = 200;
        float height = 225;

        startPosition.x = x + 5;
        startPosition.y = y + 5;
        endPosition.x = startPosition.x + width - 10;
        endPosition.y = startPosition.y + height - 10;

        RenderUtil.glowRoundedRectangle(matrixStack, x, y, width, height, 3, new Color(0, 0, 0, 150), 3, 1);
        RenderUtil.roundedOutlineRectangle(startPosition.x, startPosition.y, width - 10, width - 10, 0.5, 1, new Color(255, 255, 255));

        Fonts.ROBOTO_REGULAR.get(20).draw("Bezier Curve Panel", startPosition.x, y + height - 17.5, -1);

        // стартовые точки
        float x1 = startPosition.x + 1, y1 = startPosition.y + 1;
        // управляемые точки
        float cx1 = controlPosition1.x, cy1 = controlPosition1.y;
        float cx2 = controlPosition2.x, cy2 = controlPosition2.y;
        // конченные точки
        endPosition.y = startPosition.y + width - 10;
        float x2 = endPosition.x - 1, y2 = endPosition.y - 1;

        // хуйня чтобы точки за которые хватаешься за края не вылезали, да
        cx1 = MathHelper.clamp(cx1, startPosition.x + 5, endPosition.x - 5);
        cy1 = MathHelper.clamp(cy1, startPosition.y + 5, endPosition.y - 5);
        controlPosition1.x = MathHelper.clamp(controlPosition1.x, startPosition.x + 5, endPosition.x - 5);
        controlPosition1.y = MathHelper.clamp(controlPosition1.y, startPosition.y + 5, endPosition.y - 5);
        cx2 = MathHelper.clamp(cx2, startPosition.x + 5, endPosition.x - 5);
        cy2 = MathHelper.clamp(cy2, startPosition.y + 5, endPosition.y - 5);
        controlPosition2.x = MathHelper.clamp(controlPosition2.x, startPosition.x + 5, endPosition.x - 5);
        controlPosition2.y = MathHelper.clamp(controlPosition2.y, startPosition.y + 5, endPosition.y - 5);

        // start dot
        RenderUtil.drawLine(x1, y1, cx1, cy1, new Color(255, 255, 255));
        RenderUtil.roundedRectangle(cx1 - 1.5, cy1 - 1.5, 3, 3, 1.5, new Color(255, 255, 255));
        if (GUIUtil.mouseOver(cx1 - 5, cy1 - 5, 10, 10, mouseX, mouseY)) {
            animationC1.setDirection(Direction.FORWARDS);
        } else {
            animationC1.setDirection(Direction.BACKWARDS);
        }
        RenderUtil.roundedOutlineRectangle(cx1 - 5, cy1 - 5, 10, 10, 1, 0.8, new Color(255, 255, 255, animationC1.getOutput().intValue()));

        // end dot
        RenderUtil.drawLine(x2, y2, cx2, cy2, new Color(255, 255, 255));
        RenderUtil.roundedRectangle(cx2 - 1.5, cy2 - 1.5, 3, 3, 1.5, new Color(255, 255, 255));
        if (GUIUtil.mouseOver(cx2 - 5, cy2 - 5, 10, 10, mouseX, mouseY)) {
            animationC2.setDirection(Direction.FORWARDS);
        } else {
            animationC2.setDirection(Direction.BACKWARDS);
        }
        RenderUtil.roundedOutlineRectangle(cx2 - 5, cy2 - 5, 10, 10, 1, 0.5, new Color(255, 255, 255, animationC2.getOutput().intValue()));

        // curve
        GlStateManager.pushMatrix();
        RenderUtil.start();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (double t = 0; t <= 1; t += 0.01) {
            double lX = Math.pow((1 - t), 3) * x1 + 3 * Math.pow((1 - t), 2) * t * cx1 + 3 * (1 - t) * Math.pow(t, 2) * cx2 + Math.pow(t, 3) * x2;
            double lY = Math.pow((1 - t), 3) * y1 + 3 * Math.pow((1 - t), 2) * t * cy1 + 3 * (1 - t) * Math.pow(t, 2) * cy2 + Math.pow(t, 3) * y2;
            lX = MathHelper.clamp(lX, startPosition.x, endPosition.x);
            lY = MathHelper.clamp(lY, startPosition.y, endPosition.y);
            Color color = Color.WHITE;
            RenderUtil.color(color, color.getAlpha());
            GL11.glVertex2d(lX, lY);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderUtil.stop();
        GlStateManager.popMatrix();

    }

    public void dragging(int mouseX, int mouseY) {
        if (drag) {
            position.x = (float) (mouseX + prevX);
            position.y = (float) (mouseY + prevY);
            controlPosition1.x = (float) (mouseX + prevCX1);
            controlPosition1.y = (float) (mouseY + prevCY1);
            controlPosition2.x = (float) (mouseX + prevCX2);
            controlPosition2.y = (float) (mouseY + prevCY2);
        }
        if (dragC1) {
            controlPosition1.x = (float) (mouseX + prevCX1);
            controlPosition1.y = (float) (mouseY + prevCY1);
        }
        if (dragC2) {
            controlPosition2.x = (float) (mouseX + prevCX2);
            controlPosition2.y = (float) (mouseY + prevCY2);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float width = 200;
            float height = 225;
            if (GUIUtil.mouseOver(position.x, position.y + height - 30, width, 30, mouseX, mouseY)) {
                drag = true;
                prevX = position.x - mouseX;
                prevY = position.y - mouseY;
                prevCX1 = controlPosition1.x - mouseX;
                prevCY1 = controlPosition1.y - mouseY;
                prevCX2 = controlPosition2.x - mouseX;
                prevCY2 = controlPosition2.y - mouseY;
            }
            if (!dragC2 && GUIUtil.mouseOver(controlPosition1.x - 5, controlPosition1.y - 5, 10, 10, mouseX, mouseY)) {
                dragC1 = true;
                prevCX1 = controlPosition1.x - mouseX;
                prevCY1 = controlPosition1.y - mouseY;
            }
            if (!dragC1 && GUIUtil.mouseOver(controlPosition2.x - 5, controlPosition2.y - 5, 10, 10, mouseX, mouseY)) {
                dragC2 = true;
                prevCX2 = controlPosition2.x - mouseX;
                prevCY2 = controlPosition2.y - mouseY;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        drag = false;
        dragC1 = false;
        dragC2 = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void update() {

    }

    @Override
    public void bloom() {

    }

    @Override
    public void onClose() {
        drag = false;
        dragC1 = false;
        dragC2 = false;
    }
}