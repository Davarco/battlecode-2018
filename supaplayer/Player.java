import java.util.HashMap;

import bc.*;


public class Player {

    private static GameController gc;
    private static VecUnit units;
    public static HashMap<Integer, Pathway> unitpaths;
    public static MapLocation focalPoint;
    public static boolean largemap;
    public static boolean initialKarbReached;
    public static int time;

    public static void main(String[] args) {

        // Start game by connecting to game controller
        gc = new GameController();
        unitpaths = new HashMap<>();
        initialKarbReached = false;
        largemap = (gc.startingMap(Planet.Earth).getHeight() + gc.startingMap(Planet.Earth).getHeight() < 55);

        // Initialize focus points
        //FocusPoints.init(gc);
        //FocusPoints.GeographicFocusPoints();

        // Initialize the different types of troops
        setUnits();
        Worker.init(gc);
        Knight.init(gc);
        Ranger.init(gc);
        Mage.init(gc);
        Factory.init(gc);
        Rocket.init(gc);

        // Initialize utils
        Util.init(gc);

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
            setUnits();

            // Run corresponding code for each type of unit
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                switch (unit.unitType() ) {
                	case Ranger:
                		Ranger.run(unit);
                		break;
                    case Worker:
                        Worker.run(unit);
                        break;
                    case Knight:
                        Knight.run(unit);
                        break;
                    case Mage:
                        Mage.run(unit);
                        break;
                    case Healer:
                        // Healer.run(unit);
                        break;
                    case Factory:
                        Factory.run(unit);
                        break;
                    case Rocket:
                        Rocket.run(unit);
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
        gc.queueResearch(UnitType.Ranger);  // 25
        gc.queueResearch(UnitType.Rocket);  // 100 <- Enables us to send troops to Mars
        gc.queueResearch(UnitType.Ranger);  // 25
        gc.queueResearch(UnitType.Ranger);  // 25
        gc.queueResearch(UnitType.Worker);  // 25
        gc.queueResearch(UnitType.Worker);  // 25
    }

    private static void setUnits() {

        // Get units and get counts
        units = gc.myUnits();
        Info.reset();
        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i);
            Info.addUnit(unit);
        }
        Info.totalUnits = units.size();
    }
}
