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

package com.abuilder.auto.dfa.nfa.rx_range;

import java.util.Arrays;

import com.abuilder.auto.dfa.nfa.NFAState;
import com.abuilder.auto.dfa.nfa.NFAConstants.NFAType;

public class RxRange implements Comparable<RxRange>{//Regular Expression Range
	
	public static final int INFINITY_BOUND = -2;
	
	private static final int HEAD = 0;
	private static final int REENTRY = 1;
	private static final int GOBACK = 2;
	private static final int TAIL = 3;
	
	public final int rngID;
	private final int[] bounds;
	private final NFAState[] nfas = new NFAState[4];
	
	
	@Override
	public int compareTo(RxRange other) {
		if(this.rngID < other.rngID)
			return -1;
		if(this.rngID == other.rngID)
			return 0;
		return 1;
	}
	
	public RxRange(int rngID, NFAState head, NFAState reent, NFAState goback, NFAState tail, int[] bounds) {
		
		this.rngID = rngID;
		
		nfas[HEAD] = head; 
		head.setType(NFAType.RNG_HD); 					
		head.setRange(this, NFAType.RNG_HD);
		
		nfas[REENTRY] = reent; 
		reent.setType(NFAType.RNG_RE); 	
		reent.setRange(this, NFAType.RNG_RE);
		
		nfas[GOBACK] = goback; 
		goback.setType(NFAType.RNG_GB); 	
		goback.setRange(this, NFAType.RNG_GB);
		
		nfas[TAIL] = tail; 
		tail.setType(NFAType.RNG_TL); 
		tail.setRange(this, NFAType.RNG_TL);
		this.bounds = bounds;
	}

	public NFAState getHeadNFA(){
		return nfas[HEAD];
	}
	
	public NFAState getReNFA(){
		return nfas[REENTRY];
	}
	
	public int getReID(){
		return getReNFA().getNfaID();
	}
	
	public NFAState getGbNFA(){
		return nfas[GOBACK];
	}
	
	public int getGbID(){
		return nfas[GOBACK].getNfaID();
	}
	
	public NFAState getTailNFA(){
		return nfas[TAIL];
	}
	
	public void setHead(NFAState nfa){
		nfas[HEAD] = nfa;
		nfa.setRange(this, NFAType.RNG_HD);
	}
	
	public NFAState[] getNFAs(){
		return nfas;
	}

	public boolean isMaxBndInf(){
		return bounds[1] == INFINITY_BOUND;
	}
	
	public void setMinBound(int min){
		bounds[0] = min;
	}
	
	public void setMaxBnd(int max){
		bounds[1] = max;
	}

	public int getMinBnd(){
		return bounds[0];
	}
	
	public int getMaxBnd(){
		return bounds[1];
	}
		

	public int hashCode() {
		return rngID;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RxRange other = (RxRange) obj;
		if (rngID != other.rngID)
			return false;
		return true;
	}

	public int getRuleID(){
		return getHeadNFA().getRuleID();
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RxRange [rngID=");
		sb.append(rngID);
		sb.append(", reID=" + getReID());
		sb.append(", bounds=");
		sb.append(Arrays.toString(bounds));
		sb.append(", ruleID=" + getRuleID() );
		sb.append("]");
		return sb.toString();
	}

	public int getRngID() {
		return rngID;
	}

	public int[] getBounds() {
		return bounds;
	}
}
