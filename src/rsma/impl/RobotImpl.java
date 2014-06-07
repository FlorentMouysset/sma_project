package rsma.impl;

import java.awt.Rectangle;

import rsma.Robots.Robot;
import rsma.cycle.robot.impl.RobotAction;
import rsma.cycle.robot.impl.RobotDecision;
import rsma.cycle.robot.impl.RobotKnowlage;
import rsma.cycle.robot.impl.RobotPerception;
import rsma.cycle.robot.impl.RobotUtils;
import rsma.interfaces.IRobotActions;
import rsma.robot.cycle.IRobotAction;
import rsma.robot.cycle.IRobotDecision;
import rsma.robot.cycle.IRobotKnowlage;
import rsma.robot.cycle.IRobotPerception;
import rsma.util.ConfigurationManager;
import rsma.util.Position;

public class RobotImpl extends Robot{
	private final String id;

	private Position currentPosition;

	public enum INTERNAL_AIM {PULL_AIM, PUSH_AIM};
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
