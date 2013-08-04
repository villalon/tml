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

/**
 * Interface for all importers. It defines what kind of files it can manage, 
 * basically by extension, and implements a method to obtain the plain text
 * version of the content.
 * 
 * @author Jorge Villalon
 *
 */
public interface Importer {
	/**
	 * @param content the text to clean
	 * @return the plain text version of the content
	 */
	public String getCleanContent(String content);

	/**
	 * @param fileExtension
	 * @return true if the importer can manage the extension
	 */
	public boolean isValidFileExtension(String fileExtension);
}
