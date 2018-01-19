import bc.*;

import java.util.EnumMap;
import java.util.HashMap;

public class Rocket {

    private static Unit rocket;
    private static GameController gc;
    private static VecUnit friendlies;
    private static int starti=1, startj=1;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive rocket from main runner
        rocket = unit;

        // Load units if possible
        load();

        // Send them to Mars when full
        send();

        // Start unloading troops on Mars
        unload();
    }

    private static void load() {
    	
    	if (gc.round()>=550){
        // Only load when on earth
	        if (rocket.location().mapLocation().getPlanet().equals(Planet.Mars))
	            return;
	
	        // Find units around to load
	        friendlies = gc.senseNearbyUnitsByTeam(rocket.location().mapLocation(), rocket.visionRange(), Util.friendlyTeam());
	
	        // Load them all into the rocket
	        for (int i = 0; i < friendlies.size(); i++) {
	            if (gc.canLoad(rocket.id(), friendlies.get(i).id())) {
	                gc.load(rocket.id(), friendlies.get(i).id());
	                System.out.println("Loading unit!");
	            }
	        }
    	}
    }

    private static void send() {

        // Only send when on earth
        // TODO It might not be a bad idea to have the rocket blow up once it's reached Mars, as it's useless and takes up space
        // TODO I'm keeping it as of now because it's a good HP tank
        if (rocket.location().mapLocation().getPlanet().equals(Planet.Mars))
            return;

        // Send rocket over to opposite planet when we're loaded
        // TODO For now, just sending to a random open location.
        // TODO In the future, this should actually pick a point where we can deal the most damage to enemy troops.
        PlanetMap map = gc.startingMap(Planet.Mars);
        if (gc.round()>=650){
        	int x=starti,y=startj;
        	
            for (; x < map.getWidth(); x+=3) {
                for (; y < map.getHeight(); y+=3) {
                    MapLocation temp = new MapLocation(Planet.Mars, x+1, y);
                    MapLocation temp1 = new MapLocation(Planet.Mars, x-1, y);
                    MapLocation temp2 = new MapLocation(Planet.Mars, x, y+1);
                    MapLocation temp3 = new MapLocation(Planet.Mars, x, y-1);
                    MapLocation temp4 = new MapLocation(Planet.Mars, x+1, y+1);
                    MapLocation temp5 = new MapLocation(Planet.Mars, x-1, y+1);
                    MapLocation temp6 = new MapLocation(Planet.Mars, x+1, y-1);
                    MapLocation temp7 = new MapLocation(Planet.Mars, x-1, y-1);
                    MapLocation temp8 = new MapLocation(Planet.Mars, x, y);
                    if(map.onMap(temp)&&map.onMap(temp1)&&map.onMap(temp2)&&map.onMap(temp3)&&map.onMap(temp4)&&map.onMap(temp5)
                    		&&map.onMap(temp6)&&map.onMap(temp7)&&map.onMap(temp8)){
	                    if (map.isPassableTerrainAt(temp) == 1 && map.isPassableTerrainAt(temp1) == 1 &&
	                    		map.isPassableTerrainAt(temp2) == 1 && map.isPassableTerrainAt(temp3) == 1 &&
	                    				map.isPassableTerrainAt(temp4) == 1 && map.isPassableTerrainAt(temp5) == 1 &&
	                    						map.isPassableTerrainAt(temp6) == 1 && map.isPassableTerrainAt(temp7) == 1 && map.isPassableTerrainAt(temp8) == 1 &&
	                    						gc.canLaunchRocket(rocket.id(), temp)) {
		                        gc.launchRocket(rocket.id(), temp8);
		                        starti = x;
		                        startj = y+3;
		                        System.out.println("Fucking blastoff to " + temp + "!");
		                        return;
	                    }
                    }
                }
            }
        }
    }

    private static void unload() {

        // Only unload on Mars
        if (rocket.location().mapLocation().getPlanet().equals(Planet.Earth))
            return;

        // Check all possible directions
        for (Direction dir: Direction.values()) {
            if (gc.canUnload(rocket.id(), dir)) {
                gc.unload(rocket.id(), dir);
            }
        }
    }
}
