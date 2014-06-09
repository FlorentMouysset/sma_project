package rsma.main;

import rsma.GUI;
import rsma.GUI.Requires;
import rsma.Warehouse;
import rsma.impl.GUIImpl;
import rsma.impl.WarehouseImpl;
import rsma.interfaces.IEnvironnementObservable;
import rsma.util.ConfigurationManager;

public class Main {

	public static void main(String[] args) {
		ConfigurationManager.loadPropertiesFile("warehouse5.properties");
		ConfigurationManager.speed(0);//0= very quik, 1000 low, +1000 very low

		//create the warehouse
		final Warehouse.Component warehouse = (new WarehouseImpl()).newComponent();
		
		//create the requires for the gui
		Requires b = new Requires() {
			
			@Override
			public IEnvironnementObservable guiWarehousePort() {
				return warehouse.envObservablePort(); //the requires is the warehouse observation
			}
		};
		
		GUI.Component gui = (new GUIImpl())._newComponent(b, true); //create the warehouse

		warehouse.warehouseScheduleActionPort().launchArrangement(10);//lauch
	}

}
