import java.util.ArrayList;
import java.util.LinkedList;

import bc.GameController;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;

public class FocusPoints {
    public static int H, W;
    public static int[][] enemydensity;
    public static int[][] friendlydensity;
    public static int[][] geographicmap;
    public static LinkedList<BufferPoint> queue;
    public static ArrayList<MapLocation> GeographicFocusPointsE;
    public static ArrayList<MapLocation> GeographicFocusPointsM;
    public static PlanetMap map;
    public static GameController gc;

    static class BufferPoint {
        public int x, y;

        public BufferPoint(int tx, int ty) {
            x = tx;
            y = ty;
        }
    }

    public static void init(GameController controller) {

        // Get game controller
        gc = controller;
        // Get map constraints
        // map = gc.startingMap(Planet.Earth);
        GeographicFocusPoints();
    }

    public void EnemyFocusPoints() {
        W = (int) map.getWidth();
        H = (int) map.getHeight();
    }

    public static String tab(int x) {
        if (x < 10 && x >= 0) {
            return "  ";
        }
        return " ";
    }

    public static void GeographicFocusPoints() {
        map = gc.startingMap(Planet.Earth);
        W = (int) map.getWidth();
        H = (int) map.getHeight();
        InitGeoGraph();
        GeographicFocusPointsE = Assign();
        for (MapLocation k : GeographicFocusPointsE) {
            // System.out.println(k.getX() + " " + k.getY());
        }
        map = gc.startingMap(Planet.Mars);
        W = (int) map.getWidth();
        H = (int) map.getHeight();
    }

    public static ArrayList<MapLocation> Assign() {
        // Run until queue is empty
        int max = 0;
        ArrayList<MapLocation> ans = new ArrayList<MapLocation>();
        while (!queue.isEmpty()) {
            BufferPoint location = queue.poll();
            for (int i = 0; i < 8; i++) {
                int a = location.x + Pathing.move[i][0];
                int b = location.y + Pathing.move[i][1];
                if (a >= 0 && a < W + 2 && b >= 0 && b < H + 2 && geographicmap[a][b] == -1) {
                    geographicmap[a][b] = geographicmap[location.x][location.y] + 1;
                    max = max > geographicmap[location.x][location.y] + 1 ? max : geographicmap[location.x][location.y] + 1;
                    queue.add(new BufferPoint(a, b));
                }
            }
        }
        for (int x = 0; x < W + 2; x++) {
            for (int y = 0; y < H + 2; y++) {
                // System.out.print(geographicmap[x][y] + tab(geographicmap[x][y]));
                if (geographicmap[x][y] == max) {
                    ans.add(new MapLocation(map.getPlanet(), x - 1, y - 1));
                }
            }
            // System.out.println("");
        }
        return ans;
    }


    public static void InitGeoGraph() {
        //adds a buffer
        geographicmap = new int[W + 2][H + 2];
        queue = new LinkedList<BufferPoint>();
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                if (map.isPassableTerrainAt(new MapLocation(map.getPlanet(), x, y)) == 1) {
                    geographicmap[x + 1][y + 1] = -1;
                } else {
                    geographicmap[x + 1][y + 1] = 0;
                    queue.add(new BufferPoint(x + 1, y + 1));
                }
            }
        }
        for (int x = 0; x < W + 2; x++) {
            geographicmap[x][0] = 0;
            geographicmap[x][H + 1] = 0;
            queue.add(new BufferPoint(x, 0));
            queue.add(new BufferPoint(x, H + 1));
        }
        for (int y = 0; y < H + 2; y++) {
            geographicmap[0][y] = 0;
            geographicmap[W + 1][y] = 0;
            queue.add(new BufferPoint(0, y));
            queue.add(new BufferPoint(W + 1, y));
        }
        //floodfill to add obstacles to queue
        // System.out.println("******" + queue.size());
    }
}
