package AlgorithmSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Random;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Comparators.CompTopColorAndMinIndex;
import Main.MainSimulator;
import Messages.MsgAMDLS;
import Messages.MsgAMDLSColor;
import Messages.MsgAlgorithm;
import Messages.MsgMgm2Phase5IsBestLR;
import Messages.MsgOpt2FriendReplay;
import Messages.MsgOpt2FriendRequest;
import Messages.MsgValueAssignmnet;

public class AOpt2_V1 extends AMDLS_V3 {

	private int k;
	private char typeDecisionChange;

	private boolean flag_chooseColor;
	private boolean flag_isConsistent;

	// if I send a friend request
	private NodeId isConsistent_friendNodeId;
	private Map<NodeId, Integer> isConsistent_belowMeCounterForForFriendSelection;
	private Random isConsistent_randomFriendSelection;

	public AOpt2_V1(int dcopId, int D, int id1, int k) {
		super(dcopId, D, id1);
		this.k = k;
		this.typeDecisionChange = 'a';

		resetAgentGivenParametersV3();
	}

	public AOpt2_V1(int dcopId, int D, int id1) {
		this(dcopId, D, id1, 2);

	}

	@Override
	protected void resetAgentGivenParametersV3() {
		flag_chooseColor = false;

		flag_isConsistent = false;
		isConsistent_friendNodeId = null;
		isConsistent_belowMeCounterForForFriendSelection = new HashMap<NodeId, Integer>();
		isConsistent_randomFriendSelection = new Random(dcopId * 10000 + this.id * 1717);
	}

	@Override
	public void initialize() {
		this.isWithTimeStamp = false;
		if (canSetColorInitilize()) {
			chooseColor();
			sendAMDLSColorMsgs();
			this.myCounter = 1;
		} else {
			this.valueAssignment = Integer.MIN_VALUE;
			this.myCounter = 0;
		}
	}

	@Override
	public void updateAlgorithmHeader() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAlgorithmData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "A2OPT";
	}

	@Override
	protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
		// TODO Auto-generated method stub
		return 0;
	}

//(1)------------------updateMessageInContext------------------
	@Override
	protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
		if (msgAlgorithm instanceof MsgAMDLSColor) {
			updateColorMsgInContext(msgAlgorithm);
		}
		return false;
	}

	private void updateColorMsgInContext(MsgAlgorithm msgAlgorithm) {
		Integer colorN = ((MsgAMDLSColor) msgAlgorithm).getColor();
		NodeId sender = msgAlgorithm.getSenderId();
		updateColor(colorN, sender);
		updateFirstValue(msgAlgorithm, sender);

	}

	private void updateColor(Integer colorN, NodeId sender) {
		neighborColors.put(sender, colorN);
		if (this.myColor != null) {
			if (this.myColor > colorN) {
				this.above.add(sender);
			} else {
				this.below.add(sender);
				isConsistent_belowMeCounterForForFriendSelection.put(sender, 0);
			}
		}
	}

	private void updateFirstValue(MsgAlgorithm msgAlgorithm, NodeId sender) {
		int currentCounterInContext = this.counters.get(sender);
		int msgCounter = ((MsgAMDLS) msgAlgorithm).getCounter();

		if (currentCounterInContext + 1 == msgCounter) {
			updateMsgInContextValueAssignmnet(msgAlgorithm);
			this.counters.put(sender, msgCounter);
		} else {
			this.future.add((MsgAMDLS) msgAlgorithm); // if the sencond msg was recieved before the first
		}

	}
//(2)------------------compute------------------

	protected boolean compute() {
		if (flag_chooseColor) {
			chooseColor();
			setAboveAndBelow();
		}

		if (flag_isConsistent) {
			this.myCounter = this.myCounter + 1;
			computeIfConsistent();
		}
	}

	private void computeIfConsistent() {
		if (this.below.size() <= this.k - 1) {

			int oldVA = this.valueAssignment;
			this.decideAndChangeLocally();

			if (MainSimulator.is2OptDebug) {
				System.out.println("A_" + this.id + " has no larger index " + "neighbors so changes locally from X_"
						+ this.id + "=" + oldVA + "too" + "X_" + this.id + "=" + this.valueAssignment);
			}
		} else {
			isConsistent_selectAFriend();
			if (MainSimulator.is2OptDebug) {
				System.out.println("A_" + this.id + " starts region as a mediator and selected "
						+ this.isConsistent_friendNodeId.toString());
			}
		}

	}

	protected void decideAndChangeLocally() {

		if (typeDecision == 'a' || typeDecision == 'A') {
			this.valueAssignment = getCandidateToChange_A();
		}
		if (typeDecision == 'b' || typeDecision == 'B') {
			this.valueAssignment = getCandidateToChange_B();
		}
		if (typeDecision == 'c' || typeDecision == 'C') {
			this.valueAssignment = getCandidateToChange_C();
		}
	}

	private void isConsistent_selectAFriend() {
		List<NodeId> idsWithMinReps = getIdsWithMinReps();
		int n = isConsistent_randomFriendSelection.nextInt(idsWithMinReps.size());
		this.isConsistent_friendNodeId = idsWithMinReps.get(n);
		int currentCounter = this.isConsistent_belowMeCounterForForFriendSelection.get(isConsistent_friendNodeId);
		int updatedCounter = currentCounter + 1;
		this.isConsistent_belowMeCounterForForFriendSelection.put(this.isConsistent_friendNodeId, updatedCounter);
	}

	private List<NodeId> getIdsWithMinReps() {
		Collection<Integer> repsOfFriendsSelected = isConsistent_belowMeCounterForForFriendSelection.values();
		int minRep = Collections.min(repsOfFriendsSelected);
		List<NodeId> idsWithMinReps = new ArrayList<NodeId>();
		for (Entry<NodeId, Integer> e : isConsistent_belowMeCounterForForFriendSelection.entrySet()) {
			NodeId nodeId = e.getKey();
			Integer reps = e.getValue();
			if (reps == minRep) {
				idsWithMinReps.add(nodeId);
			}
		}
		return idsWithMinReps;
	}

//(3)------------------sendMsgs------------------

	@Override
	public void sendMsgs() {
		if (flag_chooseColor) {
			sendAMDLSColorMsgs();
		}
		if (flag_isConsistent) {
			if (this.below.size() <= this.k - 1) {
				this.sendAMDLSmsgs();
			} else {
				this.sendFriendRequestAndFreezeRequest();
			}
		}
	}

//(4)------------------changeRecieveFlagsToTrue------------------
	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

		if (msgAlgorithm instanceof MsgAMDLSColor) {
			checkToChangeColorFlagInThisIteration(msgAlgorithm);
		}

		if (msgAlgorithm instanceof MsgValueAssignmnet) {
			isConsistent();
		}

	}

	private void isConsistent() {
		boolean initialConditions = (this.myColor != null && allNeighborsHaveColor());

		if (!initialConditions) {
			return;
		}

		boolean aboveConsistent = isAboveConsistent();
		boolean belowConsistent = isBelowConsistent();
		if (aboveConsistent && belowConsistent) {
			this.flag_isConsistent = true;
			if (MainSimulator.is2OptDebug) {
				System.out.println("A_" + this.id + " is consistent");
			}
		}
	}

	private void checkToChangeColorFlagInThisIteration(MsgAlgorithm msgAlgorithm) {
		if (canSetColor() && this.myColor == null) {
			flag_chooseColor = true;
		}
	}

	@Override
	public void changeRecieveFlagsToFalse() {
		flag_chooseColor = false;
		flag_isConsistent = false;
	}

	@Override
	public boolean getDidComputeInThisIteration() {
		return flag_chooseColor || flag_isConsistent;
	}
}
