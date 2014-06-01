package rsma.interfaces;

import java.awt.Rectangle;

import rsma.util.Position;

public interface IEnvironnementAnalysis {

	/**The different entity perceptible by the robots*/
	public enum WORLD_ENTITY {EMPTY, WALL, RESOURCE, ROBOT, ROBOT_AND_RESOURCE}

	/**Return the WORDL_ENTITY of a position */
	WORLD_ENTITY getWorldEntityAt(Position position);
	
	/**
	 * return the Rectangle of the pull zone.
	 * 
	 * */
	Rectangle getPullZone();
	
	/**
	 * return the Rectangle of the push zone.
	 * 
	 * */
	Rectangle getPushZone();
	
}
