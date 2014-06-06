package rsma.aux.robot.impl;

import rsma.aux.robot.IRobotPerception;
import rsma.impl.RobotImpl;
import rsma.interfaces.IEnvironnementAnalysis;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.util.Position;

public class RobotPerception implements IRobotPerception{
	private static final int LOCALPERCEPTSIZE = 7;
	private static final int MAX_VISIBILITY = 3;
	private static final int Y_POSIT_REF = 3;
	private static final int X_POSIT_REF = 3;
	private WORLD_ENTITY[][] localTempPercep = new WORLD_ENTITY[LOCALPERCEPTSIZE][LOCALPERCEPTSIZE];
	private IEnvironnementAnalysis envAnalysis;
	private RobotImpl robotAgent;


	public RobotPerception(RobotImpl robotImpl, IEnvironnementAnalysis pEnvLookAt) {
		envAnalysis = pEnvLookAt;
		robotAgent = robotImpl;
	}

	@Override
	public void doPerception() {
		//init the local temp perception map
		localTempPercep[Y_POSIT_REF][X_POSIT_REF]=RobotUtils.getWEFromAim(robotAgent.getAim());
		WORLD_ENTITY we;
		Position posit;
		InternalRobotPerceptionInterator ite = new InternalRobotPerceptionInterator();

		while(!ite.interatorIsTerminate()){
			posit = ite.getNextPosition();
			if(RobotUtils.positionIsValide(posit)){
				we = envAnalysis.getWorldEntityAt(posit);
			}else{
				we = null;
			}
			localTempPercep[ite.y][ite.x] = we;
		}
	}




	//	private void doPerception() {
	//	//init the local temp perception map
	//	localTempPercep[3][3]=externalState;
	//	WORLD_ENTITY we;
	//	Position posit;
	//	for(int x=0; x<7; x++){
	//		for(int y=0; y<7; y++){
	//			posit = new Position(currentPosition.getX() - (3-x), currentPosition.getY() - (3-y));
	//			if(positionIsValide(posit)){
	//				we = eco_requires().pEnvLookAt().getWorldEntityAt(posit);
	//			}else{
	//				we = null;
	//			}
	//			localTempPercep[y][x] = we;
	//		}
	//	}
	//}
	//

	@Override
	public boolean perceptionCurrentPositionIsLaneExit(SEARCH_PERCEPTION perceptionType) {
		return isLaneEntrance(perceptionType);//TODO refactor
	}

	@Override
	public boolean perceptionCurrentPositionIsLaneEntrance(SEARCH_PERCEPTION perceptionType) {
		return isLaneEntrance(perceptionType);
	}

	private boolean isLaneEntrance(SEARCH_PERCEPTION perceptionType){
		boolean ret = false;
		int xOffset = 1;
		if(perceptionType == SEARCH_PERCEPTION.LEFT){
			xOffset = -1;
		}
		ret = ! localTempPercep[Y_POSIT_REF][X_POSIT_REF+xOffset].equals(WORLD_ENTITY.WALL);
		ret &= localTempPercep[Y_POSIT_REF-1][X_POSIT_REF+xOffset].equals(WORLD_ENTITY.WALL);
		ret &= localTempPercep[Y_POSIT_REF+1][X_POSIT_REF+xOffset].equals(WORLD_ENTITY.WALL);
		ret &= (!localTempPercep[Y_POSIT_REF+1][X_POSIT_REF].equals(WORLD_ENTITY.WALL)) || (!localTempPercep[Y_POSIT_REF-1][X_POSIT_REF].equals(WORLD_ENTITY.WALL));
		return ret;
	}


	@Override
	public Position perceptionHasEntity(WORLD_ENTITY we){
		Position ret = null;
		InternalRobotPerceptionInterator ite = new InternalRobotPerceptionInterator();
		while(!ite.interatorIsTerminate()){
			Position postTry = ite.getNextPosition();
			if(RobotUtils.positionIsValide(postTry)){
				if(localTempPercep[ite.y][ite.x].equals(we)){ //if a we is found
					if(ret==null){
						ret = postTry;//init
					}else{//maybe postTry is most close than ret
						Position currentPosition = robotAgent.getCurrentPosition();
						if(RobotUtils.getDistance(currentPosition, ret)>RobotUtils.getDistance(currentPosition, postTry)){
							ret = postTry;
						}
					}
				}
			}
		}
		return ret;
	}

	@Override
	public Position perceptionHasEntity(WORLD_ENTITY we,
			SEARCH_PERCEPTION level) {
		Position ret = null;
		if(level.equals(SEARCH_PERCEPTION.ALL)){
			ret = perceptionHasEntity(we);
		}else{
			int xDirection = -1;
			if(level.equals(SEARCH_PERCEPTION.RIGHT)){
				xDirection = 1;
			}
			ret = lookAtNPerceptionFromDirection(xDirection, MAX_VISIBILITY, we);
		}
		return ret;
	}

	private Position lookAtNPerceptionFromDirection(int xDirection, int lookDeep, WORLD_ENTITY we){
		int xOffset = 0;
		Position ret = null;
		while(xOffset<lookDeep && ret==null){
			xOffset++;
			WORLD_ENTITY weTry = localTempPercep[Y_POSIT_REF][X_POSIT_REF + (xDirection*xOffset)];
			if(weTry.equals(we)){
				Position currentPosition = robotAgent.getCurrentPosition();
				ret = new Position(currentPosition.getX() + (xDirection*xOffset), currentPosition.getY());
			}
		}
		return ret;
	}

	@Override
	public boolean perceptionHasFreePlace() {
		return searchOnPerceptionFreePlacePosition()!=null;
	}

	@Override
	public Position searchOnPerceptionFreePlacePosition() {
		Position post = null;
		InternalRobotPerceptionInterator ite = new InternalRobotPerceptionInterator();
		while(!ite.interatorIsTerminate()){
			Position posit = ite.getNextPosition();
			//if the position is valide and is int he push zone
			if(RobotUtils.positionIsValide(posit) && RobotUtils.pushZone.contains(posit.getX(), posit.getY())){
				if(localTempPercep[ite.y][ite.x].equals(WORLD_ENTITY.EMPTY)){ //and is a free place
					post = posit;
					//!!! DO NO EXIT TO WHILE BECAUSE WE GET THE MOST FAR FREE PLACE
				}
			}
		}
		return post;
	}

	@Override
	public boolean checkSuicideBeforStartCycle() {
		Position currentPosition = robotAgent.getCurrentPosition();
		return envAnalysis.getWorldEntityAt(currentPosition).equals(WORLD_ENTITY.WALL);		
	}

	@Override
	public WORLD_ENTITY getWorldEntityFromPosition(Position currentPosition, Position nextPost) {
		int xOffset = nextPost.getX() - currentPosition.getX();
		int yOffset = nextPost.getY() - currentPosition.getY();
		return localTempPercep[Y_POSIT_REF + yOffset][X_POSIT_REF + xOffset];
	}

	private class InternalRobotPerceptionInterator{
		private int x=-1; 
		private int y=-1;

		/*
		 * return null if the iterator is terminate, don't check if the position is valide
		 * */
		public Position getNextPosition(){
			Position post = null;
			Position absRobotPost = RobotPerception.this.robotAgent.getCurrentPosition();
			if(!interatorIsTerminate()){ 
				if(x==-1 || y==(LOCALPERCEPTSIZE-1)){
					x++;
					y=-1;
				}
				y++;
				post = new Position(absRobotPost.getX() - (X_POSIT_REF-x), absRobotPost.getY() - (Y_POSIT_REF-y));
			}
			return post;
		}

		public boolean interatorIsTerminate(){
			return x==(LOCALPERCEPTSIZE-1) && y ==(LOCALPERCEPTSIZE-1);
		}
	}

	@Override
	public boolean isInLane(Position currentPosition) {
		return localTempPercep[Y_POSIT_REF -1 ][X_POSIT_REF].equals(WORLD_ENTITY.WALL) && localTempPercep[Y_POSIT_REF +1 ][X_POSIT_REF].equals(WORLD_ENTITY.WALL);
	}


}
