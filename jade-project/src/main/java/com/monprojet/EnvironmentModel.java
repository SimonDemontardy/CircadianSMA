package com.monprojet;

public class EnvironmentModel {
    private static EnvironmentModel instance = new EnvironmentModel();

    //private static EnvironmentModel instance = new EnvironmentModel();

    //Métabolites (fusion des variables MetabolicState ici)
    private double glucose = 10;
    private double glycogene = 0;
    private double acidesGras = 0;
    private double acidesAmines = 0;
    private double atp = 1000.0;
    private double pyruvate = 1;
    private double acetylCoA = 1;
    // Hormones
    private double cortisolLevel = 0.0;
    private double insulinLevel = 0.0;
    private double glucagonLevel = 0.0;
    //private double glucoseLevel = 0.0;

    private EnvironmentModel() {
        cortisolLevel = 0.0;
        insulinLevel = 0.0;
        glucagonLevel = 0.0;
        //glucoseLevel = 0.0;
        // Initialisation des métabolites
        glucose = 50;
        glycogene = 0;
        acidesGras = 0;
        acidesAmines = 0;
        atp = 1000.0;
        pyruvate = 1;
        acetylCoA = 1;
        
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

    // Glucose
    public double getGlucoseLevel() {
        return glucose;
    }

        // Getters & setters (optionnel, ici en accès direct pour simplifier)
    public double getGlucose() { return glucose; }
    public void setGlucose(double glucose) { this.glucose = glucose; }
    public void addGlucose(double amount) { this.glucose += amount; }
    public void removeGlucose(double amount) { this.glucose -= amount; }

    public double getGlycogene() { return glycogene; }
    public void setGlycogene(double glycogene) { this.glycogene = glycogene; }
    public void addGlycogene(double amount) { this.glycogene += amount; }
    public void removeGlycogene(double amount) { this.glycogene -= amount; }

    public double getAcidesGras() { return acidesGras; }
    public void setAcidesGras(double acidesGras) { this.acidesGras = acidesGras; }
    public void addAcidesGras(double amount) { this.acidesGras += amount; }
    public void removeAcidesGras(double amount) { this.acidesGras -= amount; }

    public double getAcidesAmines() { return acidesAmines; }
    public void setAcidesAmines(double acidesAmines) { this.acidesAmines = acidesAmines; }
    public void addAcidesAmines(double amount) { this.acidesAmines += amount; }
    public void removeAcidesAmines(double amount) { this.acidesAmines -= amount; }

    public double getAtp() { return atp; }
    public void setAtp(double atp) { this.atp = atp; }
    public void addAtp(double amount) { this.atp += amount; }
    public void removeAtp(double amount) { this.atp -= amount; }


    public double getPyruvate() { return pyruvate; }
    public void setPyruvate(double pyruvate) { this.pyruvate = pyruvate; }
    public void addPyruvate(double amount) { this.pyruvate += amount; }
    public void removePyruvate(double amount) { this.pyruvate -= amount; }

    public double getAcetylCoA() { return acetylCoA; }
    public void setAcetylCoA(double acetylCoA) { this.acetylCoA = acetylCoA; }
    public void addAcetylCoA(double amount) { this.acetylCoA += amount; }
    public void removeAcetylCoA(double amount) { this.acetylCoA -= amount;}
}
