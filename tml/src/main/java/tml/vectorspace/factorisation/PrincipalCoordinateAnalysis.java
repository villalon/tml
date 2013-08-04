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

import tml.utils.DistanceLib;
import tml.utils.DistanceLib.DistanceMeasure;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Principal Coordinate Analysis
 * 
 * @author Stephen O'Rourke
 */
public class PrincipalCoordinateAnalysis {
	public static final int X = 0; // attribute Y
	public static final int Y = 1; // attribute X
	private static final int p = 2; // number of dimensions
	private DistanceMeasure distanceMeasure = DistanceMeasure.EUCLIDEAN;

	public Instances scale(Instances instances) {
		// number of points
		int n = instances.numInstances();

		// distance matrix
		Matrix d = new Matrix(n, n);
		Matrix G = new Matrix(n, n);

		// points instances
		FastVector attributes = new FastVector(p);
		attributes.addElement(new Attribute("X"));
		attributes.addElement(new Attribute("Y"));
		Instances x = new Instances("PCO", attributes, instances.numInstances());

		// calculate distance matrix
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < j; i++) {
				double distance = this.distance(instances.instance(i), instances.instance(j));
				d.set(i, j, distance);
				d.set(j, i, distance);
			}
		}

		// create centered matrix G by centering the elements of A
		Matrix A = d.arrayTimes(d).times((double) -1 / 2);
		Matrix B = Matrix.identity(n, n).minus(new Matrix(n, n, 1).times((double) 1 / n));
		G = B.times(A).times(B);

		// eigenvalue decomposition
		EigenvalueDecomposition eig = G.eig();
		Matrix eigenvalues = eig.getD();
		Matrix eigenvectors = eig.getV();

		// output eigenvectors as the principal coordinate axes, and normalise 
		// them by dividing by the square root of their corresponding eigenvalue.
		for (int i = 0; i < n; i++) {
			Instance instance = new Instance(p);
			instance.setValue(X, eigenvectors.get(i, X) / Math.copySign(Math.sqrt(Math.abs(eigenvalues.get(X, X))), eigenvalues.get(X, X)));
			instance.setValue(Y, eigenvectors.get(i, Y) / Math.copySign(Math.sqrt(Math.abs(eigenvalues.get(Y, Y))), eigenvalues.get(Y, Y)));
			x.add(instance);
		}
		
		return x;
	}

	protected double distance(Instance inst1, Instance inst2) {
		double distance = Math.sqrt(1 - DistanceLib.distance(distanceMeasure, inst1, inst2));
		return distance;
	}
	
	public DistanceMeasure getDistanceMeasure() {
		return distanceMeasure;
	}

	public void setDistanceMeasure(DistanceMeasure distanceMeasure) {
		this.distanceMeasure = distanceMeasure;
	}

}
