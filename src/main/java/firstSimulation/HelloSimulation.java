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

import java.util.List;

public class HelloSimulation extends SimFactory {
    public HelloSimulation(SimProperties sp) {
        super(sp);
    }

    public static void main(String[] Args) throws Exception {
        IniFile ifile = null;
        try {
            ifile = new IniFile("D:\\EMSE\\Sem8\\Defi AI\\UP simulation\\helloSimulation\\parameters\\configuration.ini");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SimProperties sp = new SimProperties(ifile);
        sp.simulationParams();
        sp.displayParams();
        System.out.println("seed: " + sp.seed);
        HelloSimulation hs = new HelloSimulation(sp);
        hs.createEnvironment();
        hs.createObstacle();
        hs.createRobot();

        hs.initializeGW();
        hs.schedule();

    }


    @Override
    public void createEnvironment() {
        environment = new ColorGridEnvironment(sp.rows, sp.columns, sp.debug, sp.seed);
    }

    @Override
    public void createObstacle() {
        //getPlace() gets the empty emplacements
        for (int i = 0; i < sp.nbobstacle; i++) {
            ColorObstacle co = new ColorObstacle(environment.getPlace(), sp.colorobstacle);
            addNewComponent(co);
        }
    }

    @Override
    public void createRobot() {
        for (int i = 0; i < sp.nbrobot; i++) {
            HelloRobot hr = new HelloRobot("name" + i, sp.field, sp.debug, environment.getPlace(), sp.colorrobot, sp.rows, sp.columns);
            addNewComponent(hr);
        }
    }

    @Override
    public void createGoal() {

    }

    @Override
    public void schedule() {
        List<Robot> lr = environment.getRobot();

        for (int i = 0; i < sp.step; i++) {
            System.out.println("Step:␣" + i);
            for (Robot t : lr) {
                for (Robot t2 : lr) {
                    for (Message m : ((InteractionRobot) t2).popSentMessages()) {
                        if (t.getId() != t2.getId())
                            ((InteractionRobot) t).receiveMessage(m);
                    }
                }
            }


            for (Robot t : lr) {
                int[] posr = t.getLocation();
                Cell[][] p = environment.getNeighbor(t.getX(),
                        t.getY(), t.getField());
                t.updatePerception(p);
                t.move(1);
                updateEnvironment(posr, t.getLocation());
            }
            refreshGW();
            try {
                Thread.sleep(sp.waittime);
            } catch (InterruptedException ie) {
                System.out.println(ie);
            }
        }
    }

}
