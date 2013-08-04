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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import tml.utils.Stats;


/**
 * This class represents a text passage, that is part of a {@link Corpus}. It
 * can be a sentence, paragraph, a complete document or any other piece of text
 * of any length.
 * 
 * @author Jorge Villalon
 * 
 */
public class TextPassage {

	/**
	 * This class represents the statistics for a {@link TextPassage}. Basically
	 * how many total terms and how many different terms.
	 * 
	 * @author Jorge Villalon
	 * 
	 */
	public class TextPassageStats {
		private Stats stats;

		/**
		 * How many different terms the {@link TextPassage} contains
		 * 
		 * @return the number of different terms
		 */
		public int getDifferentTerms() {
			return (int) this.stats.count;
		}

		/**
		 * @return the statistics for the {@link TextPassage}
		 */
		public Stats getStats() {
			return stats;
		}

		/**
		 * @return the total number of terms in a {@link TextPassage}
		 */
		public int getTotalTerms() {
			return (int) this.stats.sum;
		}

		/**
		 * Loads a set of term frequencies into the passage
		 * 
		 * @param termsFrequencies
		 *            an array with the term frequencies
		 */
		public void load(int[] termsFrequencies) {
			this.stats = new Stats();

			for (int i = 0; i < termsFrequencies.length; i++) {
				if (termsFrequencies[i] > 0)
					this.stats.add(termsFrequencies[i]);
			}

			this.stats.calculateDerived();
		}
	}

	/** The log4j logger */
	private static Logger logger = Logger.getLogger(TextPassage.class);
	/** A unique id */
	private int id;
	/** The external id to identify it in Lucene */
	private String externalId;
	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return externalId;
	}

	/** The statistics of the passage */
	private Stats stats;
	/** The raw term frequencies */
	private double[] termsFreqs = null;
	/** The term indices in the dictionary */
	private int[] termsIndices = null;
	/** A map with the sorted terms */
	private TreeMap<Integer, Term> terms;
	/** A hash table with the term frequencies */
	private Hashtable<Integer, Integer> termFrequencies;
	/** The corpus to which the passage belongs */
	private Corpus corpus = null;
	/** The content of the TextPassage */
	private String content;
	/** A human readable title for the passage */
	private String title;
	/** URL of the passage */
	private String url;
	/** The type of the passage (doc, parag, senten) */
	private String type;
	/** Annotations obtained from the Lucene index */
	private Hashtable<String, String> annotations = null;

	/**
	 * Creates a new instance of a {@link TextPassage}.
	 * 
	 * @param id the id of the passage
	 * @param title the title for the passage
	 * @param corpus the {@link Corpus} to which the passage belongs
	 * @param content the content of the passage
	 * @param url the url for the passage
	 * @param type the type of the passage (document, paragraph or sentence)
	 * @param externalId Lucene id of the passage
	 */
	public TextPassage(int id, Corpus corpus, String content, String title, 
			String url, String type, String externalId) {
		assert (corpus != null);
		assert (id >= 0);

		this.id = id;
		this.corpus = corpus;
		this.title = title;
		this.content = content;
		this.url = url;
		this.type = type;
		this.externalId = externalId;

		// Initialising containers
		this.stats = new Stats();
		this.terms = new TreeMap<Integer, Term>();
		this.termFrequencies = new Hashtable<Integer, Integer>();
		this.annotations = new Hashtable<String, String>();
	}

	/**
	 * @return the annotations
	 */
	public Hashtable<String, String> getAnnotations() {
		return annotations;
	}

	/**
	 * Adds a {@link Term} to the passage, it adds a number to the statistics
	 * but it doesn't calculate the final values
	 * 
	 * @param term
	 * @param frequency
	 */
	public void addTerm(Term term, int frequency) {
		this.terms.put(term.getIndex(), term);
		this.termFrequencies.put(term.getIndex(), frequency);
		this.stats.add(frequency);
	}

	/**
	 * Calculates the packed arrays for terms and frequencies
	 */
	private void calculate() {
		this.termsFreqs = new double[this.terms.size()];
		this.termsIndices = new int[this.terms.size()];

		Iterator<Term> it = this.terms.values().iterator();

		for (int i = 0; it.hasNext(); i++) {
			Term term = it.next();
			this.termsFreqs[i] = this.termFrequencies.get(term.getIndex());
			this.termsIndices[i] = term.getIndex();
			if (Double.isNaN(this.termsFreqs[i])
					|| Double.isInfinite(this.termsFreqs[i])) {
				this.termsFreqs[i] = 0;
				logger.error("Invalid frequency, setting to 0");
			}
		}
	}


	/**
	 * @return the content of the passage
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @return the {@link Corpus} to which the passage belongs
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @return the external id of a passage
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return basic statistics for the passage
	 */
	public Stats getStats() {
		return stats;
	}

	/**
	 * @return a packed array with the frequencies of the passage terms
	 */
	public double[] getTermFreqs() {
		if (termsFreqs == null) {
			try {
				this.calculate();
			} catch (Exception e) {
				e.printStackTrace();
				this.termsFreqs = null;
			}
		}
		return termsFreqs;
	}

	/**
	 * @return all the {@link Term}s in the passage
	 */
	public Collection<Term> getTerms() {
		return this.terms.values();
	}

	/**
	 * @return an array of indices of the terms within the passage
	 */
	public int[] getTermsCorpusIndices() {
		if (this.termsIndices == null) {
			try {
				this.calculate();
			} catch (Exception e) {
				e.printStackTrace();
				this.termsIndices = null;
			}
		}
		return this.termsIndices;
	}

	/**
	 * @return the title of the passage
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the type of the passage (document, paragraph or sentence)
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the url of the passage
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return if the {@link TextPassage} contains any {@link Term}
	 */
	public boolean isEmpty() {
		return this.getTerms().size() == 0;
	}

	/**
	 * Removes a {@link Term} from the passage
	 * 
	 * @param term
	 */
	public void removeTerm(Term term) {
		this.terms.remove(term.getIndex());
		this.termFrequencies.remove(term.getIndex());
	}

	/**
	 * Basic output of a text passage
	 */
	@Override
	public String toString() {
		return "Text passage [" + this.getId() + "]";
	}

	/**
	 * Updates the index of a {@link Term} in the passage
	 * 
	 * @param term
	 *            the {@link Term} which index will be updated
	 * @param oldIndex
	 *            the old index
	 * @param newIndex
	 *            the new index
	 */
	public void updateTermIndex(Term term, int oldIndex, int newIndex) {
		Term oldTerm = this.terms.get(oldIndex);
		assert (oldTerm == term);
		this.terms.remove(oldIndex);
		this.terms.put(newIndex, term);
		int frequency = this.termFrequencies.get(oldIndex);
		this.termFrequencies.remove(oldIndex);
		this.termFrequencies.put(newIndex, frequency);
	}
}
