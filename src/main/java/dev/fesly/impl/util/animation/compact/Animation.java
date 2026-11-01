package dev.fesly.impl.util.animation.compact;
public class Animation {
    private Easing easing;
    private long duration;
    private long millis;
    private long startTime;
    private double startValue;
    private double destinationValue;
    private double value;
    private boolean finished;
    public Animation(final Easing easing, final long duration) {
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }
    public void run(final double destinationValue) {
        this.millis = System.currentTimeMillis();
        if (this.destinationValue != destinationValue) {
            this.destinationValue = destinationValue;
            this.reset();
        } else {
            this.finished = this.millis - this.duration > this.startTime;
            if (this.finished) {
                this.value = destinationValue;
                return;
            }
        }
        final double result = this.easing.getFunction().apply(this.getProgress());
        if (this.value > destinationValue) {
            this.value = this.startValue - (this.startValue - destinationValue) * result;
        } else {
            this.value = this.startValue + (destinationValue - this.startValue) * result;
        }
    }
    public Number getNumberValue() {
        return getValue();
    }
    public double getProgress() {
        return (double) (System.currentTimeMillis() - this.startTime) / (double) this.duration;
    }
    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.startValue = value;
        this.finished = false;
    }
    @java.lang.SuppressWarnings("all")
    public Easing getEasing() {
        return this.easing;
    }
    @java.lang.SuppressWarnings("all")
    public long getDuration() {
        return this.duration;
    }
    @java.lang.SuppressWarnings("all")
    public long getMillis() {
        return this.millis;
    }
    @java.lang.SuppressWarnings("all")
    public long getStartTime() {
        return this.startTime;
    }
    @java.lang.SuppressWarnings("all")
    public double getStartValue() {
        return this.startValue;
    }
    @java.lang.SuppressWarnings("all")
    public double getDestinationValue() {
        return this.destinationValue;
    }
    @java.lang.SuppressWarnings("all")
    public double getValue() {
        return this.value;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isFinished() {
        return this.finished;
    }
    @java.lang.SuppressWarnings("all")
    public void setEasing(final Easing easing) {
        this.easing = easing;
    }
    @java.lang.SuppressWarnings("all")
    public void setDuration(final long duration) {
        this.duration = duration;
    }
    @java.lang.SuppressWarnings("all")
    public void setMillis(final long millis) {
        this.millis = millis;
    }
    @java.lang.SuppressWarnings("all")
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }
    @java.lang.SuppressWarnings("all")
    public void setStartValue(final double startValue) {
        this.startValue = startValue;
    }
    @java.lang.SuppressWarnings("all")
    public void setDestinationValue(final double destinationValue) {
        this.destinationValue = destinationValue;
    }
    @java.lang.SuppressWarnings("all")
    public void setValue(final double value) {
        this.value = value;
    }
    @java.lang.SuppressWarnings("all")
    public void setFinished(final boolean finished) {
        this.finished = finished;
    }
}
