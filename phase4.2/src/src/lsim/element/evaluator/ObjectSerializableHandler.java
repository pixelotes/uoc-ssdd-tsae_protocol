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

import util.Serializer;
import lsim.application.handler.Handler;
import lsim.result.Result;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class ObjectSerializableHandler<E> implements Handler {
	
	private E value = null;
	private Result result = null;

	@Override
	public Object execute(Object obj) {
		if (obj == null){
			return null;
		}
		try {
			result = (Result) obj;
			value = (E) Serializer.deserialize((byte []) result.value());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}
		return value;
	}

	public E value() {
		return value;
	}
}