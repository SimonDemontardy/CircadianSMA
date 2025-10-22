package com.monprojet;

import java.util.*;

// Cette classe contient le modèle de Thomas pour la simulation du cycle circadien.
// elle est adaptable à plusieurs appels de modèle avec différents constructeurs. 
   
public class ThomasNetwork {
    // initialisation des variables
    private int G; // forme libre de PER/CRY
    private int PC; // forme complexe de PER/CRY
    //private int ampk; // état AMPK 
    private int ressource; // ressource (cortisol ou lumière) 1ere ressource
    private String ressourceType; // ressource utilisée
    private int ressource2; // 2eme ressource 
    private String ressource2Type; // type de la 2eme ressource
    private double x, y; // valeurs de x et y évolutives en fonction des status de G et PC
    private double currentTime; // heure biologique continue
    private static final double epsilon = 0.05; // valeur epsilon pour la gestion des transitions
    public static final double dt = 1; // pas de temps en heures

    // on crée plusieurs maps de vitesses qui seront utilisées par les appels des différentes horloges circadiennes et
    // de leurs ressources respectives.

    // Définition des célérités
    // cas du foie
    private static final Map<List<Integer>, double[]> transitionCeleritiesCortisol = new HashMap<List<Integer>, double[]>() {{
        // cortisol = 0 donc il est ressource 
        put(Arrays.asList(0, 0, 0), new double[]{0.21, -0.18}); // vkg/pc/C, vkpc
        put(Arrays.asList(0, 1, 0), new double[]{-0.10, -0.13}); // vkg/C, vkpc
        put(Arrays.asList(1, 0, 0), new double[]{0.27, 0.13}); // vkg/pc/C, vkpc/g
        put(Arrays.asList(1, 1, 0), new double[]{-0.15, 0.25}); // vkg/C, vkpc/g
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
        // avec lumiere = 1 or activateur donc ressource
        put(Arrays.asList(0, 0, 1), new double[]{0.21, -0.18}); // vkg/pc/L, vkpc
        put(Arrays.asList(0, 1, 1), new double[]{-0.10, -0.13}); // vkg/L, vkpc
        put(Arrays.asList(1, 0, 1), new double[]{0.27, 0.13}); // vkg/pc/L, vkpc/g
        put(Arrays.asList(1, 1, 1), new double[]{-0.15, 0.25}); // vkg/L, vkpc/g
    }};
    // cas du foie mais avec prise en compte de l'action de l'AMPK
    private static final Map<List<Integer>, double[]> transitionCeleritiesAMPK_andC = new HashMap<List<Integer>, double[]>() {{
        put(Arrays.asList(0, 0, 0, 0), new double[]{0.21, -0.18}); // vkg/pc/C, vkpc
        put(Arrays.asList(0, 1, 0, 0), new double[]{-0.10, -0.13}); // vkg/C, vkpc
        put(Arrays.asList(1, 0, 0, 0), new double[]{0.27, 0.13}); // vkg/pc/C, vkpc/g
        put(Arrays.asList(1, 1, 0, 0), new double[]{-0.15, 0.25}); // vkg/C, vkpc/g
        // avec cortisol = 0 donc inib est ressource 
        put(Arrays.asList(0, 0, 1, 0), new double[]{0.18, -0.18}); // vkg/pc, vkpc
        put(Arrays.asList(0, 1, 1, 0), new double[]{-0.13, -0.13}); // vkg, vkpc
        put(Arrays.asList(1, 0, 1, 0), new double[]{0.25, 0.13}); // vkg/pc, vkpc/g
        put(Arrays.asList(1, 1, 1, 0), new double[]{-0.18, 0.25}); // vkg, vkpc/g

        // avec ampk = 1
        put(Arrays.asList(0, 0, 0, 1), new double[]{0.21, -0.16}); // vkg/pc/C, vkpc/MK
        put(Arrays.asList(0, 1, 0, 1), new double[]{-0.10, -0.11}); // vkg/C, vkpc/MK
        put(Arrays.asList(1, 0, 0, 1), new double[]{0.27, 0.15}); // vkg/pc/C, vkpc/g/MK
        put(Arrays.asList(1, 1, 0, 1), new double[]{-0.15, 0.27}); // vkg/C, vkpc/g/MK
        // avec cortisol = 1 donc inib est pas ressource 
        put(Arrays.asList(0, 0, 1, 1), new double[]{0.18, -0.16}); // vkg/pc, vkpc/MK
        put(Arrays.asList(0, 1, 1, 1), new double[]{-0.13, -0.11}); // vkg, vkpc/MK
        put(Arrays.asList(1, 0, 1, 1), new double[]{0.25, 0.15}); // vkg/pc, vkpc/g/MK
        put(Arrays.asList(1, 1, 1, 1), new double[]{-0.18, 0.27}); // vkg, vkpc/g/MK
    }};

    private static final Map<List<Integer>, double[]> transitionCeleritiesGlucose = new HashMap<List<Integer>, double[]>() {{
        put(Arrays.asList(0, 0, 0), new double[]{0.18, -0.18}); // vkg/pc, vkpc
        put(Arrays.asList(0, 1, 0), new double[]{-0.13, -0.13}); // vkg, vkpc
        put(Arrays.asList(1, 0, 0), new double[]{0.25, 0.13}); // vkg/pc, vkpc/g
        put(Arrays.asList(1, 1, 0), new double[]{-0.18, 0.25}); // vkg, vkpc/g

        put(Arrays.asList(0, 0, 1), new double[]{0.22, -0.18}); // vkg/pc/G, vkpc
        put(Arrays.asList(0, 1, 1), new double[]{-0.10, -0.13}); // vkg/G, vkpc
        put(Arrays.asList(1, 0, 1), new double[]{0.27, 0.13}); // vkg/pc/G, vkpc/g
        put(Arrays.asList(1, 1, 1), new double[]{-0.15, 0.25}); // vkg/G, vkpc/g
    }};

    private static final Map<List<Integer>, double[]> transitionCeleritiesGlu_andIns = new HashMap<List<Integer>, double[]>() {{
        // insuline = 0 or inib g donc ressource quand pas là
        put(Arrays.asList(0, 0, 0, 0), new double[]{0.18, -0.17}); // vkg/pc, vkpc/ins
        put(Arrays.asList(0, 1, 0, 0), new double[]{-0.13, -0.12}); // vkg, vkpc/ins
        put(Arrays.asList(1, 0, 0, 0), new double[]{0.25, 0.14}); // vkg/pc, vkpc/g/ins
        put(Arrays.asList(1, 1, 0, 0), new double[]{-0.18, 0.26}); // vkg, vkpc/g/ins
        // glucose = 1 or activateur donc ressource
        // insuline = 0 or inib g donc ressource quand pas là
        put(Arrays.asList(0, 0, 1, 0), new double[]{0.20, -0.17}); // vkg/pc/glu, vkpc/ins
        put(Arrays.asList(0, 1, 1, 0), new double[]{-0.11, -0.12}); // vkg/glu, vkpc/ins
        put(Arrays.asList(1, 0, 1, 0), new double[]{0.27, 0.14}); // vkg/pc/glu, vkpc/g/ins
        put(Arrays.asList(1, 1, 1, 0), new double[]{-0.16, 0.26}); // vkg/glu, vkpc/g/ins

        //put(Arrays.asList(0, 0, 1, 0), new double[]{0.18, -0.18}); // vkg/pc/glu/ins, vkpc
        //put(Arrays.asList(0, 1, 1, 0), new double[]{-0.13, -0.13}); // vkg/glu/ins, vkpc
        //put(Arrays.asList(1, 0, 1, 0), new double[]{0.25, 0.13}); // vkg/pc/glu/ins, vkpc/g
        //put(Arrays.asList(1, 1, 1, 0), new double[]{-0.18, 0.25}); // vkg/glu/ins, vkpc/g

        //// insuline = 1 or inib g donc ressource quand pas là
        put(Arrays.asList(0, 0, 1, 0), new double[]{0.18, -0.18}); // vkg/pc, vkpc
        put(Arrays.asList(0, 1, 1, 0), new double[]{-0.13, -0.13}); // vkg, vkpc
        put(Arrays.asList(1, 0, 1, 0), new double[]{0.25, 0.13}); // vkg/pc, vkpc/g
        put(Arrays.asList(1, 1, 1, 0), new double[]{-0.18, 0.25}); // vkg, vkpc/g

        // glucose = 1 or activateur donc ressource
        put(Arrays.asList(0, 0, 1, 1), new double[]{0.20, -0.18}); // vkg/pc/glu, vkpc
        put(Arrays.asList(0, 1, 1, 1), new double[]{-0.11, -0.13}); // vkg/glu, vkpc
        put(Arrays.asList(1, 0, 1, 1), new double[]{0.27, 0.13}); // vkg/pc/glu, vkpc/g
        put(Arrays.asList(1, 1, 1, 1), new double[]{-0.16, 0.25}); // vkg/glu, vkpc/g
    }};

    private static final Map<List<Integer>, double[]> transitionCeleritiesGlucose_Cortisol = new HashMap<List<Integer>, double[]>() {{
        //
    }};
    
    // Constructeurs:

    // constructeur sans AMPK avec soit cortisol soit lumière soit glucose
    public ThomasNetwork(int initialG, int initialPC, int initialRessource, String ressourceType, double initialX, double initialY) {
        this.G = initialG;
        this.PC = initialPC;
        //this.C = initialC;
        this.ressource = initialRessource;
        this.ressourceType = ressourceType;
        this.currentTime = 0.0;
        this.x = initialX;
        this.y = initialY;
    }

    // constructeur avec AMPK avec soit cortisol soit lumière
    public ThomasNetwork(int initialG, int initialPC, int initialRessource, String ressourceType, int initialRessource2, String ressource2Type, double initialX, double initialY) {
        this.G = initialG;
        this.PC = initialPC;
        this.ressource = initialRessource;
        this.ressourceType = ressourceType;
        this.currentTime = 0.0;
        this.x = initialX;
        this.y = initialY;
        this.ressource2 = initialRessource2;
        this.ressource2Type = ressource2Type;
        //this.ampk = initialAMPK;
    }

    // update sans AMPK
    public void update(boolean ressourceActive){
        this.ressource = ressourceActive ? 1 : 0;
        updatecore();
    }

    // update avec AMPK
    public void update(boolean ressourceActive, boolean ressource2Active){
        this.ressource = ressourceActive ? 1 : 0;
        //this.ampk = ampkActive;
        this.ressource2 = ressource2Active ? 1 : 0;
        //System.out.println("AMPK: " + this.ampk);
        updatecore();
    }

    // mise à jours des variable de l'horloge par l'appel lors des behavior de chaque agent.
public void updatecore() {

    // mise à jour des valeurs de x et y en fonction des célérités
    double[] celerities = getCurrentCelerities();
    
    x += celerities[0] * dt;
    y += celerities[1] * dt;
    currentTime += dt;
    
    // Clamp pour éviter que x ou y ne dépassent 1 ou ne descendent sous 0
    x = Math.max(0.0, Math.min(1.0, x));
    y = Math.max(0.0, Math.min(1.0, y));

    // Gestion des transitions d'état
    if (G == 1 && PC == 1) {
        if (x <= epsilon) {
            G = 0; PC = 1; 
            x = 1.0; // reset instantané à 1
        }
        if (y >= 1.0 - epsilon) {
            y = 1.0; // clamp à 1
        }
    } 
    else if (G == 0 && PC == 1) {
        if (y <= epsilon) {
            G = 0; PC = 0; 
            y = 1.0; // reset instantané à 1
        }
        if (x <= epsilon) {
            x = 0.0; // clamp à 0
        }
    } 
    else if (G == 0 && PC == 0) {
        if (x >= 1.0 - epsilon) {
            G = 1; PC = 0; 
            x = 0.0; // reset instantané à 0
        }
        if (y <= epsilon) {
            y = 0.0; // clamp à 0
        }
    } 
    else if (G == 1 && PC == 0) {
        if (y >= 1.0 - epsilon) {
            G = 1; PC = 1; 
            y = 0.0; // reset instantané à 0
        }
        if (x >= 1.0 - epsilon) {
            x = 1.0; // clamp à 1
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
        } else if (ressourceType.equals("Cortisol") && ressource2Type.equals("AMPK")) {
            return transitionCeleritiesAMPK_andC.get(Arrays.asList(G, PC, ressource, ressource2));
        // cas du glucose
        } else if (ressourceType.equals("Glucose")){
            return transitionCeleritiesGlucose.get(Arrays.asList(G, PC, ressource)); 
        // cas du glucose et de l'insuline (alphacell)
        } else if (ressourceType.equals("Glucose") && ressource2Type.equals("Insuline")) {
            return transitionCeleritiesGlu_andIns.get(Arrays.asList(G, PC, ressource, ressource2));
        // cas du glucose et du cortisol
        } else if (ressourceType.equals("Glucose") && ressource2Type.equals("Cortisol")) {
            return transitionCeleritiesGlucose_Cortisol.get(Arrays.asList(G, PC, ressource, ressource2));
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
    
    public int getRessourceLevel() {
        return ressource;
    }

    public int getRessource2Level() {
        return ressource2;
    }
    
    public int getG() {
        return G;
    }
    
    public int getPC() {
        return PC;
    }
}
