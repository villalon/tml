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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import tml.corpus.Corpus;
import tml.storage.Repository;
import tml.vectorspace.operations.results.AbstractResult;


import Jama.Matrix;
import javax.swing.event.EventListenerList;

/**
 * Abstract class for all operations, it contains the common attributes and the
 * start() and end() methods.
 * 
 * @author Jorge Villalon
 * 
 * @param <E>
 *            The class for the result of the particular operation
 */
public abstract class AbstractOperation<E> implements Operation<E> {

    protected static Logger logger = Logger.getLogger(AbstractOperation.class);
    protected long timeElapsed = 0;
    protected Date executionDate;
    protected int maxResults = -1;
    protected ArrayList<E> results = new ArrayList<E>();
    protected Corpus corpus = null;
    protected String name;
    protected double summaryResult = -1;
    protected Corpus backgroundKnowledge = null;
    protected Repository repository = null;
    protected boolean requiresSemanticSpace = true;
    protected EventListenerList listeners = null;

    /**
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }

    @Override
    public void addOperationListener(OperationListener listener) {
        this.listeners.add(OperationListener.class, listener);
    }

    @Override
    public void removeOperationListener(OperationListener listener) {
        this.listeners.remove(OperationListener.class, listener);
    }

    protected void operationPerformed(OperationEvent evt) {
        OperationListener[] list = this.listeners.getListeners(OperationListener.class);

        for (OperationListener listener : list) {
            listener.operationAction(evt);
        }
    }

    /**
     * Constructs a default operation with no {@link Corpus} attached to it
     */
    public AbstractOperation() {
        this(null);
    }

    /**
     * Creates a new instance with a corpus ready
     * @param corpus the corpus to process
     */
    public AbstractOperation(Corpus corpus) {
        this.setCorpus(corpus);
        this.name = this.getClass().getSimpleName();
        this.listeners = new EventListenerList();
    }

    protected void end() {
        this.timeElapsed = System.currentTimeMillis() - this.timeElapsed;
        String message = "Operation " + this.name + " finished,";
        if (this.results != null) {
            message += " " + this.results.size() + " results obtained";
        }
        message += " in " + this.timeElapsed + " ms.";
        logger.info(message);
    }

    @Override
    public Corpus getCorpus() {
        return corpus;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public String getName() {
        return name;
    }

    protected Object[][] getObjectArrayFromMatrix(Matrix m) {
        Object[][] data = new Object[m.getRowDimension()][m.getColumnDimension()];

        for (int i = 0; i < m.getRowDimension(); i++) {
            for (int j = 0; j < m.getColumnDimension(); j++) {
                data[i][j] = m.get(i, j);
            }
        }

        return data;
    }

    /**
     * @return the corpus used as background knowledge
     */
    @Override
    public Corpus getBackgroundKnowledgeCorpus() {
        return backgroundKnowledge;
    }

    @Override
    public List<E> getResults() {
        return this.results;
    }

    /**
     * @return results are presented as a set of lines separated by commas
     */
    @Override
    public String getResultsCSVString() {
        return getResultsString("", "", "", "\n", ",");
    }

    @Override
    public int getResultsNumber() {
        return this.results.size();
    }

    /**
     * @param start
     * @param end
     * @param startLine
     * @param endLine
     * @param separator
     * @return a String containing the representation of the results
     */
    public String getResultsString(String start, String end, String startLine,
            String endLine, String separator) {
        StringBuffer buff = new StringBuffer();
        Object[][] data = this.getResultsTable();
        Object[] headers = this.getResultsTableHeader();

        if (data == null || headers == null) {
            return "Operation doesn't implement results as output.";
        }

        buff.append(start);
        int columns = headers.length;
        int rows = data.length;
        buff.append(startLine);
        for (int j = 0; j < columns; j++) {
            if (j == columns - 1) {
                buff.append(this.getResultsTableHeader()[j]);
            } else {
                buff.append(this.getResultsTableHeader()[j] + separator);
            }
        }
        buff.append(endLine);
        for (int i = 0; i < rows; i++) {
            buff.append(startLine);
            for (int j = 0; j < columns; j++) {
                if (j == columns - 1) {
                    buff.append(data[i][j]);
                } else {
                    buff.append(data[i][j] + separator);
                }
            }
            buff.append(endLine);
        }
        buff.append(end);

        return buff.toString();
    }

    @Override
    public long getTimeElapsed() {
        return this.timeElapsed;
    }

    /**
     * Print results on the console
     */
    public void printResults() {
        printResults("", "", "", "\n", "|");
    }

    private void printResults(String start, String end, String startLine,
            String endLine, String separator) {
        System.out.print(getResultsString(start, end, startLine, endLine,
                separator));
    }

    /**
     * Prints the results on the console using a comma as separator
     */
    @Override
    public void printResultsCSV() {
        printResults("", "", "", "\n", ",");
    }

    /**
     * Prints the results on the console a'la Matlab
     */
    @Override
    public void printResultsMatlab() {
        printResults("[", "]", "", ";", " ");
    }

    @Override
    public void setCorpus(Corpus corpus) {
        this.corpus = corpus;
        if (corpus != null) {
            this.repository = this.corpus.getRepository();
        }
    }

    /**
     * Sets the maximum results the operation will return
     */
    @Override
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * @param backgroundKnowledgeCorpus the corpus that will be used as background knowledge
     */
    @Override
    public void setBackgroundKnowledgeCorpus(Corpus backgroundKnowledgeCorpus) {
        this.backgroundKnowledge = backgroundKnowledgeCorpus;
    }

    @Override
    public void start() throws Exception {
        logger.info("Operation " + this.name + " started");
        this.executionDate = new Date();
        this.timeElapsed = System.currentTimeMillis();

        if (backgroundKnowledge != null) {
            try {
                if (!backgroundKnowledge.getSemanticSpace().isCalculated()) {
                    backgroundKnowledge.getSemanticSpace().calculate();
                }
                Corpus projectedCorpus = backgroundKnowledge.projectCorpus(this.corpus);
                this.corpus = projectedCorpus;
            } catch (Exception e) {
                logger.error(e);
                return;
            }
        } else {
            if (requiresSemanticSpace && !this.corpus.getSemanticSpace().isCalculated()) {
                try {
                    this.corpus.getSemanticSpace().calculate();
                } catch (Exception e) {
                    e.printStackTrace();
                    this.corpus = null;
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.getName() + " " + this.corpus.getName();
    }

    @Override
    public Object[][] getResultsTable() {
        if (this.results == null || this.results.size() == 0) {
            return null;
        }

        E e = this.results.get(0);
        Object[][] data = new Object[this.results.size()][e.getClass().getDeclaredFields().length];
        int resultNumber = 0;
        for (E r : this.results) {
            AbstractResult result = (AbstractResult) r;
            try {
                data[resultNumber] = result.getValues();
            } catch (Exception e1) {
                logger.error(e1);
                continue;
            }
            resultNumber++;
        }
        return data;
    }
    
    @Override
	public String[][] getResultsStringTable() {
    	Object[][] resultsTable = getResultsTable();
    	Object[] resultsHeaders = getResultsTableHeader();
    	String[][] output = new String[resultsTable.length+1][resultsTable[0].length];
       	for(int j=0;j<resultsTable[0].length;j++)
       		output[0][j] = resultsHeaders[j].toString();
    	for(int i=0;i<resultsTable.length;i++)
        	for(int j=0;j<resultsTable[0].length;j++)
        		output[i+1][j] = resultsTable[i][j].toString();
    	return output;
    }

    @Override
    public Object[] getResultsTableHeader() {
        if (this.results == null || this.results.size() == 0) {
            return null;
        }

        AbstractResult result = (AbstractResult) this.results.get(0);

        return result.getHeaders();
    }
    
    @Override
    public String getResultsXML() {
    	return getResultsString("<?xml version=\"1.0\"?><results>", "</results>", "<result>", "</result>", "#");
    }
}
