package Delays;

import java.util.Random;

public class ProtocolDelaysPossion extends ProtocolDelay {

	private double lambda;
	private Random rndLambdaAlgo, rndLambdaAnytime;

	public ProtocolDelaysPossion(boolean isTimeStamp, double gamma, double lambda) {
		super(true, isTimeStamp, gamma);
		this.lambda = lambda;
	}

	public ProtocolDelaysPossion() {
		super(false, true, 0.0);
		this.lambda = 0;
	}

	public ProtocolDelaysPossion(double gamma) {
		super(false, true, gamma);
		this.lambda = 0;
	}

	@Override
	protected Double createDelayGivenParameters(boolean isAlgoMsg) {

		if (this.lambda == 0) {
			return 0.0;
		}

		Random whichRand;
		if (isAlgoMsg) {
			whichRand = rndLambdaAlgo;
		} else {
			whichRand = rndLambdaAnytime;
		}

		double ans = getRandomPoission(whichRand, this.lambda);
		if (ans < 0) {
			return 0.0;
		} else {
			return ans;
		}
	}

	public static double getRandomPoission(Random r, double lambda) {
		double L = Math.exp(-lambda);
		int k = 0;
		double p = 1.0;
		do {
			k++;
			p = p * r.nextDouble();
		} while (p > L);

		return k - 1;
	}
	
	
	
}
