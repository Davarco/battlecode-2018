import bc.Unit;
import bc.UnitType;

import java.util.HashMap;

public class Count {

    private static HashMap<UnitType, Integer> unitCounts;
    public static long totalUnits;

    public static void reset() {
        unitCounts = new HashMap<>();
        for (UnitType type: UnitType.values()) {
            unitCounts.put(type, 0);
        }
    }

    public static void addUnit(UnitType type) {
        unitCounts.replace(type, unitCounts.get(type) + 1);
    }

    public static int number(UnitType type) {
        return unitCounts.get(type);
    }
}
