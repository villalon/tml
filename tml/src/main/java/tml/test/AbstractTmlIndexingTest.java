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
/**
 * 
 */
package tml.test;


import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;

import tml.Configuration;
import tml.storage.Repository;


/**
 * This class implements a base class for all tests that require indexing all the documents
 * within a specific folder.
 * 
 * @author Jorge Villalon
 *
 */
public abstract class AbstractTmlIndexingTest {

	protected static Logger logger = Logger.getLogger(AbstractTmlIndexingTest.class);
	
	protected static Repository repository;
	protected static String repositoryFolder = null;
	protected static String documentsFolder;
	protected static File[] filesToAdd = null;
	protected static Properties prop;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		prop = Configuration.getTmlProperties(true);
		Repository.cleanStorage(Configuration.getTmlFolder() + "/test/lucene");
		repository = new Repository(Configuration.getTmlFolder() + "/test/lucene");
	}
}
