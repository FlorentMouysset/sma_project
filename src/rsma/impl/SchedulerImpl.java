package rsma.impl;

import rsma.Sheduler;
import rsma.interfaces.IWarehouseScheduleActions;

public class SchedulerImpl extends Sheduler{

	@Override
	protected IWarehouseScheduleActions make_shedulerLaunchPort() {
		return new IWarehouseScheduleActions() {
			private boolean run = false;
			private Object lock = new Object();
			
			@Override
			public void launchArrangement(int nbRobots) {
				boolean result;
				run = true;
				do{
					if(!run){
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//System.out.println("Le sheduler doit faire un cyle");
					result = requires().shedulerSMAPort().doCycle();
					//System.out.println("Le sheduler a fait un cyle : fin");
				}while(!result);//result == true when all robot are suicide
			}

			@Override
			public void pauseArrangement() {
				run = false;
			}

			@Override
			public void resumeArrangement() {
				run= true;
				lock.notify();
			}

			@Override
			public boolean doNextCycle() {
				run = false;
				return requires().shedulerSMAPort().doCycle();
			}
		};
	}

}
