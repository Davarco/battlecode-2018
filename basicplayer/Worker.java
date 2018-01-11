import bc.*;

public class Worker {

    private static Unit worker;
    private static GameController gc;

    public static void run(Unit unit, GameController controller) {

        // Receive worker from main runner
        worker = unit;
        gc = controller;

        // Move unit (placeholder for now)
        Pathing.move(worker, worker.location().mapLocation(), new MapLocation(Planet.Earth, 10, 10));
    }
}
