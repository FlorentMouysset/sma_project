package rsma.cycle.robot.impl;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import rsma.impl.RobotImpl;
import rsma.impl.RobotImpl.INTERNAL_AIM;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.robot.cycle.IRobotDecision;
import rsma.robot.cycle.IRobotKnowlage;
import rsma.robot.cycle.IRobotPerception;
import rsma.robot.cycle.IRobotKnowlage.INTERNAL_LANE_STATUS;
import rsma.robot.cycle.IRobotPerception.SEARCH_PERCEPTION;
import rsma.util.Position;

public class RobotDecision implements IRobotDecision{

	private enum INTERNAL_STATE {RESOURCE_SEARCH, ZONE_PULL_GO, ZONE_PUSH_GO, LANE_PULL_GO, LANE_PUSH_GO, LANE_IN, FREEPLACE_SEARCH, LANE_SEARCH_UP, LANE_SEARCH_DOWN, RESIGNATION;

	public INTERNAL_LANE_STATUS getConvertToLaneStatus() {
		Assert.assertTrue(this == ZONE_PULL_GO || this == ZONE_PUSH_GO);
		return this == ZONE_PULL_GO ? INTERNAL_LANE_STATUS.PULL_LANE : INTERNAL_LANE_STATUS.PUSH_LANE ;
	}

	public boolean isLaneGoState() {
		return this == LANE_PULL_GO || this == LANE_PUSH_GO;
	}
	}
	private static final int NB_CYC_RESI_OPP = 5;
	private int ranAmplitudeCycResiOpp= 3;
	private static final int NB_CYC_RESI_FRIEND = 1;
	private static final int RAN_AMPITUDE_CYC_RESI_FRIEND = 3;
	private static final int PROBA_TO_NOTHING = 20; //probabilité de ne pas bouger si l'on est bloqué par un robot. Sur 100 

	private INTERNAL_STATE state = INTERNAL_STATE.ZONE_PULL_GO;

	private INTERNAL_ACTION action;
	private int cptCycleLaneWainting;
	private RobotImpl robotAgent;
	private IRobotKnowlage robotKnowlage;
	private IRobotPerception robotPerception;
	private boolean force = false;

	public RobotDecision(RobotImpl robotImpl, IRobotKnowlage robotKnowlage, IRobotPerception robotPerception) {
		this.robotAgent = robotImpl;
		this.robotKnowlage = robotKnowlage;
		this.robotPerception = robotPerception;
	}

	@Override
	public Position doDecision() {
		Position nextPost= null;
		Position currentPosition = robotAgent.getCurrentPosition();
		System.out.println("Robot " + robotAgent.getID() + " state " + state + " currentP" + currentPosition + " " + robotAgent.getAim());
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
			nextPost = resourceSearch(currentPosition);
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
		}
		if(nextPost!=null){
			nextPost = computeAlternativeIfNeed(currentPosition, nextPost);
		}
		return nextPost;
	}


	/****
	 * Calcule une position aternative si la position souhaitée n'est pas libre.
	 * Si aucune position n'est trouvée alors le robot attend
	 */ 
	private Position computeAlternativeIfNeed(Position currentPosition,
			Position nextPost) {
		Position postAlt = nextPost; //la position alternative (à calculer)
		INTERNAL_AIM aim = robotAgent.getAim();
		WORLD_ENTITY we = robotPerception.getWorldEntityFromPosition(currentPosition, nextPost);
		if(action.equals(INTERNAL_ACTION.WALK) && !we.equals(WORLD_ENTITY.EMPTY) ){
			if(we.equals(WORLD_ENTITY.WALL)&& !robotPerception.isInLane(currentPosition)){//la position souhaitée est un mur
				if(state.isLaneGoState()){//si l'état était un état de recherche de couloir alors le couloir a bougé 
					robotKnowlage.cleanLaneKnowlage(RobotUtils.getLaneStatusFromAim(aim));
				}
				if(nextPost.getY()==0){//si mur du haut
					state = INTERNAL_STATE.LANE_SEARCH_DOWN; //alors on descend
					postAlt = laneSearchDown(currentPosition);
				}else if(nextPost.getY() == RobotUtils.Y_MAX){//si mur du bas
					state = INTERNAL_STATE.LANE_SEARCH_UP;//alors on monte
					postAlt = laneSearchUp(currentPosition);
				}else{ //central wall
					state = getBestLaneSearch(aim, currentPosition); //déduire la meilleur direction de recherche
					if(state.equals(INTERNAL_STATE.LANE_SEARCH_UP)){
						postAlt = laneSearchUp(currentPosition);
					}else{
						postAlt = laneSearchDown(currentPosition);
					}
				}
			}else if(robotPerception.isInLane(currentPosition)){//si on est dans un couloir alors passer
				state = INTERNAL_STATE.LANE_IN;//en mode laneIN
				postAlt = laneIn(currentPosition);
			}else{//sinon la position souhaitée est occupée pas un robot (chargé ou pas) ou une ressource
				if(we.equals(WORLD_ENTITY.ROBOT)||we.equals(WORLD_ENTITY.ROBOT_AND_RESOURCE)){ //si c'est un robot
					if(RobotUtils.getRandomInt(100)>PROBA_TO_NOTHING || force){//si on peut bouger on l'on est en "force"
						postAlt = tryToComputeAlt(currentPosition, nextPost);
					}else{
						action = INTERNAL_ACTION.NOTHING;
						postAlt = currentPosition;
					}
				}else if(we.equals(WORLD_ENTITY.RESOURCE)){//si c'est une ressource alors il faut bouger
					postAlt = tryToComputeAlt(currentPosition, nextPost);
				}
			}
			if(!robotPerception.getWorldEntityFromPosition(currentPosition, postAlt).equals(WORLD_ENTITY.EMPTY)){//(re) verificaton 
				postAlt = currentPosition;
				action = INTERNAL_ACTION.NOTHING;
			}
		}
		return postAlt;
	}

/**
 * Calcule une position alternative à nextPost sachant que currentPosition est la position de départ
 * */
	private Position tryToComputeAlt(Position currentPosition, Position nextPost) {
		Position alt = currentPosition;
		List<Position> goodsAlts = new ArrayList<Position>(); //The list of alternatives
		Position tryAlt; //current alternative
		int bestDist = Integer.MAX_VALUE; //init the best distance
		int tryDist;
		for(int xOffset=-1; xOffset<2; xOffset ++){
			for(int yOffset=-1; yOffset<2; yOffset++){ //for all 1-neighborhood of robot
				tryAlt = new Position(currentPosition.getX() + xOffset, currentPosition.getY() + yOffset);
				if(!tryAlt.equals(nextPost) && RobotUtils.positionIsValide(tryAlt)){ //if the current neighbor is valid
					tryDist = RobotUtils.getDistance(tryAlt, nextPost); //compute the distance from bad position to alternative
					if(robotPerception.getWorldEntityFromPosition(currentPosition, tryAlt).equals(WORLD_ENTITY.EMPTY)&&tryDist<=bestDist){ //if the alternative is empty and equals or best than current best distance
						if(tryDist<bestDist){ //if best 
							goodsAlts.clear(); //clear all previous alternatives
							bestDist = tryDist; //update the best distance
						}
						goodsAlts.add(tryAlt);//add alternative
					}
				}
			}
		}
		alt = goodsAlts.get(RobotUtils.getRandomInt(goodsAlts.size()));//choice a random alternative
		if(!alt.equals(currentPosition)){
			action = INTERNAL_ACTION.WALK;
		}
		return alt;
	}

	private INTERNAL_STATE getBestLaneSearch(INTERNAL_AIM aim, Position currentPosition) {
		Rectangle aimZone = RobotUtils.getZoneFromAim(aim);
		INTERNAL_STATE stateLaneSearch = null;
		Position positZone = new Position(aimZone.x, aimZone.y);
		Position upPosit = new Position(currentPosition.getX(), currentPosition.getY()-2);
		Position downPosit = new Position(currentPosition.getX(), currentPosition.getY()+2);
		double distUpPositToZone = Double.MAX_VALUE;
		double distDownPositToZone = Double.MAX_VALUE;
		if(RobotUtils.positionIsValide(upPosit)){
			distUpPositToZone = RobotUtils.getEuclideDistance(upPosit, positZone);
		}
		if(RobotUtils.positionIsValide(downPosit)){
			distDownPositToZone = RobotUtils.getEuclideDistance(downPosit, positZone);
		}

		if(distDownPositToZone>distUpPositToZone){
			stateLaneSearch = INTERNAL_STATE.LANE_SEARCH_UP;
		}else if(distDownPositToZone==distUpPositToZone){
			stateLaneSearch = RobotUtils.getRandomBool() ? INTERNAL_STATE.LANE_SEARCH_UP : INTERNAL_STATE.LANE_SEARCH_DOWN;
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
		}else if(robotPerception.perceptionCurrentPositionIsLaneEntrance(SEARCH_PERCEPTION.RIGHT)){
			state = INTERNAL_STATE.LANE_IN;
			nextPost = laneIn(currentPosition);
		}else if(robotKnowlage.knowFreePlace()){
			nextPost = computeNextPositionFromFarPosition(currentPosition, robotKnowlage.getAFreePlace());
			action = INTERNAL_ACTION.WALK;
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
		List<Position> freePlacesPost = robotPerception.searchOnPerceptionFreePlacesPositionOnPushZone();
		List<Position> rscPlacesPost = robotPerception.searchOnPerceptionResourcesPlacesPositionOnPushZone();
		Position freePlacePost  = freePlacesPost.get(freePlacesPost.size()-1);
		robotKnowlage.updateFreePlaces(freePlacesPost, rscPlacesPost);
		action = INTERNAL_ACTION.WALK;
		if(freePlacePost == null){
			nextPost = moveInPushZone(currentPosition);
		}else if(RobotUtils.getDistance(currentPosition, freePlacePost)==1){
			nextPost = freePlacePost;
			rscPlacesPost.clear();
			rscPlacesPost.add(freePlacePost);
			freePlacesPost.clear();
			robotKnowlage.updateFreePlaces(freePlacesPost, rscPlacesPost);
			robotKnowlage.addSucces();
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
		Position rscPosit = robotPerception.perceptionHasEntity(WORLD_ENTITY.RESOURCE, IRobotPerception.SEARCH_PERCEPTION.ALL);
		boolean hasRessourceFound = (rscPosit != null) && !RobotUtils.pushZone.contains(rscPosit.getX(), rscPosit.getY());
		if( hasRessourceFound || RobotUtils.pullZone.contains(currentPosition.getX(), currentPosition.getY())){
			state =INTERNAL_STATE.RESOURCE_SEARCH;
			nextPost = resourceSearch(currentPosition);
		}else if(robotPerception.perceptionCurrentPositionIsLaneEntrance(SEARCH_PERCEPTION.LEFT)){
			state = INTERNAL_STATE.LANE_IN;
			nextPost = laneIn(currentPosition);
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
	private Position resourceSearch(Position currentPosition) {
		Position nextPost = null;
		Position rescPost = robotPerception.perceptionHasEntity(WORLD_ENTITY.RESOURCE, IRobotPerception.SEARCH_PERCEPTION.ALL);
		List<Position> freePlaces = robotPerception.searchOnPerceptionFreePlacesPositionOnPullZone();
		action = INTERNAL_ACTION.WALK;
		if(RobotUtils.pullZone.contains(currentPosition.getX(), currentPosition.getY())){
			robotKnowlage.rememberOldResourcesPlaceFreeNow(freePlaces);			
		}
		if(rescPost == null){
			int nbFRP = robotKnowlage.countFreeResourcePlaces();
			if(RobotUtils.pullZone.height * RobotUtils.pullZone.width != nbFRP){
				nextPost = moveInPullZone(currentPosition);
			}//else if -> end = suicide !
		}else if(RobotUtils.getDistance(currentPosition, rescPost) == 1){
			action = INTERNAL_ACTION.PULL;
			nextPost = rescPost;
			robotAgent.setAim(INTERNAL_AIM.PUSH_AIM);
			robotKnowlage.rememberOldResourcesPlaceFreeNow(rescPost);			

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
		SEARCH_PERCEPTION perceptType = (aim == INTERNAL_AIM.PULL_AIM) ? SEARCH_PERCEPTION.LEFT : SEARCH_PERCEPTION.RIGHT;
		if(robotPerception.perceptionCurrentPositionIsLaneExit(perceptType)){
			force = true;
			robotKnowlage.confirmTryLane(RobotUtils.getLaneStatusFromAim(aim));
			robotKnowlage.reverseLaneKnowlage();
			robotKnowlage.addConflicts();
			nextPost = restartRandomSearchLane(currentPosition, aim);
		}else{
			Position position = robotPerception.perceptionHasEntity(RobotUtils.getRobotSameTypeByAim(aim), perceptType.reverse());	
			int dist = Integer.MAX_VALUE;
			if(position != null){
				dist = RobotUtils.getDistance(currentPosition, position);
			}
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

	private Position restartRandomSearchLane(Position currentPosition,
			INTERNAL_AIM aim) {
		Position restartPosition = null;
		INTERNAL_STATE restartState = null;	
		if(RobotUtils.getRandomBool()){
			restartPosition = new Position(currentPosition.getX(), currentPosition.getY()+1);
			if(RobotUtils.positionIsValide(restartPosition)){
				if(!robotPerception.getWorldEntityFromPosition(currentPosition, restartPosition).equals(WORLD_ENTITY.WALL)){
					restartState = INTERNAL_STATE.LANE_SEARCH_DOWN;
				}
			}
		}
		if(restartState==null){
			restartPosition = new Position(currentPosition.getX(), currentPosition.getY()-1);
			if(RobotUtils.positionIsValide(restartPosition)){
				if(!robotPerception.getWorldEntityFromPosition(currentPosition, restartPosition).equals(WORLD_ENTITY.WALL)){
					restartState = INTERNAL_STATE.LANE_SEARCH_UP;
				}else{
					restartState = null;
				}
			}
			if(restartState==null){
				restartPosition = new Position(currentPosition.getX(), currentPosition.getY()+1);
				restartState = INTERNAL_STATE.LANE_SEARCH_DOWN;
			}
		}
		action = INTERNAL_ACTION.WALK;
		state = restartState;
		return restartPosition;
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
		SEARCH_PERCEPTION perceptType = (aim != INTERNAL_AIM.PULL_AIM) ? SEARCH_PERCEPTION.LEFT : SEARCH_PERCEPTION.RIGHT;
		if(robotPerception.perceptionCurrentPositionIsLaneExit(perceptType)){
			state = aim.equals(INTERNAL_AIM.PULL_AIM) ? INTERNAL_STATE.ZONE_PULL_GO : INTERNAL_STATE.ZONE_PUSH_GO;			
			INTERNAL_LANE_STATUS laneStatus = state.getConvertToLaneStatus();
			robotKnowlage.confirmTryLane(laneStatus);
			force=false;
			if(state.equals(INTERNAL_STATE.ZONE_PULL_GO)){
				nextPost = zonePullGo(currentPosition);
			}else{
				nextPost = zonePushGo(currentPosition);
			}
		}else{
			WORLD_ENTITY oppositeRobotType = RobotUtils.getOppositeRobotTypeByAim(aim);
			Position otherRobotPosit = robotPerception.perceptionHasEntity(oppositeRobotType, perceptType.reverse());	
			int dist = Integer.MAX_VALUE;
			if(otherRobotPosit != null){
				dist = RobotUtils.getDistance(currentPosition, otherRobotPosit);
			}
			ranAmplitudeCycResiOpp = updateConviction();
			int nbRandCycOpp = RobotUtils.getRandomInt(NB_CYC_RESI_OPP)+ranAmplitudeCycResiOpp;
			if(cptCycleLaneWainting<nbRandCycOpp && dist==1){
				if(hadLanePriority(currentPosition, otherRobotPosit, oppositeRobotType) || force ){
					cptCycleLaneWainting++;
					nextPost = currentPosition;
					action = INTERNAL_ACTION.NOTHING;
				}else{
					if(force){
						if(RobotUtils.getRandomInt(100)>PROBA_TO_NOTHING){
							cptCycleLaneWainting=0;
							nextPost = currentPosition;
							action = INTERNAL_ACTION.NOTHING;
							force = false;
						}else{
							state = INTERNAL_STATE.RESIGNATION;
							cptCycleLaneWainting = 0;
							nextPost = resignation(currentPosition);
						}
					}else{
						state = INTERNAL_STATE.RESIGNATION;
						cptCycleLaneWainting = 0;
						nextPost = resignation(currentPosition);
						// update lanemap ???
					}
				}
			}else if(cptCycleLaneWainting >= NB_CYC_RESI_OPP){
				state = INTERNAL_STATE.RESIGNATION; //TODO del ??
				cptCycleLaneWainting = 0;
				nextPost = resignation(currentPosition);
				// update lanemap ??
			}else{
				otherRobotPosit = robotPerception.perceptionHasEntity(aim.equals(INTERNAL_AIM.PUSH_AIM)? WORLD_ENTITY.ROBOT_AND_RESOURCE : WORLD_ENTITY.ROBOT, perceptType.reverse());	
				dist = Integer.MAX_VALUE;
				if(otherRobotPosit != null){
					dist = RobotUtils.getDistance(currentPosition, otherRobotPosit);
				}
				int nbRandCycFriend = RobotUtils.getRandomInt(NB_CYC_RESI_FRIEND)+RAN_AMPITUDE_CYC_RESI_FRIEND;
				if(dist < 3 && cptCycleLaneWainting>=nbRandCycFriend){
					state =INTERNAL_STATE.RESIGNATION;
					cptCycleLaneWainting = 0;
					nextPost = resignation(currentPosition);
					// update lanemap ???
				}else if(dist <= 3 && cptCycleLaneWainting<nbRandCycFriend){
					cptCycleLaneWainting++;
					action = INTERNAL_ACTION.NOTHING;
					nextPost = currentPosition;
				}else{
					cptCycleLaneWainting = 0;
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

	private int updateConviction() {
		int nbSucces = robotKnowlage.getNbSucces();
		int nbConflicts = robotKnowlage.getNbConflicts();
		int ret = 3;
		if(nbConflicts==0){
			ret = 7;
		}else{
			ret = (nbSucces/nbConflicts>1) ? 7 : 3; 
		}
		return ret;
	}

	private Position searchLane(Position currentPosition, int yDirection){
		Position nextPost = null;
		INTERNAL_AIM aim = robotAgent.getAim();
		SEARCH_PERCEPTION perceptType = (aim == INTERNAL_AIM.PULL_AIM) ? SEARCH_PERCEPTION.LEFT : SEARCH_PERCEPTION.RIGHT;
		boolean hasLaneEntrance = robotPerception.perceptionCurrentPositionIsLaneEntrance(perceptType);
		boolean isNotTheOtherLane = true;
		INTERNAL_LANE_STATUS laneTypeOther = INTERNAL_LANE_STATUS.reverseStatus(RobotUtils.getLaneStatusFromAim(aim));
		Position position = robotKnowlage.getPositionOf(laneTypeOther);
		if(position != null){
			isNotTheOtherLane = !(position.getY()==currentPosition.getY()) || force;
		}
		if(hasLaneEntrance && isNotTheOtherLane){
			state = INTERNAL_STATE.LANE_IN;
			robotKnowlage.rememberLane(INTERNAL_LANE_STATUS.TRY, currentPosition);
			nextPost = laneIn(currentPosition);
		}else{
			int xOffset = aim == INTERNAL_AIM.PULL_AIM ? -1 : 1;
			action = INTERNAL_ACTION.WALK;
			if(!robotPerception.yWallDetect(xOffset)){
				Position newPosit = new Position(currentPosition.getX()+xOffset, currentPosition.getY());
				if(robotPerception.getWorldEntityFromPosition(currentPosition, newPosit).equals(WORLD_ENTITY.EMPTY)){
					nextPost = newPosit;
				}else{
					if(RobotUtils.getRandomBool()){
						nextPost = new Position(currentPosition.getX(), currentPosition.getY() + yDirection);
					}else{
						nextPost = currentPosition;
						action = INTERNAL_ACTION.NOTHING;
					}
				}
			}else{
				nextPost = new Position(currentPosition.getX(), currentPosition.getY() + yDirection);
			}

		}
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
		Position randomPositAim = RobotUtils.getRandomPostOnRectangle(RobotUtils.pushZone, currentPosition);
		randomPositAim = computeNextPositionFromFarPosition(currentPosition, randomPositAim);
		return randomPositAim;
	}

	/**
	 * Return a position to go in pull zone
	 * */
	private Position moveInPullZone(Position currentPosition) {
		Position randomPositAim = RobotUtils.getRandomPostOnRectangle(RobotUtils.pullZone, currentPosition);
		randomPositAim = computeNextPositionFromFarPosition(currentPosition, randomPositAim);
		return randomPositAim;
	}

	private Position computeNextPositionFromRectangle(Position currentPosition, Rectangle pullZone2) {
		Position post = new Position((int)pullZone2.getCenterX(), (int)pullZone2.getCenterY());
		return computeNextPositionFromFarPosition(currentPosition, post);
	}


	/**
	 * Return the next position to lane entrance or if the entrance of lane is found switch to LANE_IN state. action is set to walk
	 * */
	private Position lanePuXXGo(Position currentPosition, IRobotKnowlage.INTERNAL_LANE_STATUS lane) {
		Position nextPost = null;
		Position lanePost = robotKnowlage.getPositionOf(lane);
		System.out.println("RD : knowlage get postLane" + lane + " "+lanePost);
		Assert.assertNotNull(lanePost);
		if(currentPosition.equals(lanePost)){
			state = INTERNAL_STATE.LANE_IN;
			nextPost = laneIn(currentPosition);
		}else{
			action = INTERNAL_ACTION.WALK;
			nextPost  = computeNextPositionFromFarPosition(currentPosition, lanePost);
		}
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
