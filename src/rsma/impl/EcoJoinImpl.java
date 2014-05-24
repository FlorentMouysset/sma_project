package rsma.impl;

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
					public WORDL_ENTITY getWordEntityAt(Position position) {
						System.out.println("Join LookAt " + position);
						return eco_requires().prxLookAtPort().getWordEntityAt(position);
					}
				};
			}
			
			@Override
			protected IEnvironnementActions make_joinActions() {
				return new IEnvironnementActions() {
					
					@Override
					public void pushResource(Position position) {
						eco_requires().prxActions().pushResource(position);
					}
					
					@Override
					public void pullResource(Position position) {
						eco_requires().prxActions().pullResource(position);
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
				};
			}
		};
	}
	
}
