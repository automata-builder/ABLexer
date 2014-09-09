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

import java.util.*;

import com.abuilder.auto.dfa.nfa.NFAConstants.*;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.CharClassEdge;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.EdgeType;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.SimpleEdge;
import com.abuilder.auto.dfa.nfa.rx_range.RxRange;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.exception.MyLogicException;

public class NFAState implements Comparable<NFAState>{
	
	public static final int INVALID_NFAID = -1; 
	private final NFAMachine nfaMach;
	public final NFACard nfaCard;
	
	private NFACard huggerRe;
	private int huggerRngID = DFABaseMatrix.BASE_MXID;
	private RxRange[] ranges;
	private NFAState[] next = new NFAState[2];
	private RuleAccept accept;
	private NFATrnEdge edge;
	private int nfaType = NFAType.NONE;

		
	public NFAState(DFAProduct dfaProd, int ruleID, int sSteID) {
		this.nfaMach = dfaProd.getNFAMach();
		this.nfaCard = new NFACard(nfaMach.getNextNfaID(), ruleID, sSteID);
		nfaMach.addNFA(this);
	}
	
	public void memCpy(NFAState src){
		this.edge = src.edge;
		this.next = src.next;
		this.accept = src.accept;
		this.nfaType |= src.nfaType;
	}
	
	public NFAState getNext1() {
		return next[0];
	}
	
	public NFAState getNext2() {
		return next[1];
	}
	
	public NFAState getNext(int index){
		return next[index];
	}

	public void setNext(int index, NFAState s){
		next[index] = s;
	}
	
	NFAState setNext1(NFAState nfa) {
		return next[0] = nfa;
	}
	
	NFAState setNext2(NFAState nfa) {
		return next[1] = nfa;
	}
		
	void setAccept(RuleAccept accept){
		this.accept = accept;
	}
	
	public RuleAccept getAccept(){
		return accept;
	}
		
	public boolean isAccept(){
		return accept != null;
	}

	public EdgeType getEdgeType(){
		return edge.getType();
	}
	
	public NFACard getNFACard() {
		return nfaCard;
	}
	
	public boolean isEpsEdge(){
		return edge == null;
	}
	
	public NFATrnEdge getEdge() {
		return edge;
	}
	
	public void setEdge(NFATrnEdge other){
		if(other.getType() == EdgeType.SIMPLE){
			setEdge(((SimpleEdge)other).getChar());
		} else {
			CharClassEdge ccl = (CharClassEdge)other;
			setEdge(ccl.getOrgSet(), ccl.isNegate());
		}
	}
	
	public void setEdge(int ch) {
		this.edge = nfaMach.getAlphabet().addSimpleEdge(ch);
	}

	public void setEdge(BitSet chars, boolean isNegate) {
		this.edge = nfaMach.getAlphabet().addCharClassEdge(chars, isNegate);
	}
	
	public int getSStateID() {
		return nfaCard.sID;
	}

	public int getType() {
		return nfaType;
	}

	public void setType(int type) {
		this.nfaType |= type;
	}


	public RxRange getRange(int nfaType) {
		
		if(ranges == null)
			return null;
		
		switch(nfaType){
		
		case NFAType.RNG_TL:
		case NFAType.RNG_RE:
		case NFAType.RNG_GB:	
			return ranges[0];
		
		case NFAType.RNG_HD:
			return ranges[1];
			
		default:
			throw new MyLogicException("Invalid NFAType ID");			
		}
	}

	public void setRange(RxRange range, int nfaType)  {
		if(ranges == null)
			ranges = new RxRange[2];
		
		switch(nfaType){
		case NFAType.RNG_TL:
		case NFAType.RNG_RE:
		case NFAType.RNG_GB:	
			ranges[0] = range;
			return;
			
		case NFAType.RNG_HD:
			ranges[1] = range;
			return;
			
		default:
			throw new MyLogicException("Invalid NFAType ID");
		}
	}

	
	public int hashCode() {
		return nfaCard.nfaID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NFAState other = (NFAState) obj;
		if (nfaCard.nfaID != other.nfaCard.nfaID)
			return false;
		return true;
	}
	
	public void setHuggerRe(NFACard reCard)  {
		
		if(huggerRe != null && huggerRe != reCard){
			throw new MyLogicException();
		}
		
		this.huggerRe = reCard;
		if(reCard != null){
			this.huggerRngID = nfaMach.getRxRangeWithReID(reCard.nfaID).rngID;
		}
	}
	
	public NFACard getHuggerRe() {
		return huggerRe;
	}

	public int getHuggerRngID() {
		return huggerRngID;
	}
	
	public RxRange getRxRangeHugger(){
		if(huggerRe == null)
			return null;
		return nfaMach.getRxRangeWithReID(huggerRe.nfaID);
	}
	

	@Override
	public int compareTo(NFAState other) {
		if(this.nfaCard.nfaID < other.nfaCard.nfaID)
			return -1;
		if(this.nfaCard.nfaID == other.nfaCard.nfaID)
			return 0;
		return 1;
	}

	
	public int getNfaID() {
		return nfaCard.nfaID;
	}

	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("ID=" + nfaCard.nfaID +", ruleID=" + nfaCard.rID);
		sb.append("\n\tedge=" + ((edge!=null)?edge:"null"));
		sb.append("\n\tnext1 id=" + (getNext1() != null? getNext1().getNfaID(): ""));
		sb.append("\n\tnext2 id=" + (getNext2() != null? getNext2().getNfaID(): ""));
		sb.append("\n\tEpsilon type=" + NFAType.toString(getType()));
		if(NFAType.isRngReent(getType())){
			RxRange rng = nfaMach.getRxRangeWithReID(getNfaID());
			sb.append("\n\tRxRange: {"+rng.getMinBnd()+"," + (rng.isMaxBndInf()?"Infinity":rng.getMaxBnd())+"}" );
		}
		sb.append("\n\tHugger: " + getHuggerRngID());
		sb.append("\n\tStart State: " + getSStateID());
		
		if(accept != null){
			sb.append("\n\t" + accept);
		}
		sb.append("\n");
		return sb.toString();
	}

	public int getRuleID() {
		return nfaCard.rID;
	}

	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class NFACard implements Comparable<NFACard>{
		
		public final int nfaID;
		public final int rID; //ruleID
		public final int sID; //Start State ID
		
		public NFACard(int nfaID, int ruleID, int sSteID) {
			this.nfaID = nfaID;
			this.rID = ruleID;
			this.sID = sSteID;
		}

		@Override
		public String toString() {
			return "[nfaID=" + nfaID + ", ssID=" + sID + ", rID=" + rID + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + nfaID;
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
			NFACard other = (NFACard) obj;
			if (nfaID != other.nfaID)
				return false;
			return true;
		}

		@Override
		public int compareTo(NFACard other) {
			if(this.nfaID < other.nfaID)
				return -1;
			if(this.nfaID == other.nfaID)
				return 0;
			return 1;
		}

		public int getNfaID() {
			return nfaID;
		}

		public int getrID() {
			return rID;
		}

		public int getSID() {
			return sID;
		}
		
		
	}


	public DFAProduct getDfaProd() {
		return nfaMach.getDfaProd();
	}
}
