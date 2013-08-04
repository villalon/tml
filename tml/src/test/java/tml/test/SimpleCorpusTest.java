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

import org.junit.Test;

import tml.Configuration;
import tml.corpus.SimpleCorpus;
import static org.junit.Assert.*;


/**
 * This test creates a simple corpus that loads a set of documents and then it
 * can be used directly to create a {@link SemanticSpace}.
 * 
 * @author Jorge Villalon
 * @see SimpleCorpus
 */
public class SimpleCorpusTest extends AbstractTmlIndexingTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void CreateSimpleCorpus() throws Exception {
		SimpleCorpus corpus = new SimpleCorpus(Configuration.getTmlFolder() + "/corpora/introLSA", prop.getProperty("tml.lucene.indexpath"));

		for (String term : corpus.getTerms())
			System.out.print(term + " ");
		System.out.println();
		for (String doc : corpus.getDocuments())
			System.out.print(doc + " ");
		System.out.println();
		double[][] m = corpus.getMatrix();
		for (int i = 0; i < corpus.getTerms().length; i++) {
			for (int j = 0; j < corpus.getDocuments().length; j++) {
				System.out.print(m[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		assertNotNull(corpus);
	}
}
