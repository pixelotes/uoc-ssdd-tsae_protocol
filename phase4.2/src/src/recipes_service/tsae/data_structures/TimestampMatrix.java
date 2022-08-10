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
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.uoc.dpcs.lsim.LSimFactory;
import lsim.worker.LSimWorker;

/**
 * @author Joan-Manuel Marques, Daniel LÃ¡zaro Iglesias
 * December 2012
 *
 */
public class TimestampMatrix implements Serializable{
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();
	
	private static final long serialVersionUID = 3331148113387926667L;
	ConcurrentHashMap<String, TimestampVector> timestampMatrix = new ConcurrentHashMap<String, TimestampVector>();
	
	/**
	 * Constructor
	 * @param participants
	 */
	public TimestampMatrix(List<String> participants)
	{
        for (String participant : participants) 
        {
            timestampMatrix.put(participant, new TimestampVector(participants));
        }	
	}
	
	/**
	 * Contructor que sirve para hacer la clonacion 
	 * @param timestampMatrix
	 */
	private TimestampMatrix(Map<String, TimestampVector> timestampMatrix)
	{
		this.timestampMatrix = new ConcurrentHashMap<String, TimestampVector>(timestampMatrix);
	}
	
	/**
	 * Not private for testing purposes.
	 * @param node
	 * @return the timestamp vector of node in this timestamp matrix
	 */
	public synchronized TimestampVector getTimestampVector(String node) {
		
		// buscamos el timestampvector correspondiente al nodo
		TimestampVector tsvLocal = this.timestampMatrix.get(node);

		// devolvemos el TSV que hemos recuperado
		return tsvLocal;
	}
	
	/**
	 * Merges two timestamp matrix taking the elementwise maximum
	 * @param tsMatrix
	 */
	public synchronized void updateMax(TimestampMatrix tsMatrix) {
		
		// comprobamos que no nos hayan pasado un tsMatrix nulo por error
        if (tsMatrix == null) return;
        
        // recuperamos los TSV de la matriz TSV
        Set<Entry<String, TimestampVector>> elementos = tsMatrix.timestampMatrix.entrySet();
        
        // recorremos la matriz que nos han pasado como parámetro
        for (Entry<String, TimestampVector> elemento : elementos) {
        	
        	// recuperamos los timestamps
            TimestampVector tsvRemoto = elemento.getValue();
            TimestampVector tsvLocal = this.timestampMatrix.get(elemento.getKey());
            
            // miramos que los TSV no sean nulos
            if (tsvLocal == null || tsvRemoto == null) return;
            
            // realizamos la comparación
            tsvLocal.updateMax(tsvRemoto);
            
        }
	}
	
	/**
	 * substitutes current timestamp vector of node for tsVector
	 * @param node
	 * @param tsVector
	 */
	public synchronized  void update(String node, TimestampVector tsVector) {
		
		// si el nodo o el TSV son nulos, salimos
		if(node==null || tsVector==null) return;
		
		// recuperamos el TSV local para el nodo que buscamos
		TimestampVector tsvLocal = this.timestampMatrix.get(node);
		
		//si no hemos encontrado un TSV para el nodo, copiamos el del TSV externo
		if (tsvLocal == null) this.timestampMatrix.put(node, tsVector);
		
		//en caso contrario, reemplazamos el que tenemos
		else this.timestampMatrix.replace(node, tsVector);
	}
	
	/**
	 * 
	 * @return a timestamp vector containing, for each node, 
	 * the timestamp known by all participants
	 */
	public synchronized TimestampVector minTimestampVector() {
		Collection<TimestampVector> vectores = this.timestampMatrix.values();
		
		// lo ideal sería darle el valor del primer TSV de vectores, pero en Java no se
		// puede acceder directamente a una posición concreta de una colección sin iterarla
		TimestampVector tsvMinimo = null;

        // iteramos la colección de TSV que hemos llamado vectores
        for (TimestampVector vector : vectores) {

            if (tsvMinimo != null) tsvMinimo.mergeMin(vector); 
            else tsvMinimo = vector.clone(); //damos valor la primera vez a minTSV, no queremos nulos!
        }
                
        // devolvemos el valor
        return tsvMinimo;
	}
	
	/**
	 * clone
	 */
	public synchronized TimestampMatrix clone() {
		
		// obtenemos los nodos participantes de la TSM local
		List<String> participantes = new ArrayList<String>(timestampMatrix.keySet());
			
		// cremos una TSM con el constructor, pero sus TSV estarán vacíos
		TimestampMatrix clon = new TimestampMatrix(participantes);
			
		// recorremos la TSM local y recuperamos los TSV de cada clave			
		for (String nodo : timestampMatrix.keySet()) {	
			TimestampVector tsv = timestampMatrix.get(nodo);
			clon.update(nodo, tsv);
		}	
			
		// devolvemos la copia de nuestro TimestampMatrix
		return clon;
	}
	
	/**
	 * equals
	 */
	@Override
    public synchronized boolean equals(Object obj) {

    	//comprobamos que obj exista y sea de la clase que queremos
		if (obj == null || obj.getClass() != this.getClass()) return false;	
		
		//definimos nombres para facilitar debug
		TimestampMatrix tsmLocal = this;
		TimestampMatrix tsmRemota = (TimestampMatrix) obj;
		
		//comprobamos que los logs no sean nulos
		if(tsmLocal == null || tsmRemota == null) return false;
		
		//si los dos logs son iguales, devolvemos true
		if(tsmLocal.timestampMatrix == tsmRemota.timestampMatrix) return true;
		
		//si no se cumple nada de lo anterior
		return this.timestampMatrix.equals(tsmRemota.timestampMatrix);
    }

	
	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all="";
		if(timestampMatrix==null){
			return all;
		}
		for(Enumeration<String> en=timestampMatrix.keys(); en.hasMoreElements();){
			String name=en.nextElement();
			if(timestampMatrix.get(name)!=null)
				all+=name+":   "+timestampMatrix.get(name)+"\n";
		}
		return all;
	}
}