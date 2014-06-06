package rsma.interfaces;

public interface IWarehouseScheduleActions {

	void launchArrangement(int nbRobots);
	void pauseArrangement();
	void resumeArrangement();
	
	/**
	 * Return false is the arrangement if over.
	 * */
	boolean doNextCycle();
	
}
