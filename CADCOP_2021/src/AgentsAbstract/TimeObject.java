package AgentsAbstract;

import java.util.Vector;

public class TimeObject {

	private long timeOfObject;
	private long idleTime;

	public TimeObject(long timeOfObject) {
		this.timeOfObject = timeOfObject;
		this.idleTime = 0;
	}

	public synchronized long getTimeOfObject() {
		return timeOfObject;
	}

	public synchronized void setTimeOfObject(long timeOfObject) {
		this.idleTime = this.idleTime+(timeOfObject-this.timeOfObject);
		this.timeOfObject = timeOfObject;
	}

	public synchronized void addToTime(long atomicActionCounter) {
		this.timeOfObject += atomicActionCounter;
	}
	
	public synchronized long getIdleTime() {
		return this.idleTime;
	}
	
	
	
}
