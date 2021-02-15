package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Delays.ProtocolDelay;

public class CreatorDelaysUniform extends CreatorDelays {

	private double[] UBs = {250,500,1000,1500,2000,3000};

	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		return new ProtocolDelayUniform(gamma);
	}

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean timestampBoolean, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		for (double ub : UBs) {
			ans.add(new ProtocolDelayUniform(timestampBoolean, gamma, ub));
		} // sigma
		return ans;
	}
	@Override

	protected String header() {
		return "UB";

	}

}
