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
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import edu.uoc.dpcs.lsim.utils.LSimParameters;
import lsim.LSimDispatcherHandler;
import lsim.application.ApplicationManager;
import lsim.application.handler.DummyHandler;
import lsim.coordinator.LSimCoordinator;

/*
* @author Joan-Manuel Marques
* December 2012
*
*/
public class Coordinator implements ApplicationManager{

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(LSimDispatcherHandler disp) {
		// TODO Auto-generated method stub

		LSimCoordinator lsim=LSimFactory.getCoordinatorInstance();
		lsim.setDispatcher(disp);

		// init coordinator and workers
//		InitHandler init=new InitHandler(lsim,30);
//		lsim.init(init);

		// Initial parameters		
		//LSimParameters paramsServer = new LSimParameters();

		// ABANS PROVA: versió abans d'afegir instanceId
//		// add init params to server
//		lsim.addInitParam("Wserver0","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver1","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver2","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver3","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver4","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver5","coordinatorLSimParameters",paramsServer);
//
//		
//		// add init params to serverSD
//		lsim.addInitParam("WserverSD0","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD1","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD2","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD3","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD4","coordinatorLSimParameters",paramsServer);

		// PROVA: instanceId intenta notificar a cada node un identificador descriptiu 
		// que permeti saber qui ha generat cada resultat. Per a que sigui més informatiu pels alumnes
		
		// add init params to server 
		// Now the params to server are added from the CoordinatorInitHandlear sgeag_2017
//		lsim.addInitParam("Wserver_0","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver_0","instanceDescription","student - instance 1");
//		lsim.addInitParam("Wserver_1","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver_1","instanceDescription","student - instance 2");
//		lsim.addInitParam("Wserver_2","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver_2","instanceDescription","student - instance 3");
//		lsim.addInitParam("Wserver_3","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver_3","instanceDescription","student - instance 4");
//		lsim.addInitParam("Wserver_4","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver_4","instanceDescription","student - instance 5");
//		lsim.addInitParam("Wserver_5","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("Wserver_5","instanceDescription","student - instance 6");
//
//		
//		// add init params to serverSD
		// Now the params to serverSD are added from the CoordinatorInitHandlear sgeag_2017
//		lsim.addInitParam("WserverSD_0","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD_0","instanceDescription","instructor - instance 1");
//		lsim.addInitParam("WserverSD_1","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD_1","instanceDescription","instructor - instance 2");
//		lsim.addInitParam("WserverSD_2","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD_2","instanceDescription","instructor - instance 3");
//		lsim.addInitParam("WserverSD_3","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD_3","instanceDescription","instructor - instance 4");
//		lsim.addInitParam("WserverSD_4","coordinatorLSimParameters",paramsServer);
//		lsim.addInitParam("WserverSD_4","instanceDescription","instructor - instance 5");
		//+++++++++++++++++++++++++++++++++
		
		// init coordinator and workers
		//CoordinatorInitHandler init=new CoordinatorInitHandler(paramsServer);
		CoordinatorInitHandler init=new CoordinatorInitHandler();
		// InitHandler init=new InitHandler(lsim,30);
		lsim.init(init);
		// start workers
		CoordinatorStartHandler startHandler = new CoordinatorStartHandler();
		lsim.start(startHandler);
		
		// add parameter to evaluator parameters
		lsim.addInitParam("evaluator", "groupId", init.getGroupId());
		lsim.addInitParam("evaluator", "numServers", Integer.valueOf(startHandler.numWorkers()));
		lsim.addInitParam("evaluator", "phase", init.getPhase());
//		lsim.addInitParam("evaluator", "expIdDSLab", init.getExpIdDSLab());

		// stop
		lsim.stop(new DummyHandler());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}


}
