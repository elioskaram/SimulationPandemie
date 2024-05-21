package firstSimulation;

import fr.emse.fayol.maqit.simulator.SimFactory;
import fr.emse.fayol.maqit.simulator.components.Robot;
import fr.emse.fayol.maqit.simulator.configuration.SimProperties;
import fr.emse.fayol.maqit.simulator.configuration.IniFile;
import fr.emse.fayol.maqit.simulator.environment.ColorGridEnvironment;


import java.util.ArrayList;
import java.util.*;
import java.util.List;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

public class EpidemicSimulation extends SimFactory {

    private static final double proba_infection = 0.15;
    private static final double CONTACT_INFECTION_MULTIPLIER = 1.5;
    private static final int numElements = 15;

    private static final double facteur_reduc_mask = 0.8;
    private static final double facteur_reduc_confinement = 0.1;
    private static final double proba_recovery = 0.7;
    private static final double proba_mort = 0.05;
    private static final double proba_sortir_exterieur = 0.01;
    //1% de la population sort de la ville
    private static final double proba_infection_exterieur = 0.1;

    private static final int seuilMorts = 1300;
    private static final int seuilInfection = 1600;

    public static int nbInfection = 2;
    public static int nbMorts = 0;
    public static int nbSain;
    public static int nbConfiner=0;
    public static int nbMaks=0;
    public static double contactProbability=0.95;

    public static double mediaImpact = 0.4;

    String csvFilePath = "EpidemicMetrics_scenario6.csv";



    public EpidemicSimulation(SimProperties sp) {
        super(sp);
    }

    public static void main(String[] args) throws Exception {
        IniFile ifile = new IniFile("D:\\EMSE\\Sem8\\Defi AI\\UP simulation\\helloSimulation\\parameters\\configuration.ini");
        SimProperties sp = new SimProperties(ifile);
        sp.simulationParams();
        sp.displayParams();
        System.out.println("seed: " + sp.seed);
        EpidemicSimulation es = new EpidemicSimulation(sp);
        es.createEnvironment();
        es.createObstacle();
        es.createRobot();
        es.initializeGW();
        es.schedule();
    }

    @Override
    public void createEnvironment() {
        environment = new ColorGridEnvironment(sp.rows, sp.columns, sp.debug, sp.seed);
    }

    @Override
    public void createObstacle() {
//        for (int i = 0; i < sp.nbobstacle; i++) {
//            ColorObstacle co = new ColorObstacle(environment.getPlace(), sp.colorobstacle);
//            addNewComponent(co);
//        }
    }


    /**
     * this function creates the agents.
     * initially most of them are healthy beside a certain number chosen in the configuration
     * these infected agents are randomly placed each time.
     */
    @Override
    public void createRobot() {
        int initial_infected = 2; //parameter

        int random[] = new int[initial_infected];
        for(int i=0; i<initial_infected; i++){
            random[i] = new Random().nextInt(sp.nbrobot-1);
        }
        for (int i = 0; i < sp.nbrobot; i++) {
            EpidemicAgent agent = new EpidemicAgent("Agent" + i, sp.field, sp.debug, environment.getPlace(), sp.colorrobot, sp.rows, sp.columns);
            for(int j=0; j<initial_infected;j++){
                if(i==random[j]){
                    agent.setInfected();
                }
            }
            addNewComponent(agent);

        }
    }

    @Override
    public void createGoal() {
        // Implement if needed
    }

    @Override
    public void schedule() {
        try(FileWriter writer = new FileWriter(csvFilePath)){
            Random random = new Random(sp.seed);
            List<Robot> robots = environment.getRobot();
            nbSain = sp.nbrobot - nbMorts - nbInfection;

            writer.append("nbSain,nbInfection,nbMorts,nbConfiner,nbMaks\n");
            writer.append(nbSain+","+nbInfection+","+nbMorts+","+nbConfiner+","+nbMaks);
            writer.append("\n");

            for (int i = 0; i < sp.step; i++) {
                nbConfiner=0;
                nbMaks=0;
                //avant chaque etape on doit mettre a jour plusieurs etats
                updateAgentStates(robots,random);

                System.out.println("Step: " + i);
                for (Robot robot : robots) {
                    EpidemicAgent epidemicAgent = (EpidemicAgent) robot;
                    // Decide whether to wear a mask
                    epidemicAgent.decideToWearMask();
                    // Simulate health outcome
                    simulateHealthOutcome(epidemicAgent, robots, random);
                }
                //only used for the last scenario where the gov decisions are only applied after the day 30;
                if(i>30){
                    GovDecisionConfinement(robots,random);
                    GovDecisionMask(robots,random);
                }

                nbSain = sp.nbrobot - nbMorts - nbInfection;
                writer.append(nbSain+","+nbInfection+","+nbMorts+","+nbConfiner+","+nbMaks);
                writer.append("\n");


                refreshGW();
//                try {
//                    Thread.sleep(sp.waittime);
//                } catch (InterruptedException ie) {
//                    System.out.println(ie);
//                }

            }
        }
        catch (IOException e) {
            System.err.println("Error exporting robots to CSV: " + e.getMessage());
            e.printStackTrace();
        }


    }

    private void GovDecisionMask(List<Robot> robots, Random random) {
        double randomNumber = random.nextDouble();
        if(nbMorts>seuilInfection){
            for(Robot robot: robots){
                if(!((EpidemicAgent)robot).isWearingMask){
                    ((EpidemicAgent)robot).acceptGovMask(random);
                }
            }
        }
    }

    private void GovDecisionConfinement(List<Robot> robots, Random random) {
        double randomNumber = random.nextDouble();
        if(nbMorts>seuilMorts){
            for(Robot robot: robots){
                if(!((EpidemicAgent)robot).isConfined){
                    ((EpidemicAgent)robot).acceptGovConfinment(random);
                }
            }
        }
    }


    public void updateAgentStates(List<Robot> robots, Random random){
        for(Robot agent: robots){
            EpidemicAgent a = ((EpidemicAgent) agent);
            if(a.incubationDays > 3){continue;}
            if( a.getHealthState() == EpidemicAgent.HealthState.INFECTED_S1 && a.incubationDays < 3 && a.incubationDays >0 ){
                a.incubationDays += 1;
            }
            if(a.getHealthState() == EpidemicAgent.HealthState.INFECTED_S1 && a.incubationDays == 3){
                a.healthState = EpidemicAgent.HealthState.INFECTED_S2;
            }

            //if the agent is confined he'll have confinement days that remains
            //decrease this count by one each day until it's 0 and the agent is not confined anymore
            //so he can't have a facteur de confinement to reduce the probability of infection 
            if(a.confinementDays>0){
                a.confinementDays -= 1;
            }else{
                a.isConfined = false;
            }

            if(a.maskDays>0){
                a.maskDays-=1;
            }
            else{
                a.isWearingMaskGov = false;
            }

            double randomNumber = random.nextDouble();
            if (randomNumber < proba_sortir_exterieur) {
                ((EpidemicAgent) agent).contactWithExterior = true;
            }

            double randomMedianumber = random.nextDouble();
            if(randomMedianumber<mediaImpact){
                ((EpidemicAgent) agent).isAffectedByMedia = true;
            }

            if(a.isConfined){
                nbConfiner+=1;
            }
            if(a.isWearingMask){
                nbMaks+=1;
            }
        }
    }


    public void simulateHealthOutcome(EpidemicAgent agent, List<Robot> robots,Random random) {


        //checkInfectionStage_confinement(robots);

        //define the daily circle of 15person
        // HashSet to store unique indices
        HashSet<Integer> chosenIndices = new HashSet<>();
        ArrayList<Robot> CircleContact = new ArrayList<>();
        // Choose random elements
        while (CircleContact.size() < numElements) {
            // Generate a random index
            int randomIndex = random.nextInt(robots.size());
            // Check if the index is not already chosen
            if (!chosenIndices.contains(randomIndex)) {
                // Add the index to the set of chosen indices
                chosenIndices.add(randomIndex);
                // Add the element at the random index to the result ArrayList
                CircleContact.add(robots.get(randomIndex));
            }
        }


        boolean isCloseContact = agent.identifyCloseContacts(contactProbability);


        if (agent.healthState == EpidemicAgent.HealthState.NON_INFECTED) {
            //check the circle of an agent if infected of stage 2
            //in that case he may want to isolate himself regardless if he was in contact with the person or not
            //because in the following code we check the close contact (only one time)
            for(Robot circleRobot: CircleContact){
                EpidemicAgent epidemicContact = (EpidemicAgent) circleRobot;
                //si il y'a quelqun dans son cercle qui est infecterS2
                //ET SI L'AGENT N'EST PAS DEJA CONFINE
                //alors il decide de se confiner
                //cela est important pour le nombre de jours de confinement
                if (epidemicContact.getHealthState() == EpidemicAgent.HealthState.INFECTED_S2 && !agent.isConfined){
                    agent.decideToConfine();
                }
            }


            //si un agent est sortie de la ville et a eu un contact il y'a une proba d'infection
            if(agent.contactWithExterior && random.nextDouble()<proba_infection_exterieur){
                agent.setInfected();
                nbInfection += 1;
            }
            else{
                // Check for infection based on close contacts
                for (Robot contact : CircleContact) {
                    //contacte uniquement avec le cercle de contacte
                    EpidemicAgent epidemicContact = (EpidemicAgent) contact;

                    // Check if the contact is infected
                    if (epidemicContact.getHealthState() == EpidemicAgent.HealthState.INFECTED_S1 || epidemicContact.getHealthState() == EpidemicAgent.HealthState.INFECTED_S2) {
                        // Adjust infection probability based on close contacts
                        if (isCloseContact) {

                            /*Pour l'implémentation de la ligne de base (baseline implementation),
                            nous n'incluons pas le port de masque et le confinement.
                            De plus, cela sera utile dans l'étape suivante pour
                            comparer les effets avec ou sans ces fonctionnalités.
                            pour cette raison on les met en commantaire.
                            */

                            //un agent soit porte un mask soit est confine il peut pas etre les deux en meme temps
                            if (agent.isWearingMask){
                                if(random.nextDouble() < proba_infection * facteur_reduc_mask) {
                                    agent.setInfected();
                                    nbInfection += 1;
                                    break; //once infected stop searching
                                }
                            }
                            else if(agent.isConfined){
                                if(random.nextDouble()<proba_infection*facteur_reduc_confinement){
                                    agent.setInfected();
                                    nbInfection += 1;
                                    break;
                                }
                            }
                            else{
                                if(random.nextDouble() < proba_infection) {
                                    agent.setInfected();
                                    nbInfection +=1;
                                    break; //once infected stop searching
                                }
                            }

                        }
                    }

                }
            }


        }
        // Simulate recovery or death from infection only if the person is in infection stage 2
        if (agent.getHealthState() == EpidemicAgent.HealthState.INFECTED_S2) {
            double randomValue = random.nextDouble();
            if (randomValue < proba_recovery) {
                agent.setRecovered();
                nbInfection -= 1;
            } else if (randomValue < proba_recovery + proba_mort) {
                //If the random value falls between the probability of recovery and the sum of the probabilities of recovery and mortality, the agent is marked as deceased.
                agent.setDeceased();
                nbInfection -= 1;
                nbMorts += 1;
            }
        }
    }


}
