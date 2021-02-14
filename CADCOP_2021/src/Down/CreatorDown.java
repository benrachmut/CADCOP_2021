package Down;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Delays.ProtocolDelay;

public abstract class CreatorDown {

	protected boolean[] agentDownScenarios = { false };
	protected double[] probPerMsgApproch = { 0 };

	public  List<ProtocolDown> createProtocolDowns() {
		List<ProtocolDown> ans = new ArrayList<ProtocolDown>();
		for (boolean agentDownScenario : agentDownScenarios) {
			if (agentDownScenario == false) {
				ans.add(createDefultProtocol());
			}
			else {
				for (double prob : probPerMsgApproch) {
					
					Collection<? extends ProtocolDown> toAdd = createCombinationsDown(prob);
					if (toAdd!=null) {
						ans.addAll(toAdd);
					}	
					
				}
			}
		}
		return ans;
	}
	
	public String getHeader() {
		return "";
	}


	protected abstract String header();
		
	protected abstract Collection<? extends ProtocolDown> createCombinationsDown(double prob);

	protected abstract ProtocolDown createDefultProtocol();
}