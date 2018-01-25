import bc.*;
import java.util.*;


public class Mars {

    private static GameController gc;
    private static PlanetMap map;
    public static ArrayList<ArrayList<MapLocation>> locations;
    private static boolean visited[][];
    private static int W, H;
    private static int index;

    public static void init(GameController controller) {
        gc = controller;
        map = gc.startingMap(Planet.Mars);
        W = (int)map.getWidth();
        H = (int)map.getHeight();
        visited = new boolean[W][H];
        for(int x1 = 0; x1<W; x1++) {
        	 for(int y1 = 0; y1<H; y1++) {
         		visited[x1][y1] = false;
             }
        }
        locations = new ArrayList<ArrayList <MapLocation> >();
        floodfill(); 
    }

    private static ArrayList<MapLocation> bfs(int x, int y) {
    	 	LinkedList<MapLocation> queue = new LinkedList<MapLocation>();
    	 	ArrayList<MapLocation> ans = new ArrayList<MapLocation>();
         visited[x][y] = true;
         queue.add(new MapLocation(Planet.Mars, x, y));
         ans.add(new MapLocation(Planet.Mars, x, y));
         // Run until queue is empty
         while (!queue.isEmpty()) {
             MapLocation location = queue.poll();
             for (int i = 0; i < 8; i++) {
                 int a = location.getX() + Pathing.move[i][0];
                 int b = location.getY() + Pathing.move[i][1];
                 MapLocation temp = new MapLocation(Planet.Mars, a, b);
                 if (map.onMap(temp) && map.isPassableTerrainAt(temp) == 1 && !visited[a][b]) {
                     visited[a][b] = true;
                     queue.add(new MapLocation(Planet.Mars, a, b));
                     ans.add(new MapLocation(Planet.Mars, a, b));
                 }
             }
         }
         return ans;
    }

    private static void floodfill() {
        // Setup the visited grid

        // Go through points, bfs
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                if (map.isPassableTerrainAt(new MapLocation(Planet.Mars, x, y)) == 1 && !visited[x][y]) {
                		locations.add(bfs(x, y));
                }
            }
        }
        Collections.sort(locations, new Comparator<ArrayList<MapLocation> >() {
            public int compare(ArrayList<MapLocation> o1, ArrayList<MapLocation> o2) {
                return o2.size()-o1.size();
            }
        });
        System.out.println("HI MY NAMER");
        for(int x= 0; x<locations.get(0).size(); x++){
        	System.out.println(locations.get(0).get(x));
        }
    }
}
