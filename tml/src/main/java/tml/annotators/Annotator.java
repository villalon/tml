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
package tml.annotators;

import java.util.ArrayList;

/**
 * Common interface for all annotators. Each annotator will be called
 * from the {@link Repository} to analyze each sentence and then
 * store the annotated text in a Lucene field while indexing.
 * 
 * @author Jorge Villalon
 *
 */
public interface Annotator {

	/**
	 * This method returns the XML annotated
	 * version of a text. E.g if we have
	 * "Rafa is in the US" the annotated version
	 * would be "<person p="1">Rafa</person><country p="5,8">US</country>".
	 * 
	 * TODO: Analyze if UIMA provides a better annotation schema
	 * 
	 * @param text the text to be annotated
	 * @return the XML
	 */
	public String getAnnotations(String text);
	
	/**
	 * The Lucene field name where this annotations are
	 * going to be stored.
	 * 
	 * @return the Lucene field name
	 */
	public String getFieldName();
	
	/**
	 * The schema by which these annotations can be verified.
	 * 
	 * @return null if no schema is attached
	 */
	public Object getSchema();
	
	/**
	 * Returns the pieces of text (words or phrases) in the text that
	 * are annotated with a particular label.
	 * 
	 * @param annotationLabel the label to search
	 * @return a list of text. Null if no text is found.
	 */
	public String[] getAnnotatedText(String annotationLabel);
	
	/**
	 * This method initialises any static attributes required for the annotator to run
	 */
	public void init();
	
	public ArrayList<String> getTypes();
}
