package Delays;

import java.util.Random;

public class ProtocolDelayDistanceConstant extends ProtocolDelayMatrix {

	private double multiplier;

	public ProtocolDelayDistanceConstant(double gamma) {
		super(false, true, gamma);
		multiplier = 0;
	}

	public ProtocolDelayDistanceConstant(boolean isTimeStamp, double gamma, double multiplier) {
		super(true, isTimeStamp, gamma);
		this.multiplier = multiplier;
	}

	@Override
	protected Double createDelay(Random r, int id1,int id2) {
		return this.matrix[id1][id2]*multiplier;
	}

	

	@Override
	protected String getStringParamets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
}
