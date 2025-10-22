package com.monprojet;

import jade.core.Agent;
//import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
//import jade.lang.acl.ACLMessage;

public class AlphaCellAgent extends Agent {

    private double GLUCAGON_RELEASE_AMOUNT = 5.0;
    private double GLUCAGON_RELEASE_AMOUNT_INDUCED = 5.5;
    private double GLYCEMIE_THRESHOLD = 50.0; // Seuil de glycémie pour la libération de glucagon
    //private double INSULIN_THRESHOLD = 5.0; // Seuil d'insuline
    //private boolean insuline_high = false;
    private boolean glycemie_low = false;




    public static ThomasNetwork AlphaCellClock;

    @Override
    protected void setup() {
        System.out.println("🧪 AlphaCellAgent " + getLocalName() + " démarré.");

        //AlphaCellClock = new ThomasNetwork(0, 1, 0, "Glucose", 0, "Insuline", 1, 1);
        //AlphaCellClock = new ThomasNetwork(0, 0, 0, "Glucose", 0.64, 0.36);
        AlphaCellClock = new ThomasNetwork(1, 0, 0, "Glucose", 0, 0);

        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) {
            @Override
            protected void onTick() {
                // Récupération du niveau de glucose dans l'environnement
                EnvlocalPancreas.getInstance().updateFromEnvironment();
                double glycemie_state = EnvlocalPancreas.getInstance().getLocalGlycemie();
                //double insuline_state = EnvironmentModel.getInstance().getInsulinLevel();

                //insuline_high = insuline_state >= INSULIN_THRESHOLD;
                glycemie_low = glycemie_state < GLYCEMIE_THRESHOLD;

                //AlphaCellClock.update(glycemie_low, insuline_high );
                AlphaCellClock.update(glycemie_low);
                //AlphaCellClock.printState();

                // récupération de G et PC de l'horloge de alpha cell
                boolean g_pc_high_alpha = AlphaCellClock.getG() == 1 && AlphaCellClock.getPC() == 1;

                if (glycemie_state < 60 && glycemie_state >= 40) {
                        if (!g_pc_high_alpha) {
                            EnvironmentModel.getInstance().addGlucagon(GLUCAGON_RELEASE_AMOUNT);
                        } else {
                            EnvironmentModel.getInstance().addGlucagon(GLUCAGON_RELEASE_AMOUNT_INDUCED);
                        }

                    }

                /*ACLMessage msg = receive();
                if (msg != null) {
                    int pcgFlag = Integer.parseInt(msg.getContent());
                    boolean g_pc_high = pcgFlag == 1;
                    //String content = msg.getContent(); // ex: "75.4;1"
                    //String[] parts = content.split(";");
                //if (parts.length == 2) {
                    //double glycemie = Double.parseDouble(parts[0]);
                    //boolean g_pc_high = parts[1].equals("1");

                    if (glycemie_state < 60 && glycemie_state >= 40) {
                        if (!g_pc_high) {
                            EnvironmentModel.getInstance().addGlucagon(GLUCAGON_RELEASE_AMOUNT);
                        } else {
                            EnvironmentModel.getInstance().addGlucagon(GLUCAGON_RELEASE_AMOUNT_INDUCED);
                        }

                    }
                } else {
                    block();
                }*/
            }
        
        });
    }
}