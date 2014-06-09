package rsma.interfaces;

import rsma.util.Position;


public interface IEnvironnementActions {

	/**Move a robot from its old position to the new position </br>
	 * The robot must check if the newPosition is free.
	 * */
	void moveRobot(Position oldPosition , Position newPosition);
	
	/**
	 * Pull a resource at the Position "position" and update the environment.</br>
	 * No check :<br/>
	 * <li>if the robot is on the 8-neighborhood of "position"</li>
	 * <li>the resource is reachable</li>
	 * <li>the resource is into the pull zone</li>
	 * <li>the resource exist at the Position position</li>
	 * 
	 * @param resrcPost the Position of the resource
	 * @param robotPost the position of the robot
	 * */
	void pullResource(Position resrcPost, Position robotPost);
	
	
	/**
	 * Push a resource at the Position "position" and update the environment.</br>
	 * No check :<br/>
	 * <li>if the robot is on the 8-neighborhood of "position"</li>
	 * <li>the free place is reachable</li>
	 * <li>the free place is into the push zone</li>
	 * <li>the free place exist at the Position position</li>
	 * 
	 * @param freePlacePost the Position of the free place
	 * @param robotPost the position of the robot
	 * */
	void pushResource(Position freePlacePost, Position robotPost);
	
	/**Move the lane from the old high to the new high. </br>
	 * All robots into the lane are kill.
	 * @param laneId 0 or 1*/
	void moveLane(int laneId,  int newHigh);
	
	/**
	 * Add into the environment a new empty robot at position.
	 * No check if the position is a free place
	 * 
	 * @param robotPost the position of the new robot
	 * */
	void addRobot(Position robotPost);
	
	
	void suicideRobot(Position robotPost);
	
}
