import bc.*;

public class Player {

    private static GameController gc;
    private static ArrayList<MapLocation> pointsofinterest;

    public static void main(String[] args) {
        // Start game by connecting to game controller
        gc = new GameController();
        pointsofinterest = new  ArrayList<MapLocation>();
        /*
        Main runner for player, do not change.
         */
        boolean quit = false;
        while (!quit) {

            // Debug, print current round
            System.out.println("Current round: " + gc.round());

            // Get units and moves them
            UpdatePointsOfInterest();
            move();
            attack();
            abilities();
            // Complete round, move on to next one
            gc.nextTurn();
        }
    }

    /*
    Initializes research path.
    The total number of turns should be equal to ~1000.
    Ideally, this would be dependent on the situation.
     */
    
    public static void UpdatePointsOfInterest() {
    		//BFS here to find width of areas?
    		//look at enemy troops?
    		//arbitrary or random choice?
    }
    
    public static void move() {
    		//Analyze points of interest to move the players 
    		//The points of interest for workers will probably be different than those of the battle troops
    	 VecUnit units = gc.myUnits();
         for (int i = 0; i < units.size(); i++) {
             Unit unit = units.get(i);
             switch (unit.unitType()) {
                 case Worker:
                     Worker.run(unit, gc);
                     break;
                 case Knight:
                     Knight.run(unit, gc);
                     break;
                 case Ranger:
                     Ranger.run(unit, gc);
                     break;
                 case Mage:
                     Mage.run(unit, gc);
                     break;
                 case Healer:
                     Healer.run(unit, gc);
                     break;
             }
         }
    }
    public static void attack() {
    	
    }

    public static void abilities() {
    
    }

    public static void initResearch() {
        gc.queueResearch(UnitType.Worker);  // 25
        gc.queueResearch(UnitType.Knight);  // 25
        gc.queueResearch(UnitType.Ranger);  // 25
        gc.queueResearch(UnitType.Rocket);  // 100 <- Enables us to send troops to Mars
    }
}