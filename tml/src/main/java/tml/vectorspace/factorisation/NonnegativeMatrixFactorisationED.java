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

import Jama.Matrix;

/**
 * NMF with Euclidean distance minimisation
 * 
 * Details of this algorithm can be found in the paper:
 * 
 * Lee, D. D., & Seung, H. S. (2001). Algorithms for Non-negative Matrix
 * Factorization. Paper presented at the Proceedings of the 2000 Conference on
 * Advances in Neural Information Processing Systems.
 */
public class NonnegativeMatrixFactorisationED extends NonnegativeMatrixFactorisationKL {

	@Override
	public void process(Matrix v) {
		int m = v.getRowDimension();
		int n = v.getColumnDimension();
		int K2 = Math.min(K, Math.min(n, m) - 1);

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
			Matrix ht = h.transpose();
			Matrix vht = v.times(ht);
			Matrix whht = w.times(h).times(ht);
			Matrix wt = w.transpose();
			Matrix wtv = wt.times(v);
			Matrix wtwh = wt.times(w).times(h);

			for (int c = 0; c < K2; c++) {
				// update h
				for (int j = 0; j < n; j++) {
					double value = h.get(c, j) * wtv.get(c, j) / (wtwh.get(c, j) + SMALL_VALUE);
					h.set(c, j, value);
				}

				// update w
				for (int i = 0; i < m; i++) {
					double value = w.get(i, c) * vht.get(i, c) / (whht.get(i, c) + SMALL_VALUE);
					w.set(i, c, value);
				}
			}

			// normalise w columns vectors
			for (int j = 0; j < K2; j++) {
				double norm = 0;
				for (int i = 0; i < m; i++) {
					norm += Math.pow(w.get(i, j), 2);
				}
				norm = Math.sqrt(norm);
				for (int i = 0; i < m; i++) {
					w.set(i, j, w.get(i, j) / norm);
				}
			}

			// check if converged
			double fnorm = v.minus(w.times(h)).norm2();
			double change = Math.abs(fnorm_previous - fnorm);
			logger.debug(l + ".\t change " + fnorm);
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
}
