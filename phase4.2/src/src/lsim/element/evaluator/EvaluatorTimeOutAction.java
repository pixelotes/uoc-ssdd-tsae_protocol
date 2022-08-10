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

import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.exceptions.LSimExceptionMessage;
import lsim.application.handler.Handler;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
/**
 * @author Joan-Manuel Marques
 * January 2013
 *
 */
public class EvaluatorTimeOutAction implements Handler{

	@Override
	public Object execute(Object arg0){
//    	System.out.println("--- *** ---> Evaluator was unable to evaluate results due to: Not all required data received.");
    	LSimFactory.getEvaluatorInstance().log(
				Level.INFO,
				"--- *** ---> Evaluator was unable to evaluate results due to: Not all required data received."
				);
		LSimFactory.getEvaluatorInstance().logException(new LSimExceptionMessage("Evaluator was unable to evaluate results due to: Not all required data received.", null, null));
    	throw new EndReceivingResults();
//		return null;
	}

}
