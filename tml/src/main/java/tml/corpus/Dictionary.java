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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class represents a group of {@link Term}s or words/symbols, usually
 * obtained from a set of documents or text passages. It is the common set of
 * words for a group of documents.
 * 
 * The dictionary can filter {@link Term}s based on a selection criteria and its
 * specific threshold. By default the {@link TermSelection} criteria is a
 * minimum DF or Document Frequency, i.e. the {@link Term} must appear in at
 * least a certain number of different {@link TextPassage}s indicated by the
 * threshold.
 * 
 * A {@link Dictionary} also maintains the list of {@link Term}s inside a
 * {@link TextPassage}. When the {@link TermSelection} criteria is applied, the
 * {@link Dictionary} removes the unused {@link Term}s from the
 * {@link TextPassage}s that contain those {@link Term}s.
 * 
 * @author Jorge Villalon
 * 
 */
public class Dictionary {

	/** Logger */
	private static Logger logger = Logger.getLogger(Dictionary.class);
	
	/** Terms indexed by their lexical representation (String) */
	private Hashtable<String, Term> termsByText;
	/** The list of lexical representation of the terms */
	private List<String> terms;
	/** The corpus to which the dictionary belongs */
	private Corpus corpus = null;

	/** Implemented local weight functions */

	/**
	 * Basic constructor of a {@link Dictionary}, initialises the list and index
	 * of {@link Term}s
	 * 
	 * @param corpus
	 */
	public Dictionary(Corpus corpus) {
		assert (corpus != null);

		this.termsByText = new Hashtable<String, Term>();
		this.terms = new ArrayList<String>();
		this.corpus = corpus;
		logger.debug("Creating dictionary for corpus " + corpus);
	}

	/**
	 * Adds an array of {@link Term}s to the {@link Dictionary} and their
	 * frequencies. Both must come from a specific {@link TextPassage}
	 * 
	 * @param newTerms
	 * @param termFreqs
	 * @param document
	 */
	public void addTerms(String[] newTerms, int[] termFreqs,
			TextPassage document) {
		assert (document != null);
		assert (newTerms != null);
		assert (termFreqs != null);

		int currentTerm = 0;
		// For each lexical representation of a term (word or symbol)
		for (String t : newTerms) {

			// Its correspondent Term is obtained, if it doesn't exist is
			// created
			Term term = this.termsByText.get(t);

			if (term == null) {
				term = new Term(t, this.termsByText.size());
				this.termsByText.put(term.getTerm(), term);
				this.terms.add(term.getTerm());
			}

			// The document is added to the term's list of documents
			term.addTermAppearance(document, termFreqs[currentTerm]);

			// The term is added to the list of terms in the document
			document.addTerm(term, termFreqs[currentTerm]);

			currentTerm++;
		}
	}

	/**
	 * Remove the {@link Term}s from the {@link Dictionary} that doesn't meet
	 * the {@link TermSelection} criteria according to the threshold.
	 */
	public void removeTerms() {

		int termIndex = 0;
		int originalNumberOfTerms = this.terms.size();

		// For each term in the dictionary
		for (int currentIndex = 0; currentIndex < originalNumberOfTerms; currentIndex++) {

			// Gets the term and its statistics
			Term term = this.termsByText.get(this.terms.get(currentIndex));

			double termValue = 0;

			// According to the criteria, a different value is read
			switch (this.corpus.getParameters().getTermSelectionCriterion()) {
			case AVG_TF:
				termValue = term.getTermGlobalFrequencyMean();
				break;
			case DF:
				termValue = term.getDocumentFrequency();
				break;
			case TF:
				termValue = term.getTermGlobalFrequency();
				break;
			default:
				logger.error("Invalid term selection criteria");
			}

			// Now validate if the term value meets the criteria
			if (termValue < this.corpus.getParameters().getTermSelectionThreshold()) {
				// Remove the term from the dictionary and from its documents
				this.termsByText.remove(term.getTerm());
				for (TextPassage document : term.getTextPassages()) {
					document.removeTerm(term);
				}
			} else {
				// Update the term index if necessary
				if (term.getIndex() != termIndex || true) {
					term.setIndex(termIndex);
				}
				termIndex++;
			}
		}

		logger.debug("Removal of terms finished, "
				+ (originalNumberOfTerms - termIndex) + " terms removed, "
				+ termIndex + " terms kept.");
	}

	/**
	 * Returns the collection of {@link Term}s in the {@link Dictionary}
	 * 
	 * @return a {@link Collection} of {@link Term}s
	 */
	public Collection<Term> getTerms() {
		return this.termsByText.values();
	}

	/**
	 * Gets the {@link Corpus} to which the {@link Dictionary} belongs
	 * 
	 * @return a {@link Corpus}
	 */
	public Corpus getCorpus() {
		return this.corpus;
	}

	/**
	 * Returns a {@link Term} that represents a word, null if it is not in the
	 * {@link Dictionary}
	 * 
	 * @param word
	 *            the word to look for
	 * @return the {@link Term}
	 */
	public Term getTermByText(String word) {
		return this.termsByText.get(word);
	}
}
