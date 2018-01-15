import bc.*;

import java.util.EnumMap;
import java.util.HashMap;

public class Rocket {

    private static Unit rocket;
    private static GameController gc;
    private static VecUnit friendlies;

    public static HashMap<Integer, Planet> destPlanets;

    public static void init(GameController controller) {
        gc = controller;
        destPlanets = new HashMap<>();
    }

    public static void run(Unit unit) {

        // Receive rocket from main runner
        rocket = unit;

        // Load units if possible
        load();

        // Send them to Mars when full
        send();
    }

    private static void load() {

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

        // Send them over to Mars when full
        // TODO For now, just sending to a random open location.
        // TODO In the future, this should actually pick a point where we can deal the most damage to enemy troops.
        Planet planet = destPlanets.get(rocket.id());
        PlanetMap map = gc.startingMap(planet);
        if (rocket.structureGarrison().size() >= 6) {
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    MapLocation temp = new MapLocation(planet, x, y);
                    if (map.isPassableTerrainAt(temp) == 1) {
                        gc.launchRocket(rocket.id(), temp);
                        destPlanets.replace(rocket.id(), Util.oppositePlanet(planet));
                        return;
                    }
                }
            }
        }
    }
}
