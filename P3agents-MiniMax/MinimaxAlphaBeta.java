package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
    	

    	GameStateChild[] bestChild = new GameStateChild[1];
    	//Create a variable to store the best successor in the minimaxValue algorithm
    	minimaxValue(node, depth, alpha, beta, true, bestChild);
    	return bestChild[0];

    }
    
    /*
     * A helper method that backup the minimax value from the leave node to the root
     */
    private double minimaxValue(GameStateChild node, int depth, double alpha, double beta, boolean isMax, GameStateChild[] bestChild) {
    	// return the estimated utility function if the depth reach 0;
    	if(depth == 0) {
    		return node.state.getUtility();
    	}
		List<GameStateChild> childrenList = orderChildrenWithHeuristics(node.state.getChildren(), isMax);
    	//If it is a MAX node
    	if(isMax) {
    		double max = Double.NEGATIVE_INFINITY;
	    	for(GameStateChild successor : childrenList) {
	    		double successorV = minimaxValue(successor, depth - 1, alpha, beta, !isMax, bestChild);
	    		// Update max node if this successor is larger, also stores the best child
	    		if(successorV > max) {
	    			max = successorV;
	    			bestChild[0] = successor;
	    		}
	    		
	    		//if the backup value is larger than beta, prune other successors by immediately returning 
	    		if(max  > beta) {
	    			break;
	    		}
	    		//otherwise update the alpha value with larger of alpha and backup value
	    		else {
	    			alpha = Double.max(alpha, max);
	    		}
	    	}
	    	
	    	return max;
	    }
    	
    	//If it is a MIN node
    	else {
        	double min = Double.MAX_VALUE;
        	//Calculate minimax value of each successor
	    	for(GameStateChild successor : childrenList) {
	    		double successorV = minimaxValue(successor, depth - 1, alpha, beta, !isMax, bestChild);
	    		min = Double.min(successorV, min);
	    		//if the backup value is less than alpha, prune other successors by immediately returning
	    		if(min  < alpha) {
	    			break;
	    		}
	    		//otherwise update the beta value with smaller of beta and backup value
	    		else {
	    			beta = Double.min(beta, min);
	    		}
	    		//Since it is a MIN node, its minimax value is the smallest of its successors, it also stores the best successor
	    	}
	    	return min;
    	}
    }

    /**
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children, boolean isMax)
    {
    	if(isMax) {
        	children.sort((c1, c2) -> ((int)(c1.state.orderingHeuristics() - c2.state.orderingHeuristics())));
    	}
    	else {
        	children.sort((c1, c2) -> ((int)(c2.state.orderingHeuristics() - c1.state.orderingHeuristics())));
    	}
    	return children;
    }
}
