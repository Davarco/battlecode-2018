import bc.*;

import java.util.ArrayList;
import java.util.List;

public class Ranger {

    private static Unit ranger;
    private static GameController gc;
    private static VecUnit enemies;
    private static Direction curr;
    private static int rangerId;
    private static MapLocation rangerLoc;
    private static long rangerVisRange;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {
        // Receive ranger from main runner
        ranger = unit;


        if (ranger.location().isInGarrison() || ranger.location().isInSpace()) return;

        rangerId = ranger.id();
        rangerLoc = ranger.location().mapLocation();
        rangerVisRange = ranger.visionRange();

        /*
        Scenario 1: Attack first and then run away to get out of enemy range
        Scenario 2: Move first to get into range and then attack
         */
        
        if (!attack()) {
            long t1 = System.currentTimeMillis();
            move();
            ranger = gc.unit(rangerId);
            long t2 = System.currentTimeMillis();
            Player.time += (t2 - t1);
            attack();
        } else {
            long t1 = System.currentTimeMillis();
            move();
            ranger = gc.unit(rangerId);
            long t2 = System.currentTimeMillis();
            Player.time += (t2 - t1);
        }
    }
    private static void MoveAway(){
    	
    }

    private static boolean attack() {

        // Return true if attack isn't ready
        if (!gc.isAttackReady(rangerId))
            return true;

        // Get enemy units

        enemies = gc.senseNearbyUnitsByTeam(rangerLoc, ranger.attackRange(), Util.enemyTeam());
        if (enemies.size() == 0)
            return false;

        // Attack lowest HP target
        long minHp = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).health() < minHp) {
                minHp = enemies.get(i).health();
                idx = i;
            }
        }
        if (gc.canAttack(rangerId, enemies.get(idx).id())) {
            gc.attack(rangerId, enemies.get(idx).id());
        }

        return true;
    }

    private static void move() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(rangerId)) {
            return;
        }
        
        
        
        // Avoid enemy units, walk outside of their view range
        if (Pathing.escape(ranger)) {
            return;
        }
        
        

        // Move towards initial enemy worker locations
        if (gc.round() < Config.RANGER_AUTO_ATTACK_ROUND && Info.number(UnitType.Ranger)>=8 && ranger.location().isOnPlanet(Planet.Earth))
            if (moveTowardsInitPoint())
                return;

        // Get closest enemy
        enemies = gc.senseNearbyUnitsByTeam(rangerLoc, rangerVisRange, Util.enemyTeam());
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            long dist = rangerLoc.distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        // Remove focal point if no units exist there
        if (Player.focalPoint != null) {
            if (Player.focalPoint.isWithinRange(rangerVisRange, rangerLoc) && gc.canSenseLocation(Player.focalPoint) &&
                    gc.hasUnitAtLocation(Player.focalPoint)) {
                // System.out.println("Works");
                Player.focalPoint = null;
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
            Pathing.move(ranger, Player.focalPoint);
        }
        
        if(gc.round()>=530 && ranger.location().isOnPlanet(Planet.Earth) && gc.round()<=640){
        	if(moveTowardsRocket()){
        		return;
        	}
        }
        if(gc.round()<=530 && ranger.location().isOnPlanet(Planet.Earth)){
        	if(moveAwayFactory()){
        		return;
        	}
        }

        // Otherwise changes towards enemies
        if (idx != -1) {
            Pathing.move(ranger, enemies.get(idx).location().mapLocation());
            return;
        }

        // If none of the above work, changes in a random direction (placeholder for now)
        Pathing.move(ranger,  Direction.values()[(int)(Math.random() * 8)]);
    }

    private static boolean moveTowardsInitPoint() {

        // Initial focal point should be the opposite of the closest worker
        List<MapLocation> locations = new ArrayList<>();
        int H=(int)gc.startingMap(Planet.Earth).getHeight();
        int W=(int)gc.startingMap(Planet.Earth).getWidth();
        for (MapLocation loc: Info.unitLocations.get(UnitType.Worker)) {
            loc.setX(W-loc.getX());
            loc.setY(H-loc.getY());
            locations.add(loc);
        }

        // Get closest one
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < locations.size(); i++) {
            long dist = rangerLoc.distanceSquaredTo(locations.get(i));
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        if (idx != -1) {
            Pathing.move(ranger, locations.get(idx));
            return true;
        }

        return false;
    }
    private static boolean moveAwayFactory() {
        int num_free_cells = 0;
        for (Direction d : Direction.values()) {
            MapLocation loc = ranger.location().mapLocation().add(d);
            if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                num_free_cells++;
            }
        }
        if (num_free_cells < Config.FREE_CELL_CONSTANT) {
            int random = (int) (Math.random() * 8);
            Pathing.tryMove(ranger, Direction.values()[random]);
            return true;
        }
        
        return false;
    }
    private static boolean moveTowardsRocket() {

        // Move towards a low-HP rocket if possible
        VecUnit rockets = gc.senseNearbyUnitsByType(ranger.location().mapLocation(), ranger.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(ranger.location().mapLocation());
            if (Util.friendlyUnit(rockets.get(i)) && rockets.get(i).health() < rockets.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
        	PlanetMap map = gc.startingMap(Planet.Earth);
        	MapLocation tmp = rockets.get(idx).location().mapLocation();
        	tmp = new MapLocation(Planet.Earth,tmp.getX()+1,tmp.getY());
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            tmp = new MapLocation(Planet.Earth,tmp.getX()-1,tmp.getY());
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            tmp = new MapLocation(Planet.Earth,tmp.getX()+1,tmp.getY()+1);
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            tmp = new MapLocation(Planet.Earth,tmp.getX()-1,tmp.getY()-1);
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            tmp = new MapLocation(Planet.Earth,tmp.getX()+1,tmp.getY()-1);
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            tmp = new MapLocation(Planet.Earth,tmp.getX()-1,tmp.getY()+1);
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            tmp = new MapLocation(Planet.Earth,tmp.getX(),tmp.getY()-1);
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            tmp = new MapLocation(Planet.Earth,tmp.getX(),tmp.getY()+1);
            if(map.onMap(tmp) && Pathing.move(ranger, tmp))return true;
            
            return false;
        }

        return false;
    }
}
