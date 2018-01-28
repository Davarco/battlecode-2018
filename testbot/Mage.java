import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Mage {

    private static Unit mage;
    private static GameController gc;
    private static VecUnit enemies;
    private static VecUnit friendly;
    private static HashMap<Integer, Direction> directionMap;
    private static HashMap<Integer, Integer> counterMap;

    public static void init(GameController controller) {
        gc = controller;
        directionMap = new HashMap<>();
        counterMap = new HashMap<>();
    }
    public static void runMars(Unit unit){
    	mage = unit;
        if (mage.location().isInGarrison()) return;
    	if (!attack()) {
            moveMars();
            attack();
        } else {
            moveMars();
        }
    	return;
    }
    public static void runEarth(Unit unit) {

        // Receive mage from main runner
        mage = unit;
        if (mage.location().isInGarrison()) return;

        /*
        Scenario 1: Attack first and then run away to get out of enemy range
        Scenario 2: Move first to get into range and then attack
         */
        if(mage.health()<80){
        	moveTowardsFactory();
        }
        if(Player.mapsize.equals("largemap")){
	        if (!attack()) {
	            move();
	            attack();
	        } else {
	            move();
	        }
        }
        else{
        	if (!attack()) {
	            moveSmall();
	            attack();
	        } else {
	            moveSmall();
	        }
        }
    }
 private static void moveSmall() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(mage.id())) {
            return;
        }
        if (mage.location().isOnPlanet(Planet.Earth) && gc.round() >=Config.ROCKET_CREATION_ROUND ) {
            if (moveTowardsRocketSmall()) {
                return;
            }
        }
        
        // Avoid enemy units, walk outside of their view range
        enemies = gc.senseNearbyUnitsByTeam(mage.location().mapLocation(), mage.visionRange(), Util.enemyTeam());
        friendly = gc.senseNearbyUnitsByTeam(mage.location().mapLocation(), mage.visionRange(), Util.enemyTeam());
        if(enemies.size()>=friendly.size() && gc.round()<=300){
        	//if (Pathing.escape(mage)) {
        		//return;
        	//}
        }
        if (Player.focalPoint != null) {
            if (Player.focalPoint.isWithinRange(mage.visionRange(), mage.location().mapLocation()) && gc.canSenseLocation(Player.focalPoint) &&
                    !gc.hasUnitAtLocation(Player.focalPoint)) {
                // System.out.println("Works");
                Player.focalPoint = null;
            }
        }

        // Get closest enemy
        
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            long dist = mage.location().mapLocation().distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        // Set new focal point
        if (Player.focalPoint == null) {
            if (idx != -1) {
                Player.focalPoint = enemies.get(idx).location().mapLocation();
            }
        }

        // Move towards focal point
        if (Player.focalPoint != null) {
        		Pathing.move(mage, Player.focalPoint);	
        }

        

        // Unit will bounce in order to escape factories
        bounce();
        
     // Move towards rockets mid-game, and escape factories early on
        

        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(mage, FocusPoints.GeographicFocusPointsE.get(0));
    }
 private static boolean moveTowardsRocketSmall() {
 	if(mage.location().mapLocation().getPlanet()==Planet.Mars)return false;
     // Move towards a low-HP rocket if possible
     VecUnit rockets = gc.senseNearbyUnitsByType(mage.location().mapLocation(), mage.visionRange(), UnitType.Rocket);
     long minDist = Long.MAX_VALUE;
     int idx = -1;
     for (int i = 0; i < rockets.size(); i++) {
         long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(mage.location().mapLocation());
         if (Util.friendlyUnit(rockets.get(i)) && dist < minDist && rockets.get(i).structureIsBuilt()==1) {
             minDist = dist;
             idx = i;
         }
     }
     if(minDist>32)return false;
     if (idx != -1) {
         PlanetMap map = gc.startingMap(mage.location().mapLocation().getPlanet());
         MapLocation tmp = rockets.get(idx).location().mapLocation();
         int initx = tmp.getX();
         int inity = tmp.getY();
         tmp = new MapLocation(Planet.Earth, initx + 1, inity);
         if (map.onMap(tmp)) {
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         tmp = new MapLocation(Planet.Earth, initx - 1, inity);
         if (map.onMap(tmp)){
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
         if (map.onMap(tmp)) {
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
         if (map.onMap(tmp)){
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
         if (map.onMap(tmp)) {
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
         if (map.onMap(tmp)) {
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         tmp = new MapLocation(Planet.Earth, initx, inity - 1);
         if (map.onMap(tmp)) {
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         tmp = new MapLocation(Planet.Earth, initx, inity + 1);
         if (map.onMap(tmp)) {
         	if(!Pathing.move(mage, tmp)){
         		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
         	}
         	return true;
         }
         // System.out.println("Moving towards friendly rocket.");
         return true;
     }

     return false;
 }

    private static boolean attack() {

        // Return true if attack isn't ready
        if (!gc.isAttackReady(mage.id()))
            return true;

        // Get enemy units
        enemies = gc.senseNearbyUnitsByTeam(mage.location().mapLocation(), mage.attackRange(), Util.enemyTeam());
        if (enemies.size() == 0)
            return false;

        // Attack lowest HP target
        long minHP = Long.MAX_VALUE;
        
        int idx = 0;
        int maxsum = 0;
        for (int i = 0; i < enemies.size(); i++) {
        	int t = sum(enemies.get(i));
        		if(t>maxsum && gc.canAttack(mage.id(), enemies.get(idx).id())){
        			maxsum = t;
        			idx = i;
       		}
       	}
        
        if (idx!= -1 && gc.canAttack(mage.id(), enemies.get(idx).id())) {
            gc.attack(mage.id(), enemies.get(idx).id());
            return true;
        }
        return false;

    }
    
    private static int sum(Unit u){
    	int sm = 0;
    	MapLocation ml = u.location().mapLocation();
    	for(int x = -1; x<=1; x++){
    		for(int y = -1; y<=1; y++){
    			MapLocation temp = new MapLocation(ml.getPlanet(), ml.getX()+x, ml.getY()+y);
    			if(gc.hasUnitAtLocation(temp)){
    				Unit k = gc.senseUnitAtLocation(temp);
    				if(u.team().equals(Util.friendlyTeam())){
    					sm-=value(k.unitType());
    				}
    				else{
    					sm+=value(k.unitType());
    				}
    			}
    		}
    	}
    	return sm;
    }

    private static int value(UnitType ut){
    	if(ut.equals(UnitType.Ranger)){
    		return 3;
    	}
    	if(ut.equals(UnitType.Worker)){
    		return 1;
    	}
    	if(ut.equals(UnitType.Mage)){
    		return 4;
    	}
    	if(ut.equals(UnitType.Healer)){
    		return 3;
    	}
    	if(ut.equals(UnitType.Knight)){
    		return 2;
    	}	
    	if(ut.equals(UnitType.Factory)){
    		return 3;
    	}	
    	return 1;
    }
    
    private static void move() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(mage.id())) {
            return;
        }
        if (mage.location().isOnPlanet(Planet.Earth) && gc.round() >=Config.ROCKET_CREATION_ROUND ) {
            if (moveTowardsRocket()) {
                return;
            }
        }
        
        // Avoid enemy units, walk outside of their view range
        enemies = gc.senseNearbyUnitsByTeam(mage.location().mapLocation(), mage.visionRange(), Util.enemyTeam());
        friendly = gc.senseNearbyUnitsByTeam(mage.location().mapLocation(), mage.visionRange(), Util.enemyTeam());
        if(enemies.size()>=friendly.size()){
        	if (Pathing.escape(mage)) {
        		return;
        	}
        }

        // Move towards initial enemy worker locations
        /*
        if (gc.round() < Config.mage_AUTO_ATTACK_ROUND && Info.number(UnitType.mage) >= 8 && mage.location().isOnPlanet(Planet.Earth))
            if (moveTowardsInitPoint())
                return;
        */

        // Remove focal point if no units exist there
        if(gc.round()<=600 && gc.planet()== Planet.Earth){
	        if (Player.focalPoint != null) {
	            if (Player.focalPoint.isWithinRange(mage.visionRange(), mage.location().mapLocation()) && gc.canSenseLocation(Player.focalPoint) &&
	                    !gc.hasUnitAtLocation(Player.focalPoint)) {
	                // System.out.println("Works");
	                Player.focalPoint = null;
	            }
	        }
	
	        // Get closest enemy
	        
	        long minDist = Long.MAX_VALUE;
	        int idx = -1;
	        for (int i = 0; i < enemies.size(); i++) {
	            long dist = mage.location().mapLocation().distanceSquaredTo(enemies.get(i).location().mapLocation());
	            if (dist < minDist) {
	                minDist = dist;
	                idx = i;
	            }
	        }
	
	        // Set new focal point
	        if (Player.focalPoint == null) {
	            if (idx != -1) {
	                Player.focalPoint = enemies.get(idx).location().mapLocation();
	            }
	        }
	
	        // Move towards focal point
	        if (Player.focalPoint != null) {
	        	Pathing.move(mage, Player.focalPoint);	
	        }
        }

        
        if(returnToFactory())
        	return;
        // Unit will bounce in order to escape factories
     // Move towards rockets mid-game, and escape factories early on
        

        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(mage, FocusPoints.GeographicFocusPointsE.get(0));
    }
    private static boolean returnToFactory(){
    	List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist =  mage.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if(dist<=16) return false;
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (minDist >= 1000)  return false;
        if(idx == -1)return false;
        Pathing.tryMove(mage, mage.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        return true;
    }
 private static void moveMars() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(mage.id())) {
            return;
        }

        // Avoid enemy units, walk outside of their view range
        enemies = gc.senseNearbyUnitsByTeam(mage.location().mapLocation(), mage.visionRange(), Util.enemyTeam());
        friendly = gc.senseNearbyUnitsByTeam(mage.location().mapLocation(), mage.visionRange(), Util.enemyTeam());
        if(enemies.size()>=friendly.size()){
        	if (Pathing.escape(mage)) {
        		return;
        	}
        }

        // Move towards initial enemy worker locations
        /*
        if (gc.round() < Config.mage_AUTO_ATTACK_ROUND && Info.number(UnitType.mage) >= 8 && mage.location().isOnPlanet(Planet.Earth))
            if (moveTowardsInitPoint())
                return;
        */

        // Remove focal point if no units exist there
        if (Player.focalPointMars != null) {
            if (Player.focalPointMars.isWithinRange(mage.visionRange(), mage.location().mapLocation()) && gc.canSenseLocation(Player.focalPointMars) &&
                    !gc.hasUnitAtLocation(Player.focalPointMars)) {
                // System.out.println("Works");
                Player.focalPoint = null;
            }
        }

        // Get closest enemy
        
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            long dist = mage.location().mapLocation().distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
       
        // Set new focal point
        if (Player.focalPointMars == null) {
            if (idx != -1) {
                Player.focalPointMars = enemies.get(idx).location().mapLocation();
            }
        }

        // Move towards focal point
        if (Player.focalPointMars != null) {
        	 System.out.println(Player.focalPointMars);
        	 System.out.println(mage.location().mapLocation());
        	 Pathing.move(mage, Player.focalPointMars);	
        }

        

        // Unit will bounce in order to escape factories
        bounce();
        
     // Move towards rockets mid-game, and escape factories early on
        if (mage.location().isOnPlanet(Planet.Earth) && gc.round() >=Config.ROCKET_CREATION_ROUND && mage.location().mapLocation().getPlanet()==Planet.Mars) {
            if (moveTowardsRocket()) {
                return;
            }
        }
        

        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(mage, FocusPoints.GeographicFocusPointsE.get(0));
    }

    private static boolean bounce() {

        // Reset if counter is 8
        counterMap.putIfAbsent(mage.id(), 0);
        if (counterMap.get(mage.id()) >= 8) {
            counterMap.put(mage.id(), 0);

            // Find possible movement directions
            List<Direction> dirList = new ArrayList<>();
            for (Direction d : Direction.values()) {
                MapLocation loc = mage.location().mapLocation().add(d);
                if (gc.startingMap(mage.location().mapLocation().getPlanet()).onMap(loc) && (gc.startingMap(mage.location().mapLocation().getPlanet()).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                    dirList.add(d);
                }
            }

            // Get one of the possible directions if they exist
            if (dirList.size() != 0) {
                int idx = (int) (Math.random() * dirList.size());

                // Set the current direction
                directionMap.put(mage.id(), dirList.get(idx));
            }
        }

        // Try to move in the current direction
        Direction dir = directionMap.get(mage.id());
        if (dir != null) {
            if (Pathing.tryMove(mage, dir))
                counterMap.put(mage.id(), counterMap.get(mage.id())+1);
            else
                counterMap.put(mage.id(), 0);
        } else {
            // Reset the direction
            counterMap.put(mage.id(), 8);
        }

        return false;
    }
    private static boolean ditchFactory() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
            long dist = 50 - mage.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0)  return false;

        Direction opposite = Pathing.opposite(mage.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(mage, opposite);
        return true;
    }
    private static boolean ditchRocket() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Rocket);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
        	if(units.get(i).structureIsBuilt()==1)continue;
            long dist = 50 - mage.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0)  return false;

        Direction opposite = Pathing.opposite(mage.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(mage, opposite);
        return true;
    }

    private static boolean moveTowardsRocket() {
    	if(mage.location().mapLocation().getPlanet()==Planet.Mars)return false;
        // Move towards a low-HP rocket if possible
        VecUnit rockets = gc.senseNearbyUnitsByType(mage.location().mapLocation(), mage.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(mage.location().mapLocation());
            if (Util.friendlyUnit(rockets.get(i)) && dist < minDist && rockets.get(i).structureIsBuilt()==1) {
                minDist = dist;
                idx = i;
            }
        }
        if(minDist>10 && gc.round()<=600)return false;
        if (idx != -1) {
            PlanetMap map = gc.startingMap(mage.location().mapLocation().getPlanet());
            MapLocation tmp = rockets.get(idx).location().mapLocation();
            int initx = tmp.getX();
            int inity = tmp.getY();
            tmp = new MapLocation(Planet.Earth, initx + 1, inity);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity);
            if (map.onMap(tmp)){
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
            if (map.onMap(tmp)){
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity - 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(mage, tmp)){
            		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
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
            long dist = mage.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
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
            //Pathing.move(mage, units.get(idx).location().mapLocation());
            	PlanetMap map = gc.startingMap(mage.location().mapLocation().getPlanet());
                MapLocation tmp = units.get(idx).location().mapLocation();
                int initx = tmp.getX();
                int inity = tmp.getY();
                tmp = new MapLocation(Planet.Earth, initx + 1, inity);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity);
                if (map.onMap(tmp)){
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
                if (map.onMap(tmp)){
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx, inity - 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
                tmp = new MapLocation(Planet.Earth, initx, inity + 1);
                if (map.onMap(tmp)) {
                	if(!Pathing.move(mage, tmp)){
                		Pathing.tryMove(mage, mage.location().mapLocation().directionTo(tmp));
                	}
                	return true;
                }
        }
        return false;
    }

}
