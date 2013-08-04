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
package tml.vectorspace.factorisation;

import Jama.Matrix;

public abstract class MatrixFactorisation {
	
	/** Terms matrix in the semantic space */
	protected Matrix Uk = null;
	/** Singular values in the semantic space */
	protected Matrix Sk = null;
	/** Documents matrix in the semantic space */
	protected Matrix Vk = null;
	/** The number of dimensions that were kept */
	protected int dimensionsKept = -1;

	protected SpaceDecomposition decomposition;
	protected int K;
	
	public int getK() {
		return K;
	}
	
	public void setK(int K) {
		this.K = K;
	}
	
	public abstract void process(Matrix v);
	
	public SpaceDecomposition getDecomposition() {
		return this.decomposition;
	}
}
