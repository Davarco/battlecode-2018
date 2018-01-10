import bc.Direction;
import bc.GameController;
import bc.Unit;

public class Ranger {

    private static Unit ranger;
    private static GameController gc;

    public static void run(Unit unit, GameController controller) {

        // Receive ranger from main runner
        ranger = unit;
        gc = controller;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(ranger.id()) && gc.canMove(ranger.id(), direction)) {
                gc.moveRobot(ranger.id(), direction);
            }
        }
    }
}
