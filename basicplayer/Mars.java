import bc.*;

import java.util.ArrayList;
import java.util.List;

public class Mars {

    private static GameController gc;
    private static PlanetMap map;
    private static List<List<MapLocation>> locations;
    private static boolean visited[][];
    private static int W, H;
    private static int index;

    public static void init(GameController controller) {
        gc = controller;
        map = gc.startingMap(Planet.Mars);
        W = (int)map.getWidth();
        H = (int)map.getHeight();
        floodfill();
    }

    private static void dfs(int x, int y) {

        // Set visited to true
        visited[x][y] = true;

        // Add the location
        locations.get(index).add(new MapLocation(Planet.Mars, x, y));
        for (int i = 0; i < 8; i++) {
            int x1 = x + Pathing.changes[i][0];
            int y1 = y + Pathing.changes[i][1];
            if (map.isPassableTerrainAt(new MapLocation(Planet.Mars, x1, y1)) == 1 && !visited[x1][y1]) {
                dfs(x1, y1);
            }
        }
    }

    private static void floodfill() {

        // Setup the visited grid
        visited = new boolean[W][H];

        // Go through points, dfs
        index = 0;
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                if (map.isPassableTerrainAt(new MapLocation(Planet.Mars, x, y)) == 1 && !visited[x][y]) {
                    locations.add(new ArrayList<>());
                    dfs(x, y);
                    index += 1;
                }
            }
        }

        // Print out locations
        for (int i = 0; i < locations.size(); i++) {
            System.out.println(locations.get(i));
        }
    }
}
