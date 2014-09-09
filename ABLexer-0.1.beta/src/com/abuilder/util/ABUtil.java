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

package com.abuilder.util;

import java.text.DateFormat;
import java.util.Date;

public class ABUtil {
	
	private static final String operSystem = System.getProperty("os.name").toLowerCase();
	private static final String fileSeparator = System.getProperty("file.separator");
	private static final String lineSeparator = System.getProperty("line.separator");
	
	public static boolean isOperSysCaseSens(){
		if(isWindows() || isMac()){
			return false;
		}
		return true;
	}
	
	public static boolean isWindows() {
		return (operSystem.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (operSystem.indexOf("mac") >= 0);

	}

	public static boolean isUnix() {
		return (operSystem.indexOf("nix") >= 0 || operSystem.indexOf("nux") >= 0);
	}

	public static boolean isSolaris() {
		return (operSystem.indexOf("sunos") >= 0);
	}
	
	public static boolean isValidFolderNameChar(char ch){
		
		switch(ch){
		case '/':
		case '\\':
		case ':':
		case '*':
		case '?':
		case '"':
		case '<':
		case '>':
		case '|':
			return false;
			
		default:
			return true;
		}
	}

	public static String getSysFileSep() {
		return fileSeparator;
	}
	
	public static String substDot4FileSeparator(String path){
		char[] cbuf = new char[path.length()];
		for (int i = 0; i < path.length(); i++) {
			char ch = path.charAt(i);
			if(ch == '.'){
				cbuf[i] = ABUtil.getSysFileSep().charAt(0);
			} else {
				cbuf[i] = ch;
			}
		}
		return new String(cbuf);
	}
	
	public static String getTimeStamp(){
		DateFormat df  = DateFormat.getDateTimeInstance();
		return df.format(new Date());
	}

	public static String getSysLineSep() {
		return lineSeparator;
	}
	
}
