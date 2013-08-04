/*******************************************************************************
 *  Copyright 2007, 2009 Jorge Villalon (jorge.villalon@uai.cl)
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0 
 *  	
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 *******************************************************************************/

package tml.vectorspace.operations.results;

/**
 * Results that represent the distances between two passages.
 * 
 * @author Jorge Villalon
 *
 */
public class PassageDistancesResult extends AbstractResult {
	int documentAId;
	int documentBId;
	double distance;
	/**
	 * @return the documentAId
	 */
	public int getDocumentAId() {
		return documentAId;
	}
	/**
	 * @param documentAId the documentAId to set
	 */
	public void setDocumentAId(int documentAId) {
		this.documentAId = documentAId;
	}
	/**
	 * @return the documentBId
	 */
	public int getDocumentBId() {
		return documentBId;
	}
	/**
	 * @param documentBId the documentBId to set
	 */
	public void setDocumentBId(int documentBId) {
		this.documentBId = documentBId;
	}
	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}
	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}
}
