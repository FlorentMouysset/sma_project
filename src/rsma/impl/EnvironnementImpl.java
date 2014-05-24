package rsma.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import rsma.Environnement;
import rsma.interfaces.IEnvironnementActions;
import rsma.interfaces.IEnvironnementAnalysis;
import rsma.interfaces.IEnvironnementAnalysis.WORDL_ENTITY;
import rsma.interfaces.IEnvironnementObservable;
import rsma.util.Position;
import rsma.util.WarehouseChangement;


public class EnvironnementImpl extends Environnement{

	
	protected static final int Y_SIZE = 0;//TODO
	protected static final int X_SIZE = 0;

	private WORDL_ENTITY[][] world = new WORDL_ENTITY[100][100]; //TODO 
	
	// because we cannot extends Observable too, we create a delegate
	private EnvObservable envObserbableDelegate = new EnvObservable();
	 

	@Override
	protected void start() {
		world[1][1] = WORDL_ENTITY.EMPTY;
	};
	
	@Override
	protected IEnvironnementAnalysis make_envLookAtPort() {
		return new IEnvironnementAnalysis() {

			@Override
			public WORDL_ENTITY getWordEntityAt(Position position) {
				System.out.println("ENV : quelqu'un fait un get à " + position + " il y a "+ world[position.getX()][position.getY()]);
				return world[position.getX()][position.getY()];
			}

			@Override
			public Position getPositionOfPullZone() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Position getPositionOfPushZone() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	protected IEnvironnementActions make_envActions() {
		return new IEnvironnementActions() {
			
			@Override
			public void pushResource(Position position) {
				world[position.getX()][position.getY()] = WORDL_ENTITY.RESOURCE;
				notifyChangement(new WarehouseChangement(makeTheSimpleChangingMap(position)));
			}
			
			@Override
			public void pullResource(Position position) {
				world[position.getX()][position.getY()] = WORDL_ENTITY.EMPTY;
				notifyChangement(new WarehouseChangement(makeTheSimpleChangingMap(position)));
			}
			
			@Override
			public void moveRobot(Position oldPosition, Position newPosition) {
				System.out.println("ENV : un robot bouge de " + oldPosition +" à " + newPosition );
				if(world[newPosition.getX()][newPosition.getY()]== WORDL_ENTITY.EMPTY){//TODO 
					WORDL_ENTITY oldRobot = world[oldPosition.getX()][oldPosition.getY()];
					world[oldPosition.getX()][oldPosition.getY()] = WORDL_ENTITY.EMPTY;
					world[newPosition.getX()][newPosition.getY()] = oldRobot;
					
					Map<Position, WORDL_ENTITY> changingMap = makeTheSimpleChangingMap(oldPosition);
					changingMap.putAll(makeTheSimpleChangingMap(newPosition));
					notifyChangement(new WarehouseChangement(changingMap));
					System.out.println("Ok pour ce deplacement");
				}else{
					System.out.println("KO pour ce mvt, la position n'est pas vide");
				}
			}
			
			@Override
			public void moveLane(int laneId, int newHigh) {
				throw new RuntimeException("pas fait!!");
				//makeTheRectangleChanginMap
				//notifyChangement(new WarehouseChangement()); TODO
				
			}
			
			private void notifyChangement(WarehouseChangement change){
				envObserbableDelegate.setChanged();
				envObserbableDelegate.notifyObservers(change);
			}
		};
	}

	@Override
	protected IEnvironnementObservable make_envObservable() {
		return new IEnvironnementObservable() {
			
			@Override
			public void registerObserver(Observer observer) {
				envObserbableDelegate.addObserver(observer);
				
				//make the changing map = all the world because is the new observer
				Map<Position, WORDL_ENTITY> changingMap = makeTheRectangleChanginMap(0,0,X_SIZE, Y_SIZE);
				WarehouseChangement changement = new WarehouseChangement(changingMap);
				
				//carefull just notify the new observer ! So we use the direct method.
				observer.update(envObserbableDelegate, changement);
			}
		};
	}
	
	/**
	 * Return the changing map for a delimited zone.</br>
	 * @param xDeb yDeb the position to start the rectangle zone.
	 * By convention 0,0 is up left
	 */
	private Map<Position, WORDL_ENTITY> makeTheRectangleChanginMap(int xDeb, int yDeb, int xLenght, int yLenght){
		Map<Position, WORDL_ENTITY> changingMap = new HashMap<Position, IEnvironnementAnalysis.WORDL_ENTITY>();
		for(int x=xDeb; x<xDeb+xLenght ; x++){
			for(int y=yDeb; y<yDeb+yLenght; y++){
				changingMap.put(new Position(x, y), world[x][y]);
			}
		}
		return changingMap;
	}
	
	private Map<Position, WORDL_ENTITY> makeTheSimpleChangingMap(Position position){
		Map<Position, WORDL_ENTITY> changingMap = new HashMap<Position, IEnvironnementAnalysis.WORDL_ENTITY>();
		changingMap.put(position, world[position.getX()][position.getY()]);
		return changingMap;
	}
	
	private class EnvObservable extends Observable{
		@Override
		protected synchronized void setChanged() {
			super.setChanged();
		}
	}

}
