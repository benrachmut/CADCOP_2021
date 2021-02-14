package Delays;

import java.util.Collection;

public class CreatorDelaysPossion extends CreatorDelays {
	private double[] lambdas = {100,250,500,1000,1500,2000};
	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String header() {
		// TODO Auto-generated method stub
		return null;
	}

}
