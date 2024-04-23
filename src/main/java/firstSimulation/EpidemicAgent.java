package firstSimulation;

import fr.emse.fayol.maqit.simulator.components.ColorInteractionRobot;
import fr.emse.fayol.maqit.simulator.components.Message;

import java.awt.*;
import java.util.Random;

public class EpidemicAgent extends ColorInteractionRobot {
    public enum HealthState { NON_INFECTED, INFECTED_S1, INFECTED_S2, RECOVERED, DECEASED }
    public boolean isWearingMask;
    public boolean isConfined;
    public boolean isCloseContact;
    public HealthState healthState;
    public int incubationDays = 0;
    public int confinementDays = 0;

    public EpidemicAgent(String name, int field, int debug, int[] pos, Color co, int rows, int columns){
        super(name, field, debug, pos, co, rows, columns);
        this.healthState = HealthState.NON_INFECTED;
        //setColor(new int[]{0,255,0});
    }

    // Méthode pour décider de porter un masque en fonction de différents facteurs
    public void decideToWearMask() {
        // Condition basée sur les mesures gouvernementales, la perception du risque, etc.
        double maskProbability = 0.5; // Probabilité initiale de porter un masque
        if (Math.random() < maskProbability) {
            this.isWearingMask = true;
        } else {
            this.isWearingMask = false;
        }

    }

    // Méthode pour décider de se confiner en fonction de différents facteurs
    public void decideToConfine() {
        // Condition basée sur les mesures gouvernementales, les symptômes, etc.
        double confinementProbability = 0.6; // Probabilité initiale de se confiner
        if (Math.random() < confinementProbability) {
            this.isConfined = true;
            this.confinementDays = 14;
        } else {
            this.isConfined = false;
        }
    }

    // Méthode pour identifier les cas contacts en fonction de différents critères
    public boolean identifyCloseContacts(double contactProbability) {
        // Conditions based on distance, duration, shared enclosed space, etc.
        if (Math.random() < contactProbability) {
            this.isCloseContact = true;
        } else {
            this.isCloseContact = false;
        }
        return isCloseContact;
    }

    @Override
    public void handleMessage(Message message) {
        System.out.println(message);
    }

    @Override
    public void move(int i) {

    }


    public void setInfected() {
        this.healthState = HealthState.INFECTED_S1;
        incubationDays = 1;
        setColor(new int[]{255, 0, 0}); // Red
    }

    public void setRecovered() {
        this.healthState = HealthState.RECOVERED;
        setColor(new int[]{0, 0, 255});
    }

    public void setDeceased() {
        this.healthState = HealthState.DECEASED;
        setColor(new int[]{0, 0, 0});
    }


    public void setInfected_S2(){
        this.healthState = HealthState.INFECTED_S2;
    }

    public HealthState getHealthState(){
        return healthState;
    }

}