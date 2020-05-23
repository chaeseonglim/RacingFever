package com.lifejourney.racingfever;

public class Effect {

    public Effect(float modifier, int duration) {
        this.modifier = modifier;
        this.duration = duration;
    }

    public void tick() {
        duration--;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public float getModifier() {
        return modifier;
    }

    public int getDuration() {
        return duration;
    }

    private float modifier;
    private int duration;
}
