import bc.Direction;
import bc.GameController;
import bc.Unit;
import bc.VecUnit;

public class Ranger {

    private static Unit ranger;
    private static GameController gc;
    private static VecUnit enemies;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive ranger from main runner
        ranger = unit;

        // Check if in garrison or space
        if (ranger.location().isInGarrison() || ranger.location().isInSpace())
            return;

        // Attack lowest HP unit
        attack();

        // Move unit in ideal direction
        move();
    }

    private static void attack() {

        // Get enemy units
        enemies = gc.senseNearbyUnitsByTeam(ranger.location().mapLocation(), ranger.visionRange(), TeamUtil.enemyTeam());
        
        // Attack lowest HP target
        long minHp = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).health() < minHp) {
                minHp = enemies.get(i).health();
                idx = 0;
            }
        }
        if (idx == -1)
            return;
        if (gc.canAttack(ranger.id(), enemies.get(idx).id()) && gc.isAttackReady(ranger.id())) {
            gc.attack(ranger.id(), enemies.get(idx).id());
        }
    }

    private static void move() {
        
        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(ranger.id()) && gc.canMove(ranger.id(), direction)) {
                gc.moveRobot(ranger.id(), direction);
            }
        }
    }
}
