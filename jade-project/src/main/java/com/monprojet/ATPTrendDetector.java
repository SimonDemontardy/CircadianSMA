package com.monprojet;

import java.util.LinkedList;
import java.util.Queue;

public class ATPTrendDetector {
    private static final int WINDOW_SIZE = 50;
    private Queue<Double> atpHistory = new LinkedList<>();
    private double sum = 0;

    public enum ATPState { HIGH, LOW, NEUTRAL }

    public ATPState updateAndDetect(double currentATP) {
        atpHistory.add(currentATP);
        sum += currentATP;

        if (atpHistory.size() > WINDOW_SIZE) {
            sum -= atpHistory.poll(); // Supprime l’ancienne
        }

        double average = sum / atpHistory.size();

        if (currentATP > average) return ATPState.HIGH;
        if (currentATP < average) return ATPState.LOW;
        return ATPState.NEUTRAL;
    }
}
