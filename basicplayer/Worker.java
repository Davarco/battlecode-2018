import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Worker {

    private static Unit worker;
    private static GameController gc;
    private static VecUnit factories;
    private static VecUnit rockets;
    private static boolean isAttacked;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive worker from main runner
        worker = unit;
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        build();
        // General move function, handles priorities
        long t1 = System.currentTimeMillis();
        move();
        long t2 = System.currentTimeMillis();
        Player.time += (t2 - t1);

        // Build things that we need to
        

        // Repair structures that we can
        repairStructure(UnitType.Rocket);
        repairStructure(UnitType.Factory);

        // Harvest karbonite if we can
        harvestKarbonite();
    }

    private static void move() {
        /*
        TODO Implement the entire worker move function as a heuristic based on priority 
         */

        // Only move if we can move
        if (!gc.isMoveReady(worker.id()))
            return;
        
        if(gc.round()<80){
        	if (moveTowardsFactory())
        		return;
        }
        // Escaping enemy units has the highest priority
        if(worker.health()<=40){
        	if (escape())
        		return;
        }
        if(gc.round()<=550 && gc.round()>Config.ROCKET_CREATION_ROUND){
	        // As we won't have rockets till later, I'm assuming our factories should be mostly built by then
	        if (moveTowardsRocket())
	            return;
	       
        }
        /// Repairing factories isn't as important, but is vital early game
        if(gc.round()>=80){
        	if (moveTowardsFactory())
        		return;
        }
        // Move towards karbonite after our buildings are taken care of
        if (moveTowardsKarbonite())
            return;
        
        // If all of the above failed, we likely have to clear up some space
        if (moveOutOfWay())
            return;
    }

    private static void build() {

        // Create factories
    	
        if (Info.number(UnitType.Factory) < (((int)gc.startingMap(Planet.Earth).getWidth())*((int)gc.startingMap(Planet.Earth).getHeight())/450 +1) && Info.number(UnitType.Factory) < ((int)(gc.round()/20)+1)) {
        	create(UnitType.Factory);
        }

        // Replicate if we don't have enough workers
        if (Info.number(UnitType.Worker) < ((int)gc.startingMap(Planet.Earth).getWidth())*((int)gc.startingMap(Planet.Earth).getHeight())/150) {
            replicate();
        }

        // Build rockets
        if (gc.round() > Config.ROCKET_CREATION_ROUND && gc.round()<=640) {
            if (Info.number(UnitType.Rocket) < Config.ROCKET_EQUILIBRIUM) {
                create(UnitType.Rocket);
            }
        }
    }

    private static boolean escape() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            isAttacked = true;
            System.out.println("Worker " + worker.location().mapLocation() + " is being attacked!");
            return true;
        } else {
            isAttacked = false;
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
        List<MapLocation> locations = Info.unitLocations.get(UnitType.Factory);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < locations.size(); i++) {
            long dist = worker.location().mapLocation().distanceSquaredTo(locations.get(i));
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            Pathing.move(worker, locations.get(idx));
            return true;
        }

        return false;

//        // Move towards a low-HP factory if possible
//        factories = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), UnitType.Factory);
//        long minDist = Long.MAX_VALUE;
//        int idx = -1;
//        for (int i = 0; i < factories.size(); i++) {
//            long dist = factories.get(i).location().mapLocation().distanceSquaredTo(worker.location().mapLocation());
//            if (Util.friendlyUnit(factories.get(i)) && factories.get(i).health() < factories.get(i).maxHealth() && dist < minDist) {
//                minDist = dist;
//                idx = i;
//            }
//        }
//        if (idx != -1) {
//            Pathing.move(worker, factories.get(idx).location().mapLocation());
//            // System.out.println("Moving towards friendly factory.");
//            return true;
//        }
//
//        return false;

//        long minDist = Long.MAX_VALUE;
//        MapLocation best = new MapLocation(Planet.Earth, -1, -1);
//        HashMap<MapLocation, Boolean> bestListOfDest = new HashMap<MapLocation, Boolean>();
//        for (HashMap<MapLocation, Boolean> listOfDest : Player.workerDestinations.values()) {
//            for (MapLocation dest : listOfDest.keySet()) {
//                long dist =  dest.distanceSquaredTo(worker.location().mapLocation());
//                if (dist < minDist && listOfDest.get(dest)) {
//                    minDist = dist;
//                    best = dest;
//                    bestListOfDest = listOfDest;
//                }
//            }
//        }
//        if (gc.startingMap(Planet.Earth).onMap(best)) {
//            // if nothing found, default location is off the map
//            Pathing.move(worker, best);
//            bestListOfDest.put(best, false);
//            return true;
//        }
//        return false;
    }

    private static boolean moveTowardsKarbonite() {
    	System.out.print("ashbdfw");
        MapLocation loc = bestKarboniteLoc();
        if (loc != worker.location().mapLocation()) { // bestKarboniteLoc returns the worker's position if nothing is found
            Pathing.move(worker, loc);
            return true;
        }
        
        return false;
    }

    private static boolean moveOutOfWay() {
        int num_free_cells = 0;
        for (Direction d : Direction.values()) {
            MapLocation loc = worker.location().mapLocation().add(d);
            if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                num_free_cells++;
            }
        }
        if (num_free_cells < Config.FREE_CELL_CONSTANT) {
            int random = (int) (Math.random() * 8);
            Pathing.tryMove(worker, Direction.values()[random]);
            return true;
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

    private static void replicate() {
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