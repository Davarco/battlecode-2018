import bc.Direction;
import bc.GameController;
import bc.Unit;
import bc.VecUnit;

public class Healer {

    private static Unit healer;
    private static GameController gc;
    private static VecUnit friendlies;

    private static boolean isAttacked;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive healer from main runner
        healer = unit;

        // Check if in garrison or space
        if (healer.location().isInGarrison() || healer.location().isInSpace())
            return;

        /*
        Scenario 1: Heal first and then run away to get out of enemy range
        Scenario 2: Move first to get into range and then heal
         */
        if (!heal()) {
            move();
            heal();
        } else {
            move();
        }
    }

    private static boolean heal() {

        // Get friendly units
        friendlies = gc.senseNearbyUnitsByTeam(healer.location().mapLocation(), healer.attackRange(), TeamUtil.friendlyTeam());
        if (friendlies.size() == 0)
            return false;

        // Heal lowest HP target
        long minHp = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < friendlies.size(); i++) {
            if (friendlies.get(i).health() < minHp) {
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

        // See if unit needs to escape
        if (Pathing.escape(healer)) {
            isAttacked = true;
            System.out.println("Healer " + healer.location().mapLocation() + " is being attacked!");
            return;
        } else {
            isAttacked = false;
        }

        // Otherwise move towards a low HP troop
        friendlies = gc.senseNearbyUnitsByTeam(healer.location().mapLocation(), healer.visionRange(), TeamUtil.friendlyTeam());
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < friendlies.size(); i++) {
            long dist = friendlies.get(i).location().mapLocation().distanceSquaredTo(healer.location().mapLocation());
            if (TeamUtil.friendlyUnit(friendlies.get(i)) && friendlies.get(i).health() < friendlies.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            Pathing.move(healer, friendlies.get(idx).location().mapLocation());
            return;
        }

        // Move randomly (placeholder, this is never optimal)
        int rand = (int)(Math.random()*8);
        Pathing.tryMove(healer, Direction.values()[rand]);
    }
}