package dev.fesly.impl.util.time;
public class TimerHelper {
    public float timerSpeed = 1.0F;
    private long prevMS;
    private long lastMS;
    private long previousTime = -1L;
    private final long tick = 0;
    public TimerHelper() {
        prevMS = 0L;
    }
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
    public boolean check(float milliseconds) {
        return (float) (getCurrentTime() - previousTime) >= milliseconds;
    }
    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }
    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset)
                reset();
            return true;
        }
        return false;
    }
    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }
    public long getDifference() {
        return getTime() - prevMS;
    }
    public void setDifference(long difference) {
        prevMS = (getTime() - difference);
    }
    public boolean hasReached(double milliseconds) {
        return (double) (getCurrentMS() - lastMS) >= milliseconds;
    }
    public boolean hasTimePassed(float ticks) {
        return tick >= ticks;
    }
    public void resetwatermark() {
        previousTime = getCurrentTime();
    }
    public void reset() {
        lastMS = getCurrentMS();
    }
    public boolean delay(float milliSec) {
        return (float) (getTime() - prevMS) >= milliSec;
    }
    public long getTime() {
        return getCurrentMS() - lastMS;
    }
    public void setTime(long time) {
        lastMS = time;
    }
}
