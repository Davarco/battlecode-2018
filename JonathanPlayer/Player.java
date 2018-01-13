import bc.*;
import java.util.*;

import java.nio.file.Path;

public class Player {

    private static GameController gc;
    public static HashMap<Integer, Pathway> unitpaths;
    public static int X1, Y1, X2, Y2;

    public static void main(String[] args) {

        // Start game by connecting to game controller
        gc = new GameController();
        unitpaths = new HashMap<Integer, Pathway>();

        // Initialize the different types of troops
        Worker.init(gc);
        Knight.init(gc);
        Ranger.init(gc);
        Mage.init(gc);
        Healer.init(gc);

        // Initialize utils
        Util.init(gc);

        // Initialize path searching
        Pathing.init(gc);

        // Initialize the research tree
        initResearch();
        X1 = (int)(Math.random()*19+1);
        Y1 = (int)(Math.random()*19+1);
        X2 = (int)(Math.random()*19+1);
        Y2 = (int)(Math.random()*19+1);
        System.out.println(X1+" "+Y1+", "+X2+" "+Y2);


        /*
        Main runner for player, do not change.
         */
        boolean quit = false;
        while (!quit) {

            // Debug, print current round
            System.out.println("Current round: " + gc.round());

            // Get units and run corresponding code
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit tempunit = units.get(i);
                switch (tempunit.unitType()) {
                    case Worker:
                        Worker.run(tempunit);
                        break;
                    case Knight:
                        Knight.run(tempunit);
                        break;
                    case Ranger:
                        Ranger.run(tempunit);
                        break;
                    case Mage:
                        Mage.run(tempunit);
                        break;
                    case Healer:
                        Healer.run(tempunit);
                        break;
                }
            }

            // Complete round, move on to next one
            gc.nextTurn();
        }
    }

    /*
    Initializes research path.
    The total number of turns should be equal to ~1000.
     */
    private static void initResearch() {
        System.out.println("Initializing research tree!");
        gc.queueResearch(UnitType.Worker);  // 25
        gc.queueResearch(UnitType.Knight);  // 25
        gc.queueResearch(UnitType.Ranger);  // 25
        gc.queueResearch(UnitType.Rocket);  // 100 <- Enables us to send troops to Mars
    }
}
