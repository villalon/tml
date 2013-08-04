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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tml.vectorspace.operations.results.TagCloudsResult;


/**
 * TagClouds returns the list of terms in the {@link Corpus} weighted by
 * the term weighting scheme used in the {@link SemanticSpace}.
 * 
 * @author Jorge Villalon
 *
 */
public class TagClouds extends AbstractOperation<TagCloudsResult> {

	/**
	 * 
	 */
	public TagClouds() {
		this.name = "Tagclouds";
		this.requiresSemanticSpace = false;
	}

	@Override
	public void start() throws Exception {
		super.start();

		this.results = new ArrayList<TagCloudsResult>();

		double max = 0;
		for (int termIndex = 0; termIndex < corpus.getTerms().length; termIndex++) {
			String term = corpus.getTerms()[termIndex];
			double weight = corpus.getTermStats()[termIndex].sum;
			TagCloudsResult result = new TagCloudsResult(term, weight);
			if(weight > max)
				max = weight;
			this.results.add(result);
		}

		if(max == 0)
			max = 1;
		
		for (TagCloudsResult result : this.results) {
			result.setWeight(result.getWeight()/max);
		}

		Collections.sort(this.results,
				new Comparator<TagCloudsResult>() {

					@Override
					public int compare(TagCloudsResult arg0,
							TagCloudsResult arg1) {
						int weight0 = (int) (arg0.getWeight() * 100);
						int weight1 = (int) (arg1.getWeight() * 100);
						return weight1 - weight0;
					}
				});

		super.end();
	}
}
