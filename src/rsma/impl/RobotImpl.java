package rsma.impl;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;

import rsma.Robots.Robot;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.interfaces.IRobotActions;
import rsma.util.ConfigurationManager;
import rsma.util.Position;

public class RobotImpl extends Robot{
	private final String id;
	private static Rectangle pullZone;
	private static Rectangle pushZone;
	private static int X_MAX;
	private static int Y_MAX;
	private static enum INTERNAL_STATE {RESOURCE_SEARCH, ZONE_PULL_GO, ZONE_PUSH_GO, LANE_PULL_GO, LANE_PUSH_GO, LANE_SEARCH, LANE_IN, FOLLOW_LANE, FORCE, FREEPLACE_SEARCH, LANE_SEARCH_UP, LANE_SEARCH_DOWN, RESIGNATION};
	private static enum INTERNAL_AIM {PULL_AIM, PUSH_AIM};
	private static enum INTERNAL_LANE_STATUS {TRY, PULL_LANE, PUSH_LANE};
	private static enum INTERNAL_ACTION {WALK, PULL, PUSH, NOTHING};
	
	
	private Position currentPosition;
	private INTERNAL_STATE state = INTERNAL_STATE.ZONE_PULL_GO;
	private INTERNAL_AIM aim = INTERNAL_AIM.PULL_AIM;
	private Map<INTERNAL_LANE_STATUS, Position> laneMap = new HashMap<INTERNAL_LANE_STATUS, Position>();
	private WORLD_ENTITY[][] localTempPercep = new WORLD_ENTITY[7][7];
	private WORLD_ENTITY externalState = WORLD_ENTITY.ROBOT;
	private INTERNAL_ACTION action;
	private int cptCycleLaneWainting;
	

	
	public RobotImpl(String id, Position positionInit, Rectangle pullZone, Rectangle pushZone){
		this.id = id;
		this.currentPosition = positionInit;
		RobotImpl.pullZone = pullZone;
		RobotImpl.pushZone = pushZone;
		
		RobotImpl.X_MAX = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_X_LENGHT"));
		RobotImpl.Y_MAX = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_Y_LENGHT"));
	}
	
	@Override
	protected IRobotActions make_roboActionPort() {
		return new IRobotActions() {
			
			@Override
			public boolean doCycle() {
				boolean doSuicide = checkSuicideBeforStartCycle();
				if(!doSuicide){
					doPerception();
					Position nextPost = doDecision();
					doSuicide = nextPost == null;
					if(!doSuicide) doAction(nextPost);
				}
				return doSuicide;
			}
		
		};
	}
	
	


//	private void doPerception() {
//		//init the local temp perception map
//		localTempPercep[3][3]=externalState;
//		WORLD_ENTITY we;
//		Position posit;
//		for(int x=0; x<7; x++){
//			for(int y=0; y<7; y++){
//				posit = new Position(currentPosition.getX() - (3-x), currentPosition.getY() - (3-y));
//				if(positionIsValide(posit)){
//					we = eco_requires().pEnvLookAt().getWorldEntityAt(posit);
//				}else{
//					we = null;
//				}
//				localTempPercep[y][x] = we;
//			}
//		}
//	}
//	
	
	private void doPerception() {
		//init the local temp perception map
		localTempPercep[3][3]=externalState;
		WORLD_ENTITY we;
		Position posit;
		InternalRobotPerceptionInterator ite = new InternalRobotPerceptionInterator();
		
		while(!ite.interatorIsTerminate()){
			posit = ite.getNextPosition();
			if(positionIsValide(posit)){
				we = eco_requires().pEnvLookAt().getWorldEntityAt(posit);
			}else{
				we = null;
			}
			localTempPercep[ite.y][ite.x] = we;
		}
	}
	
	
	private class InternalRobotPerceptionInterator{
		private int x=-1; 
		private int y=-1;
		
		/*
		 * return null if the iterator is terminate, don't check if the position is valide
		 * */
		public Position getNextPosition(){
			Position post = null;
			if(!interatorIsTerminate()){ 
				if(x==-1 || y==7){
					x++;
					y=-1;
				}
				y++;
				post = new Position(currentPosition.getX() - (3-x), currentPosition.getY() - (3-y));
			}
			return post;
		}
		
		public boolean interatorIsTerminate(){
			return x==6 && y ==6;
		}
		
	}
	
	/**
	 * take a action (update the attribute action) and eventually update the aim, the lane knowledge, the state and the external state.
	 * Return the next position of action or null if suicide**/
	private Position doDecision() {
		Position nextPost= null;
		

		/**
		 **** 0 LANE_PULL_GO
		 * if currentPost == LANE_PULL.post then//si lane atteind 
		 * 		state = LANE_IN//on le traverse
		 * 		// faire LANE_IN
		 * else
		 * 		 nxtPost = computeNextPost(LANE_PULL_GO.post);//sinon on avance (si le mur a bougé c gérer ds N)
		 * action = walk*/
		if(state.equals(INTERNAL_STATE.LANE_PULL_GO)) {
			nextPost = zonePuXXGo(INTERNAL_LANE_STATUS.PULL_LANE);
		}
		
		/**
		 **** 1 LANE_PUSH_GO
		 * if currentPost == LANE_PUSH.post then//si lane atteind 
		 * 		state = LANE_IN//on le traverse
		 * 		// faire LANE_IN
		 * else
		 * 		 nxtPost = computeNextPost(LANE_PUSH_GO.post);//sinon on avance (si le mur a bougé c gérer ds N)
		 * action = walk*/
		if(state.equals(INTERNAL_STATE.LANE_PUSH_GO)) {
				nextPost = zonePuXXGo(INTERNAL_LANE_STATUS.PUSH_LANE);
		}

		
		 /**** 2 if state == ZONE_PULL_GO
		 *  a = contientRessource(localTempPercep)
		 *  if(a!=null \/ pullZoneIsReach){
		 *  	state = RESOURCE_SEARCH
		 *  }else{
		 *  	nxtpost = computeNextPost(pullZone)
		 *  }
		 *  action = walk
		 */
		if(state.equals(INTERNAL_STATE.ZONE_PULL_GO)){
			boolean hasRessourceFound = perceptionHasEntity(WORLD_ENTITY.RESOURCE, SEARCH_PERCEPTION.ALL) != null;
			if(hasRessourceFound || pullZone.contains(currentPosition.getX(), currentPosition.getY())){
				state =INTERNAL_STATE.RESOURCE_SEARCH;
			}else{
				nextPost = computeNextPositionFromRectangle(pullZone);
			}
			action = INTERNAL_ACTION.WALK;
		}
		
		/**** 3 if state == RESOURCE_SEARCH
		 *  a = getNextMaxClosePostOfRessource(localTempPercep)
		 *  action = walk
		 *  if a == null then{
		 *   nxtpost = moveInPullZone()
		 *  }else if a.distance(current)==0 then
		 *    // PULL
		 *   actionpost = resc.post
		 *   action = pull
		 *   aim = ! aim
		 *   state = if lane push exist then lane push go
		 * 			else pushzone go 
		 * 	 extState = full
		 *  }
		 */
		if(state.equals(INTERNAL_STATE.RESOURCE_SEARCH)){
			Position rescPost = perceptionHasEntity(WORLD_ENTITY.RESOURCE, SEARCH_PERCEPTION.ALL);
			action = INTERNAL_ACTION.WALK;
			if(rescPost == null){
				nextPost = moveInPullZone();
			}else if(getDistance(currentPosition, rescPost) == 1){
				nextPost = rescPost;
				action = INTERNAL_ACTION.PULL;
				aim = INTERNAL_AIM.PUSH_AIM;
				externalState = WORLD_ENTITY.ROBOT_AND_RESOURCE;
				if(laneMap.containsKey(INTERNAL_LANE_STATUS.PUSH_LANE)){
					state =INTERNAL_STATE.LANE_PULL_GO;
				}else{
					state =INTERNAL_STATE.ZONE_PUSH_GO;
				}
			}else{
				//TODO aller vers ressouce
			}
		}
		
		/**** 4 if state == ZONE_PUSH_GO
		 *	a = containFreePlace(localTempPercep)
		 *  if(a!=null \/ pushZoneIsReach){
		 *      state = FREEPLACE_SEARCH
		 *  }else{
		 *  	nxtpost = computeNextPost(pushZone)
		 *  }
		 *  action = walk
		 */
		if(state.equals(INTERNAL_STATE.LANE_PUSH_GO)){
			boolean containFreePlace = perceptionHasFreePlace();
			if(containFreePlace || pushZone.contains(currentPosition.getX(), currentPosition.getY())){
				state = INTERNAL_STATE.FREEPLACE_SEARCH;
			}else{
				nextPost = computeNextPositionFromRectangle(pushZone);
			}
			action = INTERNAL_ACTION.WALK;
		}
		

		 /**** 5 if state == FREEPLACE_SEARCH
		 *  a = getNextMaxClosePostOfFreePlace(localTempPercep)
		 *  action = walk
		 *  if a == null then{
		 *   nxtpost = moveInPushZone()
		 *  }else if a.distance(current)==0 then
		 *    // PUSH
		 *    post = push.post
		 *    action = push
		 *    aim = !aim
		 *    state = if lane pull exist then lanepullgo else pull zone go
		 *    extState = empty
		 *  }
		 */
		if(state.equals(INTERNAL_STATE.FREEPLACE_SEARCH)){
			Position freePlacePost = searchOnPerceptionFreePlacePosition();
			action = INTERNAL_ACTION.WALK;
			if(freePlacePost == null){
				nextPost = moveInPushZone();
			}else if(getDistance(currentPosition, freePlacePost)==1){
				nextPost = freePlacePost;
				action = INTERNAL_ACTION.PUSH;
				aim = INTERNAL_AIM.PULL_AIM;
				externalState = WORLD_ENTITY.ROBOT;
				if(laneMap.containsKey(INTERNAL_LANE_STATUS.PULL_LANE)){
					state = INTERNAL_STATE.LANE_PULL_GO;
				}else{
					state = INTERNAL_STATE.ZONE_PULL_GO;
				}
			}else{
				//TODO aller vers freeplacePost
			}
		}
		

		 /**** 7 LANE_SEARCH_UP
		 *	if(laneEntranceDetect /\ isNotTheOtherLane){
		 * 		state = LANE_IN
		 * 		mapLane.put(lane.Y, TRY)
		 *  }else{
		 *  	nxtPost = computeMoveUp()
		 *  }
		 *  action = walk
		 */
		if(state.equals(INTERNAL_STATE.LANE_SEARCH_UP)){
			boolean hasLaneEntrance = perceptionCurrentPositionIsLaneEntrance();
			boolean isNotTheOtherLane = true;
			
			INTERNAL_LANE_STATUS laneTypeOther = aim.equals(INTERNAL_AIM.PULL_AIM) ? INTERNAL_LANE_STATUS.PUSH_LANE : INTERNAL_LANE_STATUS.PULL_LANE;
			Position position = laneMap.get(laneTypeOther);
			if(position != null){
				isNotTheOtherLane = !position.equals(currentPosition);
			}
			
			if(hasLaneEntrance && isNotTheOtherLane){
				state = INTERNAL_STATE.LANE_IN;
				laneMap.put(INTERNAL_LANE_STATUS.TRY, currentPosition);
			}else{
				nextPost = new Position(currentPosition.getX(), currentPosition.getY()-1);
			}
			action = INTERNAL_ACTION.WALK;
		}

		 /**** 8 LANE_SEARCH_DOWN
		 *  if(laneEntranceDetect /\ isNotTheOtherLane){
		 * 		state = LANE_IN
		 * 		mapLane.put(lane.Y, TRY)
		 *  }else{
		 *  	nxtPost = computeMoveDown()
		 *  }
		 *  action = walk
		 */ 
		 
		if(state.equals(INTERNAL_STATE.LANE_SEARCH_DOWN)){// TODO refactor avec UP
			boolean hasLaneEntrance = perceptionCurrentPositionIsLaneEntrance();
			boolean isNotTheOtherLane = true;
			
			INTERNAL_LANE_STATUS laneTypeOther = aim.equals(INTERNAL_AIM.PULL_AIM) ? INTERNAL_LANE_STATUS.PUSH_LANE : INTERNAL_LANE_STATUS.PULL_LANE;
			Position position = laneMap.get(laneTypeOther);
			if(position != null){
				isNotTheOtherLane = !position.equals(currentPosition);
			}
			
			if(hasLaneEntrance && isNotTheOtherLane){
				state = INTERNAL_STATE.LANE_IN;
				laneMap.put(INTERNAL_LANE_STATUS.TRY, currentPosition);
			}else{
				nextPost = new Position(currentPosition.getX(), currentPosition.getY()+1);
			}
			action = INTERNAL_ACTION.WALK;
		}
		
		 /**** 9 LANE_IN
		 *	if(isExitToLane){
		 *		state = (aim= PULL_AIM)=> ZONEPULLGO | ZONE_PUSH_GO
		 *		//update mapLane
		 *		//TODO GO_PUXX_ZONE
		 *	}else if(perception.containtImediatlly(opositeRobotType) && cptCycleLaneWainting<2 ){//détection d'un robot de type opposé
		 *		//ce robot n'est peut être pas en conflict
		 *		action = NOTHING
		 *		if(IHavePriorityByDistanceZone){
		 *			cptCycleLaneWainting++
		 *		}else{
		 *			state = resignation
		 *			cptCycleLaneWainting =0
		 *			//marquer le lane en fonction
		 *		}
		 *  }else if(perception.containtImediatlly(opositeRobotType) && cptCycleLaneWainting==2 ){{
		 *  	state = resignation
		 *		cptCycleLaneWainting =0
		 *
		 *  }else if(perception.containtFarAway3-v(robotSameType)){//le robot devant est en négociation
		 *  	action = NOTHING
		 *  }else if(perception.containtClosslin1-2-V(robotSameType)){//le robot ce raproche (ne marche que si tt les robot marche tt le temps pas de pause arbitraire)
		 *  	state = resignation
		 *		cptCycleLaneWainting =0
		 *  }else{
		 *  	action = WALK
		 *		nextPost = computSimpleSuite(currentpost)
		 *  }*/
		if(state.equals(INTERNAL_STATE.LANE_IN)){ //TODO refactor
			if(perceptionCurrentPositionIsLaneExit()){
				state = aim.equals(INTERNAL_AIM.PULL_AIM) ? INTERNAL_STATE.ZONE_PULL_GO : INTERNAL_STATE.ZONE_PUSH_GO;
				//TODO update map lane
				//TODO refaire le bon go zone
			}else{
				Position position = perceptionHasEntity(aim.equals(INTERNAL_AIM.PULL_AIM)? WORLD_ENTITY.ROBOT_AND_RESOURCE : WORLD_ENTITY.ROBOT, SEARCH_PERCEPTION.FRONT);	
				int dist = getDistance(currentPosition, position);
				if(cptCycleLaneWainting<2 && dist==1){
					action = INTERNAL_ACTION.NOTHING;
					if(hadLanePriority()){
						cptCycleLaneWainting++;
					}else{
						state = INTERNAL_STATE.RESIGNATION;
						cptCycleLaneWainting = 0;
					//TODO update lanemap
					}
				}else if(cptCycleLaneWainting == 2){
					state = INTERNAL_STATE.RESIGNATION;
					cptCycleLaneWainting = 0;
					//TODO update lanemap
				}else{
					position = perceptionHasEntity(aim.equals(INTERNAL_AIM.PUSH_AIM)? WORLD_ENTITY.ROBOT_AND_RESOURCE : WORLD_ENTITY.ROBOT, SEARCH_PERCEPTION.FRONT);	
					dist = getDistance(currentPosition, position);
					if(dist < 3){
						state =INTERNAL_STATE.RESIGNATION;
						cptCycleLaneWainting = 0;
						//TODO update lanemap
					}else{
						action = INTERNAL_ACTION.WALK;
						if(aim.equals(INTERNAL_AIM.PULL_AIM)){
							nextPost = new Position(currentPosition.getX()-1, currentPosition.getY());
						}else{
							nextPost = new Position(currentPosition.getX()+1, currentPosition.getY());
						}
					}
				}
			}
		}
		
		 /*  
		 **** 10  RESIGNATION
		 *	if(isExitToLane){
		 *		STATE= FORCE
		 *	}elseIf perception.containtImediatlly(robotSameType){
		 *		action = NOTHING
		 * }else{
		 * 		nxtPost = conputeNextPost(currentPost)
		 * }*/
		if(state.equals(INTERNAL_STATE.RESIGNATION)){
			if(perceptionCurrentPositionIsLaneExit()){
				state = INTERNAL_STATE.FORCE;
			}else{
				Position position = perceptionHasEntity(aim.equals(INTERNAL_AIM.PUSH_AIM)? WORLD_ENTITY.ROBOT_AND_RESOURCE : WORLD_ENTITY.ROBOT, SEARCH_PERCEPTION.FRONT);	
				int dist = getDistance(currentPosition, position);
				if (dist==1) {
					action = INTERNAL_ACTION.NOTHING;
				}else{
					action = INTERNAL_ACTION.WALK;
					if(aim.equals(INTERNAL_AIM.PUSH_AIM)){
						nextPost = new Position(currentPosition.getX()-1, currentPosition.getY());
					}else{
						nextPost = new Position(currentPosition.getX()+1, currentPosition.getY());
					}
				}				
			}
		}
		 
		 return nextPost;
		 /**** 11 FORCE_LANE_SEARCH_UP
		 * 
		 * 
		 **** N CONPUTED ALTERNATIVE [ALWAYLS]
		 * if action == walk /\ nextpost.e != EMPTY then { //si on marche et qu'il ya un qqchose l'à où on doit allez
		 *	 if(nextpost.e == WALL && WALL isDetect){//si c'est un mur
		 *
		 *		if(state == LANE_PULL_GO | LANE_PUSH_GO){ //si on est dans un état ateind de lane alors le lane a bougé
		 *			cleanLaneKnowlage(LANE_PULL_GO | LANE_PUSH_GO) //clean connaissances
		 *       }
		 *		if(wall is up){ //le mur est le mur du haut
		 *			state = LANE_SEARCH_DOWN //chercher en bas
		 *			//TODO refaire LANE_SEARCH_DOWN !!
		 *		}else if(wall is down){ //le mur est le mur du bas
		 *			state = LANE_SEARCH_UP //chercher en haut
		 *			//TODO refaire LANE_SEARCH_UP !!
		 *		}else if state == pushZoneGO{//sinon mur frontale 
		 *			state = bestLaneSearch() //return LANE_SEARCH_UP | LANE_SEARCH_DOWN //chercher en haut ou bas selon
		 *			//TODO refaire LANE_SEARCH_UP ou LANE_SEARCH_DOWN
		 * 		}
		 *	 }else{
		 *		 	nextpost = compute_alternative_if_existe_or_restaure_with_post // si pas mur alors recher contourne sinon on reprend l'ancinne valeur
		 *	 }	
		 * }

		 */ 
		
		
		
	}
	

	private boolean hadLanePriority() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Return true if the current position is a lane exit
	 * */
	private boolean perceptionCurrentPositionIsLaneExit() {
		boolean ret = false;
		int x = currentPosition.getX();
		int y = currentPosition.getY();
		Position post1 = new Position(x, y-1);
		Position post2 = new Position(x, y+1);
		
		if(positionIsValide(post1)){
			ret = !localTempPercep[post1.getY()][post1.getX()].equals(WORLD_ENTITY.WALL);			
		}
		if(positionIsValide(post2) && !ret){
			ret = !localTempPercep[post2.getY()][post2.getX()].equals(WORLD_ENTITY.WALL);			
		}
		Assert.assertTrue(positionIsValide(post1) || positionIsValide(post2));
		return ret;
	}

	/**
	 * Return true if the current position is a lane entrance
	 * */
	private boolean perceptionCurrentPositionIsLaneEntrance() {
		boolean ret = false;
		if(aim.equals(INTERNAL_AIM.PULL_AIM)){
			ret = ! localTempPercep[currentPosition.getY()][currentPosition.getX()-1].equals(WORLD_ENTITY.WALL);
		}else{
			ret = ! localTempPercep[currentPosition.getY()][currentPosition.getX()+1].equals(WORLD_ENTITY.WALL);
		}
		return ret;
	}

	/**
	 * Return a position to go in push zone
	 * */
	private Position moveInPushZone() {
		return getRandomPostOnRectangle(pushZone, currentPosition);
	}
	
	/**
	 * Return a position to go in pull zone
	 * */
	private Position moveInPullZone() {
		return getRandomPostOnRectangle(pullZone, currentPosition);
	}
	
	/**
	 * Return a random position into the rectangle and isn't excludePost
	 * */
	private static Position getRandomPostOnRectangle(Rectangle rectangle, Position excludePost){
		Position retPost = null;
		Random rand = new Random(System.currentTimeMillis());
		do{
			int x = rand.nextInt(rectangle.width-rectangle.x);
			x +=rectangle.width;
			int y = rand.nextInt(rectangle.height-rectangle.y);
			y +=rectangle.height;
			retPost = new Position(x, y);
			Assert.assertTrue(rectangle.contains(x, y));
		}while(retPost.equals(excludePost));
		Assert.assertTrue(positionIsValide(retPost));
		return retPost;
	}

	/**
	 * Return the position of a free place or null if none free place are see.
	 * the most far the free place is return (i hope)
	 * */
	private Position searchOnPerceptionFreePlacePosition() {
		Position post = null;
		InternalRobotPerceptionInterator ite = new InternalRobotPerceptionInterator();
		while(!ite.interatorIsTerminate()){
			Position posit = ite.getNextPosition();
			//if the position is valide and is int he push zone
			if(positionIsValide(posit) && pushZone.contains(posit.getX(), posit.getY())){
				if(localTempPercep[ite.y][ite.x].equals(WORLD_ENTITY.EMPTY)){ //and is a free place
					post = posit;
					//!!! DO NO EXIT TO WHILE BECAUSE WE GET THE MOST FAR FREE PLACE
				}
			}
		}
		return post;
	}

	/*** Return true is a freeplace to push is found on the perception*/
	private boolean perceptionHasFreePlace() {
		return searchOnPerceptionFreePlacePosition()!=null;
	}

	/**
	 * Return the number of move to reach posit2 from posit1. If return 1 posit2 is the 1-neighborhood to posit1
	 * */
	private int getDistance(Position posit1, Position posit2) {
		int xOffset = Math.abs(posit1.getX() - posit2.getX());
		int yOffset = Math.abs(posit1.getY() - posit2.getY());
		return Math.max(xOffset, yOffset);
	}



	private Position computeNextPositionFromRectangle(Rectangle pullZone2) {
		Position post = new Position(pullZone2.x, pullZone2.y);
		return computeNextPositionFromFarPosition(post);
	}


	/**
	 * ALL : search everyhere
	 * FRONT : search front of the robot (front is define by the aim)
	 * BEHIND : search behind of the robot (behind is define by the aim)
	 * !! CAREFULL : FRONT && BEHIND juste if you are in lane !!
	 * */
	private enum SEARCH_PERCEPTION {ALL, FRONT, BEHIND};

	/**Return true is the WORLD_ENTITY is found in the specified area. If ALL return the most close. Return null if cannot found*/	
	private Position perceptionHasEntity(WORLD_ENTITY we,
			SEARCH_PERCEPTION level) {
		Position ret = null;
		if(level.equals(SEARCH_PERCEPTION.ALL)){
			ret = perceptionHasEntity(we);
		}else{
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
					 ret = new Position(currentPosition.getX() + (xDirection*xOffset), currentPosition.getY());
				 }
			}
		}
		return ret;
	}

	/**
	 * Return the most close position where WE are (based of the map perception). Return null if WE doesn't exist in map perception
	 * */
	private Position perceptionHasEntity(WORLD_ENTITY we){
		Position ret = null;
		InternalRobotPerceptionInterator ite = new InternalRobotPerceptionInterator();
		while(!ite.interatorIsTerminate()){
			Position postTry = ite.getNextPosition();
			if(positionIsValide(postTry)){
				if(localTempPercep[ite.y][ite.x].equals(we)){ //if a we is foud
					if(ret==null){
						ret = postTry;//init
					}else{//maybe postTry is most close than ret
						if(getDistance(currentPosition, ret)>getDistance(currentPosition, postTry)){
							ret = postTry;
						}
					}
				}
			}
		}
		return ret;
	}

	
	/**
	 * Return the next position or if the entrance of lane is found switch to LANE_IN state and return null. action is set to walk
	 * */
	private Position zonePuXXGo(INTERNAL_LANE_STATUS pullLane) {
		Position nextPost = null;
		Position lanePost = laneMap.get(pullLane);
		Assert.assertNotNull(lanePost);
		if(currentPosition.equals(lanePost)){
			state = INTERNAL_STATE.LANE_IN;
		}else{
			nextPost  = computeNextPositionFromFarPosition(lanePost);
		}
		action = INTERNAL_ACTION.WALK;
		return nextPost;
	}

	/**
	 * return the next 1-neighborhood position fonction of a far position
	 * No check if the position is free
	 * */
	private Position computeNextPositionFromFarPosition(Position lanePost) {
		Position ret = null;
		int xOffSet = lanePost.getX() - currentPosition.getX();
		int yOffSet = lanePost.getY() - currentPosition.getY();
		xOffSet = xOffSet / Math.abs(xOffSet); // -9 ---> -1  ; 1---> 1 ; 3---> 3
		yOffSet = yOffSet / Math.abs(yOffSet);
		ret = new Position(currentPosition.getX() + xOffSet, currentPosition.getY() + yOffSet);
		Assert.assertTrue(positionIsValide(ret)); //impossible normalement
		return ret;
	}

	/**
	 * 
	 * */
	private void doAction(Position actionPost) {
		switch (action) {
		case WALK:
			System.out.println("Le robot "+ id +" va bouger");
			eco_requires().pEnvAction().moveRobot(currentPosition, actionPost);
			currentPosition = actionPost;
			break;
		case PULL :
			System.out.println("Le robot "+ id +" va pull");
			eco_requires().pEnvAction().pullResource(actionPost, currentPosition);
			break;
		case PUSH :
			System.out.println("Le robot "+ id +" va push");
			eco_requires().pEnvAction().pushResource(actionPost, currentPosition);
			break;
		case NOTHING:
			break;
		}

	}
	
	private static boolean positionIsValide(Position posit) {
		return posit.getX()>0 && posit.getY()>0 && posit.getX()<X_MAX && posit.getY()<Y_MAX;
	}
	
	private boolean checkSuicideBeforStartCycle() {
		return eco_requires().pEnvLookAt().getWorldEntityAt(currentPosition).equals(WORLD_ENTITY.WALL);		
	}
	
}
