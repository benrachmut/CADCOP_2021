package Delays;

import java.util.Collection;

public class CreatorDelaysNone extends CreatorDelays {

	

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String header() {
		return "";
	}

	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		return new ProtocolDelayNone(gamma);

	}
	@Override
	public String name() {
		return "None";
	}
}
