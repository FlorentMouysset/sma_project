package rsma.impl;

import rsma.Environnement;
import rsma.interfaces.IEnvironnementActions;
import rsma.interfaces.IEnvironnementAnalysis;
import rsma.interfaces.IEnvironnementObservable;
import rsma.util.Position;

public class EnvironnementImpl extends Environnement {

	public enum WORDL_ENTITY {EMPTY, WALL, RESOURCE, ROBOT, ROBOT_AND_RESOURCE /*, PLACE_PULL, PUSH_RESOURCE*/}
	
	private WORDL_ENTITY[][] world;
	

	
	@Override
	protected IEnvironnementAnalysis make_envLookAtPort() {
		return new IEnvironnementAnalysis() {

			@Override
			public WORDL_ENTITY getWordEntityAt(Position position) {
				return world[position.getX()][position.getY()];
			}
		};
	}

	@Override
	protected IEnvironnementActions make_envActions() {
		return new IEnvironnementActions() {
			
			@Override
			public void pushResource(Position position) {
				world[position.getX()][position.getY()] = WORDL_ENTITY.RESOURCE;
			}
			
			@Override
			public void pullResource(Position position) {
				world[position.getX()][position.getY()] = WORDL_ENTITY.EMPTY;
				
			}
			
			@Override
			public void moveRobot(Position oldPosition, Position newPosition) {
				WORDL_ENTITY oldRobot = world[oldPosition.getX()][oldPosition.getY()];
				world[oldPosition.getX()][oldPosition.getY()] = WORDL_ENTITY.EMPTY;
				world[newPosition.getX()][newPosition.getY()] = oldRobot;
				
			}
			
			@Override
			public void moveLane() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	protected IEnvironnementObservable make_envObservable() {
		// TODO Auto-generated method stub
		return null;
	}

}
