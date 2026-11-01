package net.minecraft.potion;

public class EffectInstance {
    private final Potion potion;
    private final int duration;
    private final int amplifier;

    public EffectInstance(Potion potion, int duration, int amplifier) {
        this.potion = potion;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public Potion getPotion() {
        return potion;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public boolean getIsPotionDurationMax() {
        return false;
    }
}
