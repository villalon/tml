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
package tml.corpus;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.storage.Repository;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.TermWeightingException;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;



/**
 * SimpleCorpus is a simple corpus which contains a set of documents from a
 * folder, it consider each document a vector. It automatically loads the
 * documents and creates a weighted matrix.
 * 
 * You can change the parameters for the term loading by accessing the internal
 * corpus. See more details in {@link Corpus}.
 * 
 * @author Jorge Villalon
 * @see Corpus
 * 
 */
public class SimpleCorpus {

	private Repository repository = null;
	private Corpus internalCorpus = null;
	private String pathToRepository = null;
	private String pathToDocuments = null;

	/**
	 * @param pathToDocuments
	 * @param pathToRepository
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 * @throws ParseException
	 * @throws NoDocumentsInCorpusException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws NormalizationException
	 * @throws TermWeightingException
	 * @throws SQLException 
	 */
	public SimpleCorpus(String pathToDocuments, String pathToRepository)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, ParseException, NotEnoughTermsInCorpusException,
			NoDocumentsInCorpusException, TermWeightingException, SQLException
			 {
		this(pathToDocuments, pathToRepository, true);
	}

	/**
	 * @param pathToDocuments
	 * @param pathToRepository
	 * @param load
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 * @throws ParseException
	 * @throws NoDocumentsInCorpusException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws NormalizationException
	 * @throws TermWeightingException
	 * @throws SQLException 
	 */
	public SimpleCorpus(String pathToDocuments, String pathToRepository,
			boolean load) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException,
			NotEnoughTermsInCorpusException, NoDocumentsInCorpusException,
			TermWeightingException, SQLException {

		this.pathToRepository = pathToRepository;

		Random rand = new Random();
		int randNum = (999 + rand.nextInt(9000));
		String randPath = this.pathToRepository + "/lucene/"
				+ Integer.toString(randNum);

		Repository.cleanStorage(randPath);
		this.repository = new Repository(randPath);
		this.repository.addDocumentsInFolder(pathToDocuments);

		this.internalCorpus = new RepositoryCorpus();

		if (load) {
			this.load();
		}
	}

	/**
	 * @return the internal corpus
	 */
	public Corpus getCorpus() {
		return internalCorpus;
	}

	/**
	 * @return the list of documents in the corpus
	 */
	public String[] getDocuments() {
		return this.internalCorpus.getPassages();
	}

	/**
	 * @return a double array of Doubles with the weighted term/doc matrix
	 */
	public double[][] getMatrix() {
		return this.internalCorpus.getSemanticSpace().getTermsDocuments()
				.getArray();
	}

	/**
	 * @return the folder from where the documents where processed
	 */
	public String getPathToDocuments() {
		return pathToDocuments;
	}

	/**
	 * @return the folder where the Lucene index is stored
	 */
	public String getPathToRepository() {
		return pathToRepository;
	}

	/**
	 * @return the list of terms in the corpus
	 */
	public String[] getTerms() {
		return this.internalCorpus.getTerms();
	}

	/**
	 * Loads the corpus (if not loaded automatically).
	 * 
	 * @throws NotEnoughTermsInCorpusException
	 * @throws IOException
	 * @throws NoDocumentsInCorpusException
	 * @throws NormalizationException
	 * @throws TermWeightingException
	 */
	public void load() throws NotEnoughTermsInCorpusException, IOException,
			NoDocumentsInCorpusException, TermWeightingException
			 {
		this.internalCorpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NO);
		this.internalCorpus.load(this.repository);
	}

	/**
	 * @throws NotEnoughTermsInCorpusException
	 * @throws IOException
	 * @throws NoDocumentsInCorpusException
	 * @throws TermWeightingException
	 * @throws NormalizationException
	 */
	public void loadTfIdfNormalised() throws NotEnoughTermsInCorpusException,
			IOException, NoDocumentsInCorpusException, TermWeightingException
			 {
		this.internalCorpus.getParameters().setTermWeightLocal(LocalWeight.TF);
		this.internalCorpus.getParameters().setTermWeightGlobal(GlobalWeight.Idf);
		this.internalCorpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NO);
		load();
	}
}
