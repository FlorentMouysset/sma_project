package rsma.interfaces;

import rsma.util.Position;


public interface IEnvironnementActions {

	/**Move a robot from its old position to the new position </br>
	 * The robot must check if the newPosition is free.
	 * */
	void moveRobot(Position oldPosition , Position newPosition);
	void pullResource(Position position);
	void pushResource(Position position);
	
	
	void moveLane();
	
}
