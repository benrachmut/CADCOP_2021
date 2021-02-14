package Down;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDownConstant extends CreatorDown {

	private Integer[] CounterNonRecieveMsg = {Integer.MAX_VALUE};
	
	@Override
	protected String header() {
		return "";
	}

	@Override
	protected Collection<? extends ProtocolDown> createCombinationsDown(double prob) {
		List<ProtocolDown>ans = new ArrayList<ProtocolDown>();
		for (Integer counter : CounterNonRecieveMsg) {
			ans.add(new ProtocolDownConstant(prob,counter));
		}
		return ans;
		
	}

	@Override
	protected ProtocolDown createDefultProtocol() {
		// TODO Auto-generated method stub
		return new ProtocolDownConstant();
	}

}
