package rsma.cycle.robot.impl;

import java.awt.Rectangle;
import java.util.Random;

import org.junit.Assert;

import rsma.impl.RobotImpl.INTERNAL_AIM;
import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.robot.cycle.IRobotKnowlage.INTERNAL_LANE_STATUS;
import rsma.util.Position;

public class RobotUtils {
	public static Rectangle pullZone;
	public static Rectangle pushZone;
	public static int X_MAX;
	public static int Y_MAX;
	private static Random rand;
	
	public static boolean positionIsValide(Position posit) {
		return posit.getX()>=0 && posit.getY()>=0 && posit.getX()<X_MAX && posit.getY()<Y_MAX;
	}
	
	/**
	 * Return a random position into the rectangle and isn't excludePost
	 * */
	public static Position getRandomPostOnRectangle(Rectangle rectangle, Position excludePost){
		Position retPost = null;
		initRand();
		do{
			int x = getRandomInt(rectangle.width-rectangle.x);
			x +=rectangle.x;
			int y = getRandomInt(rectangle.height-rectangle.y);
			y +=rectangle.y;
			retPost = new Position(x, y);
			Assert.assertTrue(rectangle.contains(x, y));
		}while(retPost.equals(excludePost));
		Assert.assertTrue(positionIsValide(retPost));
		return retPost;
	}
	
	private static void initRand() {
		if(rand == null){
			rand = new Random(System.currentTimeMillis());
		}
	}

	/**
	 * Return the number of move to reach posit2 from posit1. If return 1 posit2 is the 1-neighborhood to posit1
	 * */
	public static int getDistance(Position posit1, Position posit2) {
		int xOffset = Math.abs(posit1.getX() - posit2.getX());
		int yOffset = Math.abs(posit1.getY() - posit2.getY());
		return Math.max(xOffset, yOffset);
	}


	public static WORLD_ENTITY getWEFromAim(INTERNAL_AIM aim){
		return aim.equals(INTERNAL_AIM.PULL_AIM) ? WORLD_ENTITY.ROBOT : WORLD_ENTITY.ROBOT_AND_RESOURCE;
	}

	public static INTERNAL_LANE_STATUS getLaneStatusFromAim(INTERNAL_AIM aim){
		return aim.equals(INTERNAL_AIM.PUSH_AIM) ? INTERNAL_LANE_STATUS.PUSH_LANE : INTERNAL_LANE_STATUS.PULL_LANE;
	}

	public static WORLD_ENTITY getOppositeRobotTypeByAim(INTERNAL_AIM aim) {
		return aim.equals(INTERNAL_AIM.PULL_AIM)? WORLD_ENTITY.ROBOT_AND_RESOURCE : WORLD_ENTITY.ROBOT;
	}

	public static WORLD_ENTITY getRobotSameTypeByAim(INTERNAL_AIM aim) {
		return aim.equals(INTERNAL_AIM.PUSH_AIM)? WORLD_ENTITY.ROBOT_AND_RESOURCE : WORLD_ENTITY.ROBOT;
	}

	public static Rectangle getZoneFromAim(INTERNAL_AIM aim) {
		return aim.equals(INTERNAL_AIM.PULL_AIM) ? pullZone : pushZone;
	}

	public static Rectangle getZoneFromWE(WORLD_ENTITY oppositeRobotType) {
		Assert.assertTrue(oppositeRobotType.equals(WORLD_ENTITY.ROBOT) || oppositeRobotType.equals(WORLD_ENTITY.ROBOT_AND_RESOURCE));
		return oppositeRobotType.equals(WORLD_ENTITY.ROBOT) ? pullZone : pushZone;
	}

	public static double getEuclideDistance(Position posit, Position positZone) {
		return Math.sqrt( Math.pow((positZone.getX() - posit.getX()),2) + Math.pow( (positZone.getY() - posit.getY()), 2) );
	}

	public static boolean getRandomBool() {
		initRand();
		return rand.nextBoolean();
	}

	public static int getRandomInt(int size) {
		initRand();
		return rand.nextInt(size);
	}
	
}
