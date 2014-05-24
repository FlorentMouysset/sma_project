package rsma.util;

import java.util.Map;

import rsma.interfaces.IEnvironnementAnalysis.WORDL_ENTITY;


public class WarehouseChangement {
	private Map<Position,WORDL_ENTITY> mapModification;
	
	public WarehouseChangement(Map<Position, WORDL_ENTITY> mapModification) {
		super();
		this.mapModification = mapModification;
	}

	public Map<Position,WORDL_ENTITY> getMap(){
		return this.mapModification;
	}
	
}
