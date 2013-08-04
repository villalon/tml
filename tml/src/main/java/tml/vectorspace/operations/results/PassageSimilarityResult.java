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

public class PassageSimilarityResult extends AbstractResult {

	String documentA;
	String documentB;
	double similarity;
	
	/**
	 * @return the documentA
	 */
	public String getDocumentA() {
		return documentA;
	}
	/**
	 * @return the documentB
	 */
	public String getDocumentB() {
		return documentB;
	}
	/**
	 * @return the similarity
	 */
	public double getSimilarity() {
		return similarity;
	}
	/**
	 * @param documentA the documentA to set
	 */
	public void setDocumentA(String documentA) {
		this.documentA = documentA;
	}
	/**
	 * @param documentB the documentB to set
	 */
	public void setDocumentB(String documentB) {
		this.documentB = documentB;
	}
	/**
	 * @param similarity the similarity to set
	 */
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
}
