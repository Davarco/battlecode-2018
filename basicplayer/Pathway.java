import bc.*;
import java.util.*;

public class Pathway {
	public ArrayList<MapLocation> unitpathway;
	public int index;
	public MapLocation goal;
	
	public Pathway(ArrayList<MapLocation> p, MapLocation g) {
		unitpathway = p;
		index = 0;
		goal = g;
	}
	
	public MapLocation getNextLocation() {
		return unitpathway.get(index);
	}
	
	public void setNewPathway(ArrayList<MapLocation> p, MapLocation k) {
		unitpathway = p;
		goal = k;
		index = 0;
	}
	
	public boolean NextLocationIsEnd() {
		return index == unitpathway.size()-1;
	}
	
	public boolean PathwayDoesNotExist() {
		return unitpathway == null;
	}
}
