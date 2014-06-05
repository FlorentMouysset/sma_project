package rsma.aux.robot;

import rsma.util.Position;

public interface IRobotAction {

	/**
	 * @param robotDecition 
	 * @param currentPosition 
	 * @return 
	 * 
	 * */
	void doAction(Position nextPost, IRobotDecision robotDecition);

}
