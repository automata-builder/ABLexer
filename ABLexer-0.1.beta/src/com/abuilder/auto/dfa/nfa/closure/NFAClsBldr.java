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

package com.abuilder.auto.dfa.nfa.closure;

import java.util.ArrayDeque;
import java.util.TreeSet;

import com.abuilder.auto.dfa.nfa.NFAState;
import com.abuilder.auto.dfa.nfa.NFAConstants.NFAType;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.closure.NFACls.NFAClsType;
import com.abuilder.auto.dfa.toolbox.DFAProduct;

public class NFAClsBldr {

	private final DFAProduct product;

	public final int myTrMxID;
	private final ArrayDeque<NFACard> nfaIdQue = new ArrayDeque<>();
	private final NFACls nfaCls;
	
	public NFAClsBldr(DFAProduct product, int myTrMxID, TreeSet<NFACard> trChNfaIDs, NFAClsType type) {
		this.product = product;
		this.myTrMxID = myTrMxID;
		nfaIdQue.addAll(trChNfaIDs);
		this.nfaCls = new NFACls(product, myTrMxID,trChNfaIDs, type);
	}


	public NFAClsBldr(DFAProduct product, int myTrMxID, NFACard prnNfaID, NFAClsType type) {
		this.product = product;
		this.myTrMxID = myTrMxID;		
		nfaIdQue.add(prnNfaID);
		this.nfaCls = new NFACls(product, myTrMxID, prnNfaID, type);
	}
	
	
	public void addNext(NFACls next) throws Exception{
		nfaCls.addNext(next);
	}
	
	public void addNFA(NFACard card){
		nfaCls.addNFA(card);
		nfaIdQue.add(card);
	}
	
	public NFACard remove() {
		return nfaIdQue.remove();
	}

	public int size() {
		return nfaIdQue.size();
	}
	
	
	public void queEpsEdge(NFAState nfa){
		if(nfa.getNext1() != null){
			
			nfaIdQue.add(nfa.getNext1().nfaCard);
			
			if(nfa.getNext2() != null){
				nfaIdQue.add(nfa.getNext2().nfaCard);
			}
		}
	}

	public NFACls getNFACls() {
		return nfaCls;
	}

	public NFAClsType getNfaClsType() {
		return nfaCls.getNFAClsType();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("NFAClsBldr [myTrMxID=");
		sb.append(myTrMxID);
		//sb.append(", huggerTrMxID=" + huggerTrMxID);
		sb.append(", nfaIdQ=");
		sb.append(nfaIdQue);
		sb.append(", nfaCls=");
		sb.append(nfaCls);
		sb.append("]");
		return sb.toString();
	}

	public TreeSet<NFACard> getNfaIDs() {
		return nfaCls.getNFACards();
	}


	
	public int getMyTrMxID() {
		return myTrMxID;
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


//	public void setNext(NFACls next) {
//		nfaCls.setNext(next);
//	}


}
