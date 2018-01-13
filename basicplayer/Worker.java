import bc.*;

public class Worker {

    private static Unit worker;
    private static GameController gc;

    private static boolean isAttacked;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive worker from main runner
        worker = unit;

        // Move unit (placeholder for now)
        move();

        // Build structure if needed
        build();
    }

    private static void move() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            isAttacked = true;
            return;
        } else {
            isAttacked = false;
        }

        // Otherwise move to a random location
        Pathing.move(worker, worker.location().mapLocation(), new MapLocation(Planet.Earth, 5, 14)); // Placeholder
    }

    private static void build() {

        // Check number of factories, ideally we should have at least 1
        if (Count.number(UnitType.Factory) < 1) {
            create(UnitType.Factory);
        }
    }

    private static void create(UnitType type) {
        for (Direction dir: Direction.values()) {
            if (gc.canBlueprint(worker.id(), type, dir)) {
                gc.blueprint(worker.id(), type, dir);
            }
        }
    }
}
