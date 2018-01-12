import bc.*;
import java.util.*;

public class Ranger {

    private static Unit ranger;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive ranger from main runner
        ranger = unit;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(ranger.id()) && gc.canMove(ranger.id(), direction)) {
                gc.moveRobot(ranger.id(), direction);
            }
        }
    }
}
