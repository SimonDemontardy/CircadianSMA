package sma.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.FileWriter;
import java.io.IOException;

public class ClockAgent extends Agent {
    private double arncryLevel = 3;
    private double protcryLevel = 0;

    double seuilTrad;	// Seuil traduction de la protéine CRY
	double seuilTrans;	// Seuil transcription de l'ARN Cry
	double seuilKrebs;	// Seuil pour activation du cycle de Krebs
	double seuilDeg;	// Seuil activation de la dégradation de la protéine CRY
	int timeDeg;		// Temps pour la dégradation de la protéine CRY
	int timeTrad;		// Temps pour la traduction de la protéine CRY
	int timeTrans;		// Temps pour la transcription de l'ARNCry
	int timeKrebs;		// Temps pour le cycle de Krebs
	double quantiteArncryTrans;
	double quantiteProtcryDegradation;
	double quantiteProtcryTraduction;
	double coeffInhibition;
    private boolean krebsAct = false; // Variable qui gère l'activité du Cycle de Krebs (ne pas oublier de faire correspondre cette variable avec le niveau inital de protéine CRY)
    private boolean degCRY = false;
    private boolean glycogenolyseAct = false;
    private boolean gluconeogeneseAct = false;
    
    
    private double iterationCompteur1 = 0;
    private int iterationNombre;		// Nombre d'itération
    
	protected void setup() {
		
		sma.env.Environnement.RUNClockAgent = true;
		Object[] args = getArguments();
	    if (args != null) {
	    	iterationNombre = (int) args[0];
	    	seuilTrad = (double) args[1];
	    	seuilTrans = (double) args[2];
	    	seuilKrebs = (double) args[3];
	    	seuilDeg = (double) args[4];
	    	timeDeg = (int) args[5];
	    	timeTrad = (int) args[6];
	    	timeTrans = (int) args[7];
	    	quantiteArncryTrans = (double) args[8];
	    	quantiteProtcryDegradation = (double) args[9];
	    	quantiteProtcryTraduction = (double) args[10];
	    	
	    }
	    
		ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
		
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeTrans) {
    		
    		// GeneCry qui transcrit ARNCry en prenant compte de l'inhibition de la protéine CRY
            public void onTick() {
            	if (degCRY == false) {
	            	arncryLevel += quantiteArncryTrans;
            	}
            }
        });
    	
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeDeg) {
    		
    		// Dégradation de la protéine CRY
            public void onTick() {
            	if (degCRY == false && protcryLevel >= seuilDeg) {
	            	degCRY = true;
	            	System.out.println(iterationCompteur1 + " : Dégradation protéine CRY");
	            	sma.env.Environnement.FASTING = true;
            	}
            	if (degCRY == true && protcryLevel == 0) {
            		degCRY = false;
	            	System.out.println(iterationCompteur1 + " : Arrêt dégradation protéine CRY");
            		sma.env.Environnement.FASTING = false;
            	}
            	
            	if (degCRY == true) {	// modifier en faisait protcryLevel = protcryLevel - (protcryLevel * quantiteProtcryDegradation)
	        		if (protcryLevel - quantiteProtcryDegradation >= 0) {
	        			protcryLevel -= quantiteProtcryDegradation;
	        		}
	        		else {
	        			protcryLevel = 0;
	        		}
            	}
            	if (krebsAct == false) {
            		if (protcryLevel >= seuilKrebs) {
	            		// Envoi de l'information de l'Activation du Cycle de Krebs
	                    ACLMessage activationkrebs = new ACLMessage(ACLMessage.INFORM);
	                    activationkrebs.setContent("Activation du Cycle de Krebs");
	                    activationkrebs.addReceiver(new AID("Mitochondrie", AID.ISLOCALNAME));
	                    send(activationkrebs);
	                    krebsAct = true;
	                    System.out.println(iterationCompteur1 + ": Activation du Cycle de Krebs");
    				}
            	}
            	if (krebsAct == true) {
            		if (protcryLevel < seuilKrebs) {
	            		// Envoi de l'information de la Désactivation du Cycle de Krebs
	                    ACLMessage desactivationkrebs = new ACLMessage(ACLMessage.INFORM);
	                    desactivationkrebs.setContent("Désactivation du Cycle de Krebs");
	                    desactivationkrebs.addReceiver(new AID("Mitochondrie", AID.ISLOCALNAME));
	                    send(desactivationkrebs);
	                    krebsAct = false;
	                    System.out.println(iterationCompteur1 + ": Désactivation du Cycle de Krebs");
            		}
	        	}
            	
            	if (glycogenolyseAct == false) {
	            	if (sma.env.Environnement.FASTING == true && protcryLevel >= seuilDeg * 0.5) {
	            		ACLMessage activationGlycogenolyse = new ACLMessage(ACLMessage.INFORM);
	            		activationGlycogenolyse.setContent("Activation de la Glycogenolyse");
	            		activationGlycogenolyse.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
	                    send(activationGlycogenolyse);
	                    glycogenolyseAct = true;
	                    System.out.println(iterationCompteur1 + ": Activation de la Glycogenolyse");
	            	}
            	}
            	if (glycogenolyseAct == true) {
	            	if (!(sma.env.Environnement.FASTING == true && protcryLevel >= seuilDeg * 0.5)) {
	            		ACLMessage desactivationGlycogenolyse = new ACLMessage(ACLMessage.INFORM);
	            		desactivationGlycogenolyse.setContent("Desactivation de la Glycogenolyse");
	            		desactivationGlycogenolyse.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
	                    send(desactivationGlycogenolyse);
	                    glycogenolyseAct = false;
	                    System.out.println(iterationCompteur1 + ": Desactivation de la Glycogenolyse");
	            	}
            	}
            	
            	if (gluconeogeneseAct == false) {
	            	if (sma.env.Environnement.FASTING == true && protcryLevel < seuilDeg * 0.5) {
	            		ACLMessage activationGluconeogenese = new ACLMessage(ACLMessage.INFORM);
	            		activationGluconeogenese.setContent("Activation de la Gluconeogenese");
	            		activationGluconeogenese.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
	                    send(activationGluconeogenese);
	                    gluconeogeneseAct = true;
	                    System.out.println(iterationCompteur1 + ": Activation de la Gluconeogenese");
	            	}
            	}
            	if (gluconeogeneseAct == true) {
	            	if (!(sma.env.Environnement.FASTING == true && protcryLevel < seuilDeg * 0.5)) {
	            		ACLMessage desactivationGluconeogenese = new ACLMessage(ACLMessage.INFORM);
	            		desactivationGluconeogenese.setContent("Desactivation de la Gluconeogenese");
	            		desactivationGluconeogenese.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
	                    send(desactivationGluconeogenese);
	                    gluconeogeneseAct = false;
	                    System.out.println(iterationCompteur1 + ": Desactivation de la Gluconeogenese");
	            	}
            	}
            }
        });
    	
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeTrad) {
    		
    		// Traduction de la protéine CRY
            public void onTick() {
				// en gros si CRY ne se dégrade pas + si le niveau de protéine CRY est supérieur au seuil de traduction
            	if (degCRY == false && arncryLevel >= seuilTrad) {
            		arncryLevel -= seuilTrad; // on diminu le niveau d'ARNCry de la quantité nécessaire pour la traduction
            		protcryLevel += quantiteProtcryTraduction; // on augmente le niveau de protéine CRY de la quantité traduite
            	}
            }
        });
	    
    	// On sauvegarde les données de façon périodique
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, 100) {
	    	public void onTick() {
	            if (iterationCompteur1 <= iterationNombre) {
	            	saveDataClock();
	            	iterationCompteur1++;
	            }
	            else {
	            	sma.env.Environnement.RUNClockAgent = false;
	            }
	            if (sma.env.Environnement.RUNClockAgent == false && sma.env.Environnement.RUNAMPKAgent == false && sma.env.Environnement.RUNMitochondrieAgent == false) {
                	doDelete();
                }
	        }
	    });

    	addBehaviour(parallelBehaviour);
    	
	}
	
	private void saveDataClock() {
        try (FileWriter arnCryWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\arncryLevel.csv", true);
             FileWriter protCryWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\protcryLevel.csv", true)) {
            arnCryWriter.append(String.valueOf(arncryLevel)).append("\n");
            protCryWriter.append(String.valueOf(protcryLevel)).append("\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}