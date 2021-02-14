package AlgorithmInference;

import java.util.Arrays;
import java.util.HashMap;

import AgentsAbstract.Agent;
import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;
import Messages.MsgAlgorithmFactor;
import Messages.MsgReceive;

public class MaxSumStandardFunctionSync extends MaxSumStandardFunction {

	///// ******* Variables ******* ////

	protected HashMap<NodeId, Integer> neighborsMessageIteration; 
	protected int currentIteration;
	protected boolean print = false;
	
	//-----------------------------------------------------------------------------------------------------------//

	///// ******* Constructor ******* ////

	public MaxSumStandardFunctionSync(int dcopId, int D, int id1, int id2, Integer[][] constraints) {
		
		super(dcopId, D, id1, id2, constraints);
		this.neighborsMessageIteration = new HashMap<NodeId, Integer>();
		initiatNeighborsMessageIteration();
		this.currentIteration = 0; 
		this.iAmAsync = false; 
			
	}
	
	//OmerP - Constructor for Split Constraint Factor Graph. 
	public MaxSumStandardFunctionSync(int dcopId, int D, int id1, int id2, double[][] constraints) {
		
		super(dcopId, D, id1, id2, constraints);
		this.neighborsMessageIteration = new HashMap<NodeId, Integer>();
		initiatNeighborsMessageIteration();
		this.currentIteration = 0; 
		this.iAmAsync = false; 
		
	}
	//-----------------------------------------------------------------------------------------------------------//

	///// ******* Main Methods ******* ////

	//OmerP - To reset the agent if this is a new run. 
	@Override
	public void resetAgent() {
		super.resetAgent();
		this.variableMsgs = Agent.resetMapToValueNull(this.variableMsgs);
		this.neighborsMessageIteration.clear();
		
	}
	
	@Override
	protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		
		MsgAlgorithmFactor msgAlgorithmFactor;
		if (msgAlgorithm instanceof MsgAlgorithmFactor) {
			 msgAlgorithmFactor = (MsgAlgorithmFactor) msgAlgorithm;

		}
		else {
			throw new RuntimeException();
		}
		
		double[] contextFix = msgAlgorithmFactor.getContext(); //will cast the message object as a double[].
		
		MsgReceive<double[]> newMessageReceveid = new MsgReceive<double[]>(contextFix, msgAlgorithmFactor.getTimeStamp()); //
				
		variableMsgs.put(msgAlgorithmFactor.getSenderId(), newMessageReceveid);
		
		if(print){printReceivedMessage(msgAlgorithmFactor);}
		
		neighborsMessageIteration.put(msgAlgorithmFactor.getSenderId(), msgAlgorithm.getTimeStamp());
		return true;
	}
		
	//OmerP - will send new messages for each one of the neighbors upon the initiation of the algorithm (iteration = 0)
	@Override
	public void initialize() {
		
		produceOnlyConstraintMessages();
		sendMsgs();
							
	}
	
	@Override
	public boolean compute() {
		
		if(allMsgsForIterationReceived()) {
			
			produceNewMessages(); 
			
			return true; 
			
		}
		
		return false; 
		
	}
	
	//OmerP - will loop over the neighbors and will send to each one of the a message.
	@Override
	public void sendMsgs() {
		
		for(NodeId i: messagesToBeSent.keySet()) {
			
			//mailer.sendMsg(messagesToBeSent.get(i));
			
			if(print){printSentMessage(messagesToBeSent.get(i));}
		
			if(storedMessageOn) {
				
				storedMessgesTable.put(i, messagesToBeSent.get(i).getContext());
				
			}
				
		}
		
		
		messagesToBeSent.clear();
		
	} 
	
	@Override
	public void resetAgentGivenParametersV3() {
		
		clearHashMapIntValues(neighborsMessageIteration);
		this.currentIteration = 0;
		
	}
	
	//OmerP - produce new messages. 
	@Override
	protected void produceNewMessages() {
		
		for(NodeId i: variableMsgs.keySet()) {
			
			double[] sentTable = new double[this.domainSize];
			sentTable = produceFunctionMessage(i);
			MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.currentIteration, this.time);
			messagesToBeSent.put(i, newMsg);	
			//if(print){printStoredMessage(newMsg);}
			
		}
			
	}
	
	//-----------------------------------------------------------------------------------------------------------//
	
	///// ******* Methods to initialize a new run  ******* ////

	//OmerP - Will initiate the list at the constructor for synchronous run. 
	public void initiatNeighborsMessageIteration() {
		
		for(NodeId i: variableMsgs.keySet()) {
			
			neighborsMessageIteration.put(i, null);
			
		}
		
	}
	
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
	
	//OmerP - Will produce a message from the constraint matrix without the addition of messages - FIXED.
	protected void produceOnlyConstraintMessages() {
		
		for(NodeId i: variableMsgs.keySet()) {
			
			double[] sentTable = new double[this.domainSize];
			double[][] constraintMatrix = new double[this.domainSize][this.domainSize];
			constraintMatrix = neighborsConstraintMatrix.get(i);
			sentTable = getBestValueTable(constraintMatrix);
			MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.timeStampCounter, this.time);
			messagesToBeSent.put(i, newMsg);
			//if(print){printStoredMessage(newMsg);}
				
		}
		
	}
	
	protected boolean checkIfAllMessagesArrivedToAdvanceCouner(int messagesArriveCounter) {
		
		int number = this.getVariableMsgsSize(); 
		
		if(messagesArriveCounter == number) {
			
			return true;
			
		}
		
		return false; 
		
	}
	
	///// ******* Setters  ******* ////
	
	// -----------------------------------------------------------------------------------------------------------//
	
	///// ******* Tests and Prints Methods ******* ////

	@Override
	protected void printStoredMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Iteration:" + currentIteration + ", FunctionNode:(" + msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") STORED a message for VariableNode ("
				
				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + ".\n");
		
	}
	
	@Override
	protected void printSentMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Iteration:" + currentIteration + ",FunctionNode:(" + msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") SENT a message for VariableNode ("
				
				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + ".\n");
		
	}
	
	@Override
	protected void printReceivedMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Iteration:" + currentIteration + ",FunctionNode:(" + msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") RECEIVED a message from VariableNode ("
				
				+ msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + ".\n");
		
	}
	
	
	
	
	
}
