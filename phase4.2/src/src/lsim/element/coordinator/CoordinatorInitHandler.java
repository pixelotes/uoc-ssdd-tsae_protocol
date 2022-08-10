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

import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.utils.LSimParameters;

import lsim.application.handler.Handler;
import lsim.coordinator.LSimCoordinator;

/**
 * @author Joan-Manuel Marques, Sergi Gea
 * July 2013
 *
 */
public class CoordinatorInitHandler implements Handler {
	
	LSimParameters params;
	
//	LSimParameters paramsServer;
	
//	public CoordinatorInitHandler(){
//		this.paramsServer = paramsServer;
//	}
	
	@Override
	public Object execute(Object obj) {
		params = (LSimParameters) obj;
		
		// Add init params for the workers instances. sgeag_2017
		LSimCoordinator lsim=LSimFactory.getCoordinatorInstance();
		
		LSimParameters workerParams;
		int numWorkersByType;
		
		// For each type of worker add the initial parameters
		for(String worker_type : lsim.getWorkerTypes()){
			numWorkersByType = lsim.getNumWorkersByType(worker_type);
			workerParams = lsim.getWorkerParams(worker_type);
			if (numWorkersByType > 0){
				for(String instance_name : lsim.getAllWorkersByType(worker_type)){ 
					// Add initial parameters for this worker instance
					
					lsim.addInitParam(instance_name,"instanceDescription",workerParams.get("instanceName") +" - " + lsim.getWorkerInstanceNumber(instance_name));
					lsim.addInitParam(instance_name,"expIdDSLab",params.get("expIdDSLab"));
					
					lsim.addInitParam(instance_name,"groupId",params.get("groupId"));
					lsim.addInitParam(instance_name,"serverBasePort",params.get("serverBasePort"));
					lsim.addInitParam(instance_name,"sessionDelay",params.get("sessionDelay"));
					lsim.addInitParam(instance_name,"sessionPeriod",params.get("sessionPeriod"));
					lsim.addInitParam(instance_name,"numSes",params.get("numSes"));
					lsim.addInitParam(instance_name,"propDegree",params.get("propDegree"));
					lsim.addInitParam(instance_name,"simulationStop",params.get("simulationStop"));
					lsim.addInitParam(instance_name,"executionStop",params.get("executionStop"));
					lsim.addInitParam(instance_name,"simulationDelay",params.get("simulationDelay"));
					lsim.addInitParam(instance_name,"simulationPeriod",params.get("simulationPeriod"));
					lsim.addInitParam(instance_name,"probDisconnect",params.get("probDisconnect"));
					lsim.addInitParam(instance_name,"probReconnect",params.get("probReconnect"));
					lsim.addInitParam(instance_name,"probCreate",params.get("probCreate"));
					lsim.addInitParam(instance_name,"probDel",params.get("probDel"));
					lsim.addInitParam(instance_name,"samplingTime",params.get("samplingTime"));
					lsim.addInitParam(instance_name,"purge",params.get("purge"));
					lsim.addInitParam(instance_name,"executionMode",params.get("executionMode"));
					lsim.addInitParam(instance_name,"phase",params.get("phase"));
				
				}
			}
		}
		
		// Initial parameters	
		// new!!!
//		paramsServer.put("expIdDSLab",params.get("expIdDSLab"));
//		
//		paramsServer.put("groupId",params.get("groupId"));
//		paramsServer.put("serverBasePort",params.get("serverBasePort"));
//		paramsServer.put("sessionDelay",params.get("sessionDelay"));
//		paramsServer.put("sessionPeriod",params.get("sessionPeriod"));
//		paramsServer.put("numSes",params.get("numSes"));
//		paramsServer.put("propDegree",params.get("propDegree"));
//		paramsServer.put("simulationStop",params.get("simulationStop"));
//		paramsServer.put("executionStop",params.get("executionStop"));
//		paramsServer.put("simulationDelay",params.get("simulationDelay"));
//		paramsServer.put("simulationPeriod",params.get("simulationPeriod"));
//		paramsServer.put("probDisconnect",params.get("probDisconnect"));
//		paramsServer.put("probReconnect",params.get("probReconnect"));
//		paramsServer.put("probCreate",params.get("probCreate"));
//		paramsServer.put("probDel",params.get("probDel"));
//		paramsServer.put("samplingTime",params.get("samplingTime"));
//		paramsServer.put("purge",params.get("purge"));
//		paramsServer.put("executionMode",params.get("executionMode"));
//		paramsServer.put("phase",params.get("phase"));
	
		return null;
	}
	
	public String getGroupId(){
		return (String) params.get("groupId");
	}
	
	public String getPhase(){
		return (String) params.get("phase");
	}
	
//	public String getExpIdDSLab(){
//		return (String) params.get("expIdDSLab");
//	}
}