package com.monprojet;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

// Cette classe permet de créer et lancer le conteneur JADE du projet qui contiendra les différents agents non commentés.

// dans notre cas nous rejoignons un conteneur principal (Main-Container) qui est la GUI JADE lancée en lignes de commande

public class HumanContainer {
    public static void main(String[] args) {
        try {
            // Connexion au Main-Container existant (GUI JADE)
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl(false); // ⬅ "false" pour ne PAS créer un nouveau Main-Container
            profile.setParameter(Profile.MAIN_HOST, "localhost"); // ⬅ Connexion au conteneur principal
            AgentContainer humanContainer = runtime.createAgentContainer(profile); // ⬅ Se connecter au Main-Container
            

            // Démarrage des agents
            AgentController nscAgent = humanContainer.createNewAgent("NSC", NSCAgent.class.getName(), null);
            AgentController environmentAgent = humanContainer.createNewAgent("Environment", EnvironmentAgent.class.getName(), null);
            AgentController liverAgent = humanContainer.createNewAgent("Liver", "com.monprojet.LiverAgent", null);
            AgentController ThalamusAgent = humanContainer.createNewAgent("Thalamus", "com.monprojet.ThalamusAgent", null);

            
            // Lancer les agents
            nscAgent.start();
            environmentAgent.start();
            liverAgent.start();
            ThalamusAgent.start();

            // Démarrer les organes comme agents JADE
            //AgentController clockAgent = humanContainer.createNewAgent("Clock", "com.monprojet.MasterClockAgent", null);
            //AgentController liverAgent = humanContainer.createNewAgent("Liver", "com.monprojet.LiverAgent", null);
            //AgentController pancreasAgent = humanContainer.createNewAgent("Pancreas", "com.monprojet.PancreasAgent", null);
            //AgentController muscleAgent = humanContainer.createNewAgent("Muscle", "com.monprojet.MusclesAgent", null);
            //AgentController adiposeTissueAgent = humanContainer.createNewAgent("AdiposeTissue", "com.monprojet.BATAgent", null);
            //AgentController immuneSystemAgent = humanContainer.createNewAgent("ImmuneSystem", "com.monprojet.ImmuneAgent", null);
            //AgentController cortisolAgent = humanContainer.createNewAgent("Cortisol", "com.monprojet.CortisolAgent", null);
            //AgentController melatoninAgent = humanContainer.createNewAgent("Melatonin", "com.monprojet.MelatoninAgent", null);
            //AgentController ThalamusAgent = humanContainer.createNewAgent("Thalamus", "com.monprojet.ThalamusAgent", null);

            // Lancer les agents
            //clockAgent.start();
            //liverAgent.start();
            //pancreasAgent.start();
            //muscleAgent.start();
            //adiposeTissueAgent.start();
            //immuneSystemAgent.start();
            //cortisolAgent.start();
            //melatoninAgent.start();
            //ThalamusAgent.start();
            

            System.out.println("✅ HumanContainer started and connected to GUI JADE.");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
