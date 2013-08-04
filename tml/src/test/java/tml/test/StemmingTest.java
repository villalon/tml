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
/**
 * 
 */
package tml.test;

import org.junit.Test;

import tml.utils.LuceneUtils;

import static org.junit.Assert.*;



/**
 * This class test that the stemming algorithm is working appropriately.
 * 
 * @author Jorge Villalon
 *
 */
public class StemmingTest {

	@Test
	public void testStemming() {
		String[] words = {"increase","increasing","increased","increases","dog","dogs"};
		String[] stemmedWords = {"increas","increas","increas","increas","dog","dog"};
		
		for(int i=0; i<words.length; i++) {
			String word = words[i];
			String stem = stemmedWords[i];
			String stemmed = LuceneUtils.stemWords(word); 

			assertTrue(stemmed.equals(stem));
		}
	}
}
