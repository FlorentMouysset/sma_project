package rsma.impl;

import java.awt.Rectangle;

import rsma.Robots.Robot;
import rsma.aux.robot.IRobotAction;
import rsma.aux.robot.IRobotDecision;
import rsma.aux.robot.IRobotKnowlage;
import rsma.aux.robot.IRobotPerception;
import rsma.aux.robot.impl.RobotAction;
import rsma.aux.robot.impl.RobotDecision;
import rsma.aux.robot.impl.RobotKnowlage;
import rsma.aux.robot.impl.RobotPerception;
import rsma.aux.robot.impl.RobotUtils;
import rsma.interfaces.IRobotActions;
import rsma.util.ConfigurationManager;
import rsma.util.Position;

public class RobotImpl extends Robot{
	private final String id;

	private Position currentPosition;

	public static enum INTERNAL_AIM {PULL_AIM, PUSH_AIM};
	private INTERNAL_AIM aim = INTERNAL_AIM.PULL_AIM;

	private IRobotPerception robotPerception;
	private IRobotDecision robotDecision;
	private IRobotAction robotAction;
	private IRobotKnowlage robotKnowlage;
	
	public RobotImpl(String id, Position positionInit, Rectangle pullZone, Rectangle pushZone){
		this.id = id;
		this.currentPosition = positionInit;
		RobotUtils.pullZone = pullZone;
		RobotUtils.pushZone = pushZone;
		
		RobotUtils.X_MAX = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_X_LENGHT"));
		RobotUtils.Y_MAX = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_Y_LENGHT"));
				
	}
	
	@Override
	protected void start() {
		super.start();
		robotKnowlage = new RobotKnowlage();
		robotPerception = new RobotPerception(this, eco_requires().pEnvLookAt());
		robotDecision = new RobotDecision(this, robotKnowlage, robotPerception);
		robotAction = new RobotAction(this, eco_requires().pEnvAction());
	}
	
	@Override
	protected IRobotActions make_roboActionPort() {
		return new IRobotActions() {
			
			@Override
			public boolean doCycle() {

				boolean doSuicide = robotPerception.checkSuicideBeforStartCycle();
				if(!doSuicide){
					robotPerception.doPerception();
					Position nextPost = robotDecision.doDecision();
					doSuicide = nextPost == null;
					if(!doSuicide){
						currentPosition = robotAction.doAction(nextPost, robotDecision);
					}
				}
				return doSuicide;
			}
		};
	}
	
	public Position getCurrentPosition(){
		return currentPosition;
	}
	
	public String getID(){
		return id;
	}
	
	public INTERNAL_AIM getAim() {
		return aim;
	}

	public void setAim(INTERNAL_AIM aim) {
		this.aim = aim;
	}
}
