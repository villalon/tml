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

package tml.storage.importers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Abstract class for all importers to extend from. It implements the logger
 * a list of file extensions and a static factory to obtain the right
 * importer for a given extension
 * 
 * @author Jorge Villalon
 *
 */
public abstract class AbstractImporter {

	protected static Logger logger = Logger.getLogger(AbstractImporter.class);

	protected abstract String[] getFileExtensions();

	protected List<String> fileExtensions;

	/**
	 * Creates a new instance of an {@link AbstractImporter}. As this class
	 * is an abstract class, this can be called only by the constructor
	 * of a sub-class 
	 */
	public AbstractImporter() {
		this.fileExtensions = new ArrayList<String>();
		for (String extension : getFileExtensions()) {
			this.fileExtensions.add(extension);
		}
	}

	/**
	 * @param fileExtension the extension of a filename (e.g. txt, pdf, doc)
	 * @return true if the importer can manage the extension
	 */
	public boolean isValidFileExtension(String fileExtension) {
		for (String extension : this.fileExtensions) {
			if (extension.equals(fileExtension))
				return true;
		}
		return false;
	}

	/**
	 * @param fileExtension the file extension to validate
	 * @return an importer to manage files of the given extension 
	 */
	public static Importer createImporter(String fileExtension) {
		Importer importer = null;

		importer = new TextImporter();
		if (importer.isValidFileExtension(fileExtension))
			return importer;

		importer = new HtmlImporter();
		if (importer.isValidFileExtension(fileExtension))
			return importer;

		return null;
	}
}
