/*******************************************************************************
 *  Copyright 2007, 2009 Ming Liu
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
/*******************************************************************************
 * Copyright (C) 2001, 2009 University of Sydney
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 *******************************************************************************/
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;

import tml.Configuration;
import tml.corpus.SentenceCorpus;
import tml.corpus.TextDocument;
import tml.corpus.CorpusParameters.TermSelection;
import tml.storage.Repository;
import tml.utils.DBUtils;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.TermWeightingException;

/**
 * This class implements the management of meta information in sentence level.
 * It searches the lucene index where the sentences are stored, and 
 * then parsed the sentence and insert the annotated sentence into Mysql database. 
 * The setting for lucene index file path and Mysql database are read from TML property file
 * 
 * @author Ming Liu
 * 
 */
public class AnnotatorManager {

	// General attributes
	/** The logger for log4j */
	private static Logger logger = Logger.getLogger(AnnotatorManager.class);
	private String driver;
	private String url;
	private String username;
	private String password;
	private String indexpath;
	private List<Annotator> annotators = new ArrayList<Annotator>();
//	private String docid ="document%3Adddxrkj5142cgn4dtd6-5";
	DBUtils dbutil = null;
	private Repository repository=null;
	
	public AnnotatorManager() throws Exception {
		
		// Read default properties and initialize database connection parameters
		//Configuration.getTmlProperties();
		driver = Configuration.getTmlProperties().getProperty(
				"tml.database.driver");
		url = Configuration.getTmlProperties().getProperty("tml.database.url");
		username = Configuration.getTmlProperties().getProperty(
				"tml.database.username");
		password = Configuration.getTmlProperties().getProperty(
				"tml.database.password");
		// TODO: Analyze if storing the indexpath in the properties file violates having
		// one repository per JVM. It should be a different properties file.
		indexpath=Configuration.getTmlProperties().getProperty(
		"tml.lucene.indexpath");
		dbutil = new DBUtils(driver,url,username,password);
		repository = new Repository(indexpath);

	}
	/**
	 *  insert the annotated text into Mysql DB. 
	 */
	public void insertMetainfoToDB()
	{
		dbutil.setConnection();
		getAnnotators();
		ArrayList<String> unprocessedList = searchDocTable();
		for(int i=0; i<unprocessedList.size();i++)
		{
		 String documentid=unprocessedList.get(i);
		 HashMap<String,String> sentencesandid = getSentenceFromLucene(documentid);
		 if (sentencesandid==null)
		 {
			 updateDocTable(documentid,"Unavailable in Lucene");
			 continue;
		 }
		 Set<Map.Entry<String, String>> entrySet = sentencesandid.entrySet();
		 Iterator<Entry<String, String>> it = entrySet.iterator();	
		 while (it.hasNext()) {
		  
		   Map.Entry<String, String> en= it.next();
		   for(int j=0;j<annotators.size();j++)
			{
				Annotator annotator = annotators.get(j);				
				double time = System.nanoTime();
				
				String annotatedText = annotator.getAnnotations(en.getValue());
				if (annotatedText==null)
				{
					updateDocTable(documentid,"failure");
				}
				time = (System.nanoTime() - time) * 10E-9;
				// avoid sql injection, particularly in single quote problem
				annotatedText = annotatedText.replace("'", "''");				
				dbinsert(en.getKey(),documentid,annotatedText,annotator.getFieldName(),time);
				updateDocTable(documentid,"processed");
			}
		  }
		}
		 dbutil.closeConnection();
		  
	}
	/**
	 * retrive sentence id and value in pair from lucene index by documentid
	 * @param documentid
	 * @return a Hashmap where the key contains sentenceid and the value contains its content.
	 */
	public HashMap<String,String> getSentenceFromLucene(String documentid) 
	{
		try {							
			TextDocument document = repository.getTextDocument(documentid);
			SentenceCorpus corpus = new SentenceCorpus(document);
//			corpus.getParameters().setCalculateSemanticSpace(false);
			corpus.getParameters().setTermSelectionCriterion(TermSelection.TF);
			corpus.getParameters().setTermSelectionThreshold(0);
			corpus.load(repository);
//			document.load(repository);
//			Corpus sentenceCorpus=document.getSentenceCorpus();	
			String[] sentences =corpus.getPassages();
			HashMap<String,String> sentenceContent = new HashMap<String,String> ();
			for(int i=0;i<sentences.length;i++)
			{
				sentenceContent.put(sentences[i],repository.getDocumentField(sentences[i],"contents"));				
			}
			return sentenceContent;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotEnoughTermsInCorpusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoDocumentsInCorpusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TermWeightingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * retrieve metainfo by documentid and annotation type from Mysql database
	 * @param docid	
	 * @param type
	 * @return an ArrayList which contains the annotation information of each sentence
	 */
	public ArrayList<String> getMetaInfoBydocId(String docid,String type)
	{
		dbutil.setConnection();
	  ArrayList<String> metainfo=dbutil.sendQuery("select metadata from metainfo where docid='"+docid+"'and annotator='"+type+"';","metadata");
	
	  return metainfo;
	}
	
	/**
	 * insert meta info into Mysql database
	 * @param sentenceid
	 * @param docid
	 * @param annotatedtext
	 * @param type
	 */
	public void dbinsert(String sentenceid,String docid, String annotatedtext,String type,double time)
	{
		dbutil.setConnection();
		int result=dbutil.sendUpdate("insert into metainfo values('"+sentenceid+"','"+docid+"','"+annotatedtext+"','"+type+"','"+time+"');");
		if(result==-1)
		{
			logger.info("fail to insert to metainfo table");
		}
		
	}	
	/**
	 * get all the annotators configured in TML property file 
	 */
	
	@SuppressWarnings("rawtypes")
	public void getAnnotators()
	{
		// Loads default annotators
		String annotators=null;
		try {
			annotators = Configuration.getTmlProperties().getProperty("tml.annotators");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			logger.info(e1.getMessage());
		}

		for(String annotatorName : annotators.split(",")) {
			if(annotatorName.trim().length() == 0)
				continue;
			
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
	
	public void insertDocTable(String docid)
	{
		dbutil.setConnection();
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss"); 
		String status="Unprocessed";
		String datetime = tempDate.format(new java.util.Date());		
	
		int result=dbutil.sendUpdate("insert into docs values('"+docid+"','"+status+"','"+datetime+"');");
		if(result==-1)
		{
			logger.info("fail to insert to metainfo table");
		}
		
	}
	
	public ArrayList<String> searchDocTable()
	{
		dbutil.setConnection();
		ArrayList<String> docidInfo=dbutil.sendQuery("select docid from docs where status='Unprocessed';","docid");
		 
		  return docidInfo;
	}
	
	public void updateDocTable(String docid,String status)
	{
		dbutil.setConnection();
		int result=dbutil.sendUpdate("update docs set status='"+status+"' where docid='"+docid+"'");
		if(result==-1)
		{
			logger.info("fail to update to doc table");
		}  
		 
	}
	
	
	
	
	

}
