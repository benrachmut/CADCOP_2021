package AlgorithmSearch;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Random;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Comparators.CompTopColorAndMinIndex;
import Messages.MsgAMDLS;
import Messages.MsgAMDLSColor;
import Messages.MsgAlgorithm;
import Messages.MsgMgm2Phase5IsBestLR;
import Messages.MsgOpt2FriendReplay;
import Messages.MsgOpt2FriendRequest;
import Messages.MsgValueAssignmnet;

public class AOpt2_V1 extends AgentVariableSearch {

	protected Set<MsgAlgorithm> future;
	// protected Map<NodeId, Integer> neighborColors;
	protected Integer myCounter;
	// protected Map<NodeId, Integer> neighborCounters;
	// protected Map<NodeId, Integer> neighborBelowMeRequestCounter;
	// protected Map<NodeId, KOptInfo> mapRecieveFriendRequest;

	protected Set<NodeId_AOpt2> neighborsAOpt2;

	protected NodeId_AOpt2 nodeIdFriendIAskFor;

	protected Find2Opt find2OptFriendIAskFor;
	protected Integer myColor;

	protected Random rndFriendRequest;

	protected boolean waitingToSetColor;
	protected boolean waitForAnything;
	protected boolean waitForMoreColors;
	protected boolean waitForFriendReplay;

	protected boolean flagCanSetColor;
	protected boolean flagAskForFriend;
	protected boolean flagReplayToFriendRequest;
	protected boolean flagGotNegativeReplayForMyRequest;
	protected boolean flagGotPositiveReplayForMyRequest;
	
	public AOpt2_V1(int dcopId, int D, int agentId) {
		super(dcopId, D, agentId);
		resetAgentGivenParametersV3();
	}

	// done
	@Override
	public void updateAlgorithmHeader() {
		AgentVariable.algorithmHeader = "";
	}

	// done
	@Override
	public void updateAlgorithmData() {
		AgentVariable.algorithmData = "";
	}

	// done
	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "AOPT2";
	}

	// ------------ **reset** ------------
	@Override
	protected void resetAgentGivenParametersV3() {
		future = new HashSet<MsgAlgorithm>();
		waitingToSetColor = true;
		myColor = null;
		myCounter = 1;
		nodeIdFriendIAskFor = null;
		neighborsAOpt2 = new HashSet<NodeId_AOpt2>();

		for (NodeId nodeId : this.getNeigborSetId()) {
			neighborsAOpt2.add(new NodeId_AOpt2(nodeId));
		}
	}


	// ------------ **initialize** ------------
	@Override
	public void initialize() {
		if (canSetColorInitilize()) { // if the lowest index out of my neighbors can change to different heuristics
			chooseColor(); // choose the smallest possible color out of my neighbors
			sendAMDLSColorMsgs(); // send msg with color and first value assignmnets
			waitingToSetColor = false;
		}
		// super.initialize();
	}

	protected void chooseColor() {
		Integer currentColor = 1;
		while (true) {
			if (isColorValid(currentColor)) {
				break;
			}
			currentColor = currentColor + 1;
		}
		this.myColor = currentColor;
	}

	private boolean isColorValid(Integer currentColor) {
		// for (Integer nColor : neighborColors.values()) {
		for (NodeId_AOpt2 node : this.neighborsAOpt2) {
			Integer nColor = node.getColor();

			if (nColor != null) {
				if (nColor.equals(currentColor)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean canSetColorInitilize() {
		for (NodeId nodeId : this.neighborsConstraint.keySet()) {
			if (this.id > nodeId.getId1()) {
				return false;
			}
		}
		return true;
	}

	// ------------ **updateMessageInContext** ------------

	@Override
	protected boolean updateMessageInContext(MsgAlgorithm m) {
		NodeId sendId_temp = m.getSenderId();
		NodeId_AOpt2 senderId = get_NodeId_AOpt2(sendId_temp);
		boolean ans = false;
		if (m instanceof MsgAMDLSColor) {
			Integer colorFromMsg = ((MsgAMDLSColor) m).getColor();
			senderId.setColor(colorFromMsg);
			updateCounterAndValue(m, senderId);
			ans = true;
		}

		if (!(m instanceof MsgAMDLSColor) && waitingToSetColor) {
			future.add(m);
			ans = true;

		}

		if (m instanceof MsgAMDLS && !(m instanceof MsgAMDLSColor) && waitForAnything) {
			updateCounterAndValue(m, senderId);
			ans = true;

		}

		if (m instanceof MsgAMDLS) {
			updateCounterAndValue(m, senderId);
			ans = true;

		}

		if (m instanceof MsgOpt2FriendRequest) {
			senderId.setkOptInfo((KOptInfo) m.getContext());
			ans = true;

		}

		if (m instanceof MsgOpt2FriendReplay) {
			this.find2OptFriendIAskFor = (Find2Opt) m.getContext();
			ans = true;

			if (m.getSenderId().getId1() != this.nodeIdFriendIAskFor.getId1()) {
				throw new RuntimeException("recieve friend replay from someone I didnt ask for");
			}
		}
		return ans;

	}

	private NodeId_AOpt2 get_NodeId_AOpt2(NodeId sendId_temp) {
		for (NodeId_AOpt2 nodeIdAOpt2 : neighborsAOpt2) {
			if (sendId_temp.getId1() == nodeIdAOpt2.getId1()) {
				return nodeIdAOpt2;
			}
		}
		return null;
	}

	private void updateCounterAndValue(MsgAlgorithm m, NodeId_AOpt2 sendId) {
		Integer counterFromMsg = ((MsgAMDLS) m).getCounter();
		if (counterFromMsg < sendId.getCounter()) {
			throw new RuntimeException("something does not make sense");
		}
		sendId.setCounter(counterFromMsg);
		// this.neighborCounters.put(sendId. counterFromMsg);

		MsgValueAssignmnet vam = new MsgValueAssignmnet(m);
		super.updateMsgInContextValueAssignmnet(vam);

	}

	// ------------ **changeRecieveFlagsToTrue** ------------

	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm m) {
		if (recieveColorMsgAndContainAllNeedToSelectColor(m)) {
			flagCanSetColor = true;
		}

		if (recieveAllColorsAndCanAskForFriend(m) || recieveValueCounterMsgAndCanAskForFriend(m)) {
			flagAskForFriend = true;
		}

		if (recieveAllRequiredInfoToReplayForRequest(m)) {
			flagReplayToFriendRequest = true;
		}

		if (recieveValueFromAgentIRequestedFriendship(m)) {
			flagGotNegativeReplayForMyRequest = true;
		}

		if (recieveFriendReplay(m) ) {
			flagGotPositiveReplayForMyRequest = true;
		}

	}

	private boolean recieveFriendReplay(MsgAlgorithm m) {
		boolean typeMsg = m instanceof MsgOpt2FriendReplay;
		int idOfMsg = m.getSenderId().getId1();
		if (idOfMsg != this.nodeIdFriendIAskFor.getId1()) {
			throw new RuntimeException("got replay from someone i did not ask for");
		}
		return true;
	}

	private boolean recieveValueFromAgentIRequestedFriendship(MsgAlgorithm m) {
		// TODO Auto-generated method stub
		return  (m instanceof MsgAMDLS && !(m instanceof MsgAMDLSColor)) && this.nodeIdFriendIAskFor.getId1()== m.getSenderId().getId1();
	}

	private boolean recieveAllRequiredInfoToReplayForRequest(MsgAlgorithm m) {
		boolean validTypeMsg = m instanceof MsgOpt2FriendRequest
				|| (m instanceof MsgAMDLS && !(m instanceof MsgAMDLSColor));

		return validTypeMsg && haveAnyFriendRequest() && canRepalyToFriendRequest();
	}

	private boolean recieveValueCounterMsgAndCanAskForFriend(MsgAlgorithm m) {
		// TODO Auto-generated method stub
		return m instanceof MsgAMDLS && !(m instanceof MsgAMDLSColor) && waitForAnything
				&& statusEnableToAskForFriend();
	}

	private boolean recieveAllColorsAndCanAskForFriend(MsgAlgorithm m) {
		return m instanceof MsgAMDLSColor && this.myColor != null && allNeighborsHaveColor()
				&& statusEnableToAskForFriend();

	}

	private boolean statusEnableToAskForFriend() {
		return (allAboveOneMore() && allBelowLikeMe()) || (this.myColor == 1 && allBelowLikeMe());
	}

	private boolean recieveColorMsgAndContainAllNeedToSelectColor(MsgAlgorithm m) {
		return m instanceof MsgAMDLSColor && waitingToSetColor && this.myColor == null && canSetColor();
	}

	private boolean canRepalyToFriendRequest() {
		NodeId_AOpt2 boundedNodeIdThatRequested = getBoundedNodeIdThatRequested();
		Set<NodeId_AOpt2> releventNodeIds = getAllNodeIdsAboveReleventNeighbor(boundedNodeIdThatRequested);
		for (NodeId_AOpt2 nodeId_AOpt2 : releventNodeIds) {
			if (nodeId_AOpt2.getCounter()+1 != this.myCounter) {
				return false;
			}
		}
		return true;
	}

	private Set<NodeId_AOpt2>getAllNodeIdsAboveReleventNeighbor(NodeId_AOpt2 boundedNodeIdThatRequested){
		Set<NodeId_AOpt2> ans = new HashSet<NodeId_AOpt2>();
		CompTopColorAndMinIndex c = new CompTopColorAndMinIndex();
		for (NodeId_AOpt2 nodeId_AOpt2 : this.neighborsAOpt2) {
			if (c.compare(boundedNodeIdThatRequested, nodeId_AOpt2)>0) {
				ans.add(nodeId_AOpt2);
			}
		}
		return ans;
	}
	
	private NodeId_AOpt2 getBoundedNodeIdThatRequested() {
		Set<NodeId_AOpt2> allRequests = getAllRequests();
		return Collections.max(allRequests, new CompTopColorAndMinIndex());
	}

	private Set<NodeId_AOpt2> getAllRequests() {
		Set<NodeId_AOpt2>requestedNodeIds = new HashSet<NodeId_AOpt2>();
		for (NodeId_AOpt2 nodeId_AOpt2 : requestedNodeIds) {
			if (nodeId_AOpt2.getkOptInfo()!=null) {
				requestedNodeIds.add(nodeId_AOpt2);
			}
		}
		return requestedNodeIds;
	}

	private boolean haveAnyFriendRequest() {
		for (NodeId_AOpt2 koi : this.neighborsAOpt2) {
			if (koi != null) {
				return true;
			}
		}
		return false;
	}

	private boolean allBelowLikeMe() {
		Set<NodeId_AOpt2> belowMe = getAllBelowMe();

		for (NodeId_AOpt2 nodeId : belowMe) {
			if (nodeId.getCounter() == myCounter) {
				return false;
			}

		}
		return true;
	}

	private Set<NodeId_AOpt2> getAllBelowMe() {
		Set<NodeId_AOpt2> ans = new HashSet<NodeId_AOpt2>();
		for (NodeId_AOpt2 nodeId : neighborsAOpt2) {
			if (nodeId.getColor() > this.myColor) {
				ans.add(nodeId);
			}
			if (nodeId.getColor() == this.myColor) {
				throw new RuntimeException("i have neighbors with similar color");
			}
		}
		return ans;
	}

	private boolean allAboveOneMore() {
		Set<NodeId_AOpt2> aboveMe = getAllAboveMe();
		for (NodeId_AOpt2 nodeId_AOpt2 : aboveMe) {
			if (nodeId_AOpt2.getCounter() + 1 != myCounter) {
				return false;
			}

		}
		return true;
	}

	private Set<NodeId_AOpt2> getAllAboveMe() {
		
		Set<NodeId_AOpt2> ans = new HashSet<NodeId_AOpt2>();
		for (NodeId_AOpt2 nodeId_AOpt2 : this.neighborsAOpt2) {
			if (nodeId_AOpt2.getColor() < this.myColor) {
				ans.add(nodeId_AOpt2);

			}
			if (nodeId_AOpt2.getColor() == this.myColor) {
				throw new RuntimeException("i have neighbors with similar color");
			}
		}
		return ans;
	}

	protected boolean canSetColor() {

		Set<NodeId_AOpt2> neighborsThatHaveColor = getNeighborsThatHaveColor();
		Set<NodeId_AOpt2> neighborsIRequireToWait = neighborsWithSmallerIndexThenMe();

		for (NodeId nodeId : neighborsIRequireToWait) {
			if (!neighborsThatHaveColor.contains(nodeId)) {
				return false;
			}
		}
		return true;
	}

	private Set<NodeId_AOpt2> neighborsWithSmallerIndexThenMe() {
		Set<NodeId_AOpt2> ans = new HashSet<NodeId_AOpt2>();
		
		for (NodeId_AOpt2 nodeId_AOpt2 : this.neighborsAOpt2) {
			if (nodeId_AOpt2.getId1() < this.id) {
				ans.add(nodeId_AOpt2);
			}
		}
		return ans;
	}

	private Set<NodeId_AOpt2> getNeighborsThatHaveColor() {
		Set<NodeId_AOpt2> ans = new HashSet<NodeId_AOpt2>();
		for (NodeId_AOpt2 nodeId_AOpt2 : this.neighborsAOpt2) {
			if (nodeId_AOpt2.getColor()!=null) {
				ans.add(nodeId_AOpt2);
			}
		}
		return ans;
	}

	// ------------ **compute** ------------

	@Override
	protected boolean compute() {
		
		boolean flag = false;
		if (flagCanSetColor) {
			chooseColor();
			flag = true;
		}
		if (flagAskForFriend) {
			selectFriend();
			flag = true;
		}
		if (flagReplayToFriendRequest) {
			
			flag = true;
		}
		if (flagGotNegativeReplayForMyRequest) {
			
			flag = true;
		}
		if (flagGotPositiveReplayForMyRequest) {
			
			flag = true;
		}
		return flag;
	}

	private KOptInfo makeMyKOptInfo() {
		return new KOptInfo(this.valueAssignment, nodeId, neighborsConstraint, domainArray,
				this.neighborsValueAssignmnet);
	}

	private void selectFriend() {
		
		Set<NodeId_AOpt2> allRequests = getAllRequests();
		int minRequests = getTheMinAmountOfRequestsFromAgents(allRequests);
		Set<NodeId_AOpt2> allRequestsAtMinAmounts = new HashSet<NodeId_AOpt2>();
		
			
		increaseFriendshipByOne();
		
		
		Integer minValIRequest = Collections.min(this.neighborBelowMeRequestCounter.values());
		Set<NodeId> potentialFriends = new HashSet<NodeId>();
		for (Entry<NodeId, Integer> e : neighborBelowMeRequestCounter.entrySet()) {
			if (e.getValue() == minValIRequest) {
				potentialFriends.add(e.getKey());
			}
		}
		int i = this.rndFriendRequest.nextInt(potentialFriends.size());
		this.nodeIdFriendIAskFor = (NodeId) potentialFriends.toArray()[i];

	}

	private int getTheMinAmountOfRequestsFromAgents(Set<NodeId_AOpt2> allRequests) {
		int minRequests = Integer.MAX_VALUE;
		for (NodeId_AOpt2 nodeId_AOpt2 : allRequests) {
			if (nodeId_AOpt2.getNumberOfFrienships()<minRequests) {
				minRequests = nodeId_AOpt2.getNumberOfFrienships();
			}
		}
		return minRequests;
	}

	// ------------ **sendMsgs** ------------
	@Override
	public void sendMsgs() {
		if (flagCanSetColor) {
			sendAMDLSColorMsgs();
			this.waitingToSetColor = false;
			if (!allNeighborsHaveColor()) {
				this.waitForMoreColors = true;
			} else {
				waitForMoreColors = false;
				this.waitForAnything = true;
				resetNeighborBelowMeRequestCounter();
			}
		}
		if (flagAskForFriend) {
			sendFriendRequest();
			this.waitForAnything = false;
			this.waitForFriendReplay = true;
		}

	}

	private void sendFriendRequest() {
		MsgOpt2FriendRequest m = new MsgOpt2FriendRequest(this.nodeId, this.nodeIdFriendIAskFor, makeMyKOptInfo(),
				this.timeStampCounter, this.time);
		this.mailer.sendMsg(m);
	}

	private void resetNeighborBelowMeRequestCounter() {
		this.neighborBelowMeRequestCounter = new HashMap<NodeId, Integer>();
		for (NodeId nodeId : this.getAllBelowMe()) {
			this.neighborBelowMeRequestCounter.put(nodeId, 0);
		}

	}

	private boolean allNeighborsHaveColor() {
		for (Integer i : this.neighborColors.values()) {
			if (i == null) {
				return false;
			}
		}
		return true;
	}

	protected void sendAMDLSColorMsgs() {
		for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
			MsgAMDLSColor mva = new MsgAMDLSColor(this.nodeId, recieverNodeId, this.valueAssignment,
					this.timeStampCounter, this.time, this.myCounter, this.myColor);
			this.mailer.sendMsg(mva);
		}
	}

	// ------------ **getSenderCurrentTimeStampFromContext** ------------

	@Override
	protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getDidComputeInThisIteration() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changeRecieveFlagsToFalse() {
		// TODO Auto-generated method stub

	}

}
