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

    public static void init(GameController controller) {
        gc = controller;
        directionMap = new HashMap<>();
        counterMap = new HashMap<>();
    }

    public static void runEarth(Unit unit) {
    	
        // Receive worker from main runner
        worker = unit;
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        if(gc.round()<=15 && !Player.mapsize.equals("smallmap")){
        	harvestEarly();
        	replicate();
        	return;
        }
        if(gc.karbonite()>=100){
        	Player.initialKarbReached=true;
        }
        if(!Player.initialKarbReached){
        	harvestEarly();
        	return;
        }
        harvestKarbonite();
        
        //MAKE SURE THIS IS RUN!!!!!!!!!!!!!!!
	    if(Info.number(UnitType.Factory)*20>Info.number(UnitType.Worker)*3){
	        replicate();
	    }
        
        // Build things that we need to
        build();

        // General move function, handles priorities
        
        move();
       

        // Repair structures that we can
        repairStructure(UnitType.Rocket);
        repairStructure(UnitType.Factory);

        // Harvest karbonite if we can
    }

    public static void runMars(Unit unit) {
        worker = unit;
        System.out.println("Worker #" + worker.id() + " is on Mars!");
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
        if (gc.round() < 19) {
        	if (moveTowardsFactory())
        		return;
            if (escape())
                return;
            
        } else {
            if (escape())
                return;
            if (moveTowardsFactory())
                return;
        }

        // Move towards karbonite after our buildings are taken care of
        if (moveTowardsKarbonite())
            return;
        
        // Otherwise bounce and give more space to the factories
        if(ditchFactory()) return;
        if(bounce())return;
        //moveRandom();

    }
    private static void harvestEarly(){
        for (int i = 0; i < Direction.values().length; i++) {
        	Direction dir = Direction.values()[i];
        	if(gc.canHarvest(worker.id(),dir)){
        		gc.harvest(worker.id(), dir);
        		break;
        	}	
        }
    }

    private static void build() {
    	
        // Create factories
    	int FactoryNumber=Info.number(UnitType.Factory);
    	if(gc.round()>=80){
	        if (gc.karbonite()>20*(FactoryNumber)) {
	        	VecUnit things = gc.senseNearbyUnitsByType(worker.location().mapLocation(), 31, UnitType.Factory);
	        	if(things.size()==0)
	        		create(UnitType.Factory);
	        }
    	}
    	else{
    		if (gc.karbonite()>20*(FactoryNumber/2)) {
	        	VecUnit things = gc.senseNearbyUnitsByType(worker.location().mapLocation(), 31, UnitType.Factory);
	        	if(things.size()==0)
	        		create(UnitType.Factory);
	        }
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

    private static boolean moveTowardsKarbonite() {
        MapLocation loc = bestKarboniteLoc();
        if (loc != null) { // bestKarboniteLoc returns the worker's position if nothing is found
            return Pathing.move(worker, loc);
        }
        
        return false;
    }
    private static boolean ditchFactory() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
            long dist = 32 - worker.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
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

    private static boolean bounce() {

        // Reset if counter is 8
        counterMap.putIfAbsent(worker.id(), 0);
        if (counterMap.get(worker.id()) >= 8) {
            counterMap.put(worker.id(), 0);

            // Find possible movement directions
            List<Direction> dirList = new ArrayList<>();
            for (Direction d : Direction.values()) {
                MapLocation loc = worker.location().mapLocation().add(d);
                if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                    dirList.add(d);
                }
            }

            // Get one of the possible directions if they exist
            if (dirList.size() != 0) {
                int idx = (int) (Math.random() * dirList.size());

                // Set the current direction
                directionMap.put(worker.id(), dirList.get(idx));
            }
        }

        // Try to move in the current direction
        Direction dir = directionMap.get(worker.id());
        if (dir != null) {
            if (Pathing.tryMove(worker, dir)){
                counterMap.put(worker.id(), counterMap.get(worker.id())+1);
                return true;
            }
            else{
                counterMap.put(worker.id(), 0);
                return false;
            }
        } 
        else {
            // Reset the direction
            counterMap.put(worker.id(), 8);
            
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
                int heuristic = (int) (Math.pow((int) (worker.location().mapLocation().distanceSquaredTo(loc)), 2));
                if (heuristic < bestHeuristicSoFar) {
                    bestHeuristicSoFar = heuristic;
                    bestIdxSoFar = i;
                }
            }
        }

        if (bestIdxSoFar != -1) {
            return allInRange.get(bestIdxSoFar);
        } else {
            return null;
        }
    }
    private static void replicate(){
    	int num = (int) (Math.random() * Direction.values().length);
    	for (int i = num; i < Direction.values().length+num; i++) {
    		int tmp = i % Direction.values().length;
         	Direction dir = Direction.values()[tmp];
            if (gc.canReplicate(worker.id(), dir)) {
                gc.replicate(worker.id(), dir);
            }
        }
    }

    private static void create(UnitType type) {
    	int num = (int) (Math.random() * Direction.values().length);
        for (int i = num; i < Direction.values().length+num; i++) {
        	int tmp = i % Direction.values().length;
        	Direction dir = Direction.values()[tmp];
            if (gc.canBlueprint(worker.id(), type, dir)) {
                gc.blueprint(worker.id(), type, dir);
            }
        }
    }
}