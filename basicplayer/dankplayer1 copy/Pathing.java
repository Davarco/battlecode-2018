import bc.*;

import java.util.*;

public class Pathing {

    public static int move[][] = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };

    private static class Cell {
        public double g = 0;
        public double h = 0;
        public int x;
        public int y;
        public Cell parent;
        public boolean open = false;
        public boolean closed = false;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public Cell() {
            this.x = 0;
            this.y = 0;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) return true;
            if (!(other instanceof Cell)) return false;
            Cell other_cell = (Cell) other;
            return(this.x == other_cell.x && this.x == other_cell.y);
        }

        public MapLocation getMapLocation(Planet p) {
            return new MapLocation(p, this.x, this.y);
        }

        @Override
        public String toString() {
            return ("Cell at ("+x+","+y+"), g is "+g+ ", h is " + h);
        }
    }

    private static PlanetMap pm;
    private static GameController gc;
    private static int height;
    private static int width;
    private static Cell[][] mapNodes;
    private static boolean[][] terrain;


    public static void init(GameController _gc) {
        gc = _gc;
        pm = gc.startingMap(Planet.Earth);
        height = (int) pm.getHeight();
        width = (int) pm.getWidth();
        terrain = new boolean[width][height];
        initTerrain(); // Copies the terrain map from PlanetMap to avoid making hella API calls
    }

    private static void initTerrain() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                MapLocation loc = new MapLocation(Planet.Earth, i, j);
                terrain[i][j] = pm.isPassableTerrainAt(loc) == 1;
            }
        }
    }

    private static List<Cell> getNeighbors(Cell cell) {
        if (cell.parent == null) {
            return getGridNeighbors(cell,true);
        } else {
            List<Cell> neighbors = new ArrayList<>();
            int x = cell.x;
            int y = cell.y;
            int dx = (x - cell.parent.x)/Math.max(Math.abs(x - cell.parent.x), 1);
            int dy = (y - cell.parent.y)/Math.max(Math.abs(y - cell.parent.y), 1);
//            System.out.println("dx: " + dx);
//            System.out.println("dy: " + dy);

            // Implements diagonal move
            if (dx != 0 && dy != 0) {
                boolean moveX = false, moveY = false;
                if(occupiable(x, y+dy)) {
                    neighbors.add(mapNodes[x][y+dy]);
                    moveY = true;
                }
                if(occupiable(x+dx, y)) {
                    neighbors.add(mapNodes[x+dx][y]);
                    moveX = true;
                }
                if(moveX || moveY)
                    if (inBounds(x+dx, y+dy))
                        neighbors.add(mapNodes[x+dx][y+dy]);

                if (!occupiable(x-dx, y) && moveY)
                    neighbors.add(mapNodes[x-dx][y+dy]);

                if (!occupiable(x, y-dy) && moveX)
                    neighbors.add(mapNodes[x+dx][y-dy]);
            } else {
                // Moving along y-axis...
                if (dx == 0) {
                    if (occupiable(x, y+dy)) {
                        neighbors.add(mapNodes[x][y+dy]);
                        if (!occupiable(x+1, y) && inBounds(x+1, y+dy))
                            neighbors.add(mapNodes[x+1][y+dy]);

                        if (!occupiable(x-1, y) && inBounds(x-1, y+dy))
                            neighbors.add(mapNodes[x-1][y+dy]);
                    }
                    // Moving along x-axis...
                } else {
                    if (occupiable(x+dx, y)) {
                        neighbors.add(mapNodes[x+dx][y]);
                        if (!occupiable(x, y+1) && inBounds(x+dx, y+1))
                            neighbors.add(mapNodes[x+dx][y+1]);

                        if(!occupiable(x, y-1) && inBounds(x+dx, y-1)) {
                            neighbors.add(mapNodes[x+dx][y-1]);
                        }
                    }
                }
            }
            return neighbors;
        }

    }

    private static Cell jump(Cell node, Cell parent, Cell target) {

        if (node == null) return null;

        int x = node.x;
        int y = node.y;
        int dx = x - parent.x;
        int dy = y - parent.y;

//        System.out.println("nodex " + x);
//        System.out.println("nodey " + y);
//        System.out.println("dx: " + dx);
//        System.out.println("dy: " + dy);

        if (!occupiable(x, y)) return null;

        if (node.equals(target)) return node;

        if (dx !=0 && dy != 0) {
            if ((occupiable(x-dx, y+dy) && !occupiable(x-dx, y)) || (occupiable(x+dx, y-dy) && !occupiable(x, y-dy))) {
//                System.out.println("returning pt 1");
                return node;
            }

        } else {
            if (dx != 0) {
                if ((occupiable(x+dx, y+1) && !occupiable(x, y+1)) || (occupiable(x+dx, y-1) && !occupiable(x, y-1))) {
//                    System.out.println("returning pt 2");
                    return node;
                }
            } else {
                if ((occupiable(x + 1, y + dy) && !occupiable(x + 1, y)) || (occupiable(x - 1, y + dy) && !occupiable(x - 1, y))) {
//                    System.out.println("returning pt 3");
                    return node;
                }
            }
        }

        if (dx != 0 && dy != 0) {
            if (inBounds(x+dx, y)) {
//                System.out.println("1 About to jump into a recursion into cell " + mapNodes[x+dx][y]);
                if (jump(mapNodes[x + dx][y], node, target) != null) {
//                    System.out.println("returning pt 4");
                    return node;
                }
            }
            if (inBounds(x, y+dy)) {
//                System.out.println("2 About to jump into a recursion into cell " + mapNodes[x][y+dy]);
                if (jump(mapNodes[x][y + dy], node, target) != null) {
//                    System.out.println("returning pt 5");
                    return node;
                }
            }
        }

        if (occupiable(x+dx, y) || occupiable(x, y+dy)) {
            if (inBounds(x+dx, y+dy)) {
//                System.out.println("3 About to jump into a diagonal into cell " + mapNodes[x+dx][y+dy]);
                return jump(mapNodes[x+dx][y+dy], node, target);
            }

        }

        //should never get here
        return null;
    }

    public static ArrayList<MapLocation> path(MapLocation start, MapLocation end) {

        PriorityQueue<Cell> open = new PriorityQueue<>((Object o1, Object o2) -> {
            Cell c1 = (Cell) o1;
            Cell c2 = (Cell) o2;
            return Double.compare(c1.g+c1.h, c2.g+c2.h);
        });

        mapNodes = new Cell[width][height];
        for(int x = 0; x < mapNodes.length; x++) {
            for(int y = 0; y < mapNodes[0].length; y++) {
                mapNodes[x][y] = new Cell();
                mapNodes[x][y].x = x;
                mapNodes[x][y].y = y;
            }
        }

        Cell startNode = mapNodes[start.getX()][start.getY()];

        Cell endNode = mapNodes[end.getX()][end.getY()];

//        System.out.println(startNode);
//        System.out.println(endNode);

        open.add(startNode);

        Cell current;

        while(open.size() != 0) {
            current = open.poll();
            current.closed = true;
            if (current.equals(endNode))
                break;
            List<Cell> neighbors = getNeighbors(current);
//            System.out.println(neighbors);
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                Cell next = neighbors.get(i);
                Cell jumpNode = jump(next, current, endNode);
                if (jumpNode != null) {
                    if (!jumpNode.closed) {
                        double addG = euclidean(current, next);
                        double g = current.g + addG;
                        if (!jumpNode.open || g < jumpNode.g) {
                            jumpNode.g = g;
                            jumpNode.h = manhattan(current, next);
                            jumpNode.parent = current;
                            if (!jumpNode.open) {
                                open.add(jumpNode);
                                jumpNode.open = true;
                            }
                        }
                    }
                }
            }
        }
        if(endNode.closed) {
            ArrayList<MapLocation> path = new ArrayList<MapLocation>();
            current = endNode;
            path.add(current.getMapLocation(Planet.Earth));
            while(current.parent != null) {
                path.add(current.parent.getMapLocation(Planet.Earth));
                current = current.parent;
            }
            Collections.reverse(path);
            path.remove(0);
            return path;
        } else {
            return null;
        }
    }

    private static boolean occupiable(int x, int y) {
        return (inBounds(x, y) && terrain[x][y]);
    }
    private static boolean inBounds(int x, int y) {
        return (0 <= x && width > x && 0 <= y && height > y);
    }

    private static List<Cell> getGridNeighbors (Cell cell, boolean forcePassable) {
        List<Cell> neighbors = new ArrayList<>();
        int x = cell.x; int y = cell.y;
        for (int[] movePair : move) {
            int modX = movePair[0] + x;
            int modY = movePair[1] + y;

            if (forcePassable) {
                if (occupiable(modX, modY)) {
                    neighbors.add(mapNodes[modX][modY]);
                }
            } else {
                if (inBounds(modX, modY)) {
                    neighbors.add(mapNodes[modX][modY]);
                }
            }
        }
        return neighbors;
    }

    private static double euclidean(Cell start, Cell end) {
        // Euclidean distance
        return Math.sqrt(Math.pow(start.x-end.x, 2) + Math.pow(start.y-end.y, 2));
    }

    private static int manhattan(Cell start, Cell end) {
        //Manhattan distance
        return Math.abs(start.x-end.x) + Math.abs(start.y-end.y);
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


    /*
    Criterion for (re)calculating path:
     	1. If the unit was just made
     	2. If the goal changed
     	3. If the 
     	4. If unit is blocking the goal
    If recalculated path is null given the update of the situation, then the unit cannot move (function returns)
    First, checks if the unit movement cooldown is up
    Next, checks if the unit is already at the location
    		If it is, then returns (mission accomplished)
    Criterion 1
    Next, checks if the unit has had a pathway
    	Creates one if not
    If pathway does not exist, then returns;
    	Criterion 2
    If goal changed from previous (for units with previous pathways)
    	Recalculate path
    	If recalculation is null, then returns (can't move)
    Criterion 3
    Next, checks if the next location to move is blocked
   	    If blocked and next location is final destination, doesn't recalculate
    		returns (because can't move)
    	If not, then recalculates path
    		If path is null then return
    Move unit
    True if move successful, false if not
       */


    public static boolean move(Unit TroopUnit, MapLocation end) {
        if(!gc.isMoveReady(TroopUnit.id())) { //check if unit can move
            return false;
        }
        if(TroopUnit.location().mapLocation().equals(end)) { //check if unit is at location
            return true;
        }
        boolean hasRecalculated = false;
        //Criterion 1
        if(!Player.unitpaths.containsKey(TroopUnit.id())) { 				//check if no previous path array
            Player.unitpaths.put(TroopUnit.id(), new Pathway(path(TroopUnit.location().mapLocation(), end.clone()), end.clone(), TroopUnit.location().mapLocation()));
            hasRecalculated = true;
        }
        if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
            return false;
        }
        Pathway TroopPath = Player.unitpaths.get(TroopUnit.id());

        //Criterion 2
        if(!end.equals(TroopPath.goal)) {
            Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit.location().mapLocation(), end.clone()), end.clone(), TroopUnit.location().mapLocation());
            hasRecalculated = true;
            if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
                return false;
            }
        }
        
        //Criterion 3
        if(TroopUnit.location().mapLocation()!=Player.unitpaths.get(TroopUnit.id()).start) {
        		if(!hasRecalculated) {
        			Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit.location().mapLocation(), end.clone()), end.clone(), TroopUnit.location().mapLocation());
        		}
        		if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
                return false;
            }
        }
        
        MapLocation next = TroopPath.getNextLocation();
        //Criterion 4
        if(!gc.canMove(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next))) { 	//check if unit is in the way and unit is not in final location
            //might need to override later
            if(TroopPath.NextLocationIsEnd()) {
                return false;
            }
            else {
                Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit.location().mapLocation(), end.clone()), end.clone(), TroopUnit.location().mapLocation());
                if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
                    return false;
                }
            }
        }

        //Move unit
        if(gc.canMove(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next))){
            gc.moveRobot(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next));
            TroopPath.index++;
            return true;
        }
        return false;
    }

    private static MapLocation DirectionToMapLocation(Unit unit, Direction direction) {
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
        }
        return null;
    }

    public static void move(Unit unit, Direction direction) {
        if(gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), direction)){
    			gc.moveRobot(unit.id(), direction);
        }
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
                left = length-1;
            } else {
                left -= 1;
            }
            if (right == length-1) {
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
}