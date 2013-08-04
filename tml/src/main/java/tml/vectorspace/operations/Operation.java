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

package tml.vectorspace.operations;

import java.util.List;

import tml.corpus.Corpus;


/**
 * This interface represents an operation on the {@link SemanticSpace} of a {@link Corpus}.
 * @author Jorge Villalon
 *
 * @param <E> the class representing the results of the operation
 */
public interface Operation<E> {

	/**
	 * @return the name of the operation in a human readable way
	 */
	public String getName();

	/**
	 * @param corpus sets the {@link Corpus} that will be used for the operation
	 */
	public void setCorpus(Corpus corpus);

	/**
	 * @return the {@link Corpus} used for the operation
	 */
	public Corpus getCorpus();

	/**
	 * @return the time the operation took
	 */
	public long getTimeElapsed();

	/**
	 * Starts the operation execution
	 * @throws Exception
	 */
	public void start() throws Exception;

	/**
	 * @return the number of results the operation got
	 */
	public int getResultsNumber();

	/**
	 * @return the results of the operation
	 */
	public List<E> getResults();

	/**
	 * @return the table with the results
	 */
	public Object[][] getResultsTable();

	/**
	 * @return the table with the results
	 */
	public String[][] getResultsStringTable();

	/**
	 * @return the headers of the results table
	 */
	public Object[] getResultsTableHeader();

	/**
	 * @param maxResults the maximum results the operation will return
	 */
	public void setMaxResults(int maxResults);

	/**
	 * @return the maximum results set for this operation
	 */
	public int getMaxResults();

	/**
	 * Prints the results on the console using a comma as separator
	 */
	public void printResultsCSV();

	/**
	 * @return results are presented as a set of lines separated by commas
	 */
	public String getResultsCSVString();
	
	public String getResultsXML();

	/**
	 * Prints the results on the console a'la Matlab
	 */
	public void printResultsMatlab();
	public void setBackgroundKnowledgeCorpus(Corpus backgroundKnowledgeCorpus);
	public Corpus getBackgroundKnowledgeCorpus();
        public void addOperationListener(OperationListener listener);
        public void removeOperationListener(OperationListener listener);
}
