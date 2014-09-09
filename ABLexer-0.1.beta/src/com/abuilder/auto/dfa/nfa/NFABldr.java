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

import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.NFAConstants.NFAType;
import com.abuilder.auto.dfa.nfa.rx_range.RxRange;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.exception.MyLogicException;
import com.abuilder.parser.toolbox.LexAction;
import com.abuilder.parser.toolbox.LexRange;
import com.abuilder.parser.toolbox.LexRule;
import com.abuilder.parser.toolbox.LexState;
import com.abuilder.scriptparser.io.toolbox.LineInfo;
import com.abuilder.util.ABUtil;

public class NFABldr {

	public enum NFA_CLOSURE {
		POSITIVE, OPTIONAL, KLEENE_STAR
	}
	
	public static final int DEFINE_RID = 0;
	public static final int DEFINE_SID = -1;
	private final DFAProduct dfaProd;
	private NFAState currHead; 
	private final NFAMachine nfaMach;
	private LexRule currLexRule;
	
	
	public NFABldr(DFAProduct dfaProd, boolean isDefine) {
		this.dfaProd = dfaProd;
		this.nfaMach = new NFAMachine(dfaProd, isDefine);
	}
	
	public NFAMachine getNFAMach(){
		return nfaMach;
	}
	
	public void startRule(LexRule lxRule) {
	
		//this.currRuleSID = lxRule.getSID();
		setCurrLexRule(lxRule);
				
		if(getNFAMach().getRoot() == null){	
			currHead = getNFAMach().setRoot(getNewState(lxRule.getLexRID(), lxRule.getSID()));
		}
	}
	
	private void setAnchors(NFAMachHT nfaHT, boolean bolAnchor, boolean eolAnchor){
		
		if(bolAnchor){
			NFAState bol = new NFAState(dfaProd, nfaHT.rID, nfaHT.ssID);
			bol.setNext1(nfaHT.head);
			bol.setEdge('\n');
			nfaHT.head = bol;
		}
	
		if(eolAnchor){
			NFAMachHT eolHT_Ms = new NFAMachHT(dfaProd, nfaHT.rID, nfaHT.ssID);
			eolHT_Ms.setHeadEdge("\r\n");
			
			NFAMachHT eolHT_Unx = new NFAMachHT(dfaProd, nfaHT.rID, nfaHT.ssID);
			eolHT_Unx.setHeadEdge("\n");
			
			orJoinRightHand(eolHT_Ms, eolHT_Unx);
			catinate(nfaHT, eolHT_Ms);
		}
	}
	
	public void endRule(LexRule lxRule, LexAction action){

		endRule(lxRule.getNfaHT(), lxRule.isBolAnchor(), lxRule.isEolAnchor(), action, lxRule.getName(), null);
	}
	
	private void endRule(NFAMachHT nfaHT, boolean bolAnchor, boolean eolAnchor, LexAction action, String ruleName, LineInfo lineInfo) {
		
		
		if(getCurrRuleRID() != nfaHT.rID || getCurrRuleSID() != nfaHT.ssID){
			throw new MyLogicException();
		}
		
		setAnchors(nfaHT, bolAnchor, eolAnchor);
		
		RuleAccept accept = new RuleAccept(dfaProd, nfaHT.tail.getNFACard(), bolAnchor, eolAnchor, action);
		
		nfaHT.tail.setAccept(accept);
			
		if(nfaMach.getRuleCount() == 0){
			
			currHead.setNext1(nfaHT.head);
			
		} else {
			
			currHead = currHead.setNext2(getNewState(nfaHT.rID, nfaHT.ssID));
			currHead.setNext1(nfaHT.head);
		}
		
		currHead.setType(NFAType.RULE_HUB);
		
		if(getRuleCount() != getCurrRuleRID()){
			throw new MyLogicException();
		}
		
	  //Must be last!!!(for getRuleSize())
		getNFAMach().addRuleAttr(new RuleAttr(nfaHT.rID, ruleName, accept, lineInfo));//Must be last!!!(for getRuleSize())
		setCurrLexRule(null);
	}

	
	
	public void addRxRange(NFAMachHT nfaHT, LexRange range){
		addRxRange(nfaHT, range.getMinBound(), range.getMaxBound());
	}


//	public void addRxClos(NFAMachHT nfaHT, NFA_CLOSURE type){
//		
//		NFAMachHT f1 = new NFAMachHT(dfaProd, nfaHT.rID, nfaHT.ssID);
//		f1.insertHT(nfaHT);
//		
//		if(type == NFA_CLOSURE.POSITIVE){
//		
//			f1.tail.setNext2(f1.head);
//		
//		} else if(type == NFA_CLOSURE.OPTIONAL){
//			
//			f1.head.setNext2(f1.tail);
//		
//		} else {
//			
//			//CLOSURE_TYPE.KLEENE_STAR
//			
//			f1.head.setNext2(f1.tail);
//			f1.tail.setNext2(f1.head);
//		}
//		
//		NFAMachHT f2 = new NFAMachHT(dfaProd, nfaHT.rID, nfaHT.ssID);
//		f2.insertHT(f1);
//		nfaHT.setHT(f2);
//	}
	
	public void addRxRange(NFAMachHT nfaHT, int minBound, int maxBound) {
		NFAMachHT fctr1 = new NFAMachHT(dfaProd, nfaHT.rID, nfaHT.ssID);
		fctr1.insertHT(nfaHT);

		fctr1._setHeadEdge(Alphabet.CHR.EPS_CHR); 
		nfaHT.setTailEdge(Alphabet.CHR.EPS_CHR);
		
		NFAMachHT fctr2 = new NFAMachHT(dfaProd, nfaHT.rID, nfaHT.ssID);
		fctr2.insertHT(fctr1);
		
		dfaProd.getNFAMach().addRxRange(fctr2.head, fctr1.head, fctr1.tail, fctr2.tail, new int[]{minBound, maxBound});
		
		NFAMachHT fctr3 = new NFAMachHT(dfaProd, nfaHT.rID, nfaHT.ssID);
		fctr3.insertHT(fctr2);
		fctr3._setHeadEdge(Alphabet.CHR.EPS_CHR);
		fctr2.setTailEdge(Alphabet.CHR.EPS_CHR);
		
		if(minBound == 0){
			fctr2.head.setNext2(fctr2.tail);
		}
		
		if(maxBound == RxRange.INFINITY_BOUND || maxBound > 1){
			fctr1.tail.setNext2(fctr1.head);
		}
		nfaHT.setHT(fctr3);
	}
	
	
	
	public void catinate(NFAMachHT nfaHT, NFAMachHT cat) {
		
		if(NFAType.isRngHead(cat.head.getType())){
			RxRange rng = dfaProd.getRxRangeWithReID(cat.head.getNext1().getNfaID());
			rng.setHead(nfaHT.tail);
		}

		nfaHT.tail.memCpy(cat.head);
		dfaProd.remNFA(cat.head);
		nfaHT.tail = cat.tail;
	}
	
	public void orJoinRightHand(NFAMachHT nfaHT, NFAMachHT orRhHT){
		
		NFAState orHtStrt = new NFAState(dfaProd, nfaHT.rID, nfaHT.ssID);
		orHtStrt.setNext1(nfaHT.head);
		orHtStrt.setNext2(orRhHT.head);
		orHtStrt.setType(NFAType.OR_HUB_HD);
		nfaHT.head = orHtStrt;
		
		NFAState orHtEnd = new NFAState(dfaProd, nfaHT.rID, nfaHT.ssID);
		orHtEnd.setType(NFAType.OR_HUB_TL);
		nfaHT.tail.setNext1(orHtEnd);
		orRhHT.tail.setNext1(orHtEnd);
		nfaHT.tail = orHtEnd;
	}
	

	/**
	 * 
	 * @author mike
	 * 
	 */
	
	
	public int getRuleCount() {
		return getNFAMach().getRuleCount();
	}
	
	public NFAState getNewState(int rID, int sID){
		return new NFAState(dfaProd, rID, sID);
	}
	

	/**
	 * 
	 * @author mike
	 *
	 */
	public static class RuleAttr {
		
		public final int rID;
		private final String ruleName;
		private final RuleAccept rAcc;
		private final LineInfo lineInfo;
		
		public RuleAttr(int rID, String ruleName, RuleAccept ruleAccept, LineInfo lineInfo) {
			this.rID = rID;
			this.ruleName = ruleName;
			this.rAcc = ruleAccept;
			this.lineInfo = lineInfo;
		}

		public String getRuleName() {
			return ruleName;
		}

		public RuleAccept getRuleAccept() {
			return rAcc;
		}

		public LexAction getLexAction() {
			return rAcc.getLexAction();
		}

		public boolean isBolAnchor() {
			return rAcc.isBolAnchor();
		}

		public boolean isEolAnchor() {
			return rAcc.isEolAnchor();
		}

		public int getSID() {
			return rAcc.getSID();
		}

		public LineInfo getLineInfo() {
			return lineInfo;
		}
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class SStateMap {//Start State Map
				
		private final LinkedHashMap<String, Integer> name_ssID = new LinkedHashMap<>();
		private final TreeMap<Integer, String> ssID_Name = new TreeMap<>();
		private static final int BYTE_SIZE = 8;

		public void add(LexState lexState)  {
			
			name_ssID.put(lexState.getName(), lexState.getSID());
			ssID_Name.put(lexState.getSID(), lexState.getName());
		}

		public int ssSize() {
			return name_ssID.size();
		}
		
		
		public String getStateNameMethodAsString(){
			
			String t0 = "\t";
			String t1 = t0 + "\t";
			String t2 = t1 + "\t";
			
			String nl = ABUtil.getSysLineSep();
			StringBuilder sb = new StringBuilder();
			
			sb.append(t0 + "public String getStateName(int sID){" + nl);
			sb.append(t1 + "switch(sID) {" + nl);
			
			for(Integer i : ssID_Name.keySet()){
				sb.append(t1 + "case " + i + ":" + nl);
				sb.append(t2 + "return STATE." + ssID_Name.get(i) + ".name;" + nl);
			}
			
			sb.append(t1 + "default:" + nl);
			sb.append(t2 + "throw new IllegalArgumentException();" + nl);
			sb.append(t1 + "}" + nl);
			sb.append(t0 + "}");
			return sb.toString();
		}
		
		public int getSSSetByteSize(){
			double size_D = (double)ssSize() / BYTE_SIZE;
			int size_I = (int)size_D;
			if(size_D > size_I){
				++size_I;
			}
			return size_I;
		}
		
		public SSSetter getSSSetter(int ssID){
			int byteIx = ssID / BYTE_SIZE;
			int bitIx = ssID % BYTE_SIZE;
			int mask = 1 << bitIx;
			return new SSSetter(byteIx, bitIx, mask);
		}
		

		/**
		 * 
		 * @author mike
		 *
		 */
		public static class SSSetter {
			
			public final int byteIx, bitIx, mask;

			public SSSetter(int byteIx, int bitIx, int mask) {
				this.byteIx = byteIx;
				this.bitIx = bitIx;
				this.mask = mask;
			}
			
		}


		public Integer getSsID(String ssName) {
			return name_ssID.get(ssName);
		}

		public String getSsName(int ssID) {
			return ssID_Name.get(ssID);
		}
	}

	public int getCurrRuleSID() {
		if(currLexRule == null){
			return -1;
		}
		return currLexRule.getSID();
	}

	public int getCurrRuleRID() {
		if(currLexRule == null){
			return -1;
		}
		return currLexRule.getRxRID();
	}

	public LexRule getCurrLexRule() {
		return currLexRule;
	}

	private void setCurrLexRule(LexRule currLexRule) {
		this.currLexRule = currLexRule;
	}

	
}
