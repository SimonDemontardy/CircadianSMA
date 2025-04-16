package sma.agents;

import sma.env.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import sma.env.Environnement;

import java.io.FileWriter;
import java.io.IOException;

public class MitochondrieAgent extends Agent {
    private double pyruvateLevel = 10;
    private double acetylcoaLevel = 5;
    private double hscoaLevel = 10;
    
    double seuilGlycolyse;		// Seuil pour inhiber la Glycolyse
    int timeProcessus;
	double quantiteAcetylcoaKrebs;
	double quantiteHscoaKrebs;
	double quantitePyruvateGluconeogenese;
	double quantitePyruvateGlycolyse;
	double quantitePyruvateAACatabolism;
	double quantiteAminoacidsAACatabolism;
	double quantiteAcetylcoaBetaoxydation;
	double quantiteHscoaBetaoxydation;
	double quantiteFattyacidsBetaoxydation;
	double quantitePyruvatePyrHSAcetyl;
	double quantiteHscoaPyrHSAcetyl;
	double quantiteAcetylcoaPyrHSAcetyl;
	double quantiteAcetylcoaLipogenese;
	double quantiteFattyacidsLipogenese;
	double coeffInhibition;
	private boolean actKrebs = false; // Variable qui gère l'activité du Cycle de Krebs (ne pas oublier de faire correspondre cette variable avec le niveau inital de protéine CRY)

    private double iterationCompteur3 = 0;
    private double iterationNombre;		// Nombre d'itération
    
    private int actGlycolyse = 1; // Variable qui gère l'activité de la Glycolyse (ne pas oublier de faire correspondre cette variable avec le niveau inital d'Acetyl-CoA)

    protected void setup() {
    	
		sma.env.Environnement.RUNMitochondrieAgent = true;
    	Object[] args = getArguments();
        if (args != null) {
        	iterationNombre = (int) args[0];
        	timeProcessus = (int) args[1];
        	seuilGlycolyse = (double) args[2];
        	quantiteAcetylcoaKrebs = (double) args[3];
        	quantiteHscoaKrebs = quantiteAcetylcoaKrebs;
        	quantitePyruvateGluconeogenese = 2 * (double) args[4];
        	quantitePyruvateGlycolyse = 2 * (double) args[5];
        	quantitePyruvateAACatabolism = (double) args[6];
        	quantiteAminoacidsAACatabolism = quantitePyruvateAACatabolism;
        	quantiteAcetylcoaBetaoxydation = (double) args[7];
        	quantiteHscoaBetaoxydation = quantiteAcetylcoaBetaoxydation;
        	quantiteFattyacidsBetaoxydation = 1/8 * quantiteAcetylcoaBetaoxydation;
        	quantitePyruvatePyrHSAcetyl = (double) args[8];
        	quantiteHscoaPyrHSAcetyl = quantitePyruvatePyrHSAcetyl;
        	quantiteAcetylcoaPyrHSAcetyl = quantitePyruvatePyrHSAcetyl;
        	quantiteAcetylcoaLipogenese = (double) args[9];
        	quantiteFattyacidsLipogenese = 1/8 * quantiteAcetylcoaLipogenese;
        	coeffInhibition = (double) args[10];
        }

    	ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
    	
    	parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    AID sender = msg.getSender();
                    String content = msg.getContent();
                    
                    if (sender.getLocalName().equals("Clock")) {
                    	// Cycle de Krebs
                        if (content.startsWith("Activation du Cycle de Krebs")) {
                        	actKrebs = true;
                        }
                        if (content.startsWith("Désactivation du Cycle de Krebs")) {
                        	actKrebs = false;
                        }
                    }
                    
                    if (sender.getLocalName().equals("AMPK")) {
                        // Gluconeogenese : Changement du pyruvate en glucose
                        if (content.startsWith("Gluconeogenese inhibée")) {
            				pyruvateLevel -= quantitePyruvateGluconeogenese * coeffInhibition;
                        }		
    					if (content.startsWith("Gluconeogenese non-inhibée")) {
        					pyruvateLevel -= quantitePyruvateGluconeogenese;
        		    	}
                    }
                }
            }
        });
    	
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
            	if (actKrebs == true) {
            		acetylcoaLevel -= quantiteAcetylcoaKrebs;
            		hscoaLevel += quantiteHscoaKrebs;	// Acetyl-CoA devient HS-CoA

            		sma.env.Environnement.INCR_ADP(-1);
            		sma.env.Environnement.INCR_ATP(1);	// ADP devient ATP
            	}
            }
    	});
    	
		parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
                pyruvateLevel += quantitePyruvateAACatabolism;	// AA Catabolism : Changement des aminoacids en pyruvate
                sma.env.Environnement.aminoacidsLevel -= quantiteAminoacidsAACatabolism;
            }
    	});
		
		parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
                acetylcoaLevel += quantiteAcetylcoaBetaoxydation;	// Beta-Oxydation : Changement des fattyacids en Acetyl-CoA
                hscoaLevel -= quantiteHscoaBetaoxydation;
                sma.env.Environnement.fattyacidsLevel -= quantiteFattyacidsBetaoxydation;
                
             // Mise à jour condition Glycolyse
	            if (actGlycolyse != 2) {
            		if (sma.env.Environnement.PHOS_AMPK() == 1) {
            			if (acetylcoaLevel >= seuilGlycolyse) {
	            			ACLMessage glycolysemsg = new ACLMessage(ACLMessage.INFORM);
			            	glycolysemsg.setContent("Glycolyse inhibée");
			            	glycolysemsg.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
			                send(glycolysemsg);
		            			            		
		            		actGlycolyse = 2;
            			}
            		}
            	}
            	if (actGlycolyse != 1) {
            		if (sma.env.Environnement.PHOS_AMPK() == 1) {
            			if (acetylcoaLevel < seuilGlycolyse) {
	            			ACLMessage glycolysemsg = new ACLMessage(ACLMessage.INFORM);
			            	glycolysemsg.setContent("Glycolyse inhibée");
			            	glycolysemsg.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
			                send(glycolysemsg);
		            			            		
		            		actGlycolyse = 2;
            			}
            		}
            	}
            	if (actGlycolyse != 0) {
            		if (sma.env.Environnement.PHOS_AMPK() == 0) {
            			ACLMessage glycolysemsg = new ACLMessage(ACLMessage.INFORM);
		            	glycolysemsg.setContent("Desactivation Glycolyse");
		            	glycolysemsg.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
		                send(glycolysemsg);
		                
            			actGlycolyse = 0;
            		}
            	}
            }
    	});
		
		// Lipogenese : changement de l'Acetyl-CoA en Fatty acids
		parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
            	if (sma.env.Environnement.FASTING == false) {
	                sma.env.Environnement.fattyacidsLevel += quantiteFattyacidsLipogenese;
	                acetylcoaLevel -= quantiteAcetylcoaLipogenese;

            	}
            	else {
            		sma.env.Environnement.fattyacidsLevel += quantiteFattyacidsLipogenese * coeffInhibition;
	                acetylcoaLevel -= quantiteAcetylcoaLipogenese * coeffInhibition;
            	}
            	// Mise à jour condition Glycolyse
	            if (actGlycolyse != 2) {
            		if (sma.env.Environnement.PHOS_AMPK() == 1) {
            			if (acetylcoaLevel >= seuilGlycolyse) {
	            			ACLMessage glycolysemsg = new ACLMessage(ACLMessage.INFORM);
			            	glycolysemsg.setContent("Glycolyse inhibée");
			            	glycolysemsg.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
			                send(glycolysemsg);
		            			            		
		            		actGlycolyse = 2;
            			}
            		}
            	}
            	if (actGlycolyse != 1) {
            		if (sma.env.Environnement.PHOS_AMPK() == 1) {
            			if (acetylcoaLevel < seuilGlycolyse) {
	            			ACLMessage glycolysemsg = new ACLMessage(ACLMessage.INFORM);
			            	glycolysemsg.setContent("Glycolyse inhibée");
			            	glycolysemsg.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
			                send(glycolysemsg);
		            			            		
		            		actGlycolyse = 2;
            			}
            		}
            	}
            	if (actGlycolyse != 0) {
            		if (sma.env.Environnement.PHOS_AMPK() == 0) {
            			ACLMessage glycolysemsg = new ACLMessage(ACLMessage.INFORM);
		            	glycolysemsg.setContent("Desactivation Glycolyse");
		            	glycolysemsg.addReceiver(new AID("AMPK", AID.ISLOCALNAME));
		                send(glycolysemsg);
		                
            			actGlycolyse = 0;
            		}
            	}
            }
    	});
    			
		parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
    			// Pyruvate + HS-CoA -> Acetyl-CoA
	            pyruvateLevel -= quantitePyruvatePyrHSAcetyl;
	            hscoaLevel -= quantiteHscoaPyrHSAcetyl;
	            acetylcoaLevel += quantiteAcetylcoaPyrHSAcetyl;
            }
    	});
    	
    	// Glycolyse : Changement du glucose en pyruvate
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
            	if (actGlycolyse == 2) {	// Inhibée
            		
            		sma.env.Environnement.INCR_ADP(-5 * coeffInhibition);
            		sma.env.Environnement.INCR_ATP(5 * coeffInhibition);	// ADP devient ATP
            		
            		pyruvateLevel += quantitePyruvateGlycolyse * coeffInhibition;
            		
	            }
				if (actGlycolyse == 1) {	// Non-inhibée
				            		
            		sma.env.Environnement.INCR_ADP(-5);
            		sma.env.Environnement.INCR_ATP(5);	// ADP devient ATP
            		
            		pyruvateLevel += quantitePyruvateGlycolyse;
            		
	            }
            }
    	});
    	
    	// On sauvegarde les données de façon périodique
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, 100) {
	    	public void onTick() {
	            if (iterationCompteur3 <= iterationNombre) {
	            	saveDataMitochondrie();
	            	iterationCompteur3++;
	            } else {
	        		sma.env.Environnement.RUNMitochondrieAgent = false;
	            }
	            if (sma.env.Environnement.RUNClockAgent == false && sma.env.Environnement.RUNAMPKAgent == false && sma.env.Environnement.RUNMitochondrieAgent == false) {
                	doDelete();
                }
	        }
	    });

    	addBehaviour(parallelBehaviour);
    }
    
    private void saveDataMitochondrie() {
        try (
        		FileWriter pyruvateWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\pyruvateLevel.csv", true);
        		FileWriter acetylcoaWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\acetylcoaLevel.csv", true);
        		FileWriter hscoaWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\hscoaLevel.csv", true);
        	) {
        	pyruvateWriter.append(String.valueOf(pyruvateLevel)).append("\n");
        	acetylcoaWriter.append(String.valueOf(acetylcoaLevel)).append("\n");
        	hscoaWriter.append(String.valueOf(hscoaLevel)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}