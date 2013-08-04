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
package tml.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import tml.Configuration;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class WordNetUtils {

	private static Logger logger = Logger.getLogger(WordNetUtils.class);

	private static IDictionary dictionary = null;
	private static void initDictionary() throws Exception {
		String wordnetPath = Configuration.getTmlProperties().getProperty("tml.wordnet");
		if(dictionary == null) {
			// construct the dictionary object and open it
			dictionary = new Dictionary(new URL("file", null, wordnetPath));
			dictionary.open();			
		}
	}

	public static List<String> getSynonyms(String term, POS pos) {

		List<String> synonyms = new ArrayList<String>();

		try {
			initDictionary();
		} catch (Exception e) {
			logger.error(e);
			return synonyms;
		}

		// look up first sense of the word "dog"
		IIndexWord idxWord = dictionary.getIndexWord(term, pos);
		
		if(idxWord == null)
			return synonyms;
		
		for(int i=0; i<idxWord.getWordIDs().size(); i++) {
			IWordID wordID = idxWord.getWordIDs().get(i);
			IWord word = dictionary.getWord(wordID);
			for(IWord w : word.getSynset().getWords()) {
				String syn = w.getLemma().toLowerCase();
				if(!synonyms.contains(syn))
					synonyms.add(syn);
			}
		}
		
		if(synonyms == null || synonyms.size()==0) {
			logger.error("No synonyms for:" + term);
		} else {
			logger.debug("Found " + synonyms.size() + " synonyms for " + term);
		}
		return synonyms;	
	}
	
	public List<String> getHypernyms(String term, POS pos){

		List<String> hypernymsList = new ArrayList<String>();

		try {
			initDictionary();
		} catch (Exception e) {
			logger.error(e);
			return hypernymsList;
		}

		// get the synset
		IIndexWord idxWord = dictionary.getIndexWord(term, pos);
		IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
		IWord word = dictionary.getWord(wordID);
		ISynset synset = word.getSynset();

		// get the hypernyms
		List<ISynsetID> hypernyms =
			synset.getRelatedSynsets(Pointer.HYPERNYM);

		// print out each hypernym�s id and synonyms
		List<IWord> words;
		for(ISynsetID sid : hypernyms){
			words = dictionary.getSynset(sid).getWords();
			for(Iterator<IWord> i = words.iterator(); i.hasNext();){
				String hypernym = i.next().getLemma().toLowerCase();
				if(!hypernymsList.contains(hypernym))
					hypernymsList.add(hypernym);
			}
		}
		
		return hypernymsList;
	}
}
