package rsma.cycle.robot.impl;

import rsma.impl.RobotImpl;
import rsma.interfaces.IEnvironnementActions;
import rsma.robot.cycle.IRobotAction;
import rsma.robot.cycle.IRobotDecision;
import rsma.robot.cycle.IRobotDecision.INTERNAL_ACTION;
import rsma.util.Position;

public class RobotAction implements IRobotAction{
	private RobotImpl robotAgent;
	private IEnvironnementActions envAction;

	
	public RobotAction(RobotImpl robotAgent, IEnvironnementActions environnementActions) {
		this.robotAgent = robotAgent;
		this.envAction = environnementActions;
	}

	@Override
	public Position doAction(Position actionPost, IRobotDecision robotDecition) {
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
		return currentPosition;
	}
}
