package rsma.impl;

import java.util.ArrayList;
import java.util.List;

import rsma.EcoJoining;
import rsma.Robots;
import rsma.interfaces.IRobotActions;
import rsma.util.Position;

public class RobotsImpl extends Robots{

	private List<Robot.Component> robotList = new ArrayList<Robot.Component>();
	
	@Override
	protected void start() {
		for(int i=0; i<3; i++){//TODO
			System.out.println("création de robots ...");
			robotList.add(newRobot(i+"", new Position(0,0)));
		}
		
	};
	
	@Override
	protected IRobotActions make_robotsShedulingPort() {
		return new IRobotActions() {
			
			@Override
			public void doCycle() {
				for(Robot.Component robot : robotList){
					System.out.println("ECO robots : lancement d'un clycle pour un robot");
					robot.roboActionPort().doCycle();
				}
			}
		};
	}

	@Override
	protected EcoJoining make_pJoin() {
		return new EcoJoinImpl();
	}

	@Override
	protected Robot make_Robot(String id, Position positionInit) {
		return new RobotImpl(id, positionInit);
	}


}
