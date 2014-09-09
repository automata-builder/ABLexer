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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ABFileWriter {

	
	public static void write(File file, String content) throws IOException{
		write(file, content.toCharArray(), 0, content.length());
	}
	
	public static void write(String path, char[] cbuf, int start, int length) throws IOException{
		write(new File(path), cbuf, start, length); 
	}
	
	public static void write(File file, char[] cbuf) throws IOException{
		write(file, cbuf, 0, cbuf.length);
	}
	
	public static void write(File file, char[] cbuf, int start, int length) throws IOException{
		
		String prnt = file.getParent();
		File prntDir = new File(prnt);
		if(!prntDir.exists()){
			
			if(!prntDir.mkdirs()){
				throw new IOException("Failed to create parent folder{"+prntDir.getCanonicalPath()+"} for file{"+file.getName()+"}");
			}
		}
		
		BufferedWriter bw = null;
		
		try {
			
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(cbuf, start, length);
			
		} finally {
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
