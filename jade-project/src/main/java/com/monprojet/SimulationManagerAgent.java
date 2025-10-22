package com.monprojet;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class SimulationManagerAgent extends Agent {

    private GraphUpdater graphUpdater;

    @Override
    protected void setup() {
        System.out.println("✅ SimulationManagerAgent démarré.");

        graphUpdater = new GraphUpdater();

        // Démarrage d'un TickerBehaviour toutes les 1000 ms (1 seconde)
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                graphUpdater.updateAllGraphs();
            }
        });
    }
}
