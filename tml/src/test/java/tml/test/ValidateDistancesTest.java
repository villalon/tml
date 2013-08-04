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

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.corpus.SearchResultsCorpus;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.operations.PassagesSimilarity;



import static org.junit.Assert.*;

/**
 * Basic test to validate if two identical documents and two very dissimilar
 * documents get the proper 0 and 1 distances.
 * 
 * @author Jorge Villalon
 * 
 */
public class ValidateDistancesTest extends AbstractTmlIndexingTest {

	private static PassagesSimilarity distance;
	
	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/identical");
		
		SearchResultsCorpus corpus = new SearchResultsCorpus("type:document");
		corpus.getParameters().setDimensionalityReduction(
				DimensionalityReduction.NO);
		corpus.getParameters().setTermSelectionCriterion(TermSelection.DF);
		corpus.getParameters().setTermSelectionThreshold(0);
		corpus.getParameters().setTermWeightLocal(LocalWeight.TF);
		corpus.getParameters().setTermWeightGlobal(GlobalWeight.None);
		corpus.getParameters().setLanczosSVD(false);
		corpus.load(repository);

		distance = new PassagesSimilarity();
		distance.setCorpus(corpus);
		distance.start();
	}

	/**
	 * This test compares two identical documents
	 * 
	 * @throws Exception
	 */
	@Test
	public void identicalDocuments() throws Exception {
		assertEquals(1.0, Math.abs(distance.getSimilarities().get(0, 1)), 0.0001);
	}
	
	/**
	 * This test should compare two documents with no common words
	 * 
	 * @throws Exception
	 */
	@Test
	public void noCommonWords() throws Exception {
		assertEquals(0.0, distance.getSimilarities().get(0, 2), 0.0001);
	}
}
