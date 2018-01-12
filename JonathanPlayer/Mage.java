import bc.*;
import java.util.*;

public class Mage {

    private static Unit mage;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive mage from main runner
        mage = unit;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(mage.id()) && gc.canMove(mage.id(), direction)) {
                gc.moveRobot(mage.id(), direction);
            }
        }
    }
}
