package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.*;


/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {

	/*
	 * Potential needed information of the state
	 */
	private int XExtent;
	private int YExtent;
	private boolean isFootmanTurn;
	private Map<Integer, MapLocation> footmenMap, archersMap;
	private Map<Integer, Integer> footmenHP, archersHP;
	private Set<MapLocation> trees;

	
	/*
	 * Abstraction for a map location
	 */
	class MapLocation{
		private int x;
		private int y;
		
		public MapLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		/*
		 * Auto-generation, make two map location identical if x1 == x2 and y1 == y2
		 */
		
		@Override
		public int hashCode() {
			final int prime = 101;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MapLocation other = (MapLocation) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}
	
    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns the IDs of all of the obstacles in the map
     * state.getResourceNode(int resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     * 
     * You can get a list of all the units belonging to a player with the following command:
     * state.getUnitIds(int playerNum): gives a list of all unit IDs beloning to the player.
     * You control player 0, the enemy controls player 1.
     * 
     * In order to see information about a specific unit, you must first get the UnitView
     * corresponding to that unit.
     * state.getUnit(int id): gives the UnitView for a specific unit
     * 
     * With a UnitView you can find information about a given unit
     * unitView.getXPosition() and unitView.getYPosition(): get the current location of this unit
     * unitView.getHP(): get the current health of this unit
     * 
     * SEPIA stores information about unit types inside TemplateView objects.
     * For a given unit type you will need to find statistics from its Template View.
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit type deals
     * unitView.getTemplateView().getBaseHealth(): The initial amount of health of this unit type
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
    	XExtent = state.getXExtent();
    	YExtent = state.getYExtent();
    	isFootmanTurn = true;
    	List<ResourceView> treesView = state.getAllResourceNodes();
    	List<UnitView>  footmen = state.getUnits(0);
    	List<UnitView> archers = state.getUnits(1);
    	footmenMap = new HashMap<Integer, MapLocation>();
    	archersMap = new HashMap<Integer, MapLocation>();
    	footmenHP = new HashMap<Integer, Integer>();
    	archersHP = new HashMap<Integer, Integer>();
    	trees = new HashSet<MapLocation>();
    	for(ResourceView tree : treesView) {
    		trees.add(new MapLocation(tree.getXPosition(), tree.getYPosition()));
    	}
    	//Initialize maps that will keep track of the state
    	for(UnitView footman : footmen) {
    		MapLocation location = new MapLocation(footman.getXPosition(), footman.getYPosition());
    		Integer hp = footman.getHP();

    		footmenHP.put(footman.getID(), hp);
    		footmenMap.put(footman.getID(), location);
    	}
    	for(UnitView archer : archers) {
    		MapLocation location = new MapLocation(archer.getXPosition(), archer.getYPosition());
    		Integer hp = archer.getHP();
    		archersHP.put(archer.getID(), hp);
    		archersMap.put(archer.getID(), location);
    	}

    }


    /*
     * Constructor for specific data of future states, for the purpose of tracking state
     */
    public GameState(GameState previousState, Map<Integer, MapLocation> footmenMap, Map<Integer, MapLocation> archersMap, Map<Integer, Integer> footmenHP, Map<Integer, Integer> archersHP) {
    	this.trees = previousState.trees;
    	this.XExtent = previousState.XExtent;
    	this.YExtent = previousState.YExtent;
    	this.isFootmanTurn = !previousState.isFootmanTurn;
    	this.footmenMap = footmenMap;
    	this.archersMap = archersMap;
    	this.footmenHP = footmenHP;
    	this.archersHP = archersHP;
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plies in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
    	double utility = 0;

    	double footmanHPweight = 0;
    	double archersHPweight = -100;
    	double distanceWeight = -100;
    	double sqrtDistanceWeight = -100;
    	double squareDistanceWeight = -1;
    	double minimumStepCountWeight = -1000;
    	
    	for (int footmanID: footmenMap.keySet()) {
    		MapLocation footman = footmenMap.get(footmanID);
    		int footmanHP = footmenHP.get(footmanID);
    		int nearestArcherID = 0;
			MapLocation nearestArcher = null;
			int nearestDistance = Integer.MAX_VALUE;
    		for (int archerID: archersMap.keySet()) {
    			MapLocation archer = archersMap.get(archerID);
    			int distance = Math.abs(archer.x - footman.x) + Math.abs(archer.y - footman.y);
    			if (distance < nearestDistance) {
    				nearestDistance = distance;
    				nearestArcherID = archerID;
    				nearestArcher = archer;
    			}
    		}
    		int distance = nearestDistance;
    		int archerHP = archersHP.get(nearestArcherID);
    		int minStepCount = minimumStepCount(nearestArcher, footman);
    		utility += minStepCount * minimumStepCountWeight;
    		
    		utility += archerHP * archersHPweight;
			utility += footmanHP * footmanHPweight;

			utility += distance * distanceWeight;

			double sqrtDistance = Math.sqrt(distance);
			utility += sqrtDistance * sqrtDistanceWeight;

			double squareDistance = Math.pow(distance, 2);
			utility += squareDistance * squareDistanceWeight;
			
			utility += Math.random() * 1000;
    	}

    	return utility;
    }
    
    public int minimumStepCount(MapLocation archer, MapLocation footman) {
    	Set<MapLocation> hashmap = new HashSet<>();
    	Queue<MapLocation> queue = new LinkedList<>();
    	queue.add(footman);
    	hashmap.add(footman);
    	Queue<MapLocation> nextQueue = new LinkedList<>();
    	int step = 0;
    	while (!queue.isEmpty()) {
    		MapLocation nextLocation = queue.poll();
    		if (nextLocation.x == archer.x && nextLocation.y == archer.y) {
    			return step;
    		}
    		
    		Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    		for(Direction direction : directions) {
    			int x = nextLocation.x + direction.xComponent();
    			int y = nextLocation.y + direction.yComponent();
    			MapLocation newLocation = new MapLocation(x, y);
    			if (!trees.contains(newLocation) && !hashmap.contains(newLocation)) {
    				hashmap.add(newLocation);
    				nextQueue.add(newLocation);
    			}
    		}

    		if (queue.isEmpty()) {
    			queue = nextQueue;
    			nextQueue = new LinkedList<>();
    			step++;
    		}
    	}

    	return Integer.MAX_VALUE;
    }
    
    
    /*
     * The Utility function(Linear combination of features) at each node can also be used to order the nodes 
     */
    public double orderingHeuristics() {
    	return getUtility();
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     * 
     * It may be useful to be able to create a SEPIA Action. In this assignment you will
     * deal with movement and attacking actions. There are static methods inside the Action
     * class that allow you to create basic actions:
     * Action.createPrimitiveAttack(int attackerID, int targetID): returns an Action where
     * the attacker unit attacks the target unit.
     * Action.createPrimitiveMove(int unitID, Direction dir): returns an Action where the unit
     * moves one space in the specified direction.
     *
     * You may find it useful to iterate over all the different directions in SEPIA. This can
     * be done with the following loop:
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     * 
     * If you wish to explicitly use a Direction you can use the Direction enum, for example
     * Direction.NORTH or Direction.NORTHEAST.
     * 
     * You can check many of the properties of an Action directly:
     * action.getType(): returns the ActionType of the action
     * action.getUnitID(): returns the ID of the unit performing the Action
     * 
     * ActionType is an enum containing different types of actions. The methods given above
     * create actions of type ActionType.PRIMITIVEATTACK and ActionType.PRIMITIVEMOVE.
     * 
     * For attack actions, you can check the unit that is being attacked. To do this, you
     * must cast the Action as a TargetedAction:
     * ((TargetedAction)action).getTargetID(): returns the ID of the unit being attacked
     * 
     * @return All possible actions and their associated resulting game state
     */
    
    public List<GameStateChild> getChildren() {
    	
    	//Children nodes will be different depending on if the current node is a footman
    	if(isFootmanTurn) {
    		return getFootmanChildren();
    	}
    	else {
    		return getArcherChildren();
    	}
   
    }
    
    /*
     *  It returns a list of children of a footman with all possible actions at a specific state
     */
    private List<GameStateChild>  getFootmanChildren() {
    	//Create an array list for children

    	List<GameStateChild> childrenList = new ArrayList<>();
    	
    	// Create an array that stores all possible directions
    	Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    	
    	//Convert all footmanID to an array
    	Integer[] footmenID = new Integer[2];
    	int index = 0;
    	for(int footmanID : footmenMap.keySet()) {
    		footmenID[index] = footmanID;
    		index ++;
    	}

    	/*
    	 * For each direction create an action for the first footman
    	 */
    		for(Direction direction : directions) {
    	    	Map<Integer, Action> actionMap = new HashMap<>();
				Map<Integer, MapLocation> nextFootmenMap = copyMap(footmenMap);
				Map<Integer, Integer> nextArchersHP = copyMap(archersHP);
    			int x = footmenMap.get(footmenID[0]).getX() + direction.xComponent();
    			int y = footmenMap.get(footmenID[0]).getY() + direction.yComponent();

    			MapLocation nextMapLocation = new MapLocation(x ,y); //This is the next map location the footman is intened to move to
    			
    			//If nothing is in the next map location and it stays within the map extent
    			// add move action and update the next state
    			if(isValidMove(nextMapLocation))
    			{
    				actionMap.put(footmenID[0], Action.createPrimitiveMove(footmenID[0], direction));
    				nextFootmenMap.put(footmenID[0], nextMapLocation);
    			}
    			else {
        			//if an archer exists at the next map location
        			//add attack action and update the next state
        			for(Integer archerID :archersMap.keySet()) {
        				if(archersMap.get(archerID).equals(nextMapLocation)) {
        					actionMap.put(footmenID[0], Action.createPrimitiveAttack(footmenID[0], archerID));
        					nextArchersHP.put(archerID, archersHP.get(archerID) - 5);
        				}
        			}
    			}
    			
    			/*
    			 * If the second footman exists, create a move action for each direction
    			 */
    			if(footmenID[1] != null) {
	    			for(Direction direction2 : directions) {
	        	    	Map<Integer, Action> actionMap2 = copyMap(actionMap);
	    				Map<Integer, MapLocation> nextFootmenMap2 = copyMap(nextFootmenMap);
	    				Map<Integer, Integer> nextArchersHP2 = copyMap(nextArchersHP);
	    				int x2 = footmenMap.get(footmenID[1]).getX() + direction2.xComponent();
	        			int y2 = footmenMap.get(footmenID[1]).getY() + direction2.yComponent();
	
	        			MapLocation nextMapLocation2 = new MapLocation(x2 ,y2); //This is the next map location the footman is intented to move to
	        			
	        			
	        			if(isValidMove(nextMapLocation2)) 
	            			{
	            				actionMap2.put(footmenID[1], Action.createPrimitiveMove(footmenID[1], direction2));
	            				nextFootmenMap2.put(footmenID[1], nextMapLocation2);
	            			}
	        			else {
		        			//if an archer exists at the next map location
		        			//add attack action and update the next state
		        			for(Integer archerID :archersMap.keySet()) {
		        				if(archersMap.get(archerID).equals(nextMapLocation2)) {
		        					actionMap.put(footmenID[1], Action.createPrimitiveAttack(footmenID[1], archerID));
		        					nextArchersHP2.put(archerID, archersHP.get(archerID) - 5);
		        				}
		        			}
	        			}
	        		//	System.out.println(direction + " : " + direction2 +  " : " );
	        		//	System.out.println("First footman = " + nextFootmenMap2.get(footmenID[0]).getX() + " : "  + nextFootmenMap2.get(footmenID[0]).getY());
	        		//	System.out.println("Secondfootman = " + nextFootmenMap2.get(footmenID[1]).getX() + " : "  + nextFootmenMap2.get(footmenID[1]).getY());
	        			
	        			// For each possible actions, add to the children list
	        			if(!actionMap2.isEmpty()) {
		        			childrenList.add(new GameStateChild(actionMap2, new GameState(this, nextFootmenMap2, archersMap, footmenHP, nextArchersHP2)));
	        			}
		        		//	System.out.println(new GameState(this, nextFootmenMap2, archersMap, footmenHP, nextArchersHP2).getUtility());
	    			}
    			}
    			
    			//If the second footman does not exist, add the actions of the first footman to the list
    			else {
    				if(!actionMap.isEmpty()) {
        				childrenList.add(new GameStateChild(actionMap, new GameState(this, nextFootmenMap, archersMap, footmenHP, nextArchersHP)));
    				}
    			}
    		}
    	return childrenList;
    }
    
    /*
     *  It returns a list of children of a footman with all possible actions at a specific state
     */
    private List<GameStateChild>  getArcherChildren() {
    	//Create an array list for children
    	List<GameStateChild> childrenList = new ArrayList<>();
    	
    	// Create an array that stores all possible directions
    	Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    	List<Map<Integer, MapLocation>> archersMapList = new ArrayList<Map<Integer, MapLocation>>();
    	List<Map<Integer, Integer>> footmenHPList = new ArrayList<Map<Integer, Integer>>();
    	
    	Integer[] archersID = new Integer[2];
    	int index = 0;
    	for(int archerID : archersMap.keySet()) {
    		archersID[index] = archerID;
    		index ++;
    	}

    		for(Direction direction : directions) {
				Map<Integer, MapLocation> nextArchersMap = copyMap(archersMap);
				Map<Integer, Integer> nextFootmenHP = copyMap(footmenHP);
    			int x = archersMap.get(archersID[0]).getX() + direction.xComponent();
    			int y = archersMap.get(archersID[0]).getY() + direction.yComponent();

    			MapLocation nextMapLocation = new MapLocation(x ,y); //This is the next map location the archer is intened to move to
    			
    			//If nothing is in the next map location and it stays within the map extent
    			// add move action and update the next state
    			if(isValidMove(nextMapLocation))
    			{
    				nextArchersMap.put(archersID[0], nextMapLocation);
        			archersMapList.add(nextArchersMap);
        	    	footmenHPList.add(nextFootmenHP);
    			}


    		}
			//If there exists footman that is in the range of the archers' attack (Within 10 units away)
    		//Create an attack action
				for(Integer footmanID : footmenMap.keySet()) {
					Map<Integer, MapLocation> nextArchersMap = copyMap(archersMap);
					Map<Integer, Integer> nextFootmenHP = copyMap(footmenHP);
					if((Math.abs(archersMap.get(archersID[0]).getX() - footmenMap.get(footmanID).getX()) <= 10) ||
    					(Math.abs(archersMap.get(archersID[0]).getY() - footmenMap.get(footmanID).getY()) <= 10)) {
		    	    	nextFootmenHP.put(footmanID, footmenHP.get(footmanID) -5);
		    			archersMapList.add(nextArchersMap);
		    	    	footmenHPList.add(nextFootmenHP);
					}
				}
    		
				
				//If the second archer exists, repeats the previous step and add it to the action map
    			if(archersID[1] != null) {
	    			for(Direction direction2 : directions) {
	    				int x2 = archersMap.get(archersID[1]).getX() + direction2.xComponent();
	        			int y2 = archersMap.get(archersID[1]).getY() + direction2.yComponent();
	
	        			MapLocation nextMapLocation2 = new MapLocation(x2 ,y2);
	        			
	        			
	        			if(isValidMove(nextMapLocation2)){
	        				for(Map<Integer, MapLocation> nextArchersMap : archersMapList) {
	            				nextArchersMap.put(archersID[1], nextMapLocation2);
	        				}
	            		}
	    			}
	    			
	    				for(Integer footmanID : footmenMap.keySet()) {
	    					if((Math.abs(archersMap.get(archersID[1]).getX() - footmenMap.get(footmanID).getX()) <= 10) ||
	    						(Math.abs(archersMap.get(archersID[1]).getY() - footmenMap.get(footmanID).getY()) <= 10)) {
		        				for(Map<Integer, Integer> nextfootmanHP : footmenHPList) {
		        					nextfootmanHP.put(footmanID, nextfootmanHP.get(footmanID) -5);
		        				}
	    					}
	    				}
    			}
    			
    			//Note here both lists have the same length and each element at the same index matches to each other and together forms the next state
    			int listIndex = 0;
    			while(listIndex < footmenHPList.size()) {
        			childrenList.add(new GameStateChild(null, new GameState(this, footmenMap, archersMapList.get(listIndex), footmenHPList.get(listIndex), archersHP)));
        			listIndex ++;
    			}
    	return childrenList;
    }
    
    private boolean isValidMove(MapLocation nextLocation) {
    	int x = nextLocation.getX();
    	int y = nextLocation.getY();
    	return (x >= 0 &&
				x <= XExtent &&
				y >= 0 &&
			    y <= YExtent && 
			   !trees.contains(nextLocation) &&
			   !archersMap.containsValue(nextLocation)&&
    		   !footmenMap.containsValue(nextLocation));
    }
    /*
     * Copy a map 
     */
    private <T> Map<Integer, T> copyMap(Map<Integer, T> map){
    	Map <Integer, T> copy = new HashMap<Integer, T>();
    	for(Integer ID: map.keySet()) {
    		T value = map.get(ID);
    		copy.put(ID, value);
    	}
    	return copy;
    }

    private int distanceBetween(MapLocation l1, MapLocation l2) {
    	return Math.abs(l1.getX() - l2.getX()) + Math.abs(l1.getY() - l2.getY()); 
    }
}
