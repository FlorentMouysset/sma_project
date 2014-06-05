package rsma.aux.robot.impl;

import rsma.aux.robot.IRobotAction;
import rsma.aux.robot.IRobotDecision;
import rsma.aux.robot.IRobotDecision.INTERNAL_ACTION;
import rsma.impl.RobotImpl;
import rsma.interfaces.IEnvironnementActions;
import rsma.util.Position;

public class RobotAction implements IRobotAction{
	private RobotImpl robotAgent;
	private IEnvironnementActions envAction;

	
	public RobotAction(RobotImpl robotAgent, IEnvironnementActions environnementActions) {
		this.robotAgent = robotAgent;
		this.envAction = environnementActions;
	}

	@Override
	public void doAction(Position actionPost, IRobotDecision robotDecition) {
		Position currentPosition = robotAgent.getCurrentPosition();
		String id = robotAgent.getID();
		INTERNAL_ACTION action = robotDecition.getActionToDo();
		switch (action) {
		case WALK:
			System.out.println("Le robot "+ id +" va bouger");
			envAction.moveRobot(currentPosition, actionPost);
			currentPosition = actionPost;
			break;
		case PULL :
			System.out.println("Le robot "+ id +" va pull");
			envAction.pullResource(actionPost, currentPosition);
			break;
		case PUSH :
			System.out.println("Le robot "+ id +" va push");
			envAction.pushResource(actionPost, currentPosition);
			break;
		case NOTHING:
			break;
		}
	}
}
