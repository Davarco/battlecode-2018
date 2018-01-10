import bc.*;

public class Worker {

    private static Unit worker;
    private static GameController gc;

    public static void run(Unit unit, GameController controller) {

        // Receive worker from main runner
        worker = unit;
        gc = controller;

        // Move unit (placeholder for now)
        for (Direction direction: Direction.values()) {
            if (gc.isMoveReady(worker.id()) && gc.canMove(worker.id(), direction)) {
                gc.moveRobot(worker.id(), direction);
            }
        }
    }
}
