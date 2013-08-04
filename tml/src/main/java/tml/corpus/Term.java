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
import java.util.List;

import tml.utils.Stats;



/**
 * <p>
 * The {@link Term} class represents a unique word within a {@link Corpus}. It
 * is stored by the {@link Corpus}' {@link Dictionary} and it contain links to
 * all the {@link TextPassage}s that contain the {@link Term}.
 * </p>
 * <p>
 * The class also contains some statistics like the total number of times it
 * appears in a {@link Corpus}.
 * </p>
 * <p>
 * The following code shows how to use the {@link Term}s from a
 * {@link TextDocument}
 * </p>
 * 
 * <pre>
 * ...
 * 	TextDocument document = .....;
 * 	List&lt;Term&gt; terms = document.getSentenceCorpus().getDictionary().getTerms();
 * 	for(Term term : terms) {
 * 		System.out.println(&quot;Term:&quot; + term.getTerm());
 * 		System.out.println(&quot;DF:&quot; + term.getDocumentFrequency());
 * 		System.out.println(&quot;TF:&quot; + term.getTermFrequency());
 * 	}
 * </pre>
 * 
 * @author Jorge Villalon
 * 
 */
public class Term {

	/** The word for the term */
	private String term;
	/** The index within the dictionary */
	private int index;
	/** The text passages to which the term belongs */
	private List<TextPassage> textPassages;
	/** The term statistics */
	private Stats termStats;
	/** If there's new info */
	private boolean dirty = true;
	/** Indicates if the term is considered a concept */
	private boolean isConcept = false;

	/**
	 * @param isConcept
	 *            if the {@link Term} is a Concept
	 */
	public void setConcept(boolean isConcept) {
		this.isConcept = isConcept;
	}

	/**
	 * Creates a new {@link Term}, with an index defined by an external source
	 * (usually a {@link Dictionary}.
	 * 
	 * @param term
	 * @param index
	 */
	public Term(String term, int index) {
		this.term = term;
		this.index = index;
		this.termStats = new Stats();
		this.textPassages = new ArrayList<TextPassage>();
	}

	/**
	 * Adds a new {@link TextPassage} to the {@link Term}
	 * 
	 * @param textPassage
	 * @param termFrequency
	 */
	public void addTermAppearance(TextPassage textPassage, double termFrequency) {
		assert (this.textPassages.contains(textPassage) == false);
		this.textPassages.add(textPassage);
		this.termStats.add(termFrequency);
		this.dirty = true;
	}

	/**
	 * Checks if the statistics are dirty and recalculates it
	 */
	private void checkDirtyStats() {
		if (this.dirty) {
			this.termStats.calculateDerived();
			this.dirty = false;
		}
	}

	/**
	 * @return the document frequency of the term, i.e. in how many documents
	 *         does it appears.
	 */
	public int getDocumentFrequency() {
		this.checkDirtyStats();

		return (int) this.termStats.count;
	}

	/**
	 * @return the index of the {@link Term} within its {@link Dictionary}
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the word that represents the {@link Term}
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * @return the term frequency of the {@link Term}, i.e. how many times the
	 *         word appears in the {@link Corpus}.
	 */
	public int getTermGlobalFrequency() {
		this.checkDirtyStats();

		return (int) this.termStats.sum;
	}

	/**
	 * @return the mean appearance of the {@link Term} along the
	 *         {@link TextPassage}s of a {@link Corpus}.
	 */
	public double getTermGlobalFrequencyMean() {
		this.checkDirtyStats();

		return this.termStats.mean;
	}

	/**
	 * @return the list of {@link TextPassage}s to which the {@link Term}
	 *         belongs
	 */
	public List<TextPassage> getTextPassages() {
		return textPassages;
	}

	/**
	 * Changes the value of the index for the {@link Term} within a
	 * {@link Corpus}
	 * 
	 * @param index
	 */
	public void setIndex(int index) {
		for (TextPassage document : this.getTextPassages()) {
			document.updateTermIndex(this, this.getIndex(), index);
		}
		this.index = index;
	}

	/**
	 * The default string for a Term is it's own word
	 */
	@Override
	public String toString() {
		return this.term;
	}

	/**
	 * @return if the Term correspond to a potential concept
	 */
	public boolean isConcept() {
		return isConcept;
	}
}
