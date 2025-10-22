package com.monprojet;

import java.io.FileWriter;
import java.io.IOException;

public class DataLogger {
    private static final String LOG_FILE_PATH = "data_log240clockbis.txt";
    private static boolean headerWritten = false;

    public static void log(double time) {
        // On ne log que pour t entre 100 et 148
        if (time < 200 || time > 240) {
            return; // sortir sans rien faire
        }

        try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
            // écrire l'en-tête une seule fois au début
            if (!headerWritten) {
                writer.write("time;NSC_res;NSC_G;NSC_PC;Liver_res;Liver_G;Liver_PC;Liver_res2;"
                    + "Pancreas_res;Pancreas_G;Pancreas_PC;"
                    + "Beta_res;Beta_res2;Beta_G;Beta_PC;"
                    + "Alpha_res;Alpha_G;Alpha_PC;"
                    + "Surrenal_res;Surrenal_G;Surrenal_PC;"
                    + "Glucose;Glycogene;AcidesGras;AcidesAmines;AcetylCoA;Pyruvate;ATP;AMPK;"
                    + "Cortisol;Insulin;Glucagon\n");
                headerWritten = true;
            }

            // écrire la ligne de données
            writer.write(time + ";" +
                NSCAgent.getRessourceLevel_NSC() + ";" +
                NSCAgent.getG_NSC() + ";" +
                NSCAgent.getPC_NSC() + ";" +
                LiverAgent.circadianClock.getRessourceLevel() + ";" +
                LiverAgent.circadianClock.getG() + ";" +
                LiverAgent.circadianClock.getPC() + ";" +
                LiverAgent.circadianClock.getRessource2Level() + ";" +
                PancreasAgent.PancreasClock.getRessourceLevel() + ";" +
                PancreasAgent.PancreasClock.getG() + ";" +
                PancreasAgent.PancreasClock.getPC() + ";" +
                BetaCellAgent.BetaCellClock.getRessourceLevel() + ";" +
                BetaCellAgent.BetaCellClock.getRessource2Level() + ";" +
                BetaCellAgent.BetaCellClock.getG() + ";" +
                BetaCellAgent.BetaCellClock.getPC() + ";" +
                AlphaCellAgent.AlphaCellClock.getRessourceLevel() + ";" +
                AlphaCellAgent.AlphaCellClock.getG() + ";" +
                AlphaCellAgent.AlphaCellClock.getPC() + ";" +
                SurrenalAgent.SurrenalClock.getRessourceLevel() + ";" +
                SurrenalAgent.SurrenalClock.getG() + ";" +
                SurrenalAgent.SurrenalClock.getPC() + ";" +
                EnvironmentModel.getInstance().getGlucoseLevel() + ";" +
                EnvironmentModel.getInstance().getGlycogene() + ";" +
                EnvironmentModel.getInstance().getAcidesGras() + ";" +
                EnvironmentModel.getInstance().getAcidesAmines() + ";" +
                EnvironmentModel.getInstance().getAcetylCoA() + ";" +
                EnvironmentModel.getInstance().getPyruvate() + ";" +
                EnvironmentModel.getInstance().getAtp() + ";" +
                LiverAgent.circadianClock.getRessource2Level() + ";" + // AMPK
                EnvironmentModel.getInstance().getCortisolLevel() + ";" +
                EnvironmentModel.getInstance().getInsulinLevel() + ";" +
                EnvironmentModel.getInstance().getGlucagonLevel() + "\n"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
