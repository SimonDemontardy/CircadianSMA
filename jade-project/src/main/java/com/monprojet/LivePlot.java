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
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;


public class LivePlot extends Application {
    // NSC
    private static XYChart.Series<Number, Number> seriesG_NSC = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_NSC = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesLight = new XYChart.Series<>();

    // Liver - Réseau
    private static XYChart.Series<Number, Number> seriesG_Liver = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_Liver = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesCortisol = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesAMPKLiv = new XYChart.Series<>();

    // Surrénal - Réseau
    private static XYChart.Series<Number, Number> seriesG_Surrenal = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_Surrenal = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesCortisolSurrenal = new XYChart.Series<>();

    // Pancréas - Réseau
    private static XYChart.Series<Number, Number> seriesG_Pancreas = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_Pancreas = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesGlucosePancreas = new XYChart.Series<>();

    // Beta Cells - Réseau
    private static XYChart.Series<Number, Number> seriesG_Beta = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_Beta = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesGlucoseBeta = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesInsulineBeta = new XYChart.Series<>();

    // alpha cells - Réseau
    private static XYChart.Series<Number, Number> seriesG_Alpha = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPC_Alpha = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesGlucoseAlpha = new XYChart.Series<>();

    // Environnement - Hormones
    private static XYChart.Series<Number, Number> seriesCortisolEnv = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesInsuline = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesGlucagon = new XYChart.Series<>();

    // Foie - Métabolisme
    private static XYChart.Series<Number, Number> seriesGlucose = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesGlycogene = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesAG = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesAA = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesATP = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesPyruvate = new XYChart.Series<>();
    private static XYChart.Series<Number, Number> seriesAcetylCoa = new XYChart.Series<>();

    // état de l'ampk
    private static XYChart.Series<Number, Number> seriesAMPK = new XYChart.Series<>();

    private static LineChart<Number, Number> nscChart;
    private static LineChart<Number, Number> liverChart;
    private static LineChart<Number, Number> hormoneChart;
    private static LineChart<Number, Number> metabolismChart;
    private static LineChart<Number, Number> atpChart;
    private static LineChart<Number, Number> ampkChart;
    private static LineChart<Number, Number> SurrenalClockChart;
    private static LineChart<Number, Number> PancreasClockChart;
    private static LineChart<Number, Number> BetaClockChart;
    private static LineChart<Number, Number> AlphaClockChart;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Dynamique Circadienne ");

        // -- 1. Graphique NSC --
        final NumberAxis xAxisNSC = new NumberAxis();
        final NumberAxis yAxisNSC = new NumberAxis();
        xAxisNSC.setLabel("Temps (h)");
        yAxisNSC.setLabel("Valeurs");

        nscChart = new LineChart<>(xAxisNSC, yAxisNSC);
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

        liverChart = new LineChart<>(xAxisLiver, yAxisLiver);
        liverChart.setTitle("Foie - Réseau Thomas");

        seriesG_Liver.setName("G (Foie)");
        seriesPC_Liver.setName("PC (Foie)");
        seriesCortisol.setName("Cortisol (binaire)");
        seriesAMPKLiv.setName("AMPK (binaire)");

        liverChart.getData().addAll(seriesG_Liver, seriesPC_Liver, seriesCortisol, seriesAMPKLiv);

        // -- 2bis Graphique surrénale (réseau) --
        final NumberAxis xAxisSurrenal = new NumberAxis();
        final NumberAxis yAxisSurrenal = new NumberAxis();
        xAxisSurrenal.setLabel("Temps (h)");
        yAxisSurrenal.setLabel("Valeurs");

        SurrenalClockChart = new LineChart<>(xAxisSurrenal, yAxisSurrenal);
        SurrenalClockChart.setTitle("Surrénal - Réseau Thomas");

        seriesG_Surrenal.setName("G (Surrénal)");
        seriesPC_Surrenal.setName("PC (Surrénal)");
        seriesCortisolSurrenal.setName("Cortisol (binaire)");

        SurrenalClockChart.getData().addAll(seriesG_Surrenal, seriesPC_Surrenal, seriesCortisolSurrenal);

        // -- 2ter Graphique Pancréas (réseau) --
        final NumberAxis xAxisPancreas = new NumberAxis();
        final NumberAxis yAxisPancreas = new NumberAxis();
        xAxisPancreas.setLabel("Temps (h)");
        yAxisPancreas.setLabel("Valeurs");  

        PancreasClockChart = new LineChart<>(xAxisPancreas, yAxisPancreas);
        PancreasClockChart.setTitle("Pancréas - Réseau Thomas");

        seriesG_Pancreas.setName("G (Pancréas)");
        seriesPC_Pancreas.setName("PC (Pancréas)");
        seriesGlucosePancreas.setName("Glucose (Pancréas)");

        PancreasClockChart.getData().addAll(seriesG_Pancreas, seriesPC_Pancreas, seriesGlucosePancreas);

        // -- graph beta cells --
        final NumberAxis xAxisBeta = new NumberAxis();
        final NumberAxis yAxisBeta = new NumberAxis();
        xAxisBeta.setLabel("Temps (h)");
        yAxisBeta.setLabel("Valeurs");

        BetaClockChart = new LineChart<>(xAxisBeta, yAxisBeta);
        BetaClockChart.setTitle("Beta Cells - Réseau Thomas");

        seriesG_Beta.setName("G (Beta Cells)");
        seriesPC_Beta.setName("PC (Beta Cells)");
        seriesGlucoseBeta.setName("Glucose ressource (Beta Cells)");
        seriesInsulineBeta.setName("Insuline ressource (Beta Cells)");

        BetaClockChart.getData().addAll(seriesG_Beta, seriesPC_Beta, seriesGlucoseBeta, seriesInsulineBeta);

        // -- graph alpha cells --
        final NumberAxis xAxisAlpha = new NumberAxis();
        final NumberAxis yAxisAlpha = new NumberAxis();
        xAxisAlpha.setLabel("Temps (h)");
        yAxisAlpha.setLabel("Valeurs");
        AlphaClockChart = new LineChart<>(xAxisAlpha, yAxisAlpha);
        AlphaClockChart.setTitle("Alpha Cells - Réseau Thomas");

        seriesG_Alpha.setName("G (Alpha Cells)");
        seriesPC_Alpha.setName("PC (Alpha Cells)");
        seriesGlucoseAlpha.setName("Glucose ressource (Alpha Cells)");

        AlphaClockChart.getData().addAll(seriesG_Alpha, seriesPC_Alpha, seriesGlucoseAlpha);



        // -- 3. Graphique Environnement Hormonal --
        final NumberAxis xAxisEnv = new NumberAxis();
        final NumberAxis yAxisEnv = new NumberAxis();
        xAxisEnv.setLabel("Temps (h)");
        yAxisEnv.setLabel("µg/dL");

        hormoneChart = new LineChart<>(xAxisEnv, yAxisEnv);
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

        metabolismChart = new LineChart<>(xAxisMeta, yAxisMeta);
        metabolismChart.setTitle("Environnement - Métabolisme interne");

        seriesGlucose.setName("Glucose");
        seriesGlycogene.setName("Glycogène");
        seriesAG.setName("Acides Gras");
        seriesAA.setName("Acides Aminés");
        seriesAcetylCoa.setName("Acétyl-CoA");
        seriesPyruvate.setName("Pyruvate");

        metabolismChart.getData().addAll(seriesGlucose, seriesGlycogene, seriesAG, seriesAcetylCoa, seriesPyruvate, seriesAA);

        // 5 graph de l'ATP
        final NumberAxis xAxisATP = new NumberAxis();
        final NumberAxis yAxisATP = new NumberAxis();
        xAxisATP.setLabel("Temps (h)");
        yAxisATP.setLabel("Quantité");
        atpChart = new LineChart<>(xAxisATP, yAxisATP);
        atpChart.setTitle("Environnement - ATP");
        seriesATP.setName("ATP");
        atpChart.getData().addAll(seriesATP);

        // -- 6. Graphique AMPK --
        final NumberAxis xAxisAMPK = new NumberAxis();
        final NumberAxis yAxisAMPK = new NumberAxis();
        xAxisAMPK.setLabel("Temps (h)");
        yAxisAMPK.setLabel("Valeurs");
        ampkChart = new LineChart<>(xAxisAMPK, yAxisAMPK);
        ampkChart.setTitle("AMPK - État");
        seriesAMPK.setName("AMPK");
        ampkChart.getData().addAll(seriesAMPK);


        // -- Disposition dans la fenêtre --
        VBox vbox = new VBox(nscChart, liverChart, SurrenalClockChart, PancreasClockChart, BetaClockChart, AlphaClockChart, hormoneChart, metabolismChart, atpChart, ampkChart);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 900, 1300);
        stage.setScene(scene);
        stage.show();
    }

    public static void addVerticalLine(LineChart<Number, Number> chart, double xValue) {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();

        // Position en X dans le graphique
        double xDisplayPosition = xAxis.getDisplayPosition(xValue);

        // Position Y du haut et du bas du graphique
        double yStart = yAxis.getDisplayPosition(yAxis.getUpperBound());
        double yEnd = yAxis.getDisplayPosition(yAxis.getLowerBound());

        // Crée la ligne verticale
        Line line = new Line(xDisplayPosition, yStart, xDisplayPosition, yEnd);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.5);
        line.getStrokeDashArray().addAll(5.0, 5.0); // optionnel : ligne pointillée

        // Ajoute la ligne en superposant sur le parent du graphique
        if (chart.getParent() instanceof VBox) {
            VBox parent = (VBox) chart.getParent();
            parent.getChildren().add(line);
        } else {
            chart.getScene().getRoot().getChildrenUnmodifiable().addListener((javafx.collections.ListChangeListener<javafx.scene.Node>) change -> {
                if (chart.getParent() instanceof VBox) {
                    VBox parent = (VBox) chart.getParent();
                    parent.getChildren().add(line);
                }
            });
        }
    }



    public static void addMidnightLineToAllCharts(double time) {
        addMidnightLine(nscChart, time);
        addMidnightLine(liverChart, time);
        addMidnightLine(metabolismChart, time);
        addMidnightLine(atpChart, time);
        addMidnightLine(ampkChart, time);
    }

    // Adds a vertical line at the specified time to the given chart
    public static void addMidnightLine(LineChart<Number, Number> chart, double time) {
        Platform.runLater(() -> addVerticalLine(chart, time));
    }



    // NSC
    public static void update_NSC_Clock_Chart(double time, double light, int G, int PC) {
        Platform.runLater(() -> {
            seriesG_NSC.getData().add(new XYChart.Data<>(time, G));
            seriesPC_NSC.getData().add(new XYChart.Data<>(time, PC));
            seriesLight.getData().add(new XYChart.Data<>(time, light));
        });
    }

    // Liver - Réseau
    public static void updateLiverChart(double time, double cortisol, int G, int PC, int AMPK) {
        Platform.runLater(() -> {
            seriesG_Liver.getData().add(new XYChart.Data<>(time, G));
            seriesPC_Liver.getData().add(new XYChart.Data<>(time, PC));
            seriesCortisol.getData().add(new XYChart.Data<>(time, cortisol));
            seriesAMPKLiv.getData().add(new XYChart.Data<>(time, AMPK));
        });
    }

    // Surrénal - Réseau
    public static void updateSurrenalChart(double time, double cortisol, int G, int PC) {
        Platform.runLater(() -> {
            seriesG_Surrenal.getData().add(new XYChart.Data<>(time, G));
            seriesPC_Surrenal.getData().add(new XYChart.Data<>(time, PC));
            seriesCortisolSurrenal.getData().add(new XYChart.Data<>(time, cortisol));
        });
    }

    // Pancréas - Réseau
    public static void updatePancreasChart(double time, double glucose, int G, int PC) {
        Platform.runLater(() -> {
            seriesG_Pancreas.getData().add(new XYChart.Data<>(time, G));
            seriesPC_Pancreas.getData().add(new XYChart.Data<>(time, PC));
            seriesGlucosePancreas.getData().add(new XYChart.Data<>(time, glucose));
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

    public static void updateBetaCellsChart(double time, double glucose, double insulin, int G, int PC) {
        Platform.runLater(() -> {
            seriesG_Beta.getData().add(new XYChart.Data<>(time, G));
            seriesPC_Beta.getData().add(new XYChart.Data<>(time, PC));
            seriesGlucoseBeta.getData().add(new XYChart.Data<>(time, glucose));
            seriesInsulineBeta.getData().add(new XYChart.Data<>(time, insulin));
        });
    }

    public static void updateAlphaCellsChart(double time, double glucose, int G, int PC) {
        Platform.runLater(() -> {
            seriesG_Alpha.getData().add(new XYChart.Data<>(time, G));
            seriesPC_Alpha.getData().add(new XYChart.Data<>(time, PC));
            seriesGlucoseAlpha.getData().add(new XYChart.Data<>(time, glucose));
        });
    }

}
