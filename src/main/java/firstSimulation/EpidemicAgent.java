package firstSimulation;

import fr.emse.fayol.maqit.simulator.components.ColorInteractionRobot;
import fr.emse.fayol.maqit.simulator.components.Message;

import java.awt.*;
import java.util.Random;

public class EpidemicAgent extends ColorInteractionRobot {
    public enum HealthState { NON_INFECTED, INFECTED_S1, INFECTED_S2, RECOVERED, DECEASED }
    public boolean isWearingMask;
    public boolean isWearingMaskGov;
    public int maskDays = 0;
    public boolean isConfined;
    public boolean isCloseContact;
    public HealthState healthState;
    public int incubationDays = 0;
    public int confinementDays = 0;
    public boolean isAffectedByMedia = false;

    private static double maskProbability = 0.5;
    private static double maskMediaFactor = 1.1;
    private static double confinementProbability = 0.5;
    private static double confinmentMediaFactor = 1.1;
    private static double acceptProbability = 0.9;

    public boolean contactWithExterior = false;

    public EpidemicAgent(String name, int field, int debug, int[] pos, Color co, int rows, int columns){
        super(name, field, debug, pos, co, rows, columns);
        this.healthState = HealthState.NON_INFECTED;
        //setColor(new int[]{0,255,0});
    }

    // Méthode pour décider de porter un masque en fonction de différents facteurs
    public void decideToWearMask() {
        // Condition basée sur les mesures gouvernementales, la perception du risque, etc.
        double Maskproba;
        if(!isAffectedByMedia){
            Maskproba = maskProbability;
        }
        else{
            Maskproba = maskProbability*maskMediaFactor;
        }

        if (!this.isConfined && Math.random() < Maskproba) {
            this.isWearingMask = true;
        } else {
            this.isWearingMask = false;
        }

    }

    public int acceptGovConfinment(Random random){
        if (random.nextDouble() < acceptProbability) {
            this.isConfined = true;
            this.confinementDays = 14;
            return 1;
        } else {
            this.isConfined = false;
            return 0;
        }
    }

    public void acceptGovMask(Random random){
        if (random.nextDouble() < acceptProbability) {
            this.isWearingMaskGov = true;
            this.maskDays = 14;
        } else {
            this.isWearingMaskGov = false;
        }
    }

    // Méthode pour décider de se confiner en fonction de différents facteurs
    public int decideToConfine() {
        // Condition basée sur les mesures gouvernementales, les symptômes, etc.
        double Confproba;
        if(!isAffectedByMedia){
            Confproba = confinementProbability;
        }
        else{
            Confproba = confinementProbability*confinmentMediaFactor;
        }
        if (Math.random() < Confproba) {
            this.isConfined = true;
            this.confinementDays = 14;
            return 1;
        } else {
            this.isConfined = false;
            return 0;
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