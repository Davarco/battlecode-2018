import bc.*;

public class Worker {

    private static Unit worker;
    private static GameController gc;
    private static VecUnit factories;

    private static boolean isAttacked;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive worker from main runner
        worker = unit;

        // Check if in garrison or space
        if (worker.location().isInGarrison() || worker.location().isInSpace())
            return;

        // Move unit (placeholder for now)
        move();

        // Build structure if needed
        build();

        // Repair structure if possible
        repair();
    }

    private static void move() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            isAttacked = true;
            System.out.println("Worker " + worker.location().mapLocation() + " is being attacked!");
            return;
        } else {
            isAttacked = false;
        }

        // Move towards a low-HP factory if necessary
        factories = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), UnitType.Factory);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < factories.size(); i++) {
            long dist = factories.get(i).location().mapLocation().distanceSquaredTo(worker.location().mapLocation());
            if (TeamUtil.friendlyUnit(factories.get(i)) && factories.get(i).health() < factories.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            Pathing.move(worker, factories.get(idx).location().mapLocation());
            // System.out.println("Moving towards friendly factory.");
            return;
        }

        // Move towards karbonite
        // Placeholder, moves randomly atm
        // Move unit (placeholder for now)
        int rand = (int)(Math.random()*8);
        Pathing.tryMove(worker, Direction.values()[rand]);
    }

    private static void build() {

        // Check number of factories, ideally we should have at least 1
        if (Count.number(UnitType.Factory) < 1) {
            create(UnitType.Factory);
        }

        // Only build rockets past turn 300, good number to have is around NUM_TURNS/200
        if (gc.round() >= 200 && Count.number(UnitType.Rocket) < gc.round()/200) {
            create(UnitType.Rocket);
        }

        // If we have enough units, have workers replicate themselves
        if (Count.totalUnits >= Count.number(UnitType.Worker)*8) {
            for (Direction dir: Direction.values()) {
                if (gc.canReplicate(worker.id(), dir)) {
                    gc.replicate(worker.id(), dir);
                    break; // Don't replicate more than once
                }
            }
        }
    }

    private static void repair() {

        // Repair a structure in range
        for (int i = 0; i < factories.size(); i++) {
            if (gc.canBuild(worker.id(), factories.get(i).id())) {
                gc.build(worker.id(), factories.get(i).id());
            }
            if (gc.canRepair(worker.id(), factories.get(i).id())) {
                gc.repair(worker.id(), factories.get(i).id());
            }
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
