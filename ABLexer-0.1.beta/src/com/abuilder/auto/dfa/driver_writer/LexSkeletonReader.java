/*
 * [The "BSD license"]
 *  Copyright (c) 2013 Michael Epshteyn
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.abuilder.auto.dfa.driver_writer;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import com.abuilder.config.AppConfig;
import com.abuilder.io.ABFileReader;
import com.abuilder.text.search.PatternSearchST;

public class LexSkeletonReader {

	private final String PREFIX = "#LEX_";
	private final String START_SUFFIX = "_START#";
	private final String END_SUFFIX = "_END#";
	
	private final String IMPORT_SECTION = "IMPORT";
	private final String FIELD_SECTION = "FIELD";
	private final String LOOP_METHOD_SECTION = "LOOP_METHOD";
	private final String BODY_SECTION = "BODY";
	
	private final ArrayList<Integer> match = new ArrayList<>();
	private final char[] cbuf;
	private final Span importSpan, fieldSpan, loopMethSpan, bodySpan;
	
	public LexSkeletonReader() throws Exception{
		
		this.cbuf = getSkeleton();		
		this.importSpan = getSectionSpan(IMPORT_SECTION);
		this.fieldSpan = getSectionSpan(FIELD_SECTION);
		this.loopMethSpan = getSectionSpan(LOOP_METHOD_SECTION);
		this.bodySpan = getSectionSpan(BODY_SECTION);
	}
	
  private char[] getSkeleton() throws IOException{
  	
  	InputStream in = LexSkeletonReader.class.getResourceAsStream(AppConfig.LEXER_SKELETON_PATH);
  	CharArrayWriter wr = new CharArrayWriter();
  	
  	try(BufferedReader br = new BufferedReader(new InputStreamReader(in))){
  		
  		int i;
  		while((i = br.read()) != -1){
  			wr.write(i);
  		}
  	} 

  	return wr.toCharArray();
  }
  
  private Span getSectionSpan(String section) throws Exception{
  	
  	String pattern = PREFIX + section + START_SUFFIX;
  	
  	int offset = getPatternOffset(pattern);
  	
  	pattern = PREFIX + section + END_SUFFIX;
  	
  	int end = getPatternOffset(pattern) + pattern.length();
  
  	return new Span(offset, end);
  }
  
  private int getPatternOffset(String pattern) throws Exception{
  	
  	PatternSearchST.search(match, pattern, cbuf, 0, cbuf.length, true, 1);
  	
  	if(match.size() == 0){
  		String errMsg = "Failed to find lex skeleton separator <"+ pattern + ". File is corrupted.";
  		throw new Exception(errMsg);
  	}
  	
  	return match.get(0);
  }
  
  /**
   * 
   * @author mike
   *
   */
  public static class Span {
  	
  	public final int offset, len;

		public Span(int offset, int end) {
			this.offset = offset;
			this.len = end - offset;
		}
  }

	public char[] getCBuf() {
		return cbuf;
	}


	public Span getImportSpan() {
		return importSpan;
	}


	public Span getFieldSpan() {
		return fieldSpan;
	}


	public Span getBodySpan() {
		return bodySpan;
	}


	public Span getLoopMethodSpan() {
		return loopMethSpan;
	}
}
