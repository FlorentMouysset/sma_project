package rsma.impl;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import rsma.Environnement;
import rsma.interfaces.IEnvironnementActions;
import rsma.interfaces.IEnvironnementAnalysis;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.interfaces.IEnvironnementObservable;
import rsma.util.ConfigurationManager;
import rsma.util.Position;
import rsma.util.WarehouseChangement;


public class EnvironnementImpl extends Environnement{

	private static int X_SIZE;
	private static int Y_SIZE;

	/**the world is a WORDL_ENTITY matrix, use the  X=col, Y=lig convention*/
	private WORLD_ENTITY[][] world;
	
	// because we cannot extends Observable too, we create a delegate
	private EnvObservable envObserbableDelegate = new EnvObservable();
	private Rectangle pullZone;
	private Rectangle pushZone;
	private int[] yLanes;
	
	@Override
	protected void start() {
		System.out.println("ENV : Start Environnement");
		X_SIZE = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_X_LENGHT"));
		Y_SIZE = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_Y_LENGHT"));
		world = new WORLD_ENTITY[Y_SIZE][X_SIZE]; //init the matrix
		
		/**Initialized the matrix with a logical value, is not a optimized algorithm*/
		
		//fill by empty
		fillTheMatrixByEntity(1, X_SIZE-2, 1, Y_SIZE-2, WORLD_ENTITY.EMPTY);
		//make the border
		fillTheMatrixByEntity(0, X_SIZE, 0, 1, WORLD_ENTITY.WALL); //the up border
		fillTheMatrixByEntity(0, 1, 1, Y_SIZE-2, WORLD_ENTITY.WALL); //the left border
		fillTheMatrixByEntity(0, X_SIZE, Y_SIZE-1, 1, WORLD_ENTITY.WALL); //the bottom border
		fillTheMatrixByEntity(X_SIZE-1, 1, 1, Y_SIZE-2, WORLD_ENTITY.WALL); //the right border
		
		//make the middle wall
		int middleWallXStart = Integer.parseInt(ConfigurationManager.getProperty("MIDDLE_WALL_X_START"));
		int middleWallXStop = Integer.parseInt(ConfigurationManager.getProperty("MIDDLE_WALL_X_STOP"));
		fillTheMatrixByEntity(middleWallXStart, middleWallXStop-middleWallXStart, 0, Y_SIZE-1, WORLD_ENTITY.WALL);
		
		//make the lanes
		yLanes = new int[2];
		yLanes[0] = Integer.parseInt(ConfigurationManager.getProperty("LANE_Y_1"));
		yLanes[1] = Integer.parseInt(ConfigurationManager.getProperty("LANE_Y_2"));
		for(int nbLane : yLanes){
			fillTheMatrixByEntity(middleWallXStart, middleWallXStop-middleWallXStart, nbLane-1, 1, WORLD_ENTITY.EMPTY);
		}
		
		//make the pull/push zones
		//the pull zone
		int pullXStart = Integer.parseInt(ConfigurationManager.getProperty("PULL_ZONE_X_START"));
		int pullYStart = Integer.parseInt(ConfigurationManager.getProperty("PULL_ZONE_Y_START"));
		int pullXLenght = Integer.parseInt(ConfigurationManager.getProperty("PULL_ZONE_X_LENGHT"));
		int pullYLenght= Integer.parseInt(ConfigurationManager.getProperty("PULL_ZONE_Y_LENGHT"));
		pullZone = new Rectangle(pullXStart, pullYStart, pullXLenght, pullYLenght);
		fillTheMatrixByEntity(pullXStart, pullXLenght, pullYStart, pullYLenght, WORLD_ENTITY.RESOURCE);
		//the push zone
		int pushXStart = Integer.parseInt(ConfigurationManager.getProperty("PUSH_ZONE_X_START"));
		int pushYStart = Integer.parseInt(ConfigurationManager.getProperty("PUSH_ZONE_Y_START"));
		int pushXLenght = Integer.parseInt(ConfigurationManager.getProperty("PUSH_ZONE_X_LENGHT"));
		int pushYLenght= Integer.parseInt(ConfigurationManager.getProperty("PUSH_ZONE_Y_LENGHT"));
		pushZone = new Rectangle(pushXStart, pushYStart, pushXLenght, pushYLenght);
		//printMatrix();
	};
	
	private void fillTheMatrixByEntity(int xStart, int xLenght, int yStart, int yLenght, WORLD_ENTITY entity){
		for(int y=yStart; y<yLenght+yStart; y++){
			for(int x = xStart; x<xLenght+xStart; x++){
				world[y][x] = entity;
			}
		}
	}
	
	private void printMatrix(){
		char car;
		for(int y=0; y< Y_SIZE; y++){
			for(int x = 0 ; x<X_SIZE; x++){	
				//System.out.println("x=" + x +"  y=" + y);
				switch (world[y][x]) {
				case EMPTY:
					car = '_';
					break;
				case WALL:
					car = 'X';
					break;
				case RESOURCE:
					car = 'o';
					break;
				case ROBOT:
					car = 'O';
					break;
				case ROBOT_AND_RESOURCE:
					car = '@';
					break;
				default:
					car = 'E';
					break;
				}
				System.out.print(car);
			}
			System.out.print("\n");
		}
		
	}
	
	@Override
	protected IEnvironnementAnalysis make_envLookAtPort() {
		return new IEnvironnementAnalysis() {

			@Override
			public WORLD_ENTITY getWorldEntityAt(Position position) {
			//	System.out.println("ENV : quelqu'un fait un get à " + position + " il y a "+ world[position.getY()][position.getX()]);
				return world[position.getY()][position.getX()];
			}

			@Override
			public Rectangle getPullZone() {
				return pullZone;
			}

			@Override
			public Rectangle getPushZone() {
				return pushZone;
			}
		};
	}

	@Override
	protected IEnvironnementActions make_envActions() {
		return new IEnvironnementActions() {
			
			@Override
			public void pushResource(Position freePlacePost, Position robotPost) {
				System.out.println("ENV : PUSH done !" + freePlacePost + "  robot=" + robotPost);
				world[freePlacePost.getY()][freePlacePost.getX()] = WORLD_ENTITY.RESOURCE;
				world[robotPost.getY()][robotPost.getX()] = WORLD_ENTITY.ROBOT;
				Map<Position, WORLD_ENTITY> changeMap = makeTheSimpleChangingMap(freePlacePost);
				changeMap.putAll(makeTheSimpleChangingMap(robotPost));
				notifyChangement(new WarehouseChangement(changeMap));
			}
			
			@Override
			public void pullResource(Position resrcPost, Position robotPost) {
				System.out.println("ENV : PULL done !" + resrcPost + " robot =" + robotPost);
				world[resrcPost.getY()][resrcPost.getX()] = WORLD_ENTITY.EMPTY;
				world[robotPost.getY()][robotPost.getX()] = WORLD_ENTITY.ROBOT_AND_RESOURCE;
				Map<Position, WORLD_ENTITY> changeMap = makeTheSimpleChangingMap(resrcPost);
				changeMap.putAll(makeTheSimpleChangingMap(robotPost));
				notifyChangement(new WarehouseChangement(changeMap));
			}
			
			@Override
			public void moveRobot(Position oldPosition, Position newPosition) {
				//System.out.println("ENV : un robot bouge de " + oldPosition +" à " + newPosition );
				if(world[newPosition.getY()][newPosition.getX()]== WORLD_ENTITY.EMPTY){//TODO 
					WORLD_ENTITY oldRobot = world[oldPosition.getY()][oldPosition.getX()];
					world[oldPosition.getY()][oldPosition.getX()] = WORLD_ENTITY.EMPTY;
					world[newPosition.getY()][newPosition.getX()] = oldRobot;
					
					Map<Position, WORLD_ENTITY> changingMap = makeTheSimpleChangingMap(oldPosition);
					changingMap.putAll(makeTheSimpleChangingMap(newPosition));
					notifyChangement(new WarehouseChangement(changingMap));
					//System.out.println("Ok pour ce deplacement");
				}else{
					System.out.println("KO pour ce mvt, la position n'est pas vide");
				}
				//printMatrix();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void moveLane(int laneId, int newHigh) {
				throw new RuntimeException("pas fait!!");
				//makeTheRectangleChanginMap
				//notifyChangement(new WarehouseChangement()); TODO
				
			}
			
			private void notifyChangement(WarehouseChangement change){
				//TODO remove 2 lines
				Map<Position, WORLD_ENTITY> changingMap = makeTheRectangleChanginMap(0,0,X_SIZE, Y_SIZE);
				change = new WarehouseChangement(changingMap);
				
				envObserbableDelegate.setChanged();
				envObserbableDelegate.notifyObservers(change);
			}

			@Override
			public void addRobot(Position robotPost) {
				System.out.println("ENV : ajout d'un robot à " + robotPost );
				world[robotPost.getY()][robotPost.getX()] = WORLD_ENTITY.ROBOT;
				notifyChangement(new WarehouseChangement(makeTheSimpleChangingMap(robotPost)));
				//printMatrix();
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
				Map<Position, WORLD_ENTITY> changingMap = makeTheRectangleChanginMap(0,0,X_SIZE, Y_SIZE);
				WarehouseChangement changement = new WarehouseChangement(changingMap);
				
				//Careful just notify the new observer ! So we use the direct method.
				observer.update(envObserbableDelegate, changement);
			}
		};
	}
	
	/**
	 * Return the changing map for a delimited zone.</br>
	 * @param xDeb yDeb the position to start the rectangle zone.
	 * By convention 0,0 is up left
	 */
	private Map<Position, WORLD_ENTITY> makeTheRectangleChanginMap(int xDeb, int yDeb, int xLenght, int yLenght){
		Map<Position, WORLD_ENTITY> changingMap = new HashMap<Position, IEnvironnementAnalysis.WORLD_ENTITY>();
		for(int x=xDeb; x<xDeb+xLenght ; x++){
			for(int y=yDeb; y<yDeb+yLenght; y++){
				changingMap.put(new Position(x, y), world[y][x]);
			}
		}
		return changingMap;
	}
	
	private Map<Position, WORLD_ENTITY> makeTheSimpleChangingMap(Position position){
		Map<Position, WORLD_ENTITY> changingMap = new HashMap<Position, IEnvironnementAnalysis.WORLD_ENTITY>();
		changingMap.put(position, world[position.getY()][position.getX()]);
		return changingMap;
	}
	
	private class EnvObservable extends Observable{
		@Override
		protected synchronized void setChanged() {
			super.setChanged();
		}
	}

}
