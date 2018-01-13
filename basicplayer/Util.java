import bc.*;

import java.util.HashMap;

public class Util {

    private static GameController gc;
    private static Team friendly;
    private static Team enemy;

    public static void init(GameController controller) {
        gc = controller;

        // Initialize our team and enemy team
        friendly = gc.team();
        enemy = (friendly.equals(Team.Red)) ? Team.Blue : Team.Red;
    }

    public static Team friendlyTeam() {
        return friendly;
    }

    public static Team enemyTeam() {
        return enemy;
    }
}
