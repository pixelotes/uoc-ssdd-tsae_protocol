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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.uoc.dpcs.lsim.LSimFactory;
import lsim.worker.LSimWorker;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class TimestampVector implements Serializable{
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -765026247959198886L;
	/**
	 * This class stores a summary of the timestamps seen by a node.
	 * For each node, stores the timestamp of the last received operation.
	 */
	
	private ConcurrentHashMap<String, Timestamp> timestampVector= new ConcurrentHashMap<String, Timestamp>();
	
	/**
	 * Constructor
	 * @param participants
	 */
	public TimestampVector (List<String> participants)
	{
        for (String participant : participants) 
        {
            // when sequence number of timestamp < 0 it means that the timestamp is the null timestamp
            timestampVector.put(participant, new Timestamp(participant, Timestamp.NULL_TIMESTAMP_SEQ_NUMBER));
        }
		
	}
	
	// Contructor que sirve para hacer la clonacion
    private TimestampVector(Map<String, Timestamp> timestampVector) 
    {
        this.timestampVector = new ConcurrentHashMap<String, Timestamp>(timestampVector);
    }


	/**
	 * Updates the timestamp vector with a new timestamp. 
	 * @param timestamp
	 */
    public synchronized void updateTimestamp(Timestamp timestamp) {
    	
    	// comprobamos que no nos pasen un timestamp vacío por error
        if (timestamp != null) {	
        	
        	String hostId = timestamp.getHostid();
        	
        	// sustituímos el timestamp que tenemos de un host determinado
        	// con el timestamp que nos han pasado como parámetro
            this.timestampVector.replace(hostId, timestamp);
        }
    }
	
	/**
	 * merge in another vector, taking the elementwise maximum
	 * @param tsVector (a timestamp vector)
	 */
	public synchronized void updateMax(TimestampVector tsVector) {

		// comprobamos que no nos hayan pasado un tsVector nulo por error
		if (tsVector == null) return;

		// esto no compila en dslab
		//KeySetView<String, Timestamp> nodos = this.timestampVector.keySet();
		// for (String nodo : nodos) {
		
		for (String nodo : this.timestampVector.keySet()) {

			// variables que vamos a necesitar
			Timestamp tsRemoto = tsVector.getLast(nodo); // timestamp remoto para el nodo
			Timestamp tsLocal = this.getLast(nodo); // nuestro timestamp para el nodo
			String idNodo = tsLocal.getHostid(); // id del nodo
			
			// por si acaso comprobarmos que el timestamp remoto del nodo
			// que estamos evaluando no sea nulo
			if (tsRemoto != null) {
				
				// realizamos la comparación, y nos devuelve un valor
				long comparacion = tsLocal.compare(tsRemoto);
				
				// si el tsRemoto es más nuevo que el nuestro nos lo guardamos
				if (comparacion < 0) this.timestampVector.replace(idNodo, tsRemoto);
			}		
		}
	}
	
	/**
	 * 
	 * @param node
	 * @return the last timestamp issued by node that has been
	 * received.
	 */
	public synchronized Timestamp getLast(String node) {
		
		// buscamos el último timestamp que tenemos almacenado del nodo buscado
		Timestamp lastTS = this.timestampVector.get(node);
		
		//if (lastTS==null) System.out.println("[TSV.getLast] - ¡OJO! El último TS del nodo " + node + " tiene un valor nulo.");
		
		// devolvemos el valor
		return lastTS;
	}
	
	/**
	 * merges local timestamp vector with tsVector timestamp vector taking
	 * the smallest timestamp for each node.
	 * After merging, local node will have the smallest timestamp for each node.
	 *  @param tsVector (timestamp vector)
	 */
	public synchronized void mergeMin(TimestampVector tsVector) {
        
		// comprobamos que no nos hayan pasado un tsVector nulo por error
		if (tsVector == null) return;
		
		// esto no compila en dslab
		//KeySetView<String, Timestamp> nodos = tsVector.timestampVector.keySet();
		//for (String nodo : nodos) {
		
		// recorremos el timestampvector que tenemos de todos los nodos
		for (String nodo : tsVector.timestampVector.keySet()) {
			
			// variables que vamos a necesitas
			Timestamp tsRemoto = tsVector.timestampVector.get(nodo); // timestamp remoto para el nodo
			Timestamp tsLocal = this.timestampVector.get(nodo); // nuestro timestamp para el nodo
			String idNodo = tsLocal.getHostid(); // id del nodo
			
			// por si acaso comprobarmos que el timestamp remoto del nodo
			// que estamos evaluando no sea nulo
			if (tsRemoto != null) {
				
				// realizamos la comparación, y nos devuelve un valor
				long comparacion = tsLocal.compare(tsRemoto);
				
				// si el tsRemoto es más pequeño que el nuestro nos lo guardamos
				if(comparacion > 0) timestampVector.put(idNodo, tsRemoto);
			}	
		}
	}
	
	/**
	 * clone
	 */
    @Override
    public synchronized TimestampVector clone() {
    	
    	// obtenemos la lesta de participantes del TSV local
		List<String> participantes = new ArrayList(this.timestampVector.keySet());

		// creamos un nuevo TSV con el constructor. Obtenemos un TSV vacío.
		//List<String> participantes = new ArrayList();
		TimestampVector clon = new TimestampVector(participantes);

		// rellenamos el clon del TSV con los datos del TSV local
		for (String nodo : timestampVector.keySet()) {
			clon.timestampVector.put(nodo, timestampVector.get(nodo));
		}

		// devolvemos nuestra copia del TimestampVector
		return clon;
    }
	

	/**
	 * equals
	 */
	public synchronized boolean equals(Object obj){

		//comprobamos que obj exista y sea de la clase que queremos
		if (obj == null || obj.getClass() != this.getClass()) return false;	
		
		//definimos nombres para facilitar debug
		TimestampVector tsvLocal = this;
		TimestampVector tsvRemoto = (TimestampVector) obj;
		
		//comprobamos que los tsv no sean nulos
		if(tsvLocal == null || tsvRemoto == null) return false;
		
		//si los dos tsv son iguales, devolvemos true
		if(tsvLocal == tsvRemoto) return true;
		
		//en cualquier otro caso, devolvemos el resultado de la comparación
		return this.timestampVector.equals(tsvRemoto.timestampVector);
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all="";
		if(timestampVector==null){
			return all;
		}
		for(Enumeration<String> en=timestampVector.keys(); en.hasMoreElements();){
			String name=en.nextElement();
			if(timestampVector.get(name)!=null)
				all+=timestampVector.get(name)+"\n";
		}
		return all;
	}
}