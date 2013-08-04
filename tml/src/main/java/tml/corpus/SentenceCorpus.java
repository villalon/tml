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
package tml.corpus;

import java.io.IOException;

import tml.storage.Repository;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.TermWeightingException;


/**
 * Class representing a corpus formed with the sentences of a document
 * @author Jorge Villalon
 *
 */
public class SentenceCorpus extends Corpus {

	/**
	 * @param document the document to which the sentences belong
	 * @throws Exception if the document is null
	 */
	public SentenceCorpus(TextDocument document) throws Exception {
		if(document == null)
			throw new Exception("A sentence corpus must belong to a document");
		
		this.luceneQuery = "type:sentence AND reference:p*d" + document.getExternalId();
	}

	@Override
	public void load(Repository storage)
			throws NotEnoughTermsInCorpusException, IOException,
			NoDocumentsInCorpusException, TermWeightingException {
		super.load(storage);
	}
}
