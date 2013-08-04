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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import tml.Configuration;
import tml.annotators.Annotator;
import tml.corpus.CorpusParameters;
import tml.corpus.SearchResultsCorpus;
import tml.corpus.TextDocument;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.storage.Repository;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.operations.Operation;


/**
 * Command line interface for TML, this is probably the easiest way to access it.
 * 
 * Intended use should be:
 * usage: tml <options> [parameters] operation
 *  -I                             Insert documents into repository.
 *     --iannotators <arg>         List of annotators to use when inserting
 *                                 the documents. (e.g. PennTreeAnnotator).
 *     --iclean                    Empties the repository before inserting
 *                                 new ones.
 *     --idocs <folder>            The folder that contains the documens to
 *                                 insert.
 *     --imaxdocs <number>         Maximum number of documents to index or
 *                                 use in an operation.
 *  -O                             Performs an operation on a corpus.
 *     --oalldocs <type>           Use all documents in repository as single
 *                                 document corpora, it can be sentence or paragraph based. (e.g. sentence).
 *     --obk <query>               Lucene query that defines a background
 *                                 knowledge on which the corpus will be projected. (e.g. "type:sentences AND
 *                                 reference:Document*").
 *     --obkpar <parameter file>   Properties file with the background
 *                                 knowledge corpus parameters, if not set it will use the same as the
 *                                 corpus.
 *     --ocorpus <query>           Lucene query that defines the corpus to
 *                                 operate with. (e.g. "type:sentence AND reference:Document01").
 *     --ocpar <parameter file>    Properties file with the corpus parameters
 *                                 (optional).
 *     --odim <list>               Name of the Dimensionality Reduction
 *                                 criteria. (e.g. VARPCT,NUM,PCT,NO).
 *     --odimth <list>             Threshold for the dim options. (e.g.
 *                                 0,1,2).
 *     --olanczos                  Use Lanczos for SVD decomposition.
 *     --operations <list>         The list of operations you want to execute
 *                                 on the corpus. (e.g. PassageDistances,PassageSimilarity .
 *     --oresults <folder>         Folder where to store the results. (e.g.
 *                                 results/run01/).
 *     --otsel <name>              Name of the Term selection criteria
 *                                 (TF,AVG_TF,DF).
 *     --otselth <number>          Threshold for the tsel criteria option.
 *     --otwg <list>               Name of the Global Weight to apply. (e.g.
 *                                 None,Normal,GfIdf,Idf,Entropy).
 *     --otwl <list>               Name of the Local Weight to apply.
 *                                 (e.g.Binary,TF,TFn,LOGTF).
 *  -repo <folder>                 Full path of the repository folder, where
 *                                 TML will retrieve (or insert) documents. (e.g. /home/user/lucene).
 * 
 * @author Jorge Villalon
 *
 */
public class TmlCommandLine {

	private static Logger logger = Logger.getLogger(TmlCommandLine.class);
	private static Repository repository = null;
	private static CommandLine line = null;
	private static Options options = null;
	private static String repositoryFolder = null;


	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		long time = System.nanoTime();
		
		options = new Options();
		
		// Repository
		options.addOption(OptionBuilder
				.withDescription("Full path of the repository folder, where TML will retrieve (or insert) documents. (e.g. /home/user/lucene).")
				.hasArg()
				.withArgName("folder")
				.isRequired()
				.create("repo"));

		// Verbosity
		options.addOption(OptionBuilder
				.withDescription("Verbose output in the console (it goes verbose to the log file).")
				.hasArg(false)
				.isRequired(false)
				.create("v"));

		// Operation on corpus
		options.addOption(OptionBuilder
				.hasArg(false)
				.withDescription("Performs an operation on a corpus.")
				.isRequired(false)
				.create("O"));

		// The list of operations
		options.addOption(OptionBuilder
				.withDescription("The list of operations you want to execute on the corpus. (e.g. PassageDistances,PassageSimilarity .")
				.hasArgs()
				.withValueSeparator(',')
				.withArgName("list")
				.isRequired(false)
				.withLongOpt("operations")
				.create());

		// The file to store the results
		options.addOption(OptionBuilder
				.withDescription("Folder where to store the results. (e.g. results/run01/).")
				.hasArg()
				.withArgName("folder")
				.isRequired(false)
				.withLongOpt("oresults")
				.create());

		// The corpus on which operate
		options.addOption(OptionBuilder
				.withDescription("Lucene query that defines the corpus to operate with. (e.g. \"type:sentence AND reference:Document01\").")
				.hasArg()
				.withArgName("query")
				.isRequired(false)
				.withLongOpt("ocorpus")
				.create());

		// The corpus on which operate
		options.addOption(OptionBuilder
				.withDescription("Use all documents in repository as single document corpora, it can be sentence or paragraph based. (e.g. sentence).")
				.hasArgs()
				.withArgName("type")
				.isRequired(false)
				.withLongOpt("oalldocs")
				.create());

		// The properties file for the corpus
		options.addOption(OptionBuilder
				.withDescription("Properties file with the corpus parameters (optional).")
				.hasArg()
				.withArgName("parameter file")
				.isRequired(false)
				.withLongOpt("ocpar")
				.create());

		// Background knowledge corpus
		options.addOption(OptionBuilder
				.withDescription("Lucene query that defines a background knowledge on which the corpus will be projected. (e.g. \"type:sentences AND reference:Document*\").")
				.hasArg()
				.withArgName("query")
				.isRequired(false)
				.withLongOpt("obk")
				.create());

		// Background knowledge parameters
		options.addOption(OptionBuilder
				.withDescription("Properties file with the background knowledge corpus parameters, if not set it will use the same as the corpus.")
				.hasArg()
				.withArgName("parameter file")
				.isRequired(false)
				.withLongOpt("obkpar")
				.create());

		// Term selection
		String criteria = "";
		for(TermSelection tsel : TermSelection.values()) {
			criteria += "," + tsel.name();
		}
		criteria = criteria.substring(1);
		options.addOption(OptionBuilder
				.hasArgs()
				.withArgName("name")
				.withDescription("Name of the Term selection criteria (" + criteria + ").")
				.isRequired(false)
				.withValueSeparator(',')
				.withLongOpt("otsel")
				.create());

		//	Term selection threshold
		options.addOption(OptionBuilder
				.hasArgs()
				.withArgName("number")
				.withDescription("Threshold for the tsel criteria option.")
				.withType(Integer.TYPE)
				.isRequired(false)
				.withValueSeparator(',')
				.withLongOpt("otselth")
				.create());

		//	Dimensionality reduction
		criteria = "";
		for(DimensionalityReduction dim : DimensionalityReduction.values()) {
			criteria += "," + dim.name();
		}
		criteria = criteria.substring(1);
		options.addOption(OptionBuilder
				.hasArgs()
				.withArgName("list")
				.withDescription("Name of the Dimensionality Reduction criteria. (e.g. " + criteria + ").")
				.isRequired(false)
				.withValueSeparator(',')
				.withLongOpt("odim")
				.create());

		//	Dimensionality reduction threshold
		options.addOption(OptionBuilder
				.hasArgs()
				.withArgName("list")
				.withDescription("Threshold for the dim options. (e.g. 0,1,2).")
				.isRequired(false)
				.withValueSeparator(',')
				.withLongOpt("odimth")
				.create());

		//	Local weight
		criteria = "";
		for(LocalWeight weight : LocalWeight.values()) {
			criteria += "," + weight.name();
		}
		criteria = criteria.substring(1);
		options.addOption(OptionBuilder
				.hasArgs()
				.withArgName("list")
				.withDescription("Name of the Local Weight to apply. (e.g." + criteria + ").")
				.isRequired(false)
				.withValueSeparator(',')
				.withLongOpt("otwl")
				.create());

		//	Global weight
		criteria = "";
		for(GlobalWeight weight : GlobalWeight.values()) {
			criteria += "," + weight.name();
		}
		criteria = criteria.substring(1);
		options.addOption(OptionBuilder
				.hasArgs()
				.withArgName("list")
				.withDescription("Name of the Global Weight to apply. (e.g. " + criteria + ").")
				.isRequired(false)
				.withValueSeparator(',')
				.withLongOpt("otwg")
				.create());

		//	Use Lanczos
		options.addOption(OptionBuilder
				.hasArg(false)
				.withDescription("Use Lanczos for SVD decomposition.")
				.isRequired(false)
				.withLongOpt("olanczos")
				.create());

		// Inserting documents in repository
		options.addOption(OptionBuilder
				.hasArg(false)
				.withDescription("Insert documents into repository.")
				.isRequired(false)
				.create("I"));

		// Max documents to insert
		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("number")
				.withDescription("Maximum number of documents to index or use in an operation.")
				.withType(Integer.TYPE)
				.isRequired(false)
				.withLongOpt("imaxdocs")
				.create());

		// Clean repository
		options.addOption(OptionBuilder
				.hasArg(false)
				.withDescription("Empties the repository before inserting new ones.")
				.isRequired(false)
				.withLongOpt("iclean")
				.create());

		// Use annotator
		options.addOption(OptionBuilder
				.hasArgs()
				.withDescription("List of annotators to use when inserting the documents. (e.g. PennTreeAnnotator).")
				.isRequired(false)
				.withValueSeparator(',')
				.withLongOpt("iannotators")
				.create());

		// Documents folder
		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("folder")
				.withDescription("The folder that contains the documens to insert.")
				.isRequired(false)
				.withLongOpt("idocs")
				.create());

		// Initializing the line parser
		CommandLineParser parser = new PosixParser();
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			printHelp(options);
			return;
		}

		// Validate that either inserting or an operation are given
		if(!line.hasOption("I") && !line.hasOption("O")) {
			System.out.println("One of the options -I or -O must be present.");
			printHelp(options);
			return;
		}

		repositoryFolder = line.getOptionValue("repo");

		try {
			if(line.hasOption("I")) {
				indexing();
			} else if(line.hasOption("O")){
				operation();
			}
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelp(options);
			return;			
		}

		System.out.println("TML finished successfully in " + (System.nanoTime() - time)*10E-9 + " seconds.");
		return;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("tml <options> [parameters] operation", options);		
	}

	@SuppressWarnings("rawtypes")
	private static void indexing() throws ParseException {

		if(!line.hasOption("idocs")) {
			throw new ParseException("Indexing requires the idocs option.");
		}

		if(!startTML()) {
			throw new ParseException("Fatal error initializing TML.");
		}

		if(line.hasOption("iclean")) {
			try {
				Repository.cleanStorage(repositoryFolder);
			} catch (Exception e) {
				logger.error(e);
				return;
			}
		}

		try {
			repository = new Repository(repositoryFolder);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return;
		}

		// Remove all annotators because in command line mode they must be added one by one
		for(int i=repository.getAnnotators().size()-1; i>=0; i--) {
			Annotator annotator = repository.getAnnotators().get(i);
			repository.removeAnnotator(annotator);
		}
		
		String[] annotatorsList = line.getOptionValues("iannotators");
		if(annotatorsList != null && annotatorsList.length > 0) {
			for(String annotatorName : annotatorsList) {
			Class classDefinition = null;
			Annotator annotator = null;
			try {
				classDefinition = Class.forName("tml.annotators." + annotatorName);
				annotator = (Annotator) classDefinition.newInstance();
			} catch (Exception e) {
				logger.error("The annotator wasn't found! " + annotatorName);
				logger.error(e);
				continue;
			}

			repository.addAnnotator(annotator);
			}
		}

		String documentsFolder = line.getOptionValue("idocs");
		try {
			if(line.hasOption("imaxdocs")) {
				int maxDocs = Integer.parseInt(line.getOptionValue("imaxdocs"));
				repository.addDocumentsInFolder(documentsFolder, maxDocs);
			} else {
				repository.addDocumentsInFolder(documentsFolder);
			}
		} catch (IOException e) {
			logger.error(e);
			return;
		}
	}

	@SuppressWarnings("rawtypes")
	private static void operation() throws ParseException {

		if(line.hasOption("ocorpus")
				&& (line.getOptionValue("ocorpus") == null || line.getOptionValue("ocorpus").trim().length() == 0)) {
			throw new ParseException("Invalid ocorpus option argument value.");
		}
		
		String allDocsCorpusType = line.getOptionValue("oalldocs");
		if(line.hasOption("oalldocs")
				&& !allDocsCorpusType.equals("sentence") && !allDocsCorpusType.equals("paragraph")) {
			throw new ParseException("Invalid oalldocs option argument value.");			
		}

		String[] operations = line.getOptionValues("operations");
		if(operations == null || operations.length == 0) {
			throw new ParseException("You must specify at least one operation!");
		}

		if(!startTML()) {
			throw new ParseException("Fatal error initializing TML.");
		}

		try {
			repository = new Repository(repositoryFolder);
		} catch (Exception e) {
			logger.error(e);
			return;
		}
		
		String[] corpusQueries = null;
		if(line.hasOption("ocorpus")) {
			corpusQueries = new String[1];
			corpusQueries[0] = line.getOptionValue("ocorpus");
		} else if(line.hasOption("oalldocs")) {
			List<TextDocument> docs = null;
			try {
				docs = repository.getAllTextDocuments();
			} catch (Exception e) {
				logger.fatal("Couldn't get list of documents from repository.");
				throw new ParseException(e.getMessage());
			}
			corpusQueries = new String[docs.size()];
			for(int i=0;i<docs.size();i++) {
				TextDocument doc = docs.get(i);
				String referenceId = null;
				if(line.getOptionValue("oalldocs").equals("sentence"))
					referenceId = "p*d" + doc.getExternalId();
				else
					referenceId = doc.getExternalId();
				corpusQueries[i] = "type:" + line.getOptionValue("oalldocs")
				+ " AND reference:" + referenceId;
			}
		}
		
		String corpusLine = "NoCorpus";
		if(line.hasOption("ocorpus"))
			corpusLine = line.getOptionValue("ocorpus").replaceAll("\\W", "");
		else if(line.hasOption("oalldocs"))
			corpusLine = "AllDocuments";
		String resultsFilename = 
			repository.getIndexPath().substring(1).replaceAll("[/\\\\]", "_") + "." +
			corpusLine + "." 
			+ (new SimpleDateFormat("yyyy-MM-dd-hh-mm")).format(new Date()) + ".txt";		


		// Initialize arrays and set default parameters
		DimensionalityReduction[] dims = new DimensionalityReduction[1];
		double[] dimths = new double[1];
		boolean lanczos = false;
		TermSelection[] tsels = new TermSelection[1];
		double[] tselths = new double[1];
		LocalWeight[] twlocals = new LocalWeight[1];
		GlobalWeight[] twglobals = new GlobalWeight[1];

		CorpusParameters parameters = new CorpusParameters();
		dims[0] = parameters.getDimensionalityReduction();
		dimths[0] = parameters.getDimensionalityReductionThreshold();
		lanczos = parameters.isLanczosSVD();
		tsels[0] = parameters.getTermSelectionCriterion();
		tselths[0] = parameters.getTermSelectionThreshold();
		twlocals[0] = parameters.getTermWeightLocal();
		twglobals[0] = parameters.getTermWeightGlobal();

		// If the ocpar option is given, load the parameters file and
		// override the default parameters
		if(line.hasOption("ocpar")) {
			parameters.loadFromFile(new File(line.getOptionValue("ocpar")));
			dims[0] = parameters.getDimensionalityReduction();
			dimths[0] = parameters.getDimensionalityReductionThreshold();
			lanczos = parameters.isLanczosSVD();
			tsels[0] = parameters.getTermSelectionCriterion();
			tselths[0] = parameters.getTermSelectionThreshold();
			twlocals[0] = parameters.getTermWeightLocal();
			twglobals[0] = parameters.getTermWeightGlobal();
		} else {
			// Check for every possible parameter
			if(line.hasOption("odim")) {
				dims = new DimensionalityReduction[line.getOptionValues("odim").length];
				for(int i=0;i<dims.length;i++)
					dims[i] = DimensionalityReduction.valueOf(line.getOptionValues("odim")[i]);
			}
			if(line.hasOption("odimth")) {
				dimths = new double[line.getOptionValues("odimth").length];
				for(int i=0;i<dimths.length;i++)
					dimths[i] = Double.parseDouble(line.getOptionValues("odimth")[i]);
			}
			if(line.hasOption("olanczos"))
				lanczos = true;
			else 
				lanczos = false;
			if(line.hasOption("otsel")) {
				tsels = new TermSelection[line.getOptionValues("otsel").length];
				for(int i=0;i<tsels.length;i++)
					tsels[i] = TermSelection.valueOf(line.getOptionValues("otsel")[i]);
			}
			if(line.hasOption("otselth")) {
				tselths = new double[line.getOptionValues("otselth").length];
				for(int i=0;i<tselths.length;i++)
					tselths[i] = Double.parseDouble(line.getOptionValues("otselth")[i]);
			}
			if(line.hasOption("otwl")) {
				twlocals = new LocalWeight[line.getOptionValues("otwl").length];
				for(int i=0;i<twlocals.length;i++)
					twlocals[i] = LocalWeight.valueOf(line.getOptionValues("otwl")[i]);
			}
			if(line.hasOption("otwg")) {
				twglobals = new GlobalWeight[line.getOptionValues("otwg").length];
				for(int i=0;i<twglobals.length;i++)
					twglobals[i] = GlobalWeight.valueOf(line.getOptionValues("otwg")[i]);
			}
		}

		String resultsFolder = line.getOptionValue("oresults");
		FileWriter writer = null;
		if(resultsFolder != null) {
			File resultsFold = new File(resultsFolder);
			if(resultsFold.exists() && resultsFold.isDirectory()) {
				try {
					File results = new File(resultsFolder + "/" + resultsFilename);
					writer = new FileWriter(results);
				} catch (IOException e) {
					logger.error(e);
					writer = null;
				}
			}
		}

		// Create the whole combination of parameters
		for(TermSelection tsel : tsels)
		for(double tselth : tselths)
		for(LocalWeight lw : twlocals)
		for(GlobalWeight gw : twglobals) {
			CorpusParameters p = new CorpusParameters();
			p.setTermSelectionCriterion(tsel);
			p.setLanczosSVD(lanczos);
			p.setTermSelectionCriterion(tsel);
			p.setTermSelectionThreshold(tselth);
			p.setTermWeightLocal(lw);
			p.setTermWeightGlobal(gw);

			logger.debug("Parameters to execute: " + p.toString());
			
			SearchResultsCorpus backgroundKnowledgeCorpus = null;
			
			// If we have background knowledge, load it
			if(line.hasOption("obk")) {
				backgroundKnowledgeCorpus = new SearchResultsCorpus(line.getOptionValue("obk"));
				if(line.hasOption("obkpar")) {
					CorpusParameters bkParameters = new CorpusParameters();
					bkParameters.loadFromFile(new File(line.getOptionValue("obkpar")));
					backgroundKnowledgeCorpus.setParameters(bkParameters);
				}
				try {
					backgroundKnowledgeCorpus.load(repository);
				} catch (Exception e) {
					logger.error("Couldn't load background knowledge corpus.");
					logger.error(e);
					e.printStackTrace();
					continue;
				}
			}
			
			// Create the corpus with the query
			for(String corpusQuery : corpusQueries) {
			SearchResultsCorpus corpus = new SearchResultsCorpus(corpusQuery);
			
			// Loading the corpus
			try {
				corpus.setParameters(p);
				corpus.load(repository);
			} catch (Exception e) {
				logger.error("Couldn't load corpus. " + corpus.getLuceneQuery());
				logger.error(e);
				continue;
			}

			for(DimensionalityReduction dred : dims)
				for(double dimth : dimths) {
					p.setDimensionalityReduction(dred);
					p.setDimensionalityReductionThreshold(dimth);
					try {
						corpus.getParameters().setDimensionalityReduction(dred);
						corpus.getParameters().setDimensionalityReductionThreshold(dimth);
						if(backgroundKnowledgeCorpus == null)
						corpus.getSemanticSpace().calculate();
					} catch (Exception e) {
						logger.error("Couldn't calculate corpus' semantic space");
						logger.error(e);
						e.printStackTrace();
						continue;
					}			
					for(String operation : operations) {
						Class classDefinition = null;
						Operation op = null;
						try {
							classDefinition = Class.forName("tml.vectorspace.operations." + operation);
							op = (Operation) classDefinition.newInstance();
						} catch (Exception e) {
							logger.error("The operation wasn't found");
							e.printStackTrace();
							logger.error(e);
							continue;
						}

						op.setCorpus(corpus);
						if(backgroundKnowledgeCorpus != null)
							op.setBackgroundKnowledgeCorpus(backgroundKnowledgeCorpus);
						try {
							op.start();
						} catch (Exception e) {
							logger.error("Error while performing the operation");
							e.printStackTrace();
							logger.error(e);
							continue;
						}

						String backgroundline = "None";
						String parametersline = corpus.getParameters().toString();
						if(backgroundKnowledgeCorpus != null) {
							backgroundline = backgroundKnowledgeCorpus.getLuceneQuery()
								+ " [" + backgroundKnowledgeCorpus.getSemanticSpace().getDimensionsKept() + "]";
							parametersline = backgroundKnowledgeCorpus.getParameters().toString();
						} else {
							backgroundline += " [" + corpus.getSemanticSpace().getDimensionsKept() + "]";							
						}
						String corpusline = 
							"Corpus:" +
							corpus.getLuceneQuery() + "\n" +
							"Operation:" +
							op.getClass().getName() + "\n" +
							"Background:" +
							backgroundline + "\n" +
							"Parameters:" +
							parametersline + "\n";

						if(writer != null) {
							try {
								writer.append(corpusline);
								writer.append(op.getResultsCSVString());
							} catch (IOException e) {
								logger.error("Error writing file " + corpusline);
								logger.error(e);
							}
						} else {
							System.out.println(corpusline);
							System.out.println(op.getResultsCSVString());
						}
			}}}
		}
		
		if(writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	private static boolean startTML() {
		try {
			if(line.hasOption("v")) {
				PropertyConfigurator.configure(Configuration.getTmlProperties(true));
			} else {
				PropertyConfigurator.configure(Configuration.getTmlProperties());
			}
		} catch (IOException e1) {
			System.out.println("TML jar file is corrupt, please contact the author.");
			return false;
		}		
		return true;
	}
}
