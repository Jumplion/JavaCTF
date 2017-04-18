package ctf.agent;

import java.util.*;
import ctf.common.*;


public class txb130030Agent extends Agent 
{
	int xPos = 0, yPos = 0;
	
	//
	int mapSize;
	boolean knowMapSize = false;
	//
	
	public enum ObsType
	{
		None(0), Obstacle(1), Friend(2), Foe(3), TeamFlag(4), EnemFlag(5);
		private int value;
		private ObsType(int val){
			this.value = val;
		}
	}

	Map<String, ObsType> map;
	
	public int getMove(AgentEnvironment enviro) 
	{
		// Do we want the enemy flag or we have the enemy flag? so make our goal our base
		int goal = !enviro.hasFlag() ? enviro.ENEMY_TEAM : enviro.OUR_TEAM;

		// booleans describing direction of goal
		// goal is either enemy flag, or our base
		boolean goalNorth = enviro.isBaseNorth( goal, false );
		boolean goalSouth = enviro.isBaseSouth( goal, false );
		boolean goalEast = enviro.isBaseEast( goal, false );
		boolean goalWest = enviro.isBaseWest( goal, false );

		// now we have direction booleans for our goal	
		// check for immediate obstacles blocking our path		
		boolean obstNorth = enviro.isObstacleNorthImmediate();
		boolean obstSouth = enviro.isObstacleSouthImmediate();
		boolean obstEast = enviro.isObstacleEastImmediate();
		boolean obstWest = enviro.isObstacleWestImmediate();
		
		// Check north node for obstacles
		addNode(enviro, xPos, yPos+1, 0);	// North
		addNode(enviro, xPos, yPos-1, 1);	// South
		addNode(enviro, xPos+1, yPos, 2);	// East
		addNode(enviro, xPos-1, yPos, 3);	// West

		// If the goal is towards the north
		if( goalNorth){			
			
			// if the goal is north only, and we're not blocked, move north		
			if( ! goalEast && ! goalWest && !obstNorth )
				return AgentAction.MOVE_NORTH;
			
			// if goal both north and east
			if(goalEast){
				// pick north or east for move with 50/50 chance
				if( Math.random() < 0.5 && !obstNorth )
					return AgentAction.MOVE_NORTH;
				if( !obstEast )
					return AgentAction.MOVE_EAST;
				if( !obstNorth )
					return AgentAction.MOVE_NORTH;	
			}
			// if goal both north and west			
			if(goalWest){
				// pick north or west for move with 50/50 chance
				if( Math.random() < 0.5 && !obstNorth )
					return AgentAction.MOVE_NORTH;
				if( !obstWest )	
					return AgentAction.MOVE_WEST;
				if( !obstNorth )
					return AgentAction.MOVE_NORTH;				
			}
		}	
			
		// If the goal is towards the South
		else if(goalSouth){
			// if the goal is south only, and we're not blocked, move south
			if( !goalEast && !goalWest && !obstSouth )
				return AgentAction.MOVE_SOUTH;
			
			// do same for south-east and south-west as for north versions	
			if( goalEast ) {
				if( Math.random() < 0.5 && !obstSouth )
					return AgentAction.MOVE_SOUTH;
				if( !obstEast )
					return AgentAction.MOVE_EAST;
				if( !obstSouth )
					return AgentAction.MOVE_SOUTH;
			}
			
			if( goalWest ) {
				if( Math.random() < 0.5 && !obstSouth )
					return AgentAction.MOVE_SOUTH;
				if( !obstWest )
					return AgentAction.MOVE_WEST;
				if( !obstSouth )
					return AgentAction.MOVE_SOUTH;
			}
		}
		
		// if the goal is east only, and we're not blocked
		if( goalEast && !obstEast )
			return AgentAction.MOVE_EAST;
		
		// if the goal is west only, and we're not blocked	
		if( goalWest && !obstWest )
			return AgentAction.MOVE_WEST;
		
		// otherwise, make any unblocked move
		if( !obstNorth )
			return AgentAction.MOVE_NORTH;
		else if( !obstSouth )
			return AgentAction.MOVE_SOUTH;
		else if( !obstEast )
			return AgentAction.MOVE_EAST;
		else if( !obstWest ) 
			return AgentAction.MOVE_WEST;
		
		// completely blocked!
		else
			return AgentAction.DO_NOTHING;	
	}
	
	private ObsType getNode(int x, int y)
	{
		String key = x + ", " + y;
		return map.get(key);
	}
	
	// Adds a node at a given IMMEDIATE X,Y coordinate.
	// NSEW tells us in what direction relative to the Agent the node we're checking is
	private ObsType addNode(AgentEnvironment env, int x, int y, int NSEW)
	{
		String key = x + ", " + y;
		
		if(checkObsAdjacent(env, NSEW))
			return map.put(key, ObsType.Obstacle);
		
		else if(checkAgentAdjacent(env, NSEW, false))
			return map.put(key,ObsType.Friend);
		else if(checkAgentAdjacent(env, NSEW, true))
			return map.put(key,ObsType.Foe);
		
		else if(checkFlagAdjacent(env, NSEW, false))
			return map.put(key,ObsType.TeamFlag);
		else if(checkFlagAdjacent(env, NSEW, true))
			return map.put(key,ObsType.EnemFlag);	
		
		else
			return map.put(key,ObsType.None);
	}
	
	private boolean checkAgentAdjacent(AgentEnvironment e, int NSEW, boolean enemTeam)
	{
		if(NSEW > 3)
			NSEW = 3;
		else if (NSEW < 0)
			NSEW = 0;
		
		boolean result = false;
		
		if(enemTeam){
			switch(NSEW){
				case 0:
					result = e.isAgentNorth(e.ENEMY_TEAM, true);
				case 1:
					result = e.isAgentSouth(e.ENEMY_TEAM, true);
				case 2:
					result = e.isAgentEast(e.ENEMY_TEAM,  true);
				case 3:
					result = e.isAgentWest(e.ENEMY_TEAM, true);
			}
		}
		else{
			switch(NSEW){
				case 0:
					result = e.isAgentNorth(e.OUR_TEAM, true);
				case 1:
					result = e.isAgentSouth(e.OUR_TEAM, true);
				case 2:
					result = e.isAgentEast(e.OUR_TEAM,  true);
				case 3:
					result = e.isAgentWest(e.OUR_TEAM, true);
			}	
		}
		return result;
	}

	private boolean checkFlagAdjacent(AgentEnvironment e, int NSEW, boolean enemTeam)
	{
		if(NSEW > 3)
			NSEW = 3;
		else if (NSEW < 0)
			NSEW = 0;
		
		boolean result = false;
		
		if(enemTeam){
			switch(NSEW){
				case 0:
					result = e.isFlagNorth(e.ENEMY_TEAM, true);
				case 1:
					result = e.isFlagSouth(e.ENEMY_TEAM, true);
				case 2:
					result = e.isFlagEast(e.ENEMY_TEAM,  true);
				case 3:
					result = e.isFlagWest(e.ENEMY_TEAM, true);
			}
		}
		else{
			switch(NSEW){
				case 0:
					result = e.isFlagNorth(e.OUR_TEAM, true);
				case 1:
					result = e.isFlagSouth(e.OUR_TEAM, true);
				case 2:
					result = e.isFlagEast(e.OUR_TEAM,  true);
				case 3:
					result = e.isFlagWest(e.OUR_TEAM, true);
			}	
		}
		return result;
	}
	
	private boolean checkObsAdjacent(AgentEnvironment e, int NSEW)
	{
		if(NSEW > 3)
			NSEW = 3;
		else if (NSEW < 0)
			NSEW = 0;
		
		boolean result = false;
		
		switch(NSEW){
			case 0:
				result = e.isObstacleNorthImmediate();
			case 1:
				result = e.isObstacleSouthImmediate();
			case 2:
				result = e.isObstacleEastImmediate();
			case 3:
				result = e.isObstacleWestImmediate();
		}
		
		return result;
	}

	
	private ObsType getNorth()
	{
		return getNode(xPos, yPos+1);
	}
	private ObsType getSouth()
	{
		return getNode(xPos, yPos-1);
	}
	private ObsType getEast()
	{
		return getNode(xPos+1, yPos);
	}
	private ObsType getWest()
	{
		return getNode(xPos-1, yPos);
	}
}