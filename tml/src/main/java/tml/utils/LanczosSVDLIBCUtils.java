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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import tml.Configuration;
import tml.corpus.Corpus;

import Jama.Matrix;


public class LanczosSVDLIBCUtils {

	private static Logger logger = Logger.getLogger(LanczosSVDLIBCUtils.class);

	private File tmpFolder = null;

	private int numterms = 0;
	private int numdocs = 0;
	private int singularvalues = -1;

	private Matrix u = null;
	private Matrix v = null;
	private Matrix s = null;

	private int rank;
	private long lanczosSteps;
	private double kappa;
	private String osfolder = "windows";
	private static final String WIN32_EXEC = "svd.exe";
	private static final String LINUX_EXEC = "svd";
	private String executable = WIN32_EXEC; 
	private static final String WIN32_NEWLINE = "\r\n";
	private static final String LINUX_NEWLINE = "\n";
	private String newLine = WIN32_NEWLINE; 
	private static final String TMP_FOLDER = "tmp";
	private String baseFolder = "lanczos";

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
	
	private Matrix readDenseMatrix(File file, Corpus corpus) throws IOException {
		Matrix m = null;
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		String[] parts = line.split("\\s+");
		int rows;
		int columns;
		boolean matrixS = false;
		if(parts.length == 1) {
			rows = Integer.parseInt(parts[0]);
			if(rows != corpus.getSemanticSpace().getDimensionsKept()) {
				logger.debug("Found less singular values than solicited. " + rows + " out of " + corpus.getSemanticSpace().getDimensionsKept());
				rows = corpus.getSemanticSpace().getDimensionsKept();
			}
			columns = rows;
			matrixS = true;
		} else if(parts.length == 2) {
			rows = Integer.parseInt(parts[0]);
			columns = Integer.parseInt(parts[1]);
		} else {
			reader.close();
			logger.error("Invalid format of first line!");
			return null;
		}
		m = new Matrix(rows, columns);
		int lineNumber = 0;
		while((line = reader.readLine()) != null) {
			String[] lineparts = line.split("\\s+");
			if((!matrixS && lineparts.length != columns)
					|| (matrixS && lineparts.length != 1)){
				logger.error("Invalid matrix file! " + line);
				reader.close();
				throw new IOException("Invalid file");
			}
			if(matrixS)
				for(int col=0;col<lineparts.length;col++) {
					m.set(lineNumber, lineNumber, Double.parseDouble(lineparts[col]));
				}
			else
			for(int col=0;col<lineparts.length;col++) {
				m.set(lineNumber, col, Double.parseDouble(lineparts[col]));
			}
			if(lineNumber > rows)
				logger.error("Longer file! Extra line:" + line);
			lineNumber++;
		}
		reader.close();
		logger.debug(file.getName() + " done! " + lineNumber + " lines processed");
		return m;
	}
	
	public LanczosSVDLIBCUtils() throws IOException {
		baseFolder = Configuration.getTmlFolder() + "/lanczos";
		if(System.getProperty("os.name").startsWith("Windows")) {
			this.osfolder = "windows";
			this.executable = baseFolder + "/" + this.osfolder + "/" + WIN32_EXEC;
			this.newLine = WIN32_NEWLINE;
		}
		else {
			this.osfolder = "linux";
			this.executable = baseFolder  + "/" + this.osfolder + "/" + LINUX_EXEC;
			this.newLine = LINUX_NEWLINE;
		}
	}
	
	public void runLanczos(Corpus corpus, String svdFilename) throws Exception {
		
		this.tmpFolder = new File(baseFolder + "/" + TMP_FOLDER);
		
		// Delete output and matrix files 
		for(File f : (new File(baseFolder + "/" + TMP_FOLDER)).listFiles()) {
			if(f.getName().equals(svdFilename + ".matrix")
					|| f.getName().equals(svdFilename + "-Ut")
					|| f.getName().equals(svdFilename + "-Vt")
					|| f.getName().equals(svdFilename + "-S")) {
				f.delete();
			}
		}
		
		writeCorpusInHBFormat(corpus, this.tmpFolder.getAbsolutePath() + "/" + svdFilename + ".matrix");

		File lanczosExec = new File(this.executable);
		
		String ls_str;

		long time = System.nanoTime();
		String matrixFile = this.tmpFolder.getAbsolutePath() + "/" + svdFilename + ".matrix";
		if(this.osfolder.equals("windows"))
			matrixFile = "\"" + matrixFile + "\"";
		String outFolder = this.tmpFolder.getAbsolutePath() + "/" + svdFilename;
		if(this.osfolder.equals("windows"))
			outFolder = "\"" + outFolder + "\"";
		String linexec = lanczosExec.getAbsolutePath() 
		+ " -d " + corpus.getSemanticSpace().getDimensionsKept() 
		+ " -o " + outFolder 
		+ " -r sth "
		+ " -w dt " + matrixFile;
		logger.debug("Executing: " + linexec);
		Process ls_proc = Runtime.getRuntime().exec(linexec);

		// get its output (your input) stream
		BufferedReader readerInput = new BufferedReader(new InputStreamReader(ls_proc.getInputStream()));
		BufferedReader readerErr = new BufferedReader(new InputStreamReader(ls_proc.getErrorStream()));
		BufferedReader reader = null;

		while(!readerInput.ready() && !readerErr.ready() && (System.nanoTime() - time) <= 10E13);
		
		if((System.nanoTime() - time) > 10E13) {
			logger.error("Timeout trying to execute Lanczos");
			throw new Exception("Timeout trying to execute Lanczos");
		}
		
		if(readerInput.ready()) {
			reader = readerInput;
			readerErr.close();
		} else {
			reader = readerErr;
			readerInput.close();
		}
		
		while (reader.ready() && (ls_str = reader.readLine()) != null) {
			logger.debug(ls_str);
			Pattern pattern = Pattern.compile("^\\s*SINGULAR VALUES FOUND\\s+=\\s*(\\d+)\\s*$");
			Matcher matcher = pattern.matcher(ls_str);
			if(matcher.matches())
				singularvalues = Integer.parseInt(matcher.group(1));
		}

		ls_proc.waitFor();
		time = System.nanoTime() - time;

		logger.debug("Lanczos took " + (time * 10E-9) + " millis");

		u = readDenseMatrix(new File(baseFolder + "/" + TMP_FOLDER + "/" + svdFilename + "-Ut"), corpus).transpose();
		s = readDenseMatrix(new File(baseFolder + "/" + TMP_FOLDER + "/" + svdFilename + "-S"), corpus);
		v = readDenseMatrix(new File(baseFolder + "/" + TMP_FOLDER + "/" + svdFilename + "-Vt"), corpus).transpose();
		
		// Delete output and matrix files 
		for(File f : (new File(baseFolder + "/" + TMP_FOLDER)).listFiles()) {
			if(f.getName().equals(svdFilename + ".matrix")
					|| f.getName().equals(svdFilename + "-Ut")
					|| f.getName().equals(svdFilename + "-Vt")
					|| f.getName().equals(svdFilename + "-S")) {
				f.delete();
			}
		}
	}

	private void writeCorpusInHBFormat(Corpus corpus, String filename) throws Exception {
		FileWriter writer = new FileWriter(new File(filename));
		writer.append("Learning Systems Group University of Sydney                               matrix" + this.newLine);
		writer.append("#" + this.newLine);
		String rowsAndColsline = "rra               ";
		rowsAndColsline += corpus.getTerms().length + "     ";
		rowsAndColsline += corpus.getPassages().length + "     ";
		rowsAndColsline += corpus.getNonzeros() + "     ";
		while(rowsAndColsline.length() < 79)
			rowsAndColsline += " ";
		rowsAndColsline += "0" + this.newLine;
		writer.append(rowsAndColsline);
		writer.append("          (10i8)          (10i8)            (8f10.3)            (8f10.3)" + this.newLine);
		List<Integer> indices = new ArrayList<Integer>();
		List<Integer> termindices = new ArrayList<Integer>();
		List<Double> values = new ArrayList<Double>();
		Matrix m = corpus.getTermDocMatrix();
		if(m.get(0, 0) > 0)
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
				indicesLine += this.newLine;
				writer.append(indicesLine);
				indicesLine = "";
			}
		}
		String termIndicesLine = "             ";
		for(int i=0; i<termindices.size(); i++) {
			termIndicesLine += termindices.get(i) + "   ";
			if(termIndicesLine.length() > 75 || i == termindices.size()-1) {
				termIndicesLine += this.newLine;
				writer.append(termIndicesLine);
				termIndicesLine = "             ";
			}
		}
		String valuesLine = "             ";
		for(int i=0; i<values.size(); i++) {
			valuesLine += (new DecimalFormat("0.000")).format(values.get(i)) + "   ";
			if(valuesLine.length() > 75 || i == values.size()-1) {
				valuesLine += this.newLine;
				writer.append(valuesLine);
				valuesLine = "             ";
			}
		}
		writer.close();
		logger.debug("Matrix file written. " + indices.size() + " indices "
				+ termindices.size() + " term indices " + values.size() + " values");
	}
}
