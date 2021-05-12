package AgentsAbstract;

import java.util.Random;

public class LocationRandomUniform extends Location {

	
	public LocationRandomUniform(int dcop_id) {
		super(dcop_id);
		generateRandomXY();
	}

	@Override
	protected void generateRandomXY() {
		Random r = new Random(this.dcopId*1717);
		r.nextDouble();
		this.x=r.nextDouble();
		this.y=r.nextDouble();
	}
 
	
}
