import bc.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class Rocket {

    private static Unit rocket;
    private static GameController gc;
    private static VecUnit friendlies;
    private static int index1 = 0;
    private static List<Integer> index2;

    public static void init(GameController controller) {
        gc = controller;
        index2 = new ArrayList<>();
        for (int x = 0; x < Mars.locations.size(); x++) {
            index2.add(0);
        }
    }


    public static void runEarth(Unit unit) {

        // Receive rocket from main runner
        rocket = unit;

        // Load units if possible
        load();

        // Send them to Mars when full
        send();
    }

    public static void runMars(Unit unit) {
        rocket = unit;
        // Start unloading troops on Mars
        unload();
    }

    private static void load() {

        if (gc.round() >= Config.ROCKET_CREATION_ROUND) {
            // Only load when on earth
            if (rocket.location().mapLocation().getPlanet().equals(Planet.Mars))
                return;

            // Find units around to load
            friendlies = gc.senseNearbyUnitsByTeam(rocket.location().mapLocation(), rocket.visionRange(), Util.friendlyTeam());

            // Load them all into the rocket
            for (int i = 0; i < friendlies.size(); i++) {
                if (gc.canLoad(rocket.id(), friendlies.get(i).id())) {
                    if (Player.launchCounter < 1) {
                        gc.load(rocket.id(), friendlies.get(i).id());
                    } else {
                        if (friendlies.get(i).unitType() == UnitType.Ranger) {
                            gc.load(rocket.id(), friendlies.get(i).id());
                        }
                    }
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
        if (gc.round() >= Config.ROCKET_CREATION_ROUND && rocket.structureGarrison().size() >= 4) {
            gc.launchRocket(rocket.id(), Mars.locations.get(index1).get(index2.get(index1)));
            index1 = (index1 + 1) % (Mars.locations.size());
            index2.set(index1, (index2.get(index1) + (Mars.locations.get(index1).size() == 7 ? index2.get(index1) + 11 : index2.get(index1) + 7)) % (Mars.locations.get(index1).size()));
        }
    }

    private static void unload() {

        // Only unload on Mars
        if (rocket.location().mapLocation().getPlanet().equals(Planet.Earth))
            return;

        // Check all possible directions
        for (Direction dir : Direction.values()) {
            if (gc.canUnload(rocket.id(), dir)) {
                gc.unload(rocket.id(), dir);
            }
        }
    }
}
