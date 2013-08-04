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
 * Represents a passage (document, paragraph or sentence), its load
 * and the corresponding eigenvector by which it was selected.
 * 
 * @author Jorge Villalon
 *
 */
public class PassageExtractionSummarizationResult extends AbstractResult {
	int eigenVectorIndex;

	double load;

	String textPassageContent;

	int textPassageId;

	/**
	 * @return the eigenVectorIndex
	 */
	public int getEigenVectorIndex() {
		return eigenVectorIndex;
	}

	/**
	 * @param eigenVectorIndex the eigenVectorIndex to set
	 */
	public void setEigenVectorIndex(int eigenVectorIndex) {
		this.eigenVectorIndex = eigenVectorIndex;
	}

	/**
	 * @return the load
	 */
	public double getLoad() {
		return load;
	}

	/**
	 * @param load the load to set
	 */
	public void setLoad(double load) {
		this.load = load;
	}

	/**
	 * @return the textPassageContent
	 */
	public String getTextPassageContent() {
		return textPassageContent;
	}

	/**
	 * @param textPassageContent the textPassageContent to set
	 */
	public void setTextPassageContent(String textPassageContent) {
		this.textPassageContent = textPassageContent;
	}

	/**
	 * @return the textPassageId
	 */
	public int getTextPassageId() {
		return textPassageId;
	}

	/**
	 * @param textPassageId the textPassageId to set
	 */
	public void setTextPassageId(int textPassageId) {
		this.textPassageId = textPassageId;
	}
}
