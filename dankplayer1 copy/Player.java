import java.util.ArrayList;
import java.util.HashMap;

import bc.*;


public class Player {

    private static GameController gc;
    private static VecUnit units;
    public static HashMap<Integer, Pathway> unitpaths;
    public static boolean initialKarbReached = false;
    public static MapLocation focalPoint;
    public static long time = 0;
    public static String mapsize = "";
    public static int roundcount=0;
    public static long workertime=0,rangertime=0;

    public static void main(String[] args) {

        // Start game by connecting to game controller
        gc = new GameController();
        unitpaths = new HashMap<>();
        if(gc.startingMap(Planet.Earth).getHeight()+gc.startingMap(Planet.Earth).getWidth()<55){
        	mapsize = "smallmap";
        }
        else{
        	mapsize = "largemap";
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
        Rocket.init(gc);

        // Initialize utils
        Util.init(gc);

        // Initialize path searching
        Pathing.init(gc);

        // Initialize the research tree
        initResearch();
//
//        MapLocation start = new MapLocation(Planet.Earth, 0, (int)gc.startingMap(Planet.Earth).getHeight()-1);
//        MapLocation end = new MapLocation(Planet.Earth, (int)gc.startingMap(Planet.Earth).getWidth()-1,0);
//        double t1 = System.currentTimeMillis();
//        ArrayList<MapLocation> res = Pathing.path(start, end);
//        double t2 = System.currentTimeMillis();
//        System.out.println("JPS took " + (t2-t1) + " ms :" + res);

        /*
        Main runner for player, do not change.
         */
        boolean quit = false;
        while (!quit) {
        	//System.out.println(gc.round() +" "+ gc.karbonite());
            long t1 = System.currentTimeMillis();
//            if (gc.round()==15)System.out.println("sfhabvsufgaksvl");
            // Debug, print current round
            // System.out.println("Current round: " + gc.round());

            // Get units and get counts

            setUnits();
            Pathing.reset();

            // Run corresponding code for each type of unit

            long ta, tb;
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                boolean onMars = unit.location().isOnPlanet(Planet.Mars) && !unit.location().isInGarrison();
                boolean onEarth = unit.location().isOnPlanet(Planet.Earth) && !unit.location().isInGarrison();
                switch (unit.unitType() ) {
                    case Ranger:
                    	ta=System.currentTimeMillis();
                        if (onEarth)
                            Ranger.runEarth(unit);
                        if (onMars)
                            Ranger.runMars(unit);
                        tb=System.currentTimeMillis();
                        rangertime+=(tb-ta);
                        break;
                    case Worker:
                    	ta=System.currentTimeMillis();
                        if (onEarth)
                            Worker.runEarth(unit);
                        if (onMars)
                            Worker.runMars(unit);
                        tb=System.currentTimeMillis();
                        workertime+=tb-ta;
                        break;
                    case Knight:
                        if (onEarth)
                            Knight.runEarth(unit);
                        if (onMars)
                            Knight.runMars(unit);
                        break;
                    case Mage:
                        if (onEarth)
                            Mage.runEarth(unit);
                        if (onMars)
                            Mage.runMars(unit);
                        break;
                    case Healer:
                        if (onEarth)
                            Healer.runEarth(unit);
                        if (onMars)
                            Healer.runMars(unit);
                        break;
                    case Factory:
                        Factory.run(unit); // Only can run on Earth
                        break;
                    case Rocket:
                        if (onEarth)
                            Rocket.runEarth(unit);
                        if (onMars)
                            Rocket.runMars(unit);
                        break;
                }
            }

            long t2 = System.currentTimeMillis();
            
            Player.time = 0;
            
            rangertime = 0;
            workertime = 0;
            // Complete round, move on to next one
            
            roundcount++;
            if(roundcount==9){
            	System.gc();
            	System.runFinalization();
            	roundcount = 0;
            }
            // System.out.println("Ranger #"+Info.number(UnitType.Ranger));
            // System.out.println("Ranger Time: "+rangertime);
            // System.out.println("Worker #"+Info.number(UnitType.Worker));
            // System.out.println("Worker Time: "+workertime);
            System.out.println("Total time "+ (t2-t1));
            
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
