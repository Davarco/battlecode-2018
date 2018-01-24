import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Info {

    public static EnumMap<UnitType, List<Unit>> unitByTypes;
    private static int rocketCount;
    private static int workerCount;
    private static int rangerCount;
    private static int factoryCount;
    public static long totalUnits;

    public static void reset() {
        rocketCount = 0;
        workerCount = 0;
        rangerCount = 0;
        factoryCount = 0;
        unitByTypes = new EnumMap<>(UnitType.class);
        for (UnitType type : UnitType.values()) {
            unitByTypes.put(type, new ArrayList<>());
        }
    }

    public static void addUnit(Unit unit) {
        // System.out.println("fuck " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
        if (!unit.location().isInGarrison() && !unit.location().isInSpace())
            unitByTypes.get(unit.unitType()).add(unit);
        // System.out.println("fuck code " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
    }

    public static void addUnit(UnitType unit) {
        switch (unit) {
            case Ranger:
                rangerCount++;
                break;
            case Worker:
                workerCount++;
                break;
            case Factory:
                factoryCount++;
                break;
            case Rocket:
                rocketCount++;
                break;
        }
    }

    public static int number(UnitType type) {
        switch (type) {
            case Ranger:
                return rangerCount;
            case Worker:
                return workerCount;
            case Factory:
                return factoryCount;
            case Rocket:
                return rocketCount;
        }
        return -1;
    }
}
