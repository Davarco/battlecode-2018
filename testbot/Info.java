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
    public static int mageCount;
    public static int healerCount;
    public static long totalUnits;

    public static void reset() {
        rocketCount=0;
        workerCount = 0;
        rangerCount = 0;
        factoryCount = 0;
        healerCount = 0;
        mageCount = 0;
        unitByTypes = new EnumMap<>(UnitType.class);
        unitByTypes.put(UnitType.Rocket, new ArrayList<>());
        unitByTypes.put(UnitType.Factory, new ArrayList<>());
    }

    public static void addUnit(Unit unit) {
        // System.out.println("fuck " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
        if (!unit.location().isInGarrison() && !unit.location().isInSpace())
            unitByTypes.get(unit.unitType()).add(unit);
        // System.out.println("fuck code " + unitCounts.get(unit.unitType()) + " " + unitByTypes.get(unit.unitType()).size());
    }

    public static void addUnit(UnitType unit) {
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
    	case Healer:
    		healerCount++;
    		break;
    	case Mage:
    		mageCount++;
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
	    	case Healer:
	    		return healerCount;
	    	case Mage:
	    		return mageCount;

    	}
    	return -1;
    }
}
