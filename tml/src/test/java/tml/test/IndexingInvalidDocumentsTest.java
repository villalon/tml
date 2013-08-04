/*******************************************************************************
 * Copyright (C) 2001, 2007 University of Sydney
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 *******************************************************************************/

package tml.test;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.*;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.vectorspace.*;


/**
 * 
 * @author Jorge Villalon
 * 
 */
public class IndexingInvalidDocumentsTest extends AbstractTmlIndexingTest {

	private static String LUCENE_QUERY = "type:document";
	private static String TESTS_INVALID_DOCUMENTS_FOLDER = null;
	private static String TESTS_VALID_DOCUMENTS_FOLDER = null;

	private SearchResultsCorpus corpus;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		TESTS_INVALID_DOCUMENTS_FOLDER = Configuration.getTmlFolder() + "/corpora/invalidDocuments";
		TESTS_VALID_DOCUMENTS_FOLDER = Configuration.getTmlFolder() + "/corpora/introLSA";		
	}

	@Test(expected = NotEnoughTermsInCorpusException.class)
	public void invalidDocuments() throws NotEnoughTermsInCorpusException,
			IOException, NoDocumentsInCorpusException, ParseException, TermWeightingException {
		repository.addDocumentsInFolder(TESTS_INVALID_DOCUMENTS_FOLDER);
		corpus = new SearchResultsCorpus(LUCENE_QUERY);
		corpus.getParameters().setTermSelectionCriterion(TermSelection.TF);
		corpus.getParameters().setTermSelectionThreshold(1);
		corpus.load(repository);
	}

	@Test(expected = NotEnoughTermsInCorpusException.class)
	public void ValidDocumentsButNoTerms()
			throws NotEnoughTermsInCorpusException, IOException,
			NoDocumentsInCorpusException, TermWeightingException,
			ParseException {
		repository.addDocumentsInFolder(TESTS_VALID_DOCUMENTS_FOLDER);
		corpus = new SearchResultsCorpus(LUCENE_QUERY);
		corpus.getParameters().setTermSelectionCriterion(TermSelection.DF);
		corpus.getParameters().setTermSelectionThreshold(3);
		corpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		corpus.getParameters().setDimensionalityReductionThreshold(1);
		corpus.load(repository);
	}
}
