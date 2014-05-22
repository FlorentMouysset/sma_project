package rsma.impl;

import rsma.Sheduler;
import rsma.interfaces.IWarehouseActions;

public class ShedulerImpl extends Sheduler{

	@Override
	protected IWarehouseActions make_shedulerLaunchPort() {
		return new IWarehouseActions() {
			
			@Override
			public void launchArrangement(int nbRobots) {
				// TODO Auto-generated method stub
				
			}
		};
	}

}
