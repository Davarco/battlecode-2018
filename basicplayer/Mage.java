import bc.Direction;
import bc.GameController;
import bc.Unit;

public class Mage {

    private static Unit mage;
    private static GameController gc;

    public static void run(Unit unit, GameController controller) {

        // Receive mage from main runner
        mage = unit;
        gc = controller;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(mage.id()) && gc.canMove(mage.id(), direction)) {
                gc.moveRobot(mage.id(), direction);
            }
        }
    }
}
