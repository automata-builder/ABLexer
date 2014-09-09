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

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.rx_range.RxRange;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.util.ABUtil;

public class RxRangeMap {
	
	private final HashMap<Integer, RxRange> reID_RxRange = new HashMap<Integer, RxRange>();
	private final TreeMap<Integer, Integer> rngID_reID = new TreeMap<Integer, Integer>();
	
	private int nextMxID = DFABaseMatrix.RANGE_INIT_MXID;
	
	public RxRange add(NFAState head, NFAState reentry, NFAState goback, NFAState tail, int[] bnds)  {
		RxRange rng = new RxRange(nextMxID++, head, reentry, goback, tail, bnds);
		reID_RxRange.put(reentry.getNfaID(), rng);
		rngID_reID.put(rng.rngID, reentry.getNfaID());
		return rng;
	}
	
	
	
	public RxRange getWithReID(int reID){
		return reID_RxRange.get(reID);
	}
	
	public RxRange getWithRngID(int rngID){
		int reID = rngID_reID.get(rngID);
		return getWithReID(reID);
	}

	public int size() {
		return reID_RxRange.size();
	}

	public Collection<RxRange> values() {
		return reID_RxRange.values();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RxRangeMap: Size=" + reID_RxRange.size());
		for(Integer reID: rngID_reID.values()){
			sb.append("\n" + reID_RxRange.get(reID));
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public short[] getMxID2RangeBounds(){
		short[] mxID2RangeBnds = new short[rngID_reID.size() * 2];
		int i = 0;
		for(Integer rngID: rngID_reID.keySet()){
			RxRange r = getWithRngID(rngID);
			mxID2RangeBnds[i++] = (short) r.getMinBnd();
			mxID2RangeBnds[i++] = (short) r.getMaxBnd();
		}
		return mxID2RangeBnds;
	}
	
	public String getMxID2RangeBoundsAsString(){
		
		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder();
		sb.append("private final short[] mxID2RangeBnd = {"+nl+"\t");
		int i = 0;
		for(Integer rngID: rngID_reID.keySet()){
			if(i++ > 0){
				sb.append(", ");
			}
			RxRange r = getWithRngID(rngID);
			sb.append(r.getMinBnd() + ", " + r.getMaxBnd());
		}
		sb.append(nl + "};");
		return sb.toString();
	}
}