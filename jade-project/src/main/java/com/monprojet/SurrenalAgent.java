package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

// Classe ThalamusAgent qui s'occupe de la sécrétion hormonale
// nom à définir dans le futur.
public class SurrenalAgent extends Agent {
    // initialisation des constantes de sécretion hormonale
    private static final double CORTISOL_RELEASE_AMOUNT_FREE = 5.0; // µg/dL envoyés à l'environnement en fonction du NSC
    private static final double CORTISOL_RELEASE_AMOUNT_INIB_NSC = 2.5; // µg/dL envoyés à l'environnement en fonction du NSC
    private static final double CORTISOL_RELEASE_AMOUNT_INIB_CLOCK = 2.0; // µg/dL envoyés à l'environnement en fonction de l'horloge circadienne
    private static final double CORTISOL_RELEASE_AMOUNT_ULTRA_INIB = 1.0; // µg/dL envoyés à l'environnement en fonction de l'ultra inhibition


    private static final double CORTISOL_THRESHOLD_SURRENAL = 2.0; // Seuil de cortisol pour la surrénale
    //private static final double INSULIN_RELEASE_AMOUNT = 5.0; // Unité arbitraire pour l'insuline
    //private static final double GLUCAGON_RELEASE_AMOUNT = 5.0; // Unité arbitraire pour le glucagon

    private static boolean Cortisol_High_surrenal = false;

    public static ThomasNetwork SurrenalClock; // horloge circadienne pour la surrénale

    @Override
    protected void setup() {

        // confirmation de l'initialisation de l'agent
        System.out.println(" SurrenalAgent " + getLocalName() + " démarré.");

        // initialisation de l'horloge circadienne pour la surrénale
        //SurrenalClock = new ThomasNetwork(0, 0, 0, "Cortisol", 0.79, 0.18);
        SurrenalClock = new ThomasNetwork(1, 0, 0, "Cortisol", 0, 0);

        // comportement de tick pour gérer la sécrétion hormonale
        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) {
            @Override
            protected void onTick() {

                EnvironmentModel env = EnvironmentModel.getInstance();
                double cortisol_surr = env.getCortisolLevel();
                Cortisol_High_surrenal = cortisol_surr >= CORTISOL_THRESHOLD_SURRENAL;

                SurrenalClock.update(Cortisol_High_surrenal);
                //SurrenalClock.printState();





                // initialisation de la variable pour éviter les envois multiples
                boolean alreadyHandled = false;
                double glycemie = EnvironmentModel.getInstance().getGlucoseLevel();

                // récupération des messages de l'agent NSC
                ACLMessage msg;
                while ((msg = receive()) != null) {
                    // on récupère le message de l'agent NSC pour déterminer la secretion ou non du cortisol
                    // conditions : 1 seul message de production de cortisol par tick
                    if (!alreadyHandled && msg.getContent().equals("Cortisol Inhibition")) {
                        // on crée un message pour l'environnement et on envoie le amount of cortisol correspondant
                        /*ACLMessage envMsg = new ACLMessage(ACLMessage.INFORM);
                        envMsg.addReceiver(getAID("Environment"));
                        envMsg.setContent("Cortisol: " + CORTISOL_RELEASE_AMOUNT);
                        send(envMsg);
                        alreadyHandled = true;
                        System.out.println("📢 Thalamus: Envoi de " + CORTISOL_RELEASE_AMOUNT + " µg/dL de cortisol.");*/
                        if (glycemie < 40) {
                            if (SurrenalClock.getPC() == 1 && SurrenalClock.getG() == 1) {
                                EnvironmentModel.getInstance().addCortisol(CORTISOL_RELEASE_AMOUNT_ULTRA_INIB);
                            } else {
                                EnvironmentModel.getInstance().addCortisol(CORTISOL_RELEASE_AMOUNT_INIB_NSC);
                            }

                        }
                        else {
                        }

                    } else if(!alreadyHandled && msg.getContent().equals("Cortisol free")) {
                        if (glycemie < 40) {
                            if (SurrenalClock.getPC() == 1 && SurrenalClock.getG() == 1) {
                                EnvironmentModel.getInstance().addCortisol(CORTISOL_RELEASE_AMOUNT_INIB_CLOCK);
                            } else {
                                EnvironmentModel.getInstance().addCortisol(CORTISOL_RELEASE_AMOUNT_FREE);
                            }

                        }
                        else {
                        }


                    }
                    
                }

                /*// on récupère l'heure biologique continue pour éviter les décalages
                double time = NSCAgent.currentTime % 24;

                // 1. Sécrétion d'insuline à chaque repas (8h, 12h, 20h) format guillaume
                if (time == 8 || time == 12 || time == 20) {
                    ACLMessage envinsuline = new ACLMessage(ACLMessage.INFORM);
                    envinsuline.addReceiver(getAID("Environment"));
                    envinsuline.setContent("Insuline: " + INSULIN_RELEASE_AMOUNT);
                    send(envinsuline);
                    System.out.println("ENVOI INSULINE à " + time + "h");
                }
                
                // 2. Sécrétion de glucagon entre les repas (jeûne)
                else if ((time >= 14 && time < 20) || (time >= 22 || time < 8)) {
                    ACLMessage envglucagon = new ACLMessage(ACLMessage.INFORM);
                    envglucagon.addReceiver(getAID("Environment"));
                    envglucagon.setContent("Glucagon: " + GLUCAGON_RELEASE_AMOUNT);
                    send(envglucagon);
                    System.out.println("ENVOI GLUCAGON à " + time + "h");
                }
                
                // 3. Pas de sécrétion hormonale ( n'arrive pas)
                else {
                    System.out.println("Pas de sécrétion hormonale à " + time + "h");
                }*/


                // sécrétion hormonale basée sur la glycémie:
    

            }
        });
    }
    
    // sert à rien vu ma méthode d'acces de graphUpdater
    public static double getRessourceLevel_Surrenal() {
        return SurrenalClock.getRessourceLevel();
    }
    public static int getG_Surrenal() {
        return SurrenalClock.getG();
    }
    public static int getPC_Surrenal() {
        return SurrenalClock.getPC();
    }
}
