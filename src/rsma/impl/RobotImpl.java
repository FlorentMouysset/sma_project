package rsma.impl;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import rsma.Robots.Robot;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.interfaces.IRobotActions;
import rsma.util.ConfigurationManager;
import rsma.util.Position;

public class RobotImpl extends Robot{
	private final String id;
	private static Rectangle pullZone;
	private static Rectangle pushZone;
	private static int X_MAX;
	private static int Y_MAX;
	
	private Position currentPosition;
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
		RobotImpl.pullZone = pullZone;
		RobotImpl.pushZone = pushZone;
		
		RobotImpl.X_MAX = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_X_LENGHT"));
		RobotImpl.Y_MAX = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_Y_LENGHT"));
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
		WORLD_ENTITY we;
		Position posit;
		for(int x=0; x<7; x++){
			for(int y=0; y<7; y++){
				posit = new Position(currentPosition.getX() - (3-x), currentPosition.getY() - (3-y));
				if(positionIsValide(posit)){
					we = eco_requires().pEnvLookAt().getWorldEntityAt(posit);
				}else{
					we = null;
				}
				localTempPercep[y][x] = we;
			}
		}
	}
	
	

	private void doDecision() {
		// TODO Auto-generated method stub
		
	}
	
	private void doAction() {
		
		System.out.println("Le robot "+ id +" va bouger");
		Position positEnd = new Position(currentPosition.getX()+1, currentPosition.getY()+1);
		eco_requires().pEnvAction().moveRobot(currentPosition, positEnd);
		currentPosition = positEnd;
	}
	
	private static boolean positionIsValide(Position posit) {
		return posit.getX()>0 && posit.getY()>0 && posit.getX()<X_MAX && posit.getY()<Y_MAX;
	}
	
}
