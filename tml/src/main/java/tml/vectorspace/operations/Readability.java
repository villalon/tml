/*******************************************************************************
 *  Copyright 2007, 2009 Luan Nguyen, Stephen O'Rourke (stephen.orourke@sydney.edu.au)
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

package tml.vectorspace.operations;

import java.text.BreakIterator;
import java.util.ArrayList;

import tml.corpus.ParagraphCorpus;
import tml.corpus.TextDocument;
import tml.vectorspace.operations.results.ReadabilityResult;

/**
 * This operation a calculation of readability measurements
 * 
 * @author Luan Nguyen, Stephen O'Rourke
 * 
 */
public class Readability extends AbstractOperation<ReadabilityResult> {

	/** Number of syllables at which a word is considered difficult. */
	private static final int THRESH_HOLD = 4;

	public Readability() {
		this.name = "Readbility Index";
	}

	/**
	 * Check if a string is a word
	 * 
	 * @param word
	 * @return if the word
	 */
	public boolean isWord(String word) {
		return word.matches("[a-zA-Z]+");
	}

	/**
	 * Check if the string (char input) is a vowel e.g a,e,i,o,u and y (y is
	 * treated as a vowel also)
	 * 
	 * @param input
	 *            a character
	 * @return if the character is a vowel
	 */
	public boolean isVowel(char input) {
		if (input == 'a' || input == 'e' || input == 'i' || input == 'o' || input == 'u' || input == 'y') {
			return true;
		}
		return false;
	}

	/**
	 * Count number of syllable per word input using the rules provided
	 * 
	 * @param input
	 *            text to count syllables from
	 * @return the number of syllables in the text
	 */
	public double countSyllable(String input) {
		double syllable = 0;
		double wordLength = input.length();
		String word = input;
		char charArray[] = word.toCharArray();
		if (word.indexOf("www") > 0 || word.indexOf("http") > 0 || word.indexOf("@") > 0 || word.indexOf(".co") > 0) {
			return 1;
		}
		for (int i = 0; i < wordLength; i++) {
			if (isVowel(charArray[i])) {

				char thisChar = charArray[i];
				char nextChar;
				char secondNextChar;
				char previousChar;
				if (i < wordLength - 1) {
					nextChar = charArray[i + 1];
				} else {
					nextChar = ' ';
				}
				if (i != 0) {
					previousChar = charArray[i - 1];

				} else {
					previousChar = ' ';
				}

				// if current character is 'a'
				if (thisChar == 'a') {
					if (nextChar == 'i' || nextChar == 'u' || nextChar == 'y') {
						i++;
					}
					syllable++;
				}

				// if current character is 'o'
				if (thisChar == 'o') {
					if (nextChar == 'a' || nextChar == 'o' || nextChar == 'u' || nextChar == 'y') {
						i++;
					} else if (nextChar == 'i' && ((i + 2) < wordLength)) {
						secondNextChar = charArray[i + 2];
						if (secondNextChar != 'n') {
							i++;
						}
					}
					syllable++;
				}

				// if current character is 'u'
				if (thisChar == 'u') {
					if (nextChar == 'e' || nextChar == 'y') {
						i++;
					} else if (nextChar == 'o' && previousChar != 'd') {
						i++;
					} else if (nextChar == 'a') {
						if (previousChar == 'q' || previousChar == 's') {
							i++;
						}
					}
					syllable++;
				}

				// if current character is 'e'
				if (thisChar == 'e') {
					if (nextChar == 'e') {
						i++;
					} else if (nextChar == 'a' && ((i + 2) < wordLength)) {
						secondNextChar = charArray[i + 2];
						if (secondNextChar != 't' && secondNextChar != 'c') {
							i++;
						}
					} else if (nextChar == 'i') {
						if (previousChar != 'r') {
							i++;
						} else if ((i + 2) < wordLength) {
							secondNextChar = charArray[i + 2];
							if (secondNextChar != 'n') {
								i++;
							}
						}
					} else if (nextChar == 'o' || nextChar == 'u' || nextChar == 'y') {
						if (previousChar != 'r') {
							i++;
						}
					} else if (nextChar == 'd') {
						if (previousChar != 'd' && previousChar != 't') {
							i++;
						}
					} else if (i == (wordLength - 1)) {
						if (previousChar != 'h' && previousChar != 'w' && previousChar != 'm' && previousChar != 'b' && previousChar != 'l' && previousChar != 'r') {
							syllable--;
						} else if (i >= 2) {
							char secondPreviousChar = charArray[i - 2];
							if (previousChar == 'r' && secondPreviousChar != 'd' && secondPreviousChar != 't' && secondPreviousChar != 'i') {
								syllable--;
							} else if (previousChar != 'r' && !isVowel(previousChar)) {
								syllable--;
							}
						}
					}
					syllable++;
				}

				// if it is a 'i'
				if (thisChar == 'i') {
					if (nextChar == 'e') {
						if ((i + 2) < wordLength) {
							secondNextChar = charArray[i + 2];
							if (secondNextChar != 'm' || secondNextChar != 'n' || secondNextChar != 't' || secondNextChar != 'r' || secondNextChar != 's') {
								i++;
								// syllable++;
							}
						}
					}
					if (nextChar == 'a') {
						if ((i + 2) < wordLength) {
							secondNextChar = charArray[i + 2];
							if (secondNextChar != 't' && previousChar != 't') {
								i++;
							}
						}
					}
					if (nextChar == 'o') {
						if ((i + 2) < wordLength) {
							secondNextChar = charArray[i + 2];
							if (secondNextChar != 't' && previousChar != 't') {
								i++;
							} else if (previousChar == 'l' || previousChar == 's' || previousChar == 't' || previousChar == 'c' || previousChar == 'n') {
								i++;
							}
						}
					}
					syllable++;
				}

				// if it is y
				if (thisChar == 'y') {
					if (i == 0) {
						i++;
					} else {
						syllable++;
					}
				}
			}
		}
		if (syllable == 0) {
			syllable = 1;
		}
		return syllable;
	}

	/**
	 * Calculate Flesch Kincaid Grade Level from number of words, number of
	 * sentence and number of syllable given This uses the standard Flesch
	 * Kincaid Grade Level formula
	 * 
	 * @param numberOfWord
	 * @param numberOfSentence
	 * @param numberOfSyllable
	 * @return Flesch Kincaid grade level
	 */
	public double calculateGradeLevel(double numberOfWord, double numberOfSentence, double numberOfSyllable) {
		double fleschKincaidGradeLevel = ((0.39 * (numberOfWord / numberOfSentence) + (11.8 * numberOfSyllable / numberOfWord)) - 15.59);
		return fleschKincaidGradeLevel;
	}

	/**
	 * Calculate Flesch Reading Ease from number of words, number of sentence
	 * and number of syllable given This uses the standard Flesch Reading Ease
	 * formula
	 * 
	 * @param numberOfWord
	 * @param numberOfSentence
	 * @param numberOfSyllable
	 * @return the Flesch Reading Ease index
	 */
	public double calculateReadingEase(double numberOfWord, double numberOfSentence, double numberOfSyllable) {
		double fleschReadingEase = 206.835 - (1.015 * (numberOfWord / numberOfSentence)) - (84.6 * numberOfSyllable / numberOfWord);
		return fleschReadingEase;
	}

	/**
	 * Calculate the difference between readability indices of 2 paragraphs a
	 * absolute value is return because we are only interested in the difference
	 * not comparing between the two paragraphs
	 * 
	 * @param number1
	 * @param number2
	 * @return difference between two paragraphs' indices
	 */
	public double calculateDifferent(double number1, double number2) {
		return Math.abs(number1 - number2);
	}

	/**
	 * Calculate the difference between readability indices of 2 consecutive
	 * paragraphs
	 * 
	 * @param results
	 */
	public void differentiate(ArrayList<ReadabilityResult> results) {
		for (int i = 0; i < results.size() - 1; i++) {
			ReadabilityResult result1 = results.get(i);
			ReadabilityResult result2 = results.get(i + 1);
			result1.setDiffReadingEase(calculateDifferent(result1.getFleshReadingEase(), result2.getFleshReadingEase()));
			result1.setDiffGradeLevel(calculateDifferent(result1.getFleshKincaidGradeLevel(), result2.getFleshKincaidGradeLevel()));
		}
	}

	/**
	 * Start the operation (non-Javadoc)
	 * 
	 * @see tml.vectorspace.operations.AbstractOperation#start()
	 */
	@Override
	public void start() throws Exception {
		super.start();
		this.results = new ArrayList<ReadabilityResult>();
		if (!(corpus instanceof ParagraphCorpus)) {
			logger.debug("This function requires paragraph corpus to meet its purpose");
			return;
		}

		for (int key = 0; key < this.corpus.getPassages().length; key++) {
			double wordInParagraph = 0;
			double sentenceInParagraph = 0;
			double syllableInWord = 0;
			double syllableInParagraph = 0;
			TextDocument passage = this.repository.getTextDocument(this.corpus.getPassages()[key]);
			ReadabilityResult result = new ReadabilityResult();
			String currentParagraph = passage.getContent();
			BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(corpus.getRepository().getLocale());
			sentenceIterator.setText(currentParagraph);
			int sentenceStart = sentenceIterator.first(), sentenceEnd = 0;
			while ((sentenceEnd = sentenceIterator.next()) != BreakIterator.DONE) {
				String currentSentence = currentParagraph.substring(sentenceStart, sentenceEnd);
				sentenceInParagraph++;
				BreakIterator wordIterator = BreakIterator.getWordInstance(corpus.getRepository().getLocale());
				wordIterator.setText(currentSentence);
				int wordStart = wordIterator.first(), wordEnd = 0;
				while ((wordEnd = wordIterator.next()) != BreakIterator.DONE) {
					String currentWord = currentSentence.substring(wordStart, wordEnd).trim().toLowerCase();
					if (isWord(currentWord)) {
						wordInParagraph++;
						syllableInParagraph += countSyllable(currentWord);
						if (syllableInWord >= THRESH_HOLD) {
							result.addHardWord(currentWord);
						}
					}
					wordStart = wordEnd;
				}
				sentenceStart = sentenceEnd;
			}
			result.setFleshKincaidGradeLevel(calculateGradeLevel(wordInParagraph, sentenceInParagraph, syllableInParagraph));
			result.setFleshReadingEase(calculateReadingEase(wordInParagraph, sentenceInParagraph, syllableInParagraph));
			result.setTextPassageContent(passage.getContent());
			result.setTextPassageId(key);
			results.add(result);
		}
		differentiate(results);
	}
}