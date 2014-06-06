package rsma.aux.robot;

import rsma.util.Position;

public interface IRobotAction {

	/**
	 * @param robotDecition 
	 * @param currentPosition 
	 * @return 
	 * 
	 * */
	Position doAction(Position nextPost, IRobotDecision robotDecition);

}
