import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Worker {

    private static Unit worker;
    private static GameController gc;
    private static VecUnit factories;
    private static VecUnit rockets;

    private static boolean isAttacked;

    private static int workerId;
    private static MapLocation workerLoc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive worker from main runner
        worker = unit;

        if (worker.location().isInGarrison() || worker.location().isInSpace()) return;

        workerId = worker.id();
        workerLoc = worker.location().mapLocation();

        if (Player.underConstruction) {

            move();
            repairStructure(UnitType.Factory);
            repairStructure(UnitType.Rocket);
        }

         else {
            // General move function, handles priorities
            long t1 = System.currentTimeMillis();
            move();
            long t2 = System.currentTimeMillis();
            Player.time += (t2 - t1);
            build();

            // Repair structures that we can
            repairStructure(UnitType.Factory);
            repairStructure(UnitType.Rocket);

//            // Repair structures that we can
//            repairStructure(UnitType.Factory);
//            repairStructure(UnitType.Rocket);

            // Harvest karbonite if we can
            harvestKarbonite();

        }
    }

    private static void move() {
        
        /*
        TODO Implement the entire worker move function as a heuristic based on priority 
         */

        // Only move if we can move
        if (!gc.isMoveReady(workerId))
            return;

        // Escaping enemy units has the highest priority
        if (escape())
            return;

        if (Player.underConstruction) {
            constructionTimeBuild();
            constructionTimeMove();
            if (!(Player.constructionSite.values().contains(workerId))) {
                if (standOff()) return;
            }
        }

        else {
            if (standOff()) {
                System.out.println("Standing off!");
                return;
            }

            if (moveTowardsKarbonite())
                return;
            repairStructure(UnitType.Factory);
            repairStructure(UnitType.Rocket);

        }

        // If all of the above failed, we likely have to clear up some space
        if (moveOutOfWay())
            return;

        updateWorker();
    }

    private static void constructionTimeMove() {
        if (moveTowardsFactory()) return;
        if (moveTowardsRocket()) return;
    }

    private static void constructionTimeBuild() {
        if (Info.number(UnitType.Factory) == 0) {
            create(UnitType.Factory);

        }
    }

    private static void build() {

//        // Create factories
//        if (Info.number(UnitType.Factory) < Config.FACTORY_EQUILIBRIUM && Player.turnsSinceEndOfConstruction >= Config.ROUNDS_BETWEEN_FACTORIES && !Player.underConstruction) {
//            create(UnitType.Factory);
//
//        }
//
//        // Replicate if we don't have enough workers
//        if (Info.number(UnitType.Worker) < Config.WORKER_EQUILIBRIUM) {
//            replicate();
//        }
// Create factories
        if (Info.number(UnitType.Worker) <  Info.number(UnitType.Factory)*2+3) {
            replicate();
        }
        else {
            create(UnitType.Factory);
        }

        // Build rockets
        if (gc.round() > Config.ROCKET_CREATION_ROUND && !Player.underConstruction) {
            if (Info.number(UnitType.Rocket) < Config.ROCKET_EQUILIBRIUM) {
                create(UnitType.Rocket);
            }
        }
    }

    private static boolean escape() {

        // See if unit needs to escape
        if (Pathing.escape(worker)) {
            isAttacked = true;
            System.out.println("Worker " + workerLoc + " is being attacked!");
            return true;
        } else {
            isAttacked = false;
        }

        return false;
    }

    private static boolean standOff() {
        return Pathing.ditchFactory(worker);
    }

    private static boolean moveTowardsRocket() {

        // Move towards a low-HP rocket if possible
        rockets = gc.senseNearbyUnitsByType(workerLoc, worker.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(workerLoc);
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

//        // Move towards the closest factory
//        List<MapLocation> locations = Info.unitLocations.get(UnitType.Factory);
//        long minDist = Long.MAX_VALUE;
//        int idx = -1;
//        for (int i = 0; i < locations.size(); i++) {
//            long dist = workerLoc.distanceSquaredTo(locations.get(i));
//            if (dist < minDist) {
//                minDist = dist;
//                idx = i;
//            }
//        }
//        if (idx != -1) {
//            Pathing.move(worker, locations.get(idx));
//            return true;
//        }
//
//        return false;

//        // Move towards a low-HP factory if possible
//        factories = gc.senseNearbyUnitsByType(workerLoc, worker.visionRange(), UnitType.Factory);
//        long minDist = Long.MAX_VALUE;
//        int idx = -1;
//        for (int i = 0; i < factories.size(); i++) {
//            long dist = factories.get(i).location().mapLocation().distanceSquaredTo(workerLoc);
//            if (Util.friendlyUnit(factories.get(i)) && factories.get(i).health() < factories.get(i).maxHealth() && dist < minDist) {
//                minDist = dist;
//                idx = i;
//            }
//        }
//        if (idx != -1) {
//            Pathing.move(worker, factories.get(idx).location().mapLocation());
//            // System.out.println("Moving towards friendly factory.");
//            return true;
//        }
//
//        return false;

        if (Player.constructionSite.values().contains(workerId)) {
            for (MapLocation m : Player.constructionSite.keySet()) {
                if (Player.constructionSite.get(m) == workerId) {
                    Pathing.move(worker, m);
                    return true;
                }
            }
        } else if (Player.constructionSite.values().contains(0)) {
            for (MapLocation m : Player.constructionSite.keySet()) {
                if (Player.constructionSite.get(m) == 0) {
                    Player.constructionSite.put(m, workerId);
                    Pathing.move(worker, m);// 0 means that slot is unassigned yet
                    return true;
                }
            }
        }
        return false;
    }

//        long minDist = Long.MAX_VALUE;
//        MapLocation best = new MapLocation(Planet.Earth, -1, -1);
//        for (MapLocation dest : Player.constructionSite.keySet()) {
//            long dist =  dest.distanceSquaredTo(workerLoc);
//            if (dist < minDist && Player.constructionSite.get(dest)) {
//                minDist = dist;
//                best = dest;
//            }
////            if (dist == 0) {
////                Player.constructionSite.put(dest, false);
////            }
//        }
//
//        if (gc.startingMap(Planet.Earth).onMap(best)) {
//            // if nothing found, default location is off the map
//            Pathing.move(worker, best);
//            return true;
//        }
//        return false;

//        long minDist = Long.MAX_VALUE;
//        MapLocation best = new MapLocation(Planet.Earth, -1, -1);
//        HashMap<MapLocation, Boolean> bestListOfDest = new HashMap<MapLocation, Boolean>();
//        for (HashMap<MapLocation, Boolean> listOfDest : Player.workerDestinations.values()) {
//            for (MapLocation dest : listOfDest.keySet()) {
//                long dist =  dest.distanceSquaredTo(workerLoc);
//                if (dist < minDist && listOfDest.get(dest)) {
//                    minDist = dist;
//                    best = dest;
//                    bestListOfDest = listOfDest;
//                }
//            }
//        }
//        if (gc.startingMap(Planet.Earth).onMap(best)) {
//            // if nothing found, default location is off the map
//            Pathing.move(worker, best);
//            bestListOfDest.put(best, false);
//            return true;
//        }
//        return false;


    private static boolean moveTowardsKarbonite() {
        MapLocation loc = bestKarboniteLoc();
        if (loc != workerLoc) { // bestKarboniteLoc returns the worker's position if nothing is found
            Pathing.move(worker, loc);
            return true;
        }
        
        return false;
    }

    private static boolean moveOutOfWay() {
        int num_free_cells = 0;
        for (Direction d : Direction.values()) {
            MapLocation loc = workerLoc.add(d);
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
        VecUnit structures = gc.senseNearbyUnitsByType(workerLoc, worker.visionRange(), StructureToRepair);

        // Repair a Structure in range, only in Earth
        if (workerLoc.getPlanet().equals(Planet.Earth)) {
            for (int i = 0; i < structures.size(); i++) {
                if (gc.canBuild(workerId, structures.get(i).id())) {
                    gc.build(workerId, structures.get(i).id());
                }
                if (gc.canRepair(workerId, structures.get(i).id())) {
                    gc.repair(workerId, structures.get(i).id());
                }
            }
        }
    }

    private static void harvestKarbonite() {
        for (Direction d : Direction.values()) {
            if (gc.startingMap(Planet.Earth).onMap(workerLoc.add(d)) && gc.canHarvest(workerId, d)) {
                gc.harvest(workerId, d);
            }
        }
    }

    private static MapLocation bestKarboniteLoc() {
        VecMapLocation allInRange = gc.allLocationsWithin(workerLoc, worker.visionRange());
        int bestHeuristicSoFar = Integer.MAX_VALUE;
        int bestIdxSoFar = -1;
        for (int i = 0; i < allInRange.size(); i++) {
            MapLocation loc = allInRange.get(i);
            if (gc.startingMap(Planet.Earth).onMap(loc) && gc.karboniteAt(loc) > 0 && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1)) {
                int heuristic = (int) (Math.sqrt(50 - gc.karboniteAt(loc)) + Math.pow((int) (workerLoc.distanceSquaredTo(loc)), 2));
                if (heuristic < bestHeuristicSoFar) {
                    bestHeuristicSoFar = heuristic;
                    bestIdxSoFar = i;
                }
            }
        }

        if (bestIdxSoFar != -1) {
            return allInRange.get(bestIdxSoFar);
        } else {
            return workerLoc;
        }
    }

    private static void replicate() {
        for (Direction dir : Direction.values()) {
            if (gc.canReplicate(workerId, dir)) {
                gc.replicate(workerId, dir);
            }
        }
    }

    private static void create(UnitType type) {
        for (Direction dir : Direction.values()) {
            if (gc.canBlueprint(workerId, type, dir)) {
                updateWorker(); // Needed to avoid bugz
                gc.blueprint(workerId, type, dir);
                System.out.println("------PLACED BP ON " + workerLoc.add(dir).toString());
                startConstruction(workerLoc.add(dir));
            }
        }
    }

    private static boolean startConstruction(MapLocation loc) {
        Player.constructionLoc = loc;
        Player.underConstruction = true;
        Player.constructionSite = Util.openSpacesAround(loc, 8);
        System.out.println("Construction started on building @ " + Player.constructionLoc);
        return true;
    }

    private static void updateWorker() {
        worker = gc.unit(workerId);
        workerLoc = worker.location().mapLocation();
        workerId = worker.id();
    }
}