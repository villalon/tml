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

import java.util.ArrayList;

/**
 * 
 * @author Luan Nguyen
 * 
 */
public class ReadabilityResult extends AbstractResult {
	double fleshReadingEase;
	double fleshKincaidGradeLevel;
	ArrayList<String> hardWords;
	String textPassageContent;
	int textPassageId;
	double diffReadingEase;
	double diffGradeLevel;

	/**
	 * constructor of a readability index result
	 */
	public ReadabilityResult() {
		hardWords = new ArrayList<String>();

		fleshReadingEase = 0.0;
		fleshKincaidGradeLevel = 0.0;

	}

	/**
	 * @return the fleshReadingEase
	 */
	public double getFleshReadingEase() {
		return fleshReadingEase;
	}

	/**
	 * @param fleshReadingEase the fleshReadingEase to set
	 */
	public void setFleshReadingEase(double fleshReadingEase) {
		this.fleshReadingEase = fleshReadingEase;
	}

	/**
	 * @return the fleshKincaidGradeLevel
	 */
	public double getFleshKincaidGradeLevel() {
		return fleshKincaidGradeLevel;
	}

	/**
	 * @param fleshKincaidGradeLevel the fleshKincaidGradeLevel to set
	 */
	public void setFleshKincaidGradeLevel(double fleshKincaidGradeLevel) {
		this.fleshKincaidGradeLevel = fleshKincaidGradeLevel;
	}

	/**
	 * @return the hardWords
	 */
	public ArrayList<String> getHardWords() {
		return hardWords;
	}

	/**
	 * @param hardWords the hardWords to set
	 */
	public void setHardWords(ArrayList<String> hardWords) {
		this.hardWords = hardWords;
	}

	/**
	 * @return the textPassageContent
	 */
	public String getTextPassageContent() {
		return textPassageContent;
	}

	/**
	 * @param textPassageContent the textPassageContent to set
	 */
	public void setTextPassageContent(String textPassageContent) {
		this.textPassageContent = textPassageContent;
	}

	/**
	 * @return the textPassageId
	 */
	public int getTextPassageId() {
		return textPassageId;
	}

	/**
	 * @param textPassageId the textPassageId to set
	 */
	public void setTextPassageId(int textPassageId) {
		this.textPassageId = textPassageId;
	}

	/**
	 * @return the diffReadingEase
	 */
	public double getDiffReadingEase() {
		return diffReadingEase;
	}

	/**
	 * @param diffReadingEase the diffReadingEase to set
	 */
	public void setDiffReadingEase(double diffReadingEase) {
		this.diffReadingEase = diffReadingEase;
	}

	/**
	 * @return the diffGradeLevel
	 */
	public double getDiffGradeLevel() {
		return diffGradeLevel;
	}

	/**
	 * @param diffGradeLevel the diffGradeLevel to set
	 */
	public void setDiffGradeLevel(double diffGradeLevel) {
		this.diffGradeLevel = diffGradeLevel;
	}
	
	/**
	 * adds a hard word
	 * @param word the word to add
	 */
	public void addHardWord(String word) {
		hardWords.add(word);
	}
}
