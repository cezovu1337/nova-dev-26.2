package dev.fesly.impl.util.animation.color;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import lombok.RequiredArgsConstructor;
import java.awt.*;
public class RGBAnimation {
    private final RGBAnimationType type;
    private final int duration;
    private final Animation r;
    private final Animation g;
    private final Animation b;
    public RGBAnimation(RGBAnimationType type,int duration) {
        this.duration = duration;
        this.type = type;
        r = new Animation(Easing.EASE_OUT_CUBIC, duration);
        g = new Animation(Easing.EASE_OUT_CUBIC, duration);
        b = new Animation(Easing.EASE_OUT_CUBIC, duration);
    }
    public void run(Color color) {
        r.run(color.getRed());
        g.run(color.getRed());
        b.run(color.getRed());
    }
    public Color getColor() {
        if (type == RGBAnimationType.INTEGER) {
            return new Color(r.getNumberValue().intValue(),
                    g.getNumberValue().intValue(),
                    b.getNumberValue().intValue());
        }else {
            return new Color(r.getNumberValue().floatValue(),
                    g.getNumberValue().floatValue(),
                    b.getNumberValue().floatValue());
        }
    }
}
