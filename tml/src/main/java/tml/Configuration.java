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

package tml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import tml.sql.DbConnection;

/**
 * This class implements the configuration for TML, it reads a default config file from the jar.
 * It contains the settings for log4j, stopwords and language to use.
 * 
 * @author Jorge Villalon
 * 
 */
public class Configuration {

	private static Logger logger = Logger.getLogger(Configuration.class);
	
	/** The actual properties */
	private static Properties properties = null;
	/** If TML will run in debug mode */
	private static boolean debugMode = false;
	/** Path to the execution context */
	private static String contextPath;
	/** Folder for TML to work */
	private static String tmlFolder;
	/** */
	public static String getTmlFolder() {
		if(tmlFolder==null)
			try {
				getTmlProperties();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		return tmlFolder;
	}
	/** */
	public static void setTmlFolder(String tmlFolder) {
		Configuration.tmlFolder = tmlFolder;
		File tmlfolder = new File(tmlFolder);
		if(!tmlfolder.exists()) {
			tmlfolder.mkdir();
		}
		File lucenefolder = new File(tmlFolder + "/lucene");
		if(!lucenefolder.exists()) {
			lucenefolder.mkdir();
		}
	}

	public static String getReport() {
		if(properties == null)
			return "Configuration has not been initialized.";
		
		return properties.toString();
	}
	
	public void getReport(PrintStream out) {
		if(properties == null)
			out.append("Configuration has not been initialized.");
		
		properties.list(out);
	}
	
	public static String getContextPath() {
		return contextPath;
	}

	/**
	 * @return the debugMode
	 */
	public static boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * @param debugMode the debugMode to set
	 */
	public static void setDebugMode(boolean debugMode) {
		Configuration.debugMode = debugMode;
	}

	/**
	 * @return the set the default properties for TML
	 * @throws IOException
	 */
	public static Properties getTmlProperties() throws IOException {
		return getTmlProperties(Configuration.debugMode);
	}

	/**
	 * @return the set the default properties for TML
	 * @throws IOException
	 */
	public static Properties getTmlProperties(String tmlFolder) throws IOException {
		return getTmlProperties(Configuration.debugMode, tmlFolder);
	}

	/**
	 * @return the set the default properties for TML
	 * @throws IOException
	 */
	public static Properties getTmlProperties(boolean debugMode, String tmlFolder) throws IOException {

		Configuration.debugMode = debugMode;

		if (properties != null)
			return properties;
		
		initialize();
		
		if(tmlFolder != null)
			setTmlFolder(tmlFolder);
		else if(Configuration.tmlFolder == null)
			setTmlFolder(properties.getProperty("tml.folder"));

		logger.info("TML folder:\t\t" + getTmlFolder());

		return properties;
	}

	/**
	 * @return the set the default properties for TML
	 * @throws IOException
	 */
	public static Properties getTmlProperties(boolean debugMode) throws IOException {
		return getTmlProperties(debugMode, null);
	}

	/**
	 * Set TML properties
	 * @param prop
	 */
	public static void setProperties(Properties prop) {
		properties = prop;
	}
	
	/**
	 * Initializes TML configuration. Reads the default configuration and the overrides if
	 * tml.properties file is found in execution folder.
	 * 
	 * @throws IOException
	 */
	public static void initialize() throws IOException {
		
		File f = new File(".");
		contextPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
		
		InputStream iStream = null;

		Properties log4jProperties = new Properties();
		// Loading log4j configuration
		if(Configuration.debugMode)
			iStream = Configuration.class.getResourceAsStream("/tml/log4j.debug.properties");
		else
			iStream = Configuration.class.getResourceAsStream("/tml/log4j.properties");

		if (iStream == null) {
			throw new IOException("Couldn't find default log4j configuration!");
		}

		try {
			log4jProperties.load(iStream);
		} catch (IOException e) {
			throw new IOException("Couldn't read default log4j configuration!");
		}

		properties = new Properties(log4jProperties);

		PropertyConfigurator.configure(properties);

		// Loading tml default configuration
		iStream = Configuration.class.getResourceAsStream("/tml/tml.properties");

		if (iStream == null) {
			throw new IOException("Couldn't find default tml configuration!");
		}

		try {
			properties.load(iStream);
		} catch (IOException e) {
			throw new IOException("Couldn't read default tml configuration!");
		}
		
		Properties propertiesFromFile = null;
		File propFile = new File("tml.properties");
		if(propFile.exists()) {
			logger.debug("Reading properties from filesystem: " + propFile.getAbsolutePath());
			propertiesFromFile = new Properties(properties);
			propertiesFromFile.load(new FileReader(propFile));
			properties = propertiesFromFile;
		} else {
			logger.debug("Custom properties not found: " + propFile.getAbsolutePath());			
		}

		DbConnection conn = null;
		try {
			conn = new DbConnection();
			conn.close();
		} catch (SQLException e1) {
			throw new IOException(e1);
		}
		conn.close();
		
		
		Date date = new Date();
		try {
			File jarFile = new File
			(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			date = new Date(jarFile.lastModified());
		} catch(Exception e) {
			e.printStackTrace();
		}
		String version = Configuration.class.getPackage().getImplementationVersion();
		if(version==null)
			version = "Devel (" + DateFormat.getInstance().format(date) + ")";
		
		logger.info("----------------------------------------------------");
		logger.info("TML - Text Mining Library");
		logger.info("Version: " + version + " initialized");
		logger.info("----------------------------------------------------");
	}
}
