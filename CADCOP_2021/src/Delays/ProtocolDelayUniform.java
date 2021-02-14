package Delays;

import java.util.Random;

public class ProtocolDelayUniform extends ProtocolDelay {

	private double ub;
	private Random rndAlgoUniform, rndAnytimeUniform;
	
	public ProtocolDelayUniform( boolean isTimeStamp, double gamma, double ub) {
		super(true, isTimeStamp,gamma);

		this.ub = ub;
		
	}


	
	public ProtocolDelayUniform(double gamma) {
		super(false, true,gamma);
		ub = 0;
	}
	@Override
	protected Double createDelayGivenParameters(boolean isAlgoMsg) {
		Random whichRandom;
		if (isAlgoMsg) {
			whichRandom = rndAlgoUniform;
		}else {
			whichRandom = rndAnytimeUniform;
		}
		
		return whichRandom.nextDouble()*ub;
	}

	@Override
	protected void setSeedsGivenParameters(int dcopId) {
		rndAlgoUniform = new Random(dcopId);
		rndAnytimeUniform = new Random(dcopId*142);
		
	}
	
	protected String getStringParamets() {
		// TODO Auto-generated method stub
		return this.ub+"";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		if (other instanceof ProtocolDelayUniform) {
			ProtocolDelayUniform otherUniform = (ProtocolDelayUniform)other;
			boolean sameUB = otherUniform.getUb() == this.ub;
			return sameUB;
		}
		return false;
	}



	public double getUb() {
		// TODO Auto-generated method stub
		return this.ub;
	}


	

}
