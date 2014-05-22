package rsma.interfaces;

import rsma.util.Position;


public interface IEnvironnementActions {

	void moveRobot(Position oldPosition , Position newPosition);
	void pullResource(Position position);
	void pushResource(Position position);
	
	
	void moveLane();
	
}
