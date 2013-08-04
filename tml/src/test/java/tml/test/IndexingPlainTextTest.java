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

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.TextDocument;

import static org.junit.Assert.*;

public class IndexingPlainTextTest extends AbstractTmlIndexingTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		File[] fileList = {
				new File(Configuration.getTmlFolder() + "/corpora/uppsala/0100.a1.txt"),
			new File(Configuration.getTmlFolder() + "/corpora/uppsala/0101.a1.txt"),
			new File(Configuration.getTmlFolder() + "/corpora/uppsala/0102.a1.txt")};
		repository.addDocumentsInList(fileList);
	}

	@Test
	public void numbersDiagnostic01() throws Exception {
		TextDocument document = repository.getTextDocument("0100.a1");
		document.load(repository);
		assertEquals(30, document.getSentenceCorpus().getPassages().length);
		assertEquals(9, document.getParagraphCorpus().getPassages().length);
	}

	@Test
	public void numbersDiagnostic02() throws Exception {
		TextDocument document = repository.getTextDocument("0101.a1");
		document.load(repository);
		assertEquals(41, document.getSentenceCorpus().getPassages().length);
		assertEquals(9, document.getParagraphCorpus().getPassages().length);
	}

	@Test
	public void numbersDiagnostic36() throws Exception {
		TextDocument document = repository.getTextDocument("0102.a1");
		document.load(repository);
		assertEquals(49, document.getSentenceCorpus().getPassages().length);
		assertEquals(11, document.getParagraphCorpus().getPassages().length);
	}
}
