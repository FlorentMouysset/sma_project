package rsma.impl;

import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import rsma.GUI;
import rsma.interfaces.IEnvironnementAnalysis.WORDL_ENTITY;
import rsma.util.Position;
import rsma.util.WarehouseChangement;

public class GUIImpl extends GUI {

	@Override
	protected void start() {
		super.start();
		
		requires().guiWarehousePort().registerObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				WarehouseChangement change = (WarehouseChangement)arg;
				changementIsPush(change);
			}
		});
	}
	
	private void changementIsPush(WarehouseChangement change){
		System.out.println("GUI update");
		for(Entry<Position, WORDL_ENTITY> entry : change.getMap().entrySet()){
			Position position = entry.getKey();
			WORDL_ENTITY we = entry.getValue();
			//Oudom TODO
		}
	}
	
}
