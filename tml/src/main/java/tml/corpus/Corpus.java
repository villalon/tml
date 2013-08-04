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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.util.Version;

import tml.annotators.Annotator;
import tml.storage.Repository;
import tml.utils.Stats;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.SemanticSpace;
import tml.vectorspace.TermWeighting;
import tml.vectorspace.TermWeightingException;

import Jama.Matrix;


/**
 * <p>A {@link Corpus} is a set of {@link TextPassage}s
 * that are processed to build a {@link SemanticSpace}.</p>
 * <p>Steps of this process are:</p>
 * <ul>
 * <li>Tokenizing the document, i.e. recognizing terms, URLs, etc.</li>
 * <li>Removing stopwords, like prepositions</li>
 * <li>Stemming</li>
 * <li>Term selection</li>
 * </ul>
 * <p>Once the {@link Corpus} is loaded, it can create a {@link SemanticSpace}
 * using a particular dimensionality reduction technique. For the moment only
 * SVD is implemented, but we expect to implement some others.</p>
 * <p>The following code show how to load a {@link Corpus} and create a
 * {@link SemanticSpace}:</p>
 * <pre>
 * 	...
 * 	corpus.setName("Structure of English"); // A human readable name for the corpus
 * 	corpus.setTermSelectionCriteria(TermSelection.MIN_DF); // Every term must have a minimum document frequency
 * 	corpus.setTermSelectionThreshold(1); // Terms must appear in at least 2 documents
 *	corpus.load(storage); // Load the corpus from the storage
 *	corpus.createSemanticSpace(); // Create an empty semanticSpace
 *
 * 	SemanticSpace space = corpus.getSemanticSpace();
 *	space.setTermWeightScheme(TermWeight.TF); // The term weight scheme will be the raw term frequency
 *	space.setNormalized(true); // The final vectors will be normalized
 *	space.setDimensionalityReduction(DimensionalityReduction.DIMENSIONS_MAX_NUMBER);
 *	space.setDimensionalityReductionThreshold(2); // Number of dimensions to keep on the dimensionality reduction
 *	space.setDimensionsReduced(true); // The dimensions will be reduced
 *	space.calculate(); // Calculate the semantic space
 *	...
 * </pre>
 * 
 * @author Jorge Villalon
 *
 */
public abstract class Corpus implements Cloneable {

	private static final int MAX_DIMENSIONS = 300;

	public class PassageFreqs implements Cloneable {
		private int[] termsIndices;
		private double[] termsFrequencies;
		
		/**
		 * @param termsIndices
		 * @param termsFrequencies
		 */
		public PassageFreqs(int[] termsIndices, double[] termsFrequencies) {
			super();
			this.termsIndices = termsIndices;
			this.termsFrequencies = termsFrequencies;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			PassageFreqs clone = (PassageFreqs) super.clone();
			clone.termsFrequencies = this.termsFrequencies.clone();
			clone.termsIndices = this.termsIndices.clone();
			return clone;
		}

		/**
		 * @return the termsFrequencies
		 */
		public double[] getTermsFrequencies() {
			return termsFrequencies;
		}
		
		/**
		 * @return the termsIndices
		 */
		public int[] getTermsIndices() {
			return termsIndices;
		}
	}
	private static Logger logger = Logger.getLogger(Corpus.class);

	/** Every corpus should have a human readable name */
	private String name;
	/** SemanticSpace created from the corpus */
	protected SemanticSpace space = null;
	/** The time it took the corpus to load */
	protected long processingTime;
	/** The query to search */
	protected String luceneQuery;
	/** The list of terms in the Corpus*/
	protected String[] terms = null;
	/** The Lucene repository where the corpus original documents are stored */
	protected Repository repository;
	/** A class containing all parameters required to create a Corpus and its SemanticSpace */
	protected CorpusParameters parameters = null;
	/** External ids of all the passages (documents, paragraphs or sentences) */
	protected String[] passages = null;
	/** The id of each passage in the Lucene index */
	private int[] passagesLuceneIds = null;
	
	private boolean dbAnnotations = false;
	public boolean isDbAnnotations() {
		return dbAnnotations;
	}

	public void setDbAnnotations(boolean dbAnnotations) {
		this.dbAnnotations = dbAnnotations;
	}

	/**
	 * @return the passagesLuceneIds
	 */
	public int[] getPassagesLuceneIds() {
		return passagesLuceneIds;
	}

	/** Number of non zero values in the term doc matrix */
	protected int nonzeros = 0;
	private boolean projection = false;
	private double[] termEntropies = null;
	private Stats[] termStats = null;
	private Stats[] docStats = null;
	private Matrix termDocs = null;
	private int dimensions = -1;
	/**
	 * @return the projection
	 */
	public boolean isProjection() {
		return projection;
	}

        /**
         * Retrieves the index of the term in the corpus
         * 
         * @param term
         * @return the term index or -1 if not found
         */
        public int getIndexOfTerm(String term) {
            int i = 0;
            for(String t : this.terms) {
                if(term.equals(t))
                    return i;
                i++;
            }
            return -1;
        }

	public String getFilename() {
		return
		//this.getRepository().getIndexPath().replaceAll("[:/\\\\]", "_") + "_" +
		this.getLuceneQuery().replaceAll("\\W", "");		
	}
	/**
	 * @return the termEntropies
	 */
	public double[] getTermEntropies() {
		return termEntropies;
	}

	/**
	 * @param termEntropies the termEntropies to set
	 */
	public void setTermEntropies(double[] termEntropies) {
		this.termEntropies = termEntropies;
	}

	/**
	 * @return the termStats
	 */
	public Stats[] getTermStats() {
		return termStats;
	}

	/**
	 * @param termStats the termStats to set
	 */
	public void setTermStats(Stats[] termStats) {
		this.termStats = termStats;
	}

	/**
	 * @return the docStats
	 */
	public Stats[] getDocStats() {
		return docStats;
	}

	/**
	 * @param docStats the docStats to set
	 */
	public void setDocStats(Stats[] docStats) {
		this.docStats = docStats;
	}

	/**
	 * @param projection the projection to set
	 */
	public void setProjection(boolean projection) {
		this.projection = projection;
	}


	/**
	 * @return the nonzeros
	 */
	public int getNonzeros() {
		return nonzeros;
	}

	protected PassageFreqs[] passageFrequencies = null;

	/**
	 * Constructor for every {@link Corpus}.
	 * @param document the {@link TextDocument} to which the {@link Corpus belongs}
	 */
	public Corpus() {
		this.parameters = new CorpusParameters();
		this.space = new SemanticSpace(this);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Corpus clone = (Corpus) super.clone();
		clone.space = (SemanticSpace) this.space.clone();
		clone.space.setCorpus(clone);
		clone.passages = this.passages.clone();
		clone.terms = this.terms.clone();
		clone.passageFrequencies = new PassageFreqs[this.passageFrequencies.length];
		for(int i=0;i<clone.passageFrequencies.length;i++) {
			clone.passageFrequencies[i] = (PassageFreqs) this.passageFrequencies[i].clone();
		}
		clone.parameters = (CorpusParameters) this.parameters.clone();
		return clone;
	}

	/**
	 * Returns the string representing the Lucene query used to create the
	 * {@link Corpus}
	 * 
	 * @return the query used to create the {@link Corpus}
	 */
	public String getLuceneQuery() {
		return luceneQuery;
	}

	/**
	 * @return the name of the {@link Corpus} 
	 */
	public String getName() {
		if(this.name == null)
			return this.getLuceneQuery();
		return this.name;
	}
	
	/**
	 * @return the parameters
	 */
	public CorpusParameters getParameters() {
		return parameters;
	} 

	/**
	 * @return the passageFrequencies
	 */
	public PassageFreqs[] getPassageFrequencies() {
		return passageFrequencies;
	}

	/**
	 * @return the passages
	 */
	public String[] getPassages() {
		return passages;
	}

	/**
	 * @return the time it took to load the {@link Corpus}
	 */
	public long getProcessingTime() {
		return processingTime;
	}

	/**
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @return the {@link SemanticSpace} for the {@link Corpus}
	 */
	public SemanticSpace getSemanticSpace() {
		return this.space;
	}

	/**
	 * @return the raw matrix with the term frequencies for the {@link Corpus}
	 */
	public Matrix getTermDocMatrix() {
		return this.termDocs;
	}

	/**
	 * @return the terms
	 */
	public String[] getTerms() {
		return terms;
	}

	/**
	 * Loads the content of the documents in the query and creates the term-doc
	 * matrix
	 * @param storage the repository to search
	 * 
	 * @throws IOException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws NoDocumentsInCorpusException
	 * @throws TermWeightingException 
	 */
	public void load(Repository repository)
	throws NotEnoughTermsInCorpusException, IOException,
	NoDocumentsInCorpusException, TermWeightingException {

		assert (repository != null);
		
		// If we have enough documents we start creating a dictionary
		this.processingTime = System.currentTimeMillis();

		this.repository = repository;

		logger.debug("Corpus being loaded. Query:" + this.luceneQuery);

		TopFieldDocs hits = searchFullOpenQuery(this.repository, this.luceneQuery);
		ScoreDoc[] docs = hits.scoreDocs;

		// We start with an empty set of documents
		TreeMap<Integer, TextPassage> textPassages = new TreeMap<Integer, TextPassage>();

		// Checking if we got at least one document
		int numDocuments = hits.totalHits;
		logger.debug(numDocuments + " documents found");

		if (numDocuments < 1) {
			logger.error("No documents found in Corpus");
			throw new NoDocumentsInCorpusException();
		}

		Dictionary dictionary = new Dictionary(this);

		ArrayList<Integer> invalidDocuments = new ArrayList<Integer>();

		if (numDocuments > this.parameters.getMaxDocuments())
			numDocuments = this.parameters.getMaxDocuments();

		// For each document in the results
		for (int doc = 0; doc < numDocuments; doc++) {

			int documentId = docs[doc].doc;

			// We must get the terms and term frequencies for the document
			int[] frequencies = null;
			String[] terms = null;

			boolean documentIsEmpty = false;

			try {
				TermFreqVector tfvector = repository.getIndexReader()
				.getTermFreqVector(documentId,
						repository.getLuceneContentField());
				frequencies = tfvector.getTermFrequencies();
				terms = tfvector.getTerms();
			} catch (Exception ex) {
				// If the document has invalid terms or term frequencies we
				// leave it empty
				invalidDocuments.add(documentId);
				frequencies = new int[] { 0 };
				terms = new String[] { "" };
				documentIsEmpty = true;
				String title = repository.getIndexReader().document(documentId)
				.get("title");
				logger.debug("Invalid document found:" + documentId
						+ " ignoring :" + title);
			}

			TextPassage passage = null;

			Document luceneDocument = repository.getIndexSearcher().doc(hits.scoreDocs[doc].doc);
			String content = luceneDocument
			.get(repository.getLuceneContentField());
			String title = luceneDocument.get(repository.getLuceneTitleField());
			String url = luceneDocument.get(repository.getLuceneUrlField());
			String type = luceneDocument.get(repository.getLuceneTypeField());
			String externalId = luceneDocument.get(repository.getLuceneExternalIdField());

			passage = new TextPassage(
					documentId, // The passage's Lucene id
					this, // A link to the corpus where the passage belongs
					content, // The content of the passage 
					title, // The title for the passage
					url, // Url of the text passage (if any)
					type, // The type of the passage
					externalId); // The externalId (in Lucene) of the passage

			// Obtain annotations from the Lucene index and add them to the passage
			for(Annotator annotator : repository.getAnnotators()) {
				String annotation = null;
				annotation = repository.getAnnotations(externalId, annotator.getFieldName());
				if(annotation != null)
					passage.getAnnotations().put(annotator.getFieldName(), annotation);
			}
			// If the document is not empty, we add its terms to the dictionary
			if (!documentIsEmpty)
				dictionary.addTerms(terms, frequencies, passage);

			// We finally add the document to the corpus
			textPassages.put(documentId, passage);
		}

		// Once all the documents were insterted, we remove the terms that don't
		// meet the selection criteria from the dictionary and documents
		dictionary.removeTerms();

		logger.debug(textPassages.size() + " documents processed, "
				+ dictionary.getTerms().size() + " terms kept");

		// We validate that the corpus can be calculated as a SemanticSpace
		if (dictionary.getTerms().size() < textPassages.size() - 1
				|| dictionary.getTerms().size() <= 0) {
			logger.error("Corpus size is invalid!");
			throw new NotEnoughTermsInCorpusException();
		}
		
		this.terms = new String[dictionary.getTerms().size()];
		this.passages = new String[textPassages.size()];
		this.passagesLuceneIds = new int[textPassages.size()];
		this.passageFrequencies = new PassageFreqs[textPassages.size()];
		
		List<String> oldterms = new ArrayList<String>();
		List<String> sortedterms = new ArrayList<String>();
		
		for(Term term : dictionary.getTerms()) {
			this.terms[term.getIndex()] = term.getTerm();
			sortedterms.add(term.getTerm());
		}
		
		for(int i=0;i<this.terms.length;i++) {
			oldterms.add(this.terms[i]);
		}
		
		Collections.sort(sortedterms);
		
		logger.debug("Terms sorted");
		
		int passageIndex = 0;
		for(TextPassage passage : textPassages.values()) {
			this.passages[passageIndex] = passage.getExternalId();
			this.passagesLuceneIds[passageIndex] = passage.getId();
			PassageFreqs pf = new PassageFreqs(
					passage.getTermsCorpusIndices(), 
					passage.getTermFreqs());
			for(int i=0;i<pf.termsIndices.length;i++) {
				int oldindex = pf.termsIndices[i];
				String oldterm = oldterms.get(oldindex);
				int newindex = sortedterms.indexOf(oldterm);
				pf.termsIndices[i] = newindex;
			}
			this.passageFrequencies[passageIndex] = pf;
			passageIndex++;
			nonzeros += pf.termsIndices.length;
		}
		
		logger.debug("Frequencies calculated");
		
		for(int i=0;i<sortedterms.size();i++) {
			this.terms[i] = sortedterms.get(i);
		}

		this.termDocs = getMatrixFromTermFrequencies();
		
		TermWeighting termWeighting = new TermWeighting(this);
		termWeighting.process(this.termDocs);
		
		logger.debug("Term weighting applied");
		
		this.calculateDimensionsToKeep();
		
		this.space.calculate();
		
		this.processingTime = System.currentTimeMillis() - this.processingTime;
		
		logger.info("Corpus " + this.luceneQuery + " loaded in " + this.processingTime + " ms. Parameters:" + this.getParameters());
	}

	private void calculateDimensionsToKeep() {
		int rankS = Math.min(
				this.getPassages().length,
				this.getTerms().length);

		dimensions = 0;
		switch (this.getParameters().getDimensionalityReduction()) {
		case NUM:
			if (this.getParameters().getDimensionalityReductionThreshold() > 0) {
				dimensions = (int) this.getParameters().getDimensionalityReductionThreshold();
			}
			break;
		case VARPCT:
		case PCT:
			int maxDimensions = rankS;
			int numDimensions = (int) Math.round(maxDimensions
					* (this.getParameters().getDimensionalityReductionThreshold() 
							/ 100));
			dimensions = numDimensions;
			break;
		case NO:
			dimensions = rankS;
			break;
		default:
			logger.error("Invalid dimensionality reduction criterion");
		}

		dimensions = Math.max(1, dimensions);
		dimensions = Math.min(rankS, dimensions);
		dimensions = Math.min(MAX_DIMENSIONS, dimensions);		
	}
	
	private Matrix getMatrixFromTermFrequencies() {
		double[][] mdata = new double[this.getTerms().length][this.getPassages().length];
		for(int doc=0;doc<this.getPassages().length;doc++)
			for(int term=0;term<this.getTerms().length;term++)
				mdata[term][doc] = 0;
		int doc=0;
		for(PassageFreqs freqs : this.passageFrequencies) {
			for(int idx=0;idx<freqs.termsIndices.length;idx++) {
				int term = freqs.termsIndices[idx];
				mdata[term][doc] = freqs.termsFrequencies[idx];
			}
			doc++;
		}
		
		return new Matrix(mdata);		
	}
	/**
	 * @return the dimensions
	 */
	public int getDimensions() {
		return dimensions;
	}

	/**
	 * Prints in the console the parameters used in this corpus
	 */
	public String parametersSummary() {
		StringBuffer buff = new StringBuffer();
		buff.append("Name:");
		buff.append(this);
		buff.append("\n");
		buff.append("Query:");
		buff.append(this.getLuceneQuery());
		buff.append("\n");
		buff.append("Processing time:");
		buff.append(this.getProcessingTime());
		buff.append("\n");
		buff.append("Semantic Space:");
		buff.append(this.getSemanticSpace());
		buff.append("\n");
		buff.append("Terms:");
		buff.append(this.getTerms().length);
		buff.append("\n");
		buff.append("Passages:");
		buff.append(this.getPassages().length);
		buff.append("\n");
		return buff.toString();
	}
	
	public String printFrequencies() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.toString());
		buff.append("\n");
		for(int j=0; j<this.getTerms().length; j++) {
			buff.append(this.getTerms()[j]);
			buff.append("\t");
		}
		buff.append("\n");
		for(int i=0; i<this.getPassages().length; i++) {
			PassageFreqs freqs = this.getPassageFrequencies()[i];
			buff.append(this.getPassages()[i]);
			buff.append("\t");
			for(int j=0; j<freqs.getTermsIndices().length; j++) {
				buff.append(this.getTerms()[freqs.getTermsIndices()[j]]);
				buff.append("[");
				buff.append(freqs.getTermsIndices()[j]);
				buff.append("]-(");
				buff.append(freqs.getTermsFrequencies()[j]);
				buff.append(")\t");
			}
			buff.append("\n");
		}
		return buff.toString();
	}
	
	/**
	 * This method projects a {@link Corpus} into another one. The {@link Corpus}
	 * to project is the parameter, and the projected {@link Corpus} is what the
	 * method returns.
	 * The returned {@link Corpus} will have the same {@link Dictionary} than
	 * this {@link Corpus}, and will use the same parameters to calculate its
	 * {@link SemanticSpace}.
	 * 
	 * @param corpusToProject the {@link Corpus} to project
	 * @return the projected {@link Corpus}
	 */
	public Corpus projectCorpus(Corpus corpusToProject) throws Exception {
		
		Corpus projectedCorpus = null;
		
		if(this.space.getSk() == null ||
				this.space.getUk() == null ||
				this.space.getVk() == null) {
			logger.debug("Corpus " + this.luceneQuery + " will be used to project, but hasn't been calculated, calculating...");
			this.space.calculate();
		}
		
		try {
			logger.debug("Projecting corpus:" + corpusToProject.getName() + " on " + this.getName());
			projectedCorpus = (Corpus) corpusToProject.clone();
			projectedCorpus.terms = this.terms.clone();
			projectedCorpus.setName(corpusToProject.getName() + " projected on " + this.getName());
			List<String> termsList = new ArrayList<String>();
			for(int i=0; i<projectedCorpus.getTerms().length; i++)
				termsList.add(projectedCorpus.getTerms()[i]);
			logger.debug("Original corpus had " + corpusToProject.getTerms().length + 
					" terms and " + corpusToProject.getPassages().length + " passages");
			for(int j=0; j<projectedCorpus.passageFrequencies.length; j++) {
				PassageFreqs freqs = projectedCorpus.passageFrequencies[j];
				List<Double> newFreqs = new ArrayList<Double>();
				List<Integer> newIndices = new ArrayList<Integer>();
				
				for(int i=0; i<freqs.termsIndices.length; i++) {
					String term = corpusToProject.getTerms()[freqs.termsIndices[i]];
					double freq = freqs.termsFrequencies[i];
					int newIndex = termsList.indexOf(term);
					freqs.termsIndices[i] = newIndex;
					if(newIndex >= 0) {
						newFreqs.add(freq);
						newIndices.add(newIndex);
						if(newIndex >= projectedCorpus.getTerms().length) {
							throw new Exception("ARGH");
						}
					}
				}
				
				freqs.termsIndices = new int[newIndices.size()];
				freqs.termsFrequencies = new double[newFreqs.size()];
				for(int i=0; i<newIndices.size(); i++) {
					freqs.termsIndices[i] = newIndices.get(i);
					freqs.termsFrequencies[i] = newFreqs.get(i);
				}
				projectedCorpus.passageFrequencies[j] = freqs;
			}
			
			logger.debug("Final corpus has " + projectedCorpus.getTerms().length + 
					" terms and " + projectedCorpus.getPassages().length + " passages");
		} catch (CloneNotSupportedException e) {
			logger.error(e);
			return null;
		}

		Matrix m = projectedCorpus.getMatrixFromTermFrequencies();
		projectedCorpus.termDocs = m;
		projectedCorpus.space = (SemanticSpace) this.space.clone();
		projectedCorpus.getSemanticSpace().setCorpus(projectedCorpus);
		Matrix s = projectedCorpus.getSemanticSpace().getSk();
		Matrix u = projectedCorpus.getSemanticSpace().getUk();

		Matrix ss = new Matrix(s.getRowDimension(), s.getRowDimension());
		for(int i=0;i<s.getRowDimension();i++) {
			if(s.get(i, i) != 0)
				ss.set(i, i, 1/s.get(i, i));
		}
		// Theoretically this produces V
		Matrix v = m.transpose().times(u).times(ss);
		
		projectedCorpus.space.setVk(v);
		
		return projectedCorpus;
	}
	
	/**
	 * <p>
	 * This method searches for whatever you want, full documents, sentences or
	 * paragraphs. All mixed up, so this should only be used by experts that
	 * know how tml uses the Lucene index to store its data.
	 * </p>
	 * <p>
	 * For example, to find all the sentences from a document with external id
	 * "foo"
	 * </p>
	 * 
	 * <pre>
	 * String query = &quot;type:sentence AND reference:foo&quot;;
	 * searchFullOpenQuery(query);
	 * </pre>
	 * <p>
	 * It returns a Lucene Hits results because the documents inside can't be
	 * used directly to create a Corpus
	 * </p>
	 * 
	 * @param query
	 *            the Lucene query
	 * @return the search results
	 */
	private TopFieldDocs searchFullOpenQuery(Repository storage, String query) {
		assert (query != null);

		// The query is parsed
		QueryParser parser = new QueryParser(Version.LUCENE_29,
				storage.getLuceneContentField(),
				new KeywordAnalyzer());
		parser.setLowercaseExpandedTerms(false);
		Query documentsQuery = null;
		try {
			documentsQuery = parser.parse(query);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error(e.toString());
			return null;
		}

		// The index is searched using the query
		TopFieldDocs docs = null;
		try {
			docs = new IndexSearcher(storage.getIndexReader()).search(documentsQuery, null, 9999, Sort.INDEXORDER);
		} catch (Exception e) {
			logger.error(e.toString());
			return null;
		}

		return docs;
	}
	
	/**
	 * @param name the name for the {@link Corpus}
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(CorpusParameters parameters) {
		this.parameters = parameters;
		this.space = new SemanticSpace(this);
	}
	
	/**
	 * Returns the name of the {@link Corpus}.
	 */
	@Override
	public String toString() {
		return this.getName();
	}
}
