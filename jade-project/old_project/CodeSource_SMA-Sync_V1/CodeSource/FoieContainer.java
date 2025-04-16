package sma;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FoieContainer {
	public static int lineNumber = 1;
	
    public static void main(String[] args) {
        String parametreFilePath = "C:\\Users\\PC\\Documents\\Stage\\Code\\Code foie\\parametregridsearch.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(parametreFilePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
            	
                String[] params = line.replace("(", "").replace(")", "").split(",");

                
                int iterationNombre = Integer.parseInt(params[0].trim());
                double coeffInhibition = Double.parseDouble(params[1].trim());                
                int timeProcessus = Integer.parseInt(params[2].trim());

                double seuilGlycogen = Double.parseDouble(params[3].trim());
                double quantiteGlucoseTransfert = Double.parseDouble(params[4].trim());
                double quantiteGlucoseGlycolyse = Double.parseDouble(params[5].trim());
                double quantiteGlucoseGlycogenolyse = Double.parseDouble(params[6].trim());
                double quantiteGlucoseGlycogenogenese = Double.parseDouble(params[7].trim());
                double quantiteGlucoseGluconeogenese = Double.parseDouble(params[8].trim());
                double seuilGlycolyse = Double.parseDouble(params[9].trim());
                double quantiteAcetylcoaKrebs = Double.parseDouble(params[10].trim());
                double quantitePyruvateAACatabolism = Double.parseDouble(params[11].trim());
                double quantiteAcetylcoaBetaoxydation = Double.parseDouble(params[12].trim());
                double quantitePyruvatePyrHSAcetyl = Double.parseDouble(params[13].trim());
                double quantiteAcetylcoaLipogenese = Double.parseDouble(params[14].trim());
                double seuilTrad = Double.parseDouble(params[15].trim());
                double seuilTrans = Double.parseDouble(params[16].trim());
                double seuilKrebs = Double.parseDouble(params[17].trim());
                double seuilDeg = Double.parseDouble(params[18].trim());
                int timeDeg = Integer.parseInt(params[19].trim());
                int timeTrad = Integer.parseInt(params[20].trim());
                int timeTrans = Integer.parseInt(params[21].trim());
                double quantiteArncryTrans = Double.parseDouble(params[22].trim());
                double quantiteProtcryDegradation = Double.parseDouble(params[23].trim());
                double quantiteProtcryTraduction = Double.parseDouble(params[24].trim());

                try {
                    // Initialisation de JADE
                    Runtime runtime = Runtime.instance();
                    Properties properties = new ExtendedProperties();
                    properties.setProperty(Profile.GUI, "true");
                    Profile profile = new ProfileImpl(properties);
                    AgentContainer mainContainer = runtime.createMainContainer(profile);
                    mainContainer.start();

                    Runtime runtime1 = Runtime.instance();
                    Profile profile1 = new ProfileImpl(false);
                    profile1.setParameter(Profile.MAIN_HOST, "localhost");
                    AgentContainer agentContainer = runtime1.createAgentContainer(profile1);

                    // Création des agents JADE avec les paramètres spécifiques
                    AgentController snifferagent = agentContainer.createNewAgent("sniffer", "jade.tools.sniffer.Sniffer",
                            new Object[]{"Clock;Mitochondrie;AMPK;Plasma"});
                    snifferagent.start();

                    AgentController agentControllerClock = agentContainer.createNewAgent("Clock", "sma.agents.ClockAgent",
                            new Object[]{iterationNombre, seuilTrad, seuilTrans, seuilKrebs, seuilDeg,  timeDeg, timeTrad, timeTrans, quantiteArncryTrans, quantiteProtcryDegradation, quantiteProtcryTraduction, coeffInhibition});
                    agentControllerClock.start();

                    AgentController agentControllerMitochondrie = agentContainer.createNewAgent("Mitochondrie", "sma.agents.MitochondrieAgent",
                            new Object[]{iterationNombre, timeProcessus, seuilGlycolyse, quantiteAcetylcoaKrebs, quantiteGlucoseGluconeogenese, quantiteGlucoseGlycolyse, quantitePyruvateAACatabolism, quantiteAcetylcoaBetaoxydation, quantitePyruvatePyrHSAcetyl, quantiteAcetylcoaLipogenese, coeffInhibition});
                    agentControllerMitochondrie.start();

                    AgentController agentControllerAMPK = agentContainer.createNewAgent("AMPK", "sma.agents.AMPKAgent",
                            new Object[]{iterationNombre, timeProcessus, seuilGlycogen, quantiteGlucoseTransfert, quantiteGlucoseGlycolyse, quantiteGlucoseGlycogenolyse, quantiteGlucoseGlycogenogenese, quantiteGlucoseGluconeogenese, coeffInhibition});
                    agentControllerAMPK.start();
                    
                    while (sma.env.Environnement.RUNClockAgent == true || sma.env.Environnement.RUNAMPKAgent == true || sma.env.Environnement.RUNMitochondrieAgent == true) {
                    	/*
                    	System.out.println("Clock : " + sma.env.Environnement.RUNClockAgent);
                    	System.out.println("AMPK : " + sma.env.Environnement.RUNAMPKAgent);
                    	System.out.println("Mitochondrie : " + sma.env.Environnement.RUNMitochondrieAgent);
                    	*/
                    }
                    System.out.println("Fin de la simulation " + lineNumber);
                    // Faire méthode Done
                    
                    lineNumber++;
                    
                } 
                catch (ControllerException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
