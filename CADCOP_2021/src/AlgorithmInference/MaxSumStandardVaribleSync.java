package AlgorithmInference;

import java.util.Arrays;
import java.util.HashMap;

import AgentsAbstract.Agent;
import AgentsAbstract.NodeId;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgAlgorithmFactor;
import Messages.MsgReceive;

public class MaxSumStandardVaribleSync extends MaxSumStandardVarible{

	///// ******* Variables ******* ////

	protected HashMap<NodeId, Integer> neighborsMessageIteration; 
	protected int currentIteration;
	protected boolean print = false; 
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Variables For Damping ******* ////

	protected boolean damping = false; 
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Constructor ******* ////

	public MaxSumStandardVaribleSync(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		this.neighborsMessageIteration = new HashMap<NodeId, Integer>();
		initiatNeighborsMessageIteration();
		setCurrentIteration(0);
		this.iAmAsync = false; 
 

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Main Methods ******* ////

	// OmerP - new information has arrived and the variable node will update its value assignment.
	@Override
	public boolean compute() {

		if(allMsgsForIterationReceived()) {
		
			produceNewMessages();	
			//chooseValueAssignment();
			chooseValueLongAssignment();
			return true;
		}
		
		return false;

	}
	
	//OmerP - to reset after each run.
	@Override
	public void resetAgentGivenParametersV4() {
		
		clearHashMapIntValues(neighborsMessageIteration);
		this.currentIteration = 0;
		
	}
		
	//OmerP - update messages. 
	@Override
	protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		MsgAlgorithmFactor msgAlgorithmFactor = (MsgAlgorithmFactor) msgAlgorithm;
		
		double[] contextFix = (double[]) msgAlgorithmFactor.getContext(); // will cast the message object as a double[].

		MsgReceive<double[]> newMessageReceveid = new MsgReceive<double[]>(contextFix, msgAlgorithmFactor.getTimeStamp()); //

		functionMsgs.put(msgAlgorithmFactor.getSenderId(), newMessageReceveid);
		
		if(print){printReceivedMessage(msgAlgorithmFactor);}
				
		neighborsMessageIteration.put(msgAlgorithmFactor.getSenderId(), msgAlgorithm.getTimeStamp());
		return true;
	}
	
	// OmerP - will loop over the neighbors and will send to each one of the a
	@Override
	public void sendMsgs() {

		for (NodeId i : functionMsgs.keySet()) {
			
			MsgAlgorithmFactor m = messagesToBeSent.get(i);
			
			try {
			//mailer.sendMsg(m);
			}catch (Exception e) {
				System.out.println(3);
			}
			
			//if(print) {printSentdMessage(messagesToBeSent.get(i));}
			
			if(storedMessageOn) {
				
				storeNewMessage(i, messagesToBeSent.get(i).getContext());
				
			}
			
		}

		messagesToBeSent.clear(); 
		
	}
	
	//OmerP - will produce new messages. 
	@Override
	protected void produceNewMessages() {
		
		for (NodeId i : functionMsgs.keySet()) { //Start loop for every one of the neighbors. 
			
			double[] sentTable = new double[this.domainSize]; //Create a new table.
			sentTable = produceMessage(i, sentTable); // For each specific neighbor, sum all messages excluding the table of the receiving function node.
			sentTable = subtractMinimumValueD(sentTable);
			
			
			
			if(damping) {
				
				sentTable = damping(i, sentTable); // Will produce a damped message.
				storedMessges.put(i, sentTable); //Will store the new message
			}
			

			MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.currentIteration, this.time);
			messagesToBeSent.put(i, newMsg);
			if(print){printSentdMessage(newMsg);}
			
		}
		
	}
	
	//OmerP - will produce empty messages. 
	@Override
	protected void produceEmptyMessage() {
		
		for (NodeId i : functionMsgs.keySet()) { // Start loop over the neighbors.
		
			double[] sentTable = new double[this.domainSize];
			sentTable = produceEmptyTable(i, sentTable); // For each specific neighbor, produce an empty message.
			MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.timeStampCounter, this.time); //Create new factor message.
			messagesToBeSent.put(i, newMsg); //Store the message in the message to by sent HashMap. 
			
			if(damping) { //If damping is on it will store the new message so it could restore it for the next message that will be created. 
				
				storedMessges.put(i, newMsg.getContext());
				
			}
			
			if(print){printSentdMessage(newMsg);}

			
		}
	}
	
	protected boolean checkIfAllMessagesArrivedToAdvanceCouner(int messagesArriveCounter) {
		
		int number = this.getFunctionMsgsSize(); 
		
		if(messagesArriveCounter == number) {
			
			
			return true;
			
		}
		
		return false; 
		
	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Methods to initialize a new run  ******* ////

	//OmerP - Will initiate the list at the constructor for synchronous run. 
	public void initiatNeighborsMessageIteration() {
		
		for(NodeId i: functionMsgs.keySet()) {
			
			neighborsMessageIteration.put(i, null);
			
		}
		
	}
	
	// -----------------------------------------------------------------------------------------------------------//
	
	//OmerP - To check if all the messages at the same iteration was received. 
	protected boolean allMsgsForIterationReceived() {
		
		for(NodeId i: neighborsMessageIteration.keySet()) {
			
			if(neighborsMessageIteration.get(i) != currentIteration) {
				
				return false; 
				
			}
			
		}
				
		this.currentIteration++;
		return true; 
	}
	
	// -----------------------------------------------------------------------------------------------------------//
		
	public void setCurrentIteration(int currentIteration) {
		
		this.currentIteration = currentIteration;
		
	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Print Messages ******* ////

	@Override
	protected void printStoredMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Iteration:" + currentIteration + ", VariableNode:(" + msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") STORED a message for FunctionNode ("
				
				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + ".\n");
		
	}
	
	@Override
	protected void printSentdMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Iteration:" + currentIteration + ",VariableNode:(" + msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") SENT a message for FunctionNode ("
				
				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + ".\n");
		
	}
	
	@Override
	protected void printReceivedMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Iteration:" + currentIteration + ",VariableNode:(" + msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") RECEIVED a message from FunctionNode ("
				
				+ msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + ".\n");
		
	}
	
	//@Override
	//protected void printValueAssignment(int valueAssignment) {
		
		//System.out.println("VariableNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2() + ") value assignment is:" + valueAssignment +".\n");
		
	//}
	
	// -----------------------------------------------------------------------------------------------------------//

	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
