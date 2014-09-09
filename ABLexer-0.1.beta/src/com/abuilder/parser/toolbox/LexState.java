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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.spec.OAEPParameterSpec;

import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.exception.MyLogicException;
import com.abuilder.parser.ParseException;
import com.abuilder.parser.toolbox.LexState.MixedRuleID.Type;


public class LexState extends ScriptObject {
  
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class MixedRuleID {
		
		enum Type {
			RX, INSERT_STATE
		}
		
		private final ABIdentifier insertState;
		private final int rxID;
		private final Type type;
		
		public MixedRuleID(int rxID) {
			this.insertState = null;
			this.rxID = rxID;
			this.type = Type.RX;
		}

		public MixedRuleID(ABIdentifier insertState) {
			this.insertState = insertState;
			this.rxID = -1;
			this.type = Type.INSERT_STATE;
		}

		public ABIdentifier getInsertState() {
			if(getType() == Type.RX){
				throw new MyLogicException();
			}
			return insertState;
		}

		public int getRxID() {
			if(getType() == Type.INSERT_STATE){
				throw new MyLogicException();
			}
			return rxID;
		}

		public Type getType() {
			return type;
		}
	}
	
	/**
	 * 
	 */
	private final DFAProduct dfaProd;
	private final RuleColl ruleColl = new RuleColl();
	private final HashMap<ABIdentifier, TokenSpan> standAloneActions = new HashMap<>();
	private List<MixedRuleID> mixedRuleIDOrderList = new ArrayList<>(); 
	private boolean hasInsertStates;
	/**
	 * state s1 {
	 *   rule r0{}
	 *   rule r1{}
	 *   @insert(s2,s3)
	 *   rule r2{}
	 *   rule r3{}
	 *   @insert(s4)
	 *   rule r4{}
	 *   rule r5{}
	 * }
	 * 
	 * [ 1, -2, -3, 3, -4 ] 
	 */
	

	public void addStandAloneAction(ABIdentifier actionIfr, TokenSpan span) throws ParseException {
		if(standAloneActions.containsKey(actionIfr)){
			throw new ParseException("Lex State <" + getIdentifier() + "> already contains stand alone rule action with name <" + actionIfr + ">");
		}
		standAloneActions.put(actionIfr, span);
	}
	
	public LexState(DFAProduct dfaProd, int id, ABIdentifier stateIfr) {
		super(id, stateIfr, ObjectType.STATE);
		this.dfaProd = dfaProd;
	}

	public int getSID(){
		return getID();
	}
	
	public LexRule addRule(ABIdentifier identifier) throws ParseException {
		
		final int rxID = getNextRxRID();
		
		if(mixedRuleIDOrderList.size() == 0 || mixedRuleIDOrderList.get(mixedRuleIDOrderList.size()-1).getType() == Type.INSERT_STATE){
		
			mixedRuleIDOrderList.add(new MixedRuleID(rxID));
		
		} else {
		
			mixedRuleIDOrderList.set(mixedRuleIDOrderList.size()-1, new MixedRuleID(rxID));
		} 
		
		return ruleColl.add(new LexRule(ruleColl.size(), rxID, getSID(), identifier, ObjectType.RULE));
	}
	
	public LexRule addInsertStateRule(ABIdentifier insertState) throws ParseException {	
		
		if(insertState.equals(getIdentifier())){
			throw new ParseException(insertState.getPos() + " State "+insertState.getName()+ ": Recursive state insert is not allowed. { state a { insert(a) }");
		}
		
		mixedRuleIDOrderList.add(new MixedRuleID(insertState));
		hasInsertStates = true;
		
		return ruleColl.add(new LexRule(getNextRxRID(), getSID(), insertState));
	}

	private int getNextRxRID(){
		return dfaProd.getNfaBldr().getRuleCount();
	}

	

	public HashMap<ABIdentifier, TokenSpan> getStandAloneActions() {
		return standAloneActions;
	}

	public LexRule getLexRule(ABIdentifier idfr) {
		return ruleColl.getItem(idfr);
	}

	public LexRule getLexRule(int index) {
		return ruleColl.getItem(index);
	}

	public List<MixedRuleID> getMixedRuleIDOrderList() {
		return mixedRuleIDOrderList;
	}

	public boolean isHasInsertStates() {
		return hasInsertStates;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LexState {\n\tsID: ");
		builder.append(getSID());
		builder.append("\n\tname: ");
		builder.append(getIdentifier());
		builder.append("\n\thasInsertStates: ");
		builder.append(hasInsertStates);
		builder.append("\n\tmixedIDList: ");
		builder.append(mixedRuleIDOrderList);
		builder.append("\n}");
		return builder.toString();
	}

	public RuleColl getRuleColl() {
		return ruleColl;
	}
	
}
