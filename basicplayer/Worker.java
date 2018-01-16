import bc.*;

public class Worker {

    private static Unit worker;
    private static GameController gc;
    private static VecUnit factories;
    private static VecUnit rockets;

    private static boolean isAttacked;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive worker from main runner
        worker = unit;

        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;

        // Move unit (placeholder for now)
        move();

        // Build structure if needed
        build();

        // Repair structure if possible
        repair();
    }

    private static void move() {

        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            isAttacked = true;
            System.out.println("Worker " + worker.location().mapLocation() + " is being attacked!");
            return;
        } else {
            isAttacked = false;
        }

        // Move towards a low-HP factory if possible
        factories = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), UnitType.Factory);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < factories.size(); i++) {
            long dist = factories.get(i).location().mapLocation().distanceSquaredTo(worker.location().mapLocation());
            if (Util.friendlyUnit(factories.get(i)) && factories.get(i).health() < factories.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            Pathing.move(worker, factories.get(idx).location().mapLocation());
            // System.out.println("Moving towards friendly factory.");
            return;
        }

        // Move towards a low-HP rocket if possible
        rockets = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), UnitType.Rocket);
        minDist = Long.MAX_VALUE;
        idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(worker.location().mapLocation());
            if (Util.friendlyUnit(rockets.get(i)) && rockets.get(i).health() < rockets.get(i).maxHealth() && dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (idx != -1) {
            Pathing.move(worker, rockets.get(idx).location().mapLocation());
            // System.out.println("Moving towards friendly rocket.");
            return;
        }

        //Move towards best karbonite location
        MapLocation loc = bestKarboniteLoc();
        if (loc != worker.location().mapLocation()) { // bestKarboniteLoc returns the worker's position if nothing is found
            Pathing.move(worker, loc);
        }
    }

    private static void build() {

        // Check number of factories, ideally we should have at least 1
        if (Info.number(UnitType.Factory) < 1) {
            create(UnitType.Factory);
        }

        // Only build rockets past turn 200, good number to have is around NUM_TURNS/200
        if (gc.round() >= 200 && Info.number(UnitType.Rocket) < gc.round() / 200) {
            create(UnitType.Rocket);
        }

        // If we have enough units, have workers replicate themselves
        if (Info.totalUnits >= Info.number(UnitType.Worker) * 8) {
            for (Direction dir : Direction.values()) {
                if (gc.canReplicate(worker.id(), dir)) {
                    gc.replicate(worker.id(), dir);
                    break; // Don't replicate more than once
                }
            }
        }
    }

    private static void repair() {

        // Repair a factory in range, but factories only exist on Earth
        if (worker.location().mapLocation().getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < factories.size(); i++) {
                if (gc.canBuild(worker.id(), factories.get(i).id())) {
                    gc.build(worker.id(), factories.get(i).id());
                }
                if (gc.canRepair(worker.id(), factories.get(i).id())) {
                    gc.repair(worker.id(), factories.get(i).id());
                }
            }
        }

        // Repair a rocket in range, only in Earth
        if (worker.location().mapLocation().getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < rockets.size(); i++) {
                if (gc.canBuild(worker.id(), rockets.get(i).id())) {
                    gc.build(worker.id(), rockets.get(i).id());
                }
                if (gc.canRepair(worker.id(), rockets.get(i).id())) {
                    gc.repair(worker.id(), rockets.get(i).id());
                }
            }
        }
    }

    private static void create(UnitType type) {
        for (Direction dir : Direction.values()) {
            if (gc.canBlueprint(worker.id(), type, dir)) {
                gc.blueprint(worker.id(), type, dir);
            }
        }
    }

    private static MapLocation bestKarboniteLoc() {
        VecMapLocation allInRange = gc.allLocationsWithin(worker.location().mapLocation(), worker.visionRange());
        int bestHeuristicSoFar = Integer.MAX_VALUE;
        int bestIdxSoFar = -1;
        for (int i = 0; i < allInRange.size(); i++) {
            MapLocation loc = allInRange.get(i);
            if (gc.startingMap(Planet.Earth).onMap(loc) && gc.karboniteAt(loc) > 0 && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1)) {
                int heuristic = (int) (Math.sqrt(50 - gc.karboniteAt(loc)) + Math.pow((int)(worker.location().mapLocation().distanceSquaredTo(loc)), 2));
                if (heuristic < bestHeuristicSoFar) {
                    bestHeuristicSoFar = heuristic;
                    bestIdxSoFar = i;
                }
            }
        }

        if (bestIdxSoFar != -1) {
            return allInRange.get(bestIdxSoFar);
        } else {
            return worker.location().mapLocation();
        }
    }
}