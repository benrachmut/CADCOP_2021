package Delays;

import java.util.Random;

abstract public class ProtocolDelayMatrix extends ProtocolDelay {

	protected double[][] matrix;

	public ProtocolDelayMatrix(boolean imperfectCommunicationScenario, boolean isTimeStamp, double gamma,
			double[][] matrix) {
		super(imperfectCommunicationScenario, isTimeStamp, gamma);
		this.matrix = matrix;
	}

	public ProtocolDelayMatrix(boolean imperfectCommunicationScenario, boolean isTimeStamp, double gamma) {
		super(imperfectCommunicationScenario, isTimeStamp, gamma);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Double createDelay(Random r) {
		throw new RuntimeException("should use createDelay with "
				+ "matrix which means that in the input should reiceve indexes for 2d matrix");
	}

	abstract protected Double createDelay(Random r, int i, int j);

	public Double createDelay(boolean isAlgorithmicMsg, int id1, int id2) {
		Random whichRandom;
		if (isAlgorithmicMsg) {
			whichRandom = rndGammaAlgorthmic;
		} else {
			whichRandom = rndGammaAnytime;
		}
		double rnd = whichRandom.nextDouble();
		if (rnd < gamma) {
			return null;
		} else {

			Random whichRandomDelay;
			if (isAlgorithmicMsg) {
				whichRandomDelay = rndDelayAlgorthmic;
			} else {
				whichRandomDelay = rndDelayAnytime;
			}

			return createDelay(whichRandomDelay, id1, id2);
		}
	}

}