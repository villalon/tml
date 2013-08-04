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
/**
 * 
 */
package tml.vectorspace.operations;

import java.util.Collections;
import java.util.Comparator;

import tml.utils.MatrixUtils;
import tml.vectorspace.operations.results.PassageSimilarityResult;

import Jama.Matrix;

/**
 * This operation calculates the similarity between all documents within
 * a {@link Corpus}, it can calculate the similarities based on its own
 * {@link SemanticSpace}, or use another {@link Corpus} to project all documents
 * and the calculate similarities between all documents in both corpora.
 * 
 * @author Jorge Villalon
 *
 */
public class PassagesSimilarity extends AbstractOperation<PassageSimilarityResult> {

	private boolean includeBackgroundInSimilarity = false;
	private boolean includeBackgroundInResults = false;
	private boolean sortBySimilarity = false;
	
	public PassagesSimilarity() {
		this.name = "Passages similarity";
	}
	/**
	 * @return the sortBySimilarity
	 */
	public boolean isSortBySimilarity() {
		return sortBySimilarity;
	}

	/**
	 * @param sortBySimilarity the sortBySimilarity to set
	 */
	public void setSortBySimilarity(boolean sortBySimilarity) {
		this.sortBySimilarity = sortBySimilarity;
	}

	private Matrix similarities;

	/**
	 * @return the similarities
	 */
	public Matrix getSimilarities() {
		return similarities;
	}

	/**
	 * @param includeBackgroundInResults the includeBackgroundInResults to set
	 */
	public void setIncludeBackgroundInResults(boolean includeBackgroundInResults) {
		this.includeBackgroundInResults = includeBackgroundInResults;
	}

	/**
	 * @param includeBackgroundInSimilarity the includeBackgroundInSimilarity to set
	 */
	public void setIncludeBackgroundInSimilarity(
			boolean includeBackgroundInSimilarity) {
		this.includeBackgroundInSimilarity = includeBackgroundInSimilarity;
	}

	@Override
	public void start() throws Exception {
		super.start();

		if(!this.includeBackgroundInSimilarity) {
			fillResultsFromSpace(
					this.corpus.getSemanticSpace().getSk(), 
					this.corpus.getSemanticSpace().getVk(), 
					this.corpus.getPassages());
		} else {
			Matrix v = this.corpus.getSemanticSpace().getVk();
			Matrix vv = this.backgroundKnowledge.getSemanticSpace().getVk();
			int docs = v.getRowDimension() + vv.getRowDimension();
			int terms = v.getColumnDimension();
			Matrix newV = new Matrix(docs, terms);
			newV.setMatrix(0,v.getRowDimension()-1,0,terms-1,v);
			newV.setMatrix(v.getRowDimension(),docs-1,0,terms-1,vv);

			// The new array of passages
			String[] passages = new String[docs];
			for(int i=0;i<this.corpus.getPassages().length;i++)
				passages[i] = this.corpus.getPassages()[i];
			for(int i=this.corpus.getPassages().length;i<docs;i++)
				passages[i] = backgroundKnowledge.getPassages()[i-this.corpus.getPassages().length];

			fillResultsFromSpace(
					this.corpus.getSemanticSpace().getSk(),
					newV,
					passages);
		}
		
		if(this.isSortBySimilarity()) {
			Collections.sort(this.results, new Comparator<PassageSimilarityResult>() {
				@Override
				public int compare(PassageSimilarityResult o1,
						PassageSimilarityResult o2) {
					return (int) ((-o1.getSimilarity() * 10E9 + o2.getSimilarity() * 10E9));
				}
				
			});			
		}
		super.end();
	}

	private void fillResultsFromSpace(Matrix S, Matrix V, String[] passages) {
		// The distances between documents is calculated using V
		// First, V is scaled by S cause LSA works like that (check Deerwester 1990 
		// and Beery and Dumais 1994).
		similarities = V.times(S);
		
		// Second, normalize the distances otherwise we won't get 1 for exactly
		// the same documents.
		similarities = MatrixUtils.normalizeRows(similarities);
		
		// Finally, the all with all comparison is made.
		similarities = similarities.times(similarities.transpose());
		int totalDocsA = similarities.getColumnDimension();
		int totalDocsB = similarities.getColumnDimension();
		if(!this.includeBackgroundInResults) {
			totalDocsA = this.corpus.getPassages().length;
		}
		for(int docA=0;docA<totalDocsA;docA++)
			for(int docB=docA;docB<totalDocsB;docB++) {
				PassageSimilarityResult result = new PassageSimilarityResult();
				result.setDocumentA(passages[docA]);
				result.setDocumentB(passages[docB]);
				result.setSimilarity(similarities.get(docA, docB));
				this.results.add(result);
			}
	}
}
