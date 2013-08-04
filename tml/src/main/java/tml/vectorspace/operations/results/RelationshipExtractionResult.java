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
 * @author Jorge Villalon
 * 
 */
public class RelationshipExtractionResult extends AbstractResult {

	String conceptA;
	String conceptB;
	String linkingWord;

	private boolean directed;

	/**
	 * @return the first concept in the relationship
	 */
	public String getConceptA() {
		return conceptA;
	}

	/**
	 * @return the second concept in the relationship
	 */
	public String getConceptB() {
		return conceptB;
	}

	/**
	 * @return the linking word for the relationship
	 */
	public String getLinkingWord() {
		return linkingWord;
	}

	/**
	 * @return if the relationship is directed (from A to B) or non-directed
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @param conceptA
	 *            the first concept
	 */
	public void setConceptA(String conceptA) {
		this.conceptA = conceptA;
	}

	/**
	 * @param conceptB
	 *            the second concept
	 */
	public void setConceptB(String conceptB) {
		this.conceptB = conceptB;
	}

	/**
	 * @param directed
	 *            if A points to B
	 */
	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	/**
	 * @param linkingWord
	 *            the linking word/phrase
	 */
	public void setLinkingWord(String linkingWord) {
		this.linkingWord = linkingWord;
	}

}
