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

package com.abuilder.parser.toolbox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import com.abuilder.auto.dfa.nfa.NFABldr;
import com.abuilder.auto.dfa.nfa.RuleAccept;
import com.abuilder.auto.dfa.nfa.NFAMachHT;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.exception.MyLogicException;
import com.abuilder.parser.ParseException;
import com.abuilder.parser.Token;
import com.abuilder.parser.toolbox.LexState.MixedRuleID;
import com.abuilder.parser.toolbox.LexState.MixedRuleID.Type;
import com.abuilder.parser.toolbox.ScriptObject.ObjectType;
import com.abuilder.scriptparser.lexer.parser.toolbox.LxDefineNFAMachMap.LxDefineNfaHT;
import com.abuilder.util.ABUtil;

public class ScriptBuilder {

	private final DFAProduct dfaProd;
	private final StateColl stateColl = new StateColl();
	private final RuleColl defColl = new RuleColl();
	private final TokenSpan lexClosSpan = new TokenSpan();
	private final TokenSpan lexMethDeclrSpan = new TokenSpan();
	private final TokenSpan topClassBodySpan = new TokenSpan();
	private final TokenSpan importSpan = new TokenSpan();
	private TokenSpan lexMethInitSpan;
	private final ArrayList<TokenSpan> actions = new ArrayList<TokenSpan>();
	private LinkedHashMap<LexState, HashSet<LexState>> state_Inserts;
	private Token leftMost; //pos after 'package' & 'import' statements, if any 
	
	private char[] cbuf;
	ArrayList<Integer> nlPosList;

	public ScriptBuilder(DFAProduct dfaProd) {
		this.dfaProd = dfaProd;
	}

	public void setNLPosList(ArrayList<Integer> NLPosList) {
		this.nlPosList = NLPosList;
	}

	public LexState addState(ABIdentifier stateIfr) throws ParseException {
		return stateColl.add(new LexState(dfaProd, stateColl.size(), stateIfr));
	}

	public LexRule addDefine(ABIdentifier defineIfr) throws ParseException {
		setDefineNfaBldr();
		return defColl.add(new LexRule(defColl.size(), NFABldr.DEFINE_RID, NFABldr.DEFINE_SID, defineIfr, ObjectType.DEFINITION));
	}

	public void endDefine(LexRule lxRule) {
		getNfaBldr().endRule(lxRule, null);
		dfaProd.addDefine(lxRule.getIdentifier().getName(), getNfaBldr().getNFAMach());
		setNfaBldr(null);
	}

	public void startRule(LexRule lxRule) {
		getNfaBldr().startRule(lxRule);
	}

	public void endRule(LexRule lxRule, TokenSpan span) {
		//int rID = getNfaBldr().getRuleCount();
		if (actions.size() != getNfaBldr().getCurrRuleRID()) {
			throw new MyLogicException();
		}
		actions.add(span);
		getNfaBldr().endRule(lxRule, null);
	}

	public void setUserActions() throws ParseException {

		for (int i = 0; i < actions.size(); i++) {

			TokenSpan span = actions.get(i);

			if (span != null) {

				setLexAction(getNfaBldr().getNFAMach().getRuleAttr(i).getRuleAccept(), span);
			}
		}

		for (int i = 0; i < stateColl.size(); i++) {
			LexState s = stateColl.getItem(i);
			for (ABIdentifier actionIdfr : s.getStandAloneActions().keySet()) {

				LexRule r = s.getLexRule(actionIdfr);
				if (r == null) {
					throw new ParseException("State<" + s.getIdentifier() + ">; Stand alone rule action <" + actionIdfr + " " + actionIdfr.getPos() + "> has no matching rule defined.");
				}

				RuleAccept acc = getNfaBldr().getNFAMach().getRuleAttr(r.getRxRID()).getRuleAccept();
				if (acc.getLexAction() != null) {
					throw new ParseException("State<" + s.getIdentifier() + ">: Rule <" + r.getIdentifier() + " " + r.getIdentifier().getPos() + "> has actions defined twice; anonymous & stand alone; only one action per rule allowed.");
				}
				// acc.setLexAction(lexAction)
				setLexAction(acc, s.getStandAloneActions().get(actionIdfr));
			}
		}
	}

	private void setLexAction(RuleAccept acc, TokenSpan span) {

		int p1 = 1 + getStartTokenCBufPos(span.getStart());
		int p2 = getEndTokenCBufPos(span.getEnd());
		String userCode = (new String(cbuf, p1, p2 - p1)).trim();

		acc.setLexAction(new LexAction(userCode));
	}

	public LexState getState(ABIdentifier idfr) {
		return stateColl.getItem(idfr);
	}

	public DFAProduct getDfaProd() {
		return dfaProd;
	}

	public NFABldr getNfaBldr() {
		return dfaProd.getNfaBldr();
	}

	public void setDefineNfaBldr() {
		setNfaBldr(new NFABldr(dfaProd, true));
	}

	public void setRulesNfaBldr() {
		setNfaBldr(new NFABldr(dfaProd, false));
	}

	private void setNfaBldr(NFABldr nfaBldr) {
		dfaProd.setNfaBldr(nfaBldr);
	}

	public NFAMachHT getNewNFAMachHT(LexRule lxRule) {
		return new NFAMachHT(dfaProd, lxRule);
	}

	private void setCharIDs() {
		dfaProd.getAlphabet().setCharIDs();
	}

	public void setLexClosParseDone() throws ParseException {
		setCharIDs();
		setStates();
	}

	
	public NFAMachHT getDefineNFAMachHT(ABIdentifier idfr) throws ParseException {
		LxDefineNfaHT defNfaHT = dfaProd.getLxDefineNfaHTMap().get(idfr.getName());
		
		if(defNfaHT == null){
			throw new ParseException("@insert " + idfr + " " + idfr.getPos() + "> has not been defined.");
		}
		
		return defNfaHT.dumpMachCopy();
	}

	public boolean isLexClosDefined(){
		return lexClosSpan.getStart() != null;
	}
	
	public TokenSpan getLexClosSpan() {
		return lexClosSpan;
	}

	public void setCBuf(char[] cbuf) {
		this.cbuf = cbuf;
	}

	public char[] getCBuf() {
		return cbuf;
	}

	public int getStartTokenCBufPos(Token start) {
		int lnStart = nlPosList.get(start.beginLine - 1);
		return lnStart + start.beginColumn;
	}
	

	public int getEndTokenCBufPos(Token end) {
		int lnStart = nlPosList.get(end.beginLine - 1);
		return lnStart + end.endColumn;
	}

	public TokenSpan getLexMethDeclrSpan() {
		return lexMethDeclrSpan;
	}

	public TokenSpan getTopClassBodySpan() {
		return topClassBodySpan;
	}

	public TokenSpan getImportSpan() {
		return importSpan;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScriptBuilder {\n\timportSpan: ");
		builder.append(importSpan);
		builder.append("\n\ttopClassBodySpan: ");
		builder.append(topClassBodySpan);
		builder.append("\n\tlexMethDeclrSpan: ");
		builder.append(lexMethDeclrSpan);
		builder.append("\n\tlexClosSpan: ");
		builder.append(lexClosSpan);
		builder.append("\n}");
		return builder.toString();
	}

	public String getStateClassAsString() {

		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder();
		
		String t0 = "\t";
		String t1 = t0 + "\t";
		String t2 = t1 + "\t";

		sb.append(nl + t0 + "public interface STATE {");
		
		for (int i = 0; i < stateColl.size(); i++) {
			LexState s = stateColl.getItem(i);
			sb.append(nl + nl + t1 + "public interface " + s.getIdentifier().getName() + " {" + nl);
			sb.append(t2 + "short sID = " + s.getSID() + ";" + nl);
			sb.append(t2 + "String name = \"" + s.getIdentifier().getName() + "\";" + nl);

			for (int j = 0; j < s.getRuleColl().size(); j++) {
				LexRule r = s.getRuleColl().getItem(j);

				if (r.getRuleType() == LexRule.RuleType.INSERT_STATE) {

					sb.append(t2 + "//" + r.getIdentifier().getName().substring(2) + nl);

				} else {
					sb.append(t2 + "Rule " + r.getIdentifier().getName() + " = new Rule(" + r.getRxRID() + ", \"" + r.getIdentifier().getName() + "\");" + nl);
				}
			}

			sb.append(t1 + "}" + nl);
		}
		
		sb.append(t0 + "}" + nl + nl);
		
		sb.append(dfaProd.getNFAMach().getStateNameMethodAsString());
		
		sb.append(nl + nl);
		
		return sb.toString();
	}

	private void setStates() throws ParseException {

		this.state_Inserts = new LinkedHashMap<>();

		for (int i = 0; i < stateColl.size(); i++) {

			LexState prntState = stateColl.getItem(i);
			List<MixedRuleID> mixedIDList = prntState.getMixedRuleIDOrderList();
			dfaProd.getNFAMach().getSSteMap().add(prntState);

			if (prntState.isHasInsertStates()) {

				for (int j = 0; j < mixedIDList.size(); j++) {

					if (mixedIDList.get(j).getType() == Type.INSERT_STATE) {

						ABIdentifier stateIdfr = mixedIDList.get(j).getInsertState();
						
						LexState insertState = stateColl.getItem(stateIdfr);
					
						if (insertState == null) {
							throw new ParseException("insert <" + stateIdfr + " " + stateIdfr.getPos() + "> doesn't exist.");
						}

						HashSet<LexState> inserts = state_Inserts.get(prntState);
						if (inserts == null) {
							inserts = new HashSet<>();
							state_Inserts.put(prntState, inserts);
						}
						inserts.add(insertState);
					}
				}
			}
		}

		recursiveStateInsertsCheck(state_Inserts);
	}

	public String getRuleNamesAsString(){
		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder();
		
		sb.append("private final String[] ruleNames = {" + nl + "\t");
		
		for (int i = 0; i < dfaProd.getNFAMach().getRuleAttrList().size(); i++) {
			
			if(i > 0){
				sb.append(", ");
			}
			
			String ruleName = dfaProd.getNFAMach().getRuleAttrList().get(i).getRuleName();
			int sID = dfaProd.getNFAMach().getRuleAttrList().get(i).getSID();
			String stateName = stateColl.getItem(sID).getName();
			sb.append("\"" + stateName + "." + ruleName + "\"");
		}
		
		sb.append(nl + "}; " + nl);
		return sb.toString();
	}
	
	public String getSsRuleOrderAsString() throws ParseException {

		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder();

		sb.append("private final short[][] ssRuleOrder = {");

		for (int i = 0; i < stateColl.size(); i++) {

			LexState prntState = stateColl.getItem(i);
			List<MixedRuleID> mixedIDList = prntState.getMixedRuleIDOrderList();

			if (i > 0) {
				sb.append(", ");
			}

			sb.append(nl + "\t{");

			if (!prntState.isHasInsertStates()) {

				sb.append("}");

			} else {

				for (int j = 0; j < mixedIDList.size(); j++) {

					if (j > 0) {
						sb.append(", ");
					}
					if (mixedIDList.get(j).getType() == Type.RX) {
						int rxIDBeforeInsertState = mixedIDList.get(j).getRxID();
						sb.append(rxIDBeforeInsertState);

					} else {
						// State Name
						ABIdentifier stateName = mixedIDList.get(j).getInsertState();
						LexState insertState = stateColl.getItem(stateName);
						if (insertState == null) {
							throw new ParseException("Insert state<" + stateName + "> doesn't exist");
						}

						sb.append((-(insertState.getSID() + 1)));
					}
				}
				sb.append("}");
			}
		}

		sb.append(nl + "};" + nl);

		sb.append("private final short[][] ssInserts = {");
		
		for (int i = 0; i < stateColl.size(); i++) {
			
			LexState prntState = stateColl.getItem(i);
			if(i > 0){
				sb.append(", ");
			}
			sb.append(nl + "\t" + "{");
			
			HashSet<LexState> inserts = state_Inserts.get(prntState);
			
			if (inserts != null) {
				int j = 0;
				for (LexState insert : inserts) {
					if (j > 0) {
						sb.append(", ");
					}
					sb.append(insert.getSID());
					++j;
				}
			}
			sb.append("}");
		}
		
		sb.append(nl + "};" + nl);

		return sb.toString();
	}


	private void recursiveStateInsertsCheck(LinkedHashMap<LexState, HashSet<LexState>> state_Inserts) throws ParseException {

		for (Map.Entry<LexState, HashSet<LexState>> e : state_Inserts.entrySet()) {

			StateStack stk = new StateStack(new StateStackItem(e.getKey(), e.getValue()));

			LinkedHashSet<ABIdentifier> insSeqSet = new LinkedHashSet<>();

			insSeqSet.add(e.getKey().getIdentifier());

			while (stk.size() > 0) {

				if (stk.peek().hasNext()) {

					LexState insertState = stk.peek().next();

					if (insSeqSet.contains(insertState.getIdentifier())) {

						StringBuilder sb = new StringBuilder();

						ABIdentifier first = null;
						for (ABIdentifier idfr : insSeqSet) {
							if (first == null) {
								first = idfr;
							}
							sb.append(idfr + " -> ");
						}
						sb.append(first);

						throw new ParseException("Recursive state insert is not allowed; " + sb.toString());
					}
					insSeqSet.add(insertState.getIdentifier());

					HashSet<LexState> inserts = state_Inserts.get(insertState);
					if (inserts != null) {
						stk.push(new StateStackItem(insertState, inserts));
					}

				} else {
					stk.pop();
				}
			}
		}

	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	private class StateStackItem {

		private final LexState prnt;
		private final LexState[] chdn;
		private int index;

		private StateStackItem(LexState prnt, HashSet<LexState> chdSet) {
			this.prnt = prnt;
			this.chdn = chdSet.toArray(new LexState[chdSet.size()]);
		}

		boolean hasNext() {
			return index < chdn.length;
		}

		LexState next() {
			return chdn[index++];
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("StateStackItem {\n\tprnt: ");
			builder.append(prnt);
			builder.append("\n\tchdn: ");
			builder.append(Arrays.toString(chdn));
			builder.append("\n\tindex: ");
			builder.append(index);
			builder.append("\n}");
			return builder.toString();
		}
	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	private class StateStack {

		private final ArrayDeque<StateStackItem> stk = new ArrayDeque<StateStackItem>();

		private StateStack(StateStackItem item) {
			push(item);
		}

		public StateStackItem peek() {
			return stk.peek();
		}

		public void push(StateStackItem e) {
			stk.push(e);
		}

		public StateStackItem pop() {
			return stk.pop();
		}

		public int size() {
			return stk.size();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("StateStack {\n\tstk: ");
			builder.append(stk);
			builder.append("\n}");
			return builder.toString();
		}

	}

	public Token getLeftMost() {
		return leftMost;
	}

	public void setLeftMost(Token leftMost) {
		if(this.leftMost == null){
			this.leftMost = leftMost;
		}
	}

	public TokenSpan getLexMethInitSpan() {
		return lexMethInitSpan;
	}

	public void setLexMethInitSpan(TokenSpan lexMethInitSpan) {
		this.lexMethInitSpan = lexMethInitSpan;
	}

	public StateColl getStateColl() {
		return stateColl;
	}
}
