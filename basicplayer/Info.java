import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class Info {

    private static EnumMap<UnitType, Integer> unitCounts;
    private static EnumMap<UnitType, List<MapLocation>> unitLocations;
    public static long totalUnits;

    public static void reset() {
        unitCounts = new EnumMap<>(UnitType.class);
        unitLocations = new EnumMap<>(UnitType.class);
        for (UnitType type: UnitType.values()) {
            unitCounts.put(type, 0);
            unitLocations.put(type, new ArrayList<>());
        }
    }

    public static void addUnit(Unit unit) {
        unitCounts.replace(unit.unitType(), unitCounts.get(unit.unitType()) + 1);
        if (!unit.location().isInGarrison() && !unit.location().isInSpace())
            unitLocations.get(unit.unitType()).add(unit.location().mapLocation());
    }

    public static void addUnit(UnitType unit) {
        unitCounts.replace(unit, unitCounts.get(unit) + 1);
    }

    public static int number(UnitType type) {
        return unitCounts.get(type);
    }
}
