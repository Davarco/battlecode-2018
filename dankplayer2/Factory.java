import bc.*;

import java.util.HashMap;

public class Factory {

    private static Unit factory;
    private static GameController gc;

    public static void init(GameController controller) throws Exception {
        gc = controller;
        
    }

    public static void run(Unit unit) throws Exception {

        // Receive factory from main runner
        factory = unit;

        // Build best unit possible
        build();

        // Unload any units in queue
        unload();
    }

    private static void build() throws Exception {

        // Workers are vital, build them if we have nothing left
    	if (Info.number(UnitType.Worker) < Info.number(UnitType.Factory)) {
            if (gc.canProduceRobot(factory.id(), UnitType.Worker)) {
                gc.produceRobot(factory.id(), UnitType.Worker);
                Info.addUnit(UnitType.Worker);
            }
        }

        // See if the factory can build the ranger
        if(Player.mapsize.equals("largemap")){
	        if (gc.canProduceRobot(factory.id(), UnitType.Ranger)) {
	        	if(gc.round()<=100){
	        		 if(Info.number(UnitType.Ranger)<2*Info.number(UnitType.Worker) ){
	        			 gc.produceRobot(factory.id(), UnitType.Ranger);
	        	         Info.addUnit(UnitType.Ranger);
	        		 }   
	        	}
	        	else{
	        		if(gc.round()<=650){
		        		if(gc.round() > Config.ROCKET_CREATION_ROUND && (Info.number(UnitType.Rocket)>=(Info.number(UnitType.Ranger)-10)/5)){
		        			gc.produceRobot(factory.id(), UnitType.Ranger);
		        	    	Info.addUnit(UnitType.Ranger);
		        		}
	        		}
	        		else{
	        			if(Info.number(UnitType.Rocket)>=Info.number(UnitType.Ranger)/5){
		        			gc.produceRobot(factory.id(), UnitType.Ranger);
		        	    	Info.addUnit(UnitType.Ranger);
		        		}
	        		}
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

    private static void unload() throws Exception {

        // Check all possible directions
        for (Direction dir: Direction.values()) {
            if (gc.canUnload(factory.id(), dir)) {
                gc.unload(factory.id(), dir);
            }
        }
    }
}
