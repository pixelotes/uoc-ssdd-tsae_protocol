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

package lsim.element.coordinator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import edu.uoc.dpcs.lsim.utils.LSimParameters;
import lsim.application.handler.Handler;
import lsim.coordinator.LSimCoordinator;
import recipes_service.data.AddOperation;
import recipes_service.data.Operation;
import recipes_service.data.Recipe;
import recipes_service.tsae.data_structures.Log;
import recipes_service.tsae.data_structures.Timestamp;
import recipes_service.tsae.data_structures.TimestampVector;
import util.Serializer;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class CoordinatorPhase1InitHandler implements Handler {
	
	LSimParameters params;

	private TimestampVector summary;
	private Log log;
	private List<String> users = new Vector<String>();
	private List<Operation> operations = new Vector<Operation>();
	
//	LSimParameters paramsServer;
	
//	public CoordinatorPhase1InitHandler(LSimParameters paramsServer){
//		this.paramsServer = paramsServer;
//	}
	
	@Override
	public Object execute(Object obj) {
		params = (LSimParameters) obj;
				
		LSimCoordinator lsim=LSimFactory.getCoordinatorInstance();
		int numUsers = Integer.valueOf(((String) params.get("numUsers"))).intValue(); 
		int numOperations = Integer.valueOf(((String) params.get("numOperations"))).intValue();
		
		// create users
		//List<String> users = new Vector<String>();
		int seqnum[] = new int[numUsers];
		
		for (int i=0; i<numUsers; i++){
			users.add("user"+String.valueOf(i));
			seqnum[i] = 0;
		}
		lsim.log(Level.INFO, "[CoordinatorPhase1] Users created:\n" + users.toString());
		// create local TimestampVector and Log
		summary = new TimestampVector(users);
		lsim.log(Level.INFO, "[CoordinatorPhase1] New summary created:\n" + summary.toString());
		log = new Log(users);
		lsim.log(Level.INFO, "[CoordinatorPhase1] New log created:\n" + log.toString());
		// create list of operations
		//List<Operation> operations = new Vector<Operation>();
		Random rnd = new Random();

		for (int i=0; i<numOperations; i++){
			byte[] bytes=new byte[8];
			char[] chars=new char[8];
			byte mod=((byte)'z'-(byte)'a');
			rnd.nextBytes(bytes);
			for(int ii=0; ii<8; ii++){
				byte b=bytes[ii];
				if(b<0)
					b*=-1;
				b%=mod;
				chars[ii]=(char)((byte)'a'+b);
			}
			
			
			// apply operations locally
			int user = (((int)(rnd.nextDouble() *10000))%numUsers);
			Timestamp ts = new Timestamp(users.get(user), seqnum[user]++);
			
			Recipe rcpe = new Recipe(String.valueOf(chars), "Content--"+String.valueOf(chars), users.get(user), ts);
			lsim.log(Level.TRACE, "[CoordinatorPhase1] New recipe created: " + rcpe.toString());
			log.add(new AddOperation(rcpe, ts));
			summary.updateTimestamp(ts);
			lsim.log(Level.TRACE, "[CoordinatorPhase1] Log updated: " + log.toString());
			lsim.log(Level.TRACE, "[CoordinatorPhase1] Summary updated: " + summary.toString());
			
			operations.add(new AddOperation(rcpe, ts));
		}
		lsim.log(Level.INFO, "[CoordinatorPhase1] List of operations: " + operations.toString());
		lsim.log(Level.INFO, "[CoordinatorPhase1] Log: " + log.toString());
		lsim.log(Level.INFO, "[CoordinatorPhase1] Summary: " + summary.toString());
		
//		paramsServer.put("users",users);
//		try {
//			paramsServer.put("operations", Serializer.serialize(operations));
//		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// add users and operations to workers initialization parameters. Modified by sgeag_2018
		//LSimParameters workerParams;
		try {
			for(String worker_type : lsim.getWorkerTypes()){
				for(String instance_name : lsim.getAllWorkersByType(worker_type)){
					lsim.addInitParam(instance_name, "users", users);
					lsim.log(Level.INFO,"Add user to the instance " + instance_name);
					lsim.addInitParam(instance_name, "operations", Serializer.serialize(operations));
					lsim.log(Level.INFO,"Add operations to the instance " + instance_name);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lsim.log(Level.INFO,"END");
		return null;
	}

	public Log getLog(){
		return this.log;
	}
	
	public TimestampVector getSummary(){
		return this.summary; 
	}

	public String getPhase(){
		return (String) params.get("phase");
	}
	
	public String getUsers(){
		return users.toString();
	}
	
	public String getOperations(){
		StringBuffer strb = new StringBuffer();
		Iterator<Operation> it = operations.iterator();
		while(it.hasNext()){
			strb.append(it.next().toString() + "\n");
		}
		return strb.toString();
	}
	
//	public String getExpIdDSLab(){
//		return (String) params.get("expIdDSLab");
//	}
}