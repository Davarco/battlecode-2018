import bc.*;

import java.util.LinkedList;

public class Pathing {

    /*
    Movements correspond from N -> NE... -> W -> SW.
     */
    private static int move[][] = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };

    private static GameController gc;
    private static PlanetMap map;
    private static int H, W;
    private static int prev[][];

    public static void init(GameController controller) {

        // Get game controller
        gc = controller;

        // Get map constraints
        System.out.println("Initializing pathing directions!");
        map = gc.startingMap(Planet.Earth);
        W = (int)map.getWidth();
        H = (int)map.getHeight();
    }

    /*
    Runs BFS to get the right direction for the robot to move from point A to B.
     */
    private static Direction path(MapLocation start, MapLocation end) {

        // Initialize direction grid
        // System.out.println("Running pathing! " + start + " to " + end);
        Planet planet = start.getPlanet();
        prev = new int[W][H];
        int x=start.getX(), y=start.getY();

        // Run BFS from start node
        LinkedList<MapLocation> queue = new LinkedList<>();
        boolean visited[][] = new boolean[W][H];
        visited[x][y] = true;
        queue.add(new MapLocation(planet, x, y));

        // Run until queue is empty
        while (!queue.isEmpty()) {
            MapLocation location = queue.poll();
            for (int i = 0; i < 8; i++) {
                int a = location.getX() + move[i][0];
                int b = location.getY() + move[i][1];
                MapLocation temp = new MapLocation(planet, a, b);
                if (map.onMap(temp) && map.isPassableTerrainAt(temp) == 1 && !visited[a][b]) {
                    prev[a][b] = i;
                    // System.out.println(prev[a][b] + " " + a + " " + b);
                    visited[a][b] = true;
                    queue.add(new MapLocation(planet, a, b));
                }
            }
        }

        // Go backwards from end point
        MapLocation lastLoc = end.clone();
        while (!end.equals(start)) {
            int a=end.getX(), b=end.getY();

            // Subtract direction, NOT add
            lastLoc.setX(end.getX());
            lastLoc.setY(end.getY());
            end.setX(end.getX()-move[prev[a][b]][0]);
            end.setY(end.getY()-move[prev[a][b]][1]);
            // System.out.println("Subtracting " + prev[a][b] + " from " + lastLoc + " forms " + end);
        }

        Direction dir = end.directionTo(lastLoc);
        // System.out.println(dir);
        return dir;
    }

    /*
    Actually moves the robot, but checks before moving.
     */
    public static void move(Unit unit, MapLocation start, MapLocation end) {
        Direction direction = path(start, end);
        if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), direction)) {
            gc.moveRobot(unit.id(), direction);
        }
    }
}
