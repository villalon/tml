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

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import tml.corpus.TextDocument;
import tml.storage.importers.TextImporter;
import tml.vectorspace.TermWeighting;
import tml.vectorspace.operations.Readability;
import tml.vectorspace.operations.results.ReadabilityResult;

/**
 * This class tests the {@link Readability} operation.
 * 
 * @author Stephen O'Rourke
 * 
 */
public class ReadabilityTest extends AbstractTmlIndexingTest {

	private static TextDocument document;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		String content = "The cat sat on the mat. On the mat the cat sat.\nThe feline reclined on the axminster.";
		repository.addDocument("1", content, "Title", "N/A", new TextImporter());

		document = repository.getTextDocument("1");
		document.getParameters().setTermWeightLocal(TermWeighting.LocalWeight.TF);
		document.getParameters().setTermWeightGlobal(TermWeighting.GlobalWeight.None);
		document.load(repository);
	}

	@Test
	public void shouldCalculateReadability() throws Exception {
		Readability operation = new Readability();
		operation.setCorpus(document.getParagraphCorpus());
		operation.start();

		assertEquals(operation.getResultsNumber(), 2);

		ReadabilityResult result1 = operation.getResults().get(0);
		assertEquals(result1.getDiffGradeLevel(), 9.83, 0.005);
		assertEquals(result1.getDiffReadingEase(), 70.5, 0.005);
		assertEquals(result1.getFleshKincaidGradeLevel(), -1.45, 0.005);
		assertEquals(result1.getFleshReadingEase(), 116.19, 0.05);

		ReadabilityResult result2 = operation.getResults().get(1);
		assertEquals(result2.getDiffGradeLevel(), 0.0, 0.0);
		assertEquals(result2.getDiffReadingEase(), 0.0, 0.0);
		assertEquals(result2.getFleshKincaidGradeLevel(), 8.38, 0.005);
		assertEquals(result2.getFleshReadingEase(), 45.69, 0.05);
	}
}
