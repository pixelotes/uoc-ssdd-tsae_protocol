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

package recipes_service.tsae.sessions;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import recipes_service.ServerData;
import recipes_service.activity_simulation.SimulationData;
import recipes_service.communication.Host;
import recipes_service.communication.Message;
import recipes_service.communication.MessageAErequest;
import recipes_service.communication.MessageEndTSAE;
import recipes_service.communication.MessageOperation;
import recipes_service.communication.MsgType;
import recipes_service.data.Operation;
import recipes_service.data.OperationType;
import recipes_service.tsae.data_structures.TimestampMatrix;
import recipes_service.tsae.data_structures.TimestampVector;
import communication.ObjectInputStream_DS;
import communication.ObjectOutputStream_DS;
import recipes_service.data.RemoveOperation;
import recipes_service.data.AddOperation;

//LSim logging system imports sgeag@2017
import lsim.worker.LSimWorker;
import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class TSAESessionOriginatorSide extends TimerTask{
	// Needed for the logging system sgeag@2017
	private LSimWorker lsim = LSimFactory.getWorkerInstance();
	private static AtomicInteger session_number = new AtomicInteger(0);
	
	private ServerData serverData;
	public TSAESessionOriginatorSide(ServerData serverData){
		super();
		this.serverData=serverData;		
	}
	
	/**
	 * Implementation of the TimeStamped Anti-Entropy protocol
	 */
	public void run(){
		sessionWithN(serverData.getNumberSessions());
	}

	/**
	 * This method performs num TSAE sessions
	 * with num random servers
	 * @param num
	 */
	public void sessionWithN(int num){
		if(!SimulationData.getInstance().isConnected())
			return;
		List<Host> partnersTSAEsession= serverData.getRandomPartners(num);
		Host n;
		for(int i=0; i<partnersTSAEsession.size(); i++){
			n=partnersTSAEsession.get(i);
			sessionTSAE(n);
		}
	}
	
	/**
	 * This method perform a TSAE session
	 * with the partner server n
	 * @param n
	 */
	private synchronized void sessionTSAE(Host n){
		int current_session_number = session_number.incrementAndGet();
		if (n == null) return;
		
		lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] TSAE session");

		try {
			// establecemos canales de entrada/salida
			Socket socket = new Socket(n.getAddress(), n.getPort());
			ObjectInputStream_DS in = new ObjectInputStream_DS(socket.getInputStream());
			ObjectOutputStream_DS out = new ObjectOutputStream_DS(socket.getOutputStream());
			
			// variables para ack/summary local y remoto
			TimestampVector sumLocal = null;
			TimestampMatrix ackLocal = null;
			TimestampMatrix ackRemoto = null;
			TimestampVector sumRemoto = null;
			
			// Capturamos el summary y ack locales para enviárselos al partner
			synchronized(serverData) {
				sumLocal = this.serverData.getSummary().clone();
				serverData.getAck().update(serverData.getId(), sumLocal);
				ackLocal = this.serverData.getAck().clone();
			}
			
			// Send to partner: local's summary and ack
			Message	msg = new MessageAErequest(sumLocal, ackLocal);
			msg.setSessionNumber(current_session_number);
            out.writeObject(msg);
			lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] sent message: "+msg);

            // receive operations from partner
			msg = (Message) in.readObject();
			lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] received message: "+msg);
			
			// creamos un contenedor para las operaciones recibidas
			List<MessageOperation> operaciones = new Vector<MessageOperation>();
			
			// añadimos las operaciones al contenedor
			while (msg.type() == MsgType.OPERATION){
				// 
				operaciones.add((MessageOperation) msg);   //añadimos operación
				msg = (Message) in.readObject();	//leemos siguiente mensaje
				lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] received message: "+msg);
			}

            // receive partner's summary and ack
			if (msg.type() == MsgType.AE_REQUEST){
				// ...
				MessageAErequest peticion = (MessageAErequest) msg;
				
				// nos guardamos el ack y summary del partner
				sumRemoto = peticion.getSummary();
				ackRemoto = peticion.getAck();
				
				//sacamos las operaciones que el partner no tiene
				List<Operation> opsRemotas = serverData.getLog().listNewer(sumRemoto);
				
				// send operations
				// enviamos operaciones al partner
                for (Operation op : opsRemotas) {
                	MessageOperation mso = new MessageOperation(op);     	
                    //out.writeObject(new MessageOperation(op));
                	out.writeObject(mso);
                    lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] sent message: "+msg);
                }
				//...

				// send and "end of TSAE session" message
				msg = new MessageEndTSAE();  
				msg.setSessionNumber(current_session_number);
	            out.writeObject(msg);					
				lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] sent message: "+msg);

				// receive message to inform about the ending of the TSAE session
				msg = (Message) in.readObject();
				lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] received message: "+msg);
				
				// esperamos la respuesta de fin de sesión TSAE del partner
				if (msg.type() == MsgType.END_TSAE){
					
					// Necesitamos usar un mecanismo de sincronización para que el
					// contenido de ServerData no cambie mientras realizamos las actualizaciones
					synchronized (serverData) {
						// recorremos las operaciones que nos ha mandado antes el partner
                        for (MessageOperation op : operaciones) {
                        	// y actualizamos
                            serverData.recipeManager(op);
                        }
                        
                        // después actualizamos summary y ack y purgamos log
                        serverData.getSummary().updateMax(sumRemoto);
                        serverData.getAck().updateMax(ackRemoto);
                        serverData.getLog().purgeLog(serverData.getAck());
					}
				}

			}			
			socket.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			lsim.log(Level.FATAL, "[TSAESessionOriginatorSide] [session: "+current_session_number+"]" + e.getMessage());
			e.printStackTrace();
            System.exit(1);
		}catch (IOException e) {
	    }

		
		lsim.log(Level.TRACE, "[TSAESessionOriginatorSide] [session: "+current_session_number+"] End TSAE session");
	}
}