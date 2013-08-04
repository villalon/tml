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
import java.util.List;

import tml.vectorspace.operations.results.TermRankedResult;
import tml.vectorspace.operations.results.TermsExtractionSummarizationResult;


/**
 * Concept Extraction operation based on CMM.
 * @author Jorge Villalon
 *
 */
public class ConceptExtraction extends TermExtractionSummarization {

	List<TermsExtractionSummarizationResult> newResults = 
		new ArrayList<TermsExtractionSummarizationResult>();
	
	public ConceptExtraction() {
		this.name = "Concept extraction";
	}

	@Override
	public void start() throws Exception {
		this.maxResults = 35;
		super.start();

		logger.info("Originally " + this.results.size() + " results");
		
		CompoundNounsSummarized op = new CompoundNounsSummarized();
		op.setCorpus(corpus);
		op.start();

		// TODO: Iterate through compound nouns
		for (TermRankedResult result : op.getResults()) {
			String noun = result.getTerm();
			if (noun.trim().length() == 0)
				continue;
			TermsExtractionSummarizationResult newResult = new TermsExtractionSummarizationResult();
			newResult.setTerm(noun);
			newResults.add(newResult);
		}

		this.results.clear();
		this.results.addAll(newResults);
	}
}
