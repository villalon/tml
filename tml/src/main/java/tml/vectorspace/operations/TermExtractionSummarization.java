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

package tml.vectorspace.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tml.vectorspace.operations.results.TermsExtractionSummarizationResult;

import Jama.Matrix;

/**
 * This operation extracts the key terms from the corpus, ranked by their
 * importance on explaining the variance.
 * 
 * @author Jorge Villalon
 *
 */
public class TermExtractionSummarization extends AbstractOperation<TermsExtractionSummarizationResult> {

	/**
	 * Creates a new instance of {@link TermExtractionSummarization}
	 */
	public TermExtractionSummarization() {
		this.name = "Terms extraction";
	}

	@Override
	public void start() throws Exception {

		super.start();

		this.results = new ArrayList<TermsExtractionSummarizationResult>();

		Matrix eigenVectors = this.corpus.getSemanticSpace()
		.getUk();
		double[] eigenValues = this.corpus.getSemanticSpace()
		.getSk().getColumnPackedCopy();

		logger.debug("rows:" + eigenVectors.getRowDimension() + " columns:"
				+ eigenVectors.getColumnDimension());

		for (int i = 0; i < eigenVectors.getRowDimension(); i++) {

			String term = this.corpus.getTerms()[i];

			double maxTermWeight = 0;
			int termIndex = 0;

			for (int j = 0; j < eigenVectors.getColumnDimension(); j++) {
				double eigenvalue = 0;
				if (j < eigenValues.length)
					eigenvalue = eigenValues[j];

				double termWeight = Math.abs(eigenVectors.get(i, j)
						* eigenvalue);
				if (maxTermWeight < termWeight) {
					maxTermWeight = termWeight;
					termIndex = j;
				}
			}

			logger.debug("Inserting term " + term + " with key " + maxTermWeight);
			TermsExtractionSummarizationResult result = new TermsExtractionSummarizationResult();
			result.setEigenVectorIndex(termIndex);
			result.setLoad(maxTermWeight);
			result.setTerm(term);
			this.results.add(result);
		}

		Collections.sort(this.results, new Comparator<TermsExtractionSummarizationResult>() {

			@Override
			public int compare(TermsExtractionSummarizationResult o1, TermsExtractionSummarizationResult o2) {
				if (o1.getLoad() == o2.getLoad()) {
					return 1;
				}
				return (int) (o2.getLoad() * 100 - o1.getLoad() * 100);
			}
		});
		
		// If we have a maximum number of results, we delete others
		if(this.maxResults > 0) {
			int toRemove = this.results.size() - this.maxResults;

			while (toRemove > 0) {
				this.results.remove(0);
				toRemove--;
			}
		}

		super.end();
	}

}
