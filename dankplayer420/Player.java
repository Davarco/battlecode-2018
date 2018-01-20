import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bc.*;


public class Player {

    private static GameController gc;
    private static VecUnit units;
    public static HashMap<Integer, Pathway> unitpaths;
    public static boolean initialKarbReached = false;
    public static MapLocation focalPoint;
    public static long Rangertime = 0;
    public static long Workertime = 0;
    public static String mapsize = "";
    public static int time=1000;

    public static void main(String[] args) {

        // Start game by connecting to game controller
        gc = new GameController();
        unitpaths = new HashMap<>();
        if(gc.startingMap(Planet.Earth).getHeight()+gc.startingMap(Planet.Earth).getHeight()<55){
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
        	
            long t1 = System.currentTimeMillis();
            if (gc.round()==15)System.out.println("Karbonite Rounds Over");
            // Debug, print current round
            // System.out.println("Current round: " + gc.round());

            // Get units and get counts
            setUnits();
            long ta,tb;
            // Run corresponding code for each type of unit
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                switch (unit.unitType() ) {
                	case Ranger:
                		ta = System.currentTimeMillis();
                		Ranger.run(unit);
                		tb = System.currentTimeMillis();
                		Rangertime+=(tb-ta);
                		break;
                    case Worker:
                    	ta = System.currentTimeMillis();
                        Worker.run(unit);
                        tb = System.currentTimeMillis();
                        Workertime+=(tb-ta);
                        break;
                    case Knight:
                        //Knight.run(unit);
                        break;
                    case Mage:
                        //Mage.run(unit);
                        break;
                    case Healer:
                        //Healer.run(unit);
                        break;
                    case Factory:
                        Factory.run(unit);
                        break;
                    case Rocket:
                        Rocket.run(unit);
                        break;
                }
            }
            long t2 = System.currentTimeMillis();
            time-=(t2 - t1);
            if(gc.planet()==Planet.Earth){
            	//System.out.println("Time: " + time);
            	System.out.println("Karbonite: " + gc.karbonite());
            	//System.out.println("Used: " + (t2 - t1));
            	//System.out.println("Ranger Count: "+ Info.number(UnitType.Ranger));
            	//System.out.println("Worker Count: "+ Info.number(UnitType.Worker));
            }
            // System.out.println("pathing: " + time);
            time+=50;
            Rangertime = 0;
            Workertime = 0;
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
