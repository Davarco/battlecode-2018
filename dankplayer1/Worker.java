import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Worker {

    private static Unit worker;
    private static GameController gc;
    private static VecUnit rockets;
    private static HashMap<Integer, Direction> directionMap;
    private static HashMap<Integer, Integer> counterMap;
    private static int W,H;
    private static final int min_karbonite=100;
    private static ArrayList<Integer> suicideSquad;

    public static void init(GameController controller) {
        gc = controller;
        directionMap = new HashMap<>();
        counterMap = new HashMap<>();
        suicideSquad = new ArrayList<>();
    }

    public static void run(Unit unit) {
    	
        // Receive worker from main runner
        worker = unit;
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        
        // Build things that we need to
        build();

        // General move function, handles priorities
        long t1 = System.currentTimeMillis();
        move();
        long t2 = System.currentTimeMillis();
        Player.time += (t2 - t1);

        // Repair structures that we can
        repairStructure(UnitType.Rocket);
        repairStructure(UnitType.Factory);

        // Harvest karbonite if we can
        harvestKarbonite();
        
        //MAKE SURE THIS IS RUN!!!!!!!!!!!!!!!
        if(gc.karbonite()-min_karbonite>20*Info.number(UnitType.Factory)){
        	replicate();
        }
    }



    private static void move() {
        /*
        TODO Implement the entire worker move function as a heuristic based on priority 
         */

        // Only move if we can move
        if (!gc.isMoveReady(worker.id()))
            return;

        // Similar to below, rockets are vital late game
        /*if (gc.round() <= 550 && gc.round() > Config.ROCKET_CREATION_ROUND) {
            if (moveTowardsRocket())
                return;
        }*/

        // Moving towards factories has higher priority than escape early game
        if (gc.round() < 20) {
            if (ditchFactory())
                return;
        	if (moveTowardsFactory())
        		return;
            if (escape())
                return;
            
        } else {
            if (escape())
                return;
            if (ditchFactory())
                return;
            if (moveTowardsFactory())
                return;
        }

        // Move towards karbonite after our buildings are taken care of
        if (moveTowardsKarbonite())
            return;

    }

    private static void build() {
    	
        // Create factories
    	int FactoryNumber=Info.number(UnitType.Factory);
        if (gc.karbonite()-100>20*FactoryNumber) {
        	VecUnit things = gc.senseNearbyUnitsByType(worker.location().mapLocation(), 31, UnitType.Factory);
        	if(things.size()==0)
        		create(UnitType.Factory);
        }


        /*
        // Build rockets
        if (gc.round() > Config.ROCKET_CREATION_ROUND && gc.round() <= 640) {
            if (Info.number(UnitType.Rocket) < Config.ROCKET_EQUILIBRIUM) {
                create(UnitType.Rocket);
            }
        }*/
    }

    private static boolean escape() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            System.out.println("Worker " + worker.location().mapLocation() + " is being attacked!");
            return true;
        }

        return false;
    }

    private static boolean moveTowardsRocket() {

        // Move towards a low-HP rocket if possible
        rockets = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(worker.location().mapLocation());
            if (Util.friendlyUnit(rockets.get(i)) && rockets.get(i).health() < rockets.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            Pathing.move(worker, rockets.get(idx).location().mapLocation());
            // System.out.println("Moving towards friendly rocket.");
            return true;
        }

        return false;
    }

    private static boolean moveTowardsFactory() {

        // Move towards the closest factory
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist = worker.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist < minDist && units.get(i).health() < units.get(i).maxHealth()) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            //Pathing.move(worker, units.get(idx).location().mapLocation());
            PlanetMap map = gc.startingMap(Planet.Earth);
                MapLocation tmp = units.get(idx).location().mapLocation();
                tmp = new MapLocation(Planet.Earth, tmp.getX() + 1, tmp.getY());
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
                tmp = new MapLocation(Planet.Earth, tmp.getX() - 1, tmp.getY());
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
                tmp = new MapLocation(Planet.Earth, tmp.getX() + 1, tmp.getY() + 1);
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
                tmp = new MapLocation(Planet.Earth, tmp.getX() - 1, tmp.getY() - 1);
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
                tmp = new MapLocation(Planet.Earth, tmp.getX() + 1, tmp.getY() - 1);
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
                tmp = new MapLocation(Planet.Earth, tmp.getX() - 1, tmp.getY() + 1);
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
                tmp = new MapLocation(Planet.Earth, tmp.getX(), tmp.getY() - 1);
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
                tmp = new MapLocation(Planet.Earth, tmp.getX(), tmp.getY() + 1);
                if (map.onMap(tmp) && Pathing.move(worker, tmp)) return true;
        }

        return false;
    }

    private static boolean ditchFactory() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
            long dist = Config.FACTORY_STANDOFF_RADIUS - worker.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0)  return false;

        Direction opposite = Pathing.opposite(worker.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(worker, opposite);
        return true;
    }

    private static boolean moveTowardsKarbonite() {
        MapLocation loc = bestKarboniteLoc();
        if (loc != worker.location().mapLocation()) { // bestKarboniteLoc returns the worker's position if nothing is found
            return Pathing.move(worker, loc);
        }
        
        return false;
    }

    private static void repairStructure(UnitType StructureToRepair) {
        VecUnit structures = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), StructureToRepair);

        // Repair a Structure in range, only in Earth
        if (worker.location().mapLocation().getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < structures.size(); i++) {
                if (gc.canBuild(worker.id(), structures.get(i).id())) {
                    gc.build(worker.id(), structures.get(i).id());
                }
                if (gc.canRepair(worker.id(), structures.get(i).id())) {
                    gc.repair(worker.id(), structures.get(i).id());
                }
            }
        }
    }

    private static void harvestKarbonite() {
        for (Direction d : Direction.values()) {
            if (gc.startingMap(Planet.Earth).onMap(worker.location().mapLocation().add(d)) && gc.canHarvest(worker.id(), d)) {
                gc.harvest(worker.id(), d);
            }
        }
    }

    private static MapLocation bestKarboniteLoc() {
        VecMapLocation allInRange = gc.allLocationsWithin(worker.location().mapLocation(), worker.visionRange());
        int bestHeuristicSoFar = Integer.MAX_VALUE;
        int bestIdxSoFar = -1;
        for (int i = 0; i < allInRange.size(); i++) {
            MapLocation loc = allInRange.get(i);
            if (gc.startingMap(Planet.Earth).onMap(loc) && gc.karboniteAt(loc) > 0 && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1)) {
                int heuristic = (int) (Math.sqrt(50 - gc.karboniteAt(loc)) + Math.pow((int) (worker.location().mapLocation().distanceSquaredTo(loc)), 2));
                if (heuristic < bestHeuristicSoFar) {
                    bestHeuristicSoFar = heuristic;
                    bestIdxSoFar = i;
                }
            }
        }

        if (bestIdxSoFar != -1) {
            return allInRange.get(bestIdxSoFar);
        } else {
            return worker.location().mapLocation();
        }
    }
    private static void replicate(){
        for (Direction dir : Direction.values()) {
            if (gc.canReplicate(worker.id(), dir)) {
                gc.replicate(worker.id(), dir);
            }
        }
    }

    private static void create(UnitType type) {
        for (Direction dir : Direction.values()) {
            if (gc.canBlueprint(worker.id(), type, dir)) {
                gc.blueprint(worker.id(), type, dir);
            }
        }
    }
}