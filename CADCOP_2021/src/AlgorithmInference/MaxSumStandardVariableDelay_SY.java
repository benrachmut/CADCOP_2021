package AlgorithmInference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import AgentsAbstract.AgentVariable;
import Messages.MsgAlgorithm;
import Messages.MsgReceive;

public class MaxSumStandardVariableDelay_SY extends MaxSumStandardVariableDelay{
	
	private Collection<MsgAlgorithm> future;

	public MaxSumStandardVariableDelay_SY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		this.future = new ArrayList<MsgAlgorithm>();

	}
	
	@Override
	public void updateAlgorithmName() {
		if (this.damping == false) {
			AgentVariable.AlgorithmName = "Max Sum SY";
		}else {
			AgentVariable.AlgorithmName = "DMS SY";

		}
	}
	
	public void resetAgentGivenParametersV5() {
		this.future = new ArrayList<MsgAlgorithm>();
	}
	
	@Override
	public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		if (this.timeStampCounter == msgAlgorithm.getTimeStamp()) {
			super.updateMessageInContext(msgAlgorithm);
		} else {
			this.future.add(msgAlgorithm);
		}
		return true;
	}
	
	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
		
		for (MsgReceive<double[]> m : this.functionMsgs.values()) {
			int msgTimestamp = 0;
			if (m == null) {
				return;
			} else {
				msgTimestamp = m.getTimestamp();
			}
			if (msgTimestamp != this.timeStampCounter) {
				return;
			}
		}
		canCompute = true;
	}
	
	@Override
	public void sendMsgs() {
		super.sendMsgs();
		releaseFutureMsgs();

	}

	private void releaseFutureMsgs() {
		Collection<MsgAlgorithm> toRelease = new HashSet<MsgAlgorithm>();
		for (MsgAlgorithm m : this.future) {
			if (m.getTimeStamp() == this.timeStampCounter) {
				toRelease.add(m);
				updateMessageInContext(m);
			}
		}
		this.future.removeAll(toRelease);
	}

}
