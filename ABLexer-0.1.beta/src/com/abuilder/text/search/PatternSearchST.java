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

package com.abuilder.text.search;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * Boyer Moore Algorithm
 *
 */

public class PatternSearchST {

	private static char[] pattern;
	private static final int XSIZE = 512;
	private static int[] goodSuffix = new int[XSIZE];
	private static int[] suffix = new int[XSIZE];
	private static int[] badChar = new int[128];
	private static boolean isCaseSensitive;
	
	private static void setBadChar() {

		for (int i = 0; i < badChar.length; ++i)
			badChar[i] = pattern.length;

		for (int i = 0; i < pattern.length - 1; ++i){
			if(isCaseSensitive){
				badChar[pattern[i]] = pattern.length - i - 1;
			} else {
				badChar[Character.toLowerCase(pattern[i])] = pattern.length - i - 1;
			}
		}
	}

	private static void suffixes() {
		int f = 0, g = 0, i = 0;

		suffix[pattern.length - 1] = pattern.length;
		g = pattern.length - 1;
		
		for (i = pattern.length - 2; i >= 0; --i) {
			
			if (i > g && suffix[i + pattern.length - 1 - f] < i - g){
			
				suffix[i] = suffix[i + pattern.length - 1 - f];
			
			} else {
				
				if (i < g){
					g = i;
				}
				
				f = i;
				
				//while (g >= 0 && pattern[g] == pattern[g + pattern.length - 1 - f]){
				while (g >= 0 && cmp(pattern[g], pattern[g + pattern.length - 1 - f])){
					--g;
				}
				
				suffix[i] = f - g;
			}
		}
	}

	private static void setGoodSuffix() {
		
		suffixes();

		for (int i = 0; i < pattern.length; ++i) {
			goodSuffix[i] = pattern.length;
		}


		for (int i = pattern.length - 1, j = 0; i >= 0; --i){
			
			if (suffix[i] == i + 1){
				
				for (; j < pattern.length - 1 - i; ++j){
					
					if (goodSuffix[j] == pattern.length){
					
						goodSuffix[j] = pattern.length - 1 - i;
					
					}
				}
			}
		}
		
		for (int i = 0; i <= pattern.length - 2; ++i) {
			goodSuffix[pattern.length - 1 - suffix[i]] = pattern.length - 1 - i;
		}
	}

	public static void search(List<Integer> matchOffsets, String pattern, char[] cbuf, final int off, final int len, final boolean isCaseSensitive, final int timesToMatch) {
		search(matchOffsets, pattern.toCharArray(), cbuf, off, len, isCaseSensitive, timesToMatch);
	}
	
	public static void search(List<Integer> matchOffsets, char[] pattern, char[] cbuf, final int off, final int len, final boolean isCaseSensitive, final int timesToMatch) {

		PatternSearchST.pattern = pattern;
		PatternSearchST.isCaseSensitive = isCaseSensitive;
		if(matchOffsets.size() > 0){
			matchOffsets.clear();
		}
		
		if(pattern.length > suffix.length){
			resize();
		}
		
		/* Preprocessing */
		setGoodSuffix();
		setBadChar();

		/* Searching */
		int i, j = 0;
		
		int mcntr = 1;

		if(timesToMatch > -1){
			mcntr = timesToMatch;
		}
		
		while (mcntr > 0 && j <= len - pattern.length) {
			
			for (i = pattern.length - 1; i >= 0 && cmp(pattern[i], cbuf[off + i + j]); --i);
			
			if (i < 0) {
				
				matchOffsets.add(j + off);
				j += goodSuffix[0];
				
				if(timesToMatch > -1){
					--mcntr;
				}
				
			} else {
		
				if(isCaseSensitive){
					j += Math.max(goodSuffix[i], badChar[cbuf[off + i + j]] - pattern.length + 1 + i);
				} else { 
					j += Math.max(goodSuffix[i], badChar[Character.toLowerCase(cbuf[off + i + j])] - pattern.length + 1 + i);
				}
			}
		}
		
		PatternSearchST.pattern = null;
	}

	private static boolean cmp(char c1, char c2){
		if(isCaseSensitive){
			return c1 == c2;
		}
		
		return Character.toLowerCase(c1) == Character.toLowerCase(c2);
	}
	
	private static void resize(){
		int sz = pow2Ceil(pattern.length);
		suffix = new int[sz];
		goodSuffix = new int[sz];
	}
	
	private static int pow2Ceil(int n) {
		return 1 << 32 - Integer.numberOfLeadingZeros(n - 1);
	}
}
