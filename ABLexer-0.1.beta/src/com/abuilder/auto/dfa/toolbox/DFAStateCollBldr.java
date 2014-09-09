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

package com.abuilder.auto.dfa.toolbox;

import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.NFAMachine;
import com.abuilder.auto.dfa.nfa.NFABldr.SStateMap.SSSetter;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.closure.NFACls;
import com.abuilder.util.ABUtil;

public class DFAStateCollBldr {
	
	private final DFAProduct product;
	private final TreeMap<Integer, DFAStateColl> mxID_DfaColl = new TreeMap<Integer, DFAStateColl>();

	public DFAStateCollBldr(DFAProduct product) {
		this.product = product;
	}

	public DFAState add(DFAState dfa) throws Exception{
		DFAStateColl dfaColl = getDFAStateColl(dfa.myMxID);
		if(dfaColl == null){
			dfaColl = new DFAStateColl(dfa.myMxID);
			mxID_DfaColl.put(dfa.myMxID, dfaColl);
		}
		dfaColl.add(dfa);
		return dfa;
	}
	
	public DFAStateColl getDFAStateColl(int trMxID){
		return mxID_DfaColl.get(trMxID);
	}
	
	public DFAState getDFAState(NFACls nfaCls){
		DFAStateColl dfaColl = getDFAStateColl(nfaCls.myMxID);
		if(dfaColl == null){
			return null;
		}
		return dfaColl.getDFAState(nfaCls);
	}
	
	public DFAState getDFAState(int trMxID, int dfaID){
		DFAStateColl dfaColl = getDFAStateColl(trMxID);
		return dfaColl.getDFAState(dfaID);
	}	
	
	public byte[][] getMxID2DfaSsSet(){
		
		byte[][] mxID2DfaSsSet = new byte[mxID_DfaColl.size()][];
		NFAMachine nfaMach = product.getNFAMach();
		int dfaSsByteSz = nfaMach.getSSSetByteSize();
		
		int mxi = -1;
		for(DFAStateColl mxDfaColl : mxID_DfaColl.values()){
			
			++mxi;
			mxID2DfaSsSet[mxi] = new byte[mxDfaColl.size() * dfaSsByteSz];
			
			for(int d = 0; d < mxDfaColl.size(); ++d){
				
				DFAState dfa = mxDfaColl.getDFA(d);
				
				for(NFACard nfaCard : dfa.getNFACards()){
					SSSetter setter = nfaMach.getSSSetter(nfaCard.sID);
					mxID2DfaSsSet[mxi][ ( d * dfaSsByteSz ) + setter.byteIx] |= setter.mask;
				}
			}
		}
		return mxID2DfaSsSet;
	}
	
	public String getMxID2DfaSsSetAsString(){
		
		String nl = ABUtil.getSysLineSep();
		NFAMachine nfaMach = product.getNFAMach();
		
		int dfaSsSetByteSz = nfaMach.getSSSetByteSize();
		String dfaSsSets_S = "private final int dfaSsSetByteSize = " + dfaSsSetByteSz + ";" + nl;
		
		StringBuilder mxID2DfaSs = new StringBuilder();
		mxID2DfaSs.append("private final byte[][] mxID2DfaSsSet = {" + nl);
		
		int mxi = 0;
		for(DFAStateColl mxDfaColl : mxID_DfaColl.values()){
			
			if(mxi++ > 0){
				mxID2DfaSs.append("," + nl);
			}
			
			mxID2DfaSs.append("\t{");
			
			for(int d = 0; d < mxDfaColl.size(); ++d){
				
				if(d > 0){
					mxID2DfaSs.append(", ");
				}
				
				DFAState dfa = mxDfaColl.getDFA(d);
				
				byte[] dfaSsSet = new byte[dfaSsSetByteSz];
				
				for(NFACard nfaCard : dfa.getNFACards()){
					SSSetter setter = nfaMach.getSSSetter(nfaCard.sID);
					dfaSsSet[setter.byteIx] |= setter.mask;
				}
				
				for(int b = 0; b < dfaSsSet.length; ++b){
					if(b > 0){
						mxID2DfaSs.append(", ");
					}
					mxID2DfaSs.append(dfaSsSet[b]);
				}
			}
			
			
			
			mxID2DfaSs.append("}");
		}
		
		mxID2DfaSs.append(nl + "};");
		return dfaSsSets_S + nl + mxID2DfaSs.toString();
	}
}
