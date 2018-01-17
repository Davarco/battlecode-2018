import bc.*;

import java.util.EnumMap;
import java.util.HashMap;

public class Rocket {

    private static Unit rocket;
    private static GameController gc;
    private static VecUnit friendlies;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive rocket from main runner
        rocket = unit;

        if (rocket.id() == Player.constructionId) { // This structure is marked as under construction, check to see if completed
            manageConstruction();
        }

        // Load units if possible
        load();

        // Send them to Mars when full
        send();

        // Start unloading troops on Mars
        unload();
    }

    private static void manageConstruction() {
        if (!isStillBlueprint()) {
            System.out.println("Construction done on rocket " + rocket.id());
            Player.underConstruction = false;
            Player.constructionId = 0;
        }
    }

    private static void load() {

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
        if (rocket.structureGarrison().size() >= 6) {
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    MapLocation temp = new MapLocation(Planet.Mars, x, y);
                    if (map.isPassableTerrainAt(temp) == 1) {
                        gc.launchRocket(rocket.id(), temp);
                        System.out.println("Fucking blastoff to " + temp + "!");
                        return;
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

    private static boolean isStillBlueprint() { return rocket.health() < rocket.maxHealth(); }
}
