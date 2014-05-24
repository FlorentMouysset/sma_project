package rsma.interfaces;

import rsma.util.Position;

public interface IEnvironnementAnalysis {

	public enum WORDL_ENTITY {EMPTY, WALL, RESOURCE, ROBOT, ROBOT_AND_RESOURCE /*, PLACE_PULL, PUSH_RESOURCE*/}

	/**Return the WORDL_ENTITY of a position */
	WORDL_ENTITY getWordEntityAt(Position position);
	
}
