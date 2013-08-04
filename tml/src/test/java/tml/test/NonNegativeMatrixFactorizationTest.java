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
import tml.corpus.TextDocument;
import tml.vectorspace.factorisation.NonnegativeMatrixFactorisationED;


import Jama.Matrix;

public class NonNegativeMatrixFactorizationTest extends AbstractTmlIndexingTest {

	private static TextDocument document;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/uppsala");
		
		document = repository.getTextDocument("0100.a1");
		document.load(repository);
	}

	@Test
	public void testMatrices() {
		Matrix m = document.getSentenceCorpus().getTermDocMatrix();
		m.print(10, 5);
		
		NonnegativeMatrixFactorisationED f = new NonnegativeMatrixFactorisationED();
		f.setK(5);
		f.process(m);
		
		new Matrix(f.getDecomposition().getUkdata()).print(10, 5);
		new Matrix(f.getDecomposition().getSkdata()).print(10, 5);
		new Matrix(f.getDecomposition().getVkdata()).print(10, 5);
	}
}
