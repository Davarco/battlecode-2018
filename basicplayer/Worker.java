import bc.*;

import java.util.function.Consumer;

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

        // General move function, handles priorities
        long t1 = System.currentTimeMillis();
        move();
        long t2 = System.currentTimeMillis();
        Player.time += (t2 - t1);

        // Build things that we need to
        build();

        // Repair structures that we can
        repairStructure(UnitType.Factory);
        repairStructure(UnitType.Rocket);

        // Harvest karbonite if we can
        harvestKarbonite();
    }

    private static void move() {
        
        /*
        TODO Implement the entire worker move function as a heuristic based on priority 
         */

        // Only move if we can move
        if (!gc.isMoveReady(worker.id()))
            return;

        // Escaping enemy units has the highest priority
        if (escape())
            return;

        // As we won't have rockets till later, I'm assuming our factories should be mostly built by then
        if (moveTowardsRocket()) 
            return;
        
        // Repairing factories isn't as important, but is vital early game
        if (moveTowardsFactory())
            return;
        
        // Move towards karbonite after our buildings are taken care of
        if (moveTowardsKarbonite())
            return;
        
        // If all of the above failed, we likely have to clear up some space
        if (moveOutOfWay())
            return;
    }

    private static void build() {

        // Replicate if we don't have enough workers
        if (Info.number(UnitType.Worker) < Config.WORKER_EQUILIBRIUM) {
            replicate();
        }

        // Create factories
        if (Info.number(UnitType.Factory) < Config.FACTORY_EQUILIBRIUM) {
            create(UnitType.Factory);
        }

        // Build rockets
        if (gc.round() > Config.ROCKET_CREATION_ROUND) {
            if (Info.number(UnitType.Rocket) < Config.ROCKET_EQUILIBRIUM) {
                create(UnitType.Rocket);
            }
        }
    }

    private static boolean escape() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            isAttacked = true;
            System.out.println("Worker " + worker.location().mapLocation() + " is being attacked!");
            return true;
        } else {
            isAttacked = false;
        }

        return false;
    }

    private static boolean moveTowardsRocket() {

        // Move towards a low-HP rocket if possible
        rockets = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
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
            return true;
        }

        return false;
    }

    private static boolean moveTowardsFactory() {

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
            return true;
        }

        return false;
    }

    private static boolean moveTowardsKarbonite() {
        MapLocation loc = bestKarboniteLoc();
        if (loc != worker.location().mapLocation()) { // bestKarboniteLoc returns the worker's position if nothing is found
            Pathing.move(worker, loc);
            return true;
        }
        
        return false;
    }

    private static boolean moveOutOfWay() {
        int num_free_cells = 0;
        for (Direction d : Direction.values()) {
            MapLocation loc = worker.location().mapLocation().add(d);
            if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                num_free_cells++;
            }
        }
        if (num_free_cells < Config.FREE_CELL_CONSTANT) {
            int random = (int) (Math.random() * 8);
            Pathing.tryMove(worker, Direction.values()[random]);
            return true;
        }
        
        return false;
    }

    private static void repairStructure(UnitType StructureToRepair) {
        VecUnit structures = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), StructureToRepair);

        // Repair a Structure in range, only in Earth
        if (worker.location().mapLocation().getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < structures.size(); i++) {
                if (gc.canBuild(worker.id(), structures.get(i).id())) {
                    gc.build(worker.id(), structures.get(i).id());
                }
                if (gc.canRepair(worker.id(), structures.get(i).id())) {
                    gc.repair(worker.id(), structures.get(i).id());
                }
            }
        }
    }

    private static void harvestKarbonite() {
        for (Direction d : Direction.values()) {
            if (gc.startingMap(Planet.Earth).onMap(worker.location().mapLocation().add(d)) && gc.canHarvest(worker.id(), d)) {
                gc.harvest(worker.id(), d);
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
                int heuristic = (int) (Math.sqrt(50 - gc.karboniteAt(loc)) + Math.pow((int) (worker.location().mapLocation().distanceSquaredTo(loc)), 2));
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

    private static void replicate() {
        for (Direction dir : Direction.values()) {
            if (gc.canReplicate(worker.id(), dir)) {
                gc.replicate(worker.id(), dir);
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
}