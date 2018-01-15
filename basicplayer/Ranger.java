import bc.*;

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

        /*
        Scenario 1: Attack first and then run away to get out of enemy range
        Scenario 2: Move first to get into range and then attack
         */
        if (!attack()) {
            move();
            attack();
        } else {
            move();
        }
    }

    private static boolean attack() {

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
        if (gc.canAttack(ranger.id(), enemies.get(idx).id()) && gc.isAttackReady(ranger.id())) {
            gc.attack(ranger.id(), enemies.get(idx).id());
        }

        return true;
    }

    private static void move() {

        /*
        TODO Implement the entire worker move function as a heuristic based on priority
         */

        // Avoid enemy units, walk outside of their view range
        if (Pathing.escape(ranger)) {
            return;
        }

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

        // Otherwise move towards enemies
        if (idx != -1) {
            Pathing.move(ranger, enemies.get(idx).location().mapLocation());
            return;
        }

        // If none of the above work, move in a random direction (placeholder for now)
        int rand = (int)(Math.random()*8);
        Pathing.tryMove(ranger, Direction.values()[rand]);
    }
}
