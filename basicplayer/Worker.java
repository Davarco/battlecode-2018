import bc.*;

public class Worker {

    private static Unit worker;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
    }

    public static void run(Unit unit) {

        // Receive worker from main runner
        worker = unit;

        // Move unit (placeholder for now)

    }
}
