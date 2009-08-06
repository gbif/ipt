package org.gbif.iptlite.util;

/**
 Copyright 2005 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.geo.TransformationUtils;
import org.gbif.provider.util.MalformedTabFileException;


/**
 * A very simple CSV reader released under a commercial-friendly license.
 * 
 * @author Glen Smith
 * @author Tim Robertson - this code is unmodified other than package declaration and was extracted from
 * http://opencsv.sourceforge.net/
 */
public class CSVReader implements Iterator<String[]>, Iterable<String[]>{
	private static final String[] popularDelimiters = {"\t","|",",",";"};
	private static final String[] popularEncodings = {"UTF-8","Cp1252","ISO-8859-1","MacRoman","UTF-16","UTF-16BE","UTF-16LE"}; // big/little endian 
	protected static final Log log = LogFactory.getLog(CSVReader.class);

	private String[] header;
	private boolean returnHeader=false; // if this.header needs to be iterated too

    private BufferedReader br;

    private boolean hasNext = true;

    private char separator; // space is being ignored and is used as a NULL substitute

    private char quotechar;
    
    private int skipLines;

    private boolean linesSkiped;

  
    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param line
     *            the line number to skip for start reading 
     * @throws IOException 
     */
    private CSVReader(Reader reader, char separator, char quotechar, int line) throws IOException {
        this.br = new BufferedReader(reader);
        this.separator = separator;
        this.quotechar = quotechar;
        this.skipLines = 0;
        
		// read header row if skip lines is > 1
        if (line > 0){
			header = readNext();
			line--;
			while(line>0){
				readNext();
				line--;
			}
        }else{
			header = readNext();
			returnHeader = true;
        }
    }

    /**
     * Reads the entire file into a List with each element being a String[] of
     * tokens.
     * 
     * @return a List of String[], with each String[] representing a line of the
     *         file.
     * 
     * @throws IOException
     *             if bad things happen during the read
     */
    public List readAll() throws IOException {

        List allElements = new ArrayList();
        while (hasNext) {
            String[] nextLineAsTokens = readNext();
            if (nextLineAsTokens != null)
                allElements.add(nextLineAsTokens);
        }
        return allElements;

    }

    /**
     * Reads the next line from the buffer and converts to a string array.
     * 
     * @return a string array with each comma-separated element as a separate
     *         entry.
     * 
     * @throws IOException
     *             if bad things happen during the read
     */
    public String[] readNext() throws IOException {
    	if (returnHeader){
    		returnHeader=false;
            return header;
    	}else{
        	String nextLine = getNextLine();
            return hasNext ? parseLine(nextLine) : null;
    	}
    }

    /**
     * Reads the next line from the file.
     * 
     * @return the next line from the file without trailing newline
     * @throws IOException
     *             if bad things happen during the read
     */
    private String getNextLine() throws IOException {
    	if (!this.linesSkiped) {
            for (int i = 0; i < skipLines; i++) {
                br.readLine();
            }
            this.linesSkiped = true;
        }
        String nextLine = br.readLine();
        if (nextLine == null) {
            hasNext = false;
        }
        return hasNext ? nextLine : null;
    }

    /**
     * Parses an incoming String and returns an array of elements.
     * 
     * @param nextLine
     *            the string to parse
     * @return the comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException if bad things happen during the read
     */
    private String[] parseLine(String nextLine) throws IOException {

        if (nextLine == null) {
            return null;
        }

        List tokensOnThisLine = new ArrayList();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        do {
        	if (inQuotes) {
                // continuing a quoted section, reappend newline
                sb.append("\n");
                nextLine = getNextLine();
                if (nextLine == null)
                    break;
            }
            for (int i = 0; i < nextLine.length(); i++) {

                char c = nextLine.charAt(i);
                if (c != ' ' && c == quotechar) {
                	// this gets complex... the quote may end a quoted block, or escape another quote.
                	// do a 1-char lookahead:
                	if( inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
                	    && nextLine.length() > (i+1)  // there is indeed another character to check.
                	    && nextLine.charAt(i+1) == quotechar ){ // ..and that char. is a quote also.
                		// we have two quote chars in a row == one quote char, so consume them both and
                		// put one on the token. we do *not* exit the quoted text.
                		sb.append(nextLine.charAt(i+1));
                		i++;
                	}else{
                		inQuotes = !inQuotes;
                		// the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                		if(i>2 //not on the begining of the line
                				&& nextLine.charAt(i-1) != this.separator //not at the begining of an escape sequence 
                				&& nextLine.length()>(i+1) &&
                				nextLine.charAt(i+1) != this.separator //not at the	end of an escape sequence
                		){
                			sb.append(c);
                		}
                	}
                } else if (c == separator && !inQuotes) {
                    tokensOnThisLine.add(sb.toString());
                    sb = new StringBuffer(); // start work on next token
                } else {
                    sb.append(c);
                }
            }
        } while (inQuotes);
        tokensOnThisLine.add(sb.toString());
        return (String[]) tokensOnThisLine.toArray(new String[0]);

    }

    /**
     * Closes the underlying reader.
     * 
     * @throws IOException if the close fails
     */
    public void close() throws IOException{
    	br.close();
    }
    
    
    public static CSVReader buildReader(File source, String encoding, char fieldsTerminatedBy, char fieldsEnclosedBy, int rowsToSkip) throws IOException{
			FileInputStream fis = null; 
			fis = new FileInputStream(source); 
			InputStreamReader in = new InputStreamReader(fis, encoding);		
			CSVReader csvReader = new CSVReader(in, fieldsTerminatedBy, fieldsEnclosedBy, rowsToSkip);//file.getIgnoreHeaderLines());
			return csvReader;
    }
    /**Try to auto-detect character encoding, delimiters and quotation by trying popular values
     * @param source
     * @return
     * @throws IOException 
     */
    public static CSVReader buildReader(File source, int rowsToSkip) throws IOException{
		CSVReader reader = null;
		String encoding=null;
		for (String enc : popularEncodings){
			BufferedReader br=null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(source), enc));
				// read all lines of file to check if encoding is fine
				while(br.readLine()!=null){
					// dont do nothing, just see if we encounter any encoding exceptions
				}
				// encoding seems fine. Now try to discover field delimiters
				encoding = enc;
				break;
			} catch (UnsupportedEncodingException e) {
				// nope, try next encoding
				continue;
			} finally {
				if (br!=null){
					br.close();
				}
			}
		}
		if (encoding==null){
			log.warn("Can't find working character encoding");
			return null;
		}
		
		// now try real CSV reader with popular delimiters, starting with pure TAB and classic CSV first:
		// CSV
		reader = buildReader(source, encoding, ',', '"', rowsToSkip);
		if (testReader(reader)){
			reader.close();
			log.info("CSV reader found with delimiter >,< quotation >\"< and encoding "+encoding);
			return buildReader(source, encoding, ',', '"', rowsToSkip);
		}
		// TAB and others
		for (String dlmt : popularDelimiters){
			reader = buildReader(source, encoding, dlmt.charAt(0), ' ', rowsToSkip);
			if (testReader(reader)){
				reader.close();
				log.info("CSV reader found with delimiter >"+dlmt+"< and encoding "+encoding);
				return buildReader(source, encoding, dlmt.charAt(0), ' ', rowsToSkip);
			}
			reader.close();
		}
		// nothing seems to work... buh
		log.warn("Can't find a working CSV reader");
		return null;
    }
    
    /** make sure the reader has correct delimiter and quotation set.
     * Check first lines and make sure they have the same amount of columns and at least 2
     * @param reader
     * @return
     * @throws IOException 
     */
    private static boolean testReader(CSVReader reader) throws IOException{
    	final int minColumns = 2;
    	int linesToCheck = 25;
    	
		int headerColumnCount = 0;
		while (linesToCheck>0){
			String[] row;
				row = reader.readNext();
				if (row==null){
					break;
				}
				if (row.length < minColumns){
					log.debug("Less columns than min length");
					return false;
				}
				if (headerColumnCount==0){
					// first row
					headerColumnCount = row.length;
				}else{
					//make sure rows have the same number of columns as the header
					if (row.length != headerColumnCount){
						log.debug("Different number of columns than first row");
						return false;
					}
				}
				linesToCheck--;
		}
    	return true;
    }

    
    public static CSVReader buildReader(File source, boolean skipHeaderRow) throws IOException{
		int skipRows = 0;
		if (skipHeaderRow){
			skipRows = 1;
		}
		return buildReader(source, skipRows);
    }
    
    
	public boolean hasNext() {
		return returnHeader || hasNext;
	}

	public String[] next() {
		try {
			return readNext();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}
	
	public String[] getHeader() {
		return header;
	}

	public Iterator<String[]> iterator() {
		return this;
	}
	
}
