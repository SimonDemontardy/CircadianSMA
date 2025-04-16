package com.monprojet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LivePlot extends Application {
    // NSC
    private static XYChart.Series<Number, Number> seriesG_NSC = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_NSC = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesLight = new XYChart.Series<>();

    // Liver - Réseau
    private static XYChart.Series<Number, Number> seriesG_Liver = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_Liver = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesCortisol = new XYChart.Series<>();

    // Environnement - Hormones
    private static XYChart.Series<Number, Number> seriesCortisolEnv = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesInsuline = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesGlucagon = new XYChart.Series<>();

    // Foie - Métabolisme
    private static XYChart.Series<Number, Number> seriesGlucose = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesGlycogene = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesAG = new XYChart.Series<>();
    //private static XYChart.Series<Number, Number> seriesAA = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesATP = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPyruvate = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesAcetylCoa = new XYChart.Series<>();

    // état de l'ampk
    private static XYChart.Series<Number, Number> seriesAMPK = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Dynamique Circadienne - NSC / Foie / Environnement / Métabolisme");

        // -- 1. Graphique NSC --
        final NumberAxis xAxisNSC = new NumberAxis();
        final NumberAxis yAxisNSC = new NumberAxis();
        xAxisNSC.setLabel("Temps (h)");
        yAxisNSC.setLabel("Valeurs");

        LineChart<Number, Number> nscChart = new LineChart<>(xAxisNSC, yAxisNSC);
        nscChart.setTitle("NSC - Réseau Thomas");

        seriesG_NSC.setName("G (NSC)");
        seriesPC_NSC.setName("PC (NSC)");
        seriesLight.setName("Lumière");

        nscChart.getData().addAll(seriesG_NSC, seriesPC_NSC, seriesLight);

        // -- 2. Graphique Liver (réseau) --
        final NumberAxis xAxisLiver = new NumberAxis();
        final NumberAxis yAxisLiver = new NumberAxis();
        xAxisLiver.setLabel("Temps (h)");
        yAxisLiver.setLabel("Valeurs");

        LineChart<Number, Number> liverChart = new LineChart<>(xAxisLiver, yAxisLiver);
        liverChart.setTitle("Foie - Réseau Thomas");

        seriesG_Liver.setName("G (Foie)");
        seriesPC_Liver.setName("PC (Foie)");
        seriesCortisol.setName("Cortisol (binaire)");

        liverChart.getData().addAll(seriesG_Liver, seriesPC_Liver, seriesCortisol);

        // -- 3. Graphique Environnement Hormonal --
        final NumberAxis xAxisEnv = new NumberAxis();
        final NumberAxis yAxisEnv = new NumberAxis();
        xAxisEnv.setLabel("Temps (h)");
        yAxisEnv.setLabel("µg/dL");

        LineChart<Number, Number> hormoneChart = new LineChart<>(xAxisEnv, yAxisEnv);
        hormoneChart.setTitle("Environnement - Hormones");

        seriesCortisolEnv.setName("Cortisol");
        seriesInsuline.setName("Insuline");
        seriesGlucagon.setName("Glucagon");

        hormoneChart.getData().addAll(seriesCortisolEnv, seriesInsuline, seriesGlucagon);

        // -- 4. Graphique Métabolisme Foie --
        final NumberAxis xAxisMeta = new NumberAxis();
        final NumberAxis yAxisMeta = new NumberAxis();
        xAxisMeta.setLabel("Temps (h)");
        yAxisMeta.setLabel("Quantité");

        LineChart<Number, Number> metabolismChart = new LineChart<>(xAxisMeta, yAxisMeta);
        metabolismChart.setTitle("Foie - Métabolisme interne");

        seriesGlucose.setName("Glucose");
        seriesGlycogene.setName("Glycogène");
        seriesAG.setName("Acides Gras");
        //seriesAA.setName("Acides Aminés");
        seriesAcetylCoa.setName("Acétyl-CoA");
        seriesPyruvate.setName("Pyruvate");

        metabolismChart.getData().addAll(seriesGlucose, seriesGlycogene, seriesAG, seriesAcetylCoa, seriesPyruvate);

        // 5 graph de l'ATP
        final NumberAxis xAxisATP = new NumberAxis();
        final NumberAxis yAxisATP = new NumberAxis();
        xAxisATP.setLabel("Temps (h)");
        yAxisATP.setLabel("Quantité");
        LineChart<Number, Number> atpChart = new LineChart<>(xAxisATP, yAxisATP);
        atpChart.setTitle("Foie - ATP");
        seriesATP.setName("ATP");
        atpChart.getData().addAll(seriesATP);

        // -- 6. Graphique AMPK --
        final NumberAxis xAxisAMPK = new NumberAxis();
        final NumberAxis yAxisAMPK = new NumberAxis();
        xAxisAMPK.setLabel("Temps (h)");
        yAxisAMPK.setLabel("Valeurs");
        LineChart<Number, Number> ampkChart = new LineChart<>(xAxisAMPK, yAxisAMPK);
        ampkChart.setTitle("AMPK - État");
        seriesAMPK.setName("AMPK");
        ampkChart.getData().addAll(seriesAMPK);


        // -- Disposition dans la fenêtre --
        VBox vbox = new VBox(nscChart, liverChart, hormoneChart, metabolismChart, atpChart, ampkChart);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 900, 1300);
        stage.setScene(scene);
        stage.show();
    }

    // NSC
    public static void updateNSCChart(double time, double light, int G, int PC) {
        Platform.runLater(() -> {
            seriesG_NSC.getData().add(new XYChart.Data<>(time, G));
            seriesPC_NSC.getData().add(new XYChart.Data<>(time, PC));
            seriesLight.getData().add(new XYChart.Data<>(time, light));
        });
    }

    // Liver - Réseau
    public static void updateLiverChart(double time, double cortisol, int G, int PC) {
        Platform.runLater(() -> {
            seriesG_Liver.getData().add(new XYChart.Data<>(time, G));
            seriesPC_Liver.getData().add(new XYChart.Data<>(time, PC));
            seriesCortisol.getData().add(new XYChart.Data<>(time, cortisol));
        });
    }

    // Environnement
    public static void updateEnvironmentChart(double time, double cortisol, double insulin, double glucagon) {
        Platform.runLater(() -> {
            seriesCortisolEnv.getData().add(new XYChart.Data<>(time, cortisol));
            seriesInsuline.getData().add(new XYChart.Data<>(time, insulin));
            seriesGlucagon.getData().add(new XYChart.Data<>(time, glucagon));
        });
    }

    // Métabolisme du foie
    public static void updateMetabolicChart(double time, double glucose, double glycogene, double ag, double aa, double acetylCoA, double pyruvate) {
        Platform.runLater(() -> {
            seriesGlucose.getData().add(new XYChart.Data<>(time, glucose));
            seriesGlycogene.getData().add(new XYChart.Data<>(time, glycogene));
            seriesAG.getData().add(new XYChart.Data<>(time, ag));
            //seriesAA.getData().add(new XYChart.Data<>(time, aa));
            //seriesATP.getData().add(new XYChart.Data<>(time, atp));
            seriesAcetylCoa.getData().add(new XYChart.Data<>(time, acetylCoA));
            seriesPyruvate.getData().add(new XYChart.Data<>(time, pyruvate));

        });
    }
    public static void updateATPchart(double time, double atp) {
        Platform.runLater(() -> {
            seriesATP.getData().add(new XYChart.Data<>(time, atp));
        });
    }
    public static void updateAMPKChart(double time, double ampk) {
        Platform.runLater(() -> {
            seriesAMPK.getData().add(new XYChart.Data<>(time, ampk));
        });
    }
}
