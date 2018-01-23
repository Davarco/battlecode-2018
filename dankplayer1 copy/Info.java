import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Info {

    private static EnumMap<UnitType, Integer> unitCounts;
    public static EnumMap<UnitType, List<Unit>> unitByTypes;
    public static long totalUnits;

    public static void reset() {
        unitCounts = new EnumMap<>(UnitType.class);
        unitByTypes = new EnumMap<>(UnitType.class);
        for (UnitType type: UnitType.values()) {
            unitCounts.put(type, 0);
            unitByTypes.put(type, new ArrayList<>());
        }
    }

    public static void addUnit(Unit unit) {
        // System.out.println("fuck " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
        unitCounts.replace(unit.unitType(), unitCounts.get(unit.unitType()) + 1);
        if (!unit.location().isInGarrison() && !unit.location().isInSpace())
            unitByTypes.get(unit.unitType()).add(unit);
        // System.out.println("fuck code " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
    }

    public static void addUnit(UnitType unit) {
        unitCounts.replace(unit, unitCounts.get(unit) + 1);
    }

    public static int number(UnitType type) {
        return unitCounts.get(type);
    }
}
