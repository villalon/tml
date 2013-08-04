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

import org.apache.lucene.queryParser.ParseException;

import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.storage.Repository;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.TermWeightingException;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;



/**
 * <p>
 * The TextDocument class represents a whole document, which comprises a
 * content, a title and a url. Each document is identified by an id, known as
 * the externalId. It also has an internal id, the Lucene Id, which identifies
 * the document within the underlying Lucene index.
 * </p>
 * <p>
 * A TextDocument contains two corpora, a sentence based {@link Corpus} and a
 * paragraph based {@link Corpus}. The TextDocument is responsible for loading
 * both and assigning the necessary parameters for their creation. This means
 * that the construction of the {@link Corpus} and the {@link SemanticSpace} are
 * defined on a per document basis.
 * </p>
 * <p>
 * The TextDocument contains a duplicate of its content, this can cause
 * scalability problems with long documents (more than 2000 terms, aprox. 10000
 * words)
 * </p>
 * <p>
 * The most basic way to use a TextDocument is to perform operations to its
 * corpora. Operations can be calculating semantic distances between sentences
 * or extracting the most important paragraphs (based on variance) to give some
 * examples.
 * </p>
 * <p>
 * The following example shows how to obtain a {@link TextDocument} from a
 * {@link Repository} and then how to extract the key sentences.
 * </p>
 * 
 * <pre>
 * Repository repository = new Repository(&quot;path/to/repository&quot;);
 * TextDocument document = repository.getTextDocument(&quot;foo&quot;);
 * if (document != null) {
 * 	System.out.println(&quot;Document &quot; + document.getTitle() + &quot; found&quot;);
 * }
 * </pre>
 * <p>
 * Now we are going to set the parameters to load the document's corpora and
 * load them.
 * </p>
 * 
 * <pre>
 * document.setTermSelection(TermSelection.MIN_DF);
 * document.setTermSelectionThreshold(1);
 * document.setTermLocalWeight(LocalWeight.TF);
 * document.setTermGlobalWeight(GlobalWeight.Idf);
 * document
 * 		.setDimensionalityReduction(DimensionalityReduction.DIMENSIONS_MAX_PERCENTAGE);
 * document.setDimensionalityReductionThreshold(50);
 * document.setDimensionsReduced(true);
 * document.setNormalized(true);
 * document.load(repository);
 * </pre>
 * <p>
 * Finally we can perform an operation and show the results.
 * </p>
 * 
 * <pre>
 * KeyTextPassages operation = new KeyTextPassages();
 * operation.setCorpus(document.getSentenceCorpus());
 * operation.start();
 * 
 * for (KeyTextPassagesResult result : operation.getResults()) {
 * 	System.out.println(&quot;Sentence id: &quot; + result.getTextPassageId()
 * 			+ &quot; from eigenvector:&quot; + result.getEigenVectorIndex()
 * 			+ &quot; with load:&quot; + result.getLoad() + &quot; content:&quot;
 * 			+ result.getTextPassageContent());
 * }
 * </pre>
 * 
 * @see Repository AbstractOperation Corpus
 * @author Jorge Villalon
 * 
 */
public class TextDocument {

	/** The Lucene id of the document */
	private int luceneId;
	/** The title of the document */
	private String title;
	/** The url of the document */
	private String url;
	/** The external id of the document */
	private String externalId;
	/** The content of the document */
	private String content;

	/** The sentence corpus of the document */
	private SentenceCorpus sentenceCorpus = null;
	/** The paragraph corpus of the document */
	private ParagraphCorpus paragraphCorpus = null;

	private CorpusParameters parameters = null;

	/**
	 * Constructor of {@link TextDocument}. It creates a new instance of a
	 * TextDocument. It should be used only by the {@link Repository}.
	 * 
	 * @param luceneId
	 *            the id within the Lucene index
	 * @param title
	 *            the title of the document
	 * @param url
	 *            the url of the document
	 * @param externalId
	 *            the external id
	 * @param content
	 *            the content of the document
	 */
	public TextDocument(int luceneId, String title, String url,
			String externalId, String content) {
		super();

		this.luceneId = luceneId;
		this.title = title;
		this.url = url;
		this.externalId = externalId;
		this.content = content;
		this.parameters = new CorpusParameters();
		this.parameters.setTermSelectionCriterion(TermSelection.DF);
		this.parameters.setTermSelectionThreshold(1);
		this.parameters.setDimensionalityReduction(DimensionalityReduction.PCT);
		this.parameters.setDimensionalityReductionThreshold(25);
		this.parameters.setTermWeightLocal(LocalWeight.LOGTF);
		this.parameters.setTermWeightGlobal(GlobalWeight.Entropy);
	}

	/**
	 * Gets the content of the document
	 * 
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Gets the external id used when the document was inserted.
	 * 
	 * @return the external id
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * Gets the Lucene internal id of the document
	 * 
	 * @return the Lucene id
	 */
	public int getLuceneId() {
		return luceneId;
	}

	/**
	 * Gets the {@link ParagraphCorpus} created with the paragraphs of the
	 * {@link TextDocument}
	 * 
	 * @return A {@link ParagraphCorpus} object or null.
	 */
	public ParagraphCorpus getParagraphCorpus() {
		return paragraphCorpus;
	}

	/**
	 * Gets the {@link SentenceCorpus} created with the sentences of the
	 * {@link TextDocument}
	 * 
	 * @return A {@link SentenceCorpus} object or null.
	 */
	public SentenceCorpus getSentenceCorpus() {
		return sentenceCorpus;
	}

	/**
	 * Gets the title of the document
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the url of the document
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Loads the corpora for the {@link TextDocument} with all the parameters
	 * that the document has set. To load the term frequency vectors, a pointer
	 * to the repository is necessary.
	 * 
	 * @param repository
	 * @throws Exception 
	 * @throws IOException
	 * @throws ParseException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws NoDocumentsInCorpusException
	 * @throws TermWeightingException
	 * @throws NormalizationException
	 */
	public void load(Repository repository) throws Exception, IOException, ParseException,
			NotEnoughTermsInCorpusException, NoDocumentsInCorpusException,
			TermWeightingException {
		this.sentenceCorpus = new SentenceCorpus(this);
		this.sentenceCorpus.setName("Sentences from " + title);
		this.sentenceCorpus.setParameters(parameters);
		this.sentenceCorpus.load(repository);

		this.paragraphCorpus = new ParagraphCorpus(this);
		this.paragraphCorpus.setName("Paragraphs from " + title);
		this.paragraphCorpus.setParameters(parameters);
		this.paragraphCorpus.load(repository);
	}

	/**
	 * @return the parameters
	 */
	public CorpusParameters getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(CorpusParameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * The default view of a TextDocument is its title
	 */
	@Override
	public String toString() {
		return this.title;
	}
}
