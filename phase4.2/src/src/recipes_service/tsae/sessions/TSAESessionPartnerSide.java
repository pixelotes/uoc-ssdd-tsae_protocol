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
import java.util.ArrayList;

import communication.ObjectInputStream_DS;
import communication.ObjectOutputStream_DS;
import recipes_service.ServerData;
import recipes_service.communication.Message;
import recipes_service.communication.MessageAErequest;
import recipes_service.communication.MessageEndTSAE;
import recipes_service.communication.MessageOperation;
import recipes_service.communication.MsgType;
import recipes_service.data.AddOperation;
import recipes_service.data.Operation;
import recipes_service.data.OperationType;
import recipes_service.tsae.data_structures.TimestampMatrix;
import recipes_service.tsae.data_structures.TimestampVector;

//LSim logging system imports sgeag@2017
import lsim.worker.LSimWorker;
import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques December 2012
 *
 */
public class TSAESessionPartnerSide extends Thread {
	// Needed for the logging system sgeag@2017
	private LSimWorker lsim = LSimFactory.getWorkerInstance();

	private Socket socket = null;
	private ServerData serverData = null;

	public TSAESessionPartnerSide(Socket socket, ServerData serverData) {
		super("TSAEPartnerSideThread");
		this.socket = socket;
		this.serverData = serverData;
	}

	public synchronized void run() {

		Message msg = null;

		int current_session_number = -1;
		try {
			// establecemos streams de entrada / salida
			ObjectOutputStream_DS out = new ObjectOutputStream_DS(socket.getOutputStream());
			ObjectInputStream_DS in = new ObjectInputStream_DS(socket.getInputStream());

			// receive originator's summary and ack
			msg = (Message) in.readObject();
			current_session_number = msg.getSessionNumber();
			lsim.log(Level.TRACE, "[TSAESessionPartnerSide] [session: " + current_session_number + "] TSAE session");
			lsim.log(Level.TRACE,
					"[TSAESessionPartnerSide] [session: " + current_session_number + "] received message: " + msg);

			// el originador debería enviarnos un mensaje con el ack y el summaty
			if (msg.type() == MsgType.AE_REQUEST) {
				// ========
				// send operations

				// extraemos el ack y el summary del originador
				TimestampMatrix ackLocal = null;
				TimestampVector sumLocal = null;
				TimestampMatrix ackRemoto = null;
				TimestampVector sumRemoto = null;

				// Capturamos nuestros ack y summary locales
				synchronized (serverData) {
					sumLocal = serverData.getSummary().clone();
					serverData.getAck().update(serverData.getId(), sumLocal);
					ackLocal = serverData.getAck().clone();
				}

				// ========

				// comparamos las operaciones del originador con las nuestras
				// y sacamos aquellas que tenemos nosotros y la otra parte no (Log.listNewer())
				MessageAErequest peticion = (MessageAErequest) msg;
				// Capturamos los ack y summary remotos
				ackRemoto = peticion.getAck();
				sumRemoto = peticion.getSummary();
				List<Operation> opsLocales = serverData.getLog().listNewer(sumRemoto);

				// enviamos al originador las operaciones que ha de actualizar
				for (Operation op : opsLocales) {
					out.writeObject(new MessageOperation(op));
				}
				// ========

				// ahora es nuestro turno de enviarle al originador
				// nuestros logs y ack que hemos capturado antes
				msg = new MessageAErequest(sumLocal, ackLocal);
				msg.setSessionNumber(current_session_number);
				out.writeObject(msg);
				lsim.log(Level.TRACE,
						"[TSAESessionPartnerSide] [session: " + current_session_number + "] sent message: " + msg);

				// receive operations
				// quedamos a la espera de que el originador nos envíe las operaciones
				// que nosotros tenemos que actualizar
				msg = (Message) in.readObject();
				lsim.log(Level.TRACE,
						"[TSAESessionPartnerSide] [session: " + current_session_number + "] received message: " + msg);

				// creamos un contenedor para almacenar las operaciones del originador
				List<MessageOperation> opsRemotas = new ArrayList<MessageOperation>();

				// mientras sigamos encontrando operaciones, las añadimos al contenedor
				while (msg.type() == MsgType.OPERATION) {
					// ========
					opsRemotas.add((MessageOperation) msg);
					// ========
					msg = (Message) in.readObject();
					lsim.log(Level.TRACE, "[TSAESessionPartnerSide] [session: " + current_session_number
							+ "] received message: " + msg);
				}

				// receive message to inform about the ending of the TSAE session
				if (msg.type() == MsgType.END_TSAE) {
					// send and "end of TSAE session" message
					msg = new MessageEndTSAE();
					msg.setSessionNumber(current_session_number);
					out.writeObject(msg);
					lsim.log(Level.TRACE,
							"[TSAESessionPartnerSide] [session: " + current_session_number + "] sent message: " + msg);

					// ========
					// una vez ha finalizado la sesión TSAE con éxito, actualizamos nuestras
					// operaciones
					synchronized (serverData) {
						for (MessageOperation op : opsRemotas) {
							serverData.recipeManager(op);
						}

						// y por último actualizamos summary y ack y purgamos log
						serverData.getSummary().updateMax(sumRemoto);
						serverData.getAck().updateMax(ackRemoto);
						serverData.getLog().purgeLog(serverData.getAck());
					}
				}
			}
			socket.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			lsim.log(Level.FATAL,
					"[TSAESessionPartnerSide] [session: " + current_session_number + "]" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
		}

		lsim.log(Level.TRACE, "[TSAESessionPartnerSide] [session: " + current_session_number + "] End TSAE session");
	}
}
