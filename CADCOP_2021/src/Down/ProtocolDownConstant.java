package Down;

import java.util.Collection;

import AgentsAbstract.Agent;

public class ProtocolDownConstant extends ProtocolDown {

	private Integer constantCounter;

	

	public ProtocolDownConstant(double prob, Integer counter) {
		super(prob);
		this.constantCounter = counter;
	}
	
	public ProtocolDownConstant() {
		super();
		constantCounter = 0;
	}

	@Override
	protected Integer getCounterToRealse() {
		// TODO Auto-generated method stub
		return this.constantCounter;
	}

	@Override
	protected void setSeedSpecific(int seed) {
		// no need
		
	}

	@Override
	protected String getStringParamets() {
		return this.constantCounter+",";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDown other) {
		if (other instanceof ProtocolDownConstant) {
			return ((ProtocolDownConstant)other).getConstanst() == this.constantCounter;
		}
		return false;
	}

	private Integer getConstanst() {
		return this.constantCounter;
		
	}

}
