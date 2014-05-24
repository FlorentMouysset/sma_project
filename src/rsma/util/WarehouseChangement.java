package rsma.util;

import java.util.HashMap;
import java.util.Map;

import rsma.interfaces.IEnvironnementAnalysis;
import rsma.interfaces.IEnvironnementAnalysis.WORDL_ENTITY;


public class WarehouseChangement {
	private Map<Position,WORDL_ENTITY> mapModification = new HashMap<Position, IEnvironnementAnalysis.WORDL_ENTITY>(); 

	
	public void addChangement(Position position, WORDL_ENTITY we){
		mapModification.put(position, we);
	}
	
	public Map<Position,WORDL_ENTITY> getMap(){
		return this.mapModification;
	}
	
}
