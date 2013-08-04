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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tml.vectorspace.operations.AbstractOperation;
import tml.vectorspace.operations.results.Summary;


public abstract class AbstractSummarizationOperation extends AbstractOperation<Summary> implements
SummarizationOperation {

	private class SummaryResult {
		private int documentIndex;
		public SummaryResult(int documentIndex, double documentLoad) {
			super();
			this.documentIndex = documentIndex;
			this.documentLoad = documentLoad;
		}
		private double documentLoad;
	}

	@Override
	public void start() throws Exception {
		super.start();

		this.maxResults = 1;

		Comparator<SummaryResult> comparator = new Comparator<SummaryResult>() {
			@Override
			public int compare(SummaryResult o1, SummaryResult o2) {
				return (int) (o2.documentLoad * 100 - o1.documentLoad * 100);
			}			
		};
		
		// Starting from the biggest eigenvector
		List<SummaryResult> passagesMap = new ArrayList<SummaryResult>();

		for(int doc = 0; doc < this.corpus.getPassages().length; doc++) {
			double value = calculatePassageLoading(doc); 
			passagesMap.add(new SummaryResult(doc, value));
		}
		
		Collections.sort(passagesMap, comparator);
		
		List<SummaryResult> termsMap = new ArrayList<SummaryResult>();
		
		for(int term = 0; term < this.corpus.getTerms().length; term++) {
			double value = calculateTermLoading(term); 
			termsMap.add(new SummaryResult(term, value));
		}
		
		Collections.sort(termsMap, comparator); 

		int[] passagesIndices = new int[passagesMap.size()];
		double[] passagesLoads = new double[passagesMap.size()];
		for(int i=0; i<passagesMap.size(); i++) {
			passagesIndices[i] = passagesMap.get(i).documentIndex;
			passagesLoads[i] = passagesMap.get(i).documentLoad;
		}
		
		int[] termsIndices = new int[termsMap.size()];
		double[] termsLoads = new double[termsMap.size()];
		for(int i=0; i<termsMap.size(); i++) {
			termsIndices[i] = termsMap.get(i).documentIndex;
			termsLoads[i] = termsMap.get(i).documentLoad;
		}
		
		Summary summary = new Summary();
		summary.setPassagesLoads(passagesLoads);
		summary.setPassagesRank(passagesIndices);
		summary.setTermsLoads(termsLoads);
		summary.setTermsRank(termsIndices);
		
		this.results.add(summary);

		super.end();
	}

	protected abstract double calculatePassageLoading(int doc);
	protected abstract double calculateTermLoading(int term);
}
