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
 * This class represents the result of a {@link TermExtractionSummarization} operation. It
 * represents a {@link Term} in the {@link Corpus}, with its corresponding
 * eigenvector and loading.
 * 
 * @author Jorge Villalon
 *
 */
public class TermsExtractionSummarizationResult extends AbstractResult {
	String term;
	int termId;
	double load;
	double variance;
	int eigenVectorIndex;

	/**
	 * @return the position of the eigenvector (relative importance)
	 */
	public int getEigenVectorIndex() {
		return eigenVectorIndex;
	}

	/**
	 * @param eigenVectorIndex the position of the eigenvector
	 */
	public void setEigenVectorIndex(int eigenVectorIndex) {
		this.eigenVectorIndex = eigenVectorIndex;
	}

	/**
	 * @return the load of the term in the eigenvector
	 */
	public double getLoad() {
		return load;
	}

	/**
	 * @param load the load of the term in the eigenvector
	 */
	public void setLoad(double load) {
		this.load = load;
	}

	/**
	 * @return the textual representation of the term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * @param sentence the textual representation of the term
	 */
	public void setTerm(String sentence) {
		this.term = sentence;
	}

	/**
	 * @return the id of the term
	 */
	public int getTermId() {
		return termId;
	}

	/**
	 * @param termId the id of the term
	 */
	public void setTermId(int termId) {
		this.termId = termId;
	}

	/**
	 * @return the variance corresponding to the eigenvector
	 */
	public double getVariance() {
		return variance;
	}

	/**
	 * @param variance the variance corresponding to the eigenvector
	 */
	public void setVariance(double variance) {
		this.variance = variance;
	}
}
