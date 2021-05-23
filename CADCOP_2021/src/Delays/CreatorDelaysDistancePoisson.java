package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysDistancePoisson extends CreatorDelaysMatrixDependenet{
	private double[] multipliers= {10000};

 
	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		 return new ProtocolDelaysDistancePoisson(gamma);
	}

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		for (double m : multipliers) {
			ans.add(new ProtocolDelaysDistancePoisson(isTimeStamp, gamma, m));
		} // sigma
		return ans;
	}

	@Override
	protected String header() {
		return "multiplier";
	}

	@Override
	public String name() {
		return "Distance Poisson";
	}

}
