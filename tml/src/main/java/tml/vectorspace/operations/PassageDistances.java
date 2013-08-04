/*******************************************************************************
 *  Copyright 2007, 2009 Jorge Villalon (jorge.villalon@uai.cl)
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0 
 *  	
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 *******************************************************************************/

package tml.vectorspace.operations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tml.utils.MatrixUtils;
import tml.vectorspace.operations.results.PassageDistancesResult;


import Jama.Matrix;

/**
 * Calculates the distances (angular) for a whole set of passages
 * in a {@link Corpus}.
 * 
 * @author Jorge Villalon
 *
 */
public class PassageDistances extends
		AbstractOperation<PassageDistancesResult> {

	/**
	 * 
	 */
	public PassageDistances() {
		this.name = "Segment distances analysis";
	}

	public Object[][] getInnerData() {
		return getResultsTable();
	}

	@Override
	public Object[][] getResultsTable() {
		Object[][] data = new Object[this.results.size()][3];

		int i = 0;
		for (PassageDistancesResult result : this.results) {
			data[i][0] = result.getDocumentAId();
			data[i][1] = result.getDocumentBId();
			Double value = new Double(result.getDistance());
			DecimalFormat df = new DecimalFormat("#0.000");
			data[i][2] = df.format(value);
			i++;
		}

		return data;
	}

	@Override
	public Object[] getResultsTableHeader() {
		Object[] data = new Object[3];
		data[0] = "Document A";
		data[1] = "Document B";
		data[2] = "Distance";
		return data;
	}

	@Override
	public void start() throws Exception {
		super.start();

		double averageDistance = 0;
		this.results = new ArrayList<PassageDistancesResult>();

		Matrix m = this.corpus.getSemanticSpace().getVk().times(this.corpus.getSemanticSpace().getSk());
		m = MatrixUtils.normalizeRows(m);
		m = m.times(m.transpose());

		for (int doc1 = 0; doc1 < m.getRowDimension(); doc1++) {
			for (int doc2 = doc1 + 1; doc2 < m.getRowDimension(); doc2++) {

				if (Math.abs(doc1 - doc2) == 1) {
					PassageDistancesResult result = new PassageDistancesResult();
					result.setDistance(m.get(doc1, doc2));
					result.setDocumentAId(doc1);
					result.setDocumentBId(doc2);
					averageDistance += result.getDistance();

					this.results.add(result);
				}
			}
		}

		this.summaryResult = averageDistance / results.size();

		Collections.sort(this.results,
				new Comparator<PassageDistancesResult>() {
					@Override
					public int compare(
							final PassageDistancesResult arg0,
							final PassageDistancesResult arg1) {
						return arg0.getDocumentAId() - arg1.getDocumentBId();
					}
				});

		super.end();
	}
}
