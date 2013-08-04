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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import tml.annotators.PennTreeAnnotator;


import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.ParseException;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

/**
 * Class to consolidate the calls to the Stanford parser
 * @author Jorge Villalon
 * 
 */
public class StanfordUtils {

	private static Logger logger = Logger.getLogger(StanfordUtils.class);
	private static GrammaticalStructureFactory factory = null;
	
	private static GrammaticalStructureFactory getGrammaticalStructureFactory() throws IOException {
		if(factory == null) {
			factory = PennTreeAnnotator.getGrammaticalStructureFactory();
		}
		return factory;
	}

	private static Hashtable<String, Tree> pennTreeCache = new Hashtable<String, Tree>();

	/**
	 * @param t a grammar tree to extract the verbs
	 * @return a list of verbs in the tree, an empty list if nothing is found.
	 */
	public static List<String> extractVerbs(Tree t) {
		List<String> verbs = new ArrayList<String>();

		if(t == null)
			return verbs;

		TregexPattern pattern = null;
		try {
			pattern = TregexPattern
			.compile("/VB.?/");
			TregexMatcher matcher = pattern.matcher(t);
			while (matcher.findNextMatchingNode()) {
				String content = cleanNodeContent(nodeContent(matcher.getMatch()));
				if(content.trim().length()>0)
					verbs.add(content);
			}
		} catch (ParseException e) {
			logger.error(e);
		}
		return verbs;
	}

	/**
	 * @param t the tree to which extract the content
	 * @return the string with the content of the tree
	 */
	public static String nodeContent(Tree t, Tree pv) {

		if(t.isLeaf())
			return t.value();

		StringBuffer buff = new StringBuffer();
		for(Tree tt : t.children()) {
			if(!t.value().equals("DT") &&
					!t.value().equals("SYM") &&
					!t.value().startsWith("PRP")) {
				buff.append(nodeContent(tt, t));
				buff.append(" ");
			}
		}
		String clean = buff.toString().replace("\\s+", " ").trim();
		return clean;
	}


	/**
	 * @param t the grammar tree
	 * @return a list with all concepts identified in a tree
	 */
	public static List<String> extractNouns(Tree t) {
		List<String> concepts = new ArrayList<String>();

		if(t == null)
			return concepts;

		TregexPattern pattern;
		try {
			// This pattern means a noun phrase that is not dominating another
			// noun phrase, and is also not dominating a verbal phrase
			pattern = TregexPattern.compile("@NP !<< NP & !<<@VP");
			TregexMatcher matcher = pattern.matcher(t);
			while (matcher.findNextMatchingNode()) {
				String content = cleanNodeContent(nodeContent(matcher.getMatch(), null));
				if(content.trim().length() > 0)
					concepts.add(content);
			}
		} catch (ParseException e) {
			logger.error(e);
		}
		return concepts;
	}

	/**
	 * Added to remove punctuation from the strings extracted from the tree
	 * @param content any string containing punctuation at beginning or end
	 * @return the string without trailing or tailing punctuation
	 */
	public static String cleanNodeContent(String content) {
		String cleanContent = content.trim();
		cleanContent = cleanContent.replaceFirst("^\\W+", "");
		cleanContent = cleanContent.replaceFirst("\\W+$", "");
		cleanContent = cleanContent.replaceAll("\\s+", " ");
		return cleanContent.trim();
	}
	/**
	 * @param t the tree to which extract the content
	 * @return the string with the content of the tree
	 */
	public static String nodeContent(Tree t) {

		if(t.isLeaf())
			return t.value();

		StringBuffer buff = new StringBuffer();
		for(Tree tt : t.children()) {
			buff.append(nodeContent(tt));
			buff.append(" ");
		}
		String clean = buff.toString().replace("\\s+", " ").trim();
		return clean;
	}

	/**
	 * Calculates a Penn grammatical tree from its string representation
	 * @param pennTreeString the string
	 * @return the grammar tree
	 * @throws Exception
	 */
	public static Tree getTreeFromString(String passageId, String pennTreeString) {
		double time = System.nanoTime();
		time = System.nanoTime() - time;
		Tree t = null;
		if(pennTreeCache.containsKey(passageId)) {
			t = pennTreeCache.get(passageId);
		} else {
			LabeledScoredTreeFactory tf = new LabeledScoredTreeFactory();
			PennTreeReader reader = new PennTreeReader(new StringReader(pennTreeString), tf);
			try {
				t = reader.readTree();
				pennTreeCache.put(passageId, t);
			} catch (IOException e) {
				logger.error("Error parsing penntree string length " + pennTreeString.length());
				e.printStackTrace();
				return null;
			}
		}
		logger.debug("PennTree calculated in " + time * 10E-6 + " milliseconds.");
		return t;
	}

	/**
	 * Calculates the typed dependencies from a grammatical tree
	 * @param tree the grammatical tree
	 */
	public static List<String> calculateTypedDependencies(Tree tree) {
		double time = System.nanoTime();
		List<String> output = new ArrayList<String>();
		GrammaticalStructure gs = null;
		try {
			gs = getGrammaticalStructureFactory().newGrammaticalStructure(tree);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}

		Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();

		// Get the POS tag from each word
		Hashtable<String, String> posInfo = new Hashtable<String, String>();
		for(Tree t: tree.getLeaves()) {
			Tree pt = null;
			for(Tree tt : tree.dominationPath(t)) {
				if(tt.isLeaf()) {
					posInfo.put(tt.nodeString(), pt.nodeString());
				}
				pt = tt;
			}
		}

		for (Object obj : tdl.toArray()) {
			TypedDependency dep = (TypedDependency) obj;

			String wordGov = dep.gov().nodeString().split("-")[0];
			String wordDep = dep.dep().nodeString().split("-")[0];
			String posGov = posInfo.get(wordGov);
			String posDep = posInfo.get(wordDep);
			String dependencyString = dep.reln().toString() + "(" + dep.gov().pennString().trim() + "-" + posGov + ", " + dep.dep().pennString().trim() + "-" + posDep + ")";
			output.add(dependencyString);
		}

		time = System.nanoTime() - time;
		logger.debug("Typed dependencies obtained in " + time * 10E-6 + " milliseconds");
		return output;
	}
	
	public static String removeDeterminersFromNounPhrase(String phrase) throws IOException {
		Tree tree = getPennTree(phrase);
		return nodeContent(tree,null);
	}

	public static String getPennString(Tree tree) {
		String pennTreeString = "";

		double time = System.nanoTime();
		TreePrint print = new TreePrint("penn");
		StringWriter stw = new StringWriter();
		print.printTree(tree, new PrintWriter(stw));
		pennTreeString = stw.toString();

		time = (System.nanoTime() - time) * 10E-9;
		logger.debug("Sentence parsed in " + time + " seconds");

		return pennTreeString;		
	}
	
	public static Tree getPennTree(String text) throws IOException {
		
//		text = text.trim();
//		text = text.replaceAll("\"", "");
//		if(text.endsWith("."))
//			text = text.substring(0, text.length()-1);
//		String[] sentenceWords = text.split("\\s+");
//		Tree tree = PennTreeAnnotator.getParser().apply(Arrays
//				.asList(sentenceWords));
		Tree tree = PennTreeAnnotator.getParser().apply(text);
		return tree;
	}
	
	public static String getPennTagMinimalPhrase(Tree t) {
		if(t.isLeaf())
			return "LEAF";
		
		if(t.isPrePreTerminal())
			return t.value();
		
		return getPennTagMinimalPhrase(t.children()[0]);
	}
	
	public static String getPennTagFirstBranch(Tree orig, Tree t, Tree pt) {
		if(t.isLeaf())
			return "NOBRANCH";
		
		List<Tree> trees = t.siblings(orig); 
		if(trees != null && trees.size() > 0 && pt != null)
			return pt.value();
		
		return getPennTagFirstBranch(orig, t.getChild(0), t);
	}
}
