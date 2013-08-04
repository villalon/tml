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
 * This class represents an exception thrown when the 
 * {@link Corpus} does not contain enough terms, therefore
 * the SVD decomposition can't be performed
 * @author Jorge Villalon
 *
 */
public class NotEnoughTermsInCorpusException extends Exception {

	private static final long serialVersionUID = 470519975259755964L;

	/**
	 * Constructor 
	 */
	public NotEnoughTermsInCorpusException() {
		super(
				"The Corpus contains more text passages than terms. SVD can't be calculated.");
	}
	
	/**
	 * Constructor 
	 * @param e 
	 */
	public NotEnoughTermsInCorpusException(Exception e) {
		super(
				"The Corpus contains more text passages than terms. SVD can't be calculated.", e);
	}
}
