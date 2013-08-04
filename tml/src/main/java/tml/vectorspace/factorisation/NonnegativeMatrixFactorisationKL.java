/*******************************************************************************
 *  Copyright 2007, 2009 Jorge Villalon (jorge.villalon@uai.cl), Stephen O'Rourke (stephen.orourke@sydney.edu.au)
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
package tml.vectorspace.factorisation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Jama.Matrix;

/**
 * NMF with Kullback-Leibler divergence minimisation).
 * 
 * Details of this algorithm can be found in the paper:
 * 
 * Lee, D. D., & Seung, H. S. (2001). Algorithms for Non-negative Matrix
 * Factorization. Paper presented at the Proceedings of the 2000 Conference on
 * Advances in Neural Information Processing Systems.
 */
public class NonnegativeMatrixFactorisationKL extends MatrixFactorisation {
	protected final Log logger = LogFactory.getLog(getClass());
	protected Matrix initialW = null;
	protected Matrix initialH = null;
	protected Matrix w;
	protected Matrix h;
	protected static final double SMALL_VALUE = 10e-9;
	protected int maxIterations = 200;

	@Override
	public void process(Matrix v) {
		int m = v.getRowDimension();
		int n = v.getColumnDimension();
		int K2 = Math.min(K, Math.min(n, m) - 1);

		v = v.times(1 / v.norm1());

		// initialise h
		if (initialH != null) {
			h = initialH.copy();
		} else {
			h = Matrix.random(K2, n);
		}

		// initialise w
		if (initialW != null) {
			w = initialW.copy();
		} else {
			w = Matrix.random(m, K2);
		}

		// perform update iterations
		double fnorm_previous = v.minus(w.times(h)).norm2();
		for (int l = 0; l < maxIterations; l++) {
			// simultaneous update of w and h
			Matrix wh = w.times(h);
			Matrix h_copy = h.copy();

			for (int c = 0; c < K2; c++) {
				// update h
				for (int j = 0; j < n; j++) {
					double sum1 = 0, sum2 = 0;
					for (int i = 0; i < m; i++) {
						sum1 += w.get(i, c) * v.get(i, j) / (wh.get(i, j) + SMALL_VALUE);
						sum2 += w.get(i, c);
					}
					h.set(c, j, h.get(c, j) * sum1 / sum2);
				}

				// update w
				for (int i = 0; i < m; i++) {
					double sum1 = 0, sum2 = 0;
					for (int j = 0; j < n; j++) {
						sum1 += h_copy.get(c, j) * v.get(i, j) / (wh.get(i, j) + SMALL_VALUE);
						sum2 += h_copy.get(c, j);
					}
					w.set(i, c, w.get(i, c) * sum1 / sum2);
				}
			}

			// check if converged
			double fnorm = v.minus(w.times(h)).norm2();
			double change = Math.abs(fnorm_previous - fnorm);
			logger.debug(l + "\t change " + change);
			if (change <= SMALL_VALUE) {
				break;
			}

			fnorm_previous = fnorm;
		}

		decomposition = new SpaceDecomposition();
		decomposition.setSkdata(Matrix.identity(K2, K2).getArray());
		decomposition.setUkdata(w.getArray());
		decomposition.setVkdata(h.transpose().getArray());
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
}
