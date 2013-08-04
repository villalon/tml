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
import tml.corpus.SearchResultsCorpus;
import tml.corpus.CorpusParameters.TermSelection;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.TermWeightingException;

import static org.junit.Assert.*;

/**
 * @author Jorge Villalon
 * 
 */
public class LuceneSearchTest extends AbstractTmlIndexingTest {

	private static final String LUCENE_QUERY = "type:document";
	private static String TESTS_DOCUMENTS_FOLDER = null;

	private SearchResultsCorpus corpus;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		TESTS_DOCUMENTS_FOLDER = Configuration.getTmlFolder() + "/corpora/introLSA";
		repository.addDocumentsInFolder(TESTS_DOCUMENTS_FOLDER);
	}

	/**
	 * Test method for
	 * {@link tml.corpus.LuceneSearch#load()}.
	 * 
	 * @throws IOException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws TermWeightingException 
	 * @throws NoDocumentsInCorpusException 
	 */
	@Test
	public void testLoad() throws NotEnoughTermsInCorpusException, IOException, NoDocumentsInCorpusException, TermWeightingException {
		corpus = new SearchResultsCorpus(LUCENE_QUERY);
		corpus.getParameters().setMaxDocuments(9999);
		corpus.setName("Test corpus");
		corpus.getParameters().setTermSelectionCriterion(TermSelection.DF);
		corpus.getParameters().setTermSelectionThreshold(2);
		corpus.load(repository);
		assertEquals(9, corpus.getPassages().length);
		assertEquals(12, corpus.getTerms().length);
		assertEquals(LUCENE_QUERY, corpus.getLuceneQuery());
		assertNotNull(corpus.getSemanticSpace());
		assertNotNull(corpus.getPassages());
	}

	/**
	 * Validate that a corpus with one document
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws NoDocumentsInCorpusException
	 */
	@Test
	public void testOneDocumentCorpus() throws IOException, ParseException,
			NotEnoughTermsInCorpusException, NoDocumentsInCorpusException, TermWeightingException {
		SearchResultsCorpus oneDocCorpus = new SearchResultsCorpus(
				"type:sentence AND parent:c1");
		oneDocCorpus.getParameters().setTermSelectionCriterion(TermSelection.TF);
		oneDocCorpus.getParameters().setTermSelectionThreshold(0);
		oneDocCorpus.getParameters().setTermWeightLocal(LocalWeight.TF);
		oneDocCorpus.getParameters().setTermWeightGlobal(GlobalWeight.None);
		oneDocCorpus.load(repository);
		assertNotNull(oneDocCorpus);
	}

	/**
	 * Validate that a corpus with no documents is invalid
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws NoDocumentsInCorpusException
	 */
	@Test(expected = NoDocumentsInCorpusException.class)
	public void testCorpusWithNoDocuments() throws IOException, ParseException,
			NotEnoughTermsInCorpusException, NoDocumentsInCorpusException, TermWeightingException {
		SearchResultsCorpus oneDocCorpus = new SearchResultsCorpus(
				"queryThatCertainlyWillReturnNothing");
		oneDocCorpus.getParameters().setTermSelectionCriterion(TermSelection.TF);
		oneDocCorpus.getParameters().setTermSelectionThreshold(0);
		oneDocCorpus.load(repository);
	}

}
