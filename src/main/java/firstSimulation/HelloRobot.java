package firstSimulation;

import fr.emse.fayol.maqit.simulator.components.ColorInteractionRobot;
import fr.emse.fayol.maqit.simulator.components.Message;

import java.awt.*;

public class HelloRobot extends ColorInteractionRobot {

    public HelloRobot(String name, int field, int debug, int[] pos, Color co, int rows, int columns){
        super(name, field, debug, pos, co, rows, columns);
    }

    @Override
    public void handleMessage(Message message) {
        System.out.println(message);
    }

    @Override
    public void move(int i) {
        this.readMessages();
        if(this.freeForward()){
            this.moveForward();
        }
        else{
            this.turnLeft();
        }
        sendMessage(new Message(getId(),"location: ("+x+","+y+")" ));
    }
}
