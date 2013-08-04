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
package tml.annotators;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;

import tml.Configuration;
import tml.utils.StanfordUtils;


import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import java.io.FileInputStream;

/**
 * Annotator that implements the PennTree bank from the Stanford parser. It obtains the
 * PennTree string and stores it for further processing.
 * 
 * @author Jorge Villalon
 */
public class PennTreeAnnotator extends AbstractAnnotator implements Annotator {

	public static String FIELD_NAME = "penntree";
	private static String[] types = {"sentence"};
	private static Logger logger = Logger.getLogger(PennTreeAnnotator.class);
	private static LexicalizedParser parser = null;
	private static TreebankLanguagePack treeBankLanguagePack = null;
	private static GrammaticalStructureFactory grammaticalStructureFactory = null;
	/**
	 * @return the grammaticalStructureFactory
	 * @throws IOException 
	 */
	public static GrammaticalStructureFactory getGrammaticalStructureFactory() throws IOException {
		if(grammaticalStructureFactory == null) {
			logger.debug("PennTreeAnnotator was not initialized, initializing");
			(new PennTreeAnnotator()).init();
		}
		return grammaticalStructureFactory;
	}

	/**
	 * @return the lexicalizedParser
	 * @throws IOException 
	 */
	public static LexicalizedParser getParser() throws IOException {
		if(parser == null) {
			logger.debug("PennTreeAnnotator was not initialized, initializing");
			(new PennTreeAnnotator()).init();
		}
		return parser;
	}

	/**
	 * @return the treeBankLanguagePack
	 * @throws IOException 
	 */
	public static TreebankLanguagePack getTreeBankLanguagePack() throws IOException {
		if(treeBankLanguagePack == null) {
			logger.debug("PennTreeAnnotator was not initialized, initializing");
			(new PennTreeAnnotator()).init();
		}
		return treeBankLanguagePack;
	}

	private boolean lexicalized = false;

	public PennTreeAnnotator() throws IOException {
		super(FIELD_NAME, types);
	}

	@Override
	public String[] getAnnotatedText(String annotationLabel) {
		// TODO Redo when UIMA is added
		return null;
	}

	@Override
	public String getAnnotations(String text) {
		try {
			return StanfordUtils.getPennString(StanfordUtils.getPennTree(text));
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}

	@Override
	public Object getSchema() {
		// We don't have a schema, we should use UIMA
		return null;
	}

	@Override
	public void init() {
		try {
			Configuration.getTmlProperties();
		} catch (IOException e1) {
			e1.printStackTrace();
			logger.error("No properties");
			return;
		}
		String PARSER_FILE = Configuration.getTmlFolder() + "/stanford/englishPCFG.ser";
		String PARSER_FILE_LEXICALIZED = Configuration.getTmlFolder() + "/stanford/englishFactored.ser";
		String parserFile = null;
		try {
			if(this.isLexicalized()) {
				parserFile = PARSER_FILE_LEXICALIZED;
			}
			else {
				parserFile = PARSER_FILE;
			}
			parser = new LexicalizedParser(new ObjectInputStream(
                    new FileInputStream(parserFile)));
		} catch (Exception e) {
			logger.error("Couldn't load Stanford parser! "
					+ e.getLocalizedMessage());
			return;
		}
		parser.setOptionFlags(new String[] { "-maxLength",
				"800", "-retainTmpSubcategories" });
		treeBankLanguagePack = new PennTreebankLanguagePack();
		grammaticalStructureFactory = treeBankLanguagePack.grammaticalStructureFactory();
		logger.info("PennTreeAnnotator initialized, using " + parserFile);
	}

	/**
	 * @return the lexicalized
	 */
	public boolean isLexicalized() {
		return lexicalized;
	}

	/**
	 * @param lexicalized the lexicalized to set
	 */
	public void setLexicalized(boolean lexicalized) {
		this.lexicalized = lexicalized;
	}
}
