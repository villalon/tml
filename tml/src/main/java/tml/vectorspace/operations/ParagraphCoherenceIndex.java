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
import java.util.List;

import tml.vectorspace.operations.results.PassageDistancesResult;


public class ParagraphCoherenceIndex extends PassageDistances {

	public ParagraphCoherenceIndex() {
		this.name = "Paragraph coherence index";
	}
	
	@Override
	public void start() throws Exception {
		super.start();

		List<PassageDistancesResult> newResults = new ArrayList<PassageDistancesResult>();
		String lastParagraphId = null;
		double average = 0;
		int total = 0;
		int currentParagraphIndex = 0;
		try {
			for(int i=0; i<this.results.size(); i++) {
				PassageDistancesResult result = this.getResults().get(i);
				String sentenceId = this.corpus.getPassages()[result.getDocumentAId()];
				String currentParagraphId = repository.getDocumentField(sentenceId,"reference");
				logger.debug("Sentence:"+ sentenceId);
				logger.debug("Current paragrah:"+ currentParagraphId);
				if(currentParagraphId.equals(lastParagraphId)) {
					average += result.getDistance();
					total++;
				} else if(lastParagraphId != null){
					PassageDistancesResult r = new PassageDistancesResult();
					repository.getDocumentField(lastParagraphId,"reference");
					r.setDocumentAId(currentParagraphIndex);
					double distance = average / total;
					if(total == 0)
						distance = 0;
					r.setDistance(distance);
					average = 0;
					total = 0;
					currentParagraphIndex++;
					newResults.add(r);
				}
				lastParagraphId = currentParagraphId;
			}
		} catch (IOException e) {
			logger.error("Couldn't get reference from repository for sentences.");
			logger.error(e);
		}
		
		this.results = (ArrayList<PassageDistancesResult>) newResults;
		
		super.end();
	}
}
