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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import tml.corpus.Corpus;

import Jama.Matrix;


public class LanczosSVDPACKCUtils {

	private static Logger logger = Logger.getLogger(LanczosSVDPACKCUtils.class);

	private File tmpFolder = new File("lanczos/tmp");

	private int numterms = 0;

	private int numdocs = 0;

	private int singularvalues = -1;

	private Matrix u = null;
	private Matrix v = null;
	private Matrix s = null;

	private int rank;

	private long lanczosSteps;

	private double kappa;

	private double arr2double (byte[] arr, int start) {
		int i = 0;
		int len = 8;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			cnt++;
		}
		long accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 64; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}


	private long arr2long (byte[] arr, int start) {
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			cnt++;
		}
		long accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return accum;
	}

	private void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * @return the kappa
	 */
	public double getKappa() {
		return kappa;
	}

	/**
	 * @return the lanczosSteps
	 */
	public long getLanczosSteps() {
		return lanczosSteps;
	}
	/**
	 * @return the numdocs
	 */
	public int getNumdocs() {
		return numdocs;
	}

	/**
	 * @return the numterms
	 */
	public int getNumterms() {
		return numterms;
	}

	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * @return the s
	 */
	public Matrix getS() {
		return s;
	}

	/**
	 * @return the singularvalues
	 */
	public int getSingularvalues() {
		return singularvalues;
	}
	/**
	 * @return the u
	 */
	public Matrix getU() {
		return u;
	}
	/**
	 * @return the v
	 */
	public Matrix getV() {
		return v;
	}
	private double[] readLao(File lao) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(lao));
		String line = null;
		boolean started = false;
		singularvalues = -1;
		numterms = 0;
		numdocs = 0;
		double[] s = null;
		int current = 0;
		while((line = reader.readLine()) != null) {
			if(started) {
				if(singularvalues < 0) {
					reader.close();
					throw new Exception("Corrupt lao file! Couldn't find NSIG line before the singular values!");
				}
				if(numterms <= 0) {
					reader.close();
					throw new Exception("Corrupt lao file! Couldn't find TERMS line before the singular values!");
				}
				if(numdocs <= 0) {
					reader.close();
					throw new Exception("Corrupt lao file! Couldn't find DOCS line before the singular values!");
				}
				Pattern pattern = Pattern.compile("^ \\.{6}\\s+\\d+\\s+(\\d+\\.\\d+E[\\+\\-]\\d+)\\s+.*$");
				Matcher matcher = pattern.matcher(line);
				if(!matcher.matches()) {
					reader.close();
					throw new Exception("Corrupt lao file!");
				}
				double value = Double.parseDouble(matcher.group(1));
				s[s.length-current-1] = value;
				current++;
			}
			if(line.matches(".*COMPUTED S-VALUES.*")) {
				started = true;
				reader.readLine();
			} else if(line.matches(".*NSIG.*")) {				
				Pattern pattern = Pattern.compile("^ \\.{6}\\s+NSIG\\s+=\\s*(\\d+)\\s*$");
				Matcher matcher = pattern.matcher(line);
				if(!matcher.matches()) {
					reader.close();
					throw new Exception("Corrupt lao file! NSIG");
				}
				singularvalues = Integer.parseInt(matcher.group(1));
				s = new double[singularvalues];
				logger.debug("Total singular values:" + singularvalues);
			} else if(line.matches(".*NO\\. OF TERMS\\s+\\(ROWS\\).*")) {				
				Pattern pattern = Pattern.compile("^ \\.{3}\\s+NO\\. OF TERMS\\s+\\(ROWS\\)\\s+=\\s*(\\d+)\\s*$");
				Matcher matcher = pattern.matcher(line);
				if(!matcher.matches()) {
					reader.close();
					throw new Exception("Corrupt lao file! NO. OF TERMS");
				}
				numterms = Integer.parseInt(matcher.group(1));
				logger.debug("Total rows:" + numterms);
			} else if(line.matches(".*NO\\. OF DOCUMENTS\\s+\\(COLS\\).*")) {				
				Pattern pattern = Pattern.compile("^ \\.{3}\\s+NO\\. OF DOCUMENTS\\s+\\(COLS\\)\\s+=\\s*(\\d+)\\s*$");
				Matcher matcher = pattern.matcher(line);
				if(!matcher.matches()) {
					reader.close();
					throw new Exception("Corrupt lao file! NO. OF COLS");
				}
				numdocs = Integer.parseInt(matcher.group(1));
				logger.debug("Total columns:" + numdocs);
			}
		}
		reader.close();
		return s;
	}
	private void readLav(File lav) throws Exception {
		if(singularvalues < 0)
			throw new Exception("Corrupt lao file! Couldn't find NSIG line before the singular values!");
		if(numterms <= 0)
			throw new Exception("Corrupt lao file! Couldn't find TERMS line before the singular values!");
		if(numdocs <= 0)
			throw new Exception("Corrupt lao file! Couldn't find DOCS line before the singular values!");
		FileInputStream reader = new FileInputStream(lav);
		int vVectors = 0;
		int uVectors = 0;
		int currentData = 0;
		int blocksize = 0;
		byte[] headerbuff = new byte[24];
		reader.read(headerbuff);
		rank = (int) arr2long(headerbuff, 0);
		lanczosSteps = arr2long(headerbuff, 8);
		kappa = arr2double(headerbuff, 16);
		byte[] buff = new byte[8]; 
		while((blocksize = reader.read(buff)) != -1) {
			double num = arr2double(buff, 0);

			// HACK! No idea why bytes 24 (D0) and 46 (181) shouldn't be there... 
			double exp = 0;
			String expSt = (new DecimalFormat("0.############E0")).format(num);
			if(expSt.split("E").length>1)
				exp = Double.parseDouble(expSt.split("E")[1]);
			if(Math.abs(exp)>10) {
				logger.debug(currentData + " Jumping: " + num);
				for(int i=0;i<7;i++) {
					buff[i] = buff[i+1];
				}
				buff[7] = (byte) reader.read();
				num = arr2double(buff, 0);
			}
			int index = currentData % singularvalues;
			int indexU = currentData % numterms;
			if(vVectors < numdocs)
				v.set(numdocs - vVectors - 1, index, num);
			else if(uVectors < singularvalues) {
				u.set(indexU, singularvalues - uVectors - 1, num);
			//logger.debug(currentData + ": " + indexU + "," + (singularvalues - uVectors - 1) + " = " + num);
			//				v.set(vectors,i % rank,num);
			}
			if(blocksize != 8)
				logger.debug("Some size lost " + blocksize);
			currentData++;
			if(vVectors < numdocs) {
				if(currentData % singularvalues == 0) {
					vVectors++;
					if(vVectors == numdocs)
						currentData = 0;
				}
			} else {
				if(currentData % numterms == 0)
					uVectors++;
			}
		}
		v = v.transpose();
		logger.debug("Vectors:" + vVectors);
		reader.close();
	}
	public void runLanczos(Corpus corpus) throws Exception {
		for(File f : tmpFolder.listFiles()) {
			if(!f.isDirectory())
				f.delete();
		}
		
		writeCorpusParametersForLanczos(corpus, "lap2", corpus.getSemanticSpace().getDimensionsKept());
		writeCorpusInHBFormat(corpus, "matrix");

		File lanczosExec = new File("lanczos/windows/las2.exe");
		File exec = new File(tmpFolder.getAbsolutePath() + "/las2.exe");
		copy(lanczosExec, exec);
		String ls_str;

		long time = System.nanoTime();
		Process ls_proc = Runtime.getRuntime().exec(tmpFolder.getAbsolutePath() + "/las2.exe");

		// get its output (your input) stream

		BufferedReader reader = new BufferedReader(new InputStreamReader(ls_proc.getInputStream()));

		while ((ls_str = reader.readLine()) != null) {
			logger.debug(ls_str);
		}

		ls_proc.waitFor();
		time = System.nanoTime() - time;

		logger.debug("Lanczos took " + (time * 10E-9) + " millis");

		File matrix = new File("matrix");
		matrix.renameTo(new File(tmpFolder.getAbsolutePath() + "/matrix"));
		File lap2 = new File("lap2");
		lap2.renameTo(new File(tmpFolder.getAbsolutePath() + "/lap2"));

		File lao2 = new File("lao2");
		lao2.renameTo(new File(tmpFolder.getAbsolutePath() + "/lao2"));
		lao2 = new File(tmpFolder.getAbsolutePath() + "/lao2");
		File lav2 = new File("lav2");
		lav2.renameTo(new File(tmpFolder.getAbsolutePath() + "/lav2"));
		lav2 = new File(tmpFolder.getAbsolutePath() + "/lav2");

		double[] singulars = readLao(lao2);
		if(singulars == null)
			throw new Exception("Lanczos failed execution, please check the logs");
		s = new Matrix(singulars.length,singulars.length);
		for(int i=0;i<s.getColumnDimension();i++) {
			s.set(i, i, singulars[i]);
		}

		u = new Matrix(numterms,singularvalues);
		v = new Matrix(numdocs,singularvalues);

		readLav(lav2);		
	}

	private void writeCorpusInHBFormat(Corpus corpus, String filename) throws Exception {
		FileWriter writer = new FileWriter(new File(filename));
		writer.append("Learning Systems Group University of Sydney                               matrix\n");
		writer.append("#\n");
		String rowsAndColsline = "rra               ";
		rowsAndColsline += corpus.getTerms().length + "     ";
		rowsAndColsline += corpus.getPassages().length + "     ";
		rowsAndColsline += corpus.getNonzeros() + "     ";
		while(rowsAndColsline.length() < 79)
			rowsAndColsline += " ";
		rowsAndColsline += "0\n";
		writer.append(rowsAndColsline);
		writer.append("          (10i8)          (10i8)            (8f10.3)            (8f10.3)\n");
		List<Integer> indices = new ArrayList<Integer>();
		List<Integer> termindices = new ArrayList<Integer>();
		List<Double> values = new ArrayList<Double>();
		Matrix m = corpus.getTermDocMatrix();
		indices.add(1);
		int acumnonzeros = 1;
		for(int doc = 0; doc<m.getColumnDimension(); doc++) {
			int nonzeros = 0;
			for(int term = 0; term<m.getRowDimension(); term++) {
				if(m.get(term, doc) != 0) {
					termindices.add(term + 1);
					values.add(new Double(m.get(term, doc)));
					nonzeros++;
				}
			}
			acumnonzeros += nonzeros;
			indices.add(acumnonzeros);
		}
		String indicesLine = "             ";
		for(int i = 0; i<indices.size(); i++) {
			int ind = indices.get(i);
			indicesLine += Integer.toString(ind) + "   ";
			if(indicesLine.length() > 75 || i == indices.size()-1) {
				indicesLine += "\n";
				writer.append(indicesLine);
				indicesLine = "";
			}
		}
		String termIndicesLine = "             ";
		for(int i=0; i<termindices.size(); i++) {
			termIndicesLine += termindices.get(i) + "   ";
			if(termIndicesLine.length() > 75 || i == termindices.size()-1) {
				termIndicesLine += "\n";
				writer.append(termIndicesLine);
				termIndicesLine = "             ";
			}
		}
		String valuesLine = "             ";
		for(int i=0; i<values.size(); i++) {
			valuesLine += (new DecimalFormat("0.000")).format(values.get(i)) + "   ";
			if(valuesLine.length() > 75 || i == values.size()-1) {
				valuesLine += "\n";
				writer.append(valuesLine);
				valuesLine = "             ";
			}
		}
		writer.close();
	}


	private void writeCorpusParametersForLanczos(Corpus corpus, String filename, int maxDimensionality) throws Exception {
		FileWriter writer = new FileWriter(new File(filename));

		int rank = Math.min(corpus.getPassages().length, corpus.getTerms().length);

		writer.append("'matrix' " + rank + " " + maxDimensionality + " -1.0e-30 1.0e-30 TRUE 1.0e-6 0");

		writer.close();
	}
}
