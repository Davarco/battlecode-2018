import java.util.List;

import bc.Direction;
import bc.GameController;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;
import bc.Unit;
import bc.UnitType;
import bc.VecUnit;

public class Healer {

    private static Unit healer;
    private static GameController gc;
    private static VecUnit friendlies;

    private static boolean isAttacked;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void runEarth(Unit unit) {

        // Receive healer from main runner
        healer = unit;

        /*
        Scenario 1: Heal first and then runEarth away to get out of enemy range
        Scenario 2: Move first to get into range and then heal
         */
        if (!heal()) {
            long t1 = System.currentTimeMillis();
            move();
            long t2 = System.currentTimeMillis();
            Player.time += (t2 - t1);
            heal();
        } else {
            long t1 = System.currentTimeMillis();
            move();
            long t2 = System.currentTimeMillis();
            Player.time += (t2 - t1);
        }
    }


    public static void runMars(Unit unit) {
        healer = unit;

        System.out.println("Healer #" + healer.id() + " is on Mars!");
    }
    
    private static boolean heal() {

        // Return true if we cannot heal
        if (!gc.isHealReady(healer.id()))
            return true;

        // Get friendly units
        friendlies = gc.senseNearbyUnitsByTeam(healer.location().mapLocation(), healer.attackRange(), Util.friendlyTeam());
        if (friendlies.size() == 0)
            return false;

        // Heal lowest HP target by difference
        long minHp = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < friendlies.size(); i++) {
            if (friendlies.get(i).maxHealth() - friendlies.get(i).health() < minHp) {
                minHp = friendlies.get(i).health();
                idx = i;
            }
        }
        if (gc.canHeal(healer.id(), friendlies.get(idx).id()) && gc.isHealReady(healer.id())) {
            gc.heal(healer.id(), friendlies.get(idx).id());
        }

        return true;
    }

    private static void move() {

        // Return if we cannot move
        if (!gc.isMoveReady(healer.id()))
            return;
        if (healer.location().isOnPlanet(Planet.Earth) && gc.round() >=Config.ROCKET_CREATION_ROUND ) {
            if (moveTowardsRocket()) {
                return;
            }
        }

        // See if unit needs to escape
        if (Pathing.escape(healer)) {
            isAttacked = true;
            // System.out.println("Healer " + healer.location().mapLocation() + " is being attacked!");
            return;
        } else {
            isAttacked = false;
        }
        // Otherwise changes towards a low HP troop
        // TODO Implement this as a heuristic
        friendlies = gc.senseNearbyUnitsByTeam(healer.location().mapLocation(), healer.visionRange(), Util.friendlyTeam());
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < friendlies.size(); i++) {
            long dist = friendlies.get(i).location().mapLocation().distanceSquaredTo(healer.location().mapLocation());
            if (friendlies.get(i).unitType()==UnitType.Ranger && Util.friendlyUnit(friendlies.get(i)) && friendlies.get(i).health() < friendlies.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            if(Pathing.move(healer, friendlies.get(idx).location().mapLocation()) == false) {
                Pathing.move(healer, friendlies.get(idx).location().mapLocation());
            }
        }

        if(returnToFactory())
        	return;
        // Unit will bounce in order to escape factories
        if(ditchFactory())
        	return;
        if(ditchRocket())
        	return;
    }
    private static boolean moveTowardsRocket() {
    	if(healer.location().mapLocation().getPlanet()==Planet.Mars)return false;
        // Move towards a low-HP rocket if possible
        VecUnit rockets = gc.senseNearbyUnitsByType(healer.location().mapLocation(), healer.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(healer.location().mapLocation());
            if (Util.friendlyUnit(rockets.get(i)) && dist < minDist && rockets.get(i).structureIsBuilt()==1) {
                minDist = dist;
                idx = i;
            }
        }
        if(minDist>10 && gc.round()<=600)return false;
        if (idx != -1) {
            PlanetMap map = gc.startingMap(healer.location().mapLocation().getPlanet());
            MapLocation tmp = rockets.get(idx).location().mapLocation();
            int initx = tmp.getX();
            int inity = tmp.getY();
            tmp = new MapLocation(Planet.Earth, initx + 1, inity);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity);
            if (map.onMap(tmp)){
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth,initx + 1,inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
            if (map.onMap(tmp)){
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity - 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity + 1);
            if (map.onMap(tmp)) {
            	if(!Pathing.move(healer, tmp)){
            		Pathing.tryMove(healer, healer.location().mapLocation().directionTo(tmp));
            	}
            	return true;
            }
            // System.out.println("Moving towards friendly rocket.");
            return true;
        }

        return false;
    }
    private static boolean returnToFactory(){
    	List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist =  healer.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if(dist<=16) return false;
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (minDist >= 1000)  return false;
        if(idx == -1)return false;
        Pathing.tryMove(healer, healer.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        return true;
    }
    private static boolean ditchFactory() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
            long dist = 50 - healer.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0)  return false;

        Direction opposite = Pathing.opposite(healer.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(healer, opposite);
        return true;
    }
    private static boolean ditchRocket() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Rocket);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
        	if(units.get(i).structureIsBuilt()==1)continue;
            long dist = 50 - healer.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0)  return false;

        Direction opposite = Pathing.opposite(healer.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(healer, opposite);
        return true;
    }
}
