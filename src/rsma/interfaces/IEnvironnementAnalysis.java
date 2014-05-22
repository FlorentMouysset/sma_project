package rsma.interfaces;

import rsma.impl.EnvironnementImpl.WORDL_ENTITY;
import rsma.util.Position;

public interface IEnvironnementAnalysis {

	WORDL_ENTITY getWordEntityAt(Position position);
	
}
