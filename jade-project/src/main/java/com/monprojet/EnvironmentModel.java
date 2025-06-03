package com.monprojet;

public class EnvironmentModel {
    private static EnvironmentModel instance = new EnvironmentModel();

    private double cortisolLevel = 0.0;
    private double insulinLevel = 0.0;
    private double glucagonLevel = 0.0;
    private double glucoseLevel = 0.0;

    private EnvironmentModel() {
        cortisolLevel = 0.0;
        insulinLevel = 0.0;
        glucagonLevel = 0.0;
        glucoseLevel = 0.0;
    }

    public static EnvironmentModel getInstance() {
        return instance;
    }

    // Cortisol
    public double getCortisolLevel() {
        return cortisolLevel;
    }

    public void addCortisol(double amount) {
        this.cortisolLevel += amount;
    }

    public void degradeCortisol(double rate) {
        cortisolLevel *= Math.exp(-rate);
        if (cortisolLevel < 0.05) cortisolLevel = 0;
    }

    // Insuline
    public double getInsulinLevel() {
        return insulinLevel;
    }

    public void addInsulin(double amount) {
        this.insulinLevel += amount;
    }

    public void degradeInsulin(double rate) {
        insulinLevel *= Math.exp(-rate);
        if (insulinLevel < 0.05) insulinLevel = 0;
    }

    // Glucagon
    public double getGlucagonLevel() {
        return glucagonLevel;
    }

    public void addGlucagon(double amount) {
        this.glucagonLevel += amount;
    }

    public void degradeGlucagon(double rate) {
        glucagonLevel *= Math.exp(-rate);
        if (glucagonLevel < 0.05) glucagonLevel = 0;
    }
}
