package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class BetaCellAgent extends Agent {

    private double INSULIN_RELEASE_AMOUNT = 4.0;
    private double INSULIN_RELEASE_AMOUNT_INDUCED_beta = 4.5;
    private double INSULIN_RELEASE_AMOUNT_INDUCED_pancreas = 5.0;
    private double INSULIN_RELEASE_AMOUNT_INDUCED_both = 6.0;

    private double GLYCEMIE_TRESHOLD_BETA = 60.0; // Seuil de glycémie pour la libération d'insuline
    private double INSULINE_TRESHOLD_BETA = 5.0; // Seuil d'insuline
    private boolean glycemie_high = false;
    private boolean insuline_high = false;

    public static ThomasNetwork BetaCellClock;

    //private Double glycemie = null;
    //private Integer gPcState = null;

    @Override
    protected void setup() {
        System.out.println("🧪 BetaCellAgent " + getLocalName() + " démarré.");

        //BetaCellClock = new ThomasNetwork(0, 0, 0, "Glucose", 0, "Insuline", 0.64, 0.34);
        BetaCellClock = new ThomasNetwork(1, 0, 0, "Glucose", 0, "Insuline", 0, 0);

        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) {
            @Override
            protected void onTick() {
                EnvlocalPancreas.getInstance().updateFromEnvironment();
                double glycemie = EnvlocalPancreas.getInstance().getLocalGlycemie();
                double insuline = EnvlocalPancreas.getInstance().getLocalInsuline();

                insuline_high = insuline >= INSULINE_TRESHOLD_BETA; // Seuil d'insuline
                glycemie_high = glycemie >= GLYCEMIE_TRESHOLD_BETA; // Seuil de glycémie

                BetaCellClock.update(glycemie_high, insuline_high);


                boolean g_pc_high_beta = BetaCellClock.getG() == 1 && BetaCellClock.getPC() == 1;

                ACLMessage msg = receive();
                if (msg != null) {
                    //int pcgFlag = Integer.parseInt(msg.getContent());
                    //boolean g_pc_high_pancreas = pcgFlag == 1;
                    boolean g_pc_high_pancreas = Boolean.parseBoolean(msg.getContent());


                    //String content = msg.getContent(); // ex: "75.4;1"
                    //String[] parts = content.split(";");
                    //if (parts.length == 2) {
                        //double glycemie = Double.parseDouble(parts[0]);
                        //boolean g_pc_high = parts[1].equals("1");

                        // Exemple de logique : insuline si glycémie haute, mais ralentie si G_PC_HIGH est true
                        if (glycemie > 60) {
                            if (!g_pc_high_pancreas) {
                                if (!g_pc_high_beta) {
                                    EnvironmentModel.getInstance().addInsulin(INSULIN_RELEASE_AMOUNT);
                                } else {
                                    EnvironmentModel.getInstance().addInsulin(INSULIN_RELEASE_AMOUNT_INDUCED_beta);
                                }
                            } else {
                                if (!g_pc_high_beta) {
                                    EnvironmentModel.getInstance().addInsulin(INSULIN_RELEASE_AMOUNT_INDUCED_pancreas);
                                } else {
                                    EnvironmentModel.getInstance().addInsulin(INSULIN_RELEASE_AMOUNT_INDUCED_both);
                                }
                            }
                        } else {
                            System.out.println("Glycémie trop basse pour libérer de l'insuline.");
                        }
                        
                    //}
                } else {
                    block();
                }
            }

        });
    }
}
