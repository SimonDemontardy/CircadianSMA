package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class PancreasAgent extends Agent {


    private double local_glycemie;
    //private double local_cortisol;
    private double GLUCOSE_HIGH_THRESHOLD = 60.0; 
    //private double CORTISOL_HIGH_TRESHOLD = 1.0; // Seuil de cortisol pour la libération de glucagon
    public static ThomasNetwork PancreasClock;
    private boolean Glucose_High_Pancreas = false;
    //private boolean Cortisol_High_Pancreas = false;
    public static boolean G_PC_HIGH_Pancreas = false; // Indique si G et PC sont élevés
    

    @Override
    protected void setup() {

        System.out.println(" Pancreas Agent " + getLocalName() + " démarré.");

        //PancreasClock = new ThomasNetwork(0, 1, 0, "Glucose", 0, "Cortisol", 1,1);
        //PancreasClock = new ThomasNetwork(0, 0, 0, "Glucose", 0.46, 0.54);
        PancreasClock = new ThomasNetwork(1, 0, 0, "Glucose", 0, 0);

        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) {
            @Override
            protected void onTick() {

                // Récupération du niveau de glucose dans l'environnement
                EnvlocalPancreas.getInstance().updateFromEnvironment();
                local_glycemie = EnvlocalPancreas.getInstance().getLocalGlycemie();
                //local_cortisol = EnvlocalPancreas.getInstance().getLocalCortisol();


                Glucose_High_Pancreas = local_glycemie >= GLUCOSE_HIGH_THRESHOLD;
                //Cortisol_High_Pancreas = local_cortisol >= CORTISOL_HIGH_TRESHOLD;
                //PancreasClock.update(Glucose_High_Pancreas, Cortisol_High_Pancreas);
                PancreasClock.update(Glucose_High_Pancreas);
                //PancreasClock.printState();
                // récuperation de l'instancedu glucose dans l'environnement

                // Mise à jour de l'état de G et PC
                G_PC_HIGH_Pancreas = PancreasClock.getG() == 1 && PancreasClock.getPC() == 1;

                // Construction d'un message combiné
                //String content = local_glycemie + ";" + G_PC_HIGH_Pancreas;
                String content = String.valueOf(G_PC_HIGH_Pancreas);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new jade.core.AID("BetaCell", jade.core.AID.ISLOCALNAME));
                //msg.addReceiver(new jade.core.AID("AlphaCell", jade.core.AID.ISLOCALNAME));
                msg.setContent(content);
                send(msg);
            }
        });

    }

}