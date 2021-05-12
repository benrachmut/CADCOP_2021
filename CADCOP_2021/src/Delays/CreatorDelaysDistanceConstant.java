package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysDistanceConstant extends CreatorDelaysMatrixDependenet{

	private double[] multipliers = {100,200};
	
	public CreatorDelaysDistanceConstant(double[][] agentsQuadraticDistance) {
		super(agentsQuadraticDistance);
	}
	
	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		return new ProtocolDelayDistanceConstant(gamma);
	}
	
	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean timestampBoolean, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		for (double multiplier : multipliers) {
			ans.add(new ProtocolDelayDistanceConstant(timestampBoolean, gamma, multiplier, this.matrix));
		} // sigma
		return ans;
	}

	@Override
	protected String header() {
		return "multiplier";
	}

	@Override
	public String name() {
		return "Distance Constant";
	}

}
