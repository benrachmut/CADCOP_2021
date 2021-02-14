package Down;


public class ProtocolDownNone extends ProtocolDown{

	
	public ProtocolDownNone() {
		super();
	}
	
	@Override
	protected Integer getCounterToRealse() {
		return null;
	}

	@Override
	protected void setSeedSpecific(int seed) {
	}

	@Override
	protected String getStringParamets() {
		return "";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDown other) {
		if (other instanceof ProtocolDownNone) {
			return true;
		}
		return false;
	}

}
