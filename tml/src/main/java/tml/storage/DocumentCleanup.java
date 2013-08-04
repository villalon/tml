/**
 * 
 */
package tml.storage;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import tml.corpus.CorpusParameters;
import tml.corpus.TextDocument;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;

/**
 * @author Jorge Villalon
 *
 */
public class DocumentCleanup implements Runnable {

	private static Logger logger = Logger.getLogger(DocumentCleanup.class);
	private Repository repository;
	private CorpusParameters params;

	public DocumentCleanup(Repository repo) {
		this.repository = repo;
		this.params = new CorpusParameters();
		this.params.setDimensionalityReduction(DimensionalityReduction.NO);
		this.params.setDimensionalityReductionThreshold(0);
		this.params.setLanczosSVD(false);
		this.params.setNormalizeDocuments(false);
		this.params.setTermSelectionCriterion(TermSelection.DF);
		this.params.setTermSelectionThreshold(0);
		this.params.setTermWeightGlobal(GlobalWeight.None);
		this.params.setTermWeightLocal(LocalWeight.TF);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.debug("Document cleanup started");

		int total = 0;
		List<TextDocument> docs;
		try {
			docs = this.repository.getAllTextDocuments();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return;
		}

		if(docs == null) {
			logger.debug("No documents to cleanup");
			return;
		}

		for(TextDocument doc : docs) {
				try {
					String[][] subs = this.repository.getDbConnection().getSubDocuments(doc.getExternalId());
					if(subs.length <= 1) {
						logger.debug("Inserting document in the database:" + doc.getExternalId());
						Document document = repository.getIndexReader().document(doc.getLuceneId());
						this.repository.getDbConnection().insertDocument(repository, document);
						doc.setParameters(this.params);
						doc.load(repository);
						for(int id : doc.getSentenceCorpus().getPassagesLuceneIds()) {
							Document sentence = repository.getIndexReader().document(id);
							this.repository.getDbConnection().insertDocument(repository, sentence);							
						}
						for(int id : doc.getParagraphCorpus().getPassagesLuceneIds()) {
							Document sentence = repository.getIndexReader().document(id);
							this.repository.getDbConnection().insertDocument(repository, sentence);							
						}
						total++;
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					continue;
				}
			}
		
		if(total > 0)
			logger.info("Cleaned " + total + " documents");
		else
			logger.debug("Nothing to clean!");
	}

}
