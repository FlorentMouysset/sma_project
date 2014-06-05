package rsma.aux.robot;

import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.util.Position;

public interface IRobotPerception {
	/**
	 * ALL : search everyhere
	 * FRONT : search front of the robot (front is define by the aim)
	 * BEHIND : search behind of the robot (behind is define by the aim)
	 * !! CAREFULL : FRONT && BEHIND juste if you are in lane !!
	 * */
	public enum SEARCH_PERCEPTION {ALL, FRONT, BEHIND};
	
	boolean checkSuicideBeforStartCycle();

	void doPerception();

	/**
	 * Return true if the current position is a lane exit
	 * */
	boolean perceptionCurrentPositionIsLaneExit();

	/**
	 * Return the most close position where WE are (based of the map perception). Return null if WE doesn't exist in map perception
	 * */
	Position perceptionHasEntity(WORLD_ENTITY we);

	/**Return true is the WORLD_ENTITY is found in the specified area. If ALL return the most close. Return null if cannot found*/	
	Position perceptionHasEntity(WORLD_ENTITY we, SEARCH_PERCEPTION level);

	/*** Return true is a freeplace to push is found on the perception*/
	boolean perceptionHasFreePlace();

	/**
	 * Return the position of a free place or null if none free place are see.
	 * the most far the free place is return (i hope)
	 * */
	Position searchOnPerceptionFreePlacePosition();

	/**
	 * Return true if the current position is a lane entrance
	 * */
	boolean perceptionCurrentPositionIsLaneEntrance();

	/**
	 * Return the WORLD_ENTITY from a position
	 * */
	WORLD_ENTITY getWorldEntityFromPosition(Position nextPost);

}
