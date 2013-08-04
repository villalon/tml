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

import org.apache.log4j.Logger;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

/**
 * This importer uses org.htmlpraser to obtain plain text from an HTML file.
 * 
 * @author Jorge Villalon
 *
 */
public class HtmlImporter extends AbstractImporter implements Importer {

	private static Logger logger = Logger.getLogger(HtmlImporter.class);

	@Override
	public String getCleanContent(String content) {

		String clean = null;
		try {
			Parser parser = new Parser();
			parser.setInputHTML(content);
			StringBean bean = new StringBean();
			parser.visitAllNodesWith(bean);
			clean = bean.getStrings();
		} catch (ParserException e) {
			logger.error(e);
		}
		return clean;
	}

	@Override
	protected String[] getFileExtensions() {
		String[] extensions = new String[3];
		extensions[0] = "xhtml";
		extensions[1] = "html";
		extensions[2] = "htm";
		return extensions;
	}

}
