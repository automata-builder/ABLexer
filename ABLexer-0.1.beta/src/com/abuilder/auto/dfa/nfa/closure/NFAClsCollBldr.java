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

import java.util.ArrayList;
import java.util.TreeSet;

import com.abuilder.auto.dfa.nfa.NFAMachine;
import com.abuilder.auto.dfa.nfa.NFAState;
import com.abuilder.auto.dfa.nfa.NFAConstants.NFAType;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.closure.NFACls.NFAClsType;
import com.abuilder.auto.dfa.toolbox.DFAProduct;

public class NFAClsCollBldr {

	private final DFAProduct product;
	private final NFAMachine nfaMach;
	
	public NFAClsCollBldr(DFAProduct product) {
		this.product = product;
		this.nfaMach = product.getNFAMach();
	}

	public NFAClsList getNFAClsList(int myMxID, TreeSet<NFACard> trChNfaIDs) throws Exception {
	
		NFAClsBldrQue clsBldrQ = new NFAClsBldrQue(product, myMxID, trChNfaIDs);
		NFAClsList nfaClsList = new NFAClsList();
		
		while(clsBldrQ.size() > 0){
			
			NFAClsBldr clsBldr = clsBldrQ.getFirst();
			
			while(clsBldr.size() > 0){
				
				NFACard card = clsBldr.remove();
				NFAState cur = nfaMach.getNFA(card);
				
				setHugger(cur);
				
				if(cur.isEpsEdge()){
					
					addEpsEdge(cur, clsBldrQ);
					
				} else {
					
					clsBldrQ.addSymbolEdge(cur);
				}
			}
			
			clsBldrQ.remove();
			NFACls nfaCls = clsBldr.getNFACls();
			nfaClsList.add(nfaCls);
		}
		
		return nfaClsList;
	}
	
	


	private void addEpsEdge(NFAState cur, NFAClsBldrQue clsBldrQ) throws Exception{
		
		setTrMxID(cur, clsBldrQ);
		
		clsBldrQ.addEpsEdge(cur);
	}
	
	private void setHugger(NFAState cur) throws Exception{
	
		if(NFAType.isRngHead(cur.getType())){
			
			NFAState head = cur;
			NFAState re = head.getNext1();
			re.setHuggerRe(re.nfaCard);
			
			if(cur.getNext2() != null){
				NFAState tail = cur.getNext2();
				tail.setHuggerRe(head.getHuggerRe());
			}
			return;
		}
		
		if(NFAType.isRngTail(cur.getType())){
			NFAState tail = cur;
			NFAState head =  tail.getRange(NFAType.RNG_TL).getHeadNFA();
			tail.setHuggerRe(head.getHuggerRe());
			tail.getNext1().setHuggerRe(tail.getHuggerRe());//Eps->head-tail->Eps
			return;
		}
		
		if(!NFAType.isRngGoback(cur.getType()) && cur.getNext1() != null){//if(cur.type != GB 
			
			cur.getNext1().setHuggerRe(cur.getHuggerRe());
			if(cur.getNext2() != null){
				cur.getNext2().setHuggerRe(cur.getHuggerRe());
			}
		}
	}
	
	
	private void setTrMxID(NFAState cur, NFAClsBldrQue clsBldrQ) throws Exception{
		
		if(cur.getNext1() == null){
			return;
		}
		
		if(NFAType.isRngHead(cur.getType())){
			
			if (!clsBldrQ.getParent().isSimpleType()) {
				
				NFAState reentry = cur.getNext1();
				int myTrMxID = reentry.getHuggerRngID();
				clsBldrQ.addLast(new NFAClsBldr(product, myTrMxID,  reentry.nfaCard, NFAClsType.SIMPLE));
			
			}
			
			if(cur.getNext2() != null){//add Tail for 'minBnd = 0'
				enqueTail(cur.getNext2(), clsBldrQ);
			}
			return;
		}
		
			
		if(NFAType.isRngGoback(cur.getType())){
			
			//Add Reent
			if(cur.getNext2() != null){
		
				NFAState reentry = cur.getNext2();
				int myTrMxID = reentry.getHuggerRngID();
				//int huggerTrMxID = reentry.getRange(NFAType.RNG_RE).getHeadNFA().getHuggerRngID();
				
				clsBldrQ.addLast(new NFAClsBldr(product,  myTrMxID,  reentry.nfaCard, NFAClsType.REENTRY));
			}

			//add Tail
			NFAState tail = cur.getNext1();
			//NFAState head =  tail.getRange(NFAType.RNG_TL).getHeadNFA();

			enqueTail(tail, clsBldrQ);
		}		
	}

	
	private void enqueTail(NFAState tail, NFAClsBldrQue clsBldrQ) throws Exception{
		NFAState head =  tail.getRange(NFAType.RNG_TL).getHeadNFA();
		int myTrMxID = head.getHuggerRngID();//Re
		clsBldrQ.addLast(new NFAClsBldr(product, myTrMxID, tail.nfaCard, NFAClsType.TAIL));
	}

	public static class NFAClsList {
		
		private final ArrayList<NFACls> nfaClsList = new ArrayList<>();

		public void add(NFACls e) {
			nfaClsList.add(e);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("NFAClsList; Size=" + nfaClsList.size());
			for(int i = 0; i < nfaClsList.size(); ++i){
				sb.append("\n" + nfaClsList.get(i));
			}
			return sb.toString();
		}

		public ArrayList<NFACls> getList() {
			return nfaClsList;
		}
	}
}
