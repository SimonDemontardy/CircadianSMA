package com.monprojet;

import javafx.application.Platform;

public class GraphUpdater {

    public void updateAllGraphs() {
        double time = NSCAgent.getCurrentTime();
        EnvironmentModel env = EnvironmentModel.getInstance();
        DataLogger.log(time);

        Platform.runLater(() -> {
            LivePlot.update_NSC_Clock_Chart(
                time,
                NSCAgent.getRessourceLevel_NSC(),
                // NSCAgent.nscClock.getRessourceLevel(),
                NSCAgent.getG_NSC(),
                // NSCAgent.nscClock.getG(),
                NSCAgent.getPC_NSC()
                // NSCAgent.nscClock.getPC()
            );


            LivePlot.updateLiverChart(
                time,
                LiverAgent.circadianClock.getRessourceLevel(),
                LiverAgent.circadianClock.getG(),
                LiverAgent.circadianClock.getPC(),
                LiverAgent.circadianClock.getRessource2Level()
            );

            LivePlot.updatePancreasChart(
                time,
                PancreasAgent.PancreasClock.getRessourceLevel(),
                PancreasAgent.PancreasClock.getG(),
                PancreasAgent.PancreasClock.getPC()
            );

            LivePlot.updateBetaCellsChart(
                time,
                BetaCellAgent.BetaCellClock.getRessourceLevel(),
                BetaCellAgent.BetaCellClock.getRessource2Level(),
                BetaCellAgent.BetaCellClock.getG(),
                BetaCellAgent.BetaCellClock.getPC()
            );

            LivePlot.updateAlphaCellsChart(
                time,
                AlphaCellAgent.AlphaCellClock.getRessourceLevel(),
                AlphaCellAgent.AlphaCellClock.getG(),
                AlphaCellAgent.AlphaCellClock.getPC()
            );

            LivePlot.updateSurrenalChart(
                time,
                SurrenalAgent.SurrenalClock.getRessourceLevel(),
                SurrenalAgent.SurrenalClock.getG(),
                SurrenalAgent.SurrenalClock.getPC()
            );

            LivePlot.updateMetabolicChart(
                time,
                
                env.getGlucoseLevel(),
                env.getGlycogene(),
                env.getAcidesGras(),
                env.getAcidesAmines(),
                // LiverAgent.getAtpLevel(),
                env.getAcetylCoA(),
                env.getPyruvate()
            );
            LivePlot.updateATPchart(
                time,
                env.getAtp()
                
            );
            LivePlot.updateAMPKChart(
                time,
                // LiverAgent.getAMPKLevel()
                LiverAgent.circadianClock.getRessource2Level()
            );

            LivePlot.updateEnvironmentChart(
                time,
                env.getCortisolLevel(),
                env.getInsulinLevel(),
                env.getGlucagonLevel()
                );
            

            //if (time % 24 == 0) {
              //  LivePlot.addMidnightLineToAllCharts(time);
            //}
        });
    }
}
