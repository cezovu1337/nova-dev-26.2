package dev.fesly.impl.util.time;

import net.minecraft.client.Minecraft;

/**
 * Code by XYELAN
 **/
public class TimerUtil {
    private long lastMS;

    /**
     * Создает новый таймер и сбрасывает его.
     */
    public TimerUtil() {
        reset();
    }

    /**
     * Сбрасывает таймер.
     */
    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    /**
     * Возвращает время, прошедшее с момента последнего сброса таймера.
     */
    public long getTimePassed() {
        return System.currentTimeMillis() - lastMS;
    }

    /**
     * Устанавливает время
     */
    public void setTime(long time) {
        lastMS = time;
    }

    /**
     * Возвращает true, если прошло указанное количество времени с момента последнего сброса таймера.
     *
     * @param time время в миллисекундах
     */
    public boolean hasTimeElapsed(long time) {
        return getTimePassed() >= time;
    }

    /**
     * Возвращает true, если прошло указанное количество времени с момента последнего сброса таймера.
     * Если параметр reset равен true, таймер будет сброшен.
     *
     * @param time  время в миллисекундах
     * @param reset флаг, указывающий, нужно ли сбросить таймер
     */
    public boolean hasTimeElapsed(long time, boolean reset) {
        boolean hasElapsed = getTimePassed() >= time;
        if (hasElapsed && reset) {
            reset();
        }
        return hasElapsed;
    }

    /**
     * Возвращает true, если прошло указанное количество миллисекунд с момента последнего сброса таймера.
     *
     * @param milliseconds время в миллисекундах
     */

    public boolean hasReached(double milliseconds) {
        return getTimePassed() >= milliseconds;
    }

    /**
     * Возвращает true, если прошло указанное количество миллисекунд с момента последнего сброса таймера.
     * Если таймер истек, он будет сброшен.
     *
     * @param milliSec время в миллисекундах
     */
    public boolean delay(long milliSec) {
        boolean hasDelayElapsed = getTimePassed() - milliSec >= 0;
        if (hasDelayElapsed) {
            reset();
        }
        return hasDelayElapsed;
    }

    /**
     * Возвращает скорость таймера.
     */
    public float getTimerSpeed() {
        return Minecraft.getInstance().timer.timerSpeed;
    }

    /**
     * Устанавливает скорость таймера.
     *
     * @param timerSpeed скорость таймера
     */
    public void setTimerSpeed(float timerSpeed) {
        Minecraft.getInstance().timer.timerSpeed = timerSpeed;
    }
}