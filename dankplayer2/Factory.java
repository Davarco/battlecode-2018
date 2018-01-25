import bc.*;

import java.util.HashMap;

public class Factory {

    private static Unit factory;
    private static GameController gc;

    public static void init(GameController controller) {
        gc = controller;
        
    }

    public static void run(Unit unit) {

        // Receive factory from main runner
        factory = unit;

        // Build best unit possible
        build();

        // Unload any units in queue
        unload();
    }

    /*
    private static void manageWorkerAssignments() {
        if (factory.health() < 150 && !Player.workerDestinations.containsKey(factory.id())) // Indicates it's still a blueprint
            Player.workerDestinations.put(factory.id(), Util.openSpacesAround(factory.location().mapLocation(), (int) Info.number(UnitType.Worker)/Info.number(UnitType.Factory)));

        if (factory.health() == factory.maxHealth()) {
            Player.workerDestinations.remove(factory.id());
            System.out.println("REMOVED DESTINATION FROM FACTORY< FREE TO GO!!!!!!!!!!");
        }

    }
    */

    private static void build() {

        // Workers are vital, build them if we have nothing left
    	if (Info.number(UnitType.Worker) < Info.number(UnitType.Factory)) {
            if (gc.canProduceRobot(factory.id(), UnitType.Worker)) {
                gc.produceRobot(factory.id(), UnitType.Worker);
                Info.addUnit(UnitType.Worker);
            }
        }

        // See if the factory can build the ranger
        if(Player.mapsize.equals("largemap")){
        	if(gc.round() > Config.ROCKET_CREATION_ROUND && Info.number(UnitType.Rocket)<=(Info.number(UnitType.Healer)+Info.number(UnitType.Ranger))/8) {
        		return;
        	}
	       if ( gc.canProduceRobot(factory.id(), UnitType.Ranger)) {
	    	   //Info.number(UnitType.Ranger)<5*Info.number(UnitType.Healer) &&
	        	if(gc.round()<=100){
	        		 if(Info.number(UnitType.Ranger)<2*Info.number(UnitType.Worker)){
	        			 gc.produceRobot(factory.id(), UnitType.Ranger);
	        	         Info.addUnit(UnitType.Ranger);
	        		 }   
	        	}
	        	else{
	        		if(Info.number(UnitType.Ranger)<4*Info.number(UnitType.Worker)){
	        			 gc.produceRobot(factory.id(), UnitType.Ranger);
	        	         Info.addUnit(UnitType.Ranger);
	        		 } 
	        	}
	        }
        }
        else {
        	System.out.println(Info.number(UnitType.Ranger)+" "+Info.number(UnitType.Healer));
        		if(gc.canProduceRobot(factory.id(), UnitType.Ranger) && (Info.number(UnitType.Healer)*5>Info.number(UnitType.Ranger) || Info.number(UnitType.Ranger)<15)) {
        			gc.produceRobot(factory.id(), UnitType.Ranger);
       	         Info.addUnit(UnitType.Ranger);
        		}
        		//Info.number(UnitType.Ranger)<5*Info.number(UnitType.Healer)+10 && 
        		else if(gc.canProduceRobot(factory.id(), UnitType.Healer)) {
        			gc.produceRobot(factory.id(), UnitType.Healer);
       	        Info.addUnit(UnitType.Healer);
        		}
        }
    }
	        
        else{
        	if(Info.number(UnitType.Worker)>2){
        		if(gc.round()<=600 && (Info.number(UnitType.Rocket)>=(Info.number(UnitType.Ranger)-10)/7||gc.round()<=50)){
		        	if (gc.canProduceRobot(factory.id(), UnitType.Ranger)) {
			        	gc.produceRobot(factory.id(), UnitType.Ranger);
				          Info.addUnit(UnitType.Ranger);     
			        }
        		}
        		else if (gc.round()>=600 && Info.number(UnitType.Rocket)>=(Info.number(UnitType.Ranger))/5){
        			if (gc.canProduceRobot(factory.id(), UnitType.Ranger)) {
			        	gc.produceRobot(factory.id(), UnitType.Ranger);
				          Info.addUnit(UnitType.Ranger);     
			        }
        		}
        	}
        	else{
        		if (gc.canProduceRobot(factory.id(), UnitType.Worker)) {
        			gc.produceRobot(factory.id(), UnitType.Worker);
        			Info.addUnit(UnitType.Worker);
       			}
        	}
        }
    }

    private static void unload() {

        // Check all possible directions
        for (Direction dir: Direction.values()) {
            if (gc.canUnload(factory.id(), dir)) {
                gc.unload(factory.id(), dir);
            }
        }
    }
}
