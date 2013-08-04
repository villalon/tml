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
package tml.test;


import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.Corpus;
import tml.corpus.SearchResultsCorpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.utils.LuceneUtils;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;

import Jama.Matrix;

public class ValidateIntroToLSATest extends AbstractTmlIndexingTest {

	private static String[] documents = {
		"c1","c2","c3","c4","c5","m1","m2","m3","m4"
	};

	private static String[] terms = {
		"computer",
		"ep", // stemmed version of EPS
		"graph",
		"human",
		"interface",
		"minors",
		"response",
		"survey",
		"system",
		"time",
		"trees",
		"user"		
	};

	private static double[][] U = {
		{0.24,0.04,-0.16,-0.59,-0.11,-0.25,-0.3,0.06,0.49},
		{0.3,-0.14,0.33,0.19,0.11,0.27,0.03,-0.02,-0.17},
		{0.04,0.62,0.22,0,-0.07,0.11,0.16,-0.68,0.23},
		{0.22,-0.11,0.29,-0.41,-0.11,-0.34,0.52,-0.06,-0.41},
		{0.2,-0.07,0.14,-0.55,0.28,0.5,-0.07,-0.01,-0.11},
		{0.03,0.45,0.14,-0.01,-0.3,0.28,0.34,0.68,0.18},
		{0.27,0.11,-0.43,0.07,0.08,-0.17,0.28,-0.02,-0.05},
		{0.21,0.27,-0.18,-0.03,-0.54,0.08,-0.47,-0.04,-0.58},
		{0.64,-0.17,0.36,0.33,-0.16,-0.21,-0.17,0.03,0.27},
		{0.27,0.11,-0.43,0.07,0.08,-0.17,0.28,-0.02,-0.05},
		{0.01,0.49,0.23,0.03,0.59,-0.39,-0.29,0.25,-0.23},
		{0.4,0.06,-0.34,0.1,0.33,0.38,0,0,0.01}
	};

	private static double[] S = {
		3.34,
		2.54,
		2.35,
		1.64,
		1.50,
		1.31,
		0.85,
		0.56,
		0.36
	};

	private static double[][] V = {
		{0.2,-0.06,0.11,-0.95,0.05,-0.08,0.18,-0.01,-0.06},
		{0.61,0.17,-0.5,-0.03,-0.21,-0.26,-0.43,0.05,0.24},
		{0.46,-0.13,0.21,0.04,0.38,0.72,-0.24,0.01,0.02},
		{0.54,-0.23,0.57,0.27,-0.21,-0.37,0.26,-0.02,-0.08},
		{0.28,0.11,-0.51,0.15,0.33,0.03,0.67,-0.06,-0.26},
		{0,0.19,0.1,0.02,0.39,-0.3,-0.34,0.45,-0.62},
		{0.01,0.44,0.19,0.02,0.35,-0.21,-0.15,-0.76,0.02},
		{0.02,0.62,0.25,0.01,0.15,0,0.25,0.45,0.52},
		{0.08,0.53,0.08,-0.03,-0.6,0.36,0.04,-0.07,-0.45},
	};

	private static double[][] termdoc = {
		{1, 1, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 1, 1, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 1, 1, 1},
		{1, 0, 0, 1, 0, 0, 0, 0, 0},
		{1, 0, 1, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 1, 1},
		{0, 1, 0, 0, 1, 0, 0, 0, 0},
		{0, 1, 0, 0, 0, 0, 0, 0, 1},
		{0, 1, 1, 2, 0, 0, 0, 0, 0},
		{0, 1, 0, 0, 1, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 1, 1, 1, 0},
		{0, 1, 1, 0, 1, 0, 0, 0, 0}
	};

	private static double[][] termdocK = {
		{0.15,0.51,0.36,0.41,0.24,0.02,0.06,0.09,0.12},
		{0.22,0.55,0.51,0.63,0.24,-0.07,-0.14,-0.2,-0.11},
		{-0.06,0.34,-0.15,-0.3,0.2,0.31,0.69,0.98,0.85},
		{0.16,0.4,0.38,0.47,0.18,-0.05,-0.12,-0.16,-0.09},
		{0.14,0.37,0.33,0.4,0.16,-0.03,-0.07,-0.1,-0.04},
		{-0.04,0.25,-0.1,-0.21,0.15,0.22,0.5,0.71,0.62},
		{0.16,0.58,0.38,0.42,0.28,0.06,0.13,0.19,0.22},
		{0.1,0.53,0.23,0.21,0.27,0.14,0.31,0.44,0.42},
		{0.45,1.23,1.05,1.27,0.56,-0.07,-0.15,-0.21,-0.05},
		{0.16,0.58,0.38,0.42,0.28,0.06,0.13,0.19,0.22},
		{-0.06,0.23,-0.14,-0.27,0.14,0.24,0.55,0.77,0.66},
		{0.26,0.84,0.61,0.7,0.39,0.03,0.08,0.12,0.19}		
	};

	private static Corpus corpus = null;
	private static Corpus corpusLanczos = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		documentsFolder = Configuration.getTmlFolder() + "/corpora/introLSA";
		repository.addDocumentsInFolder(documentsFolder);
		
		corpus = new SearchResultsCorpus("type:document");
		corpus.getParameters().setTermWeightLocal(LocalWeight.TF);
		corpus.getParameters().setTermWeightGlobal(GlobalWeight.None);
		corpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NO);
		corpus.getParameters().setDimensionalityReductionThreshold(2);
		corpus.load(repository);
		
		corpusLanczos = new SearchResultsCorpus("type:document");
		corpusLanczos.getParameters().setTermWeightLocal(LocalWeight.TF);
		corpusLanczos.getParameters().setTermWeightGlobal(GlobalWeight.None);
		corpusLanczos.getParameters().setDimensionalityReduction(DimensionalityReduction.NO);
		corpusLanczos.getParameters().setDimensionalityReductionThreshold(2);
		corpusLanczos.getParameters().setLanczosSVD(true);
		corpusLanczos.load(repository);
	}

	@Test
	public void validateTermsAndDocuments() throws Exception {

		for(int i=0;i<corpus.getTerms().length; i++) {
			String term = corpus.getTerms()[i];
			assertEquals(term, LuceneUtils.stemWords(terms[i]));
		}

		for(int i=0;i<corpus.getPassages().length; i++) {
			String passage = corpus.getPassages()[i];
			assertEquals(passage, documents[i]);
		}
	}

	@Test
	public void validateTermDocMatrix() {
		Matrix actual = corpus.getTermDocMatrix();
		Matrix expected = new Matrix(termdoc);

		assertEquals(expected.getRowDimension(), actual.getRowDimension());
		assertEquals(expected.getColumnDimension(), actual.getColumnDimension());

		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

	@Test
	public void validateU() {
		Matrix expected = corpus.getSemanticSpace().getUk(); 
		Matrix actual = new Matrix(U);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(Math.abs(expected.get(i, j)), Math.abs(actual.get(i, j)), 0.01);
			}
		}
	}

	@Test
	public void validateS() {
		Matrix expected = corpus.getSemanticSpace().getSk(); 
		Matrix actual = new Matrix(S.length, S.length);
		for(int i=0; i<S.length;i++)
			actual.set(i, i, S[i]);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

	@Test
	public void validateV() {
		Matrix actual = corpus.getSemanticSpace().getVk(); 
		Matrix expected = new Matrix(V);
		
		expected.print(5, 2);
		actual.print(5, 2);

		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(Math.abs(expected.get(i, j)), Math.abs(actual.get(i, j)), 0.01);
			}
		}
	}


	@Test
	public void validateTermDocMatrixReduced() throws Exception {
		corpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		corpus.load(repository);
		corpus.getSemanticSpace().calculate();
		
		Matrix actual = corpus.getSemanticSpace().getTermsDocuments();
		Matrix expected = new Matrix(termdocK);
		
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.05);
			}
		}
	}

	// TODO: Fix Lanczos SVD and then uncomment the tests
/*	@Test
	public void validateULanczos() {
		Matrix expected = corpusLanczos.getSemanticSpace().getUk(); 
		Matrix actual = new Matrix(U);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(Math.abs(expected.get(i, j)), Math.abs(actual.get(i, j)), 0.01);
			}
		}
	}

	@Test
	public void validateSLanczos() {
		Matrix expected = corpusLanczos.getSemanticSpace().getSk(); 
		Matrix actual = new Matrix(S.length, S.length);
		for(int i=0; i<S.length;i++)
			actual.set(i, i, S[i]);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

	@Test
	public void validateVLanczos() {
		Matrix actual = corpusLanczos.getSemanticSpace().getVk(); 
		Matrix expected = new Matrix(V);
		
		expected.print(5, 2);
		actual.print(5, 2);

		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(Math.abs(expected.get(i, j)), Math.abs(actual.get(i, j)), 0.01);
			}
		}
	}

	@Test
	public void validateTermDocMatrixReducedLanczos() throws Exception {
		corpusLanczos.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		corpusLanczos.load(repository);
		corpusLanczos.getSemanticSpace().calculate();
		
		Matrix actual = corpusLanczos.getSemanticSpace().getTermsDocuments();
		Matrix expected = new Matrix(termdocK);
		
		assertEquals(expected.getRowDimension(), actual.getRowDimension());
		assertEquals(expected.getColumnDimension(), actual.getColumnDimension());

		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.05);
			}
		}
	}*/
}
