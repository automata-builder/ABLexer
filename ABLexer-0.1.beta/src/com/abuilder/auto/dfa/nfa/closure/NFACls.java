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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.abuilder.auto.dfa.nfa.NFAState;
import com.abuilder.auto.dfa.nfa.NFATrnEdge;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.CharClassEdge;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.exception.MyLogicException;

public class NFACls {

	public static enum NFAClsType {
		SIMPLE("Simple"),
		REENTRY("Reentry"),
		TAIL("Tail");
		
		NFAClsType(String name){
			this.name = name;
		}
		
		@Override
		public String toString(){
			return name;
		}
		private final String name;
	}
	
	private static int insCnt;
	public final int clsID;
	private final DFAProduct product;
	public final int myMxID;
	public final NFAClsType nfaClsType;
	private final TreeSet<NFACard> nfaCards = new TreeSet<>();
	private AccMap accMap;
	private TreeMap<NFATrnEdge, TreeMap<Integer, TreeSet<NFACard>>> trEdge_MxID_NFACards;
	private final NFACard prnCard;
	private TreeMap<Integer, ArrayList<NFACls>> mxID_NextCls;
	
	public NFACls(DFAProduct product, int myTrMxID,  TreeSet<NFACard> trChCards, NFAClsType type) {
		this.clsID = insCnt++;
		this.product = product;
		this.myMxID = myTrMxID;
		addNFAs(trChCards);
		this.nfaClsType = type;
		this.prnCard = null;
	}
	
	public NFACls(DFAProduct product, int myMxID,  NFACard prnCard, NFAClsType type) {
		this.clsID = insCnt++;
		this.product = product;
		this.myMxID = myMxID;
		addNFA(prnCard);
		this.nfaClsType = type;
		this.prnCard = prnCard;
	}
	
	public void addNext(NFACls next) throws Exception{
		if(mxID_NextCls == null){
			mxID_NextCls = new TreeMap<>();
		} 
		ArrayList<NFACls> list = mxID_NextCls.get(next.myMxID);
		if(list == null){
			list = new ArrayList<>();
			mxID_NextCls.put(next.myMxID, list);
		}
		list.add(next);
	}
	

	public boolean isAccept(){
		return accMap != null;
	}
	
	public boolean hasNfaID(int nfaID) {
		return nfaCards.contains(nfaID);
	}
	
	public void putOnTrEdgeNfaIDs(NFATrnEdge onEdge, NFAState nfa) {
		
		if(trEdge_MxID_NFACards== null){
			trEdge_MxID_NFACards = new TreeMap<>();
		}
		
		TreeMap<Integer, TreeSet<NFACard>> trMxID_Cards = trEdge_MxID_NFACards.get(onEdge);
		if(trMxID_Cards == null){
			trMxID_Cards = new TreeMap<>();
			trEdge_MxID_NFACards.put(onEdge, trMxID_Cards);
		}
		
		TreeSet<NFACard> trChCards = trMxID_Cards.get(nfa.getHuggerRngID());
		if(trChCards == null){
			trChCards = new TreeSet<>();
//			if(nfa.getHuggerRngID() == DFAEdgeBaseMatrix.INVALID_MXID){
//			}
			trMxID_Cards.put(nfa.getHuggerRngID(), trChCards);
		}
		trChCards.add(nfa.nfaCard);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nfaCards == null) ? 0 : nfaCards.hashCode());
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
		NFACls other = (NFACls) obj;
		if (nfaCards == null) {
			if (other.nfaCards != null)
				return false;
		} else if (!nfaCards.equals(other.nfaCards))
			return false;
		return true;
	}

	public TreeMap<Integer, TreeSet<NFACard>> getTrMxID_NfaIDs(NFATrnEdge onEdge) {
		return trEdge_MxID_NFACards.get(onEdge);
	}

	public Set<NFATrnEdge> getTrEdgeSet() {
		if(trEdge_MxID_NFACards == null)
			return null;
		return trEdge_MxID_NFACards.keySet();
	}
	
	public TreeSet<NFACard> getNFACards() {
		return nfaCards;
	}
	
	public void addNFAs(Set<NFACard> cards){
		for(NFACard card : cards){
			addNFA(card);
		}
	}
	
	public void addNFA(NFACard card){
		
		nfaCards.add(card);
		
		NFAState nfa = product.getNFA(card);
		if(nfa.isAccept()){
			if(accMap == null){
				accMap = new AccMap();
			}
			accMap.addAccNFA(nfa);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("NFACls [ID=");
		sb.append(clsID);
		sb.append(", myTrMxID=");
		sb.append(myMxID);
		sb.append(", nfas=");
		sb.append(nfaCards);
		sb.append(", type="+nfaClsType);
		if(accMap != null){
			sb.append(accMap);
		}
	
		if(trEdge_MxID_NFACards != null){
			for(NFATrnEdge edge : trEdge_MxID_NFACards.keySet()){
				TreeMap<Integer, TreeSet<NFACard>> trMxID_NfaIDs = trEdge_MxID_NFACards.get(edge);
//				if(edge instanceof CharClassEdge){
//					sb.append("\n\t"+edge+" -> ");
//				} else {
//					sb.append("\n\t'"+edge+"' -> ");
//				}
				sb.append("\n\t"+edge+" -> ");
				for(Integer trMxID : trMxID_NfaIDs.keySet()){
					sb.append("\n\t   TrMxID=" + trMxID + " nfas=" + trMxID_NfaIDs.get(trMxID));
				}
			}
		} 
		if (mxID_NextCls != null) {
			sb.append("\n\tEps Trn Next: ");
			for (Integer trMxID : mxID_NextCls.keySet()) {
				sb.append("\n\t   trMxID=" + trMxID + "; next clsID=[");
				int c = 0;
				for(NFACls next : mxID_NextCls.get(trMxID)){
					if(c > 0)
						sb.append(", ");
					sb.append(next.clsID);
					++c;
				}
				sb.append("]\n");
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	public NFAClsType getNFAClsType() {
		return nfaClsType;
	}

	public NFACard getPrnNFACard() {
		return prnCard;
	}


	public TreeMap<Integer, ArrayList<NFACls>> getTrMxID_NextCls() {
		return mxID_NextCls;
	}

	public boolean isSimpleType(){
		return nfaClsType == NFAClsType.SIMPLE;
	}
	
	public boolean isReType(){
		return nfaClsType == NFAClsType.REENTRY;
	}
	
	public boolean isTailType(){
		return nfaClsType == NFAClsType.TAIL;
	}
	

	public AccMap getNFAClsAccept() {
		return accMap;
	}
	
	public Set<Integer> getAccSSIDs() {
		return accMap.getAccSSIDs();
	}

	public TreeSet<Integer> getAccRIDs(int ssID) {
		return accMap.getAccRIDs(ssID);
	}

	/**
	 * 
	 * @author mike
	 *
	 */
	class AccMap {
		
		private final TreeMap<Integer, TreeSet<Integer>> ssID_rIDs = new TreeMap<>();
		
		private void assure(boolean isAccept){
			if(!isAccept){
				throw new MyLogicException();
			}
		}
		
		public void addAccNFA(NFAState nfa){
			
			assure(nfa.isAccept());
			
			TreeSet<Integer> rIDSet = ssID_rIDs.get(nfa.getSStateID());
			if(rIDSet == null){
				rIDSet = new TreeSet<>();
				ssID_rIDs.put(nfa.getSStateID(), rIDSet);
			}
			rIDSet.add(nfa.getRuleID());
		}

		public Set<Integer> getAccSSIDs() {
			return ssID_rIDs.keySet();
		}


		public TreeSet<Integer> getAccRIDs(int ssID) {
			return ssID_rIDs.get(ssID);
		}
		
		public int getTopRuleID(int ssID){
			return ssID_rIDs.get(ssID).first();
		}
	}

	public int getTopRuleID(int ssID) {
		return accMap.getTopRuleID(ssID);
	}
}


