package dev.fesly.impl.util.time;
import net.minecraft.client.Minecraft;
public class TimerUtil {
    private long lastMS;
    public TimerUtil() {
        reset();
    }
    public void reset() {
        lastMS = System.currentTimeMillis();
    }
    public long getTimePassed() {
        return System.currentTimeMillis() - lastMS;
    }
    public void setTime(long time) {
        lastMS = time;
    }
    public boolean hasTimeElapsed(long time) {
        return getTimePassed() >= time;
    }
    public boolean hasTimeElapsed(long time, boolean reset) {
        boolean hasElapsed = getTimePassed() >= time;
        if (hasElapsed && reset) {
            reset();
        }
        return hasElapsed;
    }
    public boolean hasReached(double milliseconds) {
        return getTimePassed() >= milliseconds;
    }
    public boolean delay(long milliSec) {
        boolean hasDelayElapsed = getTimePassed() - milliSec >= 0;
        if (hasDelayElapsed) {
            reset();
        }
        return hasDelayElapsed;
    }
    public float getTimerSpeed() {
        return Minecraft.getInstance().timer.timerSpeed;
    }
    public void setTimerSpeed(float timerSpeed) {
        Minecraft.getInstance().timer.timerSpeed = timerSpeed;
    }
}
