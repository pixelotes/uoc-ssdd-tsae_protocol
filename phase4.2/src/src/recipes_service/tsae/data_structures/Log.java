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

package recipes_service.tsae.data_structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.worker.LSimWorker;
import recipes_service.data.Operation;

/**
 * @author Joan-Manuel Marques, Daniel LÃ¡zaro Iglesias
 * December 2012
 *
 */
public class Log implements Serializable{
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -4864990265268259700L;
	/**
	 * This class implements a log, that stores the operations
	 * received  by a client.
	 * They are stored in a ConcurrentHashMap (a hash table),
	 * that stores a list of operations for each member of 
	 * the group.
	 */
	private ConcurrentHashMap<String, List<Operation>> log= new ConcurrentHashMap<String, List<Operation>>();  

	/**
	 * Constructor
	 * @param participants
	 */
	public Log(List<String> participants)
	{
        for (String participant : participants) 
        {
            log.put(participant, new Vector<Operation>());
        }
	}

	/**
	 * inserts an operation into the log. Operations are 
	 * inserted in order. If the last operation for 
	 * the user is not the previous operation than the one 
	 * being inserted, the insertion will fail.
	 * 
	 * @param op
	 * @return true if op is inserted, false otherwise.
	 */
	public synchronized boolean add(Operation op) {

		// si la operación es un nulo, salimos
		if (op==null) return false;
		
		// recuperamos la id del nodo
		String hostId = op.getTimestamp().getHostid();	
		
		// recuperamos la última operación local para dicho nodo y su timestamp
		List <Operation> listaLocal = log.get(hostId);
		
		// si no tenemos operaciones para ese nodo, almacenamos op
		if (listaLocal.isEmpty()) {
			log.get(hostId).add(op);
			return true;
		}
		
		// de lo contrario, cogemos la última operación almacenada y la comparamos con op
		else {
			Operation opLocal = log.get(hostId).get(log.get(hostId).size()-1);
			Timestamp TSLocal = opLocal.getTimestamp();
			
			// si el timestamp de op es más nuevo que el nuestro, añadimos op a nuestro log
			if( op.getTimestamp().compare(TSLocal) == 1 ) {
				log.get(hostId).add(op);
				return true;
			}
		}
		
		// si no se da ninguna de las condiciones anteriores, salimos con error
		return false;
	}
	
	
	/**
	 * Checks the received summary (sum) and determines the operations
	 * contained in the log that have not been seen by
	 * the proprietary of the summary.
	 * Returns them in an ordered list.
	 * @param sum
	 * @return list of operations
	 */
	public synchronized List<Operation> listNewer(TimestampVector sum) {

		// si el TSV sum es nulo, salimos
		if (sum==null) return null;
		
		// contenedor para almacenar resultados
		List<Operation> resultado = new ArrayList();
		
		// listado de id de nodos contenido en nuestro log
		// esto no compila en dslab
		//KeySetView<String, List<Operation>> nodos = this.log.keySet();
		//for (String nodo : nodos) {
		
		// recorremos todos los nodos del log
		for (String nodo : this.log.keySet()) {
			
			// para cada nodo, buscamos sus operaciones
			List<Operation> operaciones = this.log.get(nodo);
			
			// recorremos todas las operaciones del nodo
			for (Operation operacion : operaciones) {
				
				// por cada operación encontrada, comparamos con la más nueva del TSV sum
				if (operacion.getTimestamp().compare(sum.getLast(nodo)) > 0) {
					
					// las operaciones más nuevas que las de sum nos las guardamos en el
					// contenedor "resultado" para enviárselas a la otra parte de la sesión TSAE
					resultado.add(operacion);
				}
			}
		}
		
		// enviamos de vuelta las operaciones que no aparecen en el TSV sum
		return resultado;
	}
	
	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided
	 * ackSummary. 
	 * @param ack: ackSummary.
	 */
	public synchronized void purgeLog(TimestampMatrix ack) {
		
		// como siempre, si el param de la función es nulo, salimos
		if (ack == null) return;
		
		// vamos a comparar nuestro log con este TSV
		// que contiene los timestamps mínimos de ack
		TimestampVector TSVmin = ack.minTimestampVector();

		// recorremos nuestro log entrada a entrada
		for (Map.Entry<String, List<Operation>> entry : log.entrySet()) {
    	  
			// recopilamos las operaciones de cada entrada
			List<Operation> operaciones = entry.getValue();
			String nodo = entry.getKey();
			int numOps = operaciones.size();
          
			//if (numOps > 0) 
			//  System.out.println("[Log.purgeLog] Se han encontrado "+ numOps +" operaciones del nodo " + nodo);
          
			// obtenemos el último timestamp del nodo
			Timestamp lastTS = TSVmin.getLast(nodo);
          
			if (lastTS != null) {
				// recorremos hacia atrás las operaciones
				for (int i = numOps - 1; i >= 0; i--) {
            	  
            	 // recuperamos la operación
                 Operation op = operaciones.get(i);
                  
                 // comparamos y eliminamos las que son más antiguas que las del log
                 if (op.getTimestamp().compare(lastTS) <= 0) operaciones.remove(i);
                  
            }
        }
		
		}
	}

	/**
	 * equals
	 */
    @Override
    public boolean equals(Object obj) {
    	//comprobamos que obj exista y sea de la clase que queremos
		if (obj == null || obj.getClass() != this.getClass()) return false;	
		
		//definimos nombres para facilitar debug
		Log logLocal = this;
		Log logRemoto = (Log) obj;
		
		//comprobamos que los logs no sean nulos
		if(logLocal == null || logRemoto == null) return false;
		
		//si los dos logs son iguales, devolvemos true
		if(logLocal.log == logRemoto.log) return true;
		
		//si no se cumple nada de lo anterior
		return this.log.equals(logRemoto.log);
    }


	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String name="";
		for(Enumeration<List<Operation>> en=log.elements();
		en.hasMoreElements(); ){
		List<Operation> sublog=en.nextElement();
		for(ListIterator<Operation> en2=sublog.listIterator(); en2.hasNext();){
			name+=en2.next().toString()+"\n";
		}
	}
		
		return name;
	}
}