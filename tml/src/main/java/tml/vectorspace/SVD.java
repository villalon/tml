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
package tml.vectorspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SVD implements Serializable {

	/** Serialization ID */
	private static final long serialVersionUID = -1733583945325917544L;
	
	/** Terms matrix in the semantic space */
	private double[][] Ukdata = null;
	/** Singular values in the semantic space */
	private double[][] Skdata = null;
	/** Documents matrix in the semantic space */
	private double[][] Vkdata = null;
	/**
	 * @return the ukdata
	 */
	public double[][] getUkdata() {
		return Ukdata;
	}
	/**
	 * @param ukdata the ukdata to set
	 */
	public void setUkdata(double[][] ukdata) {
		Ukdata = ukdata;
	}
	/**
	 * @return the skdata
	 */
	public double[][] getSkdata() {
		return Skdata;
	}
	/**
	 * @param skdata the skdata to set
	 */
	public void setSkdata(double[][] skdata) {
		Skdata = skdata;
	}
	/**
	 * @return the vkdata
	 */
	public double[][] getVkdata() {
		return Vkdata;
	}
	/**
	 * @param vkdata the vkdata to set
	 */
	public void setVkdata(double[][] vkdata) {
		Vkdata = vkdata;
	}
	
	public void saveSVD(File file) throws IOException {
		FileOutputStream stream = new FileOutputStream(file);
		ObjectOutputStream objSt = new ObjectOutputStream(stream);
		objSt.writeObject(this);
		objSt.close();		
	}
	
	public static SVD readSVD(File file) throws IOException, ClassNotFoundException {
		FileInputStream stream = new FileInputStream(file);
		ObjectInputStream objSt = new ObjectInputStream(stream);
		SVD svd = (SVD) objSt.readObject();
		objSt.close();
		return svd;
	}
}
