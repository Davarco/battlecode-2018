import bc.*;
import java.util.*;

public class Pathway {
	private ArrayList<MapLocation> unitpathway;
	private int index;
	
	public Pathway(ArrayList<MapLocation> p) {
		unitpathway = p;
		index = 0;
	}
	
	public MapLocation getNextLocation() {
		return unitpathway.get(index++);
	}
	
	public void setNewPathway(ArrayList<MapLocation> p) {
		unitpathway = p;
		index = 0;
	}
}
