package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.application.Platform;

public class EnvironmentAgent extends Agent {

    private static final double DEGRADATION_RATE = 0.3; // Dégradation exponentielle par tick

    @Override
    protected void setup() {
        System.out.println("🌍 EnvironmentAgent lancé (mode dégradation)");

        // Initialiser les valeurs dès le setup
        EnvironmentModel env = EnvironmentModel.getInstance();
        //env.addInsulin(0.0);
        //env.addCortisol(0.0);
        //env.addGlucagon(0.0);

        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) {
            private boolean firstTick = true;

            @Override
            protected void onTick() {

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



                // 💡 Forcer 1er point sur graphique uniquement au 1er tick
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
