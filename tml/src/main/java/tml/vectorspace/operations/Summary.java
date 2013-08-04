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

import tml.vectorspace.operations.results.SummaryResult;


/**
 * This operation returns several descriptive statistics on the corpus.
 * 
 * @author Jorge Villalon
 *
 */
public class Summary extends AbstractOperation<SummaryResult> {

	/**
	 * 
	 */
	public Summary() {
		this.name = "Summary";
	}

	@Override
	public void start() throws Exception {
		super.start();

		this.results = new ArrayList<SummaryResult>();
		SummaryResult result = new SummaryResult();
		result.setItem("Documents");
			result.setValue(Integer.toString(this.corpus.getPassages().length));
		result.setComment("Number of documents in the corpus");
		results.add(result);
		result = new SummaryResult();
		result.setItem("Terms");
		result.setValue(Integer.toString(this.corpus.getTerms().length));
		result.setComment("Number of terms in the corpus");
		results.add(result);
		result = new SummaryResult();
		result.setItem("Term selection criteria");
		result.setValue(this.corpus.getParameters().getTermSelectionCriterion() + " ["
				+ this.corpus.getParameters().getTermSelectionThreshold() + "]"); // "Value"
		result
				.setComment("The selection criteria used to create the dictionary");
		results.add(result);

		super.end();
	}

}
