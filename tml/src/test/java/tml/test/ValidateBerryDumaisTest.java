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
import tml.corpus.SearchResultsCorpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.utils.LuceneUtils;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.operations.PassagesSimilarity;
import tml.vectorspace.operations.results.PassageSimilarityResult;
import static org.junit.Assert.*;

import Jama.Matrix;

public class ValidateBerryDumaisTest extends AbstractTmlIndexingTest {

	private static double[][] termDoc = {
		{0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
		{0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0},
		{1, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0},
		{0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
		{0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
		{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
		{0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1}
	};
	private static String[] documents = {
		"B01","B02","B03","B04","B05","B06","B07","B08","B09","B10","B11","B12","B13","B14","B15","B16","B17"
	};

	private static String[] terms = {
		"algorithms",
		"application",
		"delay",
		"differential",
		"equations",
		"implementation",
		"integral",
		"introduction",
		"methods",
		"nonlinear",
		"ordinary",
		"oscillation",
		"partial",
		"problem",
		"systems",
		"theory"
	};

	private static double[][] Uk = {
		{0.0159, -0.4317},
		{0.0266, -0.3756},
		{0.1785, -0.1692},
		{0.6014, 0.1187},
		{0.6691, 0.1209},
		{0.0148, -0.3603},
		{0.052, -0.2248},
		{0.0066, -0.112},
		{0.1503, 0.1127},
		{0.0813, 0.0672},
		{0.1503, 0.1127},
		{0.1785, -0.1692},
		{0.1415, 0.0974},
		{0.0105, -0.2363},
		{0.0952, 0.0399},
		{0.2051, -0.5448}
	};

	private static double[][] Sk = {
		{4.5314, 0},	
		{0, 2.7582}		
	};

	private double[][] queryVector = {
			{0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1}
	};

	private double[][] queryProjected = {
			{0.0511,-0.3337}	
	};

	private double[][] distances2Factors = {
			{17, 0.99},
			{3, 0.99},
			{6, 0.99},
			{16, 0.99},
			{5, 0.98},
			{7, 0.98},
			{12, 0.55},
			{11, 0.55},
			{1, 0.38}
	};

	private double[][] distances4Factors = {
			{17, 0.87},
			{3, 0.82},
			{12, 0.57},
			{11, 0.57},
			{16, 0.38},
			{7, 0.38},
			{1, 0.35},
			{5, 0.22}
	};

	private double[][] distances8Factors = {
			{17, 0.88},
			{3, 0.78},
			{12, 0.37},
			{11, 0.37}
	};

	private static Corpus corpus = null;
//	private static Corpus corpusLanczos = null;
	private static Corpus queryCorpus = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/BerryDumais");
		
		corpus = new SearchResultsCorpus("type:document AND -externalid:Q01");
		corpus.getParameters().setTermSelectionCriterion(TermSelection.DF);
		corpus.getParameters().setTermSelectionThreshold(2);
		corpus.getParameters().setTermWeightLocal(LocalWeight.TF);
		corpus.getParameters().setTermWeightGlobal(GlobalWeight.None);
		corpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		corpus.getParameters().setDimensionalityReductionThreshold(2);
		corpus.load(repository);
		
//		corpusLanczos = new SearchResultsCorpus("type:document AND -externalid:Q01");
//		corpusLanczos.getParameters().setTermWeightLocal(LocalWeight.TF);
//		corpusLanczos.getParameters().setTermWeightGlobal(GlobalWeight.None);
//		corpusLanczos.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
//		corpusLanczos.getParameters().setDimensionalityReductionThreshold(2);
//		corpusLanczos.getParameters().setLanczosSVD(true);
//		corpusLanczos.load(repository);
		
		queryCorpus = new SearchResultsCorpus("type:document AND externalid:Q01");
		queryCorpus.getParameters().setTermSelectionThreshold(0);
		queryCorpus.getParameters().setTermWeightLocal(LocalWeight.TF);
		queryCorpus.getParameters().setTermWeightGlobal(GlobalWeight.None);
		queryCorpus.setProjection(true);
		queryCorpus.load(repository);
	}

	@Test
	public void validateTermsAndDocuments() throws Exception {

		assertEquals(terms.length, corpus.getTerms().length);
		
		for(int i=0;i<corpus.getTerms().length; i++) {
			String term = corpus.getTerms()[i];
			assertEquals(term, LuceneUtils.stemWords(terms[i]));
		}

		for(int i=0;i<corpus.getPassages().length; i++) {
			String passage = corpus.getPassages()[i];
			assertEquals(passage, LuceneUtils.stemWords(documents[i]));
		}
	}

	@Test
	public void validateTermDocMatrix() {
		Matrix actual = corpus.getTermDocMatrix();
		Matrix expected = new Matrix(termDoc);

		actual.print(5, 2);
		expected.print(5, 2);
		
		assertEquals(expected.getRowDimension(), actual.getRowDimension());
		assertEquals(expected.getColumnDimension(), actual.getColumnDimension());

		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

	@Test
	public void validateUk() {
		Matrix expected = corpus.getSemanticSpace().getUk(); 
		Matrix actual = new Matrix(Uk);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

	@Test
	public void validateSk() {
		Matrix expected = corpus.getSemanticSpace().getSk(); 
		Matrix actual = new Matrix(Sk);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

/*	@Test
	public void validateUkLanczos() {
		Matrix expected = corpusLanczos.getSemanticSpace().getUk(); 
		Matrix actual = new Matrix(Uk);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

	@Test
	public void validateSkLanczos() {
		Matrix expected = corpusLanczos.getSemanticSpace().getSk(); 
		Matrix actual = new Matrix(Sk);
		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}*/

	@Test
	public void validateQuery() {
		Matrix mUk = new Matrix(Uk);
		Matrix mSk = new Matrix(Sk);
		Matrix q = new Matrix(queryVector).transpose();

		Matrix actual = q.transpose().times(mUk).times(mSk.inverse());
		Matrix expected = new Matrix(queryProjected);

		for(int i=0; i<actual.getRowDimension(); i++) {
			for(int j=0; j<actual.getColumnDimension(); j++) {
				assertEquals(expected.get(i, j), actual.get(i, j), 0.01);
			}
		}
	}

	@Test
	public void validateProjection2Factors() throws Exception {

		PassagesSimilarity similarity = new PassagesSimilarity();
		similarity.setCorpus(queryCorpus);
		similarity.setBackgroundKnowledgeCorpus(corpus);
		similarity.setIncludeBackgroundInSimilarity(true);
		similarity.setSortBySimilarity(true);
		similarity.start();

		double[] similarities = new double[distances2Factors.length];
		int current = 0;
		for(PassageSimilarityResult result : similarity.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
				if(current >= similarities.length)
					break;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(distances2Factors[i][1] + "," +  similarities[i]);
			assertEquals(distances2Factors[i][1], similarities[i], 0.1);
		}
	}
	
	@Test
	public void validateProjection4Factors() throws Exception {

		corpus.getParameters().setDimensionalityReductionThreshold(4);
		corpus.load(repository);
		corpus.getSemanticSpace().calculate();
		
		PassagesSimilarity similarity = new PassagesSimilarity();
		similarity.setCorpus(queryCorpus);
		similarity.setBackgroundKnowledgeCorpus(corpus);
		similarity.setIncludeBackgroundInSimilarity(true);
		similarity.setSortBySimilarity(true);
		similarity.start();

		double[] similarities = new double[distances4Factors.length];
		int current = 0;
		for(PassageSimilarityResult result : similarity.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
				if(current >= similarities.length)
					break;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(distances4Factors[i][1] + "," +  similarities[i]);
			assertEquals(distances4Factors[i][1], similarities[i], 0.1);
		}
	}
	
	@Test
	public void validateProjection8Factors() throws Exception {

		corpus.getParameters().setDimensionalityReductionThreshold(8);
		corpus.load(repository);
		corpus.getSemanticSpace().calculate();
		
		PassagesSimilarity similarity = new PassagesSimilarity();
		similarity.setCorpus(queryCorpus);
		similarity.setBackgroundKnowledgeCorpus(corpus);
		similarity.setIncludeBackgroundInSimilarity(true);
		similarity.setSortBySimilarity(true);
		similarity.start();

		double[] similarities = new double[distances8Factors.length];
		int current = 0;
		for(PassageSimilarityResult result : similarity.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
				if(current >= similarities.length)
					break;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(distances8Factors[i][1] + "," +  similarities[i]);
			assertEquals(distances8Factors[i][1], similarities[i], 0.1);
		}
	}
	
/*	@Test
	public void validateProjection2FactorsLanczos() throws Exception {

		PassagesSimilarity similarity = new PassagesSimilarity();
		similarity.setCorpus(queryCorpus);
		similarity.setBackgroundKnowledgeCorpus(corpusLanczos);
		similarity.setIncludeBackgroundInSimilarity(true);
		similarity.setSortBySimilarity(true);
		similarity.start();

		double[] similarities = new double[distances2Factors.length];
		int current = 0;
		for(PassageSimilarityResult result : similarity.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
				if(current >= similarities.length)
					break;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(distances2Factors[i][1] + "," +  similarities[i]);
			assertEquals(distances2Factors[i][1], similarities[i], 0.1);
		}
	}
	
	@Test
	public void validateProjection4FactorsLanczos() throws Exception {

		corpusLanczos.getParameters().setDimensionalityReductionThreshold(4);
		corpusLanczos.load(repository);
		corpusLanczos.getSemanticSpace().calculate();
		
		PassagesSimilarity similarity = new PassagesSimilarity();
		similarity.setCorpus(queryCorpus);
		similarity.setBackgroundKnowledgeCorpus(corpusLanczos);
		similarity.setIncludeBackgroundInSimilarity(true);
		similarity.setSortBySimilarity(true);
		similarity.start();

		double[] similarities = new double[distances4Factors.length];
		int current = 0;
		for(PassageSimilarityResult result : similarity.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
				if(current >= similarities.length)
					break;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(distances4Factors[i][1] + "," +  similarities[i]);
			assertEquals(distances4Factors[i][1], similarities[i], 0.1);
		}
	}
	
	@Test
	public void validateProjection8FactorsLanczos() throws Exception {

		corpusLanczos.getParameters().setDimensionalityReductionThreshold(8);
		corpusLanczos.load(repository);
		corpusLanczos.getSemanticSpace().calculate();
		
		PassagesSimilarity similarity = new PassagesSimilarity();
		similarity.setCorpus(queryCorpus);
		similarity.setBackgroundKnowledgeCorpus(corpusLanczos);
		similarity.setIncludeBackgroundInSimilarity(true);
		similarity.setSortBySimilarity(true);
		similarity.start();

		double[] similarities = new double[distances8Factors.length];
		int current = 0;
		for(PassageSimilarityResult result : similarity.getResults()) {
			logger.debug(
					result.getDocumentA()  +"-"+
					result.getDocumentB()+":"+
					(new DecimalFormat("0.000")).format(result.getSimilarity()));
			if(!result.getDocumentA().equals(result.getDocumentB())) {
				similarities[current] = result.getSimilarity();
				current++;
				if(current >= similarities.length)
					break;
			}
		}
		
		for(int i=0;i<similarities.length;i++) {
			logger.debug(distances8Factors[i][1] + "," +  similarities[i]);
			assertEquals(distances8Factors[i][1], similarities[i], 0.1);
		}
	}*/
}
