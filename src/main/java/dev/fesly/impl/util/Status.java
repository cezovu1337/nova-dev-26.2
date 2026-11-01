package dev.fesly.impl.util;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import lombok.Setter;
public class Status {
    public final String name;
    public final Animation animationY = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_IN_OUT_QUAD, 50);
    public final Animation animationAlpha = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 220);
    public void update() {
        animationY.run(12);
    }
    public void setAlpha(float alpha) {
        animationAlpha.run(alpha);
    }
    @java.lang.SuppressWarnings("all")
    public Status(final String name) {
        this.name = name;
    }
    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }
}
