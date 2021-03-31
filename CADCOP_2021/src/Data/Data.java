package Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import Main.Mailer;
import Main.MainSimulator;
import Problem.Dcop;
import Problem.Neighbor;

public class Data {
	private Long time;
	private Double globalCost;
	private Double globalAnytimeCost;
	private Double changeValueAssignmentCounter;
	// ------**measures from mailer**-----
	private Double algorithmMsgsCounter;
	// ------**measures from mailer: any time**-----
	private Double anytimeMsgsCounter;
	private Double monotonicy;
	private Double povCost;

	private Double globalPovABSDelta;
	private Double agentZeroGlobalCost;
	private Double agentZeroPOVCost;

	// ----**for AMDLS distributed**----
	private Double agentPercentCanStart;
	private Double numberOfColors;
	// ----**Anytime measures**----
	private Double topAgentsAnytimeContextCost;
	private Double anytimeCost;
	private Double topContextCounters;
	private Double numberOfRepsMeanAtTop;
	private Double numberOfRepsMeanAtAll;

	public Data(Entry<Long, List<Data>> e) {
		this.time = e.getKey();
		List<List<Double>> colletionPerFields = createColletionsPerField(e.getValue());
		this.globalCost = Statistics.mean(colletionPerFields.get(0));
		this.monotonicy = Statistics.mean(colletionPerFields.get(1));
		this.povCost = Statistics.mean(colletionPerFields.get(2));
		this.globalAnytimeCost = Statistics.mean(colletionPerFields.get(3));
		this.changeValueAssignmentCounter = Statistics.mean(colletionPerFields.get(4));
		this.algorithmMsgsCounter = Statistics.mean(colletionPerFields.get(5));
		this.agentZeroGlobalCost = Statistics.mean(colletionPerFields.get(6));
		this.agentZeroPOVCost = Statistics.mean(colletionPerFields.get(7));
		this.globalPovABSDelta = Statistics.mean(colletionPerFields.get(8));
		this.agentPercentCanStart = Statistics.mean(colletionPerFields.get(9));
		this.numberOfColors = Statistics.mean(colletionPerFields.get(10));
		if (MainSimulator.isAnytime) {
			this.topAgentsAnytimeContextCost = Statistics.mean(colletionPerFields.get(11));
			this.anytimeCost = Statistics.mean(colletionPerFields.get(12));
			this.topContextCounters = Statistics.mean(colletionPerFields.get(13));
			this.numberOfRepsMeanAtTop = Statistics.mean(colletionPerFields.get(14));
			this.numberOfRepsMeanAtAll = Statistics.mean(colletionPerFields.get(15));

		}
	}

	private List<List<Double>> createColletionsPerField(List<Data> datas) {
		List<List<Double>> ans = new ArrayList<List<Double>>();
		if (MainSimulator.isAnytime) {
			for (int i = 0; i < 12 + 5; i++) {
				ans.add(new ArrayList<Double>());
			}
		} else {
			for (int i = 0; i < 12; i++) {
				ans.add(new ArrayList<Double>());
			}
		}
		for (Data d : datas) {
			ans.get(0).add(d.globalCost);
			ans.get(1).add(d.monotonicy);
			ans.get(2).add(d.povCost);
			ans.get(3).add(d.globalAnytimeCost);
			ans.get(4).add(d.changeValueAssignmentCounter);
			ans.get(5).add(d.algorithmMsgsCounter);
			ans.get(6).add(d.agentZeroGlobalCost);
			ans.get(7).add(d.agentZeroPOVCost);
			ans.get(8).add(d.globalPovABSDelta);
			ans.get(9).add(d.agentPercentCanStart);
			ans.get(10).add(d.numberOfColors);

			if (MainSimulator.isAnytime) {
				ans.get(11).add(d.topAgentsAnytimeContextCost);
				ans.get(12).add(d.anytimeCost);
				ans.get(13).add(d.topContextCounters);

				if (d.topAgentsAnytimeContextCost == null) {
					ans.get(14).add(0.0);
				} else {
					ans.get(14).add(1.0);
				}

				if (d.anytimeCost == null) {
					ans.get(15).add(0.0);
				} else {
					ans.get(15).add(1.0);
				}
			}

		}
		return ans;
	}

	public Data(Long time, Dcop dcop, Mailer mailer) {
		this.time = time;
		this.globalCost = calcGlobalCost(dcop.getNeighbors());
		this.changeValueAssignmentCounter = calcChangeValueAssignmentCounter(dcop.getVariableAgents());
		this.algorithmMsgsCounter = mailer.getAlgorithmMsgsCounter();

		if (MainSimulator.isAnytime) {
			this.anytimeMsgsCounter = mailer.getAnytimeMsgsCounter();
		}
		this.monotonicy = calcMonotonicy(mailer, globalCost);

		if (MainSimulator.isAnytimeThreadDebug && time == 0) {
			System.out.println();
		}
		this.globalAnytimeCost = calcGlobalAnytimeCost(mailer);
		this.povCost = calcPovCost(dcop.getVariableAgents());
		this.globalPovABSDelta = calcGlobalPovABSDelta(this.povCost, this.globalCost);
		this.agentZeroGlobalCost = calcAgentZeroGlobalCost(0, dcop.getNeighbors());
		this.agentZeroPOVCost = calcAgentZeroPOVCost(0, dcop.getVariableAgents(0));
		this.agentPercentCanStart = calcAgentPercentCanStart(dcop.getVariableAgents());
		this.numberOfColors = calcNumberOfColors(dcop.getVariableAgents());
		if (MainSimulator.isAnytime) {
			if (mailer.getDcop().isSearchAlgorithm()) {

				this.topAgentsAnytimeContextCost = calcTopAgentsAnytimeContextCost(mailer);

				this.anytimeCost = calcAnytimeCost(dcop.getNeighbors());

				this.topContextCounters = calcTopContextCounters(mailer);
			}
		}
	}

	private static Double calcGlobalPovABSDelta(Double povCost2, Double globalCost2) {
		try {
			return Math.abs(povCost2 - globalCost2);
		} catch (NullPointerException e) {
			return null;
		}
	}

	private Double calcNumberOfColors(AgentVariable[] avs) {
		Set<Integer> colors = new HashSet<Integer>();
		for (AgentVariable av : avs) {
			if (av.getColorNumber() != null) {
				colors.add(av.getColorNumber());
			}
		}
		return (double) colors.size();
	}

	private Double calcAgentPercentCanStart(AgentVariable[] avs) {
		double numOfAgents = MainSimulator.A;
		double isColoredCounter = 0.0;
		for (AgentVariable av : avs) {
			isColoredCounter = isColoredCounter + av.getIfColor();
		}
		return isColoredCounter / numOfAgents;
	}

	private Double calcAnytimeCost(List<Neighbor> neighbors) {
		Integer ans = null;
		for (Neighbor n : neighbors) {
			// try {
			Integer costOfN = n.getCurrentAnytimeCost();
			if (costOfN != null) {
				if (ans == null) {
					ans = costOfN;
				} else {
					ans = ans + costOfN;

				}
			}
		} // for
		if (ans == null) {
			return null;
		}

		return (double) ans;
	}

	private Double calcTopContextCounters(Mailer mailer) {
		Collection<AgentVariableSearch> anytimeTopsAgents = getTopAgents(mailer.getDcop());
		Double ans = 0.0;
		for (AgentVariableSearch a : anytimeTopsAgents) {
			int CounterOfContext = a.getCounterOfContext();
			ans = ans + CounterOfContext;
		}
		return ans;
	}

	private Double calcTopAgentsAnytimeContextCost(Mailer mailer) {
		Collection<AgentVariableSearch> anytimeTopsAgents = getTopAgents(mailer.getDcop());
		Double ans = 0.0;
		for (AgentVariableSearch a : anytimeTopsAgents) {
			try {
				Double costOfContext = a.getCostOfBestContext();
				ans = ans + costOfContext;
			} catch (RuntimeException e) {
				return null;
			}

		}
		return ans / 2.0;
	}

	private Collection<AgentVariableSearch> getTopAgents(Dcop dcop) {
		AgentVariable[] avs = dcop.getVariableAgents();
		Collection<AgentVariableSearch> ans = new ArrayList<AgentVariableSearch>();
		for (AgentVariable a : avs) {
			AgentVariableSearch as = (AgentVariableSearch) a;
			if (as.isAnytimeTop()) {
				ans.add(as);
			}
		}
		return ans;
	}

	private Double calcAgentZeroPOVCost(int i, AgentVariable av) {
		if (av instanceof AgentVariableSearch) {
			return ((AgentVariableSearch) av).getCostPov();
		} else {
			return 0.0;
		}
	}

	private Double calcAgentZeroGlobalCost(int id1, List<Neighbor> neighbors) {
		double ans = 0;
		for (Neighbor n : neighbors) {
			if (n.getA1().getId() == id1 || n.getA2().getId() == id1) {
				try {
					ans += n.getCurrentCost();
				} catch (NullPointerException e) {
					return null;
				}
			}
		}
		return ans;
	}

	private static Double calcPovCost(AgentVariable[] variableAgents) {
		double ans = 0.0;
		for (AgentVariable a : variableAgents) {

			if (a instanceof AgentVariableSearch) {
				double aPOV = ((AgentVariableSearch) a).getCostPov();
				if (aPOV == -1) {
					return null;
				} else {
					ans += aPOV;
				}
			} else {
				return 0.0;
			}

		}
		return ans / 2.0;
	}

	private Double calcMonotonicy(Mailer mailer, Double globalCost2) {
		try {
			if (time == 0) {
				return 1.0;
			}
			Double lastGlobalCost = mailer.getLastGlobalCost();
			if (lastGlobalCost == null) {
				return 1.0;
			}
			if (lastGlobalCost >= globalCost2) {
				return 1.0;
			} else {
				return 0.0;
			}
		} catch (NullPointerException e) {
			return 1.0;
		}
	}

	private Double calcGlobalAnytimeCost(Mailer mailer) {
		if ((!MainSimulator.isThreadMailer && time == 0) || (MainSimulator.isThreadMailer && time == 1)) {
			return this.globalCost;
		}
		Double lastAnytimeGlobal = mailer.getLastGlobalAnytimeCost();
		
		if (MainSimulator.agentType==7 || MainSimulator.agentType==12) {
			return  0.0 ;
		}
		if (lastAnytimeGlobal == 0 || this.globalCost < lastAnytimeGlobal) {
			return this.globalCost;
		} else {
			return lastAnytimeGlobal;
		}

	}

	private static Double calcChangeValueAssignmentCounter(AgentVariable[] variableAgents) {
		Double ans = 0.0;
		for (AgentVariable a : variableAgents) {
			ans = +a.getChangeValueAssignmentCounter();
		}
		return ans;
	}

	private static Double calcGlobalCost(List<Neighbor> neighbors) {
		double ans = 0.0;
		for (Neighbor n : neighbors) {
			if (n.getCurrentCost() == null) {
				return null;
			}
			ans += n.getCurrentCost();
		}
		return ans;
	}

	public static String header() {
		String ans = "";

		ans = ans + "Iteration" + "," + "Global View Cost" + "," + "Monotonicy" + "," + "Agent View Cost" + ","
				+ "Global Anytime Cost" + "," + "Value Assignmnet Counter" + "," + "Algorithm Msgs Counter" + ","
				+ "Global View Cost Agent Zero" + "," + "Agent View Cost Agent Zero" + "," + "Abs Delta Global and POV"
				+ "," + "Percent Agents With Colors" + "," + "Number of Colors";
		if (MainSimulator.isAnytime) {
			ans = ans + "," + "Anytime top agents best context cost" + "," + "Anytime Cost" + ","
					+ "Contexts of all agents reported" + "," + "Number of Repetitions top" + ","
					+ "Number of Repetitions All";
		}
		return ans;
	}

	@Override
	public String toString() {

		String ans = this.time + "," + this.globalCost + "," + this.monotonicy + "," + this.povCost + ","
				+ this.globalAnytimeCost + "," + this.changeValueAssignmentCounter + "," + this.algorithmMsgsCounter
				+ "," + this.agentZeroGlobalCost + "," + this.agentZeroPOVCost + "," + this.globalPovABSDelta + ","
				+ agentPercentCanStart + "," + numberOfColors;

		if (MainSimulator.isAnytime) {
			ans = ans + "," + this.topAgentsAnytimeContextCost + "," + this.anytimeCost + "," + this.topContextCounters
					+ "," + this.numberOfRepsMeanAtTop + "," + this.numberOfRepsMeanAtAll;

		}

		return ans;
	}

	public Double getGlobalCost() {
		return this.globalCost;
	}

	public Double getGlobalAnytimeCost() {
		return this.globalAnytimeCost;
	}

}
