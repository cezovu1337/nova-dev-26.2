package dev.fesly.impl.util.render;

import dev.fesly.impl.interfaces.client.IAccess;
import org.lwjgl.opengl.GL11;

public class ScissorUtil implements IAccess {
    private static final int max = 1000;
    private static final ScissorUtil[] objects = new ScissorUtil[max];
    private static int lastObject = -1;

    private static int index;

    private int left;
    private int right;
    private int top;
    private int bottom;

    public static ScissorUtil prepareScissorTest(float x, float y, float x2, float y2) {
        double factor = mw.getScaleFactor();
        return new ScissorUtil((int) (x * factor), (int) ((mw.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

    public ScissorUtil(int x, int y, int width, int height) {
        lastObject++;
        if (lastObject < max) {
            index = lastObject;
            objects[index] = this;

            left = x;
            right = x + width - 1;
            top = y;
            bottom = y + height - 1;

            if (index > 0) {
                ScissorUtil parent = objects[index - 1];

                if (left < parent.left) left = parent.left;
                if (right > parent.right) right = parent.right;
                if (top < parent.top) top = parent.top;
                if (bottom > parent.bottom) bottom = parent.bottom;
            }

            resume();
        }
    }

    private void resume() {
        GL11.glScissor(left, top, right - left + 1, bottom - top + 1);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public static void destroy() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        objects[index] = null;
        lastObject--;
        if (lastObject > -1)
            objects[lastObject].resume();
    }

}