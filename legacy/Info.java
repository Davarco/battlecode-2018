import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Info {

    private static EnumMap<UnitType, Integer> unitCounts;
    public static EnumMap<UnitType, List<Unit>> unitByTypes;
    public static int rocketCount;
    public static int workerCount;
    public static int rangerCount;
    public static int factoryCount;
    public static long totalUnits;

    public static void reset() throws Exception {
        rocketCount=0;
        workerCount = 0;
        rangerCount = 0;
        factoryCount = 0;
        unitByTypes = new EnumMap<>(UnitType.class);
        unitByTypes.put(UnitType.Rocket, new ArrayList<>());
        unitByTypes.put(UnitType.Factory, new ArrayList<>());
    }

    public static void addUnit(Unit unit) throws Exception {
        // System.out.println("fuck " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
        if (!unit.location().isInGarrison() && !unit.location().isInSpace())
            unitByTypes.get(unit.unitType()).add(unit);
        // System.out.println("fuck code " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
    }

    public static void addUnit(UnitType unit) throws Exception {
    	switch (unit){
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
    	return;
    }

    public static int number(UnitType type) {
    	switch (type){
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
