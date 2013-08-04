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

import tml.vectorspace.operations.results.LastPassageResult;


/**
 * Extracts the last passage of the corpus, given the linearity expected in the
 * index.
 * 
 * @author Jorge Villalon
 *
 */
public class LastPassage extends AbstractOperation<LastPassageResult> {

	/**
	 * 
	 */
	public LastPassage() {
		this.name = "Last passage";
		this.requiresSemanticSpace = false;
	}

	@Override
	public void start() throws Exception {
		super.start();
		this.results = new ArrayList<LastPassageResult>();
		try {
			String externalId = this.corpus.getPassages()[this.corpus.getPassages().length-1];
			String content = this.repository.getDocumentField(externalId, this.repository.getLuceneContentField());
			LastPassageResult result = new LastPassageResult();
			result.setPassage(content);
			this.results.add(result);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		super.end();
	}
}
