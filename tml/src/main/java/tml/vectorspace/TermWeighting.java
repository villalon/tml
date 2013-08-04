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

package tml.vectorspace;


import org.apache.log4j.Logger;

import tml.corpus.Corpus;
import tml.utils.Stats;


import Jama.Matrix;

/**
 * The TermWeighting filter transforms a basic
 * Term/Document matrix to a different Term/Weighting scheme. At the moment we
 * support a combination of Local Weights and Global Weights. Local Weights can
 * be: TF: The raw term frequency. TFn: Raw term frequency normalized within the
 * document. LOGTF: Calculates LogEntropy weight for all numeric values in the
 * given dataset (apart from the class attribute, if set). The resulting values
 * are the product of a local weight (1 + log(tf)) and a global weight 1 - Sum_i
 * ((tf/gf)*log(tf/gf))/log(N) with tf: the raw term frequency gf: the global
 * term frequency, number of times the term appears in the corpus (i.e. Sum_i
 * tf) N: number of documents (or parts) in the corpus. More details in"Dumais, Susan 1990 Enhancing Performance in Latent Semantic Indexing (LSI) Retrieval"
 * . <p/>
 * 
 * @author Jorge Villalon
 */
public class TermWeighting {

	/**
	 * Implemented global weight functions
	 */
	public enum GlobalWeight {
		/** Just 1, so it leaves the local weight as it is */
		None,
		/** The norm of the document vector */
		Normal,
		/** The global inverse document frequency */
		GfIdf,
		/** Inverse document frequency */
		Idf,
		/** Accumulated entropy */
		Entropy
	}

	/**
	 * Implemented local weight functions
	 */
	public enum LocalWeight {
		/** 1 if the term is in the document, 0 otherwise */
		Binary,
		/** The frequency of the term in the document */
		TF,
		/**
		 * The normalised frequency, i.e. divided by the maximum frequency in
		 * the doc
		 */
		TFn,
		/** The log of 1 plus the term frequency */
		LOGTF
	}


	public TermWeighting(Corpus corpus) {
		this.corpus = corpus;
	}
	private Corpus corpus = null;

	private static Logger logger = Logger.getLogger(TermWeighting.class);
	private void calculateGlobalValues(Matrix termdoc) throws TermWeightingException {

//		if(this.corpus.getParameters().getTermWeightGlobal() == GlobalWeight.None)
//			return;
		
		// If the corpus is a projection, then we don't have to calculate
		// the term statistics, because they are defined by the corpus
		// on which we are projecting.
		if(this.corpus.isProjection()) {
			if(
					this.corpus.getTermEntropies() == null
					|| this.corpus.getTermStats() == null
					|| this.corpus.getDocStats() != null)
				throw new TermWeightingException(new Exception("The projected corpus should have entropies and termstats, and shouldn't have docstats calculated."));
		}
		// First, calculate global statistics for both terms and documents
		Stats[] termStats = null;
		if(!this.corpus.isProjection()) // Ask first to save a little bit of memory and time
			termStats = new Stats[termdoc.getRowDimension()];
		
		// Doc stats are always calculated, for a normal corpus and a projected one.
		Stats[] docStats = new Stats[termdoc.getColumnDimension()];

		if(!this.corpus.isProjection())
			for (int doc = 0; doc < termdoc.getRowDimension(); doc++)
				termStats[doc] = new Stats();
		for (int term = 0; term < termdoc.getColumnDimension(); term++)
			docStats[term] = new Stats();
		for (int doc = 0; doc < termdoc.getColumnDimension(); doc++) {
			for (int term = 0; term < termdoc.getRowDimension(); term++) {
				if(termdoc.get(term, doc) != 0) {
					if(!this.corpus.isProjection())
						termStats[term].add(termdoc.get(term, doc));
					docStats[doc].add(termdoc.get(term, doc));
				}
			}
		}
		for (int doc = 0; doc < termdoc.getColumnDimension(); doc++) {
			docStats[doc].calculateDerived();
		}
		if(!this.corpus.isProjection()) {
			for (int term = 0; term < termdoc.getRowDimension(); term++) {
				termStats[term].calculateDerived();
			}		

			this.corpus.setTermStats(termStats);
		}

		if(!this.corpus.isProjection()) {
			// Now calculate the entropy
			double[] termEntropies = new double[termdoc.getRowDimension()];

			for (int doc = 0; doc < termdoc.getColumnDimension(); doc++) {
				for (int term = 0; term < termdoc.getRowDimension(); term++) {
					Stats stats = termStats[term];
					double p = 0;
					if(stats.sum > 0)
						p = termdoc.get(term, doc) / stats.sum;
					validateValue("p for " + term + "," + doc, p);
					double entropy = 0;
					double n = termdoc.getColumnDimension();
					if (p != 0 && n > 1) {
						entropy = (p * (Math.log(p) / Math.log(2)))
						/ (Math.log(n) / Math.log(2));
					}
					validateValue("entropy for " + term + "," + doc + " and p=" + p,
							entropy);
					termEntropies[term] += entropy;
				}
			}
			this.corpus.setTermEntropies(termEntropies);
		}

		this.corpus.setDocStats(docStats);

	}

	private double getGlobalValue(Matrix termdoc, int doc, int term)
	throws TermWeightingException {

		if(corpus.getParameters().getTermWeightGlobal() == GlobalWeight.None)
			return 1;
		
		double df = corpus.getTermStats()[term].count;
		if(df <= 0)
			throw new TermWeightingException (new Exception("Invalid document frequency, this should be impossible!"));
		validateValue("df", df);
		double ndocs = termdoc.getColumnDimension();
		validateValue("ndocs", ndocs);
		double gf = corpus.getTermStats()[term].sum;
		validateValue("gf", gf);
		double sumsq = corpus.getTermStats()[term].sumSq;
		if(sumsq <= 0)
			throw new TermWeightingException (new Exception("Invalid term frequency, this should be impossible!"));
		validateValue("sumsq", sumsq);
		double entropy = corpus.getTermEntropies()[term];
		validateValue("entropy", entropy);

		double value;
		switch (this.corpus.getParameters().getTermWeightGlobal()) {
		case Entropy:
			value = 1 + entropy;
			break;
		case GfIdf:
			value = gf / df;
			break;
		case Idf:
			value = (Math.log(ndocs / df) / Math.log(2)) + 1;
			break;
		case None:
			value = 1;
			break;
		case Normal:
			value = 1 / Math.sqrt(sumsq);
			break;
		default:
			value = 0;
		}
		validateValue("global value", value);
		return value;
	}

	private double getLocalValue(Matrix termdoc, int doc, int term)
	throws TermWeightingException {
		double value = termdoc.get(term, doc);
		validateValue("local value", value);
		
		switch (this.corpus.getParameters().getTermWeightLocal()) {
		case Binary:
			if (value > 0)
				return 1;
			return 0;
		case TF:
			return value;
		case TFn:
			if(corpus.getDocStats()[doc].count == 0)
				return 0;
			return value / corpus.getDocStats()[doc].max;
		case LOGTF:
			return Math.log(1 + value);
		default:
			return 0;
		}
	}

	public Matrix process(Matrix termdoc) throws TermWeightingException {

		if(this.corpus.isProjection()) {
			logger.debug("Corpus is projection, no term weighting applied.");
			return termdoc;
		}
		
		logger.debug("Term weighting. Local: "
				+ this.corpus.getParameters().getTermWeightLocal() + 
				" Global: " + this.corpus.getParameters().getTermWeightGlobal());

		calculateGlobalValues(termdoc);

		logger.debug("Updating weights");
		for (int doc = 0; doc < termdoc.getColumnDimension(); doc++) {
			for (int term = 0; term < termdoc.getRowDimension(); term++) {

				double localValue = getLocalValue(termdoc, doc, term);
				double globalValue = getGlobalValue(termdoc, doc, term);
				double value = localValue * globalValue;

				if (Double.isInfinite(value))
					throw new TermWeightingException(new Exception("Damn it! Infinite"));
				if (Double.isNaN(value))
					throw new TermWeightingException(new Exception("Damn it! NaN"));
				termdoc.set(term, doc, value);
			}
		}

		return termdoc;
	}

	private void validateValue(String name, double value) throws TermWeightingException {
		if (Double.isInfinite(value))
			throw new TermWeightingException(new Exception(name + ":" + value + " is invalid - Infinite"));
		if (Double.isNaN(value))
			throw new TermWeightingException(new Exception(name + ":" + value + " is invalid - NaN"));
	}
}
