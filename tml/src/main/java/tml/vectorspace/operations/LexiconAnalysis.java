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

import tml.corpus.Corpus.PassageFreqs;
import tml.vectorspace.operations.results.LexiconAnalysisResult;


/**
 * LexiconAnalysis returns the accumulated lexicon per passage used in the document. It
 * is important to consider that stopwords are removed and words that are kept are stemmed,
 * therefore this doesn't correspond to the actual total number of different words.
 * 
 * @author Jorge Villalon
 *
 */
public class LexiconAnalysis extends AbstractOperation<LexiconAnalysisResult> {

	/**
	 * @param corpus
	 */
	public LexiconAnalysis() {
		this.name = "Lexicon analysis";
	}

	@Override
	public void start() throws Exception {
		super.start();

		this.results = new ArrayList<LexiconAnalysisResult>();

		List<String> list = new ArrayList<String>();

		for (int i=0; i<corpus.getPassageFrequencies().length; i++) {
			PassageFreqs freqs = corpus.getPassageFrequencies()[i];
			for (int j=0; j<freqs.getTermsIndices().length; j++) {
				String term = corpus.getTerms()[freqs.getTermsIndices()[j]];
				if (!list.contains(term))
					list.add(term);
			}

			LexiconAnalysisResult result = new LexiconAnalysisResult();
			result.setDocument(corpus.getPassages()[i]);
			result.setTerms(freqs.getTermsIndices().length);
			result.setNewTerms(list.size());
			this.results.add(result);
		}

		super.end();
	}
}
