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

import java.util.List;

import lsim.application.handler.Handler;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class CoordinatorPhase1StartHandler implements Handler {
	
	private Object values;
//	private Object returnObj;

	@Override
	public Object execute(Object obj) {
		values = obj;
		System.out.println("-- ** --> CoordinatorStartHandler -- values: " + values);
		return values;
	}

	public Object getValues() {;
		return values;
	}
	
	public int numWorkers(){
		List<Object> list = (List<Object>) values;
		return list.size();
	}
}
