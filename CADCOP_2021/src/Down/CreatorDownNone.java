package Down;

import java.util.Collection;

import Delays.ProtocolDelay;
import Delays.ProtocolDelayNone;

public class CreatorDownNone  extends CreatorDown {

	@Override
	protected ProtocolDown createDefultProtocol(){
		// TODO Auto-generated method stub
		return new ProtocolDownNone();
	}

	@Override
	protected String header() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	protected Collection<? extends ProtocolDown> createCombinationsDown(double prob) {
		// TODO Auto-generated method stub
		return null;
	}


}
