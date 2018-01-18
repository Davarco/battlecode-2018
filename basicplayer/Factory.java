import bc.*;

import java.util.HashMap;

public class Factory {

    private static Unit factory;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive factory from main runner
        factory = unit;

        System.out.println("c: " +factory.location().mapLocation());
        System.out.println("a " + Player.constructionLoc);
        if (factory.location().mapLocation().equals(Player.constructionLoc)) { // This structure is marked as under construction, check to see if completed
            manageConstruction();
        }

        else {
            // Build best unit possible
            build();
            // Unload any units in queue
            unload();
        }


    }

    private static void manageConstruction() {
        if (!isStillBlueprint()) {
            System.out.println("Construction done on factory " + factory.id());
            Player.underConstruction = false;
            Player.constructionLoc = new MapLocation(Planet.Earth, -1, -1);
            Player.turnsSinceEndOfConstruction = 0;
            Player.constructionSite = new HashMap<MapLocation, Integer>();
        }
    }


    private static void build() {

        // Workers are vital, build them if we have nothing left
        if (Info.number(UnitType.Worker) < 1) {
            if (gc.canProduceRobot(factory.id(), UnitType.Worker)) {
                gc.produceRobot(factory.id(), UnitType.Worker);
                Info.addUnit(UnitType.Worker);
            }
        }

        // Rangers aren't as vital, but we should have a decent number of them
        if (Info.number(UnitType.Ranger) < 2) {
            if (gc.canProduceRobot(factory.id(), UnitType.Ranger)) {
                gc.produceRobot(factory.id(), UnitType.Ranger);
                Info.addUnit(UnitType.Ranger);
            }
        }

        // Build healers if there are a lot of other troops
//        if (Info.totalUnits >= Info.number(UnitType.Healer)*8) {
        if (Info.number(UnitType.Healer) < Config.HEALERS) {
            if (gc.canProduceRobot(factory.id(), UnitType.Healer)) {
                gc.produceRobot(factory.id(), UnitType.Healer);
                Info.addUnit(UnitType.Healer);
            }
        }

        // See if the factory can build the ranger
        if (gc.canProduceRobot(factory.id(), UnitType.Ranger)) {
            gc.produceRobot(factory.id(), UnitType.Ranger);
            Info.addUnit(UnitType.Ranger);
        }
    }

    private static void unload() {

        // Check all possible directions
        for (Direction dir: Direction.values()) {
            if (gc.canUnload(factory.id(), dir)) {
                gc.unload(factory.id(), dir);
            }
        }
    }

    private static boolean isStillBlueprint() { return factory.health() < factory.maxHealth(); }
}
