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

import com.abuilder.auto.dfa.nfa.NFAMachHT;

//http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Freference%2Fref-tostring-templates.htm
public class LexRule extends ScriptObject{
	  
	public enum RuleType {
		RX_EXPR, INSERT_STATE
	}
	
	private ABIdentifier insertState;
	private NFAMachHT nfaHT;
	private boolean bolAnchor = false, eolAnchor = false;
	private final int rxRID, ssID;
	
	public LexRule(int lexRID, int rxRID, int ssID, ABIdentifier identifier, ObjectType type) {
		super(lexRID, identifier, type);
		this.rxRID = rxRID;
		this.ssID = ssID;
	}
	
	public LexRule(int lexRID, int ssID, ABIdentifier insertState) {
		super(lexRID, new ABIdentifier( lexRID + "_insert_state_" + insertState.getName(), insertState.getStartToken()), ObjectType.RULE);
		this.rxRID = -1;
		this.ssID = ssID;
		this.insertState = insertState;
	}

	public int getLexRID(){
		return getID();
	}
	
	public int getRxRID() {
		return rxRID;
	}
	
	public int getSID(){
		return ssID;
	}
	

	public RuleType getRuleType() {
		if(insertState == null){
			return RuleType.RX_EXPR;
		}
		return RuleType.INSERT_STATE;
	}

	public ABIdentifier getInsertState() {
		return insertState;
	}

	public NFAMachHT getNfaHT() {
		return nfaHT;
	}

	public void setNfaHT(NFAMachHT nfaHT) {
		this.nfaHT = nfaHT;
	}

	public boolean isBolAnchor() {
		return bolAnchor;
	}

	public void setBolAnchor(boolean bolAnchor) {
		this.bolAnchor = bolAnchor;
	}

	public boolean isEolAnchor() {
		return eolAnchor;
	}

	public void setEolAnchor(boolean eolAnchor) {
		this.eolAnchor = eolAnchor;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LexRule {\n\tlexRID: ");
		builder.append(getLexRID());
		builder.append("\n\tname: " + getIdentifier());
		builder.append("\n\ttype: " + getType());
		builder.append("\n\trxRID: ");
		builder.append(rxRID);
		builder.append("\n\tssID: ");
		builder.append(ssID);
		builder.append("\n\tinsertState: ");
		builder.append(insertState);
		builder.append("\n\tbolAnchor: ");
		builder.append(bolAnchor);
		builder.append("\n\teolAnchor: ");
		builder.append(eolAnchor);
		builder.append("\n}");
		return builder.toString();
	}

	

	
	
	
}
