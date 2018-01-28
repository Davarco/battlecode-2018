import bc.*;

import java.util.*;

public class Pathing {

    // Movements correspond from N -> NE... -> W -> SW.
    public static int move[][] = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };
    public static int H, W;
    public static HashMap<MapLocation, List<MapLocation>> stored;
    private static GameController gc;
    private static PlanetMap map;
    private static MapLocation start, dest;
    private static Planet planet;

    private static class Cell {
        int pX, pY;
        double f, g, h;
    }

    private static boolean isValid(MapLocation loc) {
        int x = loc.getX();
        int y = loc.getY();
        return (x >= 0) && (y >= 0) && (x < W) && (y < H);
    }

    private static boolean isUnblocked(Unit unit, MapLocation loc) {
        MapLocation start = unit.location().mapLocation();
        if (loc.equals(start) || loc.equals(dest))
            return true;
        return (map.onMap(loc) && map.isPassableTerrainAt(loc) == 1);
    }

    private static boolean isDestination(MapLocation loc) {
        return loc.equals(dest);
    }

    private static double heuristic(MapLocation loc) {
        return Math.sqrt(loc.distanceSquaredTo(dest));
    }

    private static Direction traverse(Cell details[][]) {
        int x = dest.getX();
        int y = dest.getY();
        Stack<Pair> path = new Stack<>();
        while (!(details[x][y].pX == x && details[x][y].pY == y)) {
            path.add(new Pair(x, y));
            int tx = details[x][y].pX;
            int ty = details[x][y].pY;
            x = tx;
            y = ty;
        }

        path.add(new Pair(x, y));
        Pair p1 = path.pop();
        Pair p2 = path.pop();
        return new MapLocation(planet, (Integer)p1.left, (Integer)p1.right).directionTo(new MapLocation(planet, (Integer)p2.left, (Integer)p2.right));
    }

    private static void reverse(Cell details[][]) {
        // IS BROKEN, DON'T USE
        int x = start.getX();
        int y = start.getY();
        System.out.println("-> ["  + x + ", " + y + "] ");
        while (x != dest.getX() && y != dest.getY()) {
            int tx = x;
            int ty = y;
            x = details[tx][ty].pX;
            y = details[tx][ty].pY;
        }
    }

    public static Direction astar(Unit unit, MapLocation end) {

        // Set start and dest
        // SWITCH THEM SO WE DON'T HAVE TO TRAVERSE <- NEVER MIND
        start = unit.location().mapLocation();
        dest = end;

        // Return if begin is equal to start
        if (start.equals(dest)) return null;

        // Set closed list
        boolean closedList[][] = new boolean[W][H];

        // Set initial grid
        Cell cellDetails[][] = new Cell[W][H];
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                cellDetails[x][y] = new Cell();
                cellDetails[x][y].f = Double.MAX_VALUE;
                cellDetails[x][y].g = Double.MAX_VALUE;
                cellDetails[x][y].h = Double.MAX_VALUE;
                cellDetails[x][y].pX = -1;
                cellDetails[x][y].pY = -1;
            }
        }

        // Set starting node
        int startx = start.getX(), starty = start.getY();
        cellDetails[startx][starty].f = 0;
        cellDetails[startx][starty].g = 0;
        cellDetails[startx][starty].h = 0;
        cellDetails[startx][starty].pX = startx;
        cellDetails[startx][starty].pY = starty;

        // Initialize open list
        PriorityQueue<Pair<Double, Pair<Integer, Integer>>> openList = new PriorityQueue<>(new Comparator<Pair<Double, Pair<Integer, Integer>>>() {
            @Override
            public int compare(Pair<Double, Pair<Integer, Integer>> a, Pair<Double, Pair<Integer, Integer>> b) {
                if (a.left < b.left) return -1;
                if (a.left > b.left) return 1;
                return 0;
            }
        });
        openList.add(new Pair<>(0.0, new Pair<>(startx, starty)));

        // Continue until list is empty
        while (!openList.isEmpty()) {

            // Get lowest F score
            Pair<Double, Pair<Integer, Integer>> p = openList.poll();

            // Add to closed list
            int x = p.right.left;
            int y = p.right.right;
            closedList[x][y] = false;

            // Store new g, h, and f values
            double gNew, hNew, fNew;

            // Go through all the possible directions
            for (int i = 0; i < move.length; i++) {

                // Get new location
                int xNew = x+move[i][0], yNew = y+move[i][1];
                MapLocation temp = new MapLocation(planet, xNew, yNew);

                // Make sure movement is valid
                if (isValid(temp)) {

                    // Check if the destination cell is equal to the successor
                    if (isDestination(temp)) {

                        // Set parent of the destination cell
                        cellDetails[xNew][yNew].pX = x;
                        cellDetails[xNew][yNew].pY = y;
                        // System.out.println("Found destination.");
                        return traverse(cellDetails);
                    }

                    // Ignore if the successor is already on the closed list or blocked
                    else if (!closedList[xNew][yNew] && isUnblocked(unit, temp)) {

                        // Recalculate values
                        gNew = cellDetails[x][y].g + 1.0;
                        hNew = heuristic(temp);
                        fNew = gNew + hNew;

                        // Add to open list if it isn't already
                        // Mark the parent square and new calculated values
                        if (cellDetails[xNew][yNew].f == Double.MAX_VALUE || cellDetails[xNew][yNew].f > fNew) {
                            openList.add(new Pair<>(fNew, new Pair<>(xNew, yNew)));

                            // Update cell details
                            cellDetails[xNew][yNew].f = fNew;
                            cellDetails[xNew][yNew].g = gNew;
                            cellDetails[xNew][yNew].h = hNew;
                            cellDetails[xNew][yNew].pX = x;
                            cellDetails[xNew][yNew].pY = y;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static void init(GameController controller) {

        // Get game controller
        gc = controller;

        // Get map constraints
        System.out.println("Initializing pathing directions!");
        planet = gc.planet();
        map = gc.startingMap(planet);
        W = (int) map.getWidth();
        H = (int) map.getHeight();
    }

    public static void reset() {
        stored = new HashMap<>();
    }

    public static Direction opposite(Direction direction) {
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

    public static boolean move(Unit unit, MapLocation end) {
        Direction dir = astar(unit, end);
        stored.putIfAbsent(end, new ArrayList<>());
        stored.get(end).add(start);
        long t2 = System.currentTimeMillis();
        //System.out.println((t2 - t1) + " " + unit.location().mapLocation() + " to " + end + " round " + gc.round() + " " + dir);

        // Move unit
        return move(unit, dir);
    }

    public static boolean move(Unit unit, Direction direction) {
        if (direction == null) return false;
        if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), direction)) {
            gc.moveRobot(unit.id(), direction);
            return true;
        }

        return false;
    }

    public static boolean tryMove(Unit unit, Direction direction) {

        // Get idx of direction
        int idx = -1;
        int length = Direction.values().length;
        for (int i = 0; i < length; i++) {
            if (Direction.values()[i].equals(direction)) {
                idx = i;
                break;
            }
        }

        // Set left and right, search circularly
        int left = idx, right = idx;
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
                left = length - 1;
            } else {
                left -= 1;
            }
            if (right == length - 1) {
                right = 0;
            } else {
                right += 1;
            }
        }

        // Don't move if no idx was found, otherwise move in the best direction
        if (fin == -1) {
            // System.out.println("Error: " + unit.location().mapLocation() + " is stuck!");
            return false;
        }

        move(unit, Direction.values()[fin]);
        return true;
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
        long maxDist = -Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).unitType() == UnitType.Worker || enemies.get(i).unitType() == UnitType.Rocket || enemies.get(i).unitType() == UnitType.Factory)
                continue;
            long dist = enemies.get(i).attackRange() - ourLoc.distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist > maxDist) {
                maxDist = dist;
                idx = i;
            }
        }

        // They aren't in range of shooting us, continue
        if (maxDist <= 0)
            return false;

        // Get opposite direction
        Direction opposite = opposite(ourLoc.directionTo(enemies.get(idx).location().mapLocation()));
        tryMove(unit, opposite);
        return true;
    }

    private static class Pair<L, R> {

        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() {
            return left;
        }

        public R getRight() {
            return right;
        }

        @Override
        public int hashCode() {
            return left.hashCode() ^ right.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair pairo = (Pair) o;
            return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
        }
    }
    public static MapLocation DirectionToMapLocation(Unit unit, Direction direction) {
        switch (direction) {
            case North:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX(), unit.location().mapLocation().getY()+1);
            case Northeast:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX()+1, unit.location().mapLocation().getY()+1);
            case East:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX()+1, unit.location().mapLocation().getY());
            case Southeast:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX()+1, unit.location().mapLocation().getY()-1);
            case South:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX(), unit.location().mapLocation().getY()-1);
            case Southwest:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX()-1, unit.location().mapLocation().getY()-1);
            case West:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX()-1, unit.location().mapLocation().getY());
            case Northwest:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX()-1, unit.location().mapLocation().getY()+1);
            case Center:
                return new MapLocation(unit.location().mapLocation().getPlanet(), unit.location().mapLocation().getX(), unit.location().mapLocation().getY());
        }
        return null;
}
}
