import bc.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Pathing {

    // Movements correspond from N -> NE... -> W -> SW.
    private static int move[][] = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };
    private static GameController gc;
    private static PlanetMap map;
    private static int H, W;
    public static final int DIAGONAL_COST = 10;
    public static final int V_H_COST = 10;
    
    static class Cell{  
        int heuristicCost = 0; //Heuristic cost
        int finalCost = 0; //G+H
        int i, j;
        Cell parent; 
        
        Cell(int i, int j){
            this.i = i;
            this.j = j; 
        }
        
        @Override
        public String toString(){
            return "["+this.i+", "+this.j+"]";
        }
    }

    public static void init(GameController controller) {

        // Get game controller
        gc = controller;

        // Get map constraints
        System.out.println("Initializing pathing directions!");
        map = gc.startingMap(Planet.Earth);
        W = (int)map.getWidth();
        H = (int)map.getHeight();
    }
static Cell [][] grid = new Cell[5][5];
    
    static PriorityQueue<Cell> open;
     
    static boolean closed[][];
    static int startI, startJ;
    static int endI, endJ;
            
    
    public static void setStartCell(int i, int j){
        startI = i;
        startJ = j;
    }
    
    public static void setEndCell(int i, int j){
        endI = i;
        endJ = j; 
    }
    
    static void checkAndUpdateCost(Cell current, Cell t, int cost, MapLocation temp, Unit unit, MapLocation start){
    		if (map.onMap(temp) && map.isPassableTerrainAt(temp) == 1 && !closed[t.i][t.j] && (!temp.isWithinRange(unit.visionRange(), start) || !gc.hasUnitAtLocation(temp)) ) {
	        int t_final_cost = t.heuristicCost+cost;
	        
	        boolean inOpen = open.contains(t);
	        if(!inOpen || t_final_cost<t.finalCost){
	            t.finalCost = t_final_cost;
	            t.parent = current;
	            if(!inOpen)open.add(t);
	        }
    		}
    }
    public static void AStar(MapLocation start, MapLocation end,Unit unit){ 
        //add the start location to open list.
        open.add(grid[startI][startJ]);
        
        Cell current;
        Planet planet = start.getPlanet();
        while(true){ 
            current = open.poll();
            if(current==null) break;
            closed[current.i][current.j]=true; 

            if(current.equals(grid[endI][endJ])){
                return; 
            } 

            Cell t;  
            MapLocation temp;
            if(current.i-1>=0){
            		temp = new MapLocation(planet, current.i-1, current.j);
                t = grid[current.i-1][current.j];
                checkAndUpdateCost(current, t, current.finalCost+V_H_COST,temp,unit,start); 

                if(current.j-1>=0){
                		temp = new MapLocation(planet, current.i-1, current.j-1);
                    t = grid[current.i-1][current.j-1];
                    checkAndUpdateCost(current, t, current.finalCost+DIAGONAL_COST,temp,unit,start); 
                }

                if(current.j+1<grid[0].length){
                		temp = new MapLocation(planet, current.i-1, current.j+1);
                    t = grid[current.i-1][current.j+1];
                    checkAndUpdateCost(current, t, current.finalCost+DIAGONAL_COST,temp,unit,start); 
                }
            } 

            if(current.j-1>=0){
            		temp = new MapLocation(planet, current.i, current.j-1);
                t = grid[current.i][current.j-1];
                checkAndUpdateCost(current, t, current.finalCost+V_H_COST,temp,unit,start); 
            }

            if(current.j+1<grid[0].length){
            		temp = new MapLocation(planet, current.i, current.j+1);
                t = grid[current.i][current.j+1];
                checkAndUpdateCost(current, t, current.finalCost+V_H_COST,temp,unit,start); 
            }

            if(current.i+1<grid.length){
            		temp = new MapLocation(planet, current.i+1, current.j);
                t = grid[current.i+1][current.j];
                checkAndUpdateCost(current, t, current.finalCost+V_H_COST,temp,unit,start); 

                if(current.j-1>=0){
                		temp = new MapLocation(planet, current.i+1, current.j-1);
                    t = grid[current.i+1][current.j-1];
                    checkAndUpdateCost(current, t, current.finalCost+DIAGONAL_COST,temp,unit,start); 
                }
                
                if(current.j+1<grid[0].length){
                		temp = new MapLocation(planet, current.i+1, current.j+1);
                		t = grid[current.i+1][current.j+1];
                		checkAndUpdateCost(current, t, current.finalCost+DIAGONAL_COST,temp,unit,start); 
                }  
            }
        } 
    }
    public static ArrayList<MapLocation> path(Unit TroopUnit, MapLocation start, MapLocation end){
    		int x = W;
    		int y = H;
         //Reset
        Planet planet = start.getPlanet();
        int si=start.getX(), sj=start.getY();
        int ei=end.getX(), ej=end.getY();
        
        //System.out.println("dkhfjbskhfbdsih:"+ start);
        grid = new Cell[x][y];
        closed = new boolean[x][y];
        open = new PriorityQueue<>((Object o1, Object o2) -> {
             Cell c1 = (Cell)o1;
             Cell c2 = (Cell)o2;

             return c1.finalCost<c2.finalCost?-1:
                     c1.finalCost>c2.finalCost?1:0;
         });
        //Set start position
        setStartCell(si, sj);  //Setting to 0,0 by default. Will be useful for the UI part
        
        //Set End Location
        setEndCell(ei, ej); 
        
        //generate heuristics
        for(int i=0;i<x;++i){
           for(int j=0;j<y;++j){
               grid[i][j] = new Cell(i, j);
               grid[i][j].heuristicCost = Math.abs(i-endI)+Math.abs(j-endJ);
           }
        }
        grid[si][sj].finalCost = 0;
        AStar(start,end, TroopUnit); 
        ArrayList<MapLocation> ans = new ArrayList<MapLocation>();
        if(closed[endI][endJ]){
            //Trace back the path 
             System.out.println("Path: ");
             Cell current = grid[endI][endJ];
             System.out.print(current);
             ans.add(new MapLocation(planet,current.i,current.j));
             while(current.parent!=null){
            	 		ans.add(new MapLocation(planet,current.parent.i,current.parent.j));
            	 		System.out.print(current.parent + " <- ");
            			current = current.parent;
            			
             } 
             Collections.reverse(ans);
             ans.remove(0);
             return ans;
        }else return null;
    }
    
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
    	long t1 = System.currentTimeMillis();
		if(!gc.isMoveReady(TroopUnit.id())) { //check if unit can move
			return false;
		}
		if(TroopUnit.location().mapLocation().equals(end)) { //check if unit is at location
			return false;
		}
		//Critertion 1
		System.out.print("khdfbssbcashbdvahsbdfkvahsdjhfba: "+TroopUnit.location().mapLocation());
		if(!Player.unitpaths.containsKey(TroopUnit.id())) { 				//check if no previous path array
			Player.unitpaths.put(TroopUnit.id(), new Pathway(path(TroopUnit, TroopUnit.location().mapLocation(), end), end.clone()));
		}
		if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
			return false;
		}
		Pathway TroopPath = Player.unitpaths.get(TroopUnit.id());
		MapLocation next = TroopPath.getNextLocation();
		long t2 = System.currentTimeMillis();
        
        System.out.println("sjfdbskdbfksh     TIME:"+(t2-t1));
		
		//Criterion 2
		if(!end.equals(TroopPath.goal)) {
			Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit, TroopUnit.location().mapLocation(), end), end.clone());
			if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
				return false;
			}
		}
		
		//Criterion 3
		if(!gc.canMove(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next))) { 	//check if unit is in the way and unit is not in final location
			//might need to override later
			if(TroopPath.NextLocationIsEnd()) {
				return false;
			}
			else {
				Player.unitpaths.get(TroopUnit.id()).setNewPathway(path(TroopUnit, TroopUnit.location().mapLocation(), end), end.clone());
				if(Player.unitpaths.get(TroopUnit.id()).PathwayDoesNotExist()) {
					return false;
				}
			}
		}
		
		//Move unit
		//System.out.println("Want To Move: "+TroopPath.goal);
		if(gc.canMove(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next))){
			gc.moveRobot(TroopUnit.id(), TroopUnit.location().mapLocation().directionTo(next));
			TroopPath.index++; 
			return true;
		}
		return false;
    }

    public static void move(Unit unit, Direction direction) {
        if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), direction)) {
            gc.moveRobot(unit.id(), direction);
        } else {
            // System.out.println("Cannot move " + direction + "! " + unit.location().mapLocation());
        }
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

 
}
