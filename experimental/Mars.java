import bc.*;
import java.util.*;


public class Mars {

    private static GameController gc;
    private static PlanetMap map;
    public static ArrayList<ArrayList<MapLocation>> locations;
    private static boolean visited[][];
    private static int W, H;
    private static int index;
    public static int earthplaces[][];
    public static int marsplaces[][];
    public static ArrayList<ArrayList<MapLocation> > karboniteplacesEarth;
    public static ArrayList<ArrayList<MapLocation> > karboniteplacesMars;
    public static ArrayList<MapLocation> firstworkerEarth;
 	public static LinkedList<MapLocation> Earthqueue = new LinkedList<MapLocation>();
 	public static LinkedList<Integer> Earthqueueindex = new LinkedList<Integer>();
	public static LinkedList<MapLocation> Marsqueue = new LinkedList<MapLocation>();
 	public static LinkedList<Integer> Marsqueueindex = new LinkedList<Integer>();


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
        floodfillLocationsMars(); 
        if(gc.planet().equals(Planet.Mars)) {
            for(int x1 = 0; x1<W; x1++) {
              	 for(int y1 = 0; y1<H; y1++) {
               		visited[x1][y1] = false;
                   }
               }
        		marsplaces = new int[W][H];
             karboniteplacesMars= new ArrayList<ArrayList<MapLocation> >();
        		floodfillMars();
        }
        else if(gc.planet().equals(Planet.Earth)) {
        firstworkerEarth = new ArrayList<MapLocation>();
        karboniteplacesEarth = new ArrayList<ArrayList<MapLocation> >();
        map = gc.startingMap(Planet.Earth);
        W = (int)map.getWidth();
        H = (int)map.getHeight();
        earthplaces = new int[W][H];
        visited = new boolean[W][H];
        for(int x1 = 0; x1<W; x1++) {
        	 for(int y1 = 0; y1<H; y1++) {
         		visited[x1][y1] = false;
             }
        }
        index = 0;
        floodfillEarth(); 
        }
    }

    private static ArrayList<MapLocation> bfs(int x, int y, Planet p) {
    	 	LinkedList<MapLocation> queue = new LinkedList<MapLocation>();
    	 	ArrayList<MapLocation> ans = new ArrayList<MapLocation>();
         visited[x][y] = true;
         queue.add(new MapLocation(p, x, y));
         ans.add(new MapLocation(p, x, y));
         // Run until queue is empty
         while (!queue.isEmpty()) {
             MapLocation location = queue.poll();
             for (int i = 0; i < 8; i++) {
                 int a = location.getX() + EarthPathing.move[i][0];
                 int b = location.getY() + EarthPathing.move[i][1];
                 MapLocation temp = new MapLocation(p, a, b);
                 if (map.onMap(temp) && map.isPassableTerrainAt(temp) == 1 && !visited[a][b]) {
                     visited[a][b] = true;
                     queue.add(new MapLocation(p, a, b));
                     ans.add(new MapLocation(p, a, b));
                 }
             }
         }
         return ans;
    }
    
    private static void bfsMars(Planet p) {
     // Run until queue is empty
     while (!Marsqueue.isEmpty()) {
         MapLocation location = Marsqueue.poll();
         int idx = Marsqueueindex.poll();
    	// System.out.println(location);
         if(map.initialKarboniteAt(location) != 0) { //change?
             karboniteplacesMars.get(idx).add(location.clone());
         }
         marsplaces[location.getX()][location.getY()] = idx;
         for (int i = 0; i < 8; i++) {
             int a = location.getX() + EarthPathing.move[i][0];
             int b = location.getY() + EarthPathing.move[i][1];
             MapLocation temp = new MapLocation(p, a, b);
             if (map.onMap(temp) && map.isPassableTerrainAt(temp) == 1 && !visited[a][b]) {
                 visited[a][b] = true;
                 Marsqueue.add(temp.clone());
                 Marsqueueindex.add(idx);
             }
         }
     }
    }
    
    private static void bfsEarth(Planet p) {
        // Run until queue is empty
        while (!Earthqueue.isEmpty()) {
            MapLocation location = Earthqueue.poll();
            int idx = Earthqueueindex.poll();
       	// System.out.println(location);
            if(map.initialKarboniteAt(location) != 0) { //change?
                karboniteplacesEarth.get(idx).add(location.clone());
            }
            earthplaces[location.getX()][location.getY()] = idx;
            for (int i = 0; i < 8; i++) {
                int a = location.getX() + EarthPathing.move[i][0];
                int b = location.getY() + EarthPathing.move[i][1];
                MapLocation temp = new MapLocation(p, a, b);
                if (map.onMap(temp) && map.isPassableTerrainAt(temp) == 1 && !visited[a][b]) {
                    visited[a][b] = true;
                    Earthqueue.add(temp.clone());
                    Earthqueueindex.add(idx);
                }
            }
        }
       }

    private static void floodfillLocationsMars() {
        // Setup the visited grid

        // Go through points, bfs
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                if (map.isPassableTerrainAt(new MapLocation(Planet.Mars, x, y)) == 1 && !visited[x][y]) {
                		locations.add(bfs(x, y, Planet.Mars));
                }
            }
        }
        Collections.sort(locations, new Comparator<ArrayList<MapLocation> >() {
            public int compare(ArrayList<MapLocation> o1, ArrayList<MapLocation> o2) {
                return o2.size()-o1.size();
            }
        });
        int min = 1<<30;
        for(int x = 0; x<locations.size();x++){
        	min = locations.get(x).size()<min?locations.get(x).size():min;
        }
        for(int x = 0; x<locations.size();x++){
        	Rocket.ratio.add((int)(locations.get(x).size()/min));
        	Rocket.ratiocount+=Rocket.ratio.get(x);
        }
        Rocket.orgratio = new ArrayList<>(Rocket.ratio);
        Rocket.orgratiocount = Rocket.ratiocount;
    }
    
    private static void floodfillEarth() {
        // Setup the visited grid

        // Go through points, bfs
    		for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
            	MapLocation temp = new MapLocation(Planet.Earth, x, y);
            	 if (gc.hasUnitAtLocation(temp) && map.isPassableTerrainAt(temp) == 1 && !visited[temp.getX()][temp.getY()]) {
            		 	//System.out.println(temp);
            		 	Earthqueue.add(temp);
            		 	Earthqueueindex.add(index);
            			karboniteplacesEarth.add(new ArrayList<MapLocation>());
            			Worker.skipIndex.put(index, false);
                		bfsEarth(Planet.Earth);
                		Earthqueue.clear();
            		 	Earthqueueindex.clear();
            		 	index++;
                }
            }
        }
    }
    
    private static void floodfillMars() {
        // Setup the visited grid

        // Go through points, bfs
    		index = 0;
	    	for(int x = 0; x<locations.size(); x++) {
	    		Marsqueue.add(new MapLocation(Planet.Mars, locations.get(x).get(0).getX(), locations.get(x).get(0).getY()));
    		 	Marsqueueindex.add(index);
    			karboniteplacesMars.add(new ArrayList<MapLocation>());
    			Worker.skipIndex.put(index, false);
    		 	index++;
	    	}
    		bfsMars(Planet.Mars);
    		//System.out.println("******************* "+karboniteplacesMars.get(0).size()+" "+karboniteplacesMars.get(1).size());
    }
}