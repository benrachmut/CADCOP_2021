package AgentsAbstract;

import java.util.Vector;

public class TimeObject {

	private int timeOfObject;

	public TimeObject(int timeOfObject) {
		super();
		this.timeOfObject = timeOfObject;
	}

	public synchronized int getTimeOfObject() {
		return timeOfObject;
	}

	public synchronized void setTimeOfObject(int timeOfObject) {
		this.timeOfObject = timeOfObject;
	}

	public synchronized void addToTime(int atomicActionCounter) {
		this.timeOfObject += atomicActionCounter;
		
	}
	
	
}
