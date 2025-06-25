package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.MessageTemplate;
import javafx.application.Platform;

public class LiverAgent extends Agent {

    private ThomasNetwork circadianClock;
    private MetabolismMode metabolismMode;
    private MetabolismMode currentMode;
    private static final double INSULIN_THRESHOLD = 5.0;
    private static final double GLUCAGON_THRESHOLD = 5.0;
    private static final double CORTISOL_THRESHOLD = 12.0;
    protected static int AMPK = 1;
    private ATPTrendDetector atpDetector = new ATPTrendDetector();
    //private MetabolicState metabolicState = new MetabolicState();

    // new
    public LiverAgent() {
    }

    public LiverAgent(MetabolismMode metabolismMode) {
        this.metabolismMode = metabolismMode;
    }

    public void metabolismeMode(int complexePerCry) {
        EnvironmentModel env = EnvironmentModel.getInstance();
        metabolismMode.execute(env, complexePerCry);
    }

    //private boolean cortisolHigh = false; // État du cortisol reçu de l'environnement
    //private double currentHour = 0.0; // Heure biologique continue
    //private static final double dt = 1.0; // Pas de temps, doit être aligné avec ThomasNetwork

    @Override
    protected void setup() {
        System.out.println("✅ Liver Agent " + getLocalName() + " démarré.");
        
        // 💡 Spécifie que l'horloge du foie est régulée par le cortisol
        circadianClock = new ThomasNetwork(1,
        1,
        0,
        "Cortisol and AMPK",
        1,
        0,
        1); // (G=1, PC=1, C=0), régulé par "C" (cortisol)

        // Comportement pour mise à jour toutes les dt heures simulées
        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) { // Convertit dt en millisecondes
            @Override
            protected void onTick() {
                //currentHour += ThomasNetwork.dt;
                System.out.println("⏳ Liver: Heure biologique " + String.format("%.2f", NSCAgent.currentTime) + "h");

                // Vérifier les messages entrants
                //MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                //ACLMessage msg = receive(mt);
                
                /*if (msg != null && msg.getContent().equals("Cortisol High")) {
                    cortisolHigh = true;
                    System.out.println("🔥 Liver: Cortisol activé !");
                } else {
                    cortisolHigh = false;
                    System.out.println("🛑 Liver: Cortisol inactif.");
                }*/

                // 🔍 Récupération de l'état hormonal depuis l'environnement
                EnvironmentModel env = EnvironmentModel.getInstance();
                double cortisol = env.getCortisolLevel();
                double insulin = env.getInsulinLevel();
                double glucagon = env.getGlucagonLevel();

                // Affichage état hormonal
                System.out.println(String.format("🧪 Hormones → Cortisol: %.2f, Insuline: %.2f, Glucagon: %.2f",
                        cortisol, insulin, glucagon));

                // Déterminer le mode métabolique à activer
                if (insulin >= INSULIN_THRESHOLD) {
                    currentMode = new StockageMode();
                    System.out.println("🍞 Mode activé : Stockage (insuline)");
                } else if (glucagon >= GLUCAGON_THRESHOLD) {
                    currentMode = new ConsommationMode();
                    System.out.println("🔥 Mode activé : Consommation (glucagon)");
                } else {
                    System.out.println("😴 Aucun mode métabolique activé (hormones trop faibles)");
                    currentMode = null;
                }

                // Exécuter le mode métabolique si défini
                if (currentMode != null) {
                    currentMode.execute(env, circadianClock.getPC());
                }

                ATPTrendDetector.ATPState state = atpDetector.updateAndDetect(env.getAtp());
                switch (state) {
                    case HIGH:
                        System.out.println("🔺 ATP élevé");
                        AMPK = 0;
                        break;
                    case LOW:
                        System.out.println("🔻 ATP bas");
                        AMPK = 1;
                        break;
                    case NEUTRAL:
                        System.out.println("➖ ATP stable");
                        break;
                }

                // Exécuter le modèle Thomas avec l'état du cortisol
                boolean cortisolHigh = cortisol >= CORTISOL_THRESHOLD;
                circadianClock.update(cortisolHigh, AMPK);
                circadianClock.printState();



                // Mettre à jour le graphique en direct
                Platform.runLater(() -> {
                    LivePlot.updateLiverChart(
                        circadianClock.getCurrentTime(),
                        cortisolHigh ? 1.0 : 0.0, // Convert cortisol state to a numeric value
                        circadianClock.getG(),
                        circadianClock.getPC()
                    );
                    LivePlot.updateMetabolicChart(
                        NSCAgent.currentTime,
                        env.getGlucose(),
                        env.getGlycogene(),
                        env.getAcidesGras(),
                        env.getAcidesAmines(),
                        //env.getatp,
                        env.getAcetylCoA(),
                        env.getPyruvate() 
                    );            
                    LivePlot.updateATPchart(
                        NSCAgent.currentTime,
                        env.getAtp()
                    );        
                    LivePlot.updateAMPKChart(
                        NSCAgent.currentTime,
                        AMPK
                    );
                });


                

                // Affichage de l'état métabolique
                System.out.println(String.format("⚙️ État métabolique → Glucose: %.2f | Glycogène: %.2f | AG: %.2f | AA: %.2f | ATP: %.2f | AcetylCoA: %.2f | Pyruvate: %.2f",
                        env.getGlucose(),
                        env.getGlycogene(),
                        env.getAcidesGras(),
                        env.getAcidesAmines(),
                        env.getAtp(),
                        env.getAcetylCoA(),
                        env.getPyruvate()));
            }
            
        });
    }
}
