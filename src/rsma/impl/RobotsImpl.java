package rsma.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rsma.EcoJoining;
import rsma.Robots;
import rsma.interfaces.IRobotActions;
import rsma.interfaces.IEnvironnementAnalysis.WORDL_ENTITY;
import rsma.util.ConfigurationManager;
import rsma.util.Position;

public class RobotsImpl extends Robots{

	private List<Robot.Component> robotList = new ArrayList<Robot.Component>();
	private Random genRand;
	
	@Override
	protected void start() {
		System.out.println("RBS : Start Robots");
		genRand = new Random();
		int nbrobotsStart = Integer.parseInt(ConfigurationManager.getProperty("NB_ROBOTS_START"));
		int x_size = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_X_LENGHT"));
		int y_size = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_Y_LENGHT"));
		for(int i=0; i<nbrobotsStart; i++){
			System.out.println("création du robots :" + i);
			Position position = getAFreePlace(x_size, y_size); 
			robotList.add(newRobot(i+"", position));
			//TODO put the robot into environment
		}
	};
	
	private Position getAFreePlace(int x_size, int y_size){
		Position position;
		int x, y;
		do{
			x = genRand.nextInt(x_size);
			y = genRand.nextInt(y_size);
			position = new Position(x, y);
		}while(!requires().pEnvLookAt().getWordEntityAt(position).equals(WORDL_ENTITY.EMPTY)); //POF ! Fix Me ??
		return position;
	}
	
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
