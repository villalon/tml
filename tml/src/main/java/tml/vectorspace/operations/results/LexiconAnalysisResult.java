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
 * The result of a {@link LexiconAnalysis} operation.
 * @author Jorge Villalon
 *
 */
public class LexiconAnalysisResult extends AbstractResult {
	String document;
	int terms;
	int newTerms;

	/**
	 * @return the document
	 */
	public String getDocument() {
		return document;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(String document) {
		this.document = document;
	}

	/**
	 * @return the terms
	 */
	public int getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	public void setTerms(int terms) {
		this.terms = terms;
	}

	/**
	 * @return the newTerms
	 */
	public int getNewTerms() {
		return newTerms;
	}

	/**
	 * @param newTerms the newTerms to set
	 */
	public void setNewTerms(int newTerms) {
		this.newTerms = newTerms;
	}

	@Override
	public String toString() {
		return "Document: " + this.getDocument() + " Terms: " + this.terms
				+ " Accumulated: " + this.newTerms;
	}
}
