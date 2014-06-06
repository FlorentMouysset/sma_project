package rsma.interfaces;

import java.util.Observer;

public interface IEnvironnementObservable {

	/**
	 * Add an observer of environment. The Observer is notify by push mode (see {@link Observer} and java.util.Observer.update(java.util.Observable, java.lang.Object) with Object is an WarehouseChangement).</br> 
	 * 
	 * This call update the Observer with all the world just for the registration. After just the change are push.
	 * */
	void registerObserver(Observer observer);

}
