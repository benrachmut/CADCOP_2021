package Problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.Location;
import AgentsAbstract.LocationRandomUniform;
import Main.MainSimulator;

public class DcopCities extends Dcop {
	private Map<AgentVariable, List<AgentVariable>> citiesAllocation;// agent of represents center, other agents close
																		// to
																		// the city

	private List<AgentVariable> agentsWithoutLocations;
	private int costMultiplier;
	private int numberOfCities;
	private double sdSquareFromCity;

	public DcopCities(int dcopId, int A, int D, int costMultiplier, int numberOfCities, double sdSquareFromCity) {
		super(dcopId, A, D);

		this.costMultiplier = costMultiplier;
		this.numberOfCities = numberOfCities;
		this.sdSquareFromCity = sdSquareFromCity;
		agentsWithoutLocations = new ArrayList<AgentVariable>();
		for (AgentVariable a : agentsVariables) {
			agentsWithoutLocations.add(a);
		}

		List<Location> citiesCenterLocations = createLocationToCities();
		List<AgentVariable> agentMayers = selectAgentsToBeCenter();
		
		citiesAllocation = setMayersLocationAndInitiateCityMap(citiesCenterLocations, agentMayers);
		allocateAgentsToCities();
		giveCitizensLocations();

		checkIfCityAllocationIsValid(citiesCenterLocations,agentMayers);
		
		if (MainSimulator.isDcopCityDebug) {
			printLocations();
		}

	}

	private void printLocations() {
		System.out.println("dcop id: "+this.dcopId);
		System.out.println("------Mayer locations------");
		for (AgentVariable a : citiesAllocation.keySet()) {
			System.out.println(a.getId()+","+a.getLocation());
		}
		System.out.println("------Mayer locations------");
		for (List<AgentVariable> aList : citiesAllocation.values()){
			for (AgentVariable a : aList) {
				System.out.println(a.getId()+","+a.getLocation());
			}
			
		}
		System.out.println();

		
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
			List<AgentVariable>citizens = e.getValue();
			for (AgentVariable citizen : citizens) {
				citizen.setLocationCloseToCity(mayerLocation, this.dcopId, sdSquareFromCity);
				agentsWithoutLocations.remove(citizen);
			}
		}
		
	}

	private void allocateAgentsToCities() {
		List<AgentVariable> keysAsArray = new ArrayList<AgentVariable>(citiesAllocation.keySet());
		for (AgentVariable a : agentsWithoutLocations) {
			Random r = new Random((dcopId+1)*125+(a.getId()+1)*17);
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
		Random rand = new Random((this.dcopId+1) * 175);

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
			ans.add(new LocationRandomUniform(dcopId,i));
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
		// TODO Auto-generated method stub

	}

}
