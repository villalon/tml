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

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import tml.corpus.TextDocument;
import tml.vectorspace.operations.results.PassageExtractionSummarizationResult;


import Jama.Matrix;

/**
 * @author Jorge Villalon
 *
 */
public class PassageExtractionSummarization extends AbstractOperation<PassageExtractionSummarizationResult> {

	private double loadThreshold = 0.5;

	/**
	 * 
	 */
	public PassageExtractionSummarization() {
		this.name = "Passage extraction";
	}

	/**
	 * @return the threshold by which a text passage will be kept as result
	 */
	public double getLoadThreshold() {
		return loadThreshold;
	}

	/**
	 * @param loadThreshold
	 */
	public void setLoadThreshold(double loadThreshold) {
		this.loadThreshold = loadThreshold;
	}

	@Override
	public void start() throws Exception {

		super.start();

		this.results = new ArrayList<PassageExtractionSummarizationResult>();

		Matrix eigenVectors = this.corpus.getSemanticSpace()
				.getVk();

		for (int i = 0; i < eigenVectors.getColumnDimension(); i++) {
			TreeMap<Double, Integer> v = new TreeMap<Double, Integer>();
			for (int j = 0; j < eigenVectors.getRowDimension(); j++) {
				v.put(Math.abs(eigenVectors.get(j, i)), j);
			}
			double d = v.lastKey();
			int q = v.get(d);
			PassageExtractionSummarizationResult result = new PassageExtractionSummarizationResult();
			result.setEigenVectorIndex(i);
			result.setLoad(d);
			try {
				TextDocument doc = this.repository.getTextDocument(this.corpus.getPassages()[q]);
				result.setTextPassageContent(doc.getContent());
				result.setTextPassageId(q);
				this.results.add(result);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			}
			if (this.results.size() >= this.maxResults)
				break;
		}

		super.end();
	}
}
