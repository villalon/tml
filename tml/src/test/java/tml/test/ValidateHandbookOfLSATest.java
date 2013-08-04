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

import java.text.DecimalFormat;

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.Corpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.SearchResultsCorpus;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.operations.PassagesSimilarity;
import tml.vectorspace.operations.results.PassageSimilarityResult;
import static org.junit.Assert.*;

import Jama.Matrix;


public class ValidateHandbookOfLSATest extends AbstractTmlIndexingTest {

	private static double[][] docDistances = {
			{1.00, 0.81, 0.53, 0.4, 0.85, 0.91, 0.82, 0.8, 0.79},
			{0.81, 1, 0.93, 0.87, 0.37, 0.49, 0.32, 0.29, 0.27},
			{0.53, 0.93, 1, 0.99, 0, 0.13, -0.05, -0.09, -0.1},
			{0.40, 0.87, 0.99, 1, -0.14, -0.01, -0.19, -0.23, -0.24},
			{0.85, 0.37, 0, -0.14, 1, 0.99, 1, 1, 1},
			{0.91, 0.49, 0.13, -0.01, 0.99, 1, 0.98, 0.98, 0.97},
			{0.82, 0.33, -0.05, -0.19, 1, 0.98, 1, 1, 1},
			{0.8, 0.29, -0.09, -0.23, 1, 0.98, 1, 1, 1},
			{0.79, 0.27, -0.1, -0.24, 1, 0.97, 1, 1, 1} 
	};

	// Results obtained from WinGTP with queryfile containing "Recipe for White bread" and
	// the Handbook of LSA corpus. With no singular value scaling (no -S in qrun), and
	// 2 factors (-n in qrun).
	public static double[] queryResults2FactorsNoScaling= {
		0.99225,
		0.960501,
		0.952626,
		0.550482,
		0.181349,
		0.0642336,
		0.0232894,
		-0.0109286,
		-0.0234396
	};
	
	private static String[] terms = {
		"bread",
		"composition",
		"demonstration",
		"dough",
		"drum",
		"ingredients",
		"music",
		"recipe",
		"rock",
		"roll"
	};

	private static String[] documents = {
		"b1",
		"b2",
		"b3",
		"b4",
		"m1",
		"m2",
		"m3",
		"m4",
		"m5"
	};

	private static Corpus hbkLSAcorpus;

	private static Corpus hbkLSAqueries;

	private Matrix m;

	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		//AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/handbookOfLSA");
		
		hbkLSAcorpus = new SearchResultsCorpus("type:document AND (externalid:m* OR externalid:b*)");
		hbkLSAcorpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		hbkLSAcorpus.getParameters().setDimensionalityReductionThreshold(2);
		hbkLSAcorpus.getParameters().setTermWeightLocal(LocalWeight.LOGTF);
		hbkLSAcorpus.getParameters().setTermWeightGlobal(GlobalWeight.Entropy);
		hbkLSAcorpus.getParameters().setNormalizeDocuments(true);
		hbkLSAcorpus.load(repository);

		hbkLSAqueries = new SearchResultsCorpus(
		"type:document AND (externalid:q5)");
		hbkLSAqueries.getParameters().setTermSelectionThreshold(0);
		hbkLSAqueries.setProjection(true);
		hbkLSAqueries.load(repository);
		hbkLSAqueries.setName("Handbook of LSA queries");
		}

	@Test
	public void validateTermsAndDocuments() {
		
		assertEquals(terms.length, hbkLSAcorpus.getTerms().length);
		assertEquals(documents.length, hbkLSAcorpus.getPassages().length);
		
		for(int i=0;i<terms.length;i++) {
			assertEquals(tml.utils.LuceneUtils.stemWords(terms[i]), hbkLSAcorpus.getTerms()[i]);
		}
		
		for(int i=0;i<documents.length;i++) {
			assertEquals(documents[i], hbkLSAcorpus.getPassages()[i]);
		}
	}
	
	@Test
	public void simpleProjection() throws Exception {

		PassagesSimilarity operation = new PassagesSimilarity();
		operation.setCorpus(hbkLSAcorpus);
		operation.setBackgroundKnowledgeCorpus(null);
		operation.setIncludeBackgroundInSimilarity(false);
		operation.setIncludeBackgroundInResults(false);
		operation.start();
		
		m = new Matrix(docDistances);
		assertEquals(m.normF(), operation.getSimilarities().normF(), 0.01);
	}
	
	@Test
	public void validateSimilaritiesJama() throws Exception {
		PassagesSimilarity queryOperation = new PassagesSimilarity();
		queryOperation.setCorpus(hbkLSAqueries);
		queryOperation.setBackgroundKnowledgeCorpus(hbkLSAcorpus);
		queryOperation.setIncludeBackgroundInSimilarity(true);
		queryOperation.setIncludeBackgroundInResults(false);
		queryOperation.setSortBySimilarity(true);
		queryOperation.start();

		double[] similarities = new double[queryOperation.getResults().size()-1];
		int current = 0;
		for(PassageSimilarityResult result : queryOperation.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(queryResults2FactorsNoScaling[i] + "," +  similarities[i]);
			assertEquals(queryResults2FactorsNoScaling[i], similarities[i], 0.1);
		}
	}
	
/*	@Test
	public void validateSimilaritiesLanczos() throws Exception {
		
		hbkLSAcorpus.getParameters().setLanczosSVD(true);
		hbkLSAcorpus.getSemanticSpace().calculate();
		
		PassagesSimilarity operation = new PassagesSimilarity();
		operation.setCorpus(hbkLSAcorpus);
		operation.setBackgroundKnowledgeCorpus(null);
		operation.setIncludeBackgroundInSimilarity(false);
		operation.setIncludeBackgroundInResults(false);
		operation.start();
		
		assertEquals(m.normF(), operation.getSimilarities().normF(), 0.01);

		PassagesSimilarity queryOperation = new PassagesSimilarity();
		queryOperation.setCorpus(hbkLSAqueries);
		queryOperation.setBackgroundKnowledgeCorpus(hbkLSAcorpus);
		queryOperation.setIncludeBackgroundInSimilarity(true);
		queryOperation.setIncludeBackgroundInResults(false);
		queryOperation.setSortBySimilarity(true);
		queryOperation.start();

		double[] similarities = new double[queryOperation.getResults().size()-1];
		int current = 0;
		for(PassageSimilarityResult result : queryOperation.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(queryResults2FactorsNoScaling[i] + "," +  similarities[i]);
			assertEquals(queryResults2FactorsNoScaling[i], similarities[i], 0.1);
		}
	}*/
}
