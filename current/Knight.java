import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Knight {

    private static Unit knight;
    private static GameController gc;
    private static VecUnit enemies;
    private static VecUnit friendly;
    private static HashMap<Integer, Direction> directionMap;
    private static HashMap<Integer, Integer> counterMap;
    public static boolean attacked = false;
    public static boolean rush = false;
    private static int knightId;

    public static void init(GameController controller) {
        gc = controller;
        directionMap = new HashMap<>();
        counterMap = new HashMap<>();
    }

    public static void runMars(Unit unit) {
        knight = unit;
        knightId = knight.id();
        if (knight.location().isInGarrison()) return;
        if (!attack()) {
            moveMars();
            attack();
        } else {
            moveMars();
        }
        return;
    }

    public static void runEarth(Unit unit) {

        // Receive knight from main runner
        knight = unit;
        knightId = knight.id();
        if (knight.location().isInGarrison()) return;
        if (knight.health() < knight.maxHealth()) {
            attacked = true;
        }
        /*if(gc.round()<300 && Factory.initialknights == true && Info.number(UnitType.Knight)>=5){
            rush();
    		attack();
    		return;
    	}
        if(attacked == true){
    		rush();
    		attack();
    		return;
    	}
        
        if(gc.round()<300 && Player.mapsize.equals("smallmap")){
    		rush();
    		attack();
    		return;
    	}
        */
        rush();
        attack();
        /*
        Scenario 1: Attack first and then run away to get out of enemy range
        Scenario 2: Move first to get into range and then attack
         */
        if (knight.health() < 80) {
            moveTowardsFactory();
        }
        if (Player.mapsize.equals("largemap")) {
            if (!attack()) {
                move();
                attack();
            } else {
                move();
            }
        } else {
            if (!attack()) {
                moveSmall();
                attack();
            } else {
                moveSmall();
            }
        }
    }

    private static boolean rush() {
        if (Player.enemy == null) { //|| Info.number(UnitType.Knight)<5
            return false;
        }
        rush = true;
        enemies = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.visionRange(), Util.enemyTeam());
        if (enemies == null || enemies.size() == 0) {
            if (!Pathing.move(knight, Player.enemy)) {
                return Pathing.tryMove(knight, knight.location().mapLocation().directionTo(Player.enemy));
            }
            return false;
        }
        for (int x = 0; x < enemies.size(); x++) {
            if (enemies.get(x).unitType().equals(UnitType.Ranger)) {
                if (!Pathing.move(knight, enemies.get(x).location().mapLocation())) {
                    return Pathing.tryMove(knight, knight.location().mapLocation().directionTo(enemies.get(x).location().mapLocation()));
                }
            }
            if (enemies.get(x).unitType().equals(UnitType.Factory)) {
                if (!Pathing.move(knight, enemies.get(x).location().mapLocation())) {
                    return Pathing.tryMove(knight, knight.location().mapLocation().directionTo(enemies.get(x).location().mapLocation()));
                }
            }
            if (enemies.get(x).unitType().equals(UnitType.Knight)) {
                if (!Pathing.move(knight, enemies.get(x).location().mapLocation())) {
                    return Pathing.tryMove(knight, knight.location().mapLocation().directionTo(enemies.get(x).location().mapLocation()));
                }
            }
        }
        if (!Pathing.move(knight, enemies.get(0).location().mapLocation())) {
            return Pathing.tryMove(knight, knight.location().mapLocation().directionTo(enemies.get(0).location().mapLocation()));
        }
        return true;
    }

    private static void moveSmall() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(knightId)) {
            return;
        }
        if (knight.location().isOnPlanet(Planet.Earth) && gc.round() >= Config.ROCKET_CREATION_ROUND) {
            if (moveTowardsRocketSmall()) {
                return;
            }
        }

        // Avoid enemy units, walk outside of their view range
        enemies = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.visionRange(), Util.enemyTeam());
        friendly = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.visionRange(), Util.enemyTeam());
        if (Player.focalPoint != null) {
            if (Player.focalPoint.isWithinRange(knight.visionRange(), knight.location().mapLocation()) && gc.canSenseLocation(Player.focalPoint) &&
                    !gc.hasUnitAtLocation(Player.focalPoint)) {
                // System.out.println("Works");
                Player.focalPoint = null;
            }
        }

        // Get closest enemy

        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            long dist = knight.location().mapLocation().distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        // Set new focal point
        if (Player.focalPoint == null) {
            if (idx != -1) {
                Player.focalPoint = enemies.get(idx).location().mapLocation();
            }
        }

        // Move towards focal point
        if (Player.focalPoint != null) {
            Pathing.move(knight, Player.focalPoint);
        }


        // Unit will bounce in order to escape factories
        bounce();

        // Move towards rockets mid-game, and escape factories early on


        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(knight, FocusPoints.GeographicFocusPointsE.get(0));
    }

    private static boolean moveTowardsRocketSmall() {
        if (knight.location().mapLocation().getPlanet() == Planet.Mars) return false;
        // Move towards a low-HP rocket if possible
        VecUnit rockets = gc.senseNearbyUnitsByType(knight.location().mapLocation(), knight.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(knight.location().mapLocation());
            if (Util.friendlyUnit(rockets.get(i)) && dist < minDist && rockets.get(i).structureIsBuilt() == 1) {
                minDist = dist;
                idx = i;
            }
        }
        if (minDist > 32) return false;
        if (idx != -1) {
            PlanetMap map = gc.startingMap(knight.location().mapLocation().getPlanet());
            MapLocation tmp = rockets.get(idx).location().mapLocation();
            int initx = tmp.getX();
            int inity = tmp.getY();
            tmp = new MapLocation(Planet.Earth, initx + 1, inity);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            // System.out.println("Moving towards friendly rocket.");
            return true;
        }

        return false;
    }

    private static boolean attack() {

        // Return true if attack isn't ready
        if (!gc.isAttackReady(knightId))
            return true;

        // Get enemy units
        enemies = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.attackRange(), Util.enemyTeam());
        if (enemies.size() == 0)
            return false;

        // Attack lowest HP target
        long minHP = Long.MAX_VALUE;

        int idx = 0;
        boolean check1 = false;
        boolean check2 = false;
        boolean check3 = false;
        boolean check4 = false;
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).unitType().equals(UnitType.Ranger)) {
                check1 = true;
            }
            if (enemies.get(i).unitType().equals(UnitType.Mage)) {
                check2 = true;
            }
            if (enemies.get(i).unitType().equals(UnitType.Knight)) {
                check3 = true;
            }

            if (enemies.get(i).unitType().equals(UnitType.Factory)) {
                check4 = true;
                if (!Player.snipeHitList.keySet().contains(enemies.get(i).location().mapLocation())) {
                    Player.snipeHitList.put(enemies.get(i).location().mapLocation(), (int) enemies.get(i).health());
                }
            }

        }
        for (int i = 0; i < enemies.size(); i++) {
            if (check1 == true) {
                if (enemies.get(i).equals(UnitType.Ranger) && enemies.get(i).health() < minHP) {
                    minHP = enemies.get(i).health();
                    idx = i;
                }
            } else if (check2 == true) {
                if (enemies.get(i).equals(UnitType.Mage) && enemies.get(i).health() < minHP) {
                    minHP = enemies.get(i).health();
                    idx = i;
                }
            } else if (check3 == true) {
                if (enemies.get(i).equals(UnitType.Knight) && enemies.get(i).health() < minHP) {
                    minHP = enemies.get(i).health();
                    idx = i;
                }
            } else {
                if (enemies.get(i).health() < minHP) {
                    minHP = enemies.get(i).health();
                    idx = i;
                }
            }

        }

        if (idx != -1 && gc.canAttack(knightId, enemies.get(idx).id())) {
            if (check4 && enemies.get(idx).health() <= 80) { // Remove from hit list, it's about to be killed anyway
                Player.snipeHitList.remove(enemies.get(idx).location().mapLocation());
            }
            gc.attack(knightId, enemies.get(idx).id());
            return true;
        }
        return false;

    }

    private static void move() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(knightId)) {
            return;
        }
        if (knight.location().isOnPlanet(Planet.Earth) && gc.round() >= Config.ROCKET_CREATION_ROUND) {
            if (moveTowardsRocket()) {
                return;
            }
        }

        // Avoid enemy units, walk outside of their view range
        enemies = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.visionRange(), Util.enemyTeam());
        friendly = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.visionRange(), Util.enemyTeam());

        // Move towards initial enemy worker locations
        /*
        if (gc.round() < Config.knight_AUTO_ATTACK_ROUND && Info.number(UnitType.knight) >= 8 && knight.location().isOnPlanet(Planet.Earth))
            if (moveTowardsInitPoint())
                return;
        */

        // Remove focal point if no units exist there
        if (gc.round() <= 600 && gc.planet() == Planet.Earth) {
            if (Player.focalPoint != null) {
                if (Player.focalPoint.isWithinRange(knight.visionRange(), knight.location().mapLocation()) && gc.canSenseLocation(Player.focalPoint) &&
                        !gc.hasUnitAtLocation(Player.focalPoint)) {
                    // System.out.println("Works");
                    Player.focalPoint = null;
                }
            }

            // Get closest enemy

            long minDist = Long.MAX_VALUE;
            int idx = -1;
            for (int i = 0; i < enemies.size(); i++) {
                long dist = knight.location().mapLocation().distanceSquaredTo(enemies.get(i).location().mapLocation());
                if (dist < minDist) {
                    minDist = dist;
                    idx = i;
                }
            }

            // Set new focal point
            if (Player.focalPoint == null) {
                if (idx != -1) {
                    Player.focalPoint = enemies.get(idx).location().mapLocation();
                }
            }

            // Move towards focal point
            if (Player.focalPoint != null) {
                Pathing.move(knight, Player.focalPoint);
            }
        }


        if (returnToFactory())
            return;
        // Unit will bounce in order to escape factories
        if (ditchFactory())
            return;
        if (ditchRocket())
            return;
        // Move towards rockets mid-game, and escape factories early on


        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(knight, FocusPoints.GeographicFocusPointsE.get(0));
    }

    private static boolean returnToFactory() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist = knight.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist <= 16) return false;
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }
        if (minDist >= 1000) return false;
        if (idx == -1) return false;
        Pathing.tryMove(knight, knight.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        return true;
    }

    private static void moveMars() {
    	
        /*
        TODO Implement the entire worker changes function as a heuristic based on priority
         */

        // Return if we cannot move
        if (!gc.isMoveReady(knightId)) {
            return;
        }

        // Avoid enemy units, walk outside of their view range
        enemies = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.visionRange(), Util.enemyTeam());
        friendly = gc.senseNearbyUnitsByTeam(knight.location().mapLocation(), knight.visionRange(), Util.enemyTeam());

        // Move towards initial enemy worker locations
        /*
        if (gc.round() < Config.knight_AUTO_ATTACK_ROUND && Info.number(UnitType.knight) >= 8 && knight.location().isOnPlanet(Planet.Earth))
            if (moveTowardsInitPoint())
                return;
        */

        // Remove focal point if no units exist there
        if (Player.focalPointMars != null) {
            if (Player.focalPointMars.isWithinRange(knight.visionRange(), knight.location().mapLocation()) && gc.canSenseLocation(Player.focalPointMars) &&
                    !gc.hasUnitAtLocation(Player.focalPointMars)) {
                // System.out.println("Works");
                Player.focalPoint = null;
            }
        }

        // Get closest enemy

        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < enemies.size(); i++) {
            long dist = knight.location().mapLocation().distanceSquaredTo(enemies.get(i).location().mapLocation());
            if (dist < minDist) {
                minDist = dist;
                idx = i;
            }
        }

        // Set new focal point
        if (Player.focalPointMars == null) {
            if (idx != -1) {
                Player.focalPointMars = enemies.get(idx).location().mapLocation();
            }
        }

        // Move towards focal point
        if (Player.focalPointMars != null) {
            System.out.println(Player.focalPointMars);
            System.out.println(knight.location().mapLocation());
            Pathing.move(knight, Player.focalPointMars);
        }


        // Unit will bounce in order to escape factories
        bounce();

        // Move towards rockets mid-game, and escape factories early on
        if (knight.location().isOnPlanet(Planet.Earth) && gc.round() >= Config.ROCKET_CREATION_ROUND && knight.location().mapLocation().getPlanet() == Planet.Mars) {
            if (moveTowardsRocket()) {
                return;
            }
        }


        // If none of the above work, changes in a random direction (placeholder for now)
        // Pathing.move(knight, FocusPoints.GeographicFocusPointsE.get(0));
    }

    private static boolean bounce() {

        // Reset if counter is 8
        counterMap.putIfAbsent(knightId, 0);
        if (counterMap.get(knightId) >= 8) {
            counterMap.put(knightId, 0);

            // Find possible movement directions
            List<Direction> dirList = new ArrayList<>();
            for (Direction d : Direction.values()) {
                MapLocation loc = knight.location().mapLocation().add(d);
                if (gc.startingMap(knight.location().mapLocation().getPlanet()).onMap(loc) && (gc.startingMap(knight.location().mapLocation().getPlanet()).isPassableTerrainAt(loc) == 1) && (gc.isOccupiable(loc) == 1)) {
                    dirList.add(d);
                }
            }

            // Get one of the possible directions if they exist
            if (dirList.size() != 0) {
                int idx = (int) (Math.random() * dirList.size());

                // Set the current direction
                directionMap.put(knightId, dirList.get(idx));
            }
        }

        // Try to move in the current direction
        Direction dir = directionMap.get(knightId);
        if (dir != null) {
            if (Pathing.tryMove(knight, dir))
                counterMap.put(knightId, counterMap.get(knightId) + 1);
            else
                counterMap.put(knightId, 0);
        } else {
            // Reset the direction
            counterMap.put(knightId, 8);
        }

        return false;
    }

    private static boolean ditchFactory() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
            long dist = 50 - knight.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0) return false;

        Direction opposite = Pathing.opposite(knight.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(knight, opposite);
        return true;
    }

    private static boolean ditchRocket() {
        List<Unit> units = Info.unitByTypes.get(UnitType.Rocket);
        if (units.size() == 0) return false;
        long maxDist = -Long.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).structureIsBuilt() == 1) continue;
            long dist = 50 - knight.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist > maxDist && units.get(i).health() == units.get(i).maxHealth()) {
                maxDist = dist;
                idx = i;
            }
        }
        if (maxDist <= 0) return false;

        Direction opposite = Pathing.opposite(knight.location().mapLocation().directionTo(units.get(idx).location().mapLocation()));
        Pathing.tryMove(knight, opposite);
        return true;
    }

    private static boolean moveTowardsRocket() {
        if (knight.location().mapLocation().getPlanet() == Planet.Mars) return false;
        // Move towards a low-HP rocket if possible
        VecUnit rockets = gc.senseNearbyUnitsByType(knight.location().mapLocation(), knight.visionRange(), UnitType.Rocket);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < rockets.size(); i++) {
            long dist = rockets.get(i).location().mapLocation().distanceSquaredTo(knight.location().mapLocation());
            if (Util.friendlyUnit(rockets.get(i)) && dist < minDist && rockets.get(i).structureIsBuilt() == 1) {
                minDist = dist;
                idx = i;
            }
        }
        if (minDist > 10 && gc.round() <= 600) return false;
        if (idx != -1) {
            PlanetMap map = gc.startingMap(knight.location().mapLocation().getPlanet());
            MapLocation tmp = rockets.get(idx).location().mapLocation();
            int initx = tmp.getX();
            int inity = tmp.getY();
            tmp = new MapLocation(Planet.Earth, initx + 1, inity);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            // System.out.println("Moving towards friendly rocket.");
            return true;
        }

        return false;
    }

    private static boolean moveTowardsFactory() {

        // Move towards the closest factory
        List<Unit> units = Info.unitByTypes.get(UnitType.Factory);
        long minDist = Long.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < units.size(); i++) {
            long dist = knight.location().mapLocation().distanceSquaredTo(units.get(i).location().mapLocation());
            if (dist < minDist && (units.get(i).structureIsBuilt() == 0 || gc.round() >= 600)) {
                minDist = dist;
                idx = i;
            }
        }
        if (minDist == 2) {
            return true;
        }
        //if(!Player.mapsize.equals("smallmap")){
        if (minDist > 16 || gc.round() >= 600) {
            return false;
        }
        //}
        if (idx != -1) {
            //MAYBE add bug pathing
            //Pathing.move(knight, units.get(idx).location().mapLocation());
            PlanetMap map = gc.startingMap(knight.location().mapLocation().getPlanet());
            MapLocation tmp = units.get(idx).location().mapLocation();
            int initx = tmp.getX();
            int inity = tmp.getY();
            tmp = new MapLocation(Planet.Earth, initx + 1, inity);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx + 1, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx - 1, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity - 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
            tmp = new MapLocation(Planet.Earth, initx, inity + 1);
            if (map.onMap(tmp)) {
                if (!Pathing.move(knight, tmp)) {
                    Pathing.tryMove(knight, knight.location().mapLocation().directionTo(tmp));
                }
                return true;
            }
        }
        return false;
    }

}
