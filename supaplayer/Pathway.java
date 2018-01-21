import bc.*;
import java.util.*;

public class Pathway {
	public List<MapLocation> unitpathway;
	public int index;
	public MapLocation goal;
	public MapLocation start;
	
	public Pathway(List<MapLocation> p, MapLocation g, MapLocation s) {
		unitpathway = p;
		index = 0;
		goal = g;
		start = s;
	}
	
	public MapLocation getNextLocation() {
		return unitpathway.get(index);
	}
	
	public void setNewPathway(List<MapLocation> p, MapLocation k, MapLocation s) {
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
