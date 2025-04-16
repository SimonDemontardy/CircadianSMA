package com.monprojet;

// Cette interface execute les systémes stockage et consommation pour les différents métabolites du foie. 

public interface MetabolismMode {
    void execute(MetabolicState state, int complexePerCry);
}

// classe de stockage
// Cette classe est activée par l'insuline et permet de stocker les glucides, lipides et protéines dans le foie
class StockageMode implements MetabolismMode {

    @Override
    public void execute(MetabolicState s, int complexePerCry) {
        System.out.println("\uD83D\uDCC5 Mode : Stockage activé (insuline)");
        // réactions du stockage avec leurs vitesses obtenues par FBA
        glycogenese(s, 4.46);     // glucose → glycogène
        glycolyse(s, 5);          // glucose → pyruvate + ATP
        pyrToCoA(s, 10);          // pyruvate → AcetylCoA
        if (complexePerCry == 0) {     // influence de PER CRY
            krebs(s, 3.1);
        }else if (complexePerCry == 1 ){
            krebs(s, 10);         // valeur augmentée à étudier
        }                              // AcetylCoA → ATP
        lipogenese(s, 0.86);      // AcetylCoA → AG
        nourish(s, 10);           // import de glucose dans le systeme
        movement(s, 77);          
        //aminoacids(s, 5);
    }
    /*private void aminoacids(MetabolicState s, double rate) {
        s.acidesAmines += rate;
    }*/

    // boite noire d'arrivée de glucose
    private void nourish(MetabolicState s, double rate) {
        s.glucose += rate;

    }

    // mouvement à chaque tick à partir de l'ATP
    private void movement(MetabolicState s, double rate) {
        if (s.atp >= rate) {
            s.atp -= rate;
            //System.out.println("🏃‍♂️ Déplacement : ATP -" + rate);
            System.out.println("UN MOVEMENT A EU LIEU");
        }
    }

    private void glycogenese(MetabolicState s, double rate) {
        if (s.glucose >= rate && s.atp >= rate) {
            s.glucose -= rate;
            s.glycogene += rate;
            s.atp -= rate;
            //System.out.println("✅ Glucose → Glycogène : -" + rate + ", +" + rate + ", ATP -" + rate);
        }
    }

    private void glycolyse(MetabolicState s, double rate) {
        if (s.glucose >= rate) {
            s.glucose -= rate;
            s.pyruvate += 2 * rate;
            s.atp += 2 * rate;
            //System.out.println("⚡ Glycolyse : Glucose -" + rate + ", Pyruvate +" + (2 * rate) + ", ATP +" + (2 * rate));
        }
    }

    private void pyrToCoA(MetabolicState s, double rate) {
        if (s.pyruvate >= rate) {
            s.pyruvate -= rate;
            s.acetylCoA += rate;
            //System.out.println("➡️ Pyruvate → AcetylCoA : Pyruvate -" + rate + ", CoA +" + rate);
        }
    }

    private void krebs(MetabolicState s, double rate) {
        if (s.acetylCoA >= rate) {
            s.acetylCoA -= rate;
            s.atp += 10 * rate;
            //System.out.println("🔥 Krebs : AcetylCoA -" + rate + ", ATP +" + (10 * rate));
        }
    }

    private void lipogenese(MetabolicState s, double rate) {
        if (s.acetylCoA >= 8 * rate && s.atp >= 2 * rate) {
            s.acetylCoA -= 8 * rate;
            s.acidesGras += rate;
            s.atp -= 2 * rate;
            //System.out.println("💥 Lipogenèse : AcetylCoA -" + (8 * rate) + ", AG +" + rate + ", ATP -" + (2 * rate));
        }
    }
}


// classe de consommation
// Cette classe est activée par le glucagon et l'AMPK et permet de consommer les glucides, lipides et protéines dans le foie
class ConsommationMode implements MetabolismMode {

    @Override
    public void execute(MetabolicState s, int complexePerCry) {
        System.out.println("\uD83D\uDD04 Mode : Consommation activé (glucagon / AMPK)");

        glycogenolyse(s, 5);       // Glycogène → Glucose
        glycolyse(s, 5);           // Glucose → Pyruvate + ATP
        pyrToCoA(s, 10);            // Pyruvate → AcetylCoA
        betaOxydation(s, 1.3);       // AG → AcetylCoA
        //transamination(s, 2);    // AA → Pyruvate
        if (complexePerCry == 0) {
            krebs(s, 20.4);
        }else if (complexePerCry == 1 ){
            krebs(s, 40.4);            // valeur augmentée à étudier
        }                                     // AcetylCoA → ATP
        movement(s, 77);           // ATP → Mouvement
    }

    private void glycogenolyse(MetabolicState s, double rate) {
        if (s.glycogene >= rate) {
            s.glycogene -= rate;
            s.glucose += rate;
            //System.out.println("🏗️ Glycogénolyse : Glycogène -" + rate + ", Glucose +" + rate);
        }
    }

    private void glycolyse(MetabolicState s, double rate) {
        if (s.glucose >= rate) {
            s.glucose -= rate;
            s.pyruvate += 2 * rate;
            s.atp += 2 * rate;
            //System.out.println("⚡ Glycolyse : Glucose -" + rate + ", Pyruvate +" + (2 * rate) + ", ATP +" + (2 * rate));
        }
    }

    private void pyrToCoA(MetabolicState s, double rate) {
        if (s.pyruvate >= rate) {
            s.pyruvate -= rate;
            s.acetylCoA += rate;
            //System.out.println("➡️ Pyruvate → AcetylCoA : Pyruvate -" + rate + ", CoA +" + rate);
        }
    }

    private void betaOxydation(MetabolicState s, double rate) {
        if (s.acidesGras >= rate) {
            s.acidesGras -= rate;
            double acetylProduced = 3.5 * rate; // 1 AG → ~3.5 AcetylCoA
            s.acetylCoA += acetylProduced;
            //System.out.println("🔥 Bêta-oxydation : AG -" + rate + ", AcetylCoA +" + acetylProduced);
        }
    }

    /*private void transamination(MetabolicState s, double rate) {
        if (s.acidesAmines >= rate) {
            s.acidesAmines -= rate;
            s.pyruvate += rate;
            //System.out.println("🧬 Transamination : AA -" + rate + ", Pyruvate +" + rate);
        }
    }*/

    private void krebs(MetabolicState s, double rate) {
        if (s.acetylCoA >= rate) {
            s.acetylCoA -= rate;
            s.atp += 10 * rate;
            //System.out.println("🔥 Krebs : AcetylCoA -" + rate + ", ATP +" + (10 * rate));
        }
    }
    private void movement(MetabolicState s, double rate) {
        if (s.atp >= rate) {
            s.atp -= rate;
            //System.out.println("🏃‍♂️ Déplacement : ATP -" + rate)
            System.out.println("UN MOVEMENT A EU LIEU");
        }
    }
}


class MetabolicState {
    public double glucose = 10;
    public double glycogene = 0;
    public double acidesGras = 0;
    public double acidesAmines = 0;
    public double atp = 1000.0;

    public double pyruvate = 1;
    public double acetylCoA = 1;
}

