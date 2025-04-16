package sma.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.FileWriter;
import java.io.IOException;

public class AMPKAgent extends Agent {
    private double glucosefoieLevel = 150;
    private double glycogenLevel = 40;
    
    double seuilGlycogen;					// Seuil Glycogène (Active Glycogenolyse et Gluconeogenese si glycogenLevel est en dessous)
    int timeProcessus;
	double quantiteGlucoseTransfert;
	double quantiteGlucoseGlycolyse;
	double quantiteGlucoseGlycogenolyse;
	double quantiteGlucoseGlycogenogenese;
	double quantiteGlycogeneGlycogenogenese;
	double quantiteGlucoseGluconeogenese;
	double quantiteGlycogeneGlycogenolyse;
	double coeffInhibition;
    
    private double iterationCompteur2 = 0;
    private double iterationNombre;		// Nombre d'itération
    
    private int glycolyseAct;
    private boolean actGlycogenolyse;
    private boolean actGluconeogenese;


    protected void setup() {
    	
		sma.env.Environnement.RUNAMPKAgent = true;
    	Object[] args = getArguments();
        if (args != null) {
        	iterationNombre = (int) args[0];
        	timeProcessus = (int) args[1];
        	seuilGlycogen = (double) args[2];
        	quantiteGlucoseTransfert = (double) args[3];
        	quantiteGlucoseGlycolyse = (double) args[4];
        	quantiteGlucoseGlycogenolyse  = (double) args[5];
        	quantiteGlucoseGlycogenogenese = (double) args[6];
        	quantiteGlycogeneGlycogenogenese  = quantiteGlucoseGlycogenogenese;
        	quantiteGlucoseGluconeogenese  = (double) args[7];
        	quantiteGlycogeneGlycogenolyse = quantiteGlucoseGlycogenolyse;
        	coeffInhibition = (double) args[8];
        }
        
    	ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
    	
    	parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    AID sender = msg.getSender();
                    String content = msg.getContent();
                    
                    // Glycolyse : Changement du glucose en pyruvate
                    if (sender.getLocalName().equals("Mitochondrie")) {
                    	if (content.startsWith("Glycolyse inhibée")) {
                    		glycolyseAct = 2;
                    	}
                    	if (content.startsWith("Glycolyse non-inhibée")) {
                    		glycolyseAct = 1;
                    	}
                    	if (content.startsWith("Desactivation Glycolyse")) {
                    		glycolyseAct = 0;
                    	}
                	}
                    if (sender.getLocalName().equals("Clock")) {
                    	if (content.startsWith("Activation de la Glycogenolyse")) {
                    		actGlycogenolyse = true;
                    	}
                    	if (content.startsWith("Desactivation de la Glycogenolyse")) {
                    		actGlycogenolyse = false;
                    	}
                    	if (content.startsWith("Activation de la Gluconeogenese")) {
                    		actGluconeogenese = true;
                    	}
                    	if (content.startsWith("Desactivation de la Gluconeogenese")) {
                    		actGluconeogenese = false;                    	
                    	}
                    }
                }
            }
    	});
    	
    	// Glycolyse
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
        		if (glycolyseAct == 2) {
        			glucosefoieLevel -= quantiteGlucoseGlycolyse * coeffInhibition; 
        		}
        		if (glycolyseAct == 1) {
        			glucosefoieLevel -= quantiteGlucoseGlycolyse;
        		}
            }
    	});
    	
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
        		glucosefoieLevel += quantiteGlucoseTransfert;
        		sma.env.Environnement.glucoseplasmaLevel -= quantiteGlucoseTransfert;	// Transfert du glucose du plasma vers le foie
        		
        		sma.env.Environnement.INCR_ATP(-1);
        		sma.env.Environnement.INCR_ADP(1); // Convertion d'ATP en ADP
            }
    	});
    	
    	// Glycogenolyse : Changement du glycogene en glucose (Active uniquement en période de jeûne)
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
            	if (actGlycogenolyse == true) {		// Période de jeûne ET s'il reste une certaine quantité de glycogene
            		glycogenLevel -= quantiteGlycogeneGlycogenolyse;
            		glucosefoieLevel += quantiteGlucoseGlycogenolyse;
            	}
            }
    	});
    	
    	// Glycogenogenese : Changement du glucose en glycogene
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
            	if (sma.env.Environnement.FASTING == false && sma.env.Environnement.PHOS_AMPK() == 1) {		// Inhibée
            		glucosefoieLevel -= quantiteGlucoseGlycogenogenese * coeffInhibition;
            		glycogenLevel += quantiteGlycogeneGlycogenogenese * coeffInhibition;
            		
            		sma.env.Environnement.INCR_ATP(-1 * coeffInhibition);
            		sma.env.Environnement.INCR_ADP(1 * coeffInhibition); // Convertion d'ATP en ADP
            	}
            	if (sma.env.Environnement.FASTING == false && sma.env.Environnement.PHOS_AMPK() == 0) {		// Non-inhibée
            		glucosefoieLevel -= quantiteGlucoseGlycogenogenese;
            		glycogenLevel += quantiteGlycogeneGlycogenogenese;
            		
            		sma.env.Environnement.INCR_ATP(-1);
            		sma.env.Environnement.INCR_ADP(1); // Convertion d'ATP en ADP
            	}
            }
    	});
    	
    	// Gluconeogenese : Changement du pyruvate en glucose
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
            	if (actGluconeogenese == true) {
	                if (sma.env.Environnement.PHOS_AMPK() == 1) {

	                	ACLMessage gluconeogenesemsg = new ACLMessage(ACLMessage.INFORM);
	                	gluconeogenesemsg.setContent("Gluconeogenese inhibée");
	                	gluconeogenesemsg.addReceiver(new AID("Mitochondrie", AID.ISLOCALNAME));
		                send(gluconeogenesemsg);
		                	                
	                	glucosefoieLevel += quantiteGlucoseGluconeogenese * coeffInhibition;	// Inhibée
	                	
	        			sma.env.Environnement.INCR_ATP(-2 * coeffInhibition);
	            		sma.env.Environnement.INCR_ADP(2 * coeffInhibition);	// ATP devient ADP
	                }
	                else {
	                	// On envoie l'information que le glycogène est en dessous du seuil à l'agent Mitochondrie
		            	ACLMessage gluconeogenesemsg = new ACLMessage(ACLMessage.INFORM);
		            	gluconeogenesemsg.setContent("Gluconeogenese non-inhibée");
		            	gluconeogenesemsg.addReceiver(new AID("Mitochondrie", AID.ISLOCALNAME));
		                send(gluconeogenesemsg);
	                	
	                	glucosefoieLevel += quantiteGlucoseGluconeogenese;	// Non-inhibée

	        			sma.env.Environnement.INCR_ATP(-2);
	            		sma.env.Environnement.INCR_ADP(2);	// ATP devient ADP
	                }
            	}
            }
    	});
    	
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, timeProcessus) {
            public void onTick() {
                glucosefoieLevel -= quantiteGlucoseTransfert;
                sma.env.Environnement.glucoseplasmaLevel += quantiteGlucoseTransfert;
            }
    	});
    	
    	// On sauvegarde les données de façon périodique
    	parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, 100) {
	    	public void onTick() {
	            if (iterationCompteur2 <= iterationNombre) {
	            	saveDataAMPK();
	            	iterationCompteur2++;
	            } else {
	        		sma.env.Environnement.RUNAMPKAgent = false;
	            }
	            
	            if (sma.env.Environnement.RUNClockAgent == false && sma.env.Environnement.RUNAMPKAgent == false && sma.env.Environnement.RUNMitochondrieAgent == false) {
                	doDelete();
                }
	        }
	    });

    	addBehaviour(parallelBehaviour);
    }

	private void saveDataAMPK() {
        try (
        		FileWriter glucosefoieWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\glucosefoieLevel.csv", true);
        		FileWriter glycogenWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\glycogenLevel.csv", true);
        		FileWriter totalATPWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\totalATPLevel.csv", true);
        		FileWriter totalADPWriter = new FileWriter("C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\GridSearch\\Data" + sma.FoieContainer.lineNumber + "\\totalADPLevel.csv", true);
        	) {
        	glucosefoieWriter.append(String.valueOf(glucosefoieLevel)).append("\n");
        	glycogenWriter.append(String.valueOf(glycogenLevel)).append("\n");
        	totalATPWriter.append(String.valueOf(sma.env.Environnement.getATP())).append("\n");
        	totalADPWriter.append(String.valueOf(sma.env.Environnement.getADP())).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}