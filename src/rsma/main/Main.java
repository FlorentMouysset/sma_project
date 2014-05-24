package rsma.main;

import rsma.GUI;
import rsma.GUI.Requires;
import rsma.Warehouse;
import rsma.impl.GUIImpl;
import rsma.impl.WarehouseImpl;
import rsma.interfaces.IEnvironnementObservable;

public class Main {

	public static void main(String[] args) {
		
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

		warehouse.warehouseActionPort().launchArrangement(10);//lauch
	}

}