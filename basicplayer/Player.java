import bc.*;

import java.nio.file.Path;

public class Player {

    private static GameController gc;

    public static void main(String[] args) {

        // Start game by connecting to game controller
        gc = new GameController();

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
            System.out.println("Current round: " + gc.round());

            // Get units and run corresponding code
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                switch (unit.unitType() ) {
                    case Worker:
                        Worker.run(unit, gc);
                        break;
                    case Knight:
                        Knight.run(unit, gc);
                        break;
                    case Ranger:
                        Ranger.run(unit, gc);
                        break;
                    case Mage:
                        Mage.run(unit, gc);
                        break;
                    case Healer:
                        Healer.run(unit, gc);
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
