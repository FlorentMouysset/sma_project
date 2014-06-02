package rsma.impl;

import java.awt.Rectangle;

import rsma.EcoJoining;
import rsma.interfaces.IEnvironnementActions;
import rsma.interfaces.IEnvironnementAnalysis;
import rsma.util.Position;

public class EcoJoinImpl extends EcoJoining{

	@Override
	protected JoiningEntity make_JoiningEntity() {
		return new JoiningEntity() {
			
			@Override
			protected IEnvironnementAnalysis make_joinEnvAnalyse() {
				return new IEnvironnementAnalysis() {
					
					@Override
					public WORLD_ENTITY getWorldEntityAt(Position position) {
						System.out.println("Join LookAt " + position);
						return eco_requires().prxLookAtPort().getWorldEntityAt(position);
					}

					@Override
					public Rectangle getPullZone() {
						return eco_requires().prxLookAtPort().getPullZone();
					}

					@Override
					public Rectangle getPushZone() {
						return eco_requires().prxLookAtPort().getPushZone();
					}
				};
			}
			
			@Override
			protected IEnvironnementActions make_joinActions() {
				return new IEnvironnementActions() {
					
					@Override
					public void pushResource(Position freePlacePost, Position robotPost) {
						eco_requires().prxActions().pushResource(freePlacePost, robotPost);
					}
					
					@Override
					public void pullResource(Position resrcPost, Position robotPost) {
						eco_requires().prxActions().pullResource(resrcPost, robotPost);
					}
					
					@Override
					public void moveRobot(Position oldPosition, Position newPosition) {
						eco_requires().prxActions().moveRobot(oldPosition, newPosition);
					}
					
					@Override
					public void moveLane(int laneId, int newHigh) {
						System.out.println("** WARNING **");
						System.out.println("Les robots n'ont pas le droit de bouger le couloir !");
						//eco_requires().prxActions().moveLane();
					}

					@Override
					public void addRobot(Position robotPost) {
						eco_requires().prxActions().addRobot(robotPost);
					}
				};
			}
		};
	}
	
}
