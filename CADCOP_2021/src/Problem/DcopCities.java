package Problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.management.RuntimeErrorException;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.Location;
import AgentsAbstract.LocationRandomUniform;
import AgentsAbstract.NodeId;
import Main.MainSimulator;

public class DcopCities extends Dcop {
	private Map<AgentVariable, List<AgentVariable>> citiesAllocation;// agent of represents center, other agents close
																		// to
																		// the city
	protected double[][] agentsQuadraticDistance;
	private List<AgentVariable> agentsWithoutLocations;

	private int minCost, maxCost;
	private int numberOfCities;
	private double sdSquareFromCity;
	private double exponentForNeighborCitizens;
	// private Double[][] largePforNeighbors;
	// private Double[][] smallPforNeighbors;
	// private Boolean[][] isNeigbors;

	private double dcopCityP2;
	private int neighborsOfNonMayers;
	Random randomNotHub;

	public DcopCities(int dcopId, int A, int D, int numberOfCities, double sdSquareFromCity, int minCost, int maxCost,
			double dcopCityP2, int neighborsOfNonMayers, double exponentForNeighborCitizens) {
		super(dcopId, A, D);
		this.exponentForNeighborCitizens = exponentForNeighborCitizens;
		this.neighborsOfNonMayers = neighborsOfNonMayers;
		this.dcopCityP2 = dcopCityP2;
		this.minCost = minCost;
		this.maxCost = maxCost;
		randomNotHub = new Random(dcopId * 174);
		randomNotHub.nextDouble();
		this.numberOfCities = numberOfCities;
		this.sdSquareFromCity = sdSquareFromCity;
		agentsWithoutLocations = new ArrayList<AgentVariable>();
		for (AgentVariable a : agentsVariables) {
			agentsWithoutLocations.add(a);
		}
		agentsQuadraticDistance = new double[A][A];

		List<Location> citiesCenterLocations = createLocationToCities();
		List<AgentVariable> agentMayers = selectAgentsToBeCenter();

		citiesAllocation = setMayersLocationAndInitiateCityMap(citiesCenterLocations, agentMayers);
		allocateAgentsToCities();
		giveCitizensLocations();
		checkIfCityAllocationIsValid(citiesCenterLocations, agentMayers);
		calculateQuadarticDistance();

		if (MainSimulator.isDcopCityDebug) {
			// printLocations();
			// printLocationsForExcelCheck();
			// printDistanceMatrix();
			System.out.println();
		}
		/*
		 * this.largePforNeighbors = new Double[A][A]; this.smallPforNeighbors = new
		 * Double[A][A]; this.isNeigbors = new Boolean[A][A];
		 */
	}

	private void printDistanceMatrix() {
		for (int i = 0; i < agentsQuadraticDistance.length; i++) {
			for (int j = 0; j < agentsQuadraticDistance[i].length; j++) {
				System.out.print(agentsQuadraticDistance[i][j] + ",");
			}
			System.out.println();
		}
	}

	private void printLocationsForExcelCheck() {

		for (int i = 0; i < agentsVariables.length; i++) {
			AgentVariable a = agentsVariables[i];
			System.out.println(a.getLocation());
		}

		for (AgentVariable a : this.agentsVariables) {
			System.out.print(a.getLocation() + ",");
		}
		System.out.println();
	}

	private void calculateQuadarticDistance() {
		try {
			for (int i = 0; i < agentsVariables.length; i++) {
				for (int j = 0; j < agentsVariables.length; j++) {
					agentsQuadraticDistance[i][j] = agentsVariables[i]
							.getQuadraticDistanceTo(agentsVariables[j].getLocation());
				}
			}
		} catch (NullPointerException e) {
			System.err.println("There is not location to the agents");
		}
	}

	private void printLocations() {
		System.out.println("dcop id: " + this.dcopId);
		System.out.println("------Mayer locations------");
		for (AgentVariable a : citiesAllocation.keySet()) {
			System.out.println(a.getId() + "," + a.getLocation());
		}
		System.out.println("------Mayer locations------");
		for (List<AgentVariable> aList : citiesAllocation.values()) {
			for (AgentVariable a : aList) {
				System.out.println(a.getId() + "," + a.getLocation());
			}

		}

	}

	private void checkIfCityAllocationIsValid(List<Location> citiesCenterLocations, List<AgentVariable> agentMayers) {
		if (!agentsWithoutLocations.isEmpty()) {
			throw new RuntimeException("not all variable agents have location");
		}
		if (citiesCenterLocations.size() != agentMayers.size()) {
			throw new RuntimeException("should be citiesCenterLocations.size()==agentMayers.size()");
		}
	}

	private void giveCitizensLocations() {
		for (Entry<AgentVariable, List<AgentVariable>> e : citiesAllocation.entrySet()) {
			Location mayerLocation = e.getKey().getLocation();
			List<AgentVariable> citizens = e.getValue();
			for (AgentVariable citizen : citizens) {
				citizen.setLocationCloseToCity(mayerLocation, this.dcopId, sdSquareFromCity);
				agentsWithoutLocations.remove(citizen);
			}
		}

	}

	private void allocateAgentsToCities() {
		List<AgentVariable> keysAsArray = new ArrayList<AgentVariable>(citiesAllocation.keySet());
		for (AgentVariable a : agentsWithoutLocations) {
			Random r = new Random((dcopId + 1) * 125 + (a.getId() + 1) * 17);
			r.nextInt();
			List<AgentVariable> cityCitizens = citiesAllocation.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
			cityCitizens.add(a);
		}

	}

	private Map<AgentVariable, List<AgentVariable>> setMayersLocationAndInitiateCityMap(
			List<Location> citiesCenterLocations, List<AgentVariable> agentMayers) {
		Map<AgentVariable, List<AgentVariable>> ans = new HashMap<AgentVariable, List<AgentVariable>>();
		for (int i = 0; i < citiesCenterLocations.size(); i++) {
			AgentVariable mayer = agentMayers.get(i);
			mayer.setLocation(citiesCenterLocations.get(i));
			ans.put(agentMayers.get(i), new ArrayList<AgentVariable>());
		}
		return ans;
	}

	public List<AgentVariable> selectAgentsToBeCenter() {
		Random rand = new Random((this.dcopId + 1) * 175);

		// create a temporary list for storing
		// selected element
		List<AgentVariable> agentMayers = new ArrayList<AgentVariable>();
		for (int i = 0; i < this.numberOfCities; i++) {
			// take a random index between 0 to size
			// of given List
			int randomIndex = rand.nextInt(agentsWithoutLocations.size());
			// add element in temporary list
			agentMayers.add(agentsWithoutLocations.get(randomIndex));
			// Remove selected element from orginal list
			agentsWithoutLocations.remove(randomIndex);
		}
		return agentMayers;

	}

	private List<Location> createLocationToCities() {
		List<Location> ans = new ArrayList<Location>();
		for (int i = 0; i < MainSimulator.numberOfCities; i++) {
			ans.add(new LocationRandomUniform(dcopId, i));
		}
		return ans;
	}

	@Override
	protected void setDcopName() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setDcopHeader() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setDcopParameters() {
		// TODO Auto-generated method stub

	}

	@Override
	public void createNeighbors() {

		//mayersNeighborsWithOneAnother();
		otherCitizensNeighborsSelection();

		if (MainSimulator.isDcopCityDebug) { // printLocations(); //
			//printLocationsForExcelCheck(); // printDistanceMatrix();
			//printLargePforNeighbors(); // printSmallPforNeighbors();
			printNeighborsCoordinates();
			double sum = 0; 
			for (Neighbor n : this.neighbors) {
				sum = sum+ n.distanceBetween();
			}
			
			
			System.out.println(sum/this.neighbors.size());
			System.out.println();
		}

	}

	

	private void otherCitizensNeighborsSelection() {

		List<AgentVariable> allCitizens = new ArrayList<AgentVariable>();

		for (List<AgentVariable> citizensPerCity : this.citiesAllocation.values()) {
			allCitizens.addAll(citizensPerCity);
		}
		for (AgentVariable a1 : allCitizens) {
			
			
			int counter = 0;
			while (counter < this.neighborsOfNonMayers && a1.getNeigborSetId().size() < this.neighborsOfNonMayers) {
				//System.out.println(a1.getNeigborSetId());
				Map<AgentVariable, Double> probsPerAgent = getProbsPerAgent(a1, allCitizens);
				Map<AgentVariable, Double> cumulativeProb = getCumulativeProb(probsPerAgent);
				randomNotHub.nextDouble();
				randomNotHub.nextDouble();
				randomNotHub.nextDouble();
				double d = randomNotHub.nextDouble();
				for (Entry<AgentVariable, Double> e : cumulativeProb.entrySet()) {

					if (d < e.getValue()) {
						AgentVariable a2 = e.getKey();
						this.neighbors.add(new Neighbor(a1, a2, D, minCost, maxCost, dcopId, dcopCityP2));
						counter = counter + 1;
						break;
					}
				}
			}
		}
	}

	private Map<AgentVariable, Double> getCumulativeProb(Map<AgentVariable, Double> probsPerAgent) {
		probsPerAgent = DcopScaleFreeNetwork.sortByValue(probsPerAgent);
		Map<AgentVariable, Double> ans = new HashMap<AgentVariable, Double>();
		/*
		 * double sumOfAllNeighbors = getSomeOfAllNeighborsOfMarked(allMarked); double
		 * sum = 0; for (AgentVariable a : allMarked) { sum += a.neighborSize();
		 * ans.put(a, sum / sumOfAllNeighbors); } return ans;
		 * 
		 */

		double sum = getSomeOfSet(probsPerAgent.values());
		double cumelative = 0;
		for (Entry<AgentVariable, Double> e : probsPerAgent.entrySet()) {
			AgentVariable n = e.getKey();
			double currentD = e.getValue();
			cumelative += currentD;
			ans.put(n, cumelative / sum);
		}
		ans = DcopScaleFreeNetwork.sortByValue(ans);

		return ans;
	}

	private double getSomeOfSet(Collection<Double> values) {
		double sum = 0;
		for (Double d : values) {
			sum += d;
		}
		return sum;
	}

	private Map<AgentVariable, Double> getProbsPerAgent(AgentVariable a1, List<AgentVariable> allCitizents) {
		Map<AgentVariable, Double> ans = new HashMap<AgentVariable, Double>();
		List<AgentVariable> allExceptAAndNeigbors = getAllExceptAAndNeigbors(a1, allCitizents);

		double maxDistance = getMaxDistanceOfA(a1);

		for (AgentVariable a2 : allExceptAAndNeigbors) {

			double distanceToA1 = a1.getQuadraticDistanceTo(a2.getLocation());
			double p = Math.pow(1 - (distanceToA1 / maxDistance), this.exponentForNeighborCitizens);

			ans.put(a2, p);
			// System.out.print(p + ",");

		}

		return ans;
	}

	private List<AgentVariable> getAllExceptAAndNeigbors(AgentVariable a1, List<AgentVariable> allCitizents) {
		List<AgentVariable> ans = new ArrayList<AgentVariable>(allCitizents);
		ans.remove(a1);

		for (NodeId nd : a1.getNeigborSetId()) {
			AgentVariable neighbor = getAgentGivenNodeId(nd);
			ans.remove(neighbor);
		}
		return ans;
	}

	private AgentVariable getAgentGivenNodeId(NodeId nd) {
		for (AgentVariable a : this.agentsVariables) {
			if (a.getNodeId().equals(nd)) {
				return a;
			}
		}
		return null;
	}

	private double getMaxDistanceOfA(AgentVariable a1) {
		double[] distanceToAll = agentsQuadraticDistance[a1.getId()];
		Collection<Double> c = new ArrayList<Double>();
		for (double d : distanceToAll) {
			c.add(d);
		}
		return Collections.max(c);
	}

	private void mayersNeighborsWithOneAnother() {
		Set<AgentVariable> mayersSet = this.citiesAllocation.keySet();
		List<AgentVariable> mayersList = new ArrayList<AgentVariable>(mayersSet);
		for (int i = 0; i < mayersList.size(); i++) {
			AgentVariable a1 = mayersList.get(i);
			for (int j = i + 1; j < mayersList.size(); j++) {
				AgentVariable a2 = mayersList.get(j);
				this.neighbors.add(new Neighbor(a1, a2, D, minCost, maxCost, this.dcopId, this.dcopCityP2));
			}
		}

	}

	private void printNeighborsCoordinates() {
		for (Neighbor n : this.neighbors) {
			AgentVariable a1 = n.getA1();
			AgentVariable a2 = n.getA2();

			System.out.println(a1.getLocation());
			System.out.println(a2.getLocation());
			System.out.println();
		}
		System.out.println();

	}
	/*
	 * private void createIsNeighborMatrix() { for (int i = 0; i <
	 * agentsVariables.length; i++) { for (int j = i + 1; j <
	 * agentsVariables.length; j++) { double smallP = smallPforNeighbors[i][j];
	 * double largeP = largePforNeighbors[i][j]; if (smallP<largeP) {
	 * this.isNeigbors[i][j] = true; }else { this.isNeigbors[i][j] = false; } } }
	 * 
	 * }
	 */
	/*
	 * private void createSmallPforNeighbors() { for (int i = 0; i <
	 * agentsVariables.length; i++) { for (int j = i + 1; j <
	 * agentsVariables.length; j++) { Random r = new Random(i*97+j*117+dcopId*23);
	 * r.nextDouble(); smallPforNeighbors[i][j]= r.nextDouble(); } }
	 * 
	 * }
	 */
	/*
	 * private void printLargePforNeighbors() { for (int i = 0; i <
	 * largePforNeighbors.length; i++) { for (int j = 0; j <
	 * largePforNeighbors[i].length; j++) { if (largePforNeighbors[i][j] != null &&
	 * largePforNeighbors[i][j] == 1) { System.err.print(largePforNeighbors[i][j] +
	 * ",");
	 * 
	 * } else { System.out.print(largePforNeighbors[i][j] + ","); } }
	 * System.out.println(); } System.out.println();
	 * 
	 * }
	 */
	/*
	 * private void printSmallPforNeighbors() { for (int i = 0; i <
	 * largePforNeighbors.length; i++) { for (int j = 0; j <
	 * largePforNeighbors[i].length; j++) { if (largePforNeighbors[i][j] != null &&
	 * largePforNeighbors[i][j] == 1) { System.err.print(largePforNeighbors[i][j] +
	 * ",");
	 * 
	 * } else { System.out.print(smallPforNeighbors[i][j] + ","); } }
	 * System.out.println(); } System.out.println();
	 * 
	 * }
	 */

	/*
	 * private void createLargePforNeighbors() { double maxDistance =
	 * getMaxDistance(); Set<AgentVariable> mayers = this.citiesAllocation.keySet();
	 * for (int i = 0; i < agentsVariables.length; i++) { for (int j = i + 1; j <
	 * agentsVariables.length; j++) { AgentVariable a1 = agentsVariables[i];
	 * AgentVariable a2 = agentsVariables[j];
	 * 
	 * double distance = a1.getQuadraticDistanceTo(a2.getLocation());
	 * someChecksForNeighbors(a1, a2, i, j, distance); if (mayers.contains(a1) &&
	 * mayers.contains(a2)) { this.largePforNeighbors[i][j] = 1.0; } else {
	 * this.largePforNeighbors[i][j] = Math.pow((1 - (distance / maxDistance)),4); }
	 * /* if (i == 18 && j == 47) { System.out.println(a1 + "," + a2 + ":" +
	 * this.largePforNeighbors[i][j]); System.out.println(); }
	 */
	// }
	// }
	// System.out.println();

	// }

	private double getMaxDistance() {
		List<Double> distances = new ArrayList<Double>();
		for (int i = 0; i < agentsQuadraticDistance.length; i++) {
			List<Double> distancesRow = new ArrayList<Double>();
			for (int j = 0; j < agentsQuadraticDistance[i].length; j++) {
				distancesRow.add(agentsQuadraticDistance[i][j]);
			}
			distances.add(Collections.max(distancesRow));
		}

		return Collections.max(distances);
	}

	private void someChecksForNeighbors(AgentVariable a1, AgentVariable a2, int i, int j, double distance) {
		if (a1.getId() != i || a2.getId() != j) {
			throw new RuntimeException();
		}
		if (distance != this.agentsQuadraticDistance[i][j]) {
			throw new RuntimeException();
		}

	}

}
