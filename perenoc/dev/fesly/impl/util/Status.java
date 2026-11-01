package dev.fesly.impl.util;

import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Status {

    @Getter
    public final String name;
        public final Animation animationY = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_IN_OUT_QUAD, 50);
        public final Animation animationAlpha = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 220);

        public void update() {
            animationY.run(12);
        }
        public void setAlpha(float alpha) {
            animationAlpha.run(alpha);
        }
    }