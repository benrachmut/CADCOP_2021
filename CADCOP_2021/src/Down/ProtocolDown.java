package Down;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import AgentsAbstract.Agent;
import Delays.ProtocolDelay;

public abstract class ProtocolDown {

	private boolean agentDownScenario;
	private double probPerMsgApproch;

	private Map<Agent, Boolean> isAgentDown;
	protected Map<Agent, Integer> counterToRealse;

	private Random rndToTakeDown;

	public ProtocolDown(double prob) {
		agentDownScenario = true;
		this.probPerMsgApproch = prob;

	}

	public ProtocolDown() {
		agentDownScenario = false;
		this.probPerMsgApproch = 0;

	}

	public void protocolDownMeetsAgents(Collection<Agent> agents) {
		this.isAgentDown = initIsDown(agents);
		this.counterToRealse = initCounterToRelease(agents);
	}

	private Map<Agent, Integer> initCounterToRelease(Collection<Agent> agents) {
		Map<Agent, Integer> ans = new HashMap<Agent, Integer>();
		for (Agent agent : agents) {
			ans.put(agent, 0);
		}
		return ans;
	}

	private static Map<Agent, Boolean> initIsDown(Collection<Agent> agents) {
		Map<Agent, Boolean> ans = new HashMap<Agent, Boolean>();
		for (Agent agent : agents) {
			ans.put(agent, false);
		}
		return ans;
	}

	public boolean isDown(Agent a) {
		boolean isAgentDown = this.isAgentDown.get(a);
		if (isAgentDown == true) {
			return agentIsCurrntlyDown(a);
		} else {
			double rnd = this.rndToTakeDown.nextDouble();
			if (rnd < this.probPerMsgApproch) {
				agentIsGoingDown(a);
				return true;
			}
		}
		return false;
	}

	private void agentIsGoingDown(Agent a) {
		Integer specificCounterToRelease = getCounterToRealse();
		this.counterToRealse.put(a, specificCounterToRelease);
	}

	private boolean agentIsCurrntlyDown(Agent a) {
		Integer counterToRelease = this.counterToRealse.get(a);
		if (counterToRelease == 0) {
			this.isAgentDown.put(a, false);
			return false;
		} else {
			Integer counterToReleaseUpdate = counterToRelease - 1;
			this.counterToRealse.put(a, counterToReleaseUpdate);
			return true;
		}

	}

	protected abstract Integer getCounterToRealse();

	public void setSeeds(int seed) {
		this.rndToTakeDown = new Random(seed);
		setSeedSpecific(seed);
	}

	protected abstract void setSeedSpecific(int seed);

	public String toString() {
		return this.agentDownScenario + "," + probPerMsgApproch + "," + getStringParamets();
	}

	protected abstract String getStringParamets();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProtocolDown) {
			ProtocolDown other = (ProtocolDown) obj;
			boolean sameAgentDownScenario = this.agentDownScenario == other.getAgentDownScenario();
			if (this.agentDownScenario == false && sameAgentDownScenario) {
				return true;
			}
			boolean sameProbPerMsgApproch = this.probPerMsgApproch == other.getProbPerMsgApproch();
			boolean sameOthers = checkSpecificEquals(other);

			return sameAgentDownScenario && sameProbPerMsgApproch && sameOthers ;
		}
		return false;
	}

	protected abstract boolean checkSpecificEquals(ProtocolDown other);

	private double getProbPerMsgApproch() {
		return this.probPerMsgApproch;
	}

	private boolean getAgentDownScenario() {
		// TODO Auto-generated method stub
		return this.agentDownScenario;
	}
}
