/*******************************************************************************
 *  Copyright 2010 Stephen O'Rourke (stephen.orourke@sydney.edu.au)
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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tml.corpus.Corpus;
import tml.corpus.TextDocument;
import tml.vectorspace.operations.results.RapidAutomaticKeywordExtractionResult;

/**
 * This operation extracts keywords from all documents in a {@link Corpus}.
 * Keyword extraction is performed using the Rapid Automatic Keyword Extraction
 * (RAKE) method described in:
 * 
 * Rose, S., Engel, D., Cramer, N., & Cowley, W. (2010). Automatic Keyword
 * Extraction from Individual Documents. In M. W. Berry & J. Kogan (Eds.), Text
 * Mining: Theory and Applications: John Wiley & Sons.
 * 
 * @author Stephen O'Rourke
 * 
 */
public class RapidAutomaticKeywordExtraction extends AbstractOperation<RapidAutomaticKeywordExtractionResult> {

	public RapidAutomaticKeywordExtraction() {
		this.name = "Rapid Automatic Keyword Extraction";
	}

	public Object[][] getInnerData() {
		return getResultsTable();
	}

	@Override
	public Object[][] getResultsTable() {
		Object[][] resultsTable = new Object[results.size()][2];
		for (int i = 0; i < results.size(); i++) {
			resultsTable[i][0] = results.get(i).getKeyword();
			resultsTable[i][1] = results.get(i).getWeighting();
		}
		return resultsTable;
	}

	@Override
	public Object[] getResultsTableHeader() {
		Object[] data = new Object[2];
		data[0] = "Keyword";
		data[1] = "Weighting";
		return data;
	}

	@Override
	public void start() throws Exception {
		// extract keywords
		List<String> keywords = new LinkedList<String>();
		Set<String> stopwords = new HashSet<String>(Arrays.asList(repository.getStopwords()));
		for (String textDocumentId : corpus.getPassages()) {
			TextDocument textDocument = repository.getTextDocument(textDocumentId);
			String text = textDocument.getContent();
			BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(corpus.getRepository().getLocale());
			sentenceIterator.setText(text);
			int sentenceStart = sentenceIterator.first(), sentenceEnd = 0;
			while ((sentenceEnd = sentenceIterator.next()) != BreakIterator.DONE) {
				String sentence = text.substring(sentenceStart, sentenceEnd);
				BreakIterator wordIterator = BreakIterator.getWordInstance(corpus.getRepository().getLocale());
				wordIterator.setText(sentence);
				int wordStart = wordIterator.first(), wordEnd = 0, keywordStart = wordStart;
				while ((wordEnd = wordIterator.next()) != BreakIterator.DONE) {
					String word = cleanWord(sentence.substring(wordStart, wordEnd));
					if (stopwords.contains(word) || word.matches("\\W+")) {
						// word is keyword break
						String keyword = cleanWord(sentence.substring(keywordStart, wordStart));
						if (keyword.length() > 0) {
							keywords.add(keyword);
						}
						keywordStart = wordEnd;
					} else if (wordEnd == sentence.length()) {
						// word is last in sentence
						String keyword = cleanWord(sentence.substring(keywordStart, wordEnd));
						if (keyword.length() > 0) {
							keywords.add(keyword);
						}
					}
					wordStart = wordEnd;
				}
				sentenceStart = sentenceEnd;
			}
		}

		// calculate word frequency and degree
		Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
		Map<String, Integer> wordDegree = new HashMap<String, Integer>();
		for (String keyword : keywords) {
			String[] words = keyword.split("\\s");
			for (String word : words) {
				if (wordFrequency.containsKey(word)) {
					wordFrequency.put(word, wordFrequency.get(word) + 1);
					wordDegree.put(word, wordDegree.get(word) + words.length);
				} else {
					wordFrequency.put(word, 1);
					wordDegree.put(word, words.length);
				}
			}
		}

		// calculate keyword weighting results
		results = new ArrayList<RapidAutomaticKeywordExtractionResult>();
		for (String keyword : new LinkedHashSet<String>(keywords)) {
			double weighting = 0;
			for (String word : keyword.split("\\s")) {
				weighting += (double) wordDegree.get(word) / (double) wordFrequency.get(word);
			}
			RapidAutomaticKeywordExtractionResult result = new RapidAutomaticKeywordExtractionResult();
			result.setKeyword(keyword);
			result.setWeighting(weighting);
			results.add(result);
		}

		// sort results by keyword weighting
		Collections.sort(results, Collections.reverseOrder());
	}

	private String cleanWord(String word) {
		return word.trim().toLowerCase();
	}
}
