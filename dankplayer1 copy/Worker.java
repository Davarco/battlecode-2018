import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker {

    private static Unit worker;
    private static GameController gc;
    private static VecUnit rockets;
    private static HashMap<Integer, Direction> directionMap;
    private static HashMap<Integer, Integer> counterMap;
    private static int W,H;
    private static final int min_karbonite=100;
    private static int workerId;
    private static MapLocation workerLoc;

    public static void init(GameController controller) {
        gc = controller;
        directionMap = new HashMap<>();
        counterMap = new HashMap<>();
    }

    public static void runEarth(Unit unit) {
    	
        // Receive worker from main runner
        worker = unit;
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;

        workerLoc = worker.location().mapLocation();
        workerId = worker.id();

        if(Player.mapsize.equals("smallmap")){
        	build();
        	move();
        	updateWorkerStats();
        	repairStructure(UnitType.Factory);
        	repairStructure(UnitType.Rocket);
        	harvestKarbonite();
        	if(Info.number(UnitType.Factory)*20>Info.number(UnitType.Worker)*3){
    	        replicate();
    	    }
        	return;
        	
        }
        //long t1 = System.currentTimeMillis();
        if(gc.round()<=15){
        	harvestEarly();
        	replicate();
        	return;
        }
        
        if(gc.karbonite()>=100 ){
        	Player.initialKarbReached=true;
        }
        if(!Player.initialKarbReached){
        	harvestEarly();
        	return;
        }
        harvestKarbonite();
        
        //MAKE SURE THIS IS RUN!!!!!!!!!!!!!!!
        if(Info.number(UnitType.Factory)*20>Info.number(UnitType.Worker)*3 && !(gc.round() > Config.ROCKET_CREATION_ROUND && Info.number(UnitType.Rocket)<=Info.totalUnits/6)){
            replicate();
        }
        
        // Build things that we need to
        build();

        // General move function, handles priorities
        
        move();
        updateWorkerStats();
        
        // Repair structures that we can
        repairStructure(UnitType.Rocket);
        repairStructure(UnitType.Factory);

        // Harvest karbonite if we can
    }
    public static void runMars(Unit unit){
    	
    }

    private static void move() {
    	
        /*
        TODO Implement the entire worker move function as a heuristic based on priority 
         */

        // Only move if we can move
        if (!gc.isMoveReady(workerId))
            return;
        // Similar to below, rockets are vital late game
        
        // Moving towards factories has higher priority than escape early game
        //NEEDS BETTER CONDITION
        if (gc.round() < 150) {
        	if (moveTowardsFactory())
        		return;
            if (escape())
                return;
            
        } 
        else {
            if (escape()){
                return;
            }
            if (gc.round() > Config.ROCKET_CREATION_ROUND) {
                if (moveTowardsRocket())
                    return;
            }
            if (moveTowardsFactory() ){
                return;
            }
        }
        
        
        
        // Move towards karbonite after our buildings are taken care of
        if (moveTowardsKarbonite()){	
            return;
        }
       
        
        // Otherwise bounce and give more space to the factories
       if(ditchFactory())
    	   return;
       if(bounce()){
    	   return;
       }
        

    }
    private static void harvestEarly(){
        for (int i = 0; i < Direction.values().length; i++) {
        	Direction dir = Direction.values()[i];
        	if(gc.canHarvest(workerId,dir)){
        		gc.harvest(workerId, dir);
        		break;
        	}	
        }
    }
    private static void moveRandom(){
    	int a  = (int)(Math.random()*Direction.values().length);
    	if(gc.canMove(workerId,Direction.values()[a])){
    		gc.moveRobot(workerId,Direction.values()[a]);
    	}
    }

    private static void build() {
    	
        // Create factories
    	VecUnit things = gc.senseNearbyUnitsByType(workerLoc, 16, UnitType.Factory);
    	VecUnit rthings = gc.senseNearbyUnitsByType(workerLoc, 16, UnitType.Rocket);
    	int FactoryNumber=Info.number(UnitType.Factory);
    	if (gc.round() > Config.ROCKET_CREATION_ROUND && Info.number(UnitType.Rocket)<=Info.totalUnits/6) {
    		if(things.size()==0 && rthings.size()==0){
	            create(UnitType.Rocket);
    		}
        }
	    if (gc.karbonite()>20*(FactoryNumber) && !(gc.round() > Config.ROCKET_CREATION_ROUND && Info.number(UnitType.Rocket)<=Info.totalUnits/6)) {
	        if(things.size()==0)
	        	create(UnitType.Factory);
	   }
    }

    private static boolean escape() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            System.out.println("Worker " + workerLoc + " is being attacked!");
            return true;
        }

        return false;
    }

    private static boolean moveTowardsRocket() {
    	if(workerLoc.getPlanet()==Planet.Mars)return false;
        // Move towards a low-HP rocket if possible
        rockets = gc.senseNearbyUnitsByType(workerLoc, worker.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(workerLoc);
            if (Util.friendlyUnit(rockets.get(i)) && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            PlanetMap map = gc.startingMap(workerLoc.getPlanet());
            MapLocation tmp = rockets.get(idx).location().mapLocation();
            int initx = tmp.getX();
            int inity = tmp.getY();
            tmp = new MapLocation(Planet.Earth, initx + 1, inity);
            if (map.onMap(tmp)) {
            	Pathing.move(worker, tmp);
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity);
            if (map.onMap(tmp)){
            	Pathing.move(worker, tmp);
            	return true;
            }
            tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
            if (map.onMap(tmp)) {
            	Pathing.move(worker, tmp);
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
            if (map.onMap(tmp)){
            	Pathing.move(worker, tmp);
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
            if (map.onMap(tmp)) {
            	Pathing.move(worker, tmp);
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
            if (map.onMap(tmp)) {
            	Pathing.move(worker, tmp);
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity - 1);
            if (map.onMap(tmp)) {
            	Pathing.move(worker, tmp);
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity + 1);
            if (map.onMap(tmp)) {
            	Pathing.move(worker, tmp);
            	return true;
            }
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
            long dist = workerLoc.distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist < minDist && units.get(i).structureIsBuilt()==0) {
                minDist = dist;
                idx = i;
            }
        }
        if(minDist == 2){
        	return true;
        }
        if (idx != -1) {
        	//MAYBE add bug pathing
            //Pathing.move(worker, units.get(idx).location().mapLocation());
            	PlanetMap map = gc.startingMap(workerLoc.getPlanet());
                MapLocation tmp = units.get(idx).location().mapLocation();
                int initx = tmp.getX();
                int inity = tmp.getY();
                tmp = new MapLocation(Planet.Earth, initx + 1, inity);
                if (map.onMap(tmp)) {
                	Pathing.move(worker, tmp);
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity);
                if (map.onMap(tmp)){
                	Pathing.move(worker, tmp);
                	return true;
                }
                tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
                if (map.onMap(tmp)) {
                	Pathing.move(worker, tmp);
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
                if (map.onMap(tmp)){
                	Pathing.move(worker, tmp);
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
                if (map.onMap(tmp)) {
                	Pathing.move(worker, tmp);
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
                if (map.onMap(tmp)) {
                	Pathing.move(worker, tmp);
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx, inity - 1);
                if (map.onMap(tmp)) {
                	Pathing.move(worker, tmp);
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx, inity + 1);
                if (map.onMap(tmp)) {
                	Pathing.move(worker, tmp);
                	return true;
                }
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
            long dist = 32 - workerLoc.distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0)  return false;

        Direction opposite = Pathing.opposite(workerLoc.directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(worker, opposite);
        return true;
    }

    private static boolean bounce() {

        // Reset if counter is 8
        counterMap.putIfAbsent(workerId, 0);
        if (counterMap.get(workerId) >= 8) {
            counterMap.put(workerId, 0);

            // Find possible movement directions
            List<Direction> dirList = new ArrayList<>();
            for (Direction d : Direction.values()) {
                MapLocation loc = workerLoc.add(d);
                if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                    dirList.add(d);
                }
            }

            // Get one of the possible directions if they exist
            if (dirList.size() != 0) {
                int idx = (int) (Math.random() * dirList.size());

                // Set the current direction
                directionMap.put(workerId, dirList.get(idx));
            }
        }

        // Try to move in the current direction
        Direction dir = directionMap.get(workerId);
        if (dir != null) {
            if (Pathing.tryMove(worker, dir)){
                counterMap.put(workerId, counterMap.get(workerId)+1);
                return true;
            }
            else{
                counterMap.put(workerId, 0);
                return false;
            }
        } 
        else {
            // Reset the direction
            counterMap.put(workerId, 8);
            
        }

        return false;
    }

    private static void repairStructure(UnitType StructureToRepair) {
        VecUnit structures = gc.senseNearbyUnitsByType(workerLoc, worker.visionRange(), StructureToRepair);

        // Repair a Structure in range, only in Earth
        if (workerLoc.getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < structures.size(); i++) {
                if (gc.canBuild(workerId, structures.get(i).id())) {
                    gc.build(workerId, structures.get(i).id());
                }
                if (gc.canRepair(workerId, structures.get(i).id())) {
                    gc.repair(workerId, structures.get(i).id());
                }
            }
        }
    }

    private static void harvestKarbonite() {
        for (Direction d : Direction.values()) {
            if (gc.startingMap(Planet.Earth).onMap(workerLoc.add(d)) && gc.canHarvest(workerId, d)) {
                gc.harvest(workerId, d);
            }
        }
    }

    private static MapLocation bestKarboniteLoc() {
    	long ta = System.currentTimeMillis();
    	int x = workerLoc.getX();
    	int y = workerLoc.getY();
    	int bestHeuristicSoFar = Integer.MAX_VALUE;
    	int bestISoFar = -1;
    	int bestJSoFar = -1;
        for (int i = x - 4; i < x + 4; i++) {
            for (int j = y - 4; j < y + 4; j++) {
                if (i >= 0 && i < Player.earthWidth && j >= 0 && j < Player.earthHeight && Player.karboniteMap[i][j] != 0) {
                    int trueKarb = (int) gc.karboniteAt(Player.mapLocations[i][j]);
                    if (trueKarb != Player.karboniteMap[i][j]) {
                        Player.karboniteMap[i][j] = trueKarb;
                    }
                    int heuristic = (int) Math.pow((Math.pow((x-i), 2) + Math.pow((y-j), 2)), 2);
                    if (heuristic < bestHeuristicSoFar) {
                        bestHeuristicSoFar = heuristic;
                        bestISoFar = i;
                        bestJSoFar = j;
                    }
                }
            }
        }
        long tb = System.currentTimeMillis();
        System.out.println("bestKarbLoc time: " + (tb-ta));
        if (bestISoFar != -1) {
            return Player.mapLocations[bestISoFar][bestJSoFar];
        } else {
            return null;
        }
    }
    private static void replicate(){
    	int num = (int) (Math.random() * Direction.values().length);
    	for (int i = num; i < Direction.values().length+num; i++) {
    		int tmp = i % Direction.values().length;
         	Direction dir = Direction.values()[tmp];
            if (gc.canReplicate(workerId, dir)) {
                gc.replicate(workerId, dir);
            }
        }
    }

    private static void create(UnitType type) {
    	int num = (int) (Math.random() * Direction.values().length);
        for (int i = num; i < Direction.values().length+num; i++) {
        	int tmp = i % Direction.values().length;
        	Direction dir = Direction.values()[tmp];
            if (gc.canBlueprint(workerId, type, dir)) {
                gc.blueprint(workerId, type, dir);
            }
        }
    }

    private static void updateWorkerStats() {

        worker = gc.unit(workerId);
        workerId = worker.id();
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        workerLoc = worker.location().mapLocation();
    }
}
