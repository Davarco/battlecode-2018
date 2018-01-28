import java.util.ArrayList;
import java.util.HashMap;

import bc.*;


public class Player {

    private static GameController gc;
    private static VecUnit units;
    public static HashMap<Integer, Pathway> unitpaths;
    public static boolean initialKarbReached = false;
    public static MapLocation focalPoint;
    public static MapLocation focalPointMars;
    public static long time = 0;
    private static int W, H;
    public static String mapsize = "";
    public static int roundcount=0;
    public static long workertime=0,rangertime=0, factorytime = 0;
    public static int rangercount=0, workercount = 0;
    public static int[][] karboniteMap;
    public static int[][] karboniteMapMars;
    public static int earthWidth;
    public static int earthHeight;
    public static int marsWidth;
    public static int marsHeight;
    public static int launchCounter = 0;
    public static MapLocation enemy = null;

     
 
     public static void main(String[] args) {
 
         // Start game by connecting to game controller
         gc = new GameController();
         unitpaths = new HashMap<>();
        PlanetMap pm = gc.startingMap(Planet.Earth);
        PlanetMap pm1 = gc.startingMap(Planet.Mars);
        earthHeight = (int)pm.getHeight();
        earthWidth = (int)pm.getWidth();
        marsHeight = (int)pm1.getHeight();
        marsWidth = (int)pm1.getWidth();
        karboniteMap = new int[earthWidth][earthHeight];
        karboniteMapMars = new int[marsWidth][marsHeight];
        W = (int)pm.getWidth();
        H = (int)pm.getHeight();
       
        
        if(gc.startingMap(Planet.Earth).getHeight()+gc.startingMap(Planet.Earth).getWidth()<55){
        	mapsize = "smallmap";
        }
        else{
        	mapsize = "largemap";
        }
        if(gc.planet()==Planet.Earth){
        	 for (int i = 0; i < earthWidth; i++) {
                 for (int j = 0; j < earthHeight; j++) {
                     karboniteMap[i][j] = (int) pm.initialKarboniteAt(new MapLocation(Planet.Earth, i, j));
                 }
             }
        }
        if(gc.planet()==Planet.Mars){
    		for (int i = 0; i < marsWidth; i++) {
                for (int j = 0; j < marsHeight; j++) {
                    karboniteMapMars[i][j] = (int) pm1.initialKarboniteAt(new MapLocation(Planet.Mars, i, j));
                }
            }
    	}

        // Initialize focus points
        //FocusPoints.init(gc);
        //FocusPoints.GeographicFocusPoints();

        // Initialize the different types of troops
        setUnits();
        Pathing.reset();
        FocusPoints.init(gc);
        Mars.init(gc);
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
        if(mapsize.equals("largemap")){
        	initResearch();
        }
        else{
        	initResearchSmall();
        }
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

            int idx1 = 0;
            if(gc.round() == 1 && gc.planet().equals(Planet.Earth)){
            	MapLocation mltemp = units.get(idx1).location().mapLocation();
            	System.out.println(mltemp.getX()+" "+mltemp.getY()+" "+W+" "+H+" "+"HI");
            	while(Mars.earthplaces[mltemp.getX()][mltemp.getY()]!= Mars.earthplaces[W-mltemp.getX()-1][H-mltemp.getY()-1]){
            		idx1++;
                	 mltemp = units.get(idx1).location().mapLocation();
            	}
            	enemy = new MapLocation(mltemp.getPlanet(), W-mltemp.getX(), H-mltemp.getY());
            }
            // Run corresponding code for each type of unit

            long ta, tb;
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                boolean onMars = unit.location().isOnPlanet(Planet.Mars) && !unit.location().isInGarrison();
                boolean onEarth = unit.location().isOnPlanet(Planet.Earth) && !unit.location().isInGarrison();
                switch (unit.unitType() ) {
                    case Ranger:
                    	rangercount++;
                    	ta=System.currentTimeMillis();
                        if (onEarth)
                            Ranger.runEarth(unit);
                        if (onMars)
                            Ranger.runMars(unit);
                        tb=System.currentTimeMillis();
                        rangertime+=(tb-ta);
                        break;
                    case Worker:
                    	workercount++;
                    	ta=System.currentTimeMillis();
                        if (onEarth)
                            Worker.runEarth(unit);
                        if (onMars)
                            Worker.runMars(unit);
                        tb=System.currentTimeMillis();
                        //workertime+=tb-ta;
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
                    	ta=System.currentTimeMillis();
                        Factory.run(unit); // Only can run on Earth
                        tb=System.currentTimeMillis();
                        factorytime+=tb-ta;
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
            /*System.out.println("Ranger #"+Info.number(UnitType.Ranger));
            System.out.println("Ranger Time: "+rangertime);
            System.out.println("Worker #"+Info.number(UnitType.Worker));
            System.out.println("Worker Time: "+workertime);
            System.out.println("Factory #"+Info.number(UnitType.Factory));
            System.out.println("Factory Time: "+factorytime);
            System.out.println("Total time "+ (t2-t1));
            ;*/
            
            rangertime = 0;
            workertime = 0;
            // Complete round, move on to next one
            
            roundcount++;
            if(roundcount==9){
            	System.gc();
            	System.runFinalization();
            	roundcount = 0;
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
        gc.queueResearch(UnitType.Healer);  // 25
        gc.queueResearch(UnitType.Healer);  // 25
        gc.queueResearch(UnitType.Ranger);  // 25
        gc.queueResearch(UnitType.Healer);  // 25
    }
    private static void initResearchSmall() {
    	gc.queueResearch(UnitType.Rocket);
        System.out.println("Initializing research tree!");
        gc.queueResearch(UnitType.Worker);  // 25
        gc.queueResearch(UnitType.Ranger);  // 25
          // 100 <- Enables us to send troops to Mars
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
            if(unit.unitType()==UnitType.Rocket || unit.unitType()==UnitType.Factory){
            	Info.addUnit(unit);
            }
            Info.addUnit(unit.unitType());
        }
        Info.totalUnits = units.size();
        System.out.println("sdfoijwoij "+Info.number(UnitType.Ranger)+" "+Info.number(UnitType.Healer)+" "+Info.number(UnitType.Rocket)+" "+Info.number(UnitType.Factory));
    }
}
