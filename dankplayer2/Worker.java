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
    private static long marsKarbonite=0;
    private static long marsKarbonitei=0;

    public static void init(GameController controller) throws Exception {
        gc = controller;
        directionMap = new HashMap<>();
        counterMap = new HashMap<>();
    }

    public static void runEarth(Unit unit) throws Exception {
    	//ArrayList<MapLocation> tmp = Pathing.path(worker, new MapLocation(Planet.Earth,6,12));
    	//System.out.println();
    	//System.out.println();
    	//for(int x = 0; x < tmp.size(); x++){
    	//	System.out.print(tmp.get(x) + "-> ");
    	//}
    	//System.out.println();
    	//System.out.println();
    	
    	
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
        	if(Info.number(UnitType.Factory)*3+Info.number(UnitType.Rocket)*2>Info.number(UnitType.Worker)){
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
        	moveTowardsKarbonite();
        	return;
        }
        harvestKarbonite();
        
        //MAKE SURE THIS IS RUN!!!!!!!!!!!!!!!
        if(Info.number(UnitType.Factory)*20>3*Info.number(UnitType.Worker) && gc.round()<=600){
            replicate();
        }
        
        // Build things that we need to
        build();

        // General move function, handles priorities
        
        move();
        updateWorkerStats();
        
        // Repair structures that we can
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        repairStructure(UnitType.Factory);
        repairStructure(UnitType.Rocket);
        
        // Harvest karbonite if we can
    }
    public static void runMars(Unit unit) throws Exception {
    	worker = unit;
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        if(Info.number(UnitType.Worker)<10 || gc.round()>=750){
        	replicateMars();
        }
        workerLoc = worker.location().mapLocation();
        workerId = worker.id();
        marsKarbonitei = gc.karbonite();
        harvestKarbonite();
        move();
        updateWorkerStats();
        
    }

    private static void move() throws Exception {
    	
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
        	if (moveTowardsKarbonite()){	
                return;
            }
        	if (moveTowardsFactory())
        		return;
            if (escape())
                return;
            
        } 
        else {
            if (escape()){
                return;
            }
            if (moveTowardsKarbonite()){	
                return;
            }
            if(worker.location().mapLocation().getPlanet()==Planet.Earth){
            	if(Player.mapsize.equals("largemap")){
            		if (gc.round() > Config.ROCKET_CREATION_ROUND) {
            			if (moveTowardsRocket())
            				return;
            		}
            	}
            	else{
            		if (gc.round() > 50) {
            			if (moveTowardsRocket())
            				return;
            		}
            	}
            	if (moveTowardsFactory()){
            		return;
            	}
            }
        }
        
       
        
        // Otherwise bounce and give more space to the factories
       if(worker.location().mapLocation().getPlanet()==Planet.Earth){
	       if(ditchFactory())
	    	   return;
       }
       if(returnToFactory()){
    	   return;
       }
       if(bounce()){
	   	   return;
       }
    }
    private static void harvestEarly() throws Exception {
        for (int i = 0; i < Direction.values().length; i++) {
        	Direction dir = Direction.values()[i];
        	if(gc.canHarvest(workerId,dir)){
        		gc.harvest(workerId, dir);
        		break;
        	}	
        }
    }
    private static void moveRandom() throws Exception {
    	int a  = (int)(Math.random()*Direction.values().length);
    	if(gc.canMove(worker.id(),Direction.values()[a])){
    		gc.moveRobot(worker.id(),Direction.values()[a]);
    	}
    }

    private static void build() throws Exception {
    	
        // Create factories
    	VecUnit things = gc.senseNearbyUnitsByType(workerLoc, 64, UnitType.Factory);
    	
    	int FactoryNumber=Info.number(UnitType.Factory);
    	if(Player.mapsize.equals("largemap")){
	    	if (gc.round() > Config.ROCKET_CREATION_ROUND) {
	    		VecUnit rthings = gc.senseNearbyUnitsByType(workerLoc, 12, UnitType.Ranger);
	    		if((things.size()>0|| gc.round()>600) && rthings.size()>0){
		            create(UnitType.Rocket);
	    		}
	        }
    	}
    	else{
    		if (gc.round() > 50) {
	    		VecUnit rthings = gc.senseNearbyUnitsByType(workerLoc, 12, UnitType.Ranger);
	    		if((things.size()>0|| gc.round()>600) && rthings.size()>0){
		            create(UnitType.Rocket);
	    		}
	        }
    	}
    	if(Player.mapsize.equals("largemap")){
	    	if (gc.karbonite()>10*(FactoryNumber) && Info.number(UnitType.Factory)<=5) {
		        if(things.size()==0)
		        	create(UnitType.Factory);
		    }
    	}
    	else{
    		if (gc.karbonite()>10*(FactoryNumber) && Info.number(UnitType.Factory)<=5) {
    	        	create(UnitType.Factory);
    	    }
    	}
	    return;
    }

    private static boolean escape() throws Exception {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            Logging.debug("Worker " + worker.location().mapLocation() + " is being attacked!");
            return true;
        }

        return false;
    }

    private static boolean moveTowardsRocket() throws Exception {
    	if(workerLoc.getPlanet()==Planet.Mars)return false;
        // Move towards a low-HP rocket if possible
        rockets = gc.senseNearbyUnitsByType(workerLoc, worker.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(workerLoc);
            if (Util.friendlyUnit(rockets.get(i)) && dist < minDist && (rockets.get(i).structureIsBuilt()==0|| Player.launchCounter<1)) {
                minDist = dist;
                idx = i;
            }
        }
        if(minDist > 20) return false;
        if (idx != -1) {
            PlanetMap map = gc.startingMap(workerLoc.getPlanet());
            MapLocation tmp = rockets.get(idx).location().mapLocation();
            int initx = tmp.getX();
            int inity = tmp.getY();
            tmp = new MapLocation(Planet.Earth, initx + 1, inity);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity);
            if (map.onMap(tmp)){
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
            if (map.onMap(tmp)){
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity - 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(worker, tmp)){
            		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            // System.out.println("Moving towards friendly rocket.");
            return true;
        }

        return false;
    }

    private static boolean moveTowardsFactory() throws Exception {

        // Move towards the closest factory
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist = worker.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist < minDist && (units.get(i).structureIsBuilt()==0||gc.round()>=600)) {
                minDist = dist;
                idx = i;
            }
        }
        if(minDist == 2){
        	return true;
        }
        //if(!Player.mapsize.equals("smallmap")){
	        if(minDist > 16 || gc.round()>=600  ){
	        	return false;
	        }
        //}
        if (idx != -1) {
        	//MAYBE add bug pathing
            //Pathing.move(worker, units.get(idx).location().mapLocation());
            	PlanetMap map = gc.startingMap(worker.location().mapLocation().getPlanet());
                MapLocation tmp = units.get(idx).location().mapLocation();
                int initx = tmp.getX();
                int inity = tmp.getY();
                tmp = new MapLocation(Planet.Earth, initx + 1, inity);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity);
                if (map.onMap(tmp)){
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
                if (map.onMap(tmp)){
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx, inity - 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx, inity + 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(worker, tmp)){
                		Pathing.tryMove(worker, worker.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
        }

        return false;
    }

    private static boolean moveTowardsKarbonite() throws Exception {
    	MapLocation bestKarb;
    	if(gc.planet()==Planet.Earth)bestKarb = bestKarboniteLoc();
    	else{
    		bestKarb = bestKarboniteLocMars();
    	}
        if (bestKarb != null) { // bestKarboniteLoc returns the worker's position if nothing is found
        	Logging.debug(bestKarb.toString());
        	if(!Pathing.move(worker, bestKarb)){
        		Pathing.tryMove(worker,worker.location().mapLocation().directionTo(bestKarb));
        	}
            return true;
        }
        
        return false;
    }
    private static boolean ditchFactory() throws Exception {
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
            long dist = 16 - worker.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
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

    private static boolean bounce() throws Exception {

        // Reset if counter is 8
        counterMap.putIfAbsent(worker.id(), 0);
        if (counterMap.get(worker.id()) >= 8) {
            counterMap.put(worker.id(), 0);

            // Find possible movement directions
            List<Direction> dirList = new ArrayList<>();
            for (Direction d : Direction.values()) {
                MapLocation loc = worker.location().mapLocation().add(d);
                if (gc.startingMap(worker.location().mapLocation().getPlanet()).onMap(loc) && (gc.startingMap(worker.location().mapLocation().getPlanet()).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
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
        	VecUnit things = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), UnitType.Factory);
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
    private static boolean returnToFactory() throws Exception {
    	List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist =  worker.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if(dist<=100) return false;
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (minDist >= 1000)  return false;
        if(idx == -1)return false;
        Pathing.tryMove(worker, worker.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        return true;
    }

    private static void repairStructure(UnitType StructureToRepair) throws Exception {
        VecUnit structures = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), StructureToRepair);

        // Repair a Structure in range, only in Earth
        if (worker.location().mapLocation().getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < structures.size(); i++) {
                if (gc.canBuild(worker.id(), structures.get(i).id()) && structures.get(i).structureIsBuilt()==0) {
                    gc.build(worker.id(), structures.get(i).id());
                }
            }
        }
    }

    private static void harvestKarbonite() throws Exception {
        for (Direction d : Direction.values()) {
            if (gc.startingMap(worker.location().mapLocation().getPlanet()).onMap(worker.location().mapLocation().add(d)) && gc.canHarvest(worker.id(), d)) {
                gc.harvest(worker.id(), d);
                MapLocation ml = Pathing.DirectionToMapLocation(worker, d);
                if(worker.location().mapLocation().getPlanet().equals(Planet.Earth)) {
                		Player.karboniteMap[ml.getX()][ml.getY()] = (int)gc.karboniteAt(ml);
                }
                else {
                	Player.karboniteMapMars[ml.getX()][ml.getY()] = (int)gc.karboniteAt(ml);
                }
            }
        }
    }

    private static MapLocation bestKarboniteLoc() throws Exception {
    	int x = workerLoc.getX();
    	int y = workerLoc.getY();
    	int bestHeuristicSoFar = Integer.MAX_VALUE;
    	int bestISoFar = -1;
    	int bestJSoFar = -1;
        for (int i = x - 4; i < x + 4; i++) {
            for (int j = y - 4; j < y + 4; j++) {
                if (i >= 0 && i < Player.earthWidth && j >= 0 && j < Player.earthHeight && Player.karboniteMap[i][j] != 0) {
                	int heuristic = (int) (Math.pow((x-i), 2) + Math.pow((y-j), 2));
                    if (heuristic < bestHeuristicSoFar) {
                        bestHeuristicSoFar = heuristic;
                        bestISoFar = i;
                        bestJSoFar = j;
                    }
                }
            }
        }
        if (bestISoFar != -1) {
        	return new MapLocation(Planet.Earth, bestISoFar, bestJSoFar);
        } else {
            return null;
        }
    }
    private static MapLocation bestKarboniteLocMars() throws Exception {
    	int x = workerLoc.getX();
    	int y = workerLoc.getY();
    	int bestHeuristicSoFar = Integer.MAX_VALUE;
    	int bestISoFar = -1;
    	int bestJSoFar = -1;
        for (int i = x - 4; i < x + 4; i++) {
            for (int j = y - 4; j < y + 4; j++) {
                if (i >= 0 && i < Player.marsWidth && j >= 0 && j < Player.marsHeight && Player.karboniteMapMars[i][j] != 0) {
                	int heuristic = (int) (Math.pow((x-i), 2) + Math.pow((y-j), 2));
                    if (heuristic < bestHeuristicSoFar) {
                        bestHeuristicSoFar = heuristic;
                        bestISoFar = i;
                        bestJSoFar = j;
                    }
                }
            }
        }
        if (bestISoFar != -1) {
        	return new MapLocation(Planet.Mars, bestISoFar, bestJSoFar);
        } else {
            return null;
        }
    }
    private static void replicate() throws Exception {
    	int num = (int) (Math.random() * Direction.values().length);
    	for (int i = num; i < Direction.values().length+num; i++) {
    		int tmp = i % Direction.values().length;
         	Direction dir = Direction.values()[tmp];
            if (gc.canReplicate(worker.id(), dir)) {
                gc.replicate(worker.id(), dir);
            }
        }
    }
    private static void replicateMars() throws Exception {
    	int num = (int) (Math.random() * Direction.values().length);
    	int numreplicated = 0;
    	for (int i = num; i < Direction.values().length+num; i++) {
    		int tmp = i % Direction.values().length; 
        	Direction dir = Direction.values()[tmp];
            if (gc.canReplicate(worker.id(), dir)) {
                gc.replicate(worker.id(), dir);
                numreplicated++;
                if(Info.number(UnitType.Worker)+numreplicated>10 && gc.round()<=750)return;
            }
        }
    }

    private static void create(UnitType type) throws Exception {
    	int num = (int) (Math.random() * Direction.values().length);
        for (int i = num; i < Direction.values().length+num; i++) {
        	int tmp = i % Direction.values().length;
        	Direction dir = Direction.values()[tmp];
            if (gc.canBlueprint(worker.id(), type, dir)) {
                gc.blueprint(worker.id(), type, dir);
            }
        }
    }
    private static void updateWorkerStats() throws Exception {

        workerId = worker.id();
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        workerLoc = worker.location().mapLocation();
    }
}
