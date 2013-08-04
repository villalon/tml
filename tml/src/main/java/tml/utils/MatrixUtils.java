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
package tml.utils;

import org.apache.log4j.Logger;

import Jama.Matrix;

public class MatrixUtils {
	private static Logger logger = Logger.getLogger(MatrixUtils.class);

	public static Matrix normalizeRows(Matrix m) {
		Matrix norm = m.copy();
		for (int i = 0; i < m.getRowDimension(); i++) {
			Matrix v = m.getMatrix(i, i, 0, m.getColumnDimension() - 1);
			for (int j = 0; j < m.getColumnDimension(); j++) {
				double normVector = v.normF();
				if (normVector == 0) {
					if (m.get(i, j) != 0)
						logger.warn("Norm is 0 for vector where value was " + m.get(i, j));
					norm.set(i, j, 0);
				} else if (Double.isNaN(normVector))
					logger.error("INVALID NORM FOR DOCUMENT VECTOR (" + i + "," + j);
				else
					norm.set(i, j, m.get(i, j) / v.normF());
			}
		}
		return norm;
	}

	public static Matrix normalizeColumns(Matrix m) {
		Matrix norm = m.copy();
		for (int i = 0; i < m.getColumnDimension(); i++) {
			Matrix v = m.getMatrix(0, m.getRowDimension() - 1, i, i);
			for (int j = 0; j < m.getRowDimension(); j++) {
				double normVector = v.normF();
				if (normVector == 0) {
					if (m.get(j, i) != 0)
						logger.warn("Norm is 0 for vector where value was " + m.get(j, i));
					norm.set(j, i, 0);
				} else if (Double.isNaN(normVector))
					logger.error("INVALID NORM FOR DOCUMENT VECTOR (" + j + "," + i);
				else
					norm.set(j, i, m.get(j, i) / v.normF());
			}
		}
		return norm;
	}

	public static Matrix normalizeRowsL1(Matrix m) {
		Matrix norm = m.copy();
		for (int i = 0; i < m.getRowDimension(); i++) {
			double rowNorm1 = m.getMatrix(i, i, 0, m.getColumnDimension() - 1).norm1();
			if (rowNorm1 != 0) {
				for (int j = 0; j < m.getColumnDimension(); j++) {
					norm.set(i, j, m.get(i, j) / rowNorm1);
				}
			}
		}
		return norm;
	}

	public static Matrix normalizeColumnsL1(Matrix m) {
		Matrix norm = m.copy();
		for (int j = 0; j < m.getColumnDimension(); j++) {
			double colNormL1 = m.getMatrix(0, m.getRowDimension() - 1, j, j).norm1();
			if (colNormL1 != 0) {
				for (int i = 0; i < m.getRowDimension(); i++) {
					norm.set(i, j, m.get(i, j) / colNormL1);
				}
			}
		}
		return norm;
	}
}
