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
package tml.vectorspace.operations.results;

public class Summary {

	int[] passagesRank;
	int[] termsRank;
	double[] passagesLoads;
	double[] termsLoads;
	
	/**
	 * @return the passagesRank
	 */
	public int[] getPassagesRank() {
		return passagesRank;
	}
	/**
	 * @param passagesRank the passagesRank to set
	 */
	public void setPassagesRank(int[] passagesRank) {
		this.passagesRank = passagesRank;
	}
	/**
	 * @return the termsRank
	 */
	public int[] getTermsRank() {
		return termsRank;
	}
	/**
	 * @param termsRank the termsRank to set
	 */
	public void setTermsRank(int[] termsRank) {
		this.termsRank = termsRank;
	}
	/**
	 * @return the passagesLoads
	 */
	public double[] getPassagesLoads() {
		return passagesLoads;
	}
	/**
	 * @param passagesLoads the passagesLoads to set
	 */
	public void setPassagesLoads(double[] passagesLoads) {
		this.passagesLoads = passagesLoads;
	}
	/**
	 * @return the termsLoads
	 */
	public double[] getTermsLoads() {
		return termsLoads;
	}
	/**
	 * @param termsLoads the termsLoads to set
	 */
	public void setTermsLoads(double[] termsLoads) {
		this.termsLoads = termsLoads;
	}
}
