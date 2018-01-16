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

        escape();

        moveOutOfWay();

        if (gc.karbonite() < 50) {
            moveTowardsKarbonite();
            harvestKarbonite();
        }

        if (gc.round() > 5) {
            moveTowardsFactory();
            repairShit(UnitType.Factory);
        }

        if (Info.number(UnitType.Worker) < Config.WORKER_EQUILIBRIUM) {
            replicate();
        }

        if (Info.number(UnitType.Factory) < Config.FACTORY_EQUILIBRIUM) {
            create(UnitType.Factory);
            repairShit(UnitType.Factory);
        }

        if (gc.round() > Config.ROCKET_CREATION_ROUND) {
            if (Info.number(UnitType.Rocket) < Config.ROCKET_EQUILIBRIUM) {
                create(UnitType.Rocket);
            }
            moveTowardsRocket();
            repairShit(UnitType.Rocket);
        }




    }

    private static void escape() {

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

    private static void replicate() {
        for (Direction dir : Direction.values()) {
            if (gc.canReplicate(worker.id(), dir)) {
                gc.replicate(worker.id(), dir);
            }
        }
    }

    private static void moveOutOfWay() {
        int num_free_cells = 0;
        for (Direction d : Direction.values()) {
            MapLocation loc = worker.location().mapLocation().add(d);
            if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                num_free_cells++;
            }
        }
        if (num_free_cells < Config.FREE_CELL_CONSTANT) {
            int random  = (int) (Math.random() * 8);
            Pathing.tryMove(worker, Direction.values()[random]);
        }
    }

    private static void moveTowardsFactory() {
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
        }
    }

    private static void moveTowardsRocket() {
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
        }
    }

    private static void repairShit(UnitType shitToRepair) {
        VecUnit listOfShitToRepair = gc.senseNearbyUnitsByType(worker.location().mapLocation(), worker.visionRange(), shitToRepair);

        // Repair a shit in range, only in Earth
        if (worker.location().mapLocation().getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < listOfShitToRepair.size(); i++) {
                if (gc.canBuild(worker.id(), listOfShitToRepair.get(i).id())) {
                    gc.build(worker.id(), listOfShitToRepair.get(i).id());
                }
                if (gc.canRepair(worker.id(), listOfShitToRepair.get(i).id())) {
                    gc.repair(worker.id(), listOfShitToRepair.get(i).id());
                }
            }
        }
    }

    private static void moveTowardsKarbonite() {
        MapLocation loc = bestKarboniteLoc();
        if (loc != worker.location().mapLocation()) { // bestKarboniteLoc returns the worker's position if nothing is found
            Pathing.move(worker, loc);
        }
    }

    private static void harvestKarbonite() {
        for (Direction d : Direction.values()) {
            if (gc.startingMap(Planet.Earth).onMap(worker.location().mapLocation().add(d)) && gc.canHarvest(worker.id(), d)) {
                gc.harvest(worker.id(), d);
            }
        }
    }
}