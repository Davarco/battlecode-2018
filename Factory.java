import bc.*;

public class Factory {

    private static Unit factory;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive factory from main runner
        factory = unit;

        // Build best unit possible
        build();

        // Unload any units in queue
        unload();
    }

    private static void build() {

        // Workers are vital, build them if we have nothing left
        if (Count.number(UnitType.Worker) < 1) {
            if (gc.canProduceRobot(factory.id(), UnitType.Worker)) {
                gc.produceRobot(factory.id(), UnitType.Worker);
                Count.addUnit(UnitType.Worker);
            }
        }

        // Build healers if there are a lot of other troops
        if (Count.totalUnits >= Count.number(UnitType.Healer)*6) {
            if (gc.canProduceRobot(factory.id(), UnitType.Healer)) {
                gc.produceRobot(factory.id(), UnitType.Healer);
                Count.addUnit(UnitType.Healer);
            }
        }

        // See if the factory can build the ranger
        if (gc.canProduceRobot(factory.id(), UnitType.Ranger)) {
            gc.produceRobot(factory.id(), UnitType.Ranger);
            Count.addUnit(UnitType.Ranger);
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
}
