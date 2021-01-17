package sma.actionsBehaviours;

/**
 * This class contain all the authorized movemenst/actions of the agent in the environment
 * 
 *  8 possible direction (with the field of vision in the movment extension)
 *  8 possible orientation (no move, only a rotation and so only a modification of the field of vision)
 *  to fire
 *  
 * @author WonbinLIM
 *
 */
public class LegalActions {
	
	
	/**
	 * All the agent's possible actions.
	 * Shoot
	 * MoveNorth, MoveNortheast, MoveEast, MoveSouthEast, MoveSouth, MoveSouthWest, MoveWest, MoveNorthWest
	 * LookNorth, LookNortheast, LookEast, LookSouthEast, LookSouth, LookSouthWest, LookWest, LookNorthWest
	 * 
	 * @author WonbinLIM
	 *
	 */
	public enum LegalAction {
		SHOOT (0),
		MOVE_NORTH (1), MOVE_NORTHEAST (2), MOVE_EAST (3), MOVE_SOUTHEAST (4), MOVE_SOUTH (5), MOVE_SOUTHWEST (6), MOVE_WEST (7), MOVE_NORTHWEST (8),
		LOOKTO_NORTH (9), LOOKTO_NORTHEAST (10), LOOKTO_EAST (11), LOOKTO_SOUTHEAST (12), LOOKTO_SOUTH (13), LOOKTO_SOUTHWEST (14), LOOKTO_WEST (15), LOOKTO_NORTHWEST (16);
		
		public int id;
		private LegalAction(int id) {
			this.id = id;
		}
	}
	
	
	public enum Orientation {
		LOOKTO_NORTH, LOOKTO_NORTHEAST, LOOKTO_EAST, LOOKTO_SOUTHEAST, LOOKTO_SOUTH, LOOKTO_SOUTHWEST, LOOKTO_WEST, LOOKTO_NORTHWEST
	}
	
	
	/**
	 * Returns the Looking LegalAction of a Moving LegalAction, with the same direction.
	 * @param action the Moving LegalAction.
	 * @return the Looking LegalAction.
	 */
	public static LegalAction MoveToLook(LegalAction action){
		switch(action){
		case MOVE_NORTH :
			return LegalAction.LOOKTO_NORTH;
		case MOVE_NORTHEAST:
			return LegalAction.LOOKTO_NORTHEAST;
		case MOVE_EAST:
			return LegalAction.LOOKTO_EAST;
		case MOVE_SOUTHEAST:
			return LegalAction.LOOKTO_SOUTHEAST;
		case MOVE_SOUTH:
			return LegalAction.LOOKTO_SOUTH;
		case MOVE_SOUTHWEST:
			return LegalAction.LOOKTO_SOUTHWEST;
		case MOVE_WEST:
			return LegalAction.LOOKTO_WEST;
		case MOVE_NORTHWEST:
			return LegalAction.LOOKTO_NORTHWEST;
		default:
			System.out.println("Error, no compatible action");
		System.exit(-1);
		return LegalAction.LOOKTO_NORTH;
		}
		
	}
}
