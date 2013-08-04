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

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.TextDocument;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import static org.junit.Assert.*;


/**
 * @author Jorge Villalon
 * 
 */
public class IndexingHtmlTest extends AbstractTmlIndexingTest {

	private static String TESTS_DOCUMENTS_FOLDER = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		TESTS_DOCUMENTS_FOLDER = Configuration.getTmlFolder() + "/corpora/html";
		repository.addDocumentsInFolder(TESTS_DOCUMENTS_FOLDER);
	}

	@Test
	public void readPage() throws IOException {
		TextDocument doc = repository.getTextDocument("Automobile");
		assertNotNull(doc);
	}

	@Test
	public void loadPageCorpus() throws Exception {
		TextDocument doc = repository.getTextDocument("Automobile");
		doc.getParameters().setTermSelectionCriterion(TermSelection.DF);
		doc.getParameters().setTermSelectionThreshold(2);
		doc.getParameters().setDimensionalityReduction(DimensionalityReduction.NO);
		doc.load(repository);
		assertNotNull(doc.getSentenceCorpus());
		assertNotNull(doc.getParagraphCorpus());
	}
}
