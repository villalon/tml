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
import tml.corpus.CorpusParameters.TermSelection;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;



public class LanczosTest extends AbstractTmlIndexingTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/introLSA");
	}
	
	@Test
	public void timeBigCorpus() throws Exception {
		SearchResultsCorpus corpus = new SearchResultsCorpus("type:document");
		corpus.getParameters().setTermSelectionCriterion(TermSelection.DF);
		corpus.getParameters().setTermSelectionThreshold(2);
		corpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		corpus.getParameters().setDimensionalityReductionThreshold(2);
		corpus.getParameters().setTermWeightGlobal(GlobalWeight.None);
		corpus.getParameters().setTermWeightLocal(LocalWeight.TF);
		corpus.load(repository);
		corpus.getParameters().setLanczosSVD(true);
		corpus.getSemanticSpace().calculate();
	}
}
