package dev.fesly.client.notification;

import dev.fesly.client.module.impl.render.Interface;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.animation.extended.impl.EaseOutSine;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.time.TimerHelper;
import dev.fesly.impl.util.time.TimerUtil;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class Notification implements IAccess {
    private final String name;
    private final NotificationType type;
    private final Animation animation;
    private final dev.fesly.impl.util.animation.compact.Animation animationY;
    private final dev.fesly.impl.util.animation.compact.Animation animationH;
    private final Font font;
    private float height = -1;
    private float width = -1;
    private final TimerUtil timerUtil;
    private boolean timered = false;
    private boolean exit = false;
    private static final Minecraft mc = Minecraft.getInstance();

    public Notification(String name, NotificationType type) {
        this.name = name;
        this.type = type;
        this.animation = new EaseBackIn(300, 1, 1f);
        this.animationY = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 200);
        this.animationH = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 200);
        this.font = Fonts.ROBOTO_MEDIUM.get(16);
        this.timerUtil = new TimerUtil();
    }

    public Notification(String name) {
        this(name, NotificationType.INFO);
    }

    public void render(float mouseX, float mouseY, float partialTicks) {
        float[] fullHeight = {0};
        NotificationManager.notifications.forEach(notification -> fullHeight[0] += notification.height);
        fullHeight[0] = (height + 5) * NotificationManager.notifications.indexOf(this);

        if (animationY.getValue() == 0) {
            animationY.setValue(fullHeight[0]);
        }
        if (animationH.getValue() == 0) {
            animationH.setValue((height + 5) * NotificationManager.notifications.size());
        }
        animationY.run(fullHeight[0] - Interface.animation.getNumberValue().floatValue());
        animationH.run((height + 5) * NotificationManager.notifications.size());

        if (animation.finished(Direction.FORWARDS) && !timered) {
            timerUtil.reset();
            timered = true;
        }
        if (timered && timerUtil.hasReached(1000) && !exit) {
            animation.setDirection(Direction.BACKWARDS);
            timerUtil.reset();
            exit = true;
        }
        width = (font.getWidth(name) + (height + 4));
        float widthWithAnim = width * animation.getOutput().floatValue();
        float widthWithAnimB = width * (Math.min(animation.getOutput().floatValue(), 1f));

        height = font.getHeight() + 5;
        float y = mw.getScaledHeight() - 6;
        float x = mw.getScaledWidth() - 5;

        Runnable standardRound = () -> {
            RenderUtil.roundedRectangle(x - widthWithAnim, y - animationH.getValue() + animationY.getValue(), widthWithAnim, height + 1, height / 2.2f, Color.WHITE);
        };
        IAccess.addRunnableWithChecks(standardRound);

        Runnable text = () -> {
            StencilUtil.initStencil();
            standardRound.run();
            StencilUtil.bindReadStencilBuffer(1);
            RenderUtil.drawImage(new ResourceLocation("nova", "icons/nova64px.png"), (int) (x - widthWithAnim + 0.5f), y - 0.5f - animationH.getValue() + animationY.getValue(), height + 2, height + 2, new Color(255, 255, 255, 255).getRGB());
            font.draw(name, x - widthWithAnim + (height), y - animationH.getValue() + animationY.getValue() + font.getMiddleOfBox(height) + 1, -1);
            StencilUtil.uninitStencilBuffer();
        };
        IAccess.POST_RUNNABLES.add(text);
    }

    public boolean check() {
        return animation.getDirection().backwards() && exit && animation.finished(Direction.BACKWARDS);
    }

    public void mouseClicked(float mouseX, float mouseY, float partialTicks) {
    }

    public void mouseReleased(float mouseX, float mouseY, float partialTicks) {
    }
}