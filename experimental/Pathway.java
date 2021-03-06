import bc.*;
import java.util.*;

public class Pathway {
	public ArrayList<MapLocation> unitpathway;
	public int index;
	public MapLocation goal;
	public MapLocation start;
	
	public Pathway(ArrayList<MapLocation> p, MapLocation g, MapLocation s) {
		unitpathway = p;
		index = 0;
		goal = g;
		start = s;
	}
	
	public MapLocation getNextLocation() {
		if (index < unitpathway.size())
		    return unitpathway.get(index);
		else return null;
	}
	
	public void setNewPathway(ArrayList<MapLocation> p, MapLocation k, MapLocation s) {
		unitpathway = p;
		goal = k;
		index = 0;
		start = s;
	}
	
	public boolean NextLocationIsEnd() {
		return index == unitpathway.size()-1;
	}
	
	public boolean PathwayDoesNotExist() {
		return unitpathway == null;
	}
}
