package com.monprojet;

public class EnvlocalPancreas {
    private static EnvlocalPancreas instance;
    private double local_glycemie;
    private double local_insuline;
    private double local_glucagon;
    private double local_cortisol;

    private EnvlocalPancreas() {
        // Initialisation de la glycémie locale
        local_glycemie = 0.0;
        local_insuline = 0.0;
        local_glucagon = 0.0;
        local_cortisol = 0.0;

    }

    public static EnvlocalPancreas getInstance() {
        if (instance == null) {
            instance = new EnvlocalPancreas();
        }
        return instance;
    }

    public void updateFromEnvironment() {
        local_glycemie = EnvironmentModel.getInstance().getGlucoseLevel();
        local_insuline = EnvironmentModel.getInstance().getInsulinLevel();
        local_glucagon = EnvironmentModel.getInstance().getGlucagonLevel();
        local_cortisol = EnvironmentModel.getInstance().getCortisolLevel();
    }

    public double getLocalGlycemie() {
        return local_glycemie;
    }
    public double getLocalInsuline() {
        return local_insuline;
    }
    public double getLocalGlucagon() {
        return local_glucagon;  
    }
    public double getLocalCortisol() {
        return local_cortisol;
    }

    public void setLocalGlycemie(double glycemie) {
        this.local_glycemie = glycemie;
    }
    public void setLocalInsuline(double insuline) {
        this.local_insuline = insuline;
    }
    public void setLocalGlucagon(double glucagon) {
        this.local_glucagon = glucagon;
    }
    public void setLocalCortisol(double cortisol) {
        this.local_cortisol = cortisol;
    }
}
