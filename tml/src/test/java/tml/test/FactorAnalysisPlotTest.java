/**
 * 
 */
package tml.test;

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.RepositoryCorpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.vectorspace.operations.FactorAnalysisPlot;
import static org.junit.Assert.*;

/**
 * @author Jorge
 *
 */
public class FactorAnalysisPlotTest extends AbstractTmlIndexingTest {

	static FactorAnalysisPlot operation;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/handbookOfLSA");
		RepositoryCorpus corpus = new RepositoryCorpus();
		corpus.getParameters().setDimensionalityReduction(DimensionalityReduction.PCT);
		corpus.getParameters().setDimensionalityReductionThreshold(20);
		corpus.load(repository);
		
		operation = new FactorAnalysisPlot();
		operation.setCorpus(corpus);
		operation.start();
	}
	
	@Test
	public void checkTagClouds() {
		assertNotNull(operation);
		
		operation.printResults();
	}
}
