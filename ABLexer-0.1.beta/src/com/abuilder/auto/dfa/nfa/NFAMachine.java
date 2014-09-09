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

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.NFABldr.RuleAttr;
import com.abuilder.auto.dfa.nfa.NFABldr.SStateMap;
import com.abuilder.auto.dfa.nfa.NFABldr.SStateMap.SSSetter;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.rx_range.RxRange;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.parser.toolbox.LexAction;
import com.abuilder.util.ABUtil;

public class NFAMachine {
	
	public final int machID;
	private final DFAProduct dfaProd;
	private NFAState theRoot;
	private int nextNfaID;
	private final TreeMap<Integer, TreeMap<Integer, NFAState>> rID_NfaID_Nfa = new TreeMap<>();
	private final Alphabet alphabet = new Alphabet();
	private final RxRangeMap rxRngMap = new RxRangeMap();
	private final ArrayList<RuleAttr> ruleAttrList = new ArrayList<>();
	private final SStateMap ssMap = new SStateMap();
	private final boolean isDefine;
	
	public NFAMachine(DFAProduct dfaProd, boolean isDefine) {
		this.machID = dfaProd.getNextNFAMachID();
		this.dfaProd = dfaProd;
		this.isDefine = isDefine;
	}

	public int getNextNfaID() {
//		if(nextNfaID == 28){
//			int a = 10;
//		}
		return nextNfaID++;
	}
	
	public RxRange getRxRangeWithReID(int reID) {
		return rxRngMap.getWithReID(reID);
	}

	public RxRange getRxRangeWithRngID(int rngID) {
		return rxRngMap.getWithRngID(rngID);
	}
	
	public NFAState addNFA(NFAState nfa) {
		
		TreeMap<Integer, NFAState> nfaMap = rID_NfaID_Nfa.get(nfa.getRuleID());
		if(nfaMap == null){
			nfaMap = new TreeMap<>();
			rID_NfaID_Nfa.put(nfa.getRuleID(), nfaMap);
		}
		nfaMap.put(nfa.getNfaID(), nfa);
		//++nextNfaID;
		return nfa;
	}

	public NFAState remNFA(NFAState nfa) {
		TreeMap<Integer, NFAState> nfaMap = rID_NfaID_Nfa.get(nfa.getRuleID());
		nfaMap.remove(nfa.getNfaID());
		//--nextNfaID;
		return nfa;
	}
	
	public RxRange addRxRange(NFAState head, NFAState reentry, NFAState goback, NFAState tail, int[] bnds) {
		return rxRngMap.add(head, reentry, goback, tail, bnds);
	}
	
	public int getRxRangeCount(){
		return rxRngMap.size();
	}
	
	public int getNfaCount(){
		int total = 0;
		for (TreeMap<Integer, NFAState> map : rID_NfaID_Nfa.values()) {
			total += map.size();
		}
		return total;
	}
	

	public int getRuleCount() {
		return ruleAttrList.size();
	}
	
	public NFAState getNFA(NFACard card){
		TreeMap<Integer, NFAState> nfas = rID_NfaID_Nfa.get(card.rID);
		return nfas.get(card.nfaID);
	}
		
	public Alphabet getAlphabet(){
		return alphabet;
	}


	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\n---  NFA MACHINE: machID="+machID+", isDefine="+isDefine+", nfaSize=" + getNfaCount() + ", ruleSize="+getRuleCount());
		sb.append("\nrootCard=" + (getRootCard() != null? getRootCard().toString() : "null"));
		for(Integer ruleID : rID_NfaID_Nfa.keySet()){
			sb.append("\n\nRuleID=" + ruleID);
			TreeMap<Integer, NFAState> nfaMap = rID_NfaID_Nfa.get(ruleID);
			for(NFAState nfa : nfaMap.values()){
				sb.append("\n" + nfa);
			}
		}
		
		sb.append("\n*** Rx Range ***\n");
		sb.append(rxRngMap.toString());
		sb.append("\n\n---------- NFA MACHINE END ----------------\n\n");
		return sb.toString();
	}

	public NFACard getRootCard() {
		return theRoot != null? theRoot.nfaCard : null;
	}


	public NFAState getRoot() {
		return theRoot;
	}

	public NFAState setRoot(NFAState theHead) {
		return this.theRoot = theHead;
	}

	public RxRangeMap getRxRangeMap() {
		return rxRngMap;
	}


	public ArrayList<RuleAttr> getRuleAttrList() {
		return ruleAttrList;
	}

	public RuleAttr getRuleAttr(int rID) {
		return ruleAttrList.get(rID);
	}
	
	public SStateMap getSSteMap() {
		return ssMap;
	}

	public int getSStateCount() {
		return ssMap.ssSize();
	}

	public String getSsSize(String nl){
		return "private final int ssSize = " + ssMap.ssSize() + ";" + nl;
	}
	
	public String getStateNameMethodAsString() {
		return ssMap.getStateNameMethodAsString();// + nl + nl + ssMap.getSsFinals2();
	}
	
	public int getSSSetByteSize() {
		return ssMap.getSSSetByteSize();
	}

	public SSSetter getSSSetter(int ssID) {
		return ssMap.getSSSetter(ssID);
	}
	
	public short[] getRuleSsIDs(){
		short[] ruleSsIDs = new short[ruleAttrList.size()];
		for(int i = 0; i < ruleAttrList.size(); ++i){
			ruleSsIDs[i] = (short) ruleAttrList.get(i).getSID();
		}
		return ruleSsIDs;
	}
	
	public short[] getRuleDfnSrcStartPos(){//Position in lex script
		short[] pos = new short[ruleAttrList.size()];
		for(int i = 0; i < ruleAttrList.size(); ++i){
			pos[i] = (short) ruleAttrList.get(i).getLineInfo().startPos;
		}
		return pos;
	}
	
	public short[] getRuleDfnSrcLineIndex(){//Position in lex script
		short[] pos = new short[ruleAttrList.size()];
		for(int i = 0; i < ruleAttrList.size(); ++i){
			pos[i] = (short) ruleAttrList.get(i).getLineInfo().lineIndex;
		}
		return pos;
	}
	
	public byte[] getRuleAnchors(){
		
		byte[] ruleAnchors = new byte[ruleAttrList.size()];
		final int bolAnchor = 1;
		final int eolAnchor = 2;
		
		for(int i = 0; i < ruleAttrList.size(); ++i){
			
			if(ruleAttrList.get(i).isBolAnchor()){
				ruleAnchors[i] |= bolAnchor;
			}
			if(ruleAttrList.get(i).isEolAnchor()){
				ruleAnchors[i] |= eolAnchor;
			}
		}
		return ruleAnchors;
	}
	
	
	public String getRuleSsIDAndAnchorsAsString(){
		
		String nl = ABUtil.getSysLineSep();
		StringBuilder ruleSs = new StringBuilder();
		StringBuilder rAnchors = new StringBuilder();
		
		ruleSs.append("private final short[] ruleSsIDs = {"+nl+"\t");
		rAnchors.append("private final byte[] ruleAnchors = {"+nl+"\t");
		final int bolAnchor = 1;
		final int eolAnchor = 2;
		
		for(int i = 0; i < ruleAttrList.size(); ++i){
			if(i > 0){
				ruleSs.append(", ");
				rAnchors.append(", ");
			}
			ruleSs.append("" + ruleAttrList.get(i).getSID());
			byte anchors = 0;
			if(ruleAttrList.get(i).isBolAnchor()){
				anchors |= bolAnchor;
			}
			if(ruleAttrList.get(i).isEolAnchor()){
				anchors |= eolAnchor;
			}
			rAnchors.append("" + anchors);
		}
		ruleSs.append(nl + "};");
		rAnchors.append(nl + "};");
		return ruleSs.toString() + nl + rAnchors ;//+ nl + bolAnchor_S + eolAnchor_S;
	}
	
		
	public String getActionSwitch(){
		
		String nl = ABUtil.getSysLineSep();
		
		StringBuilder sb = new StringBuilder();
		String t0 = "\t\t";
		String t1 = "\t" + t0;
		String t2 = "\t" + t1;
		String t3 = "\t" + t2;
		
		sb.append(t1 + "switch(accRID) {" + nl);
				
		for(int i = 0; i < ruleAttrList.size(); ++i){
			
			sb.append(t1 + "case " + (i) + ":" + nl);
			if(i > 0){
				sb.append(t2 + "if(accRID < " + i +") break;" + nl);
			}
			
			LexAction action = ruleAttrList.get(i).getLexAction();
			if(action != null){
				sb.append(nl + t3 + action.getUserCode() + nl + nl);
			}
		}
		
		sb.append(nl + t1 + "default:" + nl);
		sb.append(t2 + "if(accRID == " + (ruleAttrList.size()-1) + ") break;" + nl);
		sb.append(t2 + "throw new Exception(\"Bad accept ruleID {\"+accRID+\"}\");" + nl);
		sb.append(t1 + "}" + nl);
		sb.append(t0 + "}" + nl);
		return sb.toString();
	}

	public NFAState getNFA(int rID, int nfaID) {
		TreeMap<Integer, NFAState> map =  rID_NfaID_Nfa.get(rID);
		if(map != null){
			return map.get(nfaID);
		}
		return null;
	}

	public Collection<NFAState> nfaValues(int rID) {
		TreeMap<Integer, NFAState> map =  rID_NfaID_Nfa.get(rID);
		if(map != null){
			return map.values();
		}
		return null;
	}

	public boolean addRuleAttr(RuleAttr e) {
		return ruleAttrList.add(e);
	}

	public boolean isDefineMach() {
		return isDefine;
	}

	public DFAProduct getDfaProd() {
		return dfaProd;
	}

	
	
	
}
