package rsma.cycle.robot.impl;

import java.util.HashMap;
import java.util.Map;

import rsma.robot.cycle.IRobotKnowlage;
import rsma.util.Position;

public class RobotKnowlage implements IRobotKnowlage{
	private Map<INTERNAL_LANE_STATUS, Position> laneMap = new HashMap<INTERNAL_LANE_STATUS, Position>();

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

}
