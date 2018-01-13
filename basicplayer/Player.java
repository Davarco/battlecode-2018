import java.util.HashMap;

import bc.*;

public class Player {

    private static GameController gc;
    public static HashMap<Integer, Pathway> unitpaths;
    
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
        Factory.init(gc);
        Rocket.init(gc);

        // Initialize utils
        TeamUtil.init(gc);

        // Initialize path searching
        Pathing.init(gc);

        // Initialize the research tree
        initResearch();

        /*
        Main runner for player, do not change.
         */
        boolean quit = false;
        while (!quit) {

            // Debug, print current round
            // System.out.println("Current round: " + gc.round());

            // Get units and get counts
            VecUnit units = gc.myUnits();
            Count.reset();
            for (int i = 0; i < units.size(); i++) {
                Count.addUnit(units.get(i).unitType());
            }

            // Run corresponding code for each type of unit
            for (int i = 0; i < units.size(); i++) {
                Unit tempunit = units.get(i);
                switch (tempunit.unitType() ) {
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
                    case Factory:
                        Factory.run(tempunit);
                        break;
                    case Rocket:
                        Rocket.run(tempunit);
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
