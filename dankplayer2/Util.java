import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    private static GameController gc;
    private static Team friendly;
    private static Team enemy;

    public static void init(GameController controller) throws Exception {
        gc = controller;

        // Initialize our team and enemy team
        friendly = gc.team();
        enemy = (friendly.equals(Team.Red)) ? Team.Blue : Team.Red;
    }

    public static Team friendlyTeam() throws Exception {
        return friendly;
    }

    public static Team enemyTeam() throws Exception {
        return enemy;
    }

    public static boolean friendlyUnit(Unit unit) throws Exception {
        return unit.team().equals(friendlyTeam());
    }

    public static Planet oppositePlanet(Planet planet) throws Exception {
        if (planet.equals(Planet.Earth)) return Planet.Mars;
        return Planet.Earth;
    }

    public static HashMap<MapLocation, Boolean> openSpacesAround(MapLocation m, int maxSpacesAround) throws Exception {
        HashMap<MapLocation, Boolean> locs = new HashMap<MapLocation, Boolean>();
        for (Direction d : Direction.values()) {
            MapLocation loc = m.add(d);
            if (gc.startingMap(Planet.Earth).onMap(loc) && (gc.startingMap(Planet.Earth).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                locs.put(loc, true);
            }
        }
        return locs;
    }

}
