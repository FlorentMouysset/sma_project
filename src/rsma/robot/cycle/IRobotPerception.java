package rsma.robot.cycle;

import java.util.List;

import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.util.Position;

public interface IRobotPerception {
	/**
	 * ALL : search everyhere

	 * */
	public enum SEARCH_PERCEPTION {ALL, LEFT, RIGHT;

	public SEARCH_PERCEPTION reverse() {
		SEARCH_PERCEPTION ret = this;
		if(ret == LEFT){
			ret = RIGHT;
		}else{
			ret = LEFT;
		}
		return ret;
	}};
	
	boolean checkSuicideBeforStartCycle();

	void doPerception();

	/**
	 * Return true if the current position is a lane exit
	 * */
	boolean perceptionCurrentPositionIsLaneExit(SEARCH_PERCEPTION perceptionType);

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
	 * the most far the free place is at the end of the list (i hope)
	 * */
	List<Position> searchOnPerceptionFreePlacesPositionOnPushZone();

	/**
	 * Return true if the current position is a lane entrance
	 * */
	boolean perceptionCurrentPositionIsLaneEntrance(SEARCH_PERCEPTION perceptionType);

	/**
	 * Return the WORLD_ENTITY from a position
	 * @param nextPost2 
	 * */
	WORLD_ENTITY getWorldEntityFromPosition(Position currentPosition, Position nextPost);

	boolean isInLane(Position currentPosition);

	List<Position> searchOnPerceptionResourcesPlacesPositionOnPushZone();

	boolean yWallDetect(int xOffset);

	List<Position> searchOnPerceptionFreePlacesPositionOnPullZone();

}
