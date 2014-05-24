package rsma.impl;

import rsma.Sheduler;
import rsma.interfaces.IWarehouseActions;

public class ShedulerImpl extends Sheduler{

	@Override
	protected IWarehouseActions make_shedulerLaunchPort() {
		return new IWarehouseActions() {
			
			@Override
			public void launchArrangement(int nbRobots) {
				System.out.println("Le sheduler doit faire un cyle");
				requires().shedulerSMAPort().doCycle();
				System.out.println("Le sheduler a fait un cyle : fin");
			}
		};
	}

}
