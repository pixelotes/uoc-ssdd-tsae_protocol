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

package lsim.element.evaluator;

import java.io.IOException;
import java.util.List;

import edu.uoc.dpcs.lsim.utils.LSimParameters;
import lsim.application.handler.Handler;
import recipes_service.tsae.data_structures.Log;
import recipes_service.tsae.data_structures.TimestampVector;
import util.Serializer;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class EvaluatorPhase1InitHandler implements Handler {
	
	private LSimParameters values;
	
	@Override
	public Object execute(Object obj) {
		values = (LSimParameters) obj;		
		
		return null;
	}
	
	public Log getLog(){
		Log log = null;
		try {
			log = (Log) Serializer.deserialize((byte []) values.get("Log"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return log;
	}

	public TimestampVector getSummary(){
		TimestampVector summary = null;
		try {
			summary = (TimestampVector) Serializer.deserialize((byte []) values.get("TimestampVector"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return summary;
	}
	
	public String getUsers(){
		return ((String) values.get("Users"));
	}
	
	public String getOperations(){
		return ((String) values.get("Operations"));
	}
	
	public String getPhase(){
		return ((String) values.get("phase"));
	}
	
//	public int getExpIdDSLab(){
//		return Integer.parseInt((String)values.get("expIdDSLab"));
//	}

}