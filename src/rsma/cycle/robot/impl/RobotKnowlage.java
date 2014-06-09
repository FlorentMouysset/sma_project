package rsma.cycle.robot.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rsma.robot.cycle.IRobotKnowlage;
import rsma.util.Position;

public class RobotKnowlage implements IRobotKnowlage{
	private Map<INTERNAL_LANE_STATUS, Position> laneMap = new HashMap<INTERNAL_LANE_STATUS, Position>();
	private Set<Position> freePlaceResourcesOnPullZone = new HashSet<Position>();
	private Set<Position> freePlaceOnPushZone = new HashSet<Position>();
	private int nbSucces = 0;
	private int nbConflict = 0;
	
	@Override
	public boolean knowPushLane() {
		return laneMap.containsKey(INTERNAL_LANE_STATUS.PUSH_LANE);
	}

	@Override
	public boolean knowPullLane() {
		return laneMap.containsKey(INTERNAL_LANE_STATUS.PULL_LANE);
	}

	@Override
	public Position getPositionOf(INTERNAL_LANE_STATUS laneType) {
		return laneMap.get(laneType);
	}

	@Override
	public void rememberLane(INTERNAL_LANE_STATUS try1, Position currentPosition) {
		laneMap.put(INTERNAL_LANE_STATUS.TRY, currentPosition);
	}

	@Override
	public void confirmTryLane(INTERNAL_LANE_STATUS laneStatus) {
		Position positLaneEntrance = laneMap.get(INTERNAL_LANE_STATUS.TRY);
		if(positLaneEntrance!=null){
			laneMap.put(laneStatus, positLaneEntrance);					
			laneMap.remove(INTERNAL_LANE_STATUS.TRY);
			internalLaneMapClean(INTERNAL_LANE_STATUS.reverseStatus(laneStatus), positLaneEntrance);
		}
	}

	private void internalLaneMapClean(INTERNAL_LANE_STATUS reverseStatus,
			Position positLaneEntrance) {
		Position posit = laneMap.get(reverseStatus);
		if(posit!= null && positLaneEntrance.getY() == posit.getY()){
			laneMap.remove(reverseStatus);
		}
	}

	@Override
	public void reverseLaneKnowlage() {
		Position pullLanePosit = laneMap.get(INTERNAL_LANE_STATUS.PULL_LANE);
		Position pushLanePosit = laneMap.get(INTERNAL_LANE_STATUS.PUSH_LANE);
		if(pullLanePosit!=null){
			laneMap.put(INTERNAL_LANE_STATUS.PUSH_LANE, pullLanePosit);
		}
		if(pushLanePosit!=null){
			laneMap.put(INTERNAL_LANE_STATUS.PULL_LANE, pushLanePosit);
		}
	}
	
	@Override
	public void forgetTryLane() {
		laneMap.remove(INTERNAL_LANE_STATUS.TRY);
	}

	@Override
	public void cleanLaneKnowlage(INTERNAL_LANE_STATUS laneStatusFromAim) {
		laneMap.remove(laneStatusFromAim);
	}

	@Override
	public void rememberOldResourcesPlaceFreeNow(Position currentPosition) {
		freePlaceResourcesOnPullZone.add(currentPosition);
	}

	@Override
	public int countFreeResourcePlaces() {
		return freePlaceResourcesOnPullZone.size();
	}

	@Override
	public void updateFreePlaces(List<Position> freePlacesPost,
			List<Position> rscPlacesPost) {
		freePlaceOnPushZone.addAll(freePlacesPost);
		freePlaceOnPushZone.removeAll(rscPlacesPost);
	}

	@Override
	public Position getAFreePlace() {
		return freePlaceOnPushZone.iterator().next();
	}

	@Override
	public boolean knowFreePlace() {
		return !freePlaceOnPushZone.isEmpty();
	}

	@Override
	public int getNbSucces() {
		return nbSucces;
	}

	@Override
	public int getNbConflicts() {
		return nbConflict;
	}

	@Override
	public void addSucces() {
		nbSucces++;
	}

	@Override
	public void addConflicts() {
		nbConflict++;
	}

	@Override
	public void rememberOldResourcesPlaceFreeNow(List<Position> freePlaces) {
		freePlaceResourcesOnPullZone.addAll(freePlaces);
	}



}
