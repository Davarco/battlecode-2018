import bc.*;
import java.util.*;

public class Pathway {
	public ArrayList<MapLocation> unitpathway;
	public int index;
	
	public Pathway(ArrayList<MapLocation> p) {
		unitpathway = p;
		index = 0;
		/*if(unitpathway == null) {
			System.out.println("HI");
		}
		for(MapLocation sadf:unitpathway) {
			System.out.println(sadf.getX()+", "+sadf.getY()+", ");
		}*/
	}
	
	public MapLocation getNextLocation() {
		return unitpathway.get(index);
	}
	
	public void setNewPathway(ArrayList<MapLocation> p) {
		unitpathway = p;
		index = 0;
	}
	
	public boolean NextLocationIsEnd() {
		return index == unitpathway.size()-1;
	}
	
	public boolean PathwayDoesNotExist() {
		return unitpathway == null;
	}
}
