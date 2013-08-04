/*******************************************************************************
 *  Copyright 2010 Stephen O'Rourke (stephen.orourke@sydney.edu.au)
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import tml.corpus.TextDocument;
import tml.storage.importers.TextImporter;
import tml.vectorspace.TermWeighting;
import tml.vectorspace.operations.RapidAutomaticKeywordExtraction;
import tml.vectorspace.operations.results.RapidAutomaticKeywordExtractionResult;

/**
 * This class tests the {@link RapidAutomaticKeywordExtraction} operation.
 * 
 * @author Stephen O'Rourke
 * 
 */
public class RapidAutomaticKeywordExtractionTest extends AbstractTmlIndexingTest {

	private static TextDocument document;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String content = "Compatibility of Systems of Linear Constraints over the Set of Natural Numbers" + 
		"\nCriteria of compatibility of a system of linear Diophantine equations, strict inequations, " + 
		"and nonstrict inequations are considered. Upper bounds for components of a minimal set " + 
		"of solutions and algorithms of construction of minimal generating sets of solutions for all " + 
		"types of systems are given. These criteria and the corresponding algorithms for " + 
		"constructing a minimal supporting set of solutions can be used in solving all the " + 
		"considered types of systems and systems of mixed types.";
		
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocument("1", content, "Title", "N/A", new TextImporter());

		document = repository.getTextDocument("1");
		document.getParameters().setTermWeightLocal(TermWeighting.LocalWeight.TF);
		document.getParameters().setTermWeightGlobal(TermWeighting.GlobalWeight.None);
		document.load(repository);
	}

	@Test
	public void shouldExtractKeywords() throws Exception {
		Map<String, Double> expectedKeywords = new LinkedHashMap<String, Double>();
		expectedKeywords.put("minimal generating sets", 8.7);
		expectedKeywords.put("linear diophantine equations", 8.5);
		expectedKeywords.put("minimal supporting set", 7.7);
		expectedKeywords.put("minimal set", 4.7);
		expectedKeywords.put("linear constraints", 4.5);
		expectedKeywords.put("natural numbers", 4.0);
		expectedKeywords.put("strict inequations", 4.0);
		expectedKeywords.put("nonstrict inequations", 4.0);
		expectedKeywords.put("upper bounds", 4.0);
		expectedKeywords.put("mixed types", 3.7);
		expectedKeywords.put("considered types", 3.2);
		expectedKeywords.put("set", 2.0);
		expectedKeywords.put("types", 1.7);
		expectedKeywords.put("considered", 1.5);
		expectedKeywords.put("compatibility", 1.0);
		expectedKeywords.put("systems", 1.0);
		expectedKeywords.put("criteria", 1.0);
		expectedKeywords.put("system", 1.0);
		expectedKeywords.put("components", 1.0);
		expectedKeywords.put("solutions", 1.0);
		expectedKeywords.put("algorithms", 1.0);
		expectedKeywords.put("construction", 1.0);
		expectedKeywords.put("constructing", 1.0);
		expectedKeywords.put("solving", 1.0);

		RapidAutomaticKeywordExtraction operation = new RapidAutomaticKeywordExtraction();
		operation.setCorpus(document.getSentenceCorpus());
		operation.start();

		assertThat(operation.getResultsNumber(), equalTo(expectedKeywords.size()));
		Iterator<String> keywords = expectedKeywords.keySet().iterator();
		for (RapidAutomaticKeywordExtractionResult result : operation.getResults()) {
			String keyword = keywords.next();
			assertThat(result.getKeyword(), equalTo(keyword));
			assertEquals(result.getWeighting(), expectedKeywords.get(keyword), 0.05);
		}
	}
}
