package AgentsAbstract;

import java.util.Random;

import Main.MainSimulator;

public class LocationRandomNormal extends Location {
	
	private int agentId;
	private double sd;
	private Location cityLocation; 

	public LocationRandomNormal(int dcop_id, int agentId,Location cityLocation) {
		this(dcop_id,agentId,cityLocation, MainSimulator.sdDistanceFromCity);
	
		
	}
	public LocationRandomNormal(int dcop_id, int agentId,Location cityLocation,double sd) {
		super(dcop_id);
		this.agentId = agentId;
		this.sd = MainSimulator.sdDistanceFromCity;
		this.cityLocation = cityLocation;
		generateRandomXY();
		
	}

	@Override
	protected void generateRandomXY() {
		Random r = new Random(117*dcopId+217*agentId);
		double xMu = cityLocation.x;
		double yMu = cityLocation.y;
		r.nextGaussian();
		this.x = r.nextGaussian() * Math.sqrt(this.sd) + xMu;
		this.y = r.nextGaussian() * Math.sqrt(this.sd) + yMu;
	}
	

}
