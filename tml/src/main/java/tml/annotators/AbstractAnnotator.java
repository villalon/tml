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
package tml.annotators;

import java.io.IOException;
import java.util.ArrayList;

public class AbstractAnnotator {

	private String fieldName;
	protected ArrayList<String> types;

	public ArrayList<String> getTypes() {
		return types;
	}
	public AbstractAnnotator(String fieldName, String[] types) throws IOException {
		this.fieldName = fieldName;
		this.types = new ArrayList<String>();
		for(String type : types) {
			this.types.add(type);
		}
	}
	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
}
