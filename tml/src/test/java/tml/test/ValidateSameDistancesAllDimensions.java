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


import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.SearchResultsCorpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.vectorspace.operations.PassageDistances;
import tml.vectorspace.operations.results.PassageDistancesResult;
import static org.junit.Assert.*;


public class ValidateSameDistancesAllDimensions extends AbstractTmlIndexingTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder()+ "/corpora/uppsala", 1);
	}
	
	@Test
	public void validatePassageDistances() throws Exception {
		SearchResultsCorpus corpusA = new SearchResultsCorpus("type:sentence AND reference:p*d0100.a1");
		corpusA.getParameters().setDimensionalityReduction(DimensionalityReduction.NO);
		corpusA.load(repository);
		
		SearchResultsCorpus corpusB = new SearchResultsCorpus("type:sentence AND reference:p*d0100.a1");
		corpusB.getParameters().setDimensionalityReduction(DimensionalityReduction.PCT);
		corpusB.getParameters().setDimensionalityReductionThreshold(100);
		corpusB.load(repository);
		
		SearchResultsCorpus corpusC = new SearchResultsCorpus("type:sentence AND reference:p*d0100.a1");
		corpusC.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		corpusC.getParameters().setDimensionalityReductionThreshold(100);
		corpusC.load(repository);
		
		PassageDistances distancesA = new PassageDistances();
		distancesA.setCorpus(corpusA);
		distancesA.start();
		
		PassageDistances distancesB = new PassageDistances();
		distancesB.setCorpus(corpusB);
		distancesB.start();
		
		PassageDistances distancesC = new PassageDistances();
		distancesC.setCorpus(corpusC);
		distancesC.start();
		
		assertEquals(distancesA.getResultsNumber(), distancesB.getResultsNumber());
		assertEquals(distancesB.getResultsNumber(), distancesC.getResultsNumber());
		
		for(int i=0; i<distancesA.getResultsNumber(); i++) {
			PassageDistancesResult resultA = distancesA.getResults().get(i);
			PassageDistancesResult resultB = distancesB.getResults().get(i);
			PassageDistancesResult resultC = distancesC.getResults().get(i);
			
			assertEquals(resultA.getDocumentAId(), resultB.getDocumentAId());
			assertEquals(resultA.getDocumentBId(), resultB.getDocumentBId());
			assertEquals(resultA.getDistance(), resultB.getDistance(), 0.001);
			assertEquals(resultB.getDocumentAId(), resultC.getDocumentAId());
			assertEquals(resultB.getDocumentBId(), resultC.getDocumentBId());
			assertEquals(resultB.getDistance(), resultC.getDistance(), 0.001);
		}
	}

}
