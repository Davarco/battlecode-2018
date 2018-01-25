import java.util.*;
import bc.*;

public class Healer {

    private static Unit healer;
    private static GameController gc;
    private static VecUnit friendlies;
    private static HashMap<Integer, Integer> counterMap;
    private static HashMap<Integer, Direction> directionMap;

    private static boolean isAttacked;

    public static void init(GameController controller) {
        gc = controller;
        counterMap = new HashMap<>();
        directionMap = new HashMap<>();
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
            if (Util.friendlyUnit(friendlies.get(i)) && friendlies.get(i).health() < friendlies.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            if(Pathing.move(healer, friendlies.get(idx).location().mapLocation()) == false && Player.focalPoint != null) {
        			Pathing.move(healer, Player.focalPoint);
            }
        }
        bounce();

        // Move randomly (placeholder, this is never optimal)
    }
    private static boolean bounce() {

        // Reset if counter is 8
        counterMap.putIfAbsent(healer.id(), 0);
        if (counterMap.get(healer.id()) >= 8) {
            counterMap.put(healer.id(), 0);

            // Find possible movement directions
            List<Direction> dirList = new ArrayList<>();
            for (Direction d : Direction.values()) {
                MapLocation loc = healer.location().mapLocation().add(d);
                if (gc.startingMap(healer.location().mapLocation().getPlanet()).onMap(loc) && (gc.startingMap(healer.location().mapLocation().getPlanet()).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                    dirList.add(d);
                }
            }

            // Get one of the possible directions if they exist
            if (dirList.size() != 0) {
                int idx = (int) (Math.random() * dirList.size());

                // Set the current direction
                directionMap.put(healer.id(), dirList.get(idx));
            }
        }

        // Try to move in the current direction
        Direction dir = directionMap.get(healer.id());
        if (dir != null) {
            if (Pathing.tryMove(healer, dir))
                counterMap.put(healer.id(), counterMap.get(healer.id())+1);
            else
                counterMap.put(healer.id(), 0);
        } else {
            // Reset the direction
            counterMap.put(healer.id(), 8);
        }

        return false;
    }

}
