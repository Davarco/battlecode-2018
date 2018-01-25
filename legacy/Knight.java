import bc.Direction;
import bc.GameController;
import bc.Unit;

public class Knight {

    private static Unit knight;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void runEarth(Unit unit) throws Exception {

        // Receive knight from main runner
        knight = unit;
        if (knight.location().isInGarrison() || knight.location().isInSpace()) return;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(knight.id()) && gc.canMove(knight.id(), direction)) {
                gc.moveRobot(knight.id(), direction);
            }
        }
    }

    public static void runMars(Unit unit) throws Exception {
        knight = unit;
        Logging.debug("Knight #" + knight.id() + " is on Mars!");
    }
}
