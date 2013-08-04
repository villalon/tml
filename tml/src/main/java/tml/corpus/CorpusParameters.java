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

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.Logger;

import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;


/**
 * Class that encapsulates all the parameters required to create
 * a {@link Corpus} and its corresponding {@link SemanticSpace}.
 *  
 * @author Jorge Villalon
 *
 */
public class CorpusParameters implements Cloneable {

	/**
	 * Criteria by which a {@link SemanticSpace} will reduce (or not) the
	 * dimensions of the space.
	 * 
	 * @author Jorge Villalon
	 * 
	 */
	public enum DimensionalityReduction {
		/**
		 * This method selects the number of dimensions based on how much
		 * variance they cover. The threshold is a percentage of the total
		 * variance.
		 */
		VARPCT,
		/**
		 * This method selects the number of dimensions based on a fixed number.
		 */
		NUM,
		/**
		 * This method selects the number of dimensions based on a percentage of
		 * the total number of dimensions in the space.
		 */
		PCT,
		/**
		 * No dimensionality reduction will be performed.
		 */
		NO
	}

	/**
	 * The criteria to select the terms that will be kept in the corpus
	 */
	public enum TermSelection {
		/**
		 * TF: Number of times a term appears in the corpus. MIN_TF: Terms with
		 * a TF lower than a threshold are discarded
		 */
		TF,
		/**
		 * TF: Number of times a term appears in the corpus. MIN_AVG_TF: The
		 * mean of the TF is calculated, and terms with an AVG_TF lower than a
		 * threshold are discarded
		 */
		AVG_TF,
		/**
		 * DF: Number of documents where the term appears. MIN_DF: Terms with a
		 * DF lower than a threshold are discarded
		 */
		DF
	}

	private static Logger logger = Logger.getLogger(CorpusParameters.class);

	public static CorpusParameters getParametersFromString(String paramString) {
		String[] parts = paramString.split("_");
		if(parts.length < 7)
			return null;
		CorpusParameters params = new CorpusParameters();
		params.setTermSelectionCriterion(TermSelection.valueOf(parts[0]));
		params.setTermSelectionThreshold(Double.parseDouble(parts[1]));
		params.setTermWeightLocal(LocalWeight.valueOf(parts[2]));
		params.setTermWeightGlobal(GlobalWeight.valueOf(parts[3]));
		params.setDimensionalityReduction(DimensionalityReduction.valueOf(parts[4]));
		params.setDimensionalityReductionThreshold(Double.parseDouble(parts[5]));
		params.setLanczosSVD(parts[6].equals("L"));
		if(parts.length > 7) {
			params.setNormalizeDocuments(parts[7].equals("Y"));
//			params.setCalculateSemanticSpace(parts[8].equals("Y"));
		}
		return params;
	}
	/** Term selection criteria */
	private TermSelection termSelectionCriterion = TermSelection.DF;

	/** Term selection threshold */
	private double termSelectionThreshold = 2;

	/** Max number of documents the corpus can manage */
	protected int maxDocuments = 3000;

	/** The term weighting scheme for this {@link SemanticSpace} */
	private LocalWeight termWeightLocal = LocalWeight.TF;

	private GlobalWeight termWeightGlobal = GlobalWeight.None;
	
	private boolean normalizeDocuments = false;

	/** The dimensionality reduction criterion */
	private DimensionalityReduction dimensionalityReduction = DimensionalityReduction.PCT;

	/** The dimensionality reduction threshold */
	private double dimensionalityReductionThreshold = 20;
	/** If the semantic space should use the Lanczos SVD */
	private boolean lanczosSVD = false;
	@Override
	protected Object clone() throws CloneNotSupportedException {
		CorpusParameters clone = (CorpusParameters) super.clone();
		return clone;
	}

	public DimensionalityReduction getDimensionalityReduction() {
		return dimensionalityReduction;
	}

	public double getDimensionalityReductionThreshold() {
		return dimensionalityReductionThreshold;
	}

	/**
	 * @return the maxDocuments
	 */
	public int getMaxDocuments() {
		return maxDocuments;
	}
	/**
	 * @return the termSelectionCriterion
	 */
	public TermSelection getTermSelectionCriterion() {
		return termSelectionCriterion;
	}
	/**
	 * @return the termSelectionThreshold
	 */
	public double getTermSelectionThreshold() {
		return termSelectionThreshold;
	}
	public GlobalWeight getTermWeightGlobal() {
		return termWeightGlobal;
	}

	public LocalWeight getTermWeightLocal() {
		return termWeightLocal;
	}

	/**
	 * @return the lanczosSVD
	 */
	public boolean isLanczosSVD() {
		return lanczosSVD;
	}

	/**
	 * @return the normalizeDocuments
	 */
	public boolean isNormalizeDocuments() {
		return normalizeDocuments;
	}


	public void loadFromFile(File file) {
		Properties props = new Properties();
		try {
			props.load(new FileReader(file));
		} catch (Exception e) {
			logger.error("Couldn't load file with parameters, sticking to the defaults");
			e.printStackTrace();
			logger.error(e);
			return;
		}

		String termSelectionCriterion = props.getProperty("termselcrit", "MIN_DF");
		String termSelectionThreshold = props.getProperty("termselthre", "2");
		String dimensionalityReductionCriterion = props.getProperty("reduxcrit", "DIMENSIONS_MAX_PERCENTAGE");
		String dimensionalityReductionThreshold = props.getProperty("reduxthre", "25");
		String localTermWeight = props.getProperty("localtw", "TF");
		String globalTermWeight = props.getProperty("globaltw", "Idf");
		String maxdocuments = props.getProperty("maxdocs", "9999");
		String useLanczos = props.getProperty("lanczos");

		if(termSelectionCriterion.equals("MIN_DF")) {
			this.setTermSelectionCriterion(TermSelection.DF);
		} else if (termSelectionCriterion.equals("MIN_AVG_TF")) {
			this.setTermSelectionCriterion(TermSelection.AVG_TF);
		} else if (termSelectionCriterion.equals("MIN_TF")) {
			this.setTermSelectionCriterion(TermSelection.TF);
		}
		
		if(useLanczos != null && useLanczos.equals("true")) {
			this.setLanczosSVD(true);
		} else
			this.setLanczosSVD(false);

		this.setTermSelectionThreshold(Double.parseDouble(termSelectionThreshold));

		if(dimensionalityReductionCriterion.equals("DIMENSIONS_MAX_NUMBER")) {
			this.setDimensionalityReduction(DimensionalityReduction.NUM);
		} else if (dimensionalityReductionCriterion.equals("DIMENSIONS_MAX_PERCENTAGE")) {
			this.setDimensionalityReduction(DimensionalityReduction.PCT);
		} else if (dimensionalityReductionCriterion.equals("NO_REDUCTION")) {
			this.setDimensionalityReduction(DimensionalityReduction.NO);
		} else if (dimensionalityReductionCriterion.equals("VARIANCE_COVERAGE")) {
			this.setDimensionalityReduction(DimensionalityReduction.VARPCT);
		}
		
		this.setDimensionalityReductionThreshold(Double.parseDouble(dimensionalityReductionThreshold));

		if(localTermWeight.equals("Binary")) {
			this.setTermWeightLocal(LocalWeight.Binary);
		} else if (localTermWeight.equals("LOGTF")) {
			this.setTermWeightLocal(LocalWeight.LOGTF);
		} else if (localTermWeight.equals("TF")) {
			this.setTermWeightLocal(LocalWeight.TF);
		} else if (localTermWeight.equals("TFn")) {
			this.setTermWeightLocal(LocalWeight.TFn);
		}		

		if(localTermWeight.equals("Binary")) {
			this.setTermWeightLocal(LocalWeight.Binary);
		} else if (localTermWeight.equals("LOGTF")) {
			this.setTermWeightLocal(LocalWeight.LOGTF);
		} else if (localTermWeight.equals("TF")) {
			this.setTermWeightLocal(LocalWeight.TF);
		} else if (localTermWeight.equals("TFn")) {
			this.setTermWeightLocal(LocalWeight.TFn);
		}		

		if(globalTermWeight.equals("Entropy")) {
			this.setTermWeightGlobal(GlobalWeight.Entropy);
		} else if (globalTermWeight.equals("GfIdf")) {
			this.setTermWeightGlobal(GlobalWeight.GfIdf);
		} else if (globalTermWeight.equals("Idf")) {
			this.setTermWeightGlobal(GlobalWeight.Idf);
		} else if (globalTermWeight.equals("None")) {
			this.setTermWeightGlobal(GlobalWeight.None);
		} else if (globalTermWeight.equals("Normal")) {
			this.setTermWeightGlobal(GlobalWeight.Normal);
		}		
		
		this.setMaxDocuments(Integer.parseInt(maxdocuments));
	}

	public void setDimensionalityReduction(
			DimensionalityReduction dimensionalityReduction) {
		this.dimensionalityReduction = dimensionalityReduction;
	}

	public void setDimensionalityReductionThreshold(
			double dimensionalityReductionThreshold) {
		this.dimensionalityReductionThreshold = dimensionalityReductionThreshold;
	}

	/**
	 * @param lanczosSVD the lanczosSVD to set
	 */
	public void setLanczosSVD(boolean lanczosSVD) {
		this.lanczosSVD = lanczosSVD;
	}

	/**
	 * @param maxDocuments the maxDocuments to set
	 */
	public void setMaxDocuments(int maxDocuments) {
		this.maxDocuments = maxDocuments;
	}

	/**
	 * @param normalizeDocuments the normalizeDocuments to set
	 */
	public void setNormalizeDocuments(boolean normalizeDocuments) {
		this.normalizeDocuments = normalizeDocuments;
	}

	/**
	 * @param termSelectionCriterion the termSelectionCriterion to set
	 */
	public void setTermSelectionCriterion(TermSelection termSelectionCriterion) {
		this.termSelectionCriterion = termSelectionCriterion;
	}

	/**
	 * @param termSelectionThreshold the termSelectionThreshold to set
	 */
	public void setTermSelectionThreshold(double termSelectionThreshold) {
		this.termSelectionThreshold = termSelectionThreshold;
	}
	
	public void setTermWeightGlobal(GlobalWeight termWeightGlobal) {
		this.termWeightGlobal = termWeightGlobal;
	}
	
	public void setTermWeightLocal(LocalWeight termWeightLocal) {
		this.termWeightLocal = termWeightLocal;
	}

        public static CorpusParameters getNoReductionParameters() {
            CorpusParameters params = new CorpusParameters();
//            params.setCalculateSemanticSpace(false);
            params.setDimensionalityReduction(DimensionalityReduction.NO);
            params.setDimensionalityReductionThreshold(0);
            params.setLanczosSVD(false);
            params.setMaxDocuments(Integer.MAX_VALUE);
            params.setNormalizeDocuments(false);
            params.setTermSelectionCriterion(TermSelection.DF);
            params.setTermSelectionThreshold(0);
            params.setTermWeightGlobal(GlobalWeight.None);
            params.setTermWeightLocal(LocalWeight.TF);
            return params;
        }
	
	@Override
	public String toString() {
		String lanczos = null;
		if(this.isLanczosSVD())
			lanczos = "L";
		else
			lanczos = "J";
		return 
		this.termSelectionCriterion + "_"
		+ (int) this.termSelectionThreshold + "_"
		+ this.termWeightLocal + "_"
		+ this.termWeightGlobal + "_"
		+ this.dimensionalityReduction + "_"
		+ (int) this.dimensionalityReductionThreshold + "_"
		+ lanczos
		;
	}
}