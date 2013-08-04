/*******************************************************************************
 *  Copyright 2007, 2009 Stephen O'Rourke (stephen.orourke@sydney.edu.au)
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

import tml.utils.MatrixUtils;

import Jama.Matrix;

/**
 * Probabilistic latent semantic analysis (PLSA)
 * 
 * An explanation of the algorithm can be found in the paper:
 * 
 * Hofmann, T. (1999). Probabilistic Latent Semantic Indexing. Paper presented
 * at the Proceedings of the 22nd annual international ACM SIGIR conference on
 * Research and development in information retrieval.
 * 
 * @author Stephen O'Rourke
 * 
 */
public class ProbabilisticLatentSemanticAnalysis extends MatrixFactorisation {

	private final Log logger = LogFactory.getLog(getClass());
	private static final double SMALL_VALUE = 10e-9;
	private Matrix Pz;
	private Matrix Pz_diag;
	private Matrix Pd_z;
	private Matrix Pw_z;
	private int maxIterations = 200;
	private double tolerence = 0.01;

	@Override
	public void process(Matrix x) {
		int m = x.getRowDimension();
		int n = x.getColumnDimension();

		int K2 = Math.min(K, Math.min(n, m) - 1);

		// initialise Pz, Pd_z, Pw_z
		Pz = new Matrix(K2, 1, 1);
		Pd_z = Matrix.random(n, K2);
		Pw_z = Matrix.random(m, K2);

		// normalise columns to sum to 1
		Pz = MatrixUtils.normalizeColumnsL1(Pz);
		Pd_z = MatrixUtils.normalizeColumnsL1(Pd_z);
		Pw_z = MatrixUtils.normalizeColumnsL1(Pw_z);

		// initialise matrices for the posterior
		Matrix[] Pz_dw = new Matrix[K2];
		for (int k = 0; k < K2; k++) {
			Pz_dw[k] = new Matrix(m, n);
		}

		double li_previous = 0;

		// start EM algorithm
		for (int l = 0; l < maxIterations; l++) {

			//E step, compute posterior on z
			for (int k = 0; k < K2; k++) {
				Pz_dw[k] = Pw_z.getMatrix(0, m - 1, k, k).times(Pd_z.getMatrix(0, n - 1, k, k).transpose()).times(Pz.get(k, 0));
			}
			// normalise posterior
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					double sum = 0;
					for (int k = 0; k < K2; k++) {
						sum += Pz_dw[k].get(i, j);
					}
					for (int k = 0; k < K2; k++) {
						Pz_dw[k].set(i, j, Pz_dw[k].get(i, j) / sum);
					}
				}
			}

			//M step, maximise log-likelihood
			for (int k = 0; k < K2; k++) {
				Matrix Pw_k = x.arrayTimes(Pz_dw[k]).times(new Matrix(n, 1, 1));
				Pw_z.setMatrix(0, m - 1, k, k, Pw_k);
			}
			for (int k = 0; k < K2; k++) {
				Matrix Pd_k = x.arrayTimes(Pz_dw[k]).transpose().times(new Matrix(m, 1, 1));
				Pd_z.setMatrix(0, n - 1, k, k, Pd_k);
			}
			Pz = Pd_z.transpose().times(new Matrix(n, 1, 1));

			// normalise columns to sum to 1
			Pw_z = MatrixUtils.normalizeColumnsL1(Pw_z);
			Pd_z = MatrixUtils.normalizeColumnsL1(Pd_z);
			Pz = MatrixUtils.normalizeColumnsL1(Pz);

			// calculate log-likelihood
			Pz_diag = Matrix.identity(K2, K2);
			for (int k = 0; k < K2; k++) {
				Pz_diag.set(k, k, Pz.get(k, 0));
			}
			Matrix logMatrix = Pw_z.times(Pz_diag).times(Pd_z.transpose());
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					logMatrix.set(i, j, Math.log(logMatrix.get(i, j) + SMALL_VALUE));
				}
			}
			double li = x.arrayTimes(logMatrix).norm1();
			
			// add small value to Pw_z.
			Pw_z = Pw_z.plus(new Matrix(m, K2, SMALL_VALUE));

			// check for convergence
			if (l > 1) {
				double change = li_previous - li;
				logger.debug(l + ".\t log-likelihood change " + change);
				if (change < tolerence) {
					break;
				}
			}
			li_previous = li;
		}

		decomposition = new SpaceDecomposition();
		decomposition.setSkdata(Pz_diag.getArray());
		decomposition.setUkdata(Pw_z.getArray());
		decomposition.setVkdata(Pd_z.getArray());
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
	
	public double getTolerence() {
		return tolerence;
	}

	public void setTolerence(double tolerence) {
		this.tolerence = tolerence;
	}

}
