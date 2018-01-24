import java.util.HashMap;

import bc.*;


public class Player {

    private static GameController gc;
    private static VecUnit units;

    public static HashMap<Integer, Pathway> unitpaths;
    public static PlanetMap earthMap;
    public static PlanetMap marsMap;
    public static MapLocation focalPoint;
    public static MapLocation focalPointMars;
    public static MapLocation[][] mapLocations;
    public static MapLocation[][] mapLocationsMars;

    public static boolean largeMap;
    public static boolean initialKarbReached;
    public static int[][] karboniteMap;
    public static int[][] karboniteMapMars;
    public static int earthWidth;
    public static int earthHeight;
    public static int marsWidth;
    public static int marsHeight;
    public static int launchCounter;

    /*
    Mostly just for debugging.
     */
    public static long time;
    public static long workertime, rangertime, factorytime;
    public static int rangercount, workercount;


    public static void main(String[] args) {

        // Start game by connecting to game controller and setting constants
        gc = new GameController();
        unitpaths = new HashMap<>();
        earthMap = gc.startingMap(Planet.Earth);
        marsMap = gc.startingMap(Planet.Mars);
        earthHeight = (int) earthMap.getHeight();
        earthWidth = (int) earthMap.getWidth();
        marsHeight = (int) marsMap.getHeight();
        marsWidth = (int) marsMap.getWidth();
        karboniteMap = new int[earthWidth][earthHeight];
        karboniteMapMars = new int[marsWidth][marsHeight];
        mapLocations = new MapLocation[earthWidth][earthHeight];
        mapLocationsMars = new MapLocation[marsWidth][marsHeight];

        largeMap =  !(gc.startingMap(Planet.Earth).getHeight() + gc.startingMap(Planet.Earth).getWidth() < 55);
        if (gc.planet() == Planet.Earth) {
            for (int i = 0; i < earthWidth; i++) {
                for (int j = 0; j < earthHeight; j++) {
                    karboniteMap[i][j] = (int) earthMap.initialKarboniteAt(new MapLocation(Planet.Earth, i, j));
                    mapLocations[i][j] = new MapLocation(Planet.Earth, i, j);
                }
            }
        }
        if (gc.planet() == Planet.Mars) {
            for (int i = 0; i < marsWidth; i++) {
                for (int j = 0; j < marsHeight; j++) {
                    karboniteMapMars[i][j] = (int) marsMap.initialKarboniteAt(new MapLocation(Planet.Mars, i, j));
                    mapLocationsMars[i][j] = new MapLocation(Planet.Mars, i, j);
                }
            }
        }

        // Initialize focus points
        //FocusPoints.init(gc);
        //FocusPoints.GeographicFocusPoints();

        // Initialize the different types of troops
        setUnits();
        Worker.init(gc);
        Knight.init(gc);
        Ranger.init(gc);
        Mage.init(gc);
        Healer.init(gc);
        Factory.init(gc);
        Mars.init(gc);
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

            //System.out.println(gc.round() +" "+ gc.karbonite());
            long t1 = System.currentTimeMillis();
            // Debug, print current round
            // System.out.println("Current round: " + gc.round());

            // Get units and get counts
            setUnits();

            // Run corresponding code for each type of unit
            long ta, tb;
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                boolean onMars = unit.location().isOnPlanet(Planet.Mars) && !unit.location().isInGarrison();
                boolean onEarth = unit.location().isOnPlanet(Planet.Earth) && !unit.location().isInGarrison();
                switch (unit.unitType()) {
                    case Ranger: {
                        rangercount++;
                        ta = System.currentTimeMillis();
                        if (onEarth)
                            Ranger.runEarth(unit);
                        if (onMars)
                            Ranger.runMars(unit);
                        tb = System.currentTimeMillis();
                        rangertime += (tb - ta);
                        break;
                    }
                    case Worker: {
                        workercount++;
                        ta = System.currentTimeMillis();
                        if (onEarth)
                            Worker.runEarth(unit);
                        if (onMars)
                            Worker.runMars(unit);
                        tb = System.currentTimeMillis();
                        workertime+=tb-ta;
                        break;
                    }
                    case Knight: {
                        if (onEarth)
                            Knight.runEarth(unit);
                        if (onMars)
                            Knight.runMars(unit);
                        break;
                    }
                    case Mage: {
                        if (onEarth)
                            Mage.runEarth(unit);
                        if (onMars)
                            Mage.runMars(unit);
                        break;
                    }
                    case Healer: {
                        if (onEarth)
                            Healer.runEarth(unit);
                        if (onMars)
                            Healer.runMars(unit);
                        break;
                    }
                    case Factory: {
                        ta = System.currentTimeMillis();
                        Factory.run(unit); // Only can run on Earth
                        tb = System.currentTimeMillis();
                        factorytime += tb - ta;
                        break;
                    }
                    case Rocket: {
                        if (onEarth)
                            Rocket.runEarth(unit);
                        if (onMars)
                            Rocket.runMars(unit);
                        break;
                    }
                }
            }

            // Check times
            long t2 = System.currentTimeMillis();
            Player.time = 0;
            rangertime = 0;
            workertime = 0;
            System.out.println("Ranger #" + Info.number(UnitType.Ranger));
            System.out.println("Ranger Time: " + rangertime);
            System.out.println("Worker #" + Info.number(UnitType.Worker));
            System.out.println("Worker Time: " + workertime);
            System.out.println("Factory #" + Info.number(UnitType.Factory));
            System.out.println("Factory Time: " + factorytime);
            System.out.println("Total time " + (t2 - t1));
            if (gc.round() % 10 == 0) {
                System.gc();
                System.runFinalization();
            }

            rangercount = 0;
            workercount = 0;
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
            Info.addUnit(unit.unitType());
        }
        Info.totalUnits = units.size();
        // System.out.println("Numbers: " + Info.number(UnitType.Ranger) + " " + Info.number(UnitType.Worker) + " " + Info.number(UnitType.Rocket) + " " + Info.number(UnitType.Factory));
    }
}
