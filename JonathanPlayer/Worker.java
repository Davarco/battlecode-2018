import bc.*;
import java.util.*;

public class Worker {

    private static Unit worker;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive worker from main runner
        worker = unit;

        // Move unit (placeholder for now)
        if(gc.isMoveReady(worker.id())) {
        	     move();
        }
    }

    private static void move() {

        // See if unit needs to escape
      //  if (Pathing.escape(worker))
         //   return;
        // Otherwise move towards the closest kryptonite
    	System.out.println("I WANT TO MOVE");
    	if(worker.team() == Team.Blue) {
    	   Pathing.move(worker, new MapLocation(Planet.Earth, Player.X1, Player.Y1));
    }
    	else {
       Pathing.move(worker, new MapLocation(Planet.Earth, Player.X2, Player.Y2));
    	}
  }
}
