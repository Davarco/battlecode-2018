import bc.Direction;
import bc.GameController;
import bc.Unit;
import bc.VecUnit;
import bc.MapLocation;

public class Healer {

    private static Unit healer;
    private static GameController gc;
    private static VecUnit friendlies;

    private static boolean isAttacked;

    private static int healerId;
    private static MapLocation healerLoc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive healer from main runner
        healer = unit;


        if (healer.location().isInGarrison() || healer.location().isInSpace()) return;

        healerId = healer.id();
        healerLoc = healer.location().mapLocation();

        /*
        Scenario 1: Heal first and then run away to get out of enemy range
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

    private static boolean heal() {

        // Return true if we cannot heal
        if (!gc.isHealReady(healerId))
            return true;

        // Get friendly units
        friendlies = gc.senseNearbyUnitsByTeam(healerLoc, healer.attackRange(), Util.friendlyTeam());
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
        if (gc.canHeal(healerId, friendlies.get(idx).id()) && gc.isHealReady(healerId)) {
            gc.heal(healerId, friendlies.get(idx).id());
        }

        return true;
    }

    private static void move() {

        // Return if we cannot move
        if (!gc.isMoveReady(healerId))
            return;

        // See if unit needs to escape
        if (Pathing.escape(healer)) {
            isAttacked = true;
            // System.out.println("Healer " + healerLoc + " is being attacked!");
            return;
        } else {
            isAttacked = false;
        }

        if (Pathing.ditchFactory(healer)) {
            return;
        }


        // Otherwise changes towards a low HP troop
        // TODO Implement this as a heuristic
        friendlies = gc.senseNearbyUnitsByTeam(healerLoc, healer.visionRange(), Util.friendlyTeam());
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < friendlies.size(); i++) {
            long dist = friendlies.get(i).location().mapLocation().distanceSquaredTo(healerLoc);
            if (Util.friendlyUnit(friendlies.get(i)) && friendlies.get(i).health() < friendlies.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            if(!Pathing.move(healer, friendlies.get(idx).location().mapLocation())) {
                Pathing.move(healer, FocusPoints.GeographicFocusPointsE.get(0));
            }
        }

        // Move randomly (placeholder, this is never optimal)
    }
}
