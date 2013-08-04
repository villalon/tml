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

import java.util.List;
import java.util.regex.Pattern;

public class RegexUtils {

	public static boolean stringContained(String s1, String s2) {
		String pattern1 = ".*(^|\\W)" + Pattern.quote(s1) + "(\\W|$).*";
		String pattern2 = ".*(^|\\W)" + Pattern.quote(s2) + "(\\W|$).*";

		boolean s1Ins2 = false;
		boolean s2Ins1 = false;
		
		if(s1.indexOf(s2) < 0) {
			if(s2.indexOf(s1) < 0)
				return false;
			
			s1Ins2 = s2.matches(pattern1);
		} else {
			s2Ins1 = s1.matches(pattern2);
		}
		
		return  s1Ins2 || s2Ins1;
	}
	
	public static boolean stringIsContainedInList(List<String> list, String word) {
		for(String w : list) {
			if(w.equals(word) || stringContained(w, word) || stringContained(word, w))
				return true;
		}
		return false;
	}
}
