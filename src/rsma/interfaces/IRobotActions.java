package rsma.interfaces;

public interface IRobotActions {

	/**
	 * Make a cycle into the Robot.
	 * @return true if the agent is still alive, false is the agent has suicide.
	 * */
	boolean doCycle();
	
	
}
