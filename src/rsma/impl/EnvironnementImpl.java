package rsma.impl;

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
		};
	}

	@Override
	protected IEnvironnementActions make_envActions() {
		return new IEnvironnementActions() {
			
			@Override
			public void pushResource(Position position) {
				world[position.getX()][position.getY()] = WORDL_ENTITY.RESOURCE;
				notifyChangement(new WarehouseChangement());
			}
			
			@Override
			public void pullResource(Position position) {
				world[position.getX()][position.getY()] = WORDL_ENTITY.EMPTY;
				notifyChangement(new WarehouseChangement());
			}
			
			@Override
			public void moveRobot(Position oldPosition, Position newPosition) {
				System.out.println("ENV : un robot bouge de " + oldPosition +" à " + newPosition );
				if(world[newPosition.getX()][newPosition.getY()]== WORDL_ENTITY.EMPTY){//TODO 
					WORDL_ENTITY oldRobot = world[oldPosition.getX()][oldPosition.getY()];
					world[oldPosition.getX()][oldPosition.getY()] = WORDL_ENTITY.EMPTY;
					world[newPosition.getX()][newPosition.getY()] = oldRobot;
					notifyChangement(new WarehouseChangement());
					System.out.println("Ok pour ce deplacement");
				}else{
					System.out.println("KO pour ce mvt, la position n'est pas vide");
				}
				//return true/false selon réusite TODO !!!
			}
			
			@Override
			public void moveLane(int newHigh) {
				throw new RuntimeException("pas fait!!");
				//notifyChangement(new WarehouseChangement());
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
			}
		};
	}
	
	
	
	
	private class EnvObservable extends Observable{
		@Override
		protected synchronized void setChanged() {
			super.setChanged();
		}
	}

}
