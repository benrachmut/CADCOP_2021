package AgentsAbstract;

import java.util.Random;

abstract public class Location {
	protected double x;
	protected double y;
	protected int dcopId;
	
	/**
	 * the input is used to initiate a random location for a given agent 
	 * @param agent_id
	 * @param dcop_id
	 */
	public Location(int dcop_id) {
		super();
		this.dcopId = dcop_id;
	}
	
	@Override
	public String toString() {
		return x+","+y;
	}

	protected abstract void generateRandomXY();

	public double getQuadraticDistanceTo(Location otherLocation) {
		double deltaX = this.x-otherLocation.x;
		double deltaY = this.y-otherLocation.y;
		
		double aSquare = Math.pow(deltaX, 2);
		double bSquare = Math.pow(deltaY, 2);
		double cSquare = aSquare+bSquare;
		
		return Math.sqrt(cSquare);
	}
	
	
	
	

}