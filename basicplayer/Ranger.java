import bc.*;

import java.util.ArrayList;
import java.util.List;

public class Ranger {

    private static Unit ranger;
    private static GameController gc;
    private static VecUnit enemies;
    private static Direction curr;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive ranger from main runner
        ranger = unit;
        if (ranger.location().isInGarrison() || ranger.location().isInSpace()) return;

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
        if (gc.round() < Config.RANGER_AUTO_ATTACK_ROUND)
            if (moveTowardsInitPoint())
                return;

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

        // Otherwise changes towards enemies
        if (idx != -1) {
            Pathing.move(ranger, enemies.get(idx).location().mapLocation());
            return;
        }

        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(ranger, FocusPoints.GeographicFocusPointsE.get(0));
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
            long dist = ranger.location().mapLocation().distanceSquaredTo(locations.get(i));
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        if (idx != -1) {
            System.out.println(locations.get(idx));
            Pathing.move(ranger, locations.get(idx));
            return true;
        }

        return false;
    }
}
