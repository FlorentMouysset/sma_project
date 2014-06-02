package rsma.impl;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import rsma.Robots.Robot;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.interfaces.IRobotActions;
import rsma.util.Position;

public class RobotImpl extends Robot{
	private final String id;
	private Position currentPosition;
	private Rectangle pullZone;
	private Rectangle pushZone;
	private INTERNAL_STATE state = INTERNAL_STATE.NORMAL;
	private INTERNAL_AIM aim = INTERNAL_AIM.PULL_AIM;
	private Map<INTERNAL_LANE_STATUS, Position> laneMap = new HashMap<INTERNAL_LANE_STATUS, Position>();
	private WORLD_ENTITY[][] localTempPercep = new WORLD_ENTITY[7][7];
	private WORLD_ENTITY externalState = WORLD_ENTITY.ROBOT;
	
	private enum INTERNAL_STATE {NORMAL, LANE, FOLLOW_LANE, FORCE};
	private enum INTERNAL_AIM {PULL_AIM, PUSH_AIM};
	private enum INTERNAL_LANE_STATUS {TRY, PULL_LANE, PUSH_LANE};
	
	public RobotImpl(String id, Position positionInit, Rectangle pullZone, Rectangle pushZone){
		this.id = id;
		this.currentPosition = positionInit;
		this.pullZone = pullZone;
		this.pushZone = pushZone;
		
	}
	
	@Override
	protected IRobotActions make_roboActionPort() {
		return new IRobotActions() {
			
			@Override
			public boolean doCycle() {
				boolean doSuicide = checkSuicideBeforStartCycle();
				if(!doSuicide){
					doPerception();
					doDecision();
					doAction();
				}
				return doSuicide;
			}
		
		};
	}
	
	
	private boolean checkSuicideBeforStartCycle() {
		return eco_requires().pEnvLookAt().getWorldEntityAt(currentPosition).equals(WORLD_ENTITY.WALL);		
	}

	private void doPerception() {
		//init the local temp perception map
		localTempPercep[3][3]=externalState;
		
		
		WORLD_ENTITY we = eco_requires().pEnvLookAt().getWorldEntityAt(new Position(0, 0));
		
		
	}
	
	private void doDecision() {
		// TODO Auto-generated method stub
		
	}
	
	private void doAction() {
		
		System.out.println("Le robot "+ id +" va bouger");
		eco_requires().pEnvAction().moveRobot(new Position(0, 0), new Position(1, 1));
	}
	
	
	
}
