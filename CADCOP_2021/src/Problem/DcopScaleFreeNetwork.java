package Problem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import AgentsAbstract.AgentVariable;

public class DcopScaleFreeNetwork extends Dcop {

	private int hubs;
	private int neighborsPerAgent;
	private double p2;
	private int costLb, costUb;
	private Random randomHub, randomNotHub, randomP2;

	public DcopScaleFreeNetwork(int dcopId,int A, int D, int costLb,int costUb, int hubs, int neighborsPerAgent, double p2) {
		super(dcopId, A, D);
		this.hubs = hubs;
		this.neighborsPerAgent = neighborsPerAgent;
		this.p2 = p2;
		randomHub = new Random(this.dcopId * 10);
		randomNotHub = new Random(this.dcopId * 20);
		randomP2 = new Random(this.dcopId * 30);
		this.costUb = costUb;
		this.costLb = costLb;

		updateNames();
	}

	
	@Override
	protected void setDcopName() {
		Dcop.dcopName = "Scale-Free Network";
		
	}

	@Override
	protected void setDcopHeader() {
		Dcop.dcopHeader = "Hubs,Neighbors Per Agents,p2,D";
		
	}

	@Override
	protected void setDcopParameters() {
		Dcop.dcopParameters = hubs+","+ neighborsPerAgent+","+ p2+","+ D;
		
	}

	
	
	

	@Override
	public void createNeighbors() {
		Map<Integer, Boolean> marked = initColored();
		createHubs(marked);
		findNeighborsToOtherAgentsWhoAreNotHubs(marked);
	}

	private Map<Integer, Boolean> initColored() {
		Map<Integer, Boolean> ans = new HashMap<Integer, Boolean>();
		for (AgentVariable a : agentsVariables) {
			ans.put(a.getId(), false);
		}
		return ans;
	}

	private void findNeighborsToOtherAgentsWhoAreNotHubs(Map<Integer, Boolean> marked) {
		for (Entry<Integer, Boolean> e : marked.entrySet()) {
			Integer id = e.getKey();
			Boolean isMarked = e.getValue();
			if (!isMarked) {
				AgentVariable af = getAgentField(id);
				findNeighborsToSingleAgentNotHub(af);
				marked.put(af.getId(), true);
			}
		}
		checkIfMarkedMakeSense(marked);
	}

	private void checkIfMarkedMakeSense(Map<Integer, Boolean> marked) {
		for (Boolean b : marked.values()) {
			if (!b) {
				System.err.println("something is wrong with iterating over all the unmarked ");
			}
		}

	}
	private void findNeighborsToSingleAgentNotHub(AgentVariable af) {
		List<Integer> idsOfANeighbor = selectNToNotHubs(af);
		if (idsOfANeighbor.size() != this.neighborsPerAgent) {
			System.err.println("something in selectNToNotHubs in dcop is wrong");
		}
		declareNeighborsOfIteratedAgentField(idsOfANeighbor, af);
	}

	private void declareNeighborsOfIteratedAgentField(List<Integer> idsOfANeighbor, AgentVariable af) {
		for (Integer id : idsOfANeighbor) {
			AgentVariable afNeighbor = getAgentField(id);

			AgentVariable a1 = null;
			AgentVariable a2 = null;

			if (id <= af.getId()) {
				a1 = afNeighbor;
				a2 = af;
			} else {
				a1 = af;
				a2 = afNeighbor;
			}
			 this.neighbors.add(new Neighbor(a1, a2, D, costLb,costUb, this.dcopId,p2));
		}

	}

	private AgentVariable getAgentField(Integer id) {
		for (AgentVariable a : agentsVariables) {
			if (a.getId() == id) {
				return a;
			}
		}
		return null;
	}

	private List<Integer> selectNToNotHubs(AgentVariable af) {
		List<Integer> ans = new ArrayList<Integer>();
		Map<Integer, Boolean> markedHere = initColored();
		markedHere.put(af.getId(), true);
		Map<Integer, Double> probs = initProbs(af);

		int counter = 0;
		while (counter < this.neighborsPerAgent) {
			int idOfNeighborShuffled = getFromProbsShuffledNeighbor(af, probs);
			if (idOfNeighborShuffled == -1) {
				System.err.println("logical bug in creating prob map");
			}
			if (!markedHere.get(idOfNeighborShuffled)) {
				counter++;
				markedHere.put(idOfNeighborShuffled, true);
				ans.add(idOfNeighborShuffled);
			}
		}
		return ans;

	}
	
	private int getFromProbsShuffledNeighbor(AgentVariable af, Map<Integer, Double> probs) {
		double rnd = randomNotHub.nextDouble();
		for (int i = 0; i < agentsVariables.length; i++) {
			if (rnd < probs.get(i)) {
				return i;
			}
		}

		return -1;
	}

	private Map<Integer, Double> initProbs(AgentVariable af) {
		Map<Integer, Double> ans = new TreeMap<Integer, Double>();

		double sigma = 0;

		for (AgentVariable a : agentsVariables) {
			sigma += a.neighborSize();
		}

		for (int i = 0; i < agentsVariables.length; i++) {
			AgentVariable a = agentsVariables[i];
			int id = a.getId();
			double aProb = a.neighborSize() / sigma;
			if (i == 0) {
				ans.put(id, aProb);
			} else {
				double probAbove = aProb + ans.get(i - 1);
				ans.put(id, probAbove);
			}

		}

		return ans;
	}

	private void createHubs(Map<Integer, Boolean> marked) {
		List<AgentVariable> hubs = getRandomHubs();
		hubNeighborsToOneAnother(hubs);
		for (AgentVariable a : hubs) {
			marked.put(a.getId(), true);
		}

	}

	private void hubNeighborsToOneAnother(List<AgentVariable> hubs) {
		List<Neighbor> ans = new ArrayList<Neighbor>();
		for (int i = 0; i < hubs.size(); i++) {
			for (int j = i + 1; j < hubs.size(); j++) {
				// informDcopAndAgentsUponNeighborhood(hubs, i, j);
				createNeighborsCorrectly(hubs, i, j);

			}
		}

	}

	private void createNeighborsCorrectly(List<AgentVariable> hubs, int i, int j) {
		AgentVariable first = hubs.get(i);
		AgentVariable second = hubs.get(j);
		
		int idFirst = first.getId();
		int idSecond = second.getId();
		
		AgentVariable a1 = null;
		AgentVariable a2 = null;

		if (idFirst<=idSecond) {
			a1 = first;
			a2 = second;
		}else {
			a1 = second ;
			a2 = first;
		}
		

		this.neighbors.add(new Neighbor(a1, a2, D, costLb,costUb, this.dcopId, p2));
		
	}

	public List<AgentVariable> getRandomHubs() {
		// create a temporary list for storing
		// selected element
		List<AgentVariable> list = turnAgentArrayToArrayList();
		List<AgentVariable> newList = new ArrayList<AgentVariable>();
		for (int i = 0; i < this.hubs; i++) {
			// take a random index between 0 to size of given List
			int randomIndex = randomHub.nextInt(list.size());
			// add element in temporary list
			newList.add(list.get(randomIndex));
			// Remove selected element from orginal list
			list.remove(randomIndex);
		}
		return newList;
	}

	private List<AgentVariable> turnAgentArrayToArrayList() {
		List<AgentVariable> ans = new ArrayList<AgentVariable>();
		for (AgentVariable a : agentsVariables) {
			ans.add(a);
		}

		return ans;
	}


}
