package rsma.aux.robot.impl;

import java.awt.Rectangle;

import org.junit.Assert;

import rsma.aux.robot.IRobotDecision;
import rsma.aux.robot.IRobotKnowlage;
import rsma.aux.robot.IRobotKnowlage.INTERNAL_LANE_STATUS;
import rsma.aux.robot.IRobotPerception;
import rsma.aux.robot.IRobotPerception.SEARCH_PERCEPTION;
import rsma.impl.RobotImpl;
import rsma.impl.RobotImpl.INTERNAL_AIM;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.util.Position;

public class RobotDecision implements IRobotDecision{

	private static enum INTERNAL_STATE {RESOURCE_SEARCH, ZONE_PULL_GO, ZONE_PUSH_GO, LANE_PULL_GO, LANE_PUSH_GO, LANE_IN, FORCE, FREEPLACE_SEARCH, LANE_SEARCH_UP, LANE_SEARCH_DOWN, RESIGNATION;

	public INTERNAL_LANE_STATUS getConvertToLaneStatus() {
		Assert.assertTrue(this == ZONE_PULL_GO || this == ZONE_PUSH_GO);
		return this == ZONE_PULL_GO ? INTERNAL_LANE_STATUS.PULL_LANE : INTERNAL_LANE_STATUS.PUSH_LANE ;
	}

	public boolean isLaneGoState() {
		return this == LANE_PULL_GO || this == LANE_PUSH_GO;
	}};
	private INTERNAL_STATE state = INTERNAL_STATE.ZONE_PULL_GO;

	private INTERNAL_ACTION action;
	private int cptCycleLaneWainting;
	private RobotImpl robotAgent;
	private IRobotKnowlage robotKnowlage;
	private IRobotPerception robotPerception;

	public RobotDecision(RobotImpl robotImpl, IRobotKnowlage robotKnowlage, IRobotPerception robotPerception) {
		this.robotAgent = robotImpl;
		this.robotKnowlage = robotKnowlage;
		this.robotPerception = robotPerception;
	}

	@Override
	public Position doDecision() {
		Position nextPost= null;
		Position currentPosition = robotAgent.getCurrentPosition();

		switch (state) {
		case LANE_PULL_GO:
			nextPost = lanePullGo(currentPosition);
			break;

		case LANE_PUSH_GO :
			nextPost = lanePushGo(currentPosition);
			break;

		case ZONE_PULL_GO:
			nextPost=zonePullGo(currentPosition);
			break;

		case RESOURCE_SEARCH:
			nextPost = ressourceSearch(currentPosition);
			break;

		case ZONE_PUSH_GO:
			nextPost = zonePushGo(currentPosition);
			break;

		case FREEPLACE_SEARCH:
			nextPost = freePlaceSearch(currentPosition);
			break;

		case LANE_SEARCH_UP:
			nextPost = laneSearchUp(currentPosition);
			break;

		case LANE_SEARCH_DOWN:
			nextPost = laneSearchDown(currentPosition);
			break;

		case LANE_IN:
			nextPost = laneIn(currentPosition);
			break;

		case RESIGNATION :
			nextPost = resignation(currentPosition);
			break;
		case FORCE:
			nextPost = force(currentPosition);
			break;
		}
		//TODO verif transition

		nextPost = computeAlternativeIfNeed(currentPosition, nextPost);
		return nextPost;
	}


	private Position force(Position currentPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	/**** N CONPUTED ALTERNATIVE [ALWAYLS]
	 * if action == walk /\ nextpost.e != EMPTY then { //si on marche et qu'il ya un qqchose l'à où on doit allez
	 *	 if(nextpost.e == WALL && WALL isDetect){//si c'est un mur
	 *
	 *		if(state == LANE_PULL_GO | LANE_PUSH_GO){ //si on est dans un état ateind de lane alors le lane a bougé
	 *			cleanLaneKnowlage(LANE_PULL_GO | LANE_PUSH_GO) //clean connaissances
	 *       }
	 *		if(wall is up){ //le mur est le mur du haut
	 *			state = LANE_SEARCH_DOWN //chercher en bas
	 *			// refaire LANE_SEARCH_DOWN !!
	 *		}else if(wall is down){ //le mur est le mur du bas
	 *			state = LANE_SEARCH_UP //chercher en haut
	 *			// refaire LANE_SEARCH_UP !!
	 *		}else if state == pushZoneGO{//sinon mur frontale 
	 *			state = bestLaneSearch() //return LANE_SEARCH_UP | LANE_SEARCH_DOWN //chercher en haut ou bas selon
	 *			// refaire LANE_SEARCH_UP ou LANE_SEARCH_DOWN
	 * 		}
	 *	 }else{
	 *		 	nextpost = compute_alternative_if_existe_or_restaure_with_post // si pas mur alors recher contourne sinon on reprend l'ancinne valeur
	 *	 }	
	 * }

	 */ 
	private Position computeAlternativeIfNeed(Position currentPosition,
			Position nextPost) {
		Position postAlt = nextPost;
		INTERNAL_AIM aim = robotAgent.getAim();
		WORLD_ENTITY we = robotPerception.getWorldEntityFromPosition(nextPost);
		if(action.equals(INTERNAL_ACTION.WALK) && !we.equals(WORLD_ENTITY.EMPTY) ){
			if(we.equals(WORLD_ENTITY.WALL)){
				if(state.isLaneGoState()){
					robotKnowlage.cleanLaneKnowlage(RobotUtils.getLaneStatusFromAim(aim));
				}
				if(nextPost.getY()==0){
					state = INTERNAL_STATE.LANE_SEARCH_DOWN;
					postAlt = laneSearchDown(currentPosition);
				}else if(nextPost.getY() == RobotUtils.Y_MAX){
					state = INTERNAL_STATE.LANE_SEARCH_UP;
					postAlt = laneSearchUp(currentPosition);
				}else{ //central wall
					state = getBestLaneSearch(aim, currentPosition);
					if(state.equals(INTERNAL_STATE.LANE_SEARCH_UP)){
						postAlt = laneSearchUp(currentPosition);
					}else{
						postAlt = laneSearchDown(currentPosition);
					}
				}
			}else{
				postAlt = tryToComputeAlt(currentPosition);
			}
		}
		return postAlt;
	}


	private Position tryToComputeAlt(Position currentPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	private INTERNAL_STATE getBestLaneSearch(INTERNAL_AIM aim, Position currentPosition) {
		Rectangle aimZone = RobotUtils.getZoneFromAim(aim);
		INTERNAL_STATE stateLaneSearch = null;
		Position positZone = new Position(aimZone.x, aimZone.y);
		Position upPosit = new Position(currentPosition.getX(), currentPosition.getY()-2);
		Position downPosit = new Position(currentPosition.getX(), currentPosition.getY()+2);
		int distUpPositToZone = Integer.MAX_VALUE;
		int distDownPositToZone = Integer.MAX_VALUE;
		if(RobotUtils.positionIsValide(upPosit)){
			distUpPositToZone = RobotUtils.getDistance(upPosit, positZone);
		}
		if(RobotUtils.positionIsValide(downPosit)){
			distDownPositToZone = RobotUtils.getDistance(downPosit, positZone);
		}
		if(distDownPositToZone>distUpPositToZone){
			stateLaneSearch = INTERNAL_STATE.LANE_SEARCH_UP;
		}else{
			stateLaneSearch = INTERNAL_STATE.LANE_SEARCH_DOWN;
		}
		return stateLaneSearch;
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
	private Position laneSearchUp(Position currentPosition) {
		return searchLane(currentPosition, -1);
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
	private Position laneSearchDown(Position currentPosition) {
		return searchLane(currentPosition, 1);
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
	private Position zonePushGo(Position currentPosition) {
		Position nextPost = null;
		boolean containFreePlace = robotPerception.perceptionHasFreePlace();
		if(containFreePlace || RobotUtils.pushZone.contains(currentPosition.getX(), currentPosition.getY())){
			state = INTERNAL_STATE.FREEPLACE_SEARCH;
			nextPost = freePlaceSearch(currentPosition);
		}else{
			nextPost = computeNextPositionFromRectangle(currentPosition, RobotUtils.pushZone);
			action = INTERNAL_ACTION.WALK;
		}
		return nextPost;
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
	private Position freePlaceSearch(Position currentPosition) {
		Position nextPost = null;
		Position freePlacePost = robotPerception.searchOnPerceptionFreePlacePosition();
		action = INTERNAL_ACTION.WALK;
		if(freePlacePost == null){
			nextPost = moveInPushZone(currentPosition);
		}else if(RobotUtils.getDistance(currentPosition, freePlacePost)==1){
			nextPost = freePlacePost;
			action = INTERNAL_ACTION.PUSH;
			robotAgent.setAim(INTERNAL_AIM.PULL_AIM);
			if(robotKnowlage.knowPullLane()){
				state = INTERNAL_STATE.LANE_PULL_GO;
			}else{
				state = INTERNAL_STATE.ZONE_PULL_GO;
			}
		}else{
			nextPost = computeNextPositionFromFarPosition(currentPosition, freePlacePost);
		}
		return nextPost;
	}

	/**
	 **** 0 LANE_PULL_GO
	 * if currentPost == LANE_PULL.post then//si lane atteind 
	 * 		state = LANE_IN//on le traverse
	 * 		// faire LANE_IN
	 * else
	 * 		 nxtPost = computeNextPost(LANE_PULL_GO.post);//sinon on avance (si le mur a bougé c gérer ds N)
	 * action = walk
	 * @return */
	private Position lanePullGo(Position currentPosition) {
		return lanePuXXGo(currentPosition, INTERNAL_LANE_STATUS.PULL_LANE);
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
	private Position zonePullGo(Position currentPosition) {
		Position nextPost = null;
		boolean hasRessourceFound = robotPerception.perceptionHasEntity(WORLD_ENTITY.RESOURCE, IRobotPerception.SEARCH_PERCEPTION.ALL) != null;
		if(hasRessourceFound || RobotUtils.pullZone.contains(currentPosition.getX(), currentPosition.getY())){
			state =INTERNAL_STATE.RESOURCE_SEARCH;
			nextPost = ressourceSearch(currentPosition);
		}else{
			nextPost = computeNextPositionFromRectangle(currentPosition, RobotUtils.pullZone);
			action = INTERNAL_ACTION.WALK;
		}
		return nextPost;
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
	private Position ressourceSearch(Position currentPosition) {
		Position nextPost = null;
		Position rescPost = robotPerception.perceptionHasEntity(WORLD_ENTITY.RESOURCE, IRobotPerception.SEARCH_PERCEPTION.ALL);
		action = INTERNAL_ACTION.WALK;
		if(rescPost == null){
			nextPost = moveInPullZone(currentPosition);
		}else if(RobotUtils.getDistance(currentPosition, rescPost) == 1){
			nextPost = rescPost;
			action = INTERNAL_ACTION.PULL;
			robotAgent.setAim(INTERNAL_AIM.PUSH_AIM);
			if(robotKnowlage.knowPushLane()){
				state =INTERNAL_STATE.LANE_PUSH_GO;
			}else{
				state =INTERNAL_STATE.ZONE_PUSH_GO;
			}
		}else{
			nextPost = computeNextPositionFromFarPosition(currentPosition, rescPost);
		}
		return nextPost;
	}


	/**
	 **** 1 LANE_PUSH_GO
	 * if currentPost == LANE_PUSH.post then//si lane atteind 
	 * 		state = LANE_IN//on le traverse
	 * 		// faire LANE_IN
	 * else
	 * 		 nxtPost = computeNextPost(LANE_PUSH_GO.post);//sinon on avance (si le mur a bougé c gérer ds N)
	 * action = walk*/
	private Position lanePushGo(Position currentPosition) {
		return lanePuXXGo(currentPosition, INTERNAL_LANE_STATUS.PUSH_LANE);
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
	private Position resignation(Position currentPosition) {
		Position nextPost = null;
		INTERNAL_AIM aim = robotAgent.getAim();	
		if(robotPerception.perceptionCurrentPositionIsLaneExit()){
			state = INTERNAL_STATE.FORCE;
			robotKnowlage.forgetTryLane();
			nextPost = force(currentPosition);
		}else{
			Position position = robotPerception.perceptionHasEntity(RobotUtils.getRobotSameTypeByAim(aim), SEARCH_PERCEPTION.FRONT);	
			int dist = RobotUtils.getDistance(currentPosition, position);
			if (dist==1) {
				action = INTERNAL_ACTION.NOTHING;
				nextPost = currentPosition;
			}else{
				action = INTERNAL_ACTION.WALK;
				if(aim.equals(INTERNAL_AIM.PUSH_AIM)){
					nextPost = new Position(currentPosition.getX()-1, currentPosition.getY());
				}else{
					nextPost = new Position(currentPosition.getX()+1, currentPosition.getY());
				}
			}				
		}
		return nextPost;
	}

	/**** 9 LANE_IN
	 *	if(isExitToLane){
	 *		state = (aim= PULL_AIM)=> ZONEPULLGO | ZONE_PUSH_GO
	 *		//update mapLane
	 *		// GO_PUXX_ZONE
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
	private Position laneIn(Position currentPosition) {
		Position nextPost = null;
		INTERNAL_AIM aim = robotAgent.getAim();
		if(robotPerception.perceptionCurrentPositionIsLaneExit()){
			state = aim.equals(INTERNAL_AIM.PULL_AIM) ? INTERNAL_STATE.ZONE_PULL_GO : INTERNAL_STATE.ZONE_PUSH_GO;			
			INTERNAL_LANE_STATUS laneStatus = state.getConvertToLaneStatus();
			robotKnowlage.confirmTryLane(laneStatus);
			if(state.equals(INTERNAL_STATE.ZONE_PULL_GO)){
				nextPost = zonePullGo(currentPosition);
			}else{
				nextPost = zonePushGo(currentPosition);
			}
		}else{
			WORLD_ENTITY oppositeRobotType = RobotUtils.getOppositeRobotTypeByAim(aim);
			Position otherRobotPosit = robotPerception.perceptionHasEntity(oppositeRobotType, SEARCH_PERCEPTION.FRONT);	
			int dist = RobotUtils.getDistance(currentPosition, otherRobotPosit);
			if(cptCycleLaneWainting<2 && dist==1){
				if(hadLanePriority(currentPosition, otherRobotPosit, oppositeRobotType)){
					cptCycleLaneWainting++;
					nextPost = currentPosition;
					action = INTERNAL_ACTION.NOTHING;
				}else{
					state = INTERNAL_STATE.RESIGNATION;
					cptCycleLaneWainting = 0;
					nextPost = resignation(currentPosition);
					// update lanemap ???
				}
			}else if(cptCycleLaneWainting == 2){
				state = INTERNAL_STATE.RESIGNATION;
				cptCycleLaneWainting = 0;
				nextPost = resignation(currentPosition);
				// update lanemap ??
			}else{
				otherRobotPosit = robotPerception.perceptionHasEntity(aim.equals(INTERNAL_AIM.PUSH_AIM)? WORLD_ENTITY.ROBOT_AND_RESOURCE : WORLD_ENTITY.ROBOT, SEARCH_PERCEPTION.FRONT);	
				dist = RobotUtils.getDistance(currentPosition, otherRobotPosit);
				if(dist < 3){
					state =INTERNAL_STATE.RESIGNATION;
					cptCycleLaneWainting = 0;
					nextPost = resignation(currentPosition);
					// update lanemap ???
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
		return nextPost;
	}

	private Position searchLane(Position currentPosition, int yDirection){
		Position nextPost = null;
		boolean hasLaneEntrance = robotPerception.perceptionCurrentPositionIsLaneEntrance();
		boolean isNotTheOtherLane = true;
		INTERNAL_AIM aim = robotAgent.getAim();
		INTERNAL_LANE_STATUS laneTypeOther = INTERNAL_LANE_STATUS.reverseStatus(RobotUtils.getLaneStatusFromAim(aim));
		Position position = robotKnowlage.getPositionOf(laneTypeOther);
		if(position != null){
			isNotTheOtherLane = !(position.getY()==currentPosition.getY());
		}

		if(hasLaneEntrance && isNotTheOtherLane){
			state = INTERNAL_STATE.LANE_IN;
			robotKnowlage.rememberLane(INTERNAL_LANE_STATUS.TRY, currentPosition);
			nextPost = laneIn(currentPosition);
		}else{
			nextPost = new Position(currentPosition.getX(), currentPosition.getY() + yDirection);
		}
		action = INTERNAL_ACTION.WALK;
		return nextPost;
	}

	/**
	 * Return true if I'm most close of my zone than he.
	 * */
	private boolean hadLanePriority(Position currentPosition, Position otherRobotPosit, WORLD_ENTITY oppositeRobotType) {
		INTERNAL_AIM aim = robotAgent.getAim();
		Rectangle myZone = RobotUtils.getZoneFromAim(aim);
		Rectangle hisZone = RobotUtils.getZoneFromWE(oppositeRobotType);
		int myDist = RobotUtils.getDistance(currentPosition, new Position(myZone.x, myZone.y)); 
		int hisDist = RobotUtils.getDistance(otherRobotPosit, new Position(hisZone.x, hisZone.y));
		return myDist<hisDist;
	}

	/**
	 * Return a position to go in push zone
	 * */
	private Position moveInPushZone(Position currentPosition) {
		return RobotUtils.getRandomPostOnRectangle(RobotUtils.pushZone, currentPosition);
	}

	/**
	 * Return a position to go in pull zone
	 * */
	private Position moveInPullZone(Position currentPosition) {
		return RobotUtils.getRandomPostOnRectangle(RobotUtils.pullZone, currentPosition);
	}

	private Position computeNextPositionFromRectangle(Position currentPosition, Rectangle pullZone2) {
		Position post = new Position(pullZone2.x, pullZone2.y);
		return computeNextPositionFromFarPosition(currentPosition, post);
	}


	/**
	 * Return the next position to lane entrance or if the entrance of lane is found switch to LANE_IN state. action is set to walk
	 * */
	private Position lanePuXXGo(Position currentPosition, IRobotKnowlage.INTERNAL_LANE_STATUS lane) {
		Position nextPost = null;
		Position lanePost = robotKnowlage.getPositionOf(lane);
		Assert.assertNotNull(lanePost);
		if(currentPosition.equals(lanePost)){
			state = INTERNAL_STATE.LANE_IN;
			nextPost = laneIn(currentPosition);
		}else{
			nextPost  = computeNextPositionFromFarPosition(currentPosition, lanePost);
		}
		action = INTERNAL_ACTION.WALK;
		return nextPost;
	}

	/**
	 * return the next 1-neighborhood position fonction of a far position
	 * No check if the position is free
	 * */
	private Position computeNextPositionFromFarPosition(Position currentPosition, Position lanePost) {
		Position ret = null;
		int xOffSet = lanePost.getX() - currentPosition.getX();
		int yOffSet = lanePost.getY() - currentPosition.getY();
		if(xOffSet!=0){
			xOffSet = xOffSet / Math.abs(xOffSet); // -9 ---> -1  ; 1---> 1 ; 3---> 3
		}
		if(yOffSet!=0){
			yOffSet = yOffSet / Math.abs(yOffSet);
		}
		ret = new Position(currentPosition.getX() + xOffSet, currentPosition.getY() + yOffSet);
		Assert.assertTrue(RobotUtils.positionIsValide(ret)); //impossible normalement
		return ret;
	}

	@Override
	public INTERNAL_ACTION getActionToDo(){
		return action;
	}
}
