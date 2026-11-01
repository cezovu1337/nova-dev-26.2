package dev.fesly.impl.util.render;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.time.TimerUtil;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFWScrollCallback;
import java.awt.*;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
public class ScrollUtil implements IAccess {
    public double target;
    public double scroll;
    public double max = 25;
    public TimerUtil timerUtil = new TimerUtil();
    public boolean scrollingIsAllowed;
    private float dwheel = 0;
    private boolean isScrolling;
    public void onRender() {
        GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                dwheel += (float) yoffset * 15;
                isScrolling = true;
            }
        };
        glfwSetScrollCallback(mw.getHandle(), scrollCallback);
        if (!isScrolling) dwheel = 0;
        double stretch = 0;
        target = Math.min(Math.max(target + dwheel / 2, max - (dwheel == 0 ? 0 : stretch)), (dwheel == 0 ? 0 : stretch));
        scroll = target;
        for (int i = 0; i < timerUtil.getTimePassed(); ++i) {
            scroll = MathUtil.lerp(scroll, target, 0.01F);
        }
        timerUtil.reset();
        isScrolling = false;
    }
    public void renderScrollBar(Vector2d position, double maxHeight) {
        double percentage = getScroll() / getMax();
        double scrollBarHeight = maxHeight - ((getMax() / (getMax() - maxHeight)) * maxHeight);
        scrollingIsAllowed = scrollBarHeight < maxHeight;
        if (!scrollingIsAllowed) return;
        double scrollX = position.x;
        double scrollY = position.y + maxHeight * percentage - scrollBarHeight * percentage;
        RenderUtil.roundedRectangle(scrollX, scrollY, 1, scrollBarHeight, 1, ColorUtil.withAlpha(Color.WHITE, 100));
    }
    public void reset() {
        this.scroll = 0;
        this.target = 0;
    }
    public static int getDWheel() {
        final int[] scroll = {0};
        GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                scroll[0] = (int) yoffset;
            }
        };
        glfwSetScrollCallback(mw.getHandle(), scrollCallback);
        return scroll[0];
    }
    public static int getDWheel(int mc) {
        return getDWheel() * mc;
    }
    @java.lang.SuppressWarnings("all")
    public double getTarget() {
        return this.target;
    }
    @java.lang.SuppressWarnings("all")
    public double getScroll() {
        return this.scroll;
    }
    @java.lang.SuppressWarnings("all")
    public double getMax() {
        return this.max;
    }
    @java.lang.SuppressWarnings("all")
    public TimerUtil getTimerUtil() {
        return this.timerUtil;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isScrollingIsAllowed() {
        return this.scrollingIsAllowed;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isScrolling() {
        return this.isScrolling;
    }
    @java.lang.SuppressWarnings("all")
    public void setTarget(final double target) {
        this.target = target;
    }
    @java.lang.SuppressWarnings("all")
    public void setScroll(final double scroll) {
        this.scroll = scroll;
    }
    @java.lang.SuppressWarnings("all")
    public void setMax(final double max) {
        this.max = max;
    }
    @java.lang.SuppressWarnings("all")
    public void setTimerUtil(final TimerUtil timerUtil) {
        this.timerUtil = timerUtil;
    }
    @java.lang.SuppressWarnings("all")
    public void setScrollingIsAllowed(final boolean scrollingIsAllowed) {
        this.scrollingIsAllowed = scrollingIsAllowed;
    }
    @java.lang.SuppressWarnings("all")
    public void setDwheel(final float dwheel) {
        this.dwheel = dwheel;
    }
    @java.lang.SuppressWarnings("all")
    public void setScrolling(final boolean isScrolling) {
        this.isScrolling = isScrolling;
    }
}
