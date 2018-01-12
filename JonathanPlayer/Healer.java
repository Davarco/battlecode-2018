import bc.*;
import java.util.*;

public class Healer {

    private static Unit healer;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive healer from main runner
        healer = unit;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(healer.id()) && gc.canMove(healer.id(), direction)) {
                gc.moveRobot(healer.id(), direction);
            }
        }
    }
}
