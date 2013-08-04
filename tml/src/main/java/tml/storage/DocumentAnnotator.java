/**
 * 
 */
package tml.storage;

import java.io.IOException;

import org.apache.log4j.Logger;

import tml.annotators.Annotator;

/**
 * @author jorge
 *
 */
public class DocumentAnnotator implements Runnable {

	private static Logger logger = Logger.getLogger(DocumentAnnotator.class);
	private Repository repository;

	public DocumentAnnotator(Repository repo) {
		this.repository = repo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		int total = 0;
		String[][] docs = this.repository.getDbConnection().getUnannotatedDocument();

		if(docs == null) {
			logger.debug("No documents to annotate");
			return;
		}

		for(String[] doc : docs) {
			String externalid = doc[0];
			String type = doc[1];
			String content = null;
			try {
				content = this.repository.getDocumentField(externalid, this.repository.getLuceneContentField());
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("No content found in Lucene index for document " + externalid);
				return;
			}
			for (Annotator annotator : this.repository.getAnnotators()) {
				String metadata = null;
				if (annotator.getTypes().contains(type)) {
					metadata = annotator.getAnnotations(content);
				} else {
					metadata = "Not available";
				}
				this.repository.getDbConnection().setAnnotation(externalid, annotator.getFieldName(), metadata);
			}
			total++;

		}
		if(total > 0)
			logger.info("Annotated " + total + " documents");
		else
			logger.debug("Nothing to annotate");
	}

}
