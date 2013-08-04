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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jorge Villalon
 *
 */
public class PassageClusteringLingoResult extends AbstractResult {

	int cluster;
	String clusterPhrase;
	List<String> documents;

	/**
	 * @return the cluster
	 */
	public int getCluster() {
		return cluster;
	}

	/**
	 * @param cluster the cluster to set
	 */
	public void setCluster(int cluster) {
		this.cluster = cluster;
	}

	/**
	 * @return the clusterPhrase
	 */
	public String getClusterPhrase() {
		return clusterPhrase;
	}

	/**
	 * @param clusterPhrase the clusterPhrase to set
	 */
	public void setClusterPhrase(String clusterPhrase) {
		this.clusterPhrase = clusterPhrase;
	}

	/**
	 * @return the documents
	 */
	public List<String> getDocuments() {
		if(documents == null)
			documents = new ArrayList<String>();
		return documents;
	}

	/**
	 * @param documents the documents to set
	 */
	public void setDocuments(List<String> documents) {
		this.documents = documents;
	}

	@Override
	public String toString() {
		return this.clusterPhrase + " [" + this.getDocuments().size() + "]";
	}
}
