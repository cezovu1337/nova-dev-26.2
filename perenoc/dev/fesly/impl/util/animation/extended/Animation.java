package dev.fesly.impl.util.animation.extended;

import dev.fesly.impl.util.time.TimerUtil;

public abstract class Animation {

    public TimerUtil timerUtil = new TimerUtil();
    protected int duration;
    protected double endPoint;
    protected Direction direction;

    public Animation(int ms, double endPoint) {
        this(ms, endPoint, Direction.FORWARDS);
    }

    public Animation(int ms, double endPoint, Direction direction) {
        this.duration = ms; //Time in milliseconds of how long you want the animation to take.
        this.endPoint = endPoint; //The desired distance for the animated object to go.
        this.direction = direction; //Direction in which the graph is going. If backwards, will start from endPoint and go to 0.
    }


    public boolean finished(Direction direction) {
        return isDone() && this.direction.equals(direction);
    }

    public double getLinearOutput() {
        return 1 - ((timerUtil.getTimePassed() / (double) duration) * endPoint);
    }

    public double getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(double endPoint) {
        this.endPoint = endPoint;
    }

    public void reset() {
        timerUtil.reset();
    }

    public boolean isDone() {
        return timerUtil.hasTimeElapsed(duration);
    }

    public void changeDirection() {
        setDirection(direction.opposite());
    }

    public Direction getDirection() {
        return direction;
    }

    public Animation setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            timerUtil.setTime(System.currentTimeMillis() - (duration - Math.min(duration, timerUtil.getTimePassed())));
        }
        return this;
    }
    public Animation setDirection(boolean forwards) {
        Direction direction = forwards ? Direction.FORWARDS : Direction.BACKWARDS;
        if (this.direction != direction) {
            this.direction = direction;
            timerUtil.setTime(System.currentTimeMillis() - (duration - Math.min(duration, timerUtil.getTimePassed())));
        }
        return this;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return this.duration;
    }

    protected boolean correctOutput() {
        return false;
    }

    public Double getOutput() {
        if (direction.forwards()) {
            if (isDone()) {
                return endPoint;
            }

            return getEquation(timerUtil.getTimePassed() / (double) duration) * endPoint;
        } else {
            if (isDone()) {
                return 0.0;
            }

            if (correctOutput()) {
                double revTime = Math.min(duration, Math.max(0, duration - timerUtil.getTimePassed()));
                return getEquation(revTime / (double) duration) * endPoint;
            }

            return (1 - getEquation(timerUtil.getTimePassed() / (double) duration)) * endPoint;
        }
    }
    public Double getNonNullOutput() {
        return Math.max(0,getOutput());
    }

    protected abstract double getEquation(double x);

}
