import bc.*;

public class Rocket {

    private static Unit rocket;
    private static GameController gc;
    private static VecUnit friendlies;
    private static PlanetMap marsMap;

    public static void init(GameController controller) {
        gc = controller;
        marsMap = gc.startingMap(Planet.Mars);
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
        friendlies = gc.senseNearbyUnitsByTeam(rocket.location().mapLocation(), rocket.visionRange(), TeamUtil.friendlyTeam());

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
        if (rocket.structureGarrison().size() >= 6) {
            for (int x = 0; x < marsMap.getWidth(); x++) {
                for (int y = 0; y < marsMap.getHeight(); y++) {
                    MapLocation temp = new MapLocation(Planet.Mars, x, y);
                    if (marsMap.isPassableTerrainAt(temp) == 1) {
                        gc.launchRocket(rocket.id(), temp);
                        return;
                    }
                }
            }
        }
    }
}
