/**
 * 
 */
package tml.test;

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.Corpus;
import tml.corpus.RepositoryCorpus;
import tml.vectorspace.operations.TagClouds;
import static org.junit.Assert.*;

/**
 * @author Jorge
 *
 */
public class TagCloudsTest extends AbstractTmlIndexingTest {

	static TagClouds operation;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/uppsala");
		Corpus corpus = new RepositoryCorpus();
		corpus.load(repository);
		
		operation = new TagClouds();
		operation.setCorpus(corpus);
		operation.start();
	}
	
	@Test
	public void checkTagClouds() {
		assertNotNull(operation);
		
		operation.printResults();
	}
	
	@Test
	public void checkVisualization() {
		tml.vectorspace.operations.visualizations.TagClouds visualization = new tml.vectorspace.operations.visualizations.TagClouds();
		visualization.setOperation(operation);
		System.out.println(visualization.getHTML());
	}
}
