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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;
import org.apache.lucene.queryParser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.TextDocument;
import tml.storage.Repository;
import tml.storage.importers.TextImporter;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.TermWeightingException;


/**
 * This class tests the processing of documents with TML.
 * 
 * @author Jorge Villalon
 * 
 */
public class IndexingDocumentsTest extends AbstractTmlIndexingTest {

	private static String TESTS_DOCUMENTS_FOLDER = null;
	private static String TESTS_NEW_DOCUMENTS_FOLDER = null;
	private static File[] LIST_OF_FILES = null;
	private static String TESTS_LUCENE_INVALID_PATH = "tests/lucene/nonexistent";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		TESTS_DOCUMENTS_FOLDER = Configuration.getTmlFolder() + "/corpora/introLSA";
		TESTS_NEW_DOCUMENTS_FOLDER = Configuration.getTmlFolder() + "/corpora/handbookOfLSA";
		LIST_OF_FILES = new File[] {
				new File(TESTS_NEW_DOCUMENTS_FOLDER + "/b1.txt"), 
				new File(TESTS_NEW_DOCUMENTS_FOLDER	+ "/b2.txt")};
	}

	/**
	 * This method verifies that a clean index contains no documents.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testCleanIndex() throws Exception {
		List<TextDocument> documents = repository.getAllTextDocuments();
		assertEquals(0, documents.size());
	}

	/**
	 * Validates that in invalid path raises an exception.
	 * 
	 * @throws IOException 
	 * @throws SQLException 
	 */
	@Test(expected = IOException.class)
	public void testLuceneIndexInvalid() throws IOException, SQLException {
		repository = new Repository(TESTS_LUCENE_INVALID_PATH);
	}

	/**
	 * Adding a document with one paragraph and one sentence should create three documents in the Lucene index.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddDocument() throws Exception {
		int numDocs = repository.getIndexReader().numDocs();
		repository.addDocument("myExternalId", "myContent needs two words",
				"myTitle", "myUrl", new TextImporter());
		int numDocsAfter = repository.getIndexReader().numDocs();
		assertEquals(numDocs + 3, numDocsAfter);
	}

	/**
	 * Adding files should create the right number of documents in the index.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFilesInFolder() throws Exception {
		int numDocs = repository.getIndexReader().numDocs();
		repository.addDocumentsInFolder(TESTS_DOCUMENTS_FOLDER);
		int numDocsAfter = repository.getIndexReader().numDocs();
		assertEquals(numDocs + 27, numDocsAfter);
	}

	/**
	 * Adding a list of files instead of a 
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddFilesInList() throws IOException {
		int numDocs = repository.getIndexReader().numDocs();
		repository.addDocumentsInList(LIST_OF_FILES);
		int numDocsAfter = repository.getIndexReader().numDocs();
		assertEquals(numDocs + 6, numDocsAfter);
	}

	/**
	 * Deletes a document.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteDocument() throws Exception {
		repository.addDocument("myExternalId", "myContent needs two words, no less than that",
				"myTitle", "myUrl", new TextImporter());
		int numDocs = repository.getIndexReader().numDocs();
		TextDocument document = repository.getTextDocument("myExternalId");
		assertNotNull(document);
		repository.deleteTextDocument(document);
		int numDocsAfter = repository.getIndexReader().numDocs();
		assertEquals(numDocs - 3, numDocsAfter);
	}

	/**
	 * Test method for {@link tml.storage.Repository#getAnalyzer()}.
	 */
	@Test
	public void testGetAnalyzer() {
		assertNotNull(repository.getAnalyzer());
	}

	/**
	 * Test method for {@link tml.storage.Repository#getIndexReader()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetIndexReader() throws IOException {
		assertNotNull(repository.getIndexReader());
	}

	/**
	 * Test method for
	 * {@link tml.storage.Repository#LuceneIndex(java.lang.String, java.lang.String)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLuceneIndex() throws IOException {
		assertNotNull(repository);
	}

	/**
	 * Test method for
	 * 
	 * @throws IOException
	 * @throws NoDocumentsInCorpusException
	 * @throws NotEnoughTermsInCorpusException
	 * @throws ParseException
	 * @throws TermWeightingException
	 * @throws SQLException 
	 */
	@Test
	public void testGetTextDocument() throws IOException, ParseException,
			NotEnoughTermsInCorpusException, NoDocumentsInCorpusException,
			TermWeightingException, SQLException {
		int numDocs = repository.getIndexReader().numDocs();
		repository.addDocument("myExternalId", "myContent needs two words",
				"myTitle", "myUrl", new TextImporter());
		int numDocsAfter = repository.getIndexReader().numDocs();
		assertEquals(numDocs + 3, numDocsAfter);
		TextDocument document = repository.getTextDocument("myExternalId");
		assertNotNull(document);
	}

}
