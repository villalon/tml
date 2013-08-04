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
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class LuceneUtils {

	private static Logger logger = Logger.getLogger(LuceneUtils.class);
	
	@SuppressWarnings("deprecation")
	public static String stemWords(String words) {
		TokenStream stream = new StandardTokenizer(Version.LUCENE_29, new StringReader(words));
		SnowballFilter filter = new SnowballFilter(stream, "English");
		Token token = new Token();
		StringBuffer stemmed = new StringBuffer();
		try {
			while((token = filter.next(token)) != null) {
				stemmed.append(token.term());
				stemmed.append(" ");
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return stemmed.toString().trim();
	}
}
