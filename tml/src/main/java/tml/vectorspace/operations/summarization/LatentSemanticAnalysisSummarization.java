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
package tml.vectorspace.operations.summarization;

import tml.corpus.Corpus;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import Jama.Matrix;

/**
 * 
 * LSA based summarization using Steiberger's formula from:
 * INPROCEEDINGS{
 *  author = {Josef Steinberger and Karel Jezek},
 *  title = {Using Latent Semantic Analysis in Text Summarization and Summary Evaluation},
 *  booktitle = {Proceedings of the 7th International Conference ISIM},
 *  year = {2004}
 *  }
 *  
 * @author Jorge Villalon
 *
 */
public class LatentSemanticAnalysisSummarization extends
AbstractSummarizationOperation implements SummarizationOperation {

	private Matrix Vk = null;
	private Matrix Sk = null;
	private Matrix Uk = null;

	public LatentSemanticAnalysisSummarization() {
		this.name = "LSA";
	}
	
	@Override
	public void setCorpus(Corpus corpus) {
		super.setCorpus(corpus);
		
		if(corpus == null)
			return;
		
		if(!this.corpus.getSemanticSpace().isCalculated()) {			
			try {
				this.corpus.getSemanticSpace().calculate();
			} catch (NotEnoughTermsInCorpusException e) {
				logger.error(e);
				super.setCorpus(null);
				return;
			}
		}
		
		// Reminder! Vk is transposed in SVD so Vk is docs by dimensions
		this.Vk = this.corpus.getSemanticSpace().getVk().copy();
		this.Uk = this.corpus.getSemanticSpace().getUk().copy();
		
		// The variance corresponds to the squared eigenvalues, so we square S
		this.Sk = this.corpus.getSemanticSpace().getSk().copy();		
		this.Sk = this.Sk.times(this.Sk);
	}
	
	@Override
	protected double calculatePassageLoading(int doc) {
		double total = 0;
		for(int dim =0; dim<Vk.getColumnDimension(); dim++)
			total += Math.pow(Vk.get(doc, dim),2) * Sk.get(dim, dim);
		return Math.sqrt(total); 
	}

	@Override
	protected double calculateTermLoading(int term) {
		double total = 0;
		for(int dim =0; dim<Uk.getColumnDimension(); dim++)
			total += Math.pow(Uk.get(term, dim),2) * Sk.get(dim, dim);
		return Math.sqrt(total); 
	}
}
