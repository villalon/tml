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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tml.annotators.PennTreeAnnotator;
import tml.utils.StanfordUtils;
import tml.vectorspace.operations.results.TermRankedResult;

import edu.stanford.nlp.trees.Tree;

public class CompoundNounsSummarized extends AbstractOperation<TermRankedResult> implements
Operation<TermRankedResult> {

	public CompoundNounsSummarized() {
		this.name = "Compound nounds summarized";
	}
	
	@Override
	public void start() throws Exception {
		super.start();

		List<String> nouns = new ArrayList<String>();
		for(String passageId : corpus.getPassages()) {
			String annotation = null;
			try {
				annotation = this.repository.getDocumentField(passageId, PennTreeAnnotator.FIELD_NAME);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			} 
			if(annotation != null) {
				Tree pennTree = StanfordUtils.getTreeFromString(passageId, annotation);
				List<String> allNouns = StanfordUtils.extractNouns(pennTree);
				if(allNouns != null)
					for(String noun : allNouns) {
						noun = noun.toLowerCase();
						if(!nouns.contains(noun)) {
							nouns.add(noun);
							TermRankedResult result = new TermRankedResult();
							result.setTerm(noun.toLowerCase());
							result.setRank(0);
							this.results.add(result);
						}
					}
			}
		}

		Collections.sort(this.results, new Comparator<TermRankedResult>() {

			@Override
			public int compare(TermRankedResult o1, TermRankedResult o2) {
				return o1.getTerm().compareTo(o2.getTerm());
			}

		});

		super.end();
	}
}
