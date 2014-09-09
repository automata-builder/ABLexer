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


/**
 * 
 * Boyer Moore Algorithm
 *
 */
public class PatternSearch {
	
	private final char[] pattern, text;
	private final int textoff, textlen;
	private final int[] goodSuffix;
	private final int[] suffix;
	private final int[] badChar = new int[128];
	private int _pi, _ti;
	private boolean noMore = false;
	
	public PatternSearch(char[] pattern, char[] text, int off, int len) {
		this.pattern = pattern;
		this.text = text;
		this.textoff = off;
		this.textlen = len;
		int sz = pow2Ceil(pattern.length);
		this.suffix = new int[sz];
		this.goodSuffix = new int[sz];
		/* Preprocessing */
		setGoodSuffix();
		setBadChar();
	}
	
	
	private static int pow2Ceil(int n) {
		return 1 << 32 - Integer.numberOfLeadingZeros(n - 1);
	}
	
	
	private void setBadChar() {

		for (int i = 0; i < badChar.length; ++i)
			badChar[i] = pattern.length;

		for (int i = 0; i < pattern.length - 1; ++i)
			badChar[pattern[i]] = pattern.length - i - 1;
	}

	private void suffixes() {
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
				
				while (g >= 0 && pattern[g] == pattern[g + pattern.length - 1 - f]){
					--g;
				}
				
				suffix[i] = f - g;
			}
		}
	}

	private void setGoodSuffix() {
		
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
	
	public int getNextMatch() throws Exception {
		
		if(noMore){
			throw new Exception("Searched to the end");
		}
		
		while (_ti <= textlen - pattern.length) {
			
			for (_pi = pattern.length - 1; _pi >= 0 && pattern[_pi] == text[textoff + _pi + _ti]; --_pi);
			
			if (_pi < 0) {
				
				//System.out.println("At index: " + (j + textoff));
				int mix = _ti;
				_ti += goodSuffix[0];
				return textoff + mix;
				
			
			} else {
		
				_ti += Math.max(goodSuffix[_pi], badChar[text[textoff + _pi + _ti]] - pattern.length + 1 + _pi);
			}
		}
		
		noMore = true;
		return -1;
	}
}
