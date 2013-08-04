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
import org.apache.log4j.Logger;

import tml.corpus.SimpleCorpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.utils.LuceneUtils;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;



/**
 * This class implements the integration with Matlab for TML
 * 
 * @author Jorge Villalon
 * 
 */
public class SemanticSpace {

	private static Logger logger = Logger.getLogger(SemanticSpace.class);
	private SimpleCorpus corpus;

	/**
	 * Creates a new instance of a SemanticSpace, the space will be created with
	 * all text documents found in a particular folder.
	 * 
	 * @param pathToRepository
	 *            folder containing the documents to be processed
	 * @param pathToMatlab
	 *            folder where the user's matlab folder is
	 * @throws Exception
	 */
	public SemanticSpace(String pathToRepository, String pathToMatlab)
			throws Exception {
		this.corpus = new SimpleCorpus(pathToRepository, pathToMatlab, false);
	}
	
	/**
	 * Stemming words with Lucene and making it available in Matlab
	 * @param phrase
	 * @return
	 */
	public String stemWords(String phrase) {
		return LuceneUtils.stemWords(phrase);
	}

	/**
	 * Loads the semantic space
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {
		try {
			this.corpus.load();
		} catch (Exception e) {
			throw new Exception(
					"Couldn't load the semantic space from the documents!", e);
		}
	}

	/**
	 * Sets the criteria to select which terms will be included in the LSA space
	 * 
	 * @param selection
	 *            the criteria
	 * @param threshold
	 *            the threshold above which the criteria will be validated
	 */
	public void setTermSelectionCriteria(int selection, double threshold) {
		switch (selection) {
		case 1:
			this.corpus.getCorpus().getParameters().setTermSelectionCriterion(
					TermSelection.DF);
			break;
		case 2:
			this.corpus.getCorpus().getParameters().setTermSelectionCriterion(
					TermSelection.TF);
			break;
		case 3:
			this.corpus.getCorpus().getParameters().setTermSelectionCriterion(
					TermSelection.AVG_TF);
			break;
		}
		this.corpus.getCorpus().getParameters().setTermSelectionThreshold(threshold);
		logger.info("Term selection criteria:"
				+ this.corpus.getCorpus().getParameters().getTermSelectionCriterion()
				+ " Threshold:"
				+ this.corpus.getCorpus().getParameters().getTermSelectionThreshold());
	}

	/**
	 * Sets the criteria to select how many dimension will be kept after SVD
	 * 
	 * @param reduction
	 *            the criteria
	 * @param threshold
	 *            the threshold above which the criteria will be validated
	 */
	public void setDimensionalityReductionCriteria(int reduction,
			double threshold) {
		switch (reduction) {
		case 1:
			this.corpus.getCorpus().getParameters()
					.setDimensionalityReduction(
							DimensionalityReduction.NUM);
			break;
		case 2:
			this.corpus.getCorpus().getParameters()
					.setDimensionalityReduction(
							DimensionalityReduction.PCT);
			break;
		case 3:
			this.corpus.getCorpus().getParameters()
					.setDimensionalityReduction(
							DimensionalityReduction.VARPCT);
			break;
		case 4:
			this.corpus.getCorpus().getParameters()
					.setDimensionalityReduction(
							DimensionalityReduction.NO);
			break;
		}
		this.corpus.getCorpus().getParameters()
				.setDimensionalityReductionThreshold(threshold);
		logger.info("Dimensionality reduction criteria:"
				+ this.corpus.getCorpus().getParameters()
						.getDimensionalityReduction()
				+ " Threshold:"
				+ this.corpus.getCorpus().getParameters()
						.getDimensionalityReductionThreshold());
	}

	/**
	 * Sets the term weighting scheme that will be used to calculate the LSA
	 * space
	 * 
	 * @param local
	 *            local weight criterion
	 * @param global
	 *            global weight criterion
	 */
	public void setTermWeightingCriteria(int local, int global) {
		switch (local) {
		case 1:
			this.corpus.getCorpus().getParameters().setTermWeightLocal(
					LocalWeight.Binary);
			break;
		case 2:
			this.corpus.getCorpus().getParameters().setTermWeightLocal(
					LocalWeight.LOGTF);
			break;
		case 3:
			this.corpus.getCorpus().getParameters().setTermWeightLocal(
					LocalWeight.TF);
			break;
		case 4:
			this.corpus.getCorpus().getParameters().setTermWeightLocal(
					LocalWeight.TFn);
			break;
		}
		switch (global) {
		case 1:
			this.corpus.getCorpus().getParameters().setTermWeightGlobal(
					GlobalWeight.Entropy);
			break;
		case 2:
			this.corpus.getCorpus().getParameters().setTermWeightGlobal(
					GlobalWeight.GfIdf);
			break;
		case 3:
			this.corpus.getCorpus().getParameters().setTermWeightGlobal(
					GlobalWeight.Idf);
			break;
		case 4:
			this.corpus.getCorpus().getParameters().setTermWeightGlobal(
					GlobalWeight.None);
			break;
		case 5:
			this.corpus.getCorpus().getParameters().setTermWeightGlobal(
					GlobalWeight.Normal);
			break;
		}
		logger.info("Term weighting. Local:"
				+ this.corpus.getCorpus().getParameters()
						.getTermWeightLocal()
				+ " Global:"
				+ this.corpus.getCorpus().getParameters()
						.getTermWeightGlobal());
	}

	/**
	 * Returns the matrix that represents the semantic space
	 * 
	 * @return a matrix of doubles
	 * @throws Exception
	 */
	public double[][] getTermDocMatrix() throws Exception {
		return this.corpus.getMatrix();
	}

	/**
	 * @return all terms in the semantic space
	 * @throws Exception
	 */
	public String[] getTerms() throws Exception {
		return this.corpus.getTerms();
	}

	/**
	 * @return all the documents in the semantic space
	 * @throws Exception
	 */
	public String[] getDocuments() throws Exception {
		return this.corpus.getDocuments();
	}	
}
