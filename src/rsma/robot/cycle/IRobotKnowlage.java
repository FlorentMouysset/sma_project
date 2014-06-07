package rsma.robot.cycle;

import rsma.util.Position;

public interface IRobotKnowlage {
	public enum INTERNAL_LANE_STATUS {TRY, PULL_LANE, PUSH_LANE;
	
	/**TRY-> TRY ; PULL --> PUSH ; PUSH --> PULL*/
		public static INTERNAL_LANE_STATUS reverseStatus(INTERNAL_LANE_STATUS status){
			return status == PULL_LANE ? PUSH_LANE : PULL_LANE;
		}
	}

	boolean knowPushLane();
	boolean knowPullLane();
	Position getPositionOf(INTERNAL_LANE_STATUS laneType);
	void rememberLane(INTERNAL_LANE_STATUS try1, Position currentPosition);
	void confirmTryLane(INTERNAL_LANE_STATUS laneStatus);
	void forgetTryLane();
	void cleanLaneKnowlage(INTERNAL_LANE_STATUS laneStatusFromAim);

}
