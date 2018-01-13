import bc.*;
import java.util.*;

public class Pathing {

    // Movements correspond from N -> NE... -> W -> SW.
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
    private static ArrayList<MapLocation> path(MapLocation start, MapLocation end) {

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
        ArrayList<MapLocation> ml = new ArrayList<MapLocation>();
        while (!end.equals(start)) {
            // Subtract direction, NOT add
            lastLoc.setX(end.getX());
            lastLoc.setY(end.getY());
            end.setX(end.getX()-move[prev[end.getX()][end.getY()]][0]);
            end.setY(end.getY()-move[prev[end.getX()][end.getY()]][1]);
            ml.add(lastLoc);
            // System.out.println("Subtracting " + prev[a][b] + " from " + lastLoc + " forms " + end);
        }
        Collections.reverse(ml);
        // System.out.println(dir);
        return ml;
    }

    /*
    Gets the direction opposite of an input direction.
     */
    private static Direction opposite(Direction direction) {
        switch (direction) {
            case North:
                return Direction.South;
            case Northeast:
                return Direction.Southwest;
            case East:
                return Direction.West;
            case Southeast:
                return Direction.Northwest;
            case South:
                return Direction.North;
            case Southwest:
                return Direction.Northeast;
            case West:
                return Direction.East;
            case Northwest:
                return Direction.Southeast;
        }

        return null;
    }

    /*
    Actually moves the robot, but checks before moving.
     */
    public static void move(Unit TroopUnit, MapLocation end) {
    		if(TroopUnit.location().mapLocation().equals(end)) {
    			return;
    		}
    		if(!Player.unitpaths.containsKey(TroopUnit.id())) { 				//check if no previous path array
    			Player.unitpaths.put(TroopUnit.id(), new Pathway(path(TroopUnit.location().mapLocation(), end)));
    		}
    		else if(false) { 				//check if enemy is on the place
    			Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit.location().mapLocation(), end));
    		}
    		MapLocation next = Player.unitpaths.get(TroopUnit.id()).getNextLocation();
    		System.out.print("FROM (");
    		System.out.print(TroopUnit.location().mapLocation().getX());
    		System.out.print(", ");
    		System.out.print(TroopUnit.location().mapLocation().getY());
    		System.out.print(") to (");
    		System.out.print(next.getX());
    		System.out.print(", ");
    		System.out.print(next.getY());
    		System.out.print(") ");
    		if (gc.isMoveReady(TroopUnit.id()) && gc.canMove(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next))) {
    			System.out.println("HI");
    			gc.moveRobot(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next));
    		}
    		
    }

    public static void move(Unit unit, Direction direction) {
        if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), direction)) {
            gc.moveRobot(unit.id(), direction);
        }
    }

    public static void escapeMove(Unit unit, Direction direction) {

        // Get idx of direction
        int idx = -1;
        for (int i = 0; i < Direction.values().length; i++) {
            if (Direction.values()[i].equals(direction)) {
                idx = i;
                break;
            }
        }

        // Set left and right, search circularly
        int left=idx, right=idx;
        int fin = -1;
        for (int i = 0; i < 4; i++) {
            if (gc.canMove(unit.id(), Direction.values()[left])) {
                fin = left;
                break;
            }
            if (gc.canMove(unit.id(), Direction.values()[right])) {
                fin = right;
                break;
            }
            if (left == 0) {
                left = 7;
            } else {
                left -= 1;
            }
            if (right == 7) {
                right = 0;
            } else {
                right += 1;
            }
        }

        // Don't move if no idx was found, otherwise move in the best direction
        if (fin == -1) {
            System.out.println("Error: " + unit.location().mapLocation() + " is stuck!");
            return;
        }
        move(unit, Direction.values()[fin]);
    }

    /*
    Code that moves the robot away from the closest enemy.
    Best used for Worker, Healers, Low HP units.
     */
    public static boolean escape(Unit unit) {

        // Return false if no units are found
        VecUnit enemies = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), unit.visionRange(), Util.enemyTeam());
        if (enemies.size() == 0)
            return false;

        // Find the closest enemy
        MapLocation ourLoc = unit.location().mapLocation();
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            long dist = ourLoc.distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        // Get opposite direction
        Direction opposite = opposite(ourLoc.directionTo(enemies.get(idx).location().mapLocation()));
        escapeMove(unit, opposite);
        return true;
    }
}