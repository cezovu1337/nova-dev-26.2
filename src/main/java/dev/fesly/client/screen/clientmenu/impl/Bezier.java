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
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import java.awt.*;
public class Bezier implements IAccess, IScreen {
    private Vector2f position;
    private Vector2f startPosition = new Vector2f();
    private Vector2f controlPosition1;
    private Vector2f controlPosition2;
    private Vector2f endPosition = new Vector2f();
    private boolean drag;
    private boolean dragC1;
    private boolean dragC2;
    private double prevX;
    private double prevY;
    private double prevCX1;
    private double prevCY1;
    private double prevCX2;
    private double prevCY2;
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
        float x1 = startPosition.x + 1;
        float y1 = startPosition.y + 1;
        float cx1 = controlPosition1.x;
        float cy1 = controlPosition1.y;
        float cx2 = controlPosition2.x;
        float cy2 = controlPosition2.y;
        endPosition.y = startPosition.y + width - 10;
        float x2 = endPosition.x - 1;
        float y2 = endPosition.y - 1;
        cx1 = MathHelper.clamp(cx1, startPosition.x + 5, endPosition.x - 5);
        cy1 = MathHelper.clamp(cy1, startPosition.y + 5, endPosition.y - 5);
        controlPosition1.x = MathHelper.clamp(controlPosition1.x, startPosition.x + 5, endPosition.x - 5);
        controlPosition1.y = MathHelper.clamp(controlPosition1.y, startPosition.y + 5, endPosition.y - 5);
        cx2 = MathHelper.clamp(cx2, startPosition.x + 5, endPosition.x - 5);
        cy2 = MathHelper.clamp(cy2, startPosition.y + 5, endPosition.y - 5);
        controlPosition2.x = MathHelper.clamp(controlPosition2.x, startPosition.x + 5, endPosition.x - 5);
        controlPosition2.y = MathHelper.clamp(controlPosition2.y, startPosition.y + 5, endPosition.y - 5);
        RenderUtil.drawLine(x1, y1, cx1, cy1, new Color(255, 255, 255));
        RenderUtil.roundedRectangle(cx1 - 1.5, cy1 - 1.5, 3, 3, 1.5, new Color(255, 255, 255));
        if (GUIUtil.mouseOver(cx1 - 5, cy1 - 5, 10, 10, mouseX, mouseY)) {
            animationC1.setDirection(Direction.FORWARDS);
        } else {
            animationC1.setDirection(Direction.BACKWARDS);
        }
        RenderUtil.roundedOutlineRectangle(cx1 - 5, cy1 - 5, 10, 10, 1, 0.8, new Color(255, 255, 255, animationC1.getOutput().intValue()));
        RenderUtil.drawLine(x2, y2, cx2, cy2, new Color(255, 255, 255));
        RenderUtil.roundedRectangle(cx2 - 1.5, cy2 - 1.5, 3, 3, 1.5, new Color(255, 255, 255));
        if (GUIUtil.mouseOver(cx2 - 5, cy2 - 5, 10, 10, mouseX, mouseY)) {
            animationC2.setDirection(Direction.FORWARDS);
        } else {
            animationC2.setDirection(Direction.BACKWARDS);
        }
        RenderUtil.roundedOutlineRectangle(cx2 - 5, cy2 - 5, 10, 10, 1, 0.5, new Color(255, 255, 255, animationC2.getOutput().intValue()));
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
    @java.lang.SuppressWarnings("all")
    public Vector2f getPosition() {
        return this.position;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2f getStartPosition() {
        return this.startPosition;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2f getControlPosition1() {
        return this.controlPosition1;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2f getControlPosition2() {
        return this.controlPosition2;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2f getEndPosition() {
        return this.endPosition;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isDrag() {
        return this.drag;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isDragC1() {
        return this.dragC1;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isDragC2() {
        return this.dragC2;
    }
    @java.lang.SuppressWarnings("all")
    public double getPrevX() {
        return this.prevX;
    }
    @java.lang.SuppressWarnings("all")
    public double getPrevY() {
        return this.prevY;
    }
    @java.lang.SuppressWarnings("all")
    public double getPrevCX1() {
        return this.prevCX1;
    }
    @java.lang.SuppressWarnings("all")
    public double getPrevCY1() {
        return this.prevCY1;
    }
    @java.lang.SuppressWarnings("all")
    public double getPrevCX2() {
        return this.prevCX2;
    }
    @java.lang.SuppressWarnings("all")
    public double getPrevCY2() {
        return this.prevCY2;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getAnimationC1() {
        return this.animationC1;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getAnimationC2() {
        return this.animationC2;
    }
    @java.lang.SuppressWarnings("all")
    public void setPosition(final Vector2f position) {
        this.position = position;
    }
    @java.lang.SuppressWarnings("all")
    public void setStartPosition(final Vector2f startPosition) {
        this.startPosition = startPosition;
    }
    @java.lang.SuppressWarnings("all")
    public void setControlPosition1(final Vector2f controlPosition1) {
        this.controlPosition1 = controlPosition1;
    }
    @java.lang.SuppressWarnings("all")
    public void setControlPosition2(final Vector2f controlPosition2) {
        this.controlPosition2 = controlPosition2;
    }
    @java.lang.SuppressWarnings("all")
    public void setEndPosition(final Vector2f endPosition) {
        this.endPosition = endPosition;
    }
    @java.lang.SuppressWarnings("all")
    public void setDrag(final boolean drag) {
        this.drag = drag;
    }
    @java.lang.SuppressWarnings("all")
    public void setDragC1(final boolean dragC1) {
        this.dragC1 = dragC1;
    }
    @java.lang.SuppressWarnings("all")
    public void setDragC2(final boolean dragC2) {
        this.dragC2 = dragC2;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrevX(final double prevX) {
        this.prevX = prevX;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrevY(final double prevY) {
        this.prevY = prevY;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrevCX1(final double prevCX1) {
        this.prevCX1 = prevCX1;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrevCY1(final double prevCY1) {
        this.prevCY1 = prevCY1;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrevCX2(final double prevCX2) {
        this.prevCX2 = prevCX2;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrevCY2(final double prevCY2) {
        this.prevCY2 = prevCY2;
    }
    @java.lang.SuppressWarnings("all")
    public void setAnimationC1(final Animation animationC1) {
        this.animationC1 = animationC1;
    }
    @java.lang.SuppressWarnings("all")
    public void setAnimationC2(final Animation animationC2) {
        this.animationC2 = animationC2;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Bezier)) return false;
        final Bezier other = (Bezier) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.isDrag() != other.isDrag()) return false;
        if (this.isDragC1() != other.isDragC1()) return false;
        if (this.isDragC2() != other.isDragC2()) return false;
        if (java.lang.Double.compare(this.getPrevX(), other.getPrevX()) != 0) return false;
        if (java.lang.Double.compare(this.getPrevY(), other.getPrevY()) != 0) return false;
        if (java.lang.Double.compare(this.getPrevCX1(), other.getPrevCX1()) != 0) return false;
        if (java.lang.Double.compare(this.getPrevCY1(), other.getPrevCY1()) != 0) return false;
        if (java.lang.Double.compare(this.getPrevCX2(), other.getPrevCX2()) != 0) return false;
        if (java.lang.Double.compare(this.getPrevCY2(), other.getPrevCY2()) != 0) return false;
        final java.lang.Object this$position = this.getPosition();
        final java.lang.Object other$position = other.getPosition();
        if (this$position == null ? other$position != null : !this$position.equals(other$position)) return false;
        final java.lang.Object this$startPosition = this.getStartPosition();
        final java.lang.Object other$startPosition = other.getStartPosition();
        if (this$startPosition == null ? other$startPosition != null : !this$startPosition.equals(other$startPosition)) return false;
        final java.lang.Object this$controlPosition1 = this.getControlPosition1();
        final java.lang.Object other$controlPosition1 = other.getControlPosition1();
        if (this$controlPosition1 == null ? other$controlPosition1 != null : !this$controlPosition1.equals(other$controlPosition1)) return false;
        final java.lang.Object this$controlPosition2 = this.getControlPosition2();
        final java.lang.Object other$controlPosition2 = other.getControlPosition2();
        if (this$controlPosition2 == null ? other$controlPosition2 != null : !this$controlPosition2.equals(other$controlPosition2)) return false;
        final java.lang.Object this$endPosition = this.getEndPosition();
        final java.lang.Object other$endPosition = other.getEndPosition();
        if (this$endPosition == null ? other$endPosition != null : !this$endPosition.equals(other$endPosition)) return false;
        final java.lang.Object this$animationC1 = this.getAnimationC1();
        final java.lang.Object other$animationC1 = other.getAnimationC1();
        if (this$animationC1 == null ? other$animationC1 != null : !this$animationC1.equals(other$animationC1)) return false;
        final java.lang.Object this$animationC2 = this.getAnimationC2();
        final java.lang.Object other$animationC2 = other.getAnimationC2();
        if (this$animationC2 == null ? other$animationC2 != null : !this$animationC2.equals(other$animationC2)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Bezier;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isDrag() ? 79 : 97);
        result = result * PRIME + (this.isDragC1() ? 79 : 97);
        result = result * PRIME + (this.isDragC2() ? 79 : 97);
        final long $prevX = java.lang.Double.doubleToLongBits(this.getPrevX());
        result = result * PRIME + (int) ($prevX >>> 32 ^ $prevX);
        final long $prevY = java.lang.Double.doubleToLongBits(this.getPrevY());
        result = result * PRIME + (int) ($prevY >>> 32 ^ $prevY);
        final long $prevCX1 = java.lang.Double.doubleToLongBits(this.getPrevCX1());
        result = result * PRIME + (int) ($prevCX1 >>> 32 ^ $prevCX1);
        final long $prevCY1 = java.lang.Double.doubleToLongBits(this.getPrevCY1());
        result = result * PRIME + (int) ($prevCY1 >>> 32 ^ $prevCY1);
        final long $prevCX2 = java.lang.Double.doubleToLongBits(this.getPrevCX2());
        result = result * PRIME + (int) ($prevCX2 >>> 32 ^ $prevCX2);
        final long $prevCY2 = java.lang.Double.doubleToLongBits(this.getPrevCY2());
        result = result * PRIME + (int) ($prevCY2 >>> 32 ^ $prevCY2);
        final java.lang.Object $position = this.getPosition();
        result = result * PRIME + ($position == null ? 43 : $position.hashCode());
        final java.lang.Object $startPosition = this.getStartPosition();
        result = result * PRIME + ($startPosition == null ? 43 : $startPosition.hashCode());
        final java.lang.Object $controlPosition1 = this.getControlPosition1();
        result = result * PRIME + ($controlPosition1 == null ? 43 : $controlPosition1.hashCode());
        final java.lang.Object $controlPosition2 = this.getControlPosition2();
        result = result * PRIME + ($controlPosition2 == null ? 43 : $controlPosition2.hashCode());
        final java.lang.Object $endPosition = this.getEndPosition();
        result = result * PRIME + ($endPosition == null ? 43 : $endPosition.hashCode());
        final java.lang.Object $animationC1 = this.getAnimationC1();
        result = result * PRIME + ($animationC1 == null ? 43 : $animationC1.hashCode());
        final java.lang.Object $animationC2 = this.getAnimationC2();
        result = result * PRIME + ($animationC2 == null ? 43 : $animationC2.hashCode());
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "Bezier(position=" + this.getPosition() + ", startPosition=" + this.getStartPosition() + ", controlPosition1=" + this.getControlPosition1() + ", controlPosition2=" + this.getControlPosition2() + ", endPosition=" + this.getEndPosition() + ", drag=" + this.isDrag() + ", dragC1=" + this.isDragC1() + ", dragC2=" + this.isDragC2() + ", prevX=" + this.getPrevX() + ", prevY=" + this.getPrevY() + ", prevCX1=" + this.getPrevCX1() + ", prevCY1=" + this.getPrevCY1() + ", prevCX2=" + this.getPrevCX2() + ", prevCY2=" + this.getPrevCY2() + ", animationC1=" + this.getAnimationC1() + ", animationC2=" + this.getAnimationC2() + ")";
    }
}
