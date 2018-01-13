import java.util.*;
import bc.Direction;
import bc.GameController;
import bc.Unit;

public class Knight {

    private static Unit knight;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive knight from main runner
        knight = unit;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(knight.id()) && gc.canMove(knight.id(), direction)) {
                gc.moveRobot(knight.id(), direction);
            }
        }
    }
}
