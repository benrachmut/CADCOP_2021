package Delays;

import java.util.Random;

public class ProtocolDelayNone extends ProtocolDelay{

	public ProtocolDelayNone(double gamma) {
		super(false, false, gamma);
	}

	

	@Override
	protected void setSeedsGivenParameters(int dcopId) {
		rndGammaAlgorthmic=new Random(dcopId);
	}

	@Override
	protected Double createDelayGivenParameters(boolean blah) {
		// TODO Auto-generated method stub
		return 0.0;
	}



	@Override
	protected String getStringParamets() {
		// TODO Auto-generated method stub
		return "";
	}



	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		if (other instanceof ProtocolDelayNone) {
			return true;
		}
		return false;
	}




}
