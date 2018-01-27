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
	private static int W, H;
	private static final int min_karbonite = 100;
	private static int workerId;
	private static MapLocation workerLoc;
	private static long marsKarbonite = 0;
	private static long marsKarbonitei = 0;
	private static int[][] floodfillEarth;
	public static PlanetMap earthmap;
	public static int amountskipped = 0;
	public static boolean noMoreKarbonite = false;
	public static HashMap<Integer, Boolean> skipIndex = new HashMap<>();
	private static ArrayList<Integer> earthkarboindex = new ArrayList<Integer>();
	private static ArrayList<Integer> marskarboindex = new ArrayList<Integer>();
	public static HashMap<Integer, Boolean> builders = new HashMap<>();
	public static HashMap<Integer, Boolean> originalworker = new HashMap<>();
	public static boolean initialworkers = false;
	public static boolean stopcollecting = false;
	public static boolean initialreached = false;
	public static int maxworkers = 0;


	public static void init(GameController controller) {
		gc = controller;
		directionMap = new HashMap<>();
		counterMap = new HashMap<>();
		if (gc.planet().equals(Planet.Earth)) {
			for (int x = 0; x < Mars.karboniteplacesEarth.size(); x++) {
				earthkarboindex.add(0);
			}
		} else {
			for (int x = 0; x < Mars.karboniteplacesMars.size(); x++) {
				marskarboindex.add(0);
			}
		}
		earthmap = gc.startingMap(Planet.Earth);
	}


    public static void runEarth(Unit unit) {
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
        if(Info.number(UnitType.Worker)<=maxworkers-2 && initialreached){
        	stopcollecting = true;
        }
        /*if(Player.mapsize.equals("smallmap")){
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
        	
        }*/
        //long t1 = System.currentTimeMillis();
        updateKarboniteIndexEarth();
        updateKarboniteIndexMars();
        if(gc.round()<=15){
        	moveTowardsKarbonite();
        	replicate();
        	return;
        }
        
        if(gc.karbonite()>=200 ){
        	Player.initialKarbReached=true;
        }
        if(!Player.initialKarbReached){
        	System.out.println("adfqadavshdhua " + gc.round() );
        	harvestKarbonite();
        	if(moveTowardsKarbonite()){
        		return;
        	}
        	bounce();
        	return;
        }
        harvestKarbonite();
        
        //MAKE SURE THIS IS RUN!!!!!!!!!!!!!!!
        if(Info.number(UnitType.Factory)*20>3*Info.number(UnitType.Worker)){
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
    public static void runMars(Unit unit){
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
        	if (moveTowardsKarbonite()){	
                return;
            }
            if (escape())
                return;
            
        } 
        else {
            if (escape()){
                return;
            }
    		if(worker.health()<=20){
    			moveTowardsFactory();
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
    	if(gc.canMove(worker.id(),Direction.values()[a])){
    		gc.moveRobot(worker.id(),Direction.values()[a]);
    	}
    }
    private static void build() {
    	
        // Create factories
    	
    	int FactoryNumber=Info.number(UnitType.Factory);
    	if(Player.mapsize.equals("largemap")){
	    	if (gc.round() > Config.ROCKET_CREATION_ROUND && (Info.number(UnitType.Rocket)<=(Info.number(UnitType.Ranger)+Info.number(UnitType.Healer)-Info.number(UnitType.Factory)*5)/4|| Player.launchCounter==0)) {
	    		VecUnit rthings = gc.senseNearbyUnitsByType(workerLoc, 16, UnitType.Ranger);
	    		VecUnit rthings1 = gc.senseNearbyUnitsByType(workerLoc, 16, UnitType.Rocket);
	    		VecUnit things = gc.senseNearbyUnitsByType(workerLoc,16, UnitType.Factory);
	    		if(rthings.size()>0 &&  rthings1.size()<2 && things.size()>0){
		            create(UnitType.Rocket);
	    		}
	        }
    	}
    	else{
    		if (gc.round() > 50) {
    			VecUnit things = gc.senseNearbyUnitsByType(workerLoc, 32, UnitType.Factory);
	    		VecUnit rthings = gc.senseNearbyUnitsByType(workerLoc, 12, UnitType.Ranger);
	    		VecUnit rthings1 = gc.senseNearbyUnitsByType(workerLoc, 16, UnitType.Rocket);
	    		if(rthings.size()>0 &&  rthings1.size()<2&& things.size()>0){
	    			
		            create(UnitType.Rocket);
	    		}
	        }
    	}
    	if(Player.mapsize.equals("largemap")){
    		VecUnit things = gc.senseNearbyUnitsByType(workerLoc, 32, UnitType.Factory);
    		VecUnit enemies = gc.senseNearbyUnitsByTeam(workerLoc, worker.visionRange(), Util.enemyTeam());
	    	if (gc.karbonite()-200>=20*Info.number(UnitType.Factory)&& Info.number(UnitType.Factory)<=2) {
		        if(things.size()==0 && enemies.size()==0)
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

    private static boolean escape() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
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

    private static boolean moveTowardsFactory() {

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
    private static void updateKarboniteIndexEarth() {
		if (gc.planet().equals(Planet.Mars)) {
			return;
		}
		int idx = Mars.earthplaces[worker.location().mapLocation().getX()][worker.location().mapLocation().getY()];
		while (earthkarboindex.get(idx) < Mars.karboniteplacesEarth.get(idx).size()
				&& gc.canSenseLocation(Mars.karboniteplacesEarth.get(idx).get(earthkarboindex.get(idx)))
				&& gc.karboniteAt(Mars.karboniteplacesEarth.get(idx).get(earthkarboindex.get(idx))) == 0) {
			earthkarboindex.set(idx, earthkarboindex.get(idx) + 1);
		}
	}

	private static void updateKarboniteIndexMars() {
		if (gc.planet().equals(Planet.Earth)) {
			return;
		}
		int idx = Mars.marsplaces[worker.location().mapLocation().getX()][worker.location().mapLocation().getY()];
		while (marskarboindex.get(idx) < Mars.karboniteplacesEarth.get(idx).size()
				&& gc.canSenseLocation(Mars.karboniteplacesEarth.get(idx).get(marskarboindex.get(idx)))
				&& gc.karboniteAt(Mars.karboniteplacesEarth.get(idx).get(marskarboindex.get(idx))) == 0) {
			marskarboindex.set(idx, marskarboindex.get(idx) + 1);
		}
	}

    
    private static boolean moveTowardsKarboniteFar() {
		if (gc.planet().equals(Planet.Earth)) {
			int idx = Mars.earthplaces[worker.location().mapLocation().getX()][worker.location().mapLocation().getY()];
			if (Mars.karboniteplacesEarth.get(idx).size() == 0) {
				if(skipIndex.get(idx) == false){
					skipIndex.put(idx, true);
					amountskipped++;
					if(amountskipped == Mars.karboniteplacesEarth.size()){
						noMoreKarbonite = true;
					}
				}
				return false;
			}
			if (earthkarboindex.get(idx) >= Mars.karboniteplacesEarth.get(idx).size()) {
				if(skipIndex.get(idx) == false){
					skipIndex.put(idx, true);
					amountskipped++;
					if(amountskipped == Mars.karboniteplacesEarth.size()){
						noMoreKarbonite = true;
					}
				}
				return false;
			}
			//if(Pathing.move(worker, Mars.karboniteplacesEarth.get(idx).get(earthkarboindex.get(idx)))==false){
				Pathing.tryMove(worker, worker.location().mapLocation()
						.directionTo(Mars.karboniteplacesEarth.get(idx).get(earthkarboindex.get(idx))));
        	//}
			
			return true;
		} else {
			int idx = Mars.marsplaces[worker.location().mapLocation().getX()][worker.location().mapLocation().getY()];
			if (Mars.karboniteplacesMars.get(idx).size() == 0) {
				if(skipIndex.get(idx) == false){
					skipIndex.put(idx, true);
					amountskipped++;
					if(amountskipped == Mars.karboniteplacesMars.size()){
						noMoreKarbonite = true;
					}
				};
				return false;
			}
			if (marskarboindex.get(idx) >= Mars.karboniteplacesMars.get(idx).size()) {
				if(skipIndex.get(idx) == false){
					skipIndex.put(idx, true);
					amountskipped++;
				}
				if(amountskipped == Mars.karboniteplacesMars.size()){
					noMoreKarbonite = true;
				}
				return false;
			}
			//if(Pathing.move(worker, Mars.karboniteplacesMars.get(idx).get(marskarboindex.get(idx)))==false){
				Pathing.tryMove(worker, worker.location().mapLocation()
						.directionTo(Mars.karboniteplacesMars.get(idx).get(marskarboindex.get(idx))));
        	//}
			return true;
		}
	}

    private static boolean moveTowardsKarbonite() {
    	/*MapLocation temp = ranger.location().mapLocation();
    	for(int x = temp.getX-2; x<temp..getX()-2; x++){
    		for(int y = temp.getY()+2; y<ranger.location().mapLocation().getY()-2; y++){
    			MapLocation t1 = new MapLocation(temp.getPlanet(), temp.getX(), temp.getY())
        		if(gc.karboniteAt()!=0){
            		Pathing.tryMove(worker,worker.location().mapLocation().directionTo(bestKarb));
        		}
        	}
    	}*/
    	if(stopcollecting == true){
        	return moveTowardsFactory();
    	}
    	MapLocation bestKarb;
    	if(gc.planet()==Planet.Earth)bestKarb = bestKarboniteLoc();
    	else{
    		bestKarb = bestKarboniteLocMars();
    	}
        if (bestKarb != null) { // bestKarboniteLoc returns the worker's position if nothing is found
        	if(Pathing.move(worker, bestKarb)==false){
        		Pathing.tryMove(worker,worker.location().mapLocation().directionTo(bestKarb));
        	}
            return true;
        }
        if(gc.round()<200){
        	return moveTowardsKarboniteFar();
        }
        return false;
    }
    private static boolean ditchFactory() {
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

    private static boolean bounce() {

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
    private static boolean returnToFactory(){
    	List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist =  worker.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if(dist<=50) return false;
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

    private static void repairStructure(UnitType StructureToRepair) {
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

    private static void harvestKarbonite() {
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

    private static MapLocation bestKarboniteLoc() {
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
    private static MapLocation bestKarboniteLocMars() {
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
    private static void replicate(){
    	if(gc.planet().equals(Planet.Earth) && Info.workerCount>10){
        	initialreached = true;
        	maxworkers = Info.workerCount;
			return;
		}
    	int num = (int) (Math.random() * Direction.values().length);
    	for (int i = num; i < Direction.values().length+num; i++) {
    		int tmp = i % Direction.values().length;
         	Direction dir = Direction.values()[tmp];
            if (gc.canReplicate(worker.id(), dir)) {
                gc.replicate(worker.id(), dir);
            }
        }
    }
    private static void replicateMars(){
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
    private static void updateWorkerStats() {

        workerId = worker.id();
        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;
        workerLoc = worker.location().mapLocation();
    }
}
