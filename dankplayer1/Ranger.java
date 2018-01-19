import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ranger {

    private static Unit ranger;
    private static GameController gc;
    private static VecUnit enemies;
    private static HashMap<Integer, Direction> directionMap;
    private static HashMap<Integer, Integer> counterMap;

    public static void init(GameController controller) {
        gc = controller;
        directionMap = new HashMap<>();
        counterMap = new HashMap<>();
    }

    public static void run(Unit unit) {

        // Receive ranger from main runner
        ranger = unit;
        if (ranger.location().isInGarrison()) return;

        /*
        Scenario 1: Attack first and then run away to get out of enemy range
        Scenario 2: Move first to get into range and then attack
         */
        if (!attack()) {
            long t1 = System.currentTimeMillis();
            move();
            long t2 = System.currentTimeMillis();
            Player.time += (t2 - t1);
            attack();
        } else {
            long t1 = System.currentTimeMillis();
            move();
            long t2 = System.currentTimeMillis();
            Player.time += (t2 - t1);
        }
    }

    private static boolean attack() {

        // Return true if attack isn't ready
        if (!gc.isAttackReady(ranger.id()))
            return true;

        // Get enemy units
        enemies = gc.senseNearbyUnitsByTeam(ranger.location().mapLocation(), ranger.attackRange(), Util.enemyTeam());
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
        if (gc.canAttack(ranger.id(), enemies.get(idx).id())) {
            gc.attack(ranger.id(), enemies.get(idx).id());
        }

        return true;
    }

    private static void move() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(ranger.id())) {
            return;
        }

        // Avoid enemy units, walk outside of their view range
        if (Pathing.escape(ranger)) {
            return;
        }

        // Move towards initial enemy worker locations
        /*
        if (gc.round() < Config.RANGER_AUTO_ATTACK_ROUND && Info.number(UnitType.Ranger) >= 8 && ranger.location().isOnPlanet(Planet.Earth))
            if (moveTowardsInitPoint())
                return;
        */

        // Get closest enemy
        enemies = gc.senseNearbyUnitsByTeam(ranger.location().mapLocation(), ranger.visionRange(), Util.enemyTeam());
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            long dist = ranger.location().mapLocation().distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        // Remove focal point if no units exist there
        if (Player.focalPoint != null) {
            if (Player.focalPoint.isWithinRange(ranger.visionRange(), ranger.location().mapLocation()) && gc.canSenseLocation(Player.focalPoint) &&
                    !gc.hasUnitAtLocation(Player.focalPoint)) {
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

        // Move towards rockets mid-game, and escape factories early on
        if (gc.round() >= 530 && ranger.location().isOnPlanet(Planet.Earth) && gc.round() <= 640) {
            if (moveTowardsRocket()) {
                return;
            }
        }

        // Unit will bounce in order to escape factories
        bounce();

        // Otherwise changes towards enemies
        /*
        if (idx != -1) {
            Pathing.move(ranger, enemies.get(idx).location().mapLocation());
            return;
        }
        */

        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(ranger, FocusPoints.GeographicFocusPointsE.get(0));
    }

    private static boolean bounce() {

        // Reset if counter is 8
        counterMap.putIfAbsent(ranger.id(), 0);
        if (counterMap.get(ranger.id()) >= 8) {
            counterMap.put(ranger.id(), 0);

            // Find possible movement directions
            List<Direction> dirList = new ArrayList<>();
            for (Direction d : Direction.values()) {
                MapLocation loc = ranger.location().mapLocation().add(d);
                if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                    dirList.add(d);
                }
            }

            // Get one of the possible directions if they exist
            if (dirList.size() != 0) {
                int idx = (int) (Math.random() * dirList.size());

                // Set the current direction
                directionMap.put(ranger.id(), dirList.get(idx));
            }
        }

        // Try to move in the current direction
        Direction dir = directionMap.get(ranger.id());
        if (dir != null) {
            if (Pathing.tryMove(ranger, dir))
                counterMap.put(ranger.id(), counterMap.get(ranger.id())+1);
            else
                counterMap.put(ranger.id(), 0);
        } else {
            // Reset the direction
            counterMap.put(ranger.id(), 8);
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
            tmp = new MapLocation(Planet.Earth, tmp.getX() + 1, tmp.getY());
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;
            tmp = new MapLocation(Planet.Earth, tmp.getX() - 1, tmp.getY());
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;
            tmp = new MapLocation(Planet.Earth, tmp.getX() + 1, tmp.getY() + 1);
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;
            tmp = new MapLocation(Planet.Earth, tmp.getX() - 1, tmp.getY() - 1);
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;
            tmp = new MapLocation(Planet.Earth, tmp.getX() + 1, tmp.getY() - 1);
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;
            tmp = new MapLocation(Planet.Earth, tmp.getX() - 1, tmp.getY() + 1);
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;
            tmp = new MapLocation(Planet.Earth, tmp.getX(), tmp.getY() - 1);
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;
            tmp = new MapLocation(Planet.Earth, tmp.getX(), tmp.getY() + 1);
            if (map.onMap(tmp) && Pathing.move(ranger, tmp)) return true;

            return false;
        }

        return false;
    }
}
