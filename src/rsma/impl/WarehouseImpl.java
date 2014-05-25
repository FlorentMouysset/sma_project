package rsma.impl;

import rsma.Environnement;
import rsma.Robots;
import rsma.Sheduler;
import rsma.Warehouse;
import rsma.util.ConfigurationManager;

public class WarehouseImpl extends Warehouse{

	@Override
	protected void start() {
		System.out.println("WHS : Start Warehouse");
	};
	
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
