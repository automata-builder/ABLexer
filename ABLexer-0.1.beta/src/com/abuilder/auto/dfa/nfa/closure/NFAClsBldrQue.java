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

import java.util.LinkedList;
import java.util.TreeSet;

import com.abuilder.auto.dfa.nfa.NFAState;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.closure.NFACls.NFAClsType;
import com.abuilder.auto.dfa.toolbox.DFAProduct;

public class NFAClsBldrQue {

	private final DFAProduct product;
	public final int myMxID;

	private final LinkedList<NFAClsBldr> nfaClsBldrQ = new LinkedList<NFAClsBldr>();
	private final TreeSet<NFACard> doneIDs = new TreeSet<>();
	private NFAClsBldr parent;
	
	public NFAClsBldrQue(DFAProduct product, int myMxID, TreeSet<NFACard> trChNfaIDs) throws Exception {
		
		this.product = product;
		this.myMxID = myMxID;

		addLast(new NFAClsBldr(product, myMxID, trChNfaIDs, NFAClsType.SIMPLE));
	}

	public NFAClsBldr remove() {
		parent = null;
		return nfaClsBldrQ.remove();
	}

	public int size() {
		return nfaClsBldrQ.size();
	}

	public void addLast(NFAClsBldr clsBldr) throws Exception {
		for(NFACard nfaID : clsBldr.getNfaIDs()){
			if(doneIDs.contains(nfaID)){
				throw new Exception("nfaID="+nfaID+"already exists");
			}
		}
		doneIDs.addAll(clsBldr.getNfaIDs());
		
		if (parent != null) {
			//Add eps tran 
			
			if(parent.isTailType()){
				
				if( clsBldr.isReType()){
					//case (ab(ab){2,3}){2,3}: inner tail -> outer re
					parent.addNext(clsBldr.getNFACls());
					throw new Exception("Error");
				}
			
			} else if(parent.isReType()){
				
				if(clsBldr.isSimpleType()){
				//case ((ab){2,3}|(ab){2,3}){2,3}: outer re -> inner head
					parent.addNext(clsBldr.getNFACls());
					throw new Exception("Error");
				}
			}
			
		}
		nfaClsBldrQ.addLast(clsBldr);
	}
	
	public void insert(int index, NFAClsBldr clsBldr) {
		doneIDs.addAll(clsBldr.getNFACls().getNFACards());
		nfaClsBldrQ.add(index, clsBldr);
	}
	
	public NFAClsBldr getFirst() {
		return parent = nfaClsBldrQ.getFirst();
	}
	
	public void addEpsEdge(NFAState nfa){
		
		if(nfa.getNext1() != null){
			
			queIt(nfa.getNext1().nfaCard);
			
			if(nfa.getNext2() != null){
				queIt(nfa.getNext2().nfaCard);
			}
		}
	}
	

	private void queIt(NFACard nfaID){
		
		if(!doneIDs.contains(nfaID)){
			
			doneIDs.add(nfaID);
			nfaClsBldrQ.getFirst().addNFA(nfaID);
		}
	}
	
	public void addSymbolEdge(NFAState nfa){
		//int dfaColInd = product.getDfaClmnIndex(nfa.getEdge());
		nfaClsBldrQ.getFirst().getNFACls().putOnTrEdgeNfaIDs(nfa.getEdge(), nfa.getNext1());
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("NFAClsBldrQue [myTrMxID=" + myMxID + "]");
		sb.append(", doneIDs=" + doneIDs);
		sb.append("\nnfaClsBldrQ=");
		NFAClsBldr[] bldrs = nfaClsBldrQ.toArray(new NFAClsBldr[nfaClsBldrQ.size()]);
		for(int i = 0; i < bldrs.length; ++i){
			sb.append("\n" + bldrs[i]);
		}
		return sb.toString();
	}

	public NFAClsBldr getParent() {
		return parent;
	}
}
