package com.monprojet;

// Cette interface execute les systémes stockage et consommation pour les différents métabolites du foie. 

public interface MetabolismMode {
    //void execute(MetabolicState state, int complexePerCry);
    void execute(EnvironmentModel env, int complexePerCry);
}
// classe de stockage
// Cette classe est activée par l'insuline et permet de stocker les glucides, lipides et protéines dans le foie
class StockageMode implements MetabolismMode {

    @Override
    //public void execute(MetabolicState s, int complexePerCry) {
    public void execute(EnvironmentModel env, int complexePerCry) {
        System.out.println("\uD83D\uDCC5 Mode : Stockage activé (insuline)");
        // réactions du stockage avec leurs vitesses obtenues par FBA
        glycogenese(env, 4.46);     // glucose → glycogène
        glycolyse(env, 5);          // glucose → pyruvate + ATP
        pyrToCoA(env, 10);          // pyruvate → AcetylCoA
        if (complexePerCry == 0) {     // influence de PER CRY
            krebs(env, 3.1);
        }else if (complexePerCry == 1 ){
            krebs(env, 6);         // valeur augmentée à étudier
        }                              // AcetylCoA → ATP
        lipogenese(env, 0.86);      // AcetylCoA → AG
        //nourish(env, 10);           // import de glucose dans le systeme
        movement(env, 77);          
        //aminoacids(s, 5);
    }
    /*private void aminoacids(MetabolicState s, double rate) {
        s.acidesAmines += rate;
    }*/

    // boite noire d'arrivée de glucose
    //private void nourish(MetabolicState s, double rate) {
    /*private void nourish(EnvironmentModel env, double rate) {
        env.addGlucose(rate);
        //glucose += rate;

    }*/

    // mouvement à chaque tick à partir de l'ATP
    //private void movement(MetabolicState s, double rate) {
    private void movement(EnvironmentModel env, double rate) {
        if (env.getAtp() >= rate) {
            //env.atp -= rate;
            env.removeAtp(rate);
            //System.out.println("🏃‍♂️ Déplacement : ATP -" + rate);
            System.out.println("UN MOVEMENT A EU LIEU");
        }
    }

    private void glycogenese(EnvironmentModel env, double rate) {
        if (env.getGlucose() >= rate && env.getAtp() >= rate) {
            env.removeGlucose(rate);
            env.addGlycogene(rate);
            env.removeAtp(rate);
            //System.out.println("✅ Glucose → Glycogène : -" + rate + ", +" + rate + ", ATP -" + rate);
        }
    }
    /*private void glycogenese(MetabolicState s, double rate) {
        if (s.glucose >= rate && s.atp >= rate) {
            s.glucose -= rate;
            s.glycogene += rate;
            s.atp -= rate;
            //System.out.println("✅ Glucose → Glycogène : -" + rate + ", +" + rate + ", ATP -" + rate);
        }
    }*/

    private void glycolyse(EnvironmentModel env, double rate) {
        if (env.getGlucose() >= rate) {
            env.removeGlucose(rate);
            env.addPyruvate(2 * rate);
            env.addAtp(2 * rate);
            //System.out.println("⚡ Glycolyse : Glucose -" + rate + ", Pyruvate +" + (2 * rate) + ", ATP +" + (2 * rate));
        }
    }
    /*private void glycolyse(MetabolicState s, double rate) {
        if (s.glucose >= rate) {
            s.glucose -= rate;
            s.pyruvate += 2 * rate;
            s.atp += 2 * rate;
            //System.out.println("⚡ Glycolyse : Glucose -" + rate + ", Pyruvate +" + (2 * rate) + ", ATP +" + (2 * rate));
        }
    }*/

    private void pyrToCoA(EnvironmentModel env, double rate) {
        if (env.getPyruvate() >= rate) {
            env.removePyruvate(rate);
            env.addAcetylCoA(rate);
            //System.out.println("➡️ Pyruvate → AcetylCoA : Pyruvate -" + rate + ", CoA +" + rate);
        }
    }
    /*private void pyrToCoA(MetabolicState s, double rate) {
        if (s.pyruvate >= rate) {
            s.pyruvate -= rate;
            s.acetylCoA += rate;
            //System.out.println("➡️ Pyruvate → AcetylCoA : Pyruvate -" + rate + ", CoA +" + rate);
        }
    }*/

    private void krebs(EnvironmentModel env, double rate) {
        if (env.getAcetylCoA() >= rate) {
            env.removeAcetylCoA(rate);
            env.addAtp(10 * rate);
            //System.out.println("🔥 Krebs : AcetylCoA -" + rate + ", ATP +" + (10 * rate));
        }
    }

    /*private void krebs(MetabolicState s, double rate) {
        if (s.acetylCoA >= rate) {
            s.acetylCoA -= rate;
            s.atp += 10 * rate;
            //System.out.println("🔥 Krebs : AcetylCoA -" + rate + ", ATP +" + (10 * rate));
        }
    }*/

    private void lipogenese(EnvironmentModel env, double rate) {
        if (env.getAcetylCoA() >= 8 * rate && env.getAtp() >= 2 * rate) {
            env.removeAcetylCoA(8 * rate);
            env.addAcidesGras(rate);
            env.removeAtp(2 * rate);
            //System.out.println("💥 Lipogenèse : AcetylCoA -" + (8 * rate) + ", AG +" + rate + ", ATP -" + (2 * rate));
        }
    }

    /*private void lipogenese(MetabolicState s, double rate) {
        if (s.acetylCoA >= 8 * rate && s.atp >= 2 * rate) {
            s.acetylCoA -= 8 * rate;
            s.acidesGras += rate;
            s.atp -= 2 * rate;
            //System.out.println("💥 Lipogenèse : AcetylCoA -" + (8 * rate) + ", AG +" + rate + ", ATP -" + (2 * rate));
        }
    }*/
}


// classe de consommation
// Cette classe est activée par le glucagon et l'AMPK et permet de consommer les glucides, lipides et protéines dans le foie
class ConsommationMode implements MetabolismMode {

    @Override
    //public void execute(MetabolicState s, int complexePerCry) {
    public void execute(EnvironmentModel env, int complexePerCry) {
        System.out.println("\uD83D\uDD04 Mode : Consommation activé (glucagon / AMPK)");

        glycogenolyse(env, 5);       // Glycogène → Glucose
        glycolyse(env, 5);           // Glucose → Pyruvate + ATP
        pyrToCoA(env, 10);            // Pyruvate → AcetylCoA
        betaOxydation(env, 1.3);       // AG → AcetylCoA
        //transamination(s, 2);    // AA → Pyruvate
        if (complexePerCry == 0) {
            krebs(env, 20.4);
        }else if (complexePerCry == 1 ){
            krebs(env, 30.4);            // valeur augmentée à étudier
        }                                     // AcetylCoA → ATP
        movement(env, 77);           // ATP → Mouvement
    }


    private void glycogenolyse(EnvironmentModel env, double rate) {
        if (env.getGlycogene() >= rate) {
            env.removeGlycogene(rate);
            env.addGlucose(rate);
            //System.out.println("🏗️ Glycogénolyse : Glycogène -" + rate + ", Glucose +" + rate);
        }
    }

    /*private void glycogenolyse(MetabolicState s, double rate) {
        if (s.glycogene >= rate) {
            s.glycogene -= rate;
            s.glucose += rate;
            //System.out.println("🏗️ Glycogénolyse : Glycogène -" + rate + ", Glucose +" + rate);
        }
    }*/

    private void glycolyse(EnvironmentModel env, double rate) {
        if (env.getGlucose() >= rate) {
            env.removeGlucose(rate);
            env.addPyruvate(2 * rate);
            env.addAtp(2 * rate);
            //System.out.println("⚡ Glycolyse : Glucose -" + rate + ", Pyruvate +" + (2 * rate) + ", ATP +" + (2 * rate));
        }
    }
    /*
    private void glycolyse(MetabolicState s, double rate) {
        if (s.glucose >= rate) {
            s.glucose -= rate;
            s.pyruvate += 2 * rate;
            s.atp += 2 * rate;
            //System.out.println("⚡ Glycolyse : Glucose -" + rate + ", Pyruvate +" + (2 * rate) + ", ATP +" + (2 * rate));
        }
    }*/

    private void pyrToCoA(EnvironmentModel env, double rate) {
        if (env.getPyruvate() >= rate) {
            env.removePyruvate(rate);
            env.addAcetylCoA(rate);
            //System.out.println("➡️ Pyruvate → AcetylCoA : Pyruvate -" + rate + ", CoA +" + rate);
        }
    }
    /*
    private void pyrToCoA(MetabolicState s, double rate) {
        if (s.pyruvate >= rate) {
            s.pyruvate -= rate;
            s.acetylCoA += rate;
            //System.out.println("➡️ Pyruvate → AcetylCoA : Pyruvate -" + rate + ", CoA +" + rate);
        }
    } */

    private void betaOxydation(EnvironmentModel env, double rate) {
        if (env.getAcidesGras() >= rate) {
            env.removeAcidesGras(rate);
            double acetylProduced = 3.5 * rate; // 1 AG → ~3.5 AcetylCoA
            env.addAcetylCoA(acetylProduced);
            //System.out.println("🔥 Bêta-oxydation : AG -" + rate + ", AcetylCoA +" + acetylProduced);
        }
    }

    /* 

    private void betaOxydation(MetabolicState s, double rate) {
        if (s.acidesGras >= rate) {
            s.acidesGras -= rate;
            double acetylProduced = 3.5 * rate; // 1 AG → ~3.5 AcetylCoA
            s.acetylCoA += acetylProduced;
            //System.out.println("🔥 Bêta-oxydation : AG -" + rate + ", AcetylCoA +" + acetylProduced);
        }
    } */

    /*private void transamination(MetabolicState s, double rate) {
        if (s.acidesAmines >= rate) {
            s.acidesAmines -= rate;
            s.pyruvate += rate;
            //System.out.println("🧬 Transamination : AA -" + rate + ", Pyruvate +" + rate);
        }
    }*/

    private void krebs(EnvironmentModel env, double rate) {
        if (env.getAcetylCoA() >= rate) {
            env.removeAcetylCoA(rate);
            env.addAtp(10 * rate);
            //System.out.println("🔥 Krebs : AcetylCoA -" + rate + ", ATP +" + (10 * rate));
        }
    }
    /*
    private void krebs(MetabolicState s, double rate) {
        if (s.acetylCoA >= rate) {
            s.acetylCoA -= rate;
            s.atp += 10 * rate;
            //System.out.println("🔥 Krebs : AcetylCoA -" + rate + ", ATP +" + (10 * rate));
        }
    } */

    private void movement(EnvironmentModel env, double rate) {
        if (env.getAtp() >= rate) {
            env.removeAtp(rate);
            //System.out.println("🏃‍♂️ Déplacement : ATP -" + rate);
            System.out.println("UN MOVEMENT A EU LIEU");
        }
    }
    /*
    private void movement(MetabolicState s, double rate) {
        if (s.atp >= rate) {
            s.atp -= rate;
            //System.out.println("🏃‍♂️ Déplacement : ATP -" + rate)
            System.out.println("UN MOVEMENT A EU LIEU");
        }
    }*/
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

