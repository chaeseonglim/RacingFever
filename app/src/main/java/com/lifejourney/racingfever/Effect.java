package com.lifejourney.racingfever;

public class Effect {

    Effect(float modifier, int duration) {
        this.modifier = modifier;
        this.duration = duration;
    }

    /**
     *
     */
    void tick() {
        duration--;
    }

    /**
     *
     * @return
     */
    boolean isExpired() {
        return duration <= 0;
    }

    /**
     *
     * @return
     */
    float getModifier() {
        return modifier;
    }

    /**
     *
     * @return
     */
    int getDuration() {
        return duration;
    }

    private float modifier;
    private int duration;
}
