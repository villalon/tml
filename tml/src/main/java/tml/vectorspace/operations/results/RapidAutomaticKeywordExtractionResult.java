/*******************************************************************************
 *  Copyright 2010 Stephen O'Rourke (stephen.orourke@sydney.edu.au)
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

/**
 * This class represents the result of a {@link RapidAutomaticKeywordExtraction}
 * operation.
 * 
 * @author Stephen O'Rourke
 * 
 */
public class RapidAutomaticKeywordExtractionResult extends AbstractResult implements Comparable<RapidAutomaticKeywordExtractionResult> {
	private String keyword;
	private Double weighting;

	public String getKeyword() {
		return keyword;
	}

	public Double getWeighting() {
		return weighting;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setWeighting(Double weighting) {
		this.weighting = weighting;
	}

	@Override
	public int compareTo(RapidAutomaticKeywordExtractionResult result) {
		return this.weighting.compareTo(result.weighting);
	}
}
