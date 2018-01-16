import bc.*;

public class Rocket {

    private static Unit rocket;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive rocket from main runner
        rocket = unit;
    }
}
