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

/**
 * This class implements an identifiable exception when
 * a {@link TextPassage} contains no words
 * @author Jorge Villalon
 *
 */
public class EmptyTextPassageException extends Exception {

	private static final long serialVersionUID = 7942208472521880051L;

	/**
	 * Constructor
	 */
	public EmptyTextPassageException() {
		super(
				"The text passage is empty after removing stopwords and stemming, can't be used in a Corpus.");
	}
	
	/**
	 * Constructor
	 * @param e 
	 */
	public EmptyTextPassageException(Exception e) {
		super(
				"The text passage is empty after removing stopwords and stemming, can't be used in a Corpus.", e);
	}
}
