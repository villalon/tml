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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import tml.utils.DistanceLib;
import tml.utils.DistanceLib.DistanceMeasure;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import Jama.Matrix;

/**
 * This class converts instances for plotting using Multidimensional Scaling. It
 * use a Newton-Raphson algorithm to project instances into 2 dimensions.
 * 
 * Details of this algorithm can be found in the paper:
 * http://www.pavis.org/essay/multidimensional_scaling.html
 * 
 * @author Stephen O'Rourke
 */
public class MultiDimensionalScalingNR {
	public static final int X = 0; // attribute Y
	public static final int Y = 1; // attribute X

	private static final int p = 2; // number of dimensions
	private double tolerence = 0.01;
	private int maxIterations = 1000; // maximum iterations

	private double error;
	private Matrix d;
	private Matrix d_hat;
	private Instances initialX;

	private DistanceMeasure lowDimensionalDistanceMeasure = DistanceMeasure.EUCLIDEAN;
	private DistanceMeasure highDimensionlDistanceMeasure = DistanceMeasure.COSINE;

	private final Log logger = LogFactory.getLog(getClass());

	public Instances scale(Instances instances) {
		// approximation error
		error = 0.0;
		double error_previous;

		// number of points
		int n = instances.numInstances();

		// distance between points in the p-dimensional layout
		d = new Matrix(n, n);
		Matrix d_previous;

		// dissimilarity between vectors
		d_hat = new Matrix(n, n);

		// points instances
		FastVector attributes = new FastVector(p);
		attributes.addElement(new Attribute("X"));
		attributes.addElement(new Attribute("Y"));
		Instances x = new Instances("MDS", attributes, instances.numInstances());
		Instances x_previous;

		// initialise points sequence
		ArrayList<Integer> kseq = new ArrayList<Integer>();
		for (int k = 0; k < n; k++) {
			kseq.add(k);
		}

		// initialise x
		if (initialX != null) {
			x = new Instances(initialX);

		} else {
			Random rand = new Random();
			for (int k = 0; k < n; k++) {
				Instance x_inst = new Instance(p);
				x_inst.setValue(X, rand.nextDouble() - rand.nextInt(1));
				x_inst.setValue(Y, rand.nextDouble() - rand.nextInt(1));
				x.add(x_inst);
			}
		}

		// calculate d
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < j; i++) {
				double distance = this.distance(x.instance(i), x.instance(j));
				d.set(i, j, distance);
				d.set(j, i, distance);

				double dissimilarity = this.dissimilarity(instances.instance(i), instances.instance(j));
				d_hat.set(i, j, dissimilarity);
				d_hat.set(j, i, dissimilarity);

				if (d_hat.get(i, j) != 0) {
					error += Math.pow(d.get(i, j) - d_hat.get(i, j), 2) / Math.pow(d_hat.get(i, j), 2);
				}
			}
		}

		// record previous results
		error_previous = error;
		d_previous = d.copy();
		x_previous = new Instances(x);

		// start of Newton-Raphson method
		logger.info("Starting Newton-Raphson MDS.");
		for (int iter = 0; iter < maxIterations; iter++) {
			// randomise points sequence to ensure faster convergence
			Collections.shuffle(kseq);
			for (int k : kseq) {
				Matrix gradient = new Matrix(p, 1);
				Matrix hessian = new Matrix(p, p);

				// calculate gradient vector
				for (int a = 0; a < p; a++) {
					double sum = 0;
					for (int l = 0; l < n; l++) {
						if (k != l) {
							if (d.get(k, l) != 0 && d_hat.get(k, l) != 0) {
								sum += ((d.get(k, l) - d_hat.get(k, l)) / (d.get(k, l) * Math.pow(d_hat.get(k, l), 2))) * (x.instance(k).value(a) - x.instance(l).value(a));
							}
						}
					}
					gradient.set(a, 0, 2 * sum);
				}

				// calculate hessian matrix
				for (int a = 0; a < p; a++) {
					for (int b = 0; b < p; b++) {
						double sum = 0.0;
						if (a != b) {
							for (int l = 0; l < n; l++) {
								if (k != l) {
									if (d.get(k, l) != 0 && d_hat.get(k, l) != 0) {
										sum += ((x.instance(k).value(a) - x.instance(l).value(a)) * (x.instance(k).value(b) - x.instance(l).value(b))) / (Math.pow(d.get(k, l), 3) * d_hat.get(k, l));
									}
								}
							}
							sum = 2 * sum;
						} else {
							for (int l = 0; l < n; l++) {
								if (k != l) {
									if (d_hat.get(k, l) != 0 && d.get(k, l) != 0) {
										sum += (1.0 / Math.pow(d_hat.get(k, l), 2)) - (Math.pow(d.get(k, l), 2) - Math.pow((x.instance(k).value(a) - x.instance(l).value(a)), 2)) / (Math.pow(d.get(k, l), 3) * d_hat.get(k, l));
									}
								}
							}
							sum = 2 * sum;
						}
						hessian.set(a, b, sum);
					}
				}

				// update x
				Matrix x_k = new Matrix(x.instance(k).toDoubleArray(), p);
				Matrix x_k_tilda = x_k.minus(hessian.inverse().times(gradient));
				x.instance(k).setValue(X, x_k_tilda.get(X, 0));
				x.instance(k).setValue(Y, x_k_tilda.get(Y, 0));
			}

			// calculate d and error
			error = 0;
			for (int j = 0; j < n; j++) {
				for (int i = 0; i < j; i++) {
					double distance = this.distance(x.instance(i), x.instance(j));
					d.set(i, j, distance);
					d.set(j, i, distance);

					if (d_hat.get(i, j) != 0) {
						error += Math.pow(d.get(i, j) - d_hat.get(i, j), 2) / Math.pow(d_hat.get(i, j), 2);
					}
				}
			}

			if (error < error_previous) {
				logger.debug(iter + ".\t error " + error);
				if (error_previous - error <= tolerence) {
					break;
				}

				error_previous = error;
				d_previous = d.copy();
				x_previous = new Instances(x);
			} else // invalidates last run
			{
				x = new Instances(x_previous);
				d = d_previous.copy();
			}
		}

		logger.info("Finished Newton-Raphson MDS.");
		return x;
	}

	protected double distance(Instance inst1, Instance inst2) {
		double distance = DistanceLib.distance(lowDimensionalDistanceMeasure, inst1, inst2);
		return distance;
	}

	protected double dissimilarity(Instance inst1, Instance inst2) {
		double distance = Math.sqrt(1 - DistanceLib.distance(highDimensionlDistanceMeasure, inst1, inst2));
		return distance;
	}

	public double getTolerence() {
		return tolerence;
	}

	public void setTolerence(double tolerence) {
		this.tolerence = tolerence;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public DistanceMeasure getLowDimensionalDistanceMeasure() {
		return lowDimensionalDistanceMeasure;
	}

	public void setLowDimensionalDistanceMeasure(DistanceMeasure lowDimensionalDistanceMeasure) {
		this.lowDimensionalDistanceMeasure = lowDimensionalDistanceMeasure;
	}

	public DistanceMeasure getHighDimensionlDistanceMeasure() {
		return highDimensionlDistanceMeasure;
	}

	public void setHighDimensionlDistanceMeasure(DistanceMeasure highDimensionlDistanceMeasure) {
		this.highDimensionlDistanceMeasure = highDimensionlDistanceMeasure;
	}

	public Instances getInitialX() {
		return initialX;
	}

	public void setInitialX(Instances initialX) {
		this.initialX = initialX;
	}

	public Matrix d() {
		return d;
	}

	public Matrix d_hat() {
		return d_hat;
	}

	public double error() {
		return error;
	}
}
