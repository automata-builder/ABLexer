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

import com.abuilder.auto.dfa.nfa.NFAConstants.NFAType;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.parser.toolbox.LexCharClass;
import com.abuilder.parser.toolbox.LexRule;

public class NFAMachHT {// NFA Machine Head, Tail

	public NFAState head;// machine head
	public NFAState tail;// machine tail
	public final int rID, ssID;
	
	public NFAMachHT(DFAProduct dfaProd, LexRule lxRule) {
		this(dfaProd, lxRule.getRxRID(), lxRule.getSID());
	}
	
	public NFAMachHT(DFAProduct prod, int rID, int sID) {
		this.rID = rID; this.ssID = sID;
		head = new NFAState(prod, rID, sID);
		tail = new NFAState(prod, rID, sID);
		head.setNext1(tail);
	}

	public NFAMachHT(NFAState head, NFAState tail){
		this.rID = head.getRuleID(); 
		this.ssID = head.getSStateID();
		this.head = head;
		this.tail = tail;
	}
	
	public void insertHT(NFAMachHT other) {
		this.head.setNext1(other.head);
		other.tail.setNext1(this.tail);
	}

	public void setHT(NFAMachHT other) {
		this.head = other.head;
		this.tail = other.tail;
	}

	void _setHeadEdge(int edge) {
		head.setEdge(edge);
	}

	public void setHeadEdge(String s) {
		
		if(s.length() == 0){
			return;
		}
		
		head.setEdge(s.charAt(0));
		
		for(int i = 1; i < s.length(); ++i){
			
			tail.setNext1(new NFAState(tail.getDfaProd(), tail.getRuleID(), tail.getSStateID()));
			tail.setEdge(s.charAt(i));
			tail = tail.getNext1();
		}
	}
	
	public void setHeadEdge(LexCharClass cc) {
		head.setEdge(cc.getBitSet(), cc.isNegate());
	}
	
	public void setTailEdge(int edge) {
		tail.setEdge(edge);
	}

	public boolean isRxRange() {
		return head.getType() == NFAType.RNG_HD && tail.getType() == NFAType.RNG_TL;
	}

	public int getReNfaID() {
		return head.getNext1().getNfaID();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("NfaMachHT [rID="+rID+", ssID="+ssID+", mHead=");
		sb.append(head.getNfaID());
		sb.append(", mTail=");
		sb.append(tail.getNfaID());
		sb.append("]");
		return sb.toString();
	}
}