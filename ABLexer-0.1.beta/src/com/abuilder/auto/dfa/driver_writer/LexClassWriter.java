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

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.abuilder.auto.dfa.driver_writer.LexSkeletonReader.Span;
import com.abuilder.io.ABFileWriter;
import com.abuilder.parser.toolbox.ScriptBuilder;
import com.abuilder.util.ABUtil;

public class LexClassWriter {

	private final ScriptBuilder scBldr;
	private final File outfile;
	private final DFAAutoWriter autoWriter;
	//private final Logger logger = Logger.getLogger(getClass().getName());
  private final LexSkeletonReader skeletonReader;
	private final String nl = ABUtil.getSysLineSep();
	
	public LexClassWriter(File outfile, ScriptBuilder scBldr) throws Exception {
		this.scBldr = scBldr;
		this.outfile = outfile;
		this.skeletonReader = new LexSkeletonReader();
		this.autoWriter = new DFAAutoWriter(scBldr.getDfaProd(), scBldr);
	}

	public void write(boolean isDebug) throws Exception{
		ABFileWriter.write(outfile, writeLexClass(isDebug));
	}
	
	private char[] writeLexClass(boolean isDebug) throws Exception{
		
		CharArrayWriter wr = new CharArrayWriter(scBldr.getCBuf().length);

		writeTimeStamp(wr);
				
		writeTopJavaClassHead(wr);
		
	  writeSkeletonFields(wr, isDebug);
	  
	  writeStateClass(wr);
	  
	  writeLoopMethDeclr(wr);
	  
	  writeLoopMethodBody(wr);
	  
	  writeLoopMethodActionSwitch(wr);
	  
	  writeSkeletonBody(wr);
	
	  writeDfaAuto(wr);
	  
	  writeTopJavaClassBody(wr);

	  return wr.toCharArray();
	}

	private void writeTimeStamp(CharArrayWriter wr) throws IOException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		wr.write("//Creation Time: " + dateFormat.format(cal.getTime()) + nl);
	}
	
	private void writeStateClass(CharArrayWriter wr) throws IOException{
		wr.write(scBldr.getStateClassAsString());
	}
	
	private void writeDfaAuto(CharArrayWriter wr) throws Exception {
		wr.write("class DfaAutoMx {" + nl);
		wr.write(autoWriter.getDfaAutoMxClass());
		wr.write("}" + nl);
	}
	
	private void writeTopJavaClassHead(CharArrayWriter wr) throws IOException{
		//from cbuf[0] to  "top class declaration left bracket '{', inclusive
		
		int pos2 = scBldr.getStartTokenCBufPos(scBldr.getLeftMost());
		wr.write(scBldr.getCBuf(), 0, pos2);
		
		writeImports(wr);
		
		int pos3 = 1 + scBldr.getStartTokenCBufPos(scBldr.getTopClassBodySpan().getStart());
		wr.write(scBldr.getCBuf(), pos2, pos3 - pos2);
	}
	
	private void writeImports(CharArrayWriter wr) throws IOException{
		wr.write(nl);
		wr.write("import java.io.BufferedReader;" + nl);
		wr.write("import java.io.File;" + nl);
		wr.write("import java.io.FileReader;" + nl);
		wr.write("import java.io.InputStream;" + nl);
		wr.write("import java.util.ArrayDeque;" + nl);
		wr.write("import java.util.ArrayList;" + nl);
		wr.write("import java.util.Arrays;" + nl);
		wr.write("import java.util.HashMap;" + nl);
		wr.write("import java.util.HashSet;" + nl);
		wr.write("import java.util.Iterator;" + nl);
		wr.write("import java.util.List;" + nl);
		wr.write("import java.util.Map;" + nl);
		wr.write("import java.util.Set;" + nl);
		wr.write("import java.util.Map.Entry;" + nl);
		wr.write("import java.util.BitSet;" + nl);
		wr.write("import java.io.IOException;" + nl);
		wr.write("import java.io.FileNotFoundException;" + nl);
		wr.write("import java.io.InputStreamReader;" + nl);
		
		wr.write(nl);
	}
	
	private void writeSkeletonFields(CharArrayWriter wr, boolean isDebug) throws IOException{
		
		wr.write(nl + nl + "\t" + "private boolean debugMode = "+(isDebug?"true":"false")+";" + nl);
		Span f = skeletonReader.getFieldSpan();
		wr.write(nl + nl + "//");
		wr.write(skeletonReader.getCBuf(), f.offset, f.len);
		wr.write(nl + nl);
	}
	

	
	private void writeLoopMethodBody(CharArrayWriter wr) throws IOException{
		Span f = skeletonReader.getLoopMethodSpan();
		wr.write(nl + nl + "//");
		wr.write(skeletonReader.getCBuf(), f.offset, f.len);
		wr.write(nl + nl);
	}
	
	private void writeLoopMethodActionSwitch(CharArrayWriter wr) throws IOException {
		
		wr.write(autoWriter.getActionSwitch());
		
	//add 'return null;' if return type is not 'void'
		
		wr.write(nl + "\t\treturn null;");
		wr.write(nl + "\t}" + nl + nl);
	}
	
	private void writeSkeletonBody(CharArrayWriter wr) throws IOException{
		Span f = skeletonReader.getBodySpan();
		wr.write(nl + nl + "//");
		wr.write(skeletonReader.getCBuf(), f.offset, f.len);
		wr.write(nl + nl);
	}
	
	private void writeTopJavaClassBody(CharArrayWriter wr){
		//from "top class declaration left bracket '{', exclusive
		
		int bodyStart = 1 + scBldr.getStartTokenCBufPos(scBldr.getTopClassBodySpan().getStart());

		//Exclude Lex script closure
		int scriptClosStart = scBldr.getStartTokenCBufPos(scBldr.getLexClosSpan().getStart());
		int scriptClosEnd = 1 + scBldr.getEndTokenCBufPos(scBldr.getLexClosSpan().getEnd());
			
		wr.write(scBldr.getCBuf(), bodyStart, scriptClosStart - bodyStart);
		wr.write(scBldr.getCBuf(), scriptClosEnd, scBldr.getCBuf().length - scriptClosEnd);
	}
	
	private void writeLoopMethDeclr(CharArrayWriter wr) throws IOException{
	 
		int pos1 = scBldr.getStartTokenCBufPos(scBldr.getLexClosSpan().getStart());
		pos1 += "@@".length();
		int pos2 = scBldr.getStartTokenCBufPos(scBldr.getLexMethDeclrSpan().getEnd());
		wr.write(scBldr.getCBuf(), pos1, pos2 - pos1);
    wr.write(" throws Exception {");
    
    if(scBldr.getLexMethInitSpan() != null){
    	
    	pos1 = 1 + scBldr.getStartTokenCBufPos(scBldr.getLexMethInitSpan().getStart());
    	pos2 = scBldr.getEndTokenCBufPos(scBldr.getLexMethInitSpan().getEnd());
   
    	wr.write(nl);
    	wr.write(scBldr.getCBuf(), pos1, pos2 - pos1);
    	wr.write(nl);
    }
	}
}
