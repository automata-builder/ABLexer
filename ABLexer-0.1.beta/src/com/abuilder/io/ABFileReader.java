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

package com.abuilder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ABFileReader {

	public static char[] read(File file) throws IOException{ 
		
		BufferedReader br = null;
		
		try {
			
			char[] cbuf = new char[(int) file.length()];
			br = new BufferedReader(new FileReader(file));
			br.read(cbuf);	
			return cbuf;
				
		} finally{
			
			if(br != null){
				try {
					br.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	public static char[] readWithoutCarriageReturn(File file) throws IOException{
		
		
		//assure(file);
		
		BufferedReader br = null;
		
		try {
			
			char[] cbuf = new char[(int) file.length()];
			br = new BufferedReader(new FileReader(file));
			
			int got = 0, ch;
			while(((ch = br.read()) != -1)){
				if(ch != '\r'){
					cbuf[got++] = (char)ch;
				}
			}
			
			if(got < cbuf.length){
				char[] temp = new char[got];
				System.arraycopy(cbuf, 0, temp, 0, got);
				cbuf = temp;
			}
			
			return cbuf;
			
	
		} finally{
			if(br != null){
				try {
					br.close();
				} catch (Exception e) {

				}
			}
		}
	}
//	
//	private static void assure(File file) throws IOException{
//		if(!file.exists()){ 
//			throw new IOException("File {"+file.toPath()+"} doesn't exist");
//		}
//
//		if(!file.isFile()){
//			throw new IOException("File {"+file.toPath()+"} is not a normal file");
//		}
//		
//		if(!file.canRead()){
//			throw new IOException("File {"+file.toPath()+"} is not readable");
//		}
//	}
}
