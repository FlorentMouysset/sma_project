package rsma.aux.robot.impl;

import rsma.aux.robot.IRobotPerception;
import rsma.impl.RobotImpl;
import rsma.impl.RobotImpl.INTERNAL_AIM;
import rsma.interfaces.IEnvironnementAnalysis;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.util.Position;

public class RobotPerception implements IRobotPerception{
	private static final int LOCALPERCEPTSIZE = 7;
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
		localTempPercep[3][3]=RobotUtils.getWEFromAim(robotAgent.getAim());
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
	public boolean perceptionCurrentPositionIsLaneExit() {
		boolean ret = false;
		ret = !localTempPercep[2][3].equals(WORLD_ENTITY.WALL);	
		ret |= !localTempPercep[4][3].equals(WORLD_ENTITY.WALL);
		return ret;
	}


	@Override
	public Position perceptionHasEntity(WORLD_ENTITY we){
		Position ret = null;
		InternalRobotPerceptionInterator ite = new InternalRobotPerceptionInterator();
		while(!ite.interatorIsTerminate()){
			Position postTry = ite.getNextPosition();
			if(RobotUtils.positionIsValide(postTry)){
				if(localTempPercep[ite.y][ite.x].equals(we)){ //if a we is foud
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
			INTERNAL_AIM aim = robotAgent.getAim();
			boolean aimIsPush = aim.equals(INTERNAL_AIM.PUSH_AIM);
			boolean frontIsAsked = level.equals(SEARCH_PERCEPTION.FRONT);
			int xDirection = -1;
			if( (aimIsPush && frontIsAsked) || (!aimIsPush && !frontIsAsked)  ){
				xDirection = 1;
			}
			int xOffset = 1;
			while(xOffset<=3 && ret==null){
				xOffset++;
				WORLD_ENTITY weTry = localTempPercep[3][3 + (xDirection*xOffset)];
				if(weTry.equals(we)){
					Position currentPosition = robotAgent.getCurrentPosition();
					ret = new Position(currentPosition.getX() + (xDirection*xOffset), currentPosition.getY());
				}
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
	public boolean perceptionCurrentPositionIsLaneEntrance() {
		boolean ret = false;
		INTERNAL_AIM aim = robotAgent.getAim();
		if(aim.equals(INTERNAL_AIM.PULL_AIM)){
			ret = ! localTempPercep[3][3-1].equals(WORLD_ENTITY.WALL);
		}else{
			ret = ! localTempPercep[3][3+1].equals(WORLD_ENTITY.WALL);
		}
		return ret;
	}

	@Override
	public boolean checkSuicideBeforStartCycle() {
		Position currentPosition = robotAgent.getCurrentPosition();
		return envAnalysis.getWorldEntityAt(currentPosition).equals(WORLD_ENTITY.WALL);		
	}

	@Override
	public WORLD_ENTITY getWorldEntityFromPosition(Position nextPost) {
		return envAnalysis.getWorldEntityAt(nextPost);
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
				post = new Position(absRobotPost.getX() - (3-x), absRobotPost.getY() - (3-y));
			}
			return post;
		}

		public boolean interatorIsTerminate(){
			return x==(LOCALPERCEPTSIZE-1) && y ==(LOCALPERCEPTSIZE-1);
		}
	}


}
