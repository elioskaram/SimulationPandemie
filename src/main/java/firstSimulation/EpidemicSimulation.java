package firstSimulation;

import fr.emse.fayol.maqit.simulator.SimFactory;
import fr.emse.fayol.maqit.simulator.components.ColorObstacle;
import fr.emse.fayol.maqit.simulator.components.InteractionRobot;
import fr.emse.fayol.maqit.simulator.components.Message;
import fr.emse.fayol.maqit.simulator.components.Robot;
import fr.emse.fayol.maqit.simulator.configuration.SimProperties;
import fr.emse.fayol.maqit.simulator.configuration.IniFile;
import fr.emse.fayol.maqit.simulator.environment.Cell;
import fr.emse.fayol.maqit.simulator.environment.ColorGridEnvironment;
import fr.emse.fayol.maqit.simulator.environment.GridEnvironment;
import fr.emse.fayol.maqit.simulator.components.ColorInteractionRobot;


import java.util.ArrayList;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.awt.*;
import java.util.Random;

public class EpidemicSimulation extends SimFactory {

    private static final double proba_infection = 0.1;
    private static final double CONTACT_INFECTION_MULTIPLIER = 1.5;
    private static final int numElements = 15;

    private static final double facteur_reduc_mask = 0.8;
    private static final double facteur_reduc_confinement = 0.1;
    private static final double proba_recovery = 0.2;
    private static final double proba_mort = 0.01;

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
            random[i] = new Random().nextInt(10000-1);
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
        List<Robot> robots = environment.getRobot();

        for (int i = 0; i < sp.step; i++) {
            System.out.println("Step: " + i);
            for (Robot robot : robots) {
                EpidemicAgent epidemicAgent = (EpidemicAgent) robot;
                // Decide whether to wear a mask
                epidemicAgent.decideToWearMask();
                // Decide whether to confine
                epidemicAgent.decideToConfine();
                //REMOVE THAT FUNCTION TO DEAL WITH IT BSAED ON THE MEDIA AND ON OTHER'S INFECTION
                // Simulate health outcome
                simulateHealthOutcome(epidemicAgent, robots);
            }
            refreshGW();
            try {
                Thread.sleep(sp.waittime);
            } catch (InterruptedException ie) {
                System.out.println(ie);
            }
        }
    }

    public void simulateHealthOutcome(EpidemicAgent agent, List<Robot> robots) {
        Random random = new Random();

        boolean isCloseContact = agent.identifyCloseContacts(0.7);

        // HashSet to store unique indices
        HashSet<Integer> chosenIndices = new HashSet<>();

        ArrayList<Robot> CercleContact = new ArrayList();
        // Choose random elements
        while (CercleContact.size() < numElements) {
            // Generate a random index
            int randomIndex = random.nextInt(robots.size());
            // Check if the index is not already chosen
            if (!chosenIndices.contains(randomIndex)) {
                // Add the index to the set of chosen indices
                chosenIndices.add(randomIndex);
                // Add the element at the random index to the result ArrayList
                CercleContact.add(robots.get(randomIndex));
            }
        }

        if (agent.healthState == EpidemicAgent.HealthState.NON_INFECTED) {
            // Check for infection based on close contacts

            for (Robot contact : CercleContact) {
                //contacte uniquement avec le cercle de contacte
                EpidemicAgent epidemicContact = (EpidemicAgent) contact;

                // Check if the contact is infected
                if (epidemicContact.getHealthState() == EpidemicAgent.HealthState.INFECTED) {
                    // Adjust infection probability based on close contacts
                    if (isCloseContact) {
                        if(agent.isWearingMask && Math.random() < proba_infection*facteur_reduc_mask){
                            agent.setInfected();
                        }
                        break; // Once infected, no need to check further contacts
                    }
                }
            }





        } else if (agent.healthState == EpidemicAgent.HealthState.INFECTED) {
            // Check if the agent recovers or dies
            double recoveryChance = random.nextDouble();
            double mortalityChance = random.nextDouble();
            if (recoveryChance < proba_recovery) {
                agent.setRecovered();
            } else if (mortalityChance < proba_mort) {
                agent.setDeceased();
            }
        }
    }

}
