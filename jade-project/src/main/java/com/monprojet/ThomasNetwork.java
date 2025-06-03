package com.monprojet;

import java.util.*;

// Cette classe contient le modèle de Thomas pour la simulation du cycle circadien.
// elle est adaptable à plusieurs appels de modèle avec différents constructeurs. 
   
public class ThomasNetwork {
    // initialisation des variables
    private int G; // forme libre de PER/CRY
    private int PC; // forme complexe de PER/CRY
    private int ampk; // état AMPK 
    private int ressource; // ressource (cortisol ou lumière) 1ere ressource
    private String ressourceType; // ressource utilisée
    private double x, y; // valeurs de x et y évolutives en fonction des status de G et PC
    private double currentTime; // heure biologique continue
    private static final double epsilon = 0.05; // valeur epsilon pour la gestion des transitions
    public static final double dt = 1; // pas de temps en heures

    // on crée plusieurs maps de vitesses qui seront utilisées par les appels des différentes horloges circadiennes et
    // de leurs ressources respectives.

    // Définition des célérités
    // cas du foie
    private static final Map<List<Integer>, double[]> transitionCeleritiesCortisol = new HashMap<List<Integer>, double[]>() {{
        put(Arrays.asList(0, 0, 0), new double[]{0.14, -0.18}); // vkg/pc/C, vkpc
        put(Arrays.asList(0, 1, 0), new double[]{-0.14, -0.13}); // vkg/C, vkpc
        put(Arrays.asList(1, 0, 0), new double[]{0.23, 0.13}); // vkg/pc/C, vkpc/g
        put(Arrays.asList(1, 1, 0), new double[]{-0.22, 0.25}); // vkg/C, vkpc/g
        // avec cortisol = 1 donc inib est pas ressource 
        put(Arrays.asList(0, 0, 1), new double[]{0.18, -0.18}); // vkg/pc, vkpc
        put(Arrays.asList(0, 1, 1), new double[]{-0.13, -0.13}); // vkg, vkpc
        put(Arrays.asList(1, 0, 1), new double[]{0.25, 0.13}); // vkg/pc, vkpc/g
        put(Arrays.asList(1, 1, 1), new double[]{-0.18, 0.25}); // vkg, vkpc/g
    }};

    // cas du NSC
    // vitesses originelles thèse émilien
     private static final Map<List<Integer>, double[]> transitionCeleritiesLight = new HashMap<List<Integer>, double[]>() {{
        put(Arrays.asList(0, 0, 0), new double[]{0.18, -0.18}); // vkg/pc, vkpc
        put(Arrays.asList(0, 1, 0), new double[]{-0.13, -0.13}); // vkg, vkpc
        put(Arrays.asList(1, 0, 0), new double[]{0.25, 0.13}); // vkg/pc, vkpc/g
        put(Arrays.asList(1, 1, 0), new double[]{-0.18, 0.25}); // vkg, vkpc/g

        put(Arrays.asList(0, 0, 1), new double[]{0.22, -0.18}); // vkg/pc/L, vkpc
        put(Arrays.asList(0, 1, 1), new double[]{-0.13, -0.13}); // vkg/L, vkpc
        put(Arrays.asList(1, 0, 1), new double[]{0.25, 0.13}); // vkg/pc/L, vkpc/g
        put(Arrays.asList(1, 1, 1), new double[]{-0.1, 0.25}); // vkg/L, vkpc/g
    }};
    // cas du foie mais avec prise en compte de l'action de l'AMPK
    private static final Map<List<Integer>, double[]> transitionCeleritiesAMPK_andC = new HashMap<List<Integer>, double[]>() {{
        put(Arrays.asList(0, 0, 0, 0), new double[]{0.14, -0.18}); // vkg/pc/C, vkpc
        put(Arrays.asList(0, 1, 0, 0), new double[]{-0.14, -0.13}); // vkg/C, vkpc
        put(Arrays.asList(1, 0, 0, 0), new double[]{0.23, 0.13}); // vkg/pc/C, vkpc/g
        put(Arrays.asList(1, 1, 0, 0), new double[]{-0.22, 0.25}); // vkg/C, vkpc/g
        // avec cortisol = 1 donc inib est pas ressource 
        put(Arrays.asList(0, 0, 1, 0), new double[]{0.18, -0.18}); // vkg/pc, vkpc
        put(Arrays.asList(0, 1, 1, 0), new double[]{-0.13, -0.13}); // vkg, vkpc
        put(Arrays.asList(1, 0, 1, 0), new double[]{0.25, 0.13}); // vkg/pc, vkpc/g
        put(Arrays.asList(1, 1, 1, 0), new double[]{-0.18, 0.25}); // vkg, vkpc/g

        // avec ampk = 1
        put(Arrays.asList(0, 0, 0, 1), new double[]{0.14, -0.16}); // vkg/pc/C, vkpc/MK
        put(Arrays.asList(0, 1, 0, 1), new double[]{-0.14, -0.11}); // vkg/C, vkpc/MK
        put(Arrays.asList(1, 0, 0, 1), new double[]{0.23, 0.15}); // vkg/pc/C, vkpc/g/MK
        put(Arrays.asList(1, 1, 0, 1), new double[]{-0.22, 0.25}); // vkg/C, vkpc/g/MK
        // avec cortisol = 1 donc inib est pas ressource 
        put(Arrays.asList(0, 0, 1, 1), new double[]{0.18, -0.16}); // vkg/pc, vkpc/MK
        put(Arrays.asList(0, 1, 1, 1), new double[]{-0.13, -0.11}); // vkg, vkpc/MK
        put(Arrays.asList(1, 0, 1, 1), new double[]{0.25, 0.15}); // vkg/pc, vkpc/g/MK
        put(Arrays.asList(1, 1, 1, 1), new double[]{-0.18, 0.25}); // vkg, vkpc/g/MK
    }};
    
    // Constructeurs:

    // constructeur sans AMPK avec soit cortisol soit lumière
    public ThomasNetwork(int initialG, int initialPC, int initialRessource, String ressourceType, double initialX, double initialY) {
        this.G = initialG;
        this.PC = initialPC;
        //this.C = initialC;
        this.ressource = initialRessource;
        this.ressourceType = ressourceType;
        this.currentTime = 0.0;
        this.x = 1.0;
        this.y = 1.0;
    }

    // constructeur avec AMPK avec soit cortisol soit lumière
    public ThomasNetwork(int initialG, int initialPC, int initialRessource, String ressourceType, double initialX, double initiaLY, int initialAMPK) {
        this.G = initialG;
        this.PC = initialPC;
        this.ressource = initialRessource;
        this.ressourceType = ressourceType;
        this.currentTime = 0.0;
        this.x = 1.0;
        this.y = 1.0;
        this.ampk = initialAMPK;
    }

    // update sans AMPK
    public void update(boolean ressourceActive){
        this.ressource = ressourceActive ? 1 : 0;
        //this.C = cortisolHigh ? 1 : 0;
        updatecore();
    }

    // update avec AMPK
    public void update(boolean ressourceActive, int ampkActive){
        this.ressource = ressourceActive ? 1 : 0;
        //this.C = cortisolHigh ? 1 : 0;
        this.ampk = ampkActive;
        System.out.println("AMPK: " + this.ampk);
        updatecore();
    }

    // mise à jours des variable de l'horloge par l'appel lors des behavior de chaque agent.
    public void updatecore() {

        // on met à jour les valeurs de x et y en fonction des célérités obtenues selon les ressources du systeme.
        // Si x ou y dans l'etat actuel atteint un seuil epsilon, on change de status pour PC et G selon un cycle précis.
        // cela peut être une transition claire ou en dépassement
        
        double[] celerities = getCurrentCelerities();
        
        x += celerities[0] * dt;
        y += celerities[1] * dt;
        currentTime += dt;
        
        // Gestion des transitions dans le cas où le glissement fait une transition instantanée et la provoque
        /* 
        if (G == 1 && PC == 1) {
            if (x <= epsilon) {
                G = 0; PC = 1; x = 1;
            } else if (y >= 1 - epsilon) {
                G = 0; PC = 1; x = 1; y = 1;
            }
        } else if (G == 0 && PC == 1) {
            if (y <= epsilon) {
                G = 0; PC = 0; y = 1;
            } else if (x <= epsilon) {
                G = 0; PC = 0; x = 0; y = 1;
            }
        } else if (G == 0 && PC == 0) {
            if (x >= 1 - epsilon) {
                G = 1; PC = 0; x = 0;
            } else if (y <= epsilon) {
                G = 1; PC = 0; x = 0; y = 0;
            }
        } else if (G == 1 && PC == 0) {
            if (y >= 1 - epsilon) {
                G = 1; PC = 1; y = 0;
            } else if (x >= 1 - epsilon) {
                G = 1; PC = 1; x = 1; y = 0;
            }
        } */
        if (G == 1 && PC == 1) {
            if (x <= epsilon) {
                G = 0; PC = 1; x = 1;
            } else {
                if (y >= 1 - epsilon) {
                    y = 1;
                }
            }
        } else if (G == 0 && PC == 1) {
            if (y <= epsilon) {
                G = 0; PC = 0; y = 1;
            } else {
                if (x <= epsilon) {
                    x = 0;
                }
            }
        } else if (G == 0 && PC == 0) {
            if (x >= 1 - epsilon) {
                G = 1; PC = 0; x = 0;
            } else {
                if (y <= epsilon) {
                    y = 0;
                }
            }
        } else if (G == 1 && PC == 0) {
            if (y >= 1 - epsilon) {
                G = 1; PC = 1; y = 0;
            } else {
                if (x >= 1 - epsilon) {
                    x = 1;
                }
            }
        }
    }

    // méthode pour obtenir les célérités actuelles en fonction des ressources concernées
    // on récupère le message initial du constructeur et on attribue la plage de célérités correspondante
    public double[] getCurrentCelerities() {
        // cas uniquement du cortisol
        if (ressourceType.equals("Cortisol")) {
            return transitionCeleritiesCortisol.get(Arrays.asList(G, PC, ressource));
        // cas uniquement de la lumière
        } else if (ressourceType.equals("Light")) {
            return transitionCeleritiesLight.get(Arrays.asList(G, PC, ressource));
        // cas du cortisol et de l'AMPK
        } else if (ressourceType.equals("Cortisol and AMPK")) {
            return transitionCeleritiesAMPK_andC.get(Arrays.asList(G, PC, ressource, ampk));
        } else {
        // cas d'erreur
            System.out.println("Type de ressource inconnu. IMPOSSIBLE de continuer.");
            return new double[]{0.0, 0.0}; // Default return value
        }
    }

    // méthode pour afficher l'état de l'horloge circadienne
    public void printState() {
        System.out.println("⏳ Temps: " + NSCAgent.currentTime + "h | G: " + G + " | PC: " + PC + " | Resource (" + ressourceType + "): " + ressource + " | x: " + x + " | y: " + y);
        System.out.println("///////////////////////");
    }

    // getters and setters
    public double getCurrentTime() {
        return currentTime;
    }
    
    public double getRessourceLevel() {
        return (double) ressource;
    }
    
    public int getG() {
        return G;
    }
    
    public int getPC() {
        return PC;
    }
}
