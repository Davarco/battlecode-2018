import bc.Direction;
import bc.GameController;
import bc.Unit;

public class Healer {

    private static Unit healer;
    private static GameController gc;

    public static void run(Unit unit, GameController controller) {

        // Receive healer from main runner
        healer = unit;
        gc = controller;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(healer.id()) && gc.canMove(healer.id(), direction)) {
                gc.moveRobot(healer.id(), direction);
            }
        }
    }
}
