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

package lsim.element.recipes_service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import util.Serializer;
import lsim.application.handler.Handler;
import recipes_service.communication.Host;
import recipes_service.communication.Hosts;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class WorkerPhase1StartHandler implements Handler {
	
	private Object values;

	@Override
	public Object execute(Object obj) {
		values = obj;
		return null;
	}

	public Hosts getparticipants(Host localNode) {
		Hosts participants = new Hosts(localNode);
		List<Object> startValues = (List<Object>) values;
		for (Iterator<Object> it = startValues.iterator(); it.hasNext(); ){
			try {
				Host node = (Host) Serializer.deserialize((byte []) it.next());
				participants.add(node);
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return participants;
	}
}