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

import tml.vectorspace.operations.results.FactorAnalysisPlotResult;
import Jama.Matrix;

/**
 * This operation simply presents the content of the reconstructed term/doc
 * matrix with a column for documents and the first row showing the terms
 * 
 * @author Jorge Villalon
 * 
 */
public class FactorAnalysisPlot extends AbstractOperation<FactorAnalysisPlotResult> {

	public FactorAnalysisPlot() {
		this.name = "Factor analysis";
		this.requiresSemanticSpace = true;
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		Matrix u = this.corpus.getSemanticSpace().getUk();
		Matrix v = this.corpus.getSemanticSpace().getVk();

		for (int i = 0; i < u.getRowDimension(); i++) {
			FactorAnalysisPlotResult result = new FactorAnalysisPlotResult();
			result.setName(this.corpus.getTerms()[i]);
			result.setX(u.get(i, 0));
			result.setY(u.get(i, 1));
			results.add(result);
		}
		for (int i = u.getRowDimension(); i < u.getRowDimension()
				+ v.getRowDimension(); i++) {
			FactorAnalysisPlotResult result = new FactorAnalysisPlotResult();
			result.setName(this.corpus.getPassages()[i - u.getRowDimension()]);
			result.setX(v.get(i - u.getRowDimension(), 0));
			result.setY(v.get(i - u.getRowDimension(), 1));
			results.add(result);
		}
		super.end();
	}
}
