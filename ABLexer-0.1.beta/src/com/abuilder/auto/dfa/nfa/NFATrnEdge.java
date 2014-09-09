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

package com.abuilder.auto.dfa.nfa;

import java.util.BitSet;

import com.abuilder.auto.dfa.nfa.Alphabet.Ascii;

public abstract class NFATrnEdge implements Comparable<NFATrnEdge>{

	public enum EdgeType {
		SIMPLE,
		CHAR_CLASS
	}
	public final int edgeID;
	private final EdgeType type;
	
	public static final int INVALID_CHR_ID = -1; 
	private int chID = INVALID_CHR_ID;
	
	public int getChID() {
		return chID;
	}

	public void setChID(int chID) {
		this.chID = chID;
	}
	
	public NFATrnEdge(int edgeID, EdgeType type) {
		this.edgeID = edgeID;
		this.type = type;
	}

	public EdgeType getType() {
		return type;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + edgeID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NFATrnEdge other = (NFATrnEdge) obj;
		if (edgeID != other.edgeID)
			return false;
		return true;
	}

	public abstract boolean isEpsChar();


	/**
	 * 
	 * @author mike
	 *
	 */
	public static class SimpleEdge extends NFATrnEdge {

		private final int ch;
		
		public SimpleEdge(int edgeID, int ch) {
			super(edgeID, EdgeType.SIMPLE);
			this.ch = ch;
		}

		public int getChar() {
			return ch;
		}

		@Override
		public String toString() {
			return Ascii.toString(ch);
		}

		@Override
		public boolean isEpsChar() {
			return ch == Alphabet.CHR.EPS_CHR;
		}
	}
	

	@Override
	public int compareTo(NFATrnEdge other) {
		if(this.edgeID < other.edgeID){
			return -1;
		}
		if(this.edgeID == other.edgeID){
			return 0;
		}
		return 1;
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class CharClassEdge extends NFATrnEdge {

		private final boolean isNegate;
		private final BitSet orgSet, usedSet;
	
		public CharClassEdge(int edgeID, BitSet orgSet, boolean isNegate) {
			super(edgeID, EdgeType.CHAR_CLASS);
			this.orgSet = orgSet;
			this.isNegate = isNegate;
			
			if(isNegate){
				this.usedSet = new BitSet(128);
				this.usedSet.set(0, 128);
				for(int i = orgSet.nextSetBit(0); i >= 0; i = orgSet.nextSetBit(i+1)){
					this.usedSet.set(i, false);
				}
			} else {
				this.usedSet = orgSet;
			}
		}

		public BitSet getUsedBitSet() {
			return usedSet;
		}
		
		public BitSet getOrgSet() {
			return orgSet;
		}	
		
		public int nextSetBit(int fromIndex) {
			return usedSet.nextSetBit(fromIndex);
		}

		public boolean get(int bitIndex) {
			return usedSet.get(bitIndex);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("CharClassEdge [isNegate=");
			sb.append(isNegate);
			sb.append(", orgSet=[");
			int cnt = 0;
			for(int i = orgSet.nextSetBit(0); i >= 0; i = orgSet.nextSetBit(i+1)){
				if(cnt > 0){
					sb.append(",");
				}
				sb.append(" '" + (char)i + "'");
				++cnt;
			}
			sb.append(" ]");
			return sb.toString();
		}

		@Override
		public boolean isEpsChar() {
			return false;
		}

		public boolean isNegate() {
			return isNegate;
		}

	
	}
}
