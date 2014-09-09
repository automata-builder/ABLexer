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

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.abuilder.auto.dfa.nfa.NFATrnEdge;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.closure.NFACls;
import com.abuilder.auto.dfa.nfa.closure.NFACls.NFAClsType;

public class DFAState {
	
	public interface DFAID {
		int INVALID_DFAID = Short.MIN_VALUE;
		int REENTRY_DFAID = Short.MAX_VALUE;//Accept rules go to dfamx as negative numbers
	}
	
	private final DFAProduct product;
	public final int myMxID;
	public final int dfaID;
	private final NFACls nfaCls;
	
	public final int one4AllDfaID;
	
	public DFAState(DFAProduct product, NFACls nfaCls) {
		this.product = product;
		this.myMxID = nfaCls.myMxID;
		this.dfaID = product.getNextDfaID(myMxID);
		this.nfaCls = nfaCls;
		
		this.one4AllDfaID = product.getNextOne4AllDfaID();
	}

	public Set<NFATrnEdge> getTrChIDs() {
		return nfaCls.getTrEdgeSet();
	}


	public NFACls getNfaCls() {
		return nfaCls;
	}

	public DFAProduct getProduct() {
		return product;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DFAState [myTrMxID=");
		sb.append(myMxID);
		sb.append(", dfaID=");
		sb.append(dfaID);
		sb.append(", one4AllDfaID=" + one4AllDfaID);
		sb.append("\nnfaCls=");
		sb.append(nfaCls);
		sb.append("]");
		return sb.toString();
	}

	public boolean isAccept() {
		return nfaCls.isAccept();
	}

	public NFAClsType getNFAClsType() {
		return nfaCls.getNFAClsType();
	}

	public TreeMap<Integer, TreeSet<NFACard>> getTrMxID_NfaIDs(NFATrnEdge onEdge) {
		return nfaCls.getTrMxID_NfaIDs(onEdge);
	}

	public boolean isSimpleType() {
		return nfaCls.isSimpleType();
	}

	public boolean isReType() {
		return nfaCls.isReType();
	}

	public boolean isTailType() {
		return nfaCls.isTailType();
	}

	public int getMyTrMxID() {
		return myMxID;
	}

	public int getOne4AllDfaID() {
		return one4AllDfaID;
	}

	public TreeSet<NFACard> getNFACards() {
		return nfaCls.getNFACards();
	}	
}
