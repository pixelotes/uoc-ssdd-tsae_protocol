/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipes_service;

import java.util.List;
import java.util.Timer;
import java.util.Vector;

import edu.uoc.dpcs.lsim.LSimFactory;
import lsim.worker.LSimWorker;
import recipes_service.activity_simulation.SimulationData;
import recipes_service.communication.Host;
import recipes_service.communication.Hosts;
import recipes_service.communication.MessageOperation;
import recipes_service.data.AddOperation;
import recipes_service.data.Operation;
import recipes_service.data.OperationType;
import recipes_service.data.RemoveOperation;
import recipes_service.data.Recipe;
import recipes_service.data.Recipes;
import recipes_service.tsae.data_structures.Log;
import recipes_service.tsae.data_structures.Timestamp;
import recipes_service.tsae.data_structures.TimestampMatrix;
import recipes_service.tsae.data_structures.TimestampVector;
import recipes_service.tsae.sessions.TSAESessionOriginatorSide;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class ServerData {
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();
	
	// groupId
	private String groupId;
	
	// server id
	private String id;
	
	// sequence number of the last recipe timestamped by this server
	private long seqnum=Timestamp.NULL_TIMESTAMP_SEQ_NUMBER; // sequence number (to timestamp)

	// timestamp lock
	private Object timestampLock = new Object();
	
	// TSAE data structures
	private Log log = null;
	private TimestampVector summary = null;
	private TimestampMatrix ack = null;
	
	// recipes data structure
	private Recipes recipes = new Recipes();

	// number of TSAE sessions
	int numSes = 1; // number of different partners that a server will contact for a TSAE session each time that TSAE timer (each sessionPeriod seconds) expires

	// propDegree: (default value: 0) number of TSAE sessions done each time a new data is created
	int propDegree = 0;
	
	// Participating nodes
	private Hosts participants;

	// TSAE timers
	private long sessionDelay;
	private long sessionPeriod = 10;

	private Timer tsaeSessionTimer;

	//
	TSAESessionOriginatorSide tsae = null;

	// TODO: esborrar aquesta estructura de dades
	// tombstones: timestamp of removed operations
	List<Timestamp> tombstones = new Vector<Timestamp>();
	
	// end: true when program should end; false otherwise
	private boolean end;

	public ServerData(String groupId){
		this.groupId = groupId;
	}
	
	/**
	 * Starts the execution
	 * @param participantss
	 */
	public void startTSAE(Hosts participants){
		this.participants = participants;
		this.log = new Log(participants.getIds());
		this.summary = new TimestampVector(participants.getIds());
		this.ack = new TimestampMatrix(participants.getIds());
		

		//  Sets the Timer for TSAE sessions
	    tsae = new TSAESessionOriginatorSide(this);
		tsaeSessionTimer = new Timer();
		tsaeSessionTimer.scheduleAtFixedRate(tsae, sessionDelay, sessionPeriod);
	}

	public void stopTSAEsessions(){
		this.tsaeSessionTimer.cancel();
	}
	
	public boolean end(){
		return this.end;
	}
	
	public void setEnd(){
		this.end = true;
	}

	// ******************************
	// *** timestamps
	// ******************************
	private Timestamp nextTimestamp(){
		Timestamp nextTimestamp = null;
		synchronized (timestampLock){
			if (seqnum == Timestamp.NULL_TIMESTAMP_SEQ_NUMBER){
				seqnum = -1;
			}
			nextTimestamp = new Timestamp(id, ++seqnum);
		}
		return nextTimestamp;
	}

	// ******************************
	// *** add and remove recipes
	// ******************************
	public synchronized void addRecipe(String recipeTitle, String recipe) {

		Timestamp timestamp= nextTimestamp();
		Recipe rcpe = new Recipe(recipeTitle, recipe, groupId, timestamp);
		Operation op=new AddOperation(rcpe, timestamp);

		this.log.add(op);
		this.summary.updateTimestamp(timestamp);
		this.recipes.add(rcpe);
	}
	
	public synchronized void removeRecipe(String recipeTitle) {
		// esta funci�n es muy parecida a addRecipe
		Timestamp timestamp = nextTimestamp();
        Recipe rcpe = this.recipes.get(recipeTitle);
        Operation op = new RemoveOperation(recipeTitle, rcpe.getTimestamp(), timestamp);

        this.log.add(op);
        this.summary.updateTimestamp(timestamp);
        this.recipes.remove(recipeTitle);
	}
	
	//=========
	// Recibe un MessageOperation y, seg�n el tipo de operaci�n que contenta, a�ade o elimina una receta
	public synchronized void recipeManager(MessageOperation mso) {
		
		OperationType tipoOp = mso.getOperation().getType();
		
		// Operaci�n A�ADIR
		if (tipoOp == OperationType.ADD) {
			//System.out.println(">>> Ejecutada funci�n ServerData.recipeManager(add)");
			AddOperation op = ((AddOperation) mso.getOperation());
			if (this.log.add(op)) this.recipes.add(op.getRecipe());		
		// Operaci�n ELIMINAR
        } else {
        	//System.out.println(">>> Ejecutada funci�n ServerData.recipeManager(remove)");
        	RemoveOperation op = ((RemoveOperation) mso.getOperation());
        	if (this.log.add(op)) this.recipes.remove(op.getRecipeTitle());
        }
	}
    //=========

	// ****************************************************************************
	// *** operations to get the TSAE data structures. Used to send to evaluation
	// ****************************************************************************
	public Log getLog() {
		return log;
	}
	public TimestampVector getSummary() {
		return summary;
	}
	public TimestampMatrix getAck() {
		return ack;
	}
	public Recipes getRecipes(){
		return recipes;
	}

	// ******************************
	// *** getters and setters
	// ******************************
	public String getGroupId(){
		return this.groupId;
	}
	public void setId(String id){
		this.id = id;		
	}
	public String getId(){
		return this.id;
	}

	public int getNumberSessions(){
		return numSes;
	}

	public void setNumberSessions(int numSes){
		this.numSes = numSes;
	}

	public int getPropagationDegree(){
		return this.propDegree;
	}

	public void setPropagationDegree(int propDegree){
		this.propDegree = propDegree;
	}

	public void setSessionDelay(long sessionDelay) {
		this.sessionDelay = sessionDelay;
	}
	public void setSessionPeriod(long sessionPeriod) {
		this.sessionPeriod = sessionPeriod;
	}
	public TSAESessionOriginatorSide getTSAESessionOriginatorSide(){
		return this.tsae;
	}
	
	// ******************************
	// *** other
	// ******************************
	
	public List<Host> getRandomPartners(int num){
		return participants.getRandomPartners(num);
	}
	
	/**
	 * waits until the Server is ready to receive TSAE sessions from partner servers   
	 */
	public synchronized void waitServerConnected(){
		while (!SimulationData.getInstance().isConnected()){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//			e.printStackTrace();
			}
		}
	}
	
	/**
	 * 	Once the server is connected notifies to ServerPartnerSide that it is ready
	 *  to receive TSAE sessions from partner servers  
	 */ 
	public synchronized void notifyServerConnected(){
		notifyAll();
	}
}