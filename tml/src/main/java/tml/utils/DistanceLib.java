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
package tml.utils;

import weka.core.Instance;
import weka.core.matrix.DoubleVector;

/**
 * A library for calculating the semantic distance between instances.
 * 
 * @author Stephen O'Rourke
 *
 */
public class DistanceLib {
	
	public static enum DistanceMeasure {COSINE, EUCLIDEAN, JACCARD, JENSEN_SHANNON, KULLBACK_LEIBLER};
	
	public static double distance(DistanceMeasure distanceMeasure, Instance inst1, Instance inst2) {
		switch (distanceMeasure) 
		{
			case COSINE: {
				return cosine(inst1, inst2);
			}
			case EUCLIDEAN: {
				return euclidean(inst1, inst2);
			}
			case JACCARD: {
				return jaccard(inst1, inst2);
			}
			case JENSEN_SHANNON: {
				return jensenShannon(inst1, inst2);
			}
			case KULLBACK_LEIBLER: {
				return kullbackLeibler(inst1, inst2);
			}
			default: {
				return Double.NaN;
			}
		}
	}
	
	public static double euclidean(Instance inst1, Instance inst2) {
		DoubleVector x = new DoubleVector(inst1.toDoubleArray());
		DoubleVector y = new DoubleVector(inst2.toDoubleArray());

		double distance = x.minus(y).norm2();
		return distance;
	}

	public static double cosine(Instance inst1, Instance inst2) {
		DoubleVector x = new DoubleVector(inst1.toDoubleArray());
		DoubleVector y = new DoubleVector(inst2.toDoubleArray());

		double dotXY = x.times(y).norm1();
		double cosim = dotXY / (x.norm2() * y.norm2());

		return cosim;
	}

	public static double jaccard(Instance inst1, Instance inst2) {
		DoubleVector x = new DoubleVector(inst1.toDoubleArray());
		DoubleVector y = new DoubleVector(inst2.toDoubleArray());

		double intersection = 0.0;

		for (int i = 0; i < x.size(); i++) {
			intersection += Math.min(x.get(i), y.get(i));
		}
		if (intersection > 0.0) {
			double union = x.norm1() + y.norm1() - intersection;
			return intersection / union;
		} else {
			return 0.0;
		}
	}

	public static double kullbackLeibler(Instance inst1, Instance inst2) {

	    double divergence = 0.0;
	    for (int i = 0; i < inst1.numAttributes(); ++i) {
	    	if (inst1.value(i) != 0 && inst2.value(i) != 0) {
	    		divergence += inst1.value(i) * Math.log(inst1.value(i) / inst2.value(i));
	    	}
	    }
	    divergence /= Math.log(2);
	    return divergence;
	}

	public static double jensenShannon(Instance inst1, Instance inst2) {
		
	    Instance averageInst = new Instance(inst1.numAttributes());
	    for (int i=0; i<inst1.numAttributes(); i++) {
	    	averageInst.setValue(i, (inst1.value(i) + inst2.value(i))/2);
	    }

	    double divergence = (kullbackLeibler(inst1, averageInst) + kullbackLeibler(inst2, averageInst))/2;
		return divergence;
	}
}
