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

import java.io.File;

import Jama.SingularValueDecomposition;
import Jama.Matrix;

import org.apache.log4j.Logger;

import tml.corpus.Corpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.utils.LanczosSVDLIBCUtils;


/**
 * <p>
 * This class is a Vector Space Model representation of a group of documents or
 * {@link Corpus} constructed using Latent Semantic Indexing, it contains a term
 * by document matrix for the {@link Corpus}.
 * </p>
 * <p>
 * Some of the LSI steps are performed by this class:
 * </p>
 * <ul>
 * <li>6. Term weighting, calculates a new term weight as the multiplication of
 * a local and a global weight for each term-doc pair.</li>
 * <li>7. Dimensionality reduction, performs the SVD and reconstruct the matrix
 * considering less dimensions.</li>
 * <li>8. Normalisation, performs a crude normalisation of the vectors in the
 * space.</li>
 * </ul>
 * <p>
 * Several {@link Operation}s can be performed on a {@link SemanticSpace}. Each
 * one contains a list of results, that can be read from the operation in
 * Object[][], HTML and Graphic format for human consumption.
 * </p>
 * 
 * @author Jorge Villalon
 * 
 */
public class SemanticSpace implements Cloneable {

	private static final int MAX_MATRIX_SIZE = 100000;

	/** The logger */
	private static Logger logger = Logger.getLogger(SemanticSpace.class);

	/** The {@link Corpus} that was the source for the {@link TextPassage}s */
	private Corpus corpus = null;

	/** Terms matrix in the semantic space */
	private Matrix Uk = null;
	/** Singular values in the semantic space */
	private Matrix Sk = null;
	/** Documents matrix in the semantic space */
	private Matrix Vk = null;
	/** The number of dimensions that were kept */
	private int dimensionsKept = -1;
	/** The time in milliseconds the {@link SemanticSpace} took to calculate the space */
	private long processingTime = 0;

	/**
	 * Creates a new {@link SemanticSpace} from a {@link Corpus}.
	 * 
	 * @param sourceCorpus
	 *            the {@link Corpus} for the {@link SemanticSpace}
	 */
	public SemanticSpace(Corpus sourceCorpus) {
		assert (sourceCorpus != null);
		this.corpus = sourceCorpus;
	}
	
	public boolean isCalculated() {
		return this.Uk != null
		&& this.Vk != null
		&& this.Sk != null
		&& this.dimensionsKept > 0;
	}

	/**
	 * Applies the dimensionality reduction to the matrix
	 */
	private Matrix applyDimensionalityReduction(Matrix termDoc) {

		logger.debug("Applying dimensionality reduction");

		dimensionsKept = this.corpus.getDimensions();

		String svdFilename = "tml_" +
			this.corpus.getFilename() + "_" +
			this.corpus.getParameters() + "_DIM_" +
			this.dimensionsKept + ".svd";

		File svdFile = new File(this.corpus.getRepository().getSvdStoragePath()
				+ "/" + svdFilename);

		boolean readSVDFromFile = false;
		if(this.corpus.getPassages().length * this.corpus.getTerms().length > MAX_MATRIX_SIZE) {
			if(svdFile.exists()) {
				try {
					SVD svd = SVD.readSVD(svdFile);
					readSVDFromFile = true;
					this.Uk = new Matrix(svd.getUkdata());
					this.Sk = new Matrix(svd.getSkdata());
					this.Vk = new Matrix(svd.getVkdata());
					logger.debug("Big corpus, SVD file exists, reading it.");
				} catch (Exception e) {
					logger.debug("Big corpus, SVD file exists, but there were problems reading it.");
					logger.error(e);
				}
			} else {
				logger.debug("Big corpus, but SVD file wasn't found.");				
			}
		}

		if(this.corpus.getParameters().isLanczosSVD()
				&& !readSVDFromFile 
				&& this.corpus.getParameters().getDimensionalityReduction() 
				!= DimensionalityReduction.VARPCT) {
			logger.debug("Using Lanczos");
			LanczosSVDLIBCUtils utils = null;
			try {
				utils = new LanczosSVDLIBCUtils();
				utils.runLanczos(this.corpus, svdFilename);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				this.Uk = null;
				this.Sk = null;
				this.Vk = null;
				return null;
			}
			this.Uk = utils.getU();
			this.Sk = utils.getS();
			this.Vk = utils.getV();
		}
		else if(!readSVDFromFile){
			logger.debug("Using Jama SVD");
			SingularValueDecomposition svd = termDoc.svd();
			this.Uk = new Matrix(svd.getU().getArray());
			this.Sk = new Matrix(svd.getS().getArray());
			this.Vk = new Matrix(svd.getV().getArray());

			boolean invert = false;
			for(int i=0;i<this.Uk.getRowDimension();i++) {
				if(this.Uk.get(i, 0) < 0) {
					invert = true;
					break;
				}
			}
			if(invert) {
				logger.warn("Matrix inverted because first dimensions caused negative singular vectors in Jama");
				this.Uk = this.Uk.times(-1);
				this.Vk = this.Vk.times(-1);
			}
		}

		if(this.corpus.getParameters().getDimensionalityReduction() != DimensionalityReduction.NO
				&& !readSVDFromFile) {
			// Really reducing the dimensions of the matrices
			Matrix nUk = new Matrix(this.Uk.getRowDimension(), dimensionsKept);
			nUk.setMatrix(0, nUk.getRowDimension()-1, 0, dimensionsKept-1, this.Uk);
			this.Uk = nUk;
			Matrix nVk = new Matrix(this.Vk.getRowDimension(), dimensionsKept);
			nVk.setMatrix(0, nVk.getRowDimension()-1, 0, dimensionsKept-1, this.Vk);
			this.Vk = nVk;
			Matrix nSk = new Matrix(dimensionsKept, dimensionsKept);
			for(int i=0;i<dimensionsKept;i++)
				nSk.set(i, i, this.Sk.get(i, i));
			this.Sk = nSk;
		}

		if(this.corpus.getPassages().length * this.corpus.getTerms().length > 10000
				&& !readSVDFromFile) {
			SVD svd = new SVD();
			svd.setUkdata(this.Uk.getArray());
			svd.setSkdata(this.Sk.getArray());
			svd.setVkdata(this.Vk.getArray());

			try {
				svd.saveSVD(svdFile);
			} catch (Exception e) {
				logger.error(e);
			}
		}

		try {
			termDoc = this.Uk.times(this.Sk).times(
					this.Vk.transpose());
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger
			.error("Problem reconstructing with reconstructing the matrix after DR");
			throw ex;
		}

		return termDoc;
	}
	/**
	 * Calculates the term by doc matrix for the {@link SemanticSpace} based on
	 * the documents in the {@link Corpus}.
	 * 
	 * @throws NotEnoughTermsInCorpusException
	 * @throws TermWeightingException
	 * @throws NormalizationException
	 */
	public void calculate() throws NotEnoughTermsInCorpusException {

		// First of all we check that the corpus contains enough terms to
		// calculate a semantic space
		if (this.getCorpus().getTerms().length <= 0
				|| this.getCorpus().getTerms().length < this
				.getCorpus().getPassages().length - 1) {
			throw new NotEnoughTermsInCorpusException();
		}

		this.processingTime = System.currentTimeMillis();

		Matrix m = this.corpus.getTermDocMatrix();

		if(!this.corpus.isProjection()) {
			// Apply the dimensionality reduction
			m = this.applyDimensionalityReduction(m);
		}

		this.processingTime = System.currentTimeMillis()
		- this.processingTime;

		logger.info("Semantic space calculated in "
				+ this.processingTime + " ms. " +
				"Parameters:" + this.corpus.getParameters());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SemanticSpace clone = (SemanticSpace) super.clone();
		if(this.Uk != null)
			clone.Uk = this.Uk.copy();
		if(this.Sk != null)
			clone.Sk = this.Sk.copy();
		if(this.Vk != null)
			clone.Vk = this.Vk.copy();
		return clone;
	}

	/**
	 * @return the {@link Corpus} that a {@link SemanticSpace} uses
	 */
	public Corpus getCorpus() {
		return this.corpus;
	}

	/**
	 * @return the number of dimensions that the space kept
	 */
	public int getDimensionsKept() {
		return dimensionsKept;
	}

	/**
	 * Gets the name of the {@link SemanticSpace}
	 * 
	 * @return a String with the name
	 */
	public String getName() {
		return "Semantic space for " + this.getCorpus().getName();
	}

	/**
	 * The time that the {@link SemanticSpace} took to calculate its basic
	 * operations
	 * 
	 * @return time in milliseconds
	 */
	public long getProcessingTime() {
		return this.processingTime;
	}

	/**
	 * @return the sk
	 */
	public Matrix getSk() {
		return Sk;
	}

	/**
	 * @return The Ak reduced term-documents matrix.
	 */
	public Matrix getTermsDocuments() {
		return this.Uk.times(this.Sk).times(this.Vk.transpose());
	}

	/**
	 * @return the time taken to calculate the semantic space
	 */
	public long getTimeToCalculate() {
		return processingTime;
	}

	/**
	 * @return the uk
	 */
	public Matrix getUk() {
		return Uk;
	}

	/**
	 * @return the vk
	 */
	public Matrix getVk() {
		return Vk;
	}

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	/**
	 * @param vk the vk to set
	 */
	public void setVk(Matrix vk) {
		Vk = vk;
	}

	/**
	 * Overrides the default toString method and replaces it with the
	 * {@link SemanticSpace} name
	 */
	@Override
	public String toString() {
		if (this.getName() == null)
			return super.toString();
		return this.getName();
	}
}
