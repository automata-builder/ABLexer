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

import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.parser.toolbox.LexAction;

public class RuleAccept {//RuleAccept

	private final DFAProduct product;
	private final NFACard ownerCard;//rule end nfa
	private final boolean bolAnchor, eolAnchor;
	private LexAction lexAction;
	
	public RuleAccept(DFAProduct product, NFACard ownerCard, boolean bolAnchor, boolean eolAnchor, LexAction action) {
		this.product = product;
		this.ownerCard = ownerCard;
		this.bolAnchor = bolAnchor;
		this.eolAnchor = eolAnchor;
		this.lexAction = action;
	}

	public LexAction getLexAction() {
		return lexAction;
	}


	public boolean isBolAnchor() {
		return bolAnchor;
	}

	public boolean isEolAnchor() {
		return eolAnchor;
	}


	public NFACard getOwnerCard() {
		return ownerCard;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NFAAccept [ownerCard=");
		builder.append(ownerCard.toString());
		builder.append(", bolAnchor=");
		builder.append(bolAnchor);
		builder.append(", eolAnchor=");
		builder.append(eolAnchor);
		builder.append(", action=");
		builder.append(lexAction);
		builder.append("]");
		return builder.toString();
	}

	public int getSID() {
		return ownerCard.getSID();
	}

	public void setLexAction(LexAction lexAction) {
		this.lexAction = lexAction;
	}
}
