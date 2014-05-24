package rsma.impl;

import rsma.Environnement;
import rsma.Robots;
import rsma.Sheduler;
import rsma.Warehouse;

public class WarehouseImpl extends Warehouse{

	@Override
	protected Sheduler make_sheduler() {
		return new ShedulerImpl();
	}

	@Override
	protected Environnement make_environnement() {
		return new EnvironnementImpl();
	}
	
	@Override
	protected Robots make_robots() {
		return new RobotsImpl();
	}

}
