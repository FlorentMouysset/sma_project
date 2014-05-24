package rsma.impl;

import rsma.Robots.Robot;
import rsma.interfaces.IEnvironnementAnalysis.WORDL_ENTITY;
import rsma.interfaces.IRobotActions;
import rsma.util.Position;

public class RobotImpl extends Robot{
	private final String id;
	private Position currentPosition;
	
	public RobotImpl(String id, Position positionInit){
		this.id = id;
		this.currentPosition = positionInit;
	}
	
	@Override
	protected IRobotActions make_roboActionPort() {
		return new IRobotActions() {
			
			@Override
			public void doCycle() {
				// TODO Auto-generated method stub
				// Pour damien ...
				
				//exemple d'utilisation
				System.out.println("Le robot "+ id +" fait un cyle.\nIl fait un appel sur l'environnement");
				WORDL_ENTITY we = eco_requires().pEnvLookAt().getWordEntityAt(new Position(0, 0));
				
				System.out.println("Le robot "+ id +" va bouger");
				eco_requires().pEnvAction().moveRobot(new Position(0, 0), new Position(1, 1));
				
				System.out.println("Le robot a regardé l'environnement a la position 0 0 il y a " + we + " terminé \n");
			}
		};
	}
}
