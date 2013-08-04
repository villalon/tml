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
package tml.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import tml.Configuration;
import tml.annotators.Annotator;
import tml.corpus.CorpusParameters;
import tml.corpus.RepositoryCorpus;
import tml.corpus.TextDocument;
import tml.sql.DbConnection;
import tml.storage.importers.AbstractImporter;
import tml.storage.importers.Importer;
import tml.storage.importers.TextImporter;
import tml.vectorspace.NoDocumentsInCorpusException;

/**
 * This class represents a documents repository. Documents can be inserted,
 * deleted and searched from a Repository. All documents that were successfully
 * inserted in a repository can then later be used to create a {@link Corpus}
 * and perform operations on them.
 * </p>
 * <p>
 * At the heart of a repository lies a {@link TextDocument}, that represents a
 * text document and is accessible using any id of your choice (e.g. from a
 * database, or from the filesystem). The content of a new documents is expected
 * to be just plain text. Importers from different formats will be provided in
 * time, for the moment we have only a Wiki cleaner.
 * </p>
 * <p>
 * All the documents, once inserted in the Repository can then be searched using
 * the searchTextDocuments method. Queries are made using the syntax from
 * Apache's Lucene.
 * </p>
 * <p>
 * <em>Code examples</em>
 * </p>
 * <p>
 * Initialising a {@link Repository}:
 * </p>
 * 
 * <pre>
 * Repository repository = new Repository(&quot;path/to/repository/folder&quot;);
 * </pre>
 * <p>
 * Obtaining all the documents in a Repository
 * </p>
 * 
 * <pre>
 * ...
 * List&lt;TextDocument&gt; documents = repository.getAllTextDocuments();
 * for(TextDocument doc : documents) {
 *   System.out.println(&quot;Document:&quot; + doc.getTitle());
 * }
 * ...
 * </pre>
 * <p>
 * Inserting a document
 * </p>
 * 
 * <pre>
 * String content = &quot;The content of my document&quot;;
 * String title = &quot;A title&quot;;
 * String url = &quot;http://www/mydoc.txt&quot;;
 * String id = &quot;TheIdOfMyDoc&quot;;
 * repository.addDocument(id, content, title, url);
 * </pre>
 * <p>
 * Obtaining a document from the repository
 * </p>
 * 
 * <pre>
 * String id = &quot;TheIdOfMyDoc&quot;;
 * TextDocument doc = repository.getTextDocument(id);
 * </pre>
 * <p>
 * Removing a document from the repository
 * </p>
 * 
 * <pre>
 * TextDocument doc = repository.getTextDocument(&quot;someId&quot;);
 * repository.deleteDocument(doc);
 * </pre>
 * <p>
 * Searching for documents containing "foo"
 * </p>
 * 
 * <pre>
 * String query = &quot;foo&quot;;
 * List&lt;TextDocument&gt; documents = repository.searchTextDocuments(query);
 * for (TextDocument doc : documents) {
 * 	System.out.println(&quot;Document found:&quot; + doc.getTitle());
 * }
 * </pre>
 * 
 * @see TextDocument
 * @see Corpus
 * @author Jorge Villalon
 * 
 */
public class Repository {

	/**
	 * Cleans an id (typically a file name) to suits the syntax of Lucene
	 *
	 * @param id
	 *            the external id of a document
	 * @return the id clean of special characters that Lucene uses
	 */
	public static String cleanIdForLucene(String id) {
		String cleanId = id.replace(" ", "");
		cleanId = cleanId.replace("_", "");
		cleanId = cleanId.replace("\\.", "");
		return cleanId;
	}

	/**
	 * Deletes all the files of the {@link Repository}.
	 *
	 * @param indexPath
	 *            The path to the folder where the LuceneIndex files are stored
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 * @throws SQLException 
	 *
	 */
	public static void cleanStorage(String indexPath)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, SQLException {

		DbConnection conn = new DbConnection();
		conn.cleanMetaDataStorage();

		// Opening an IndexWriter with true to create a new empty one
		IndexWriter writer = new IndexWriter(
				SimpleFSDirectory.open(new File(indexPath)),
				new StandardAnalyzer(Version.LUCENE_29),
				true,
				IndexWriter.MaxFieldLength.UNLIMITED);
		writer.close(true);
		writer = null;
	}

	/**
	 * This method is necessary due to problems on processing UTF-8 encoded text that comes from
	 * a paste from word. Usually quotations and double quotations come with weird characters
	 * that do not correspond to those of quotations. That makes it impossible to detect
	 * for the parsers.
	 *
	 * @param word
	 * @return
	 */
	public static String cleanWord(String word) {
		word = word.replace('\u0060', '\'');
		word = word.replace('\u2018', '\'');
		word = word.replace('\u2019', '\'');
		word = word.replace('\u201A', '\'');
		word = word.replace('\u201B', '\'');
		word = word.replace('\u2032', '\'');
		word = word.replace('\u2035', '\'');

		word = word.replace('\u201C', '\"');
		word = word.replace('\u201D', '\"');
		word = word.replace('\u201E', '\"');
		word = word.replace('\u201F', '\"');
		word = word.replace('\u2033', '\"');
		word = word.replace('\u2036', '\"');

		word = word.replace('\u2010', '-');
		word = word.replace('\u2012', '-');
		word = word.replace('\u2013', '-');
		word = word.replace('\u2014', '-');
		word = word.replace('\u2015', '-');
		word = word.replaceAll("\r\n", "");
		word = word.replace('\r', ' ');
		word = word.replace('\n', ' ');
		word = word.replaceAll("\uFEFF", "");
		word = word.trim();

		return word;
	}

	/**
	 * Obtains the content of a text file. Basically it uses readline and then
	 * writes only a \n for newlines so it removes any \r to make further
	 * process easier.
	 * 
	 * @param file
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String getFileContent(File file, String charset) throws IOException {
		StringBuffer buffer = new StringBuffer();


		String line = null;

		// Remove special characters Unicode!
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(file), 
						charset));
		
		while ((line = reader.readLine()) != null) {
			line = cleanWord(line);
			buffer.append(line);
			buffer.append('\n');
		}
		
		reader.close();
		return buffer.toString();
	}

	private IndexWriter luceneIndexWriter = null;
	private IndexReader luceneIndexReader = null;
	// General attributes
	/** The logger for log4j */
	private static Logger logger = Logger.getLogger(Repository.class);
	/** Timer for indexing */
	private Timer indexerTimer;
	/** Timer for annotations */
	private Timer annotatorTimer;
	/** Timer for cleanup */
	private Timer cleanupTimer;
	/** The language for the documents in the repository */
	private Locale locale;
	/** The character encoding used to read files from the fileystem */
	private String encoding = "UTF-8";
	/** The parser for the content before inserting into the index */
	private Importer defaultImporter = null;
	// Lucene specific attributes
	/** The standard Lucene analyser for this repository */
	private Analyzer analyzer;
	/** The folder where the repository is kept */
	private String indexPath;
	/** Path to the storage of calculated SVDs */
	private String svdStoragePath;
	private String tmpPath;
	public String getTmpPath() {
		return tmpPath;
	}
	private String processedPath;
	public String getProcessedPath() {
		return processedPath;
	}
	/** The stopwords */
	private String[] stopwords;
	/** The field that contains the content of a document */
	private String luceneContentField = "contents";
	/** The field that contains the title of a document */
	private String luceneTitleField = "title";
	/** The field that contains the url of a document */
	private String luceneUrlField = "url";
	/** The field that contains the external ID of a document */
	private String luceneExternalIdField = "externalid";
	/** The field that contains the ID of the parent of a document */
	private String luceneParentField = "reference";
	/** The field that contains the ID of the parent of a document */
	private String luceneParentDocumentField = "parent";
	/** The path to the execution folder */
	private String execPath = "";

	public String getExecPath() {
		return execPath;
	}

	public void setExecPath(String execPath) {
		this.execPath = execPath;
	}
	private DbConnection dbConnection = null;

	public DbConnection getDbConnection() {
		return dbConnection;
	}

	/**
	 * @return the luceneParentDocumentField
	 */
	public String getLuceneParentDocumentField() {
		return luceneParentDocumentField;
	}
	/** The field that contains the PennTree bank parse */
	private String lucenePenntreeField = "penntree";
	/** The field that contains the type of the passage */
	private String luceneTypeField = "type";
	/** The maximum number of documents to index every time the indexing is called */
	private int maxDocumentsToIndex = -1;
	// Metadata annotations specific attributes
	/** The list of annotators that will be used on indexing */
	private List<Annotator> annotators = null;
	/** */
	private EventListenerList listeners = null;

	public Repository() throws IOException, SQLException {
		this(Configuration.getTmlFolder() + "/lucene");
	}

	/**
	 * Creates a new instance of the class {@link Repository} using a Standard
	 * Analyzer without stop words removal.
	 *
	 * @param luceneIndexPath
	 *            an absolute path to the folder that stores the Lucene Index
	 * @throws IOException
	 * @throws SQLException 
	 */
	public Repository(String luceneIndexPath) throws IOException, SQLException {
		this(luceneIndexPath, new Locale("en"));
	}

	/**
	 * 
	 * @param luceneIndexPath
	 * @param locale
	 * @throws IOException
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes" })
	public Repository(String luceneIndexPath, Locale locale) throws IOException, SQLException {
		assert (luceneIndexPath != null);
		assert (locale != null);

		// Read default properties and initialize log4j
		Configuration.getTmlProperties(true);

		this.indexPath = luceneIndexPath;
		File folder = new File(this.indexPath);
		if (!folder.exists()) {
			String message = "Repository folder doesn't exist ["
					+ this.indexPath + "]";
			logger.error(message);
			throw new IOException(message);
		}

		this.locale = locale;
		this.defaultImporter = new TextImporter();
		this.annotators = new ArrayList<Annotator>();

		logger.info("TML initialization");
		logger.debug("Context Path:\t\t" + Configuration.getContextPath());
		logger.info("Repository path:\t" + this.indexPath);

		try {
			new IndexSearcher(SimpleFSDirectory.open(new File(luceneIndexPath)), true);
			logger.info("Repository:\t\tLucene initialized");
		} catch (Exception e1) {
			logger.warn("Repository:\t\tLucene index corrupt or inexistent, recreating");
			Repository.cleanStorage(luceneIndexPath);
		}

		this.svdStoragePath = Configuration.getTmlFolder() + "/svd";
		File svdFolder = new File(svdStoragePath);
		if(!svdFolder.exists())
			svdFolder.mkdir();
		logger.debug("Cache:\t\t\tSVDs stored in " + this.svdStoragePath);

		this.tmpPath =  Configuration.getTmlFolder() + "/tmp";
		File tmpFolder = new File(this.tmpPath);
		if(!tmpFolder.exists())
			tmpFolder.mkdir();
		logger.debug("Temp:\t\t\tTemporary files in " + this.tmpPath);

		this.processedPath =  Configuration.getTmlFolder() + "/processed";
		File processedFolder = new File(this.processedPath);
		if(!processedFolder.exists())
			processedFolder.mkdir();
		logger.debug("Indexer:\t\tProcessed files in " + this.processedPath);

		File stopWordsFile = new File(Configuration.getTmlFolder() + "/stopwords/stopwords_" + this.locale.getLanguage() + ".txt");

		if (stopWordsFile == null || !stopWordsFile.exists()) {
			InputStream stream = this.getClass().getResourceAsStream(
					"/tml/stopwords_" + this.locale.getLanguage() + ".txt");
			if (stream == null) {
				logger.info("Failed to load stopwords for language "
						+ this.locale.getLanguage()
						+ ", falling to english");
				stream = this.getClass().getResourceAsStream("/tml/stopwords.txt");
			}
			this.stopwords = getStopWordsFromBufferedReader(new BufferedReader(
					new InputStreamReader(stream)));
		} else {
			this.stopwords = getStopWordsFromFile(stopWordsFile);
		}

		logger.debug("Stopwords:\t\tUsing " + this.locale.getDisplayLanguage(Locale.ENGLISH) + " (" + this.stopwords.length + " stopwords)");

		String snowballLang = this.locale.getDisplayLanguage(Locale.ENGLISH);

		this.analyzer = new SnowballAnalyzer(
				Version.LUCENE_29,
				snowballLang, 
				this.stopwords);

		logger.debug("Stemming:\t\tUsing " + this.analyzer.toString() + " " + snowballLang);

		// TODO: Recognize when and how to analyze Korean, Chinese or some other languages
		// this.analyzer = new CJKAnalyzer(this.stopwords);

		// Check DB connectino for metadata
		this.dbConnection = new DbConnection();

		// Loads default annotators
		String annotators = Configuration.getTmlProperties().getProperty(
				"tml.annotators");

		if(annotators != null && annotators.length() > 0) {
			logger.debug("Annotators:\t\tLoading defaults");
			for (String annotatorName : annotators.split(",")) {
				if (annotatorName.trim().length() == 0) {
					continue;
				}

				Class classDefinition = null;
				Annotator annotator = null;
				try {
					classDefinition = Class.forName("tml.annotators." + annotatorName);
					annotator = (Annotator) classDefinition.newInstance();
					this.annotators.add(annotator);
					annotator.init();
				} catch (Exception e) {
					logger.error("Default annotator not found! " + annotatorName);
					logger.error(e);
					continue;
				}
			}
		}

		this.listeners = new EventListenerList();

		try {
			this.openIndexWriter();
			this.closeIndexWriter();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		if(Configuration.getTmlProperties().getProperty("tml.indexer.run").equals("true"))
			initializeIndexerTimer();

		if(Configuration.getTmlProperties().getProperty("tml.annotator.run").equals("true"))
			initializeAnnotatorTimer();

		if(Configuration.getTmlProperties().getProperty("tml.cleanup.run").equals("true"))
			initializeCleanupTimer();

		logger.info("TML initialized");
	}

	public String[][] getAllDocuments() {
		try {
			return this.getDbConnection().getDocuments();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return null;
		}
	}

	/**
	 * Adds an annotator to the repository
	 * @param annotator the annotator
	 */
	public void addAnnotator(Annotator annotator) {
		if (!this.containsAnnotator(annotator)) {
			annotator.init();
			this.annotators.add(annotator);
		} else {
			logger.debug("Annotator " + annotator.getFieldName() + " already loaded!");
		}
	}

	private boolean containsAnnotator(Annotator annotator) {
		for (Annotator existingAnnotator : this.annotators) {
			if (annotator.getFieldName().equals(existingAnnotator.getFieldName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method allows to add a listener so the Repository
	 * can report asynchronously the state of the prcessing
	 * @param l the listener to add
	 */
	public void addRepositoryListener(RepositoryListener l) {
		this.listeners.add(RepositoryListener.class, l);
	}

	/**
	 * Removes a listener that was previously added if exists
	 * @param l the listener to remove
	 */
	public void removeRepositoryListener(RepositoryListener l) {
		this.listeners.remove(RepositoryListener.class, l);
	}

	/**
	 * Fires an event of the Repository
	 * @param evt the event object
	 */
	private void doRepositoryAction(RepositoryEvent evt) {
		RepositoryListener[] list = this.listeners.getListeners(RepositoryListener.class);
		for (RepositoryListener listener : list) {
			listener.repositoryAction(evt);
		}
	}

	/**
	 * Adds a new document to the repository
	 *
	 * @param externalId
	 *            an external id to identify the document
	 * @param content
	 *            the content of the document
	 * @param title
	 *            the title of the document
	 * @param url
	 *            a url to find the document (optional)
	 * @param importer
	 *            an importer (how to decode the content)
	 * @throws IOException
	 * @throws SQLException 
	 */
	public void addDocument(String externalId, String content, String title,
			String url, Importer importer) throws IOException, SQLException {
		logger.debug("Adding document " + title + " with id:" + externalId);

		if (importer != null) {
			content = importer.getCleanContent(content);
		} else if (this.defaultImporter != null) {
			content = this.defaultImporter.getCleanContent(content);
		}

		this.openIndexWriter();

		this.addDocumentToOpenIndex(externalId, content, title, url, importer);

		closeIndexWriter();
	}

	/**
	 * Add all the files in a folder into the Lucene Index.
	 * It can only process .txt files.
	 *
	 * @param folder
	 *            an absolute path to the folder that contains the files
	 * @throws IOException
	 */
	public void addDocumentsInFolder(String folder) throws IOException {
		addDocumentsInFolder(folder, -1);
	}

	/**
	 * Add all the files in a folder into the Lucene Index. Up to a maximum.
	 * It can only process .txt files.
	 *
	 * @param folder
	 *            an absolute path to the folder that contains the files
	 * @param maxDocs
	 *            the maximum number of documents to index
	 * @throws IOException
	 */
	public void addDocumentsInFolder(String folder, int maxDocs) throws IOException {

		logger.debug("Adding text files from " + folder);

		File corpusFile = new File(folder);

		if (!corpusFile.exists() || !corpusFile.isDirectory()) {
			throw new FileNotFoundException(
					"Invalid corpus folder, it doesn't exists! (" + folder + ")");
		}

		// First insert all the filenames in an arraylist to sort them by name
		List<String> files = new ArrayList<String>();
		for (String file : corpusFile.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		})) {
			files.add(file);
		}
		Collections.sort(files);

		if (maxDocs > 0) {
			for (int i = files.size() - 1; i >= maxDocs; i--) {
				files.remove(i);
			}
		}
		// Create the list of files from the list of file names
		List<File> fileList = new ArrayList<File>();
		for (String f : files) {
			fileList.add(new File(folder + "/" + f));
		}
		File[] a = new File[fileList.size()];

		this.addDocumentsInList(fileList.toArray(a));
	}

	/**
	 * Adds all the files in the list to the repository. It will filter by
	 * extension and only load files finishing with ".txt". It also ignores
	 * files starting with a dot ".".
	 *
	 * @param fileList
	 * @throws CorruptIndexException 
	 * @throws IOException
	 */
	public void addDocumentsInList(File[] fileList) throws CorruptIndexException, IOException {

		long time = System.currentTimeMillis();

		this.openIndexWriter();

		logger.debug("Adding files using encoding " + this.encoding);

		int count = 0;
		doRepositoryAction(new RepositoryEvent(this, "addingDocument", 0, fileList.length));
		for (File f : fileList) {
			if (!f.isDirectory() && !f.getName().startsWith(".")) {
				// Calculating the file extension (e.g. .txt or .html)
				String[] pieces = f.getName().split("\\.");
				String extension = pieces[pieces.length - 1];

				// We use the file extension to get an importer
				Importer importer = AbstractImporter.createImporter(extension);
				if (importer == null) {
					logger.info("Don't know how to parse ." + extension
							+ " files, ignoring " + f.getName());
					continue;
				}

				logger.debug("Using importer " + importer.getClass().getName());
				String content = null;
				try {
					content = getFileContent(f, this.encoding);
					String title = f.getName().replace("." + extension, "");
					String url = f.getAbsolutePath();
					String externalid = cleanIdForLucene(title);
					logger.debug("Adding document " + count + ":" + f.getName());
					this.addDocumentToOpenIndex(externalid, content, title, url,
							importer);
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("Failed to load content or adding document to index for file " + f);
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("Fatal error insterting documents in the database");
					throw new IOException(e);
				} finally {
					count++;
					doRepositoryAction(new RepositoryEvent(this, "addingDocument", count, fileList.length));
				}
			} else {
				logger.debug("Ignoring document " + f.getName());
			}
		}

		this.closeIndexWriter();

		time = System.currentTimeMillis() - time;

		doRepositoryAction(new RepositoryEvent(this, "addingDocument", fileList.length, fileList.length));
		logger.info("Successfully added " + count + " documents in " + time
				+ " ms");
	}

	private void addDocumentToOpenIndex(String externalId, String content,
			String title, String url, Importer importer)
					throws IOException, SQLException {

		if (importer != null) {
			content = importer.getCleanContent(content);
		} else if (this.defaultImporter != null) {
			content = this.defaultImporter.getCleanContent(content);
		}

		if (content == null) {
			content = "";
		}

		Document doc = this.createDocument(content,
				"document",
				"null",
				"null",
				externalId,
				title,
				url);
		this.addSegmentsInDocument(content, doc, externalId);
		this.addDocumentToOpenIndex(doc);
	}

	/**
	 * Chops a content in pieces and adds a new document for each piece into the
	 * Lucene Index. The documents will have the type "segment" and will refer
	 * to its parent using the field "parent".
	 *
	 * @param content
	 *            the content of the document to chop
	 * @param document
	 *            the Lucene Document
	 * @param docId
	 *            the id of the document
	 * @throws IOException
	 * @throws SQLException 
	 */
	private void addSegmentsInDocument(String content, Document document,
			String docId) throws IOException, SQLException {

		String title = document.get(this.getLuceneTitleField());
		logger.debug("Adding segments to document " + docId + "[" + title
				+ "]");

		long time = System.currentTimeMillis();

		BufferedReader strReader = new BufferedReader(new StringReader(content));

		String line = null;
		int sentenceNumber = 0;
		int paragraphNumber = 0;
		int ignoredLines = 0;
		int ignoredSentences = 0;
		boolean isBibliography = false;
		logger.debug("Parsing text with " + this.locale);
		while ((line = strReader.readLine()) != null && !isBibliography) {
			BreakIterator iterator = BreakIterator.getSentenceInstance(this.locale);
			iterator.setText(line);
			int start = iterator.first();
			int end = 0;
			List<String> sentencesList = new ArrayList<String>();
			while ((end = iterator.next()) != BreakIterator.DONE) {
				sentencesList.add(line.substring(start, end));
				start = end;
			}
			String documentId = docId;
			if (line.length() >= 2) {
				String lowLine = line.trim().toLowerCase().replaceAll("\\W", "");
				if (isBibliographyTitle(lowLine)) {
					isBibliography = true;
					continue;
				}
				paragraphNumber++;
				String paragraphExtId = "p" + paragraphNumber + "d" + documentId;
				this.addTextPassageToOpenIndex(
						line,
						"paragraph",
						documentId,
						documentId,
						paragraphExtId,
						"Paragraph " + paragraphNumber + " of " + title,
						"N/A");
				int numSentence = 0;
				doRepositoryAction(new RepositoryEvent(this, "addingSentence", 0, sentencesList.size()));
				for (String sentence : sentencesList) {
					String url = "N/A";
					if (sentence.length() >= 2) {
						numSentence++;
						sentenceNumber++;
						doRepositoryAction(new RepositoryEvent(this, "addingSentence", numSentence, sentencesList.size()));
						if (numSentence == sentencesList.size()) {
							url = "last";
						}
						String sentenceExtId = "s" + sentenceNumber + "d" + documentId;
						this.addTextPassageToOpenIndex(
								sentence,
								"sentence",
								paragraphExtId,
								documentId,
								sentenceExtId,
								"Sentence " + sentenceNumber + " of " + title,
								url);
					} else {
						ignoredSentences++;
					}
				}
			} else {
				ignoredLines++;
			}
		}

		time = System.currentTimeMillis() - time;
		doRepositoryAction(new RepositoryEvent(this, "addingSentence", 100, 100));

		logger.debug("Added " + paragraphNumber + " paragraphs and "
				+ sentenceNumber + " sentences.");
		logger.debug("Ignored " + ignoredLines + " paragraphs and "
				+ ignoredSentences + " sentences.");
	}

	/**
	 * Inserts a new text passage into the Repository.
	 *
	 * @param content
	 *            the content of the document
	 * @param title
	 *            the title of the document
	 * @param url
	 *            the url of the document
	 * @param type
	 *            the type of the document ("document", "sentence" or
	 *            "paragraph")
	 * @param parent
	 *            the id of the parent document (when type is segment)
	 * @return the Lucene Document that was just added
	 * @throws IOException
	 * @throws SQLException 
	 */
	private Document addTextPassageToOpenIndex(String content, String type,
			String parent, String parentDocument, String externalId, String title, String url) throws IOException, SQLException {

		Document document = new Document();
		document.add(new Field(this.getLuceneContentField(), content,
				Store.YES, Index.ANALYZED, TermVector.WITH_POSITIONS));
		document.add(new Field(this.getLuceneExternalIdField(), externalId,
				Store.YES, Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field(this.getLuceneTitleField(), title, Store.YES,
				Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field(this.getLuceneUrlField(), url, Store.YES,
				Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field("indexdate", Calendar.getInstance().getTime().toString(), Store.YES, Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field(this.getLuceneParentField(), parent, Store.YES,
				Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field("type", type, Store.YES, Index.NOT_ANALYZED,
				TermVector.NO));
		document.add(new Field("parent", parentDocument, Store.YES, Index.NOT_ANALYZED,
				TermVector.NO));

		this.getDbConnection().insertDocument(this, document);

		Term term = new Term("externalid", externalId);

		luceneIndexWriter.updateDocument(term, document);

		return document;
	}

	/**
	 * Inserts a new text passage into the Repository.
	 *
	 * @param content
	 *            the content of the document
	 * @param title
	 *            the title of the document
	 * @param url
	 *            the url of the document
	 * @param type
	 *            the type of the document ("document", "sentence" or
	 *            "paragraph")
	 * @param parent
	 *            the id of the parent document (when type is segment)
	 * @return the Lucene Document that was just added
	 * @throws IOException
	 * @throws SQLException 
	 */
	private Document addDocumentToOpenIndex(Document document) throws IOException, SQLException {

		this.getDbConnection().insertDocument(this, document);

		Term term = new Term("externalid", document.get(this.getLuceneExternalIdField()));

		luceneIndexWriter.updateDocument(term, document);

		return document;
	}

	/**
	 * Inserts a new text passage into the Repository.
	 *
	 * @param content
	 *            the content of the document
	 * @param title
	 *            the title of the document
	 * @param url
	 *            the url of the document
	 * @param type
	 *            the type of the document ("document", "sentence" or
	 *            "paragraph")
	 * @param parent
	 *            the id of the parent document (when type is segment)
	 * @return the Lucene Document that was just added
	 * @throws IOException
	 */
	private Document createDocument(String content, String type,
			String parent, String parentDocument, String externalId, String title, String url) throws IOException {

		Document document = new Document();
		document.add(new Field(this.getLuceneContentField(), content,
				Store.YES, Index.ANALYZED, TermVector.WITH_POSITIONS));
		document.add(new Field(this.getLuceneExternalIdField(), externalId,
				Store.YES, Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field(this.getLuceneTitleField(), title, Store.YES,
				Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field(this.getLuceneUrlField(), url, Store.YES,
				Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field("indexdate", Calendar.getInstance().getTime().toString(), Store.YES, Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field(this.getLuceneParentField(), parent, Store.YES,
				Index.NOT_ANALYZED, TermVector.NO));
		document.add(new Field("type", type, Store.YES, Index.NOT_ANALYZED,
				TermVector.NO));
		document.add(new Field("parent", parentDocument, Store.YES, Index.NOT_ANALYZED,
				TermVector.NO));

		return document;
	}

	public Thread annotateDocuments() {
		DocumentAnnotator process = new DocumentAnnotator(this);
		Thread t = new Thread(process);
		t.start();
		return t;
	}

	private void closeIndexWriter() throws CorruptIndexException, IOException {
		if(luceneIndexWriter == null)
			return;

		luceneIndexWriter.commit();
		luceneIndexWriter.optimize(true);
		luceneIndexWriter.close(true);
	}

	/**
	 * Deletes a document from the repository. A TextDocument object must be
	 * used so the document must be first obtained from the repository.
	 *
	 * @param document
	 * @throws IOException
	 */
	public void deleteTextDocument(TextDocument document) throws IOException {
		logger.info("Deleting document " + document);
		Term term = new Term(
				this.luceneExternalIdField,
				document.getExternalId());
		this.openIndexWriter();
		luceneIndexWriter.deleteDocuments(term);
		term = new Term(this.luceneParentDocumentField, document.getExternalId());
		luceneIndexWriter.deleteDocuments(term);
		this.closeIndexWriter();
	}

	/**
	 * Returns a list with all the documents in the repository in
	 * {@link TextDocument} form
	 *
	 * @return a list of {@link TextDocument}
	 * @throws Exception
	 */
	public List<TextDocument> getAllTextDocuments() throws Exception {
		List<TextDocument> documents = new ArrayList<TextDocument>();
		RepositoryCorpus corpus = new RepositoryCorpus();
		try {
			corpus.setParameters(CorpusParameters.getNoReductionParameters());
			corpus.load(this);
		} catch (NoDocumentsInCorpusException e) {
			return documents;
		} catch (Exception e) {
			throw e;
		}
		for (String externalId : corpus.getPassages()) {
			documents.add(getTextDocument(cleanIdForLucene(externalId)));
		}
		return documents;
	}

	/**
	 * Gets the Lucene analyzer that the {@link Repository} is using
	 *
	 * @return the {@link Analyzer}
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * @return the annotators available for this repository
	 */
	public List<Annotator> getAnnotators() {
		return annotators;
	}

	/**
	 * Gets the content of a field for a document, using its external id.
	 * @param externalId the id of the document
	 * @param fieldname the name of the field to retrieve
	 * @return the content of the field
	 * @throws IOException
	 */
	public String getDocumentField(String externalId, String fieldname) throws IOException {
		Document document = getLuceneDocument(externalId);
		return document.get(fieldname);
	}

	/**
	 * @return the encoding used by TML
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @return the path to the Lucene index
	 */
	public String getIndexPath() {
		return indexPath;
	}

	/**
	 * Obtains an IndexReader of the Lucene index
	 *
	 * @return the IndexReader
	 * @throws IOException
	 */
	public IndexReader getIndexReader() throws IOException {
		if (luceneIndexReader == null || !luceneIndexReader.isCurrent()) {
			luceneIndexReader = FilterIndexReader.open(SimpleFSDirectory.open(new File(this.indexPath)), true);
		}
		return luceneIndexReader;
	}

	/**
	 * Obtains an IndexSearcher for the Lucene index
	 *
	 * @return the IndexSearcher
	 * @throws IOException
	 */
	public IndexSearcher getIndexSearcher() throws IOException {
		return new IndexSearcher(this.getIndexReader());
	}

	/**
	 * @return the {@link Locale} being used by TML
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Gets the name of the field used by the underlying Lucene index for the
	 * content
	 *
	 * @return the name of the content field
	 */
	public String getLuceneContentField() {
		return luceneContentField;
	}

	private Document getLuceneDocument(String externalId) throws IOException {
		TopDocs hits = getLuceneDocumentHits(externalId);
		if (hits == null) {
			throw new IOException("Document " + externalId + " not found!");
		}
		Document doc = this.getIndexSearcher().doc(hits.scoreDocs[0].doc);
		return doc;
	}

	private TopDocs getLuceneDocumentHits(String externalId) throws IOException {
		QueryParser parser = new QueryParser(Version.LUCENE_29,
				this.getLuceneContentField(),
				new KeywordAnalyzer());
		//		logger.debug("Retrieving document " + externalId);
		String query = "externalid:" + externalId;
		Query documentsQuery;
		try {
			documentsQuery = parser.parse(query);
		} catch (ParseException e) {
			logger.error("Invalid externalId:" + externalId);
			e.printStackTrace();
			return null;
		}

		TopDocs hits = this.getIndexSearcher().search(documentsQuery, 9999);
		if (hits.totalHits < 1) {
			return null;
		}
		if (hits.totalHits > 1) {
			throw new IOException("The query returned more than one document");
		}

		return hits;
	}

	/**
	 * Gets the name of the field used by the underlying Lucene index for the
	 * external id
	 *
	 * @return the name of the external id field
	 */
	public String getLuceneExternalIdField() {
		return luceneExternalIdField;
	}

	/**
	 * Gets the name of the field used by the underlying Lucene index for the
	 * parent
	 *
	 * @return the name of the parent field
	 */
	public String getLuceneParentField() {
		return luceneParentField;
	}

	/**
	 * @return the name of the field used to store the PennTree bank string
	 */
	public String getLucenePenntreeField() {
		return lucenePenntreeField;
	}

	/**
	 * Gets the name of the field used by the underlying Lucene index for the
	 * title
	 *
	 * @return the name of the title field
	 */
	public String getLuceneTitleField() {
		return luceneTitleField;
	}

	/**
	 * @return the name of the field that stores the type of the Lucene
	 * document (document, paragraph or sentence)
	 */
	public String getLuceneTypeField() {
		return luceneTypeField;
	}

	/**
	 * Gets the name of the field used by the underlying Lucene index for the
	 * url
	 *
	 * @return the name of the url field
	 */
	public String getLuceneUrlField() {
		return luceneUrlField;
	}

	/**
	 * @return the maxDocumentsToIndex
	 */
	public int getMaxDocumentsToIndex() {
		return maxDocumentsToIndex;
	}

	/**
	 * Gets the {@link Importer} used to transform the content before inserting
	 * into the {@link Repository}
	 *
	 * @return the {@link Importer} being used by TML
	 */
	public Importer getParser() {
		return defaultImporter;
	}

	/**
	 * @return the list of stopwords used to analyse and parse documents
	 */
	public String[] getStopwords() {
		return stopwords;
	}

	private String[] getStopWordsFromBufferedReader(BufferedReader reader)
			throws IOException {
		List<String> stopwords = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null) {
			stopwords.add(line);
			line = reader.readLine();
		}
		String[] output = new String[stopwords.size()];
		return stopwords.toArray(output);
	}

	/**
	 * Processes a file and returns each line in an array. It's useful to
	 * transform a stopwords file into the list that Lucene needs.
	 *
	 * @param file
	 *            an absolute path to the stopwords file
	 * @return an array of stop words
	 * @throws IOException
	 */
	private String[] getStopWordsFromFile(File file) throws IOException {
		List<String> stopwords = new ArrayList<String>();
		BufferedReader reader = null;
		if (file != null) {
			reader = new BufferedReader(new FileReader(file));
		} else {
			reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("/stopwords.txt")));
		}
		String line = reader.readLine();
		while (line != null) {
			stopwords.add(line);
			line = reader.readLine();
		}
		String[] output = new String[stopwords.size()];
		return stopwords.toArray(output);
	}

	/**
	 * @return the svdStoragePath
	 */
	public String getSvdStoragePath() {
		return svdStoragePath;
	}

	/**
	 * Gets a document from the repository by its external id. Returns a
	 * {@link TextDocument} object with basic information about the document,
	 * like title and url. In order to perform operations on the documents, it
	 * must be loaded, which means that a {@link Corpus} and its inner
	 * {@link SemanticSpace} will be created.
	 *
	 * @param externalId
	 *            the id of the document
	 * @return a {@link TextDocument}
	 * @throws IOException
	 */
	public TextDocument getTextDocument(String externalId) throws IOException {
		TopDocs hits = getLuceneDocumentHits(externalId);

		Document doc = this.getIndexSearcher().doc(hits.scoreDocs[0].doc);
		TextDocument document = new TextDocument(hits.scoreDocs[0].doc, doc.get(getLuceneTitleField()), doc.get(getLuceneUrlField()),
				externalId, doc.get(getLuceneContentField()));
		return document;
	}

	/**
	 * Add reference
	 *
	 * @param sentence
	 *            the sentence to evaluate
	 * @return if the sentence corresponds to the title of the references
	 *         section
	 */
	public boolean isBibliographyTitle(String sentence) {
		String[] words = sentence.split("\\s");
		if (words.length >= 4) {
			return false;
		}
		for (String word : words) {
			if (word.toLowerCase().matches(
					"(\\d+)?\\s*((resources?)|(references?)|(bibliography)|(notes?))\\s*")) {
				return true;
			}
		}
		return false;
	}

	private void openIndexWriter() throws LockObtainFailedException, CorruptIndexException, IOException {
		Directory dir = null;
		try {
			dir = SimpleFSDirectory.open(new File(indexPath));
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		try {
			luceneIndexWriter = new IndexWriter(
					dir, 
					this.analyzer, 
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
			throw e;
		} catch (LockObtainFailedException e) {
			logger.error("Index is locked! Trying to unlock.");
			IndexWriter.unlock(dir);
			luceneIndexWriter = new IndexWriter(
					dir, 
					this.analyzer, 
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Removes an annotator to the repository
	 * @param annotator the annotator
	 */
	public void removeAnnotator(Annotator annotator) {
		this.annotators.remove(annotator);
	}

	/**
	 * Sets the character encoding that will be used in this repository
	 *
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		if (Charset.isSupported(encoding)) {
			this.encoding = encoding;
		} else {
			logger.info("Invalid encoding or not supported");
		}
	}

	/**
	 * @param maxDocumentsToIndex the maxDocumentsToIndex to set
	 */
	public void setMaxDocumentsToIndex(int maxDocumentsToIndex) {
		this.maxDocumentsToIndex = maxDocumentsToIndex;
	}

	public String getAnnotations(String documentId,
			String fieldName) {

		return this.getDbConnection().getAnnotation(documentId, fieldName);
	}

	public Thread cleanup() {
		DocumentCleanup process = new DocumentCleanup(this);
		Thread t = new Thread(process);
		t.start();
		return t;
	}

	private void initializeCleanupTimer() throws IOException {

		cleanupTimer = new Timer();

		TmlCleanupTask task = new TmlCleanupTask(this);

		int seconds = 300;
		try {
			seconds = Integer.parseInt(Configuration.getTmlProperties()
					.getProperty("tml.cleanup.interval"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Annotator interval not set or invalid "
					+ Configuration.getTmlProperties().getProperty("tml.cleanup.interval"));
		}
		logger.info("TML cleanup started every " + seconds + " seconds");
		cleanupTimer.schedule(task, new Date(), seconds * 1000);
	}

	private void initializeAnnotatorTimer() throws IOException {
		if(this.getAnnotators().size() == 0) {
			logger.info("There are no annotators, no need to run.");
			return;
		}

		annotatorTimer = new Timer();

		TmlAnnotatorTask task = new TmlAnnotatorTask(this);

		int seconds = 300;
		try {
			seconds = Integer.parseInt(Configuration.getTmlProperties()
					.getProperty("tml.annotator.interval"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Annotator interval not set or invalid "
					+ Configuration.getTmlProperties().getProperty("tml.annotator.interval"));
		}
		logger.info("TML annotator started every " + seconds + " seconds");
		annotatorTimer.schedule(task, new Date(), seconds * 1000);
	}

	private void initializeIndexerTimer() throws IOException {
		indexerTimer = new Timer();

		TmlIndexerTask task = new TmlIndexerTask(this);
		task.setMaxFilesToProcess(1);
		task.setUploadFolder(Configuration.getTmlFolder() + "upload");

		int seconds = 300;
		try {
			seconds = Integer.parseInt(Configuration.getTmlProperties()
					.getProperty("tml.indexer.interval"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Indexer interval not set or invalid "
					+ Configuration.getTmlProperties().getProperty("tml.indexer.interval"));
		}
		logger.info("TML indexer started every " + seconds + " seconds");
		indexerTimer.schedule(task, new Date(), seconds * 1000);
	}
}
