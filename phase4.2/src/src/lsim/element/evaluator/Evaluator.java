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

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import lsim.LSimDispatcherHandler;
import lsim.application.ApplicationManager;
import lsim.application.handler.DummyHandler;
import lsim.evaluator.DefaultResultHandler;
import lsim.evaluator.GetResultTimeoutException;
import lsim.evaluator.LSimEvaluator;
import recipes_service.test.PartialResult;
import recipes_service.test.ResultBase;
import recipes_service.test.ServerResult;
import storage.ResultStorage;
import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.exceptions.LSimExceptionMessage;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */

public class Evaluator implements ApplicationManager {

	private boolean allResultsReceived = false;

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(LSimDispatcherHandler disp) {
		LSimFactory.getEvaluatorInstance().setDispatcher(disp);
		try{
			process(disp);
		}catch(RuntimeException e){
			LSimFactory.getEvaluatorInstance().logException(new LSimExceptionMessage("", e, null));
		}
	}

	public void process(LSimDispatcherHandler hand) {
		LSimEvaluator lsim = LSimFactory.getEvaluatorInstance();
		
		// set maximum time (minutes) that is expected the evaluation of experiment will last
		lsim.startTimer(30);
		
		// Sets the action to perform if the evaluator timeout expires
		EvaluatorTimeOutAction timeOutHandler = new EvaluatorTimeOutAction();
		lsim.setTimeOutAction(timeOutHandler);


		System.out.println("========= EVALUATOR");
		EvaluatorInitHandler init = new EvaluatorInitHandler();

		lsim.init(init);
		int percentageRequiredResults = init.getPercentageRequiredResults();
		int numNodes = init.getNumNodes();
		
		// Set result handler to standard (receive results and return one result
		// a time)
		DefaultResultHandler resultHandler = new DefaultResultHandler();
		// Must be done before start
		lsim.setResultHandler(resultHandler);
		
		lsim.start(new DummyHandler());

		ObjectSerializableHandler<ResultBase> getResultHandler = new ObjectSerializableHandler<ResultBase>();
		
        // receives results from nodes 
        int numRequiredResults = ((numNodes*percentageRequiredResults)/100 + 1);

        List<ServerResult> finalResults = new Vector<ServerResult>();
		HashMap<Integer, List<ServerResult>> partialResults = new HashMap<Integer, List<ServerResult>>();
		try{
			int i = 0;
			do{
				lsim.getResult(getResultHandler);
				ResultBase result = (ResultBase)getResultHandler.value();
				switch(result.type()){
				case PARTIAL:
					Integer iteration = ((PartialResult)result).getIteration();
					List<ServerResult> results = null;
					if (partialResults.containsKey(iteration)){
						results = partialResults.get(iteration);
					} else{
						results = new Vector<ServerResult>();
					}
					results.add(result.getServerResult());
					partialResults.put(iteration, results);
//					System.out.println("##### [iteration: "+iteration
//							+"] partial result from server: " + result.getServerResult().getNodeId());
					lsim.log(Level.INFO,
							"##### [iteration: "+iteration
							+"] partial result from server: " + result.getServerResult().getNodeId()
							);
					break;
				case FINAL:
					finalResults.add(result.getServerResult());
//					System.out.println("##### Final result from server: " + result.getServerResult().getNodeId());
					lsim.log(Level.INFO,
							"##### Final result from server: " + result.getServerResult().getNodeId()
							);
					if (finalResults.size() == numNodes){
						allResultsReceived = true;
					}
					break;
				}

				// We don't know how many nodes will send results.
				// Timer is reset at each iteration. When timer finishes 
				// the number of nodes to evaluate will be the nodes that
				// had send results
				resultHandler.setGetResultTimeout(15000);
			}while(!allResultsReceived);
		}catch (GetResultTimeoutException e){
//			System.out.println(e.getMessage());
			// TODO-JM: mirar per què no funciona: lsim.logException(new LSimExceptionMessage(e.getMessage(), null, null));
		}
		
		if (finalResults.size() < numRequiredResults){
			System.out.println("Unable to evaluate results due to: Not enough Servers where connected at the moment of finishing the Activity Simulation phase.");
//			System.out.println("Partial results received: "+partialResults.size());
//			System.out.println("Final results received: "+finalResults.size());
//			System.out.println("numRequiredResults: "+numRequiredResults);
			lsim.log(Level.ERROR,
					"Unable to evaluate results due to: Not enough Servers where connected at the moment of finishing the Activity Simulation phase."
					+ "\nReceived Results: "+finalResults.size()
					+ "\nnumRequiredResults: "+numRequiredResults
					);
			lsim.logException(new LSimExceptionMessage(
					"Unable to evaluate results due to: Not enough Servers where connected at the moment of finishing the Activity Simulation phase."
					+ "\nReceived Results: "+finalResults.size()
					+ "\nnumRequiredResults: "+numRequiredResults,
					null,
					null
					)
			);
//			lsim.logException(new LSimExceptionMessage("Unable to evaluate results due to: Not enough Servers where connected at the moment of finishing the Activity Simulation phase.", null, null));
//			lsim.logException(new LSimExceptionMessage("Received Results: "+finalResults.size(), null, null));
//			lsim.logException(new LSimExceptionMessage("numRequiredResults: "+numRequiredResults, null, null));

			String resultSummary = "Unable to evaluate";
			String resultDetail = "Unable to evaluate results due to: Not enough Servers where connected at the moment of finishing the Activity Simulation phase. Submit again the execution"
								+ '\n' + "Recieved Results: "+finalResults.size()
								+ '\n' + "numRequiredResults: "+numRequiredResults;

			ResultStorage res = new ResultStorage();
//			res.setExpIdGrails(init.getExpIdDSLab());
			res.setPhase(init.getPhase());
			res.setResult(resultDetail);
			if (resultSummary.contains("Correct")) res.setSuccess(true);
			else res.setSuccess(false);

			lsim.store(resultSummary, res);

			lsim.stop(new DummyHandler());
		}

		// evaluate final results
		String resultDetail = "##### ["+"All instances have the same value in all data structures (recipes, log, summary, Ack)"+"] Result:\n" + finalResults.get(0);
//		String resultDetail = "##### ["+finalResults.get(0).getNodeId()+"] Result:\n " + finalResults.get(0);
		String resultDetailNotAllInstancesEqual = "##### ["+finalResults.get(0).getNodeId()+"] Result:\n" + finalResults.get(0);
//		System.out.println("##### ["+finalResults.get(0).getNodeId()+"] Result:\n " + finalResults.get(0));
		boolean equal = true;
		
		for (int i = 1 ; i<finalResults.size()/* && equal*/; i++){
			if (init.getPhase().equals("2")) equal = equal && finalResults.get(0).equalsNoACK(finalResults.get(i));
			else equal = equal && finalResults.get(0).equals(finalResults.get(i));
//			if (!equal){
////				System.out.println("##### ["+finalResults.get(i).getNodeId()+"] Result:\n " + finalResults.get(i));
//				resultDetail += "##### (different) ["+finalResults.get(i).getNodeId()+"] Result:\n " + finalResults.get(i);
//			}
			resultDetailNotAllInstancesEqual += "##### ["+finalResults.get(i).getNodeId()+"] Result:\n" + finalResults.get(i);
		}
		if (!equal){
			resultDetail = resultDetailNotAllInstancesEqual;
		}
		
		// calculate in which iteration nodes converged
		boolean converged = false;
		int convergenceIteration = -1;
		for (int it = 0; partialResults.containsKey(Integer.valueOf(it))&& !converged; it++){
			List<ServerResult> results = partialResults.get(Integer.valueOf(it));
			converged = (partialResults.size() >= finalResults.size());
			for (int i = 1 ; i<results.size() && converged; i++){
				if (init.getPhase().equals("2")) converged = converged && results.get(0).equalsNoACK(results.get(i));
				else converged = converged && results.get(0).equals(results.get(i));
			}
			if (converged){
				convergenceIteration = it;
			}
		}
 
		System.out.println("\n\n");
		System.out.println("*********** num received results: "+finalResults.size());
		System.out.println("*********** % received results: "+(finalResults.size()*100/numNodes));
		System.out.println("*********** minimal required number of results: "+numRequiredResults);
		System.out.println("\n\n");
		lsim.log(Level.INFO, "*********** num received results: "+finalResults.size());
		lsim.log(Level.INFO, "*********** % received results: "+(finalResults.size()*100/numNodes));
		lsim.log(Level.INFO, "*********** minimal required number of results: "+numRequiredResults);
		resultDetail += 
				"\n\n"
				+ '\n' + "*********** num received results: "+finalResults.size()
				+ '\n' + "*********** % received results: "+(finalResults.size()*100/numNodes)
				+ '\n' + "*********** minimal required number of results: "+numRequiredResults
				+ "\n\n"
				;

        // ------------------------------------------------
        // final results
        // ------------------------------------------------
		// write final result
//		System.out.println("\n\n");
		String result;
		if (equal){
			result = "Correct. Results are equal";
			if (convergenceIteration == -1){
				result += "\t Nodes converged at the last iteration ";
			} else{
				result += "\t Nodes converged at the iteration " + convergenceIteration;
			}
		} else{
			result = "Results are NOT equal";
		}
//		lsim.store("\n\n"
//				+ finalResults.get(0).getGroupId()
//				+ '\t' + result
//				);
		// TODO-JM (3 de juliol de 2014): acabar de formatar el resultat segons el que fèiem al 2013p
		ResultStorage res = new ResultStorage();
//		res.setExpIdGrails(init.getExpIdDSLab());
		res.setPhase(init.getPhase());
		res.setResult(resultDetail);
		if (result.contains("Correct")) res.setSuccess(true);
		else res.setSuccess(false);
		
		System.out.println("abans de l'store!!!");
		lsim.store(result, res);
    	System.out.println(result);
    	lsim.log(Level.INFO, result);
//      System.out.println("\n\n");
//		System.out.println("================================================");
//		System.out.println("\n\n");
                
        // ------------------------------------------------
        // stop
        // ------------------------------------------------

        lsim.stop(new DummyHandler());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
}