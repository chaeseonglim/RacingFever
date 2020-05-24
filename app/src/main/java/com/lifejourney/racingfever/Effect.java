package com.lifejourney.racingfever;

public class Effect {

    static class Builder {
        String name;
        int duration;

        float modifierCarGeneral = 1.0f;
        float modifierDriverGeneral = 1.0f;

        Builder(String name, int duration) {
            this.duration = duration;
        }
        Builder modifierCarGeneral(float modifierCarGeneral) {
            this.modifierCarGeneral = modifierCarGeneral;
            return this;
        }
        Builder modifierDriverGeneral(float modifierDriverGeneral) {
            this.modifierDriverGeneral = modifierDriverGeneral;
            return this;
        }
        Effect build() {
            return new Effect(this);
        }
    }

    private Effect(Builder builder) {
        name = builder.name;
        duration = builder.duration;
        modifierCarGeneral = builder.modifierCarGeneral;
        modifierDriverGeneral = builder.modifierDriverGeneral;
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
    public float getModifierCarGeneral() {
        return modifierCarGeneral;
    }

    /**
     *
     * @return
     */
    public float getModifierDriverGeneral() {
        return modifierDriverGeneral;
    }

    /**
     *
     * @return
     */
    int getDuration() {
        return duration;
    }

    /**
     *
     * @return
     */
    String getName() {
        return name;
    }

    private String name;
    private int duration;
    private float modifierCarGeneral;
    private float modifierDriverGeneral;
}
