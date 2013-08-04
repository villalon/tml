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
import java.util.List;

import tml.annotators.PennTreeAnnotator;
import tml.utils.StanfordUtils;
import tml.vectorspace.operations.results.RelationshipExtractionResult;

import edu.stanford.nlp.trees.Tree;


/**
 * Relationship extraction aims to extract the labeled relationships from a set
 * of concepts
 * 
 * @author Jorge Villalon
 * 
 */
public class RelationshipExtraction extends
AbstractOperation<RelationshipExtractionResult> {

	@Override
	public void start() throws Exception {
		super.start();

		List<String> rels = new ArrayList<String>();
		for (String passageId : this.corpus.getPassages()) {
			Tree pennTree = null;
			try {
				pennTree = StanfordUtils.getTreeFromString(passageId, repository.getDocumentField(passageId, PennTreeAnnotator.FIELD_NAME));
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
				return;
			}
			List<String> verbs = StanfordUtils.extractVerbs(pennTree);
			if(verbs != null)
				for (String verb : verbs) {
					verb = verb.trim().toLowerCase();
					if (rels.contains(verb))
						continue;
					if (verb.length() == 0)
						continue;
					rels.add(verb);
				}
		}

		Collections.sort(rels);

		for (String verb : rels) {
			RelationshipExtractionResult result = new RelationshipExtractionResult();
			result.setLinkingWord(verb);
			this.results.add(result);
		}
		super.end();
	}
}
