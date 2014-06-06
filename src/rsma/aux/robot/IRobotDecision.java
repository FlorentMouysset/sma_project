package rsma.aux.robot;

import rsma.util.Position;

public interface IRobotDecision {

	public enum INTERNAL_ACTION {WALK, PULL, PUSH, NOTHING};

	/**
	 * take a action (update the attribute action) and eventually update the aim, the lane knowledge, the state and the external state.
	 * Return the next position of action or null if suicide**/
	Position doDecision();

	INTERNAL_ACTION getActionToDo();

}
