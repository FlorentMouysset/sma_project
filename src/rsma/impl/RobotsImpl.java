package rsma.impl;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rsma.EcoJoining;
import rsma.Robots;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.interfaces.IRobotActions;
import rsma.util.ConfigurationManager;
import rsma.util.Position;

public class RobotsImpl extends Robots{

	private List<Robot.Component> robotList = new ArrayList<Robot.Component>();
	private Random genRand;
	private int cpt;
	
	@Override
	protected void start() {
		System.out.println("RBS : Start Robots");
		genRand = new Random();
		int nbrobotsStart = Integer.parseInt(ConfigurationManager.getProperty("NB_ROBOTS_START"));
		int x_size = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_X_LENGHT"));
		int y_size = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_Y_LENGHT"));
		Rectangle pullZone = requires().pEnvLookAt().getPullZone();
		Rectangle pushZone = requires().pEnvLookAt().getPushZone();
		for(int i=0; i<nbrobotsStart; i++){
			System.out.println("création du robots :" + i);
			Position position = getAFreePlace(x_size, y_size); 
			robotList.add(newRobot(i+"", position, pullZone, pushZone));
			requires().pEnvAction().addRobot(position);
		}
	};
	
	private Position getAFreePlace(int x_size, int y_size){
		Position position;
		int x, y;
		do{
			x = genRand.nextInt(x_size);
			y = genRand.nextInt(y_size);
			cpt++;
			if(cpt==1){
				x=11;
				y=2;
			}else if(cpt==2){
				x=75;
				y=34;
			}else if(cpt==3){
				x=76;
				y=34;
			}else if(cpt==4){
				x=10;
				y=2;
			}
			position = new Position(x, y);
		}while(!requires().pEnvLookAt().getWorldEntityAt(position).equals(WORLD_ENTITY.EMPTY)); //POF ! Fix Me ??
		return position;
	}
	
	@Override
	protected IRobotActions make_robotsShedulingPort() {
		return new IRobotActions() {
			
			@Override
			public boolean doCycle() {
				List<Robot.Component> robotListSuicide = new ArrayList<Robot.Component>();
				boolean result;
				for(Robot.Component robot : robotList){
					//System.out.println("ROBOTS robots : lancement d'un clycle pour un robot");
					result = robot.roboActionPort().doCycle();
					if(result){
						robotListSuicide.add(robot);
					}
				}
				robotList.removeAll(robotListSuicide);
				return robotList.isEmpty();
			}
		};
	}

	@Override
	protected EcoJoining make_pJoin() {
		return new EcoJoinImpl();
	}

	@Override
	protected Robot make_Robot(String id, Position positionInit, Rectangle pullZone, Rectangle pushZone) {
		return new RobotImpl(id, positionInit, pullZone, pushZone);
	}


}
