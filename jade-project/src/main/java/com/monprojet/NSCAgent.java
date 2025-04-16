package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.application.Platform;

// cette classe représente l'agent NSC (Noyau Suprachiasmatique) qui possède un cycle circadien influencé 
// par la lumière et agissant sur la production de cortisol.

public class NSCAgent extends Agent {

    // section précédemment utilisée pour secréter le cortisol de manière fixe.
    //private int currentTime = 0; // Simule une horloge interne (0-23h)
    
    // Tableau de sécrétion du cortisol basé sur un cycle réaliste (approximé)
    //private static final double[] CORTISOL_SECRETION = {
        //2.0, 2.5, 3.0, 4.0, 6.5, 9.0, 10.5, 12.0, 11.0, 9.5, // 0h - 9h
        //7.0, 6.0, 5.5, 5.0, 4.5, 4.2, 4.0, 3.8, 3.5, 3.2, // 10h - 19h
        //3.0, 2.8, 2.5, 2.2 // 20h - 23h
    //};
    // Tableau de sécrétion du cortisol basé uniquement sur les grosses phases
    //private static final double[] CORTISOL_SECRETION = {
       // 0.0, 0.0, 0.0, 0.0, 4.0, 7.0, 9.0, 11.0, 10.0, 8.5, // 0h - 9h
       // 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, // 10h - 19h
       // 0.0, 0.0, 0.0, 0.0 // 20h - 23h
       // };
    
    /*private static final double[] CORTISOL_SECRETION = {
        0.0, 0.0, 0.0, 0.0, 0.0, 5.0, 10.0, 10.0, 5.0, 0.0, // 0h - 9h
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, // 10h - 19h
        0.0, 0.0, 0.0, 0.0 // 20h - 23h
        };
*/

    // initialisations
    private ThomasNetwork nscClock; // horloge circadienne
    private boolean LightHigh = false; // Lumière
    public static double currentTime = 0.0; // Heure biologique continue


    @Override
    protected void setup() {
        // indique le lancement de l'agent
        System.out.println("NSC Agent " + getLocalName() + " démarré.");

        // attribution des valeurs initiales au cycle circadien
        // G: la forme libre de PER/CRY: 0 car on démarre à minuit
        // PC: la forme complexe de PER/CRY: 1 car on démarre à minuit
        // la ressource principale, ici lumière: 0 car on démarre à minuit
        nscClock= new ThomasNetwork(0, 1, 0, "Light", 1,1); // parce que on démarre à minuit et en géneral la nuit cortisol inibé donc pC à 1 et G 0

        // lancement de l'interface graphique pour suivre les informations en temps réel
        new Thread(() -> javafx.application.Application.launch(LivePlot.class)).start();

        // lancement du comportement de l'agent
        addBehaviour(new TickerBehaviour(this, (long) (ThomasNetwork.dt * 1000)) { // Convertit dt en millisecondes
            @Override
            protected void onTick() {
                // on augmente le temps réel uniquement dans cette classe.
                currentTime += ThomasNetwork.dt;

                // affichage de l'heure biologique au niveau du NSC
                System.out.println("A/ NSC: Heure biologique  " + String.format("%.2f", currentTime) + "h");

                // Déterminer si la lumière est activée (jour)
                LightHigh = (currentTime % 24 >= 6) && (currentTime % 24 < 18); // Lumière active entre 6h et 18h
                //LightHigh = false; // Lumière désactivée pour les tests
                if (LightHigh) {
                    System.out.println("B/ lumière state ☀️ NSC: Lumière activée !");
                } else {
                    System.out.println("B/ lumière state 🌙 NSC: Lumière désactivée.");
                }

                // Exécuter l'horloge NSC avec la lumière comme ressource
                // on appelle la méthode de thomas network avec uniquement la lumiere comme ressource
                nscClock.update(LightHigh);
                // méthode pour afficher chaque état de l'horloge
                nscClock.printState();

                // Mise à jour du graphique en direct pour NSC
                Platform.runLater(() -> {
                    LivePlot.updateNSCChart(
                        nscClock.getCurrentTime(),
                        nscClock.getRessourceLevel(), // Lumière
                        nscClock.getG(),
                        nscClock.getPC()
                    );
                });


                // Si PC est à 1, inibition de la sécrétion de cortisol
                if (nscClock.getPC() == 1) {
                    System.out.println("C/ Message au Thalamus: 🛑 NSC: PC=1 → Sécrétion de cortisol INHIBÉE.");
                    return; // Ne pas envoyer de message
                }

                // Si PC = 0, envoie d'un message au Thalamus pour libérer du cortisol
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(getAID("Thalamus")); //thalamus receveur
                msg.setContent("Produce Cortisol"); // content du message
                send(msg); // envoi du message
                System.out.println("C/ Message au Thalamus 📢 NSC: cortisol sécrétion!"); // affichage confirmation d'envoi
            }
        });
    }
    // ancienne méthode pour libérer le cortisol
    /*private void releaseCortisol(double amount) {
        System.out.println("NSC: Libération de " + amount + " µg/dL de cortisol à " + currentTime + "h");
        
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(getAID("Environment")); // Envoi au système sanguin
        msg.setContent("Cortisol: " + amount);
        send(msg);
    }*/
}
