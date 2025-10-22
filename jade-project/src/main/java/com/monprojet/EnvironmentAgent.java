package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class EnvironmentAgent extends Agent {

    private static final double DEGRADATION_RATE = 0.3; // Dégradation exponentielle par tick
    private static final double Repas = 30.0; // Quantité de glucose ingérée par repas


    @Override
    protected void setup() {
        System.out.println("🌍 EnvironmentAgent lancé (mode dégradation)");

        // Initialiser les valeurs dès le setup
        EnvironmentModel env = EnvironmentModel.getInstance();
        //env.addInsulin(0.0);
        //env.addCortisol(0.0);
        //env.addGlucagon(0.0);

        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) {
            //private boolean firstTick = true;

            @Override
            protected void onTick() {

                // manger les 3 repas

                double currentTime = NSCAgent.currentTime;
                double timeOfDay = currentTime % 24.0;
                if (timeOfDay == 8.0 || timeOfDay == 12.0 || timeOfDay == 19.0) {
                    // Repas à 8h, 12h et 19h
                    double glucoseAmount = 0.80* Repas; // Quantité de glucose ingérée
                    double acidesAminesAmount = 10;
                    // en moyenne un repas est à 30-50 g  mais 1/3 est consommé par le métabolisme directement. 
                    EnvironmentModel.getInstance().addGlucose(glucoseAmount);
                    EnvironmentModel.getInstance().addAcidesAmines(acidesAminesAmount);
                    System.out.println("🍽️ Repas à " + currentTime + "h → +" + glucoseAmount + " de glucose");

                    // 20 pourcents sont directement métabolisés en stockage
                    double glucoseMetabolized = 0.20 * Repas;
                    double glycogeneinstant = 0.80 * glucoseMetabolized;
                    EnvironmentModel.getInstance().addGlycogene(glycogeneinstant);
                    double acidesgrasMetabolized = 0.20 * 0.25 * glucoseMetabolized;
                    EnvironmentModel.getInstance().addAcidesGras(acidesgrasMetabolized);
                    //System.out.println("GLUCOSE INSTANT : " + glucoseMetabolized);
                    //System.out.println("ACIDES GRAS INSTANT : " + acidesgrasMetabolized);
                    //System.out.println("GLYCOGENE INSTANT : " + glycogeneinstant);

                }



                // Dégradation naturelle
                env.degradeCortisol(DEGRADATION_RATE);
                env.degradeInsulin(DEGRADATION_RATE);
                env.degradeGlucagon(DEGRADATION_RATE);
                boolean cortisolHandled = false;
                boolean insulinHandled = false;
                boolean glucagonHandled = false;

                ACLMessage msg;
                while ((msg = receive()) != null) {
                    String content = msg.getContent();

                    if (content.startsWith("Cortisol:") && !cortisolHandled) {
                        double amount = Double.parseDouble(content.split(": ")[1]);
                        env.addCortisol(amount);
                        System.out.println("📨 Environnement: Cortisol reçu → +" + amount);
                        cortisolHandled = true;

                    } else if (content.startsWith("Insuline:") && !insulinHandled) {
                        double amount = Double.parseDouble(content.split(": ")[1]);
                        env.addInsulin(amount);
                        System.out.println("📨 Environnement: Insuline reçue → +" + amount);
                        insulinHandled = true;

                    } else if (content.startsWith("Glucagon:") && !glucagonHandled) {
                        double amount = Double.parseDouble(content.split(": ")[1]);
                        env.addGlucagon(amount);
                        System.out.println("📨 Environnement: Glucagon reçu → +" + amount);
                        glucagonHandled = true;

                    } else if (!content.startsWith("Cortisol:") && !content.startsWith("Insuline:") && !content.startsWith("Glucagon:")) {
                        System.out.println("⚠️ Message inconnu reçu : " + content);
                    } else {
                        // Doublon bloqué
                        System.out.println("⛔ Doublon bloqué pour ce tick : " + content);
                    }
                }



                /*// 💡 Forcer 1er point sur graphique uniquement au 1er tick
                if (firstTick) {
                    Platform.runLater(() -> {
                        LivePlot.updateEnvironmentChart(
                            NSCAgent.currentTime,
                            env.getCortisolLevel(),
                            env.getInsulinLevel(),
                            env.getGlucagonLevel()
                        );
                    });
                    firstTick = false;
                }

                // 🔁 Update graphique
                Platform.runLater(() -> {
                    LivePlot.updateEnvironmentChart(
                        NSCAgent.currentTime,
                        env.getCortisolLevel(),
                        env.getInsulinLevel(),
                        env.getGlucagonLevel()
                    );
                });
                */

                // Log global de l’état
                System.out.println(String.format(
                    "📉 Environnement (dégradation) → Cortisol: %.2f | Insuline: %.2f | Glucagon: %.2f",
                    env.getCortisolLevel(),
                    env.getInsulinLevel(),
                    env.getGlucagonLevel()
                ));
            }
        });
    }
}
