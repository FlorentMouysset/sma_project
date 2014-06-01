package rsma.util;

import java.util.Map;

import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;


public class WarehouseChangement {
	private Map<Position,WORLD_ENTITY> mapModification;
	
	public WarehouseChangement(Map<Position, WORLD_ENTITY> mapModification) {
		super();
		this.mapModification = mapModification;
	}

	public Map<Position,WORLD_ENTITY> getMap(){
		return this.mapModification;
	}
	
}
