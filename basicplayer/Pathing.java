import bc.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

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
    private static boolean visited[][];

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
    private static ArrayList<MapLocation> path(Unit unit, MapLocation start, MapLocation end) {

        // Initialize direction grid
        // System.out.println("Running pathing! " + start + " to " + end);
        Planet planet = start.getPlanet();
        prev = new int[W][H];
        int x=start.getX(), y=start.getY();

        // Run BFS from start node
        LinkedList<MapLocation> queue = new LinkedList<>();
        boolean[][] visited = new boolean[W][H];
        for(int x1 = 0; x1<W; x1++) {
        	 for(int y1 = 0; y1<H; y1++) {
         		visited[x1][y1] = false;
             }
        }
        visited[x][y] = true;
        queue.add(new MapLocation(planet, x, y));
        System.out.println(start.getX()+" "+start.getY()+" "+end.getX()+" "+end.getY());
        // Run until queue is empty
        boolean found = false;
        boolean ReachedEnd = false;
        while (!queue.isEmpty() && ReachedEnd == false) {
            MapLocation location = queue.poll();
            if(location.getX() == end.getX() && location.getY() == end.getY()) {
            	 	ReachedEnd  = true;
            	 	break;
            }
            for (int i = 0; i < 8; i++) {
                int a = location.getX() + move[i][0];
                int b = location.getY() + move[i][1];
                MapLocation temp = new MapLocation(planet, a, b);
                if (map.onMap(temp) && map.isPassableTerrainAt(temp) == 1 && !visited[a][b]) {
                		// && (!temp.isWithinRange(unit.visionRange(), start) || !gc.hasUnitAtLocation(temp)) &&
                		prev[a][b] = i;
                    // System.out.println(prev[a][b] + " " + a + " " + b);
                    visited[a][b] = true;
                    queue.add(new MapLocation(planet, a, b));
                }
            }
        }
        System.out.println(ReachedEnd);
        /*
        Use this to debug.
        for (int i = H-1; i > 0; i--) {
            for (int j = 0; j < W; j++) {
                System.out.print(prev[j][i]);
            }
            System.out.println();
        }
        */

        // Go backwards from end point
        if(!ReachedEnd) {
        		return null;
        }
        MapLocation lastLoc = end.clone();
        ArrayList<MapLocation> ml = new ArrayList<MapLocation>();
        while (!end.equals(start)) {
            // Subtract direction, NOT add
            int a=end.getX(), b=end.getY();
            lastLoc.setX(a);
            lastLoc.setY(b);
          //  System.out.println(end.getX()+" "+end.getY()+" "+prev[end.getX()][end.getY()]);
            end.setX(a-move[prev[a][b]][0]);
            end.setY(b-move[prev[a][b]][1]);
            ml.add(lastLoc.clone());
            // System.out.println("Subtracting " + prev[a][b] + " from " + lastLoc + " forms " + end);
        }
        Collections.reverse(ml);
        // System.out.println(dir);
        return ml.size() == 0?null:ml;
    }
    
 //        return ml.size() == 0?null:ml;
//    !gc.hasUnitAtLocation(new MapLocation(planet, a, b)

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
    Criterion for (re)calculating path:
     	1. If the unit was just made
     	2. If the goal changed
     	3. If unit is blocking the goal
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
		//Critertion 1
		if(!Player.unitpaths.containsKey(TroopUnit.id())) { 				//check if no previous path array
			Player.unitpaths.put(TroopUnit.id(), new Pathway(path(TroopUnit, TroopUnit.location().mapLocation(), end.clone()), end.clone()));
		}
		if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
			return false;
		}
		Pathway TroopPath = Player.unitpaths.get(TroopUnit.id());

		//Criterion 2
		if(!end.equals(TroopPath.goal)) {
			Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit, TroopUnit.location().mapLocation(), end.clone()), end.clone());
			if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
				return false;
			}
		}
		MapLocation next = TroopPath.getNextLocation();
		//Criterion 3
		if(!gc.canMove(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next))) { 	//check if unit is in the way and unit is not in final location
			//might need to override later
			if(TroopPath.NextLocationIsEnd()) {
				return false;
			}
			else {
				Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit, TroopUnit.location().mapLocation(), end.clone()), end.clone());
				if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
					return false;
				}
			}
		}
		
		//Move unit
		if(gc.canMove(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next))){
			gc.moveRobot(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next));
			TroopPath.index++; 
		}
		return true;
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
        move(unit, DirectionToMapLocation(unit, direction));
    }

    public static void tryMove(Unit unit, Direction direction) {

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
        VecUnit enemies = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), unit.visionRange(), TeamUtil.enemyTeam());
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
