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

package com.abuilder.auto.dfa.toolbox;

import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.Alphabet;
import com.abuilder.auto.dfa.nfa.NFABldr;
import com.abuilder.auto.dfa.nfa.NFAMachine;
import com.abuilder.auto.dfa.nfa.NFAState;
import com.abuilder.auto.dfa.nfa.RxRangeMap;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.rx_range.RxRange;

import com.abuilder.scriptparser.lexer.parser.toolbox.LxDefineNFAMachMap;

public class DFAProduct  {
	
	private final DFAStateIDCntr dfaIDCntr = new DFAStateIDCntr();
	private final DFAStateCollBldr dfaStateCollBldr = new DFAStateCollBldr(this);
	private final DFAMxCollBldr dfaMxCollBldr = new DFAMxCollBldr(this);
	private DFACharIDMxCollBldr dfaCharIDMxCollBldr;
	private final LxDefineNFAMachMap rxSubsMap = new LxDefineNFAMachMap(this);
	private NFABldr nfaBldr;
	private int nfaMachCntr;
	
	public int getNextNFAMachID(){
		return nfaMachCntr++;
	}
	
	public void addDefine(String name, NFAMachine m) {
		rxSubsMap.put(name, m);
	}
	
	public NFAMachine getNFAMach(){
		return nfaBldr.getNFAMach();
	}
	
	public Alphabet getAlphabet(){
		return getNFAMach().getAlphabet();
	}
	

	
	public RxRangeMap getRxRangeMap() {
		return getNFAMach().getRxRangeMap();
	}
	
	public int getNextDfaID(int trMxID) {
		return dfaIDCntr.getNextDfaID(trMxID);
	}

	public NFAState remNFA(NFAState nfa) {
		return getNFAMach().remNFA(nfa);
	}


	public Integer getNfaCount() {
		return getNFAMach().getNfaCount();
	}
	
	public NFAState getNFA(NFACard p){
		return getNFAMach().getNFA(p);
	}



	/**
	 * 
	 * @author mike
	 *
	 */
	
	class DFAStateIDCntr{
		
		private final static int DFAID_INIT_VAL = 0;
		private final TreeMap<Integer, Integer> trMxID_Cntr = new TreeMap<Integer, Integer>();
		private int one4AllDfaIDCntr;
		
		public int getNextDfaID(int trMxID){
			
			Integer cntr = trMxID_Cntr.get(trMxID);
			
			if(cntr == null){
				cntr = DFAID_INIT_VAL;
			}
			
			trMxID_Cntr.put(trMxID, cntr + 1);
			
			return cntr;
		}

		public int getNextOne4AllDfaID() {
			return one4AllDfaIDCntr++;
		}
	}
	
	public RxRange getRxRangeWithReID(int reID) {
		return getNFAMach().getRxRangeWithReID(reID);
	}

	public RxRange getRxRangeWithRngID(int rngID) {
		return getNFAMach().getRxRangeWithRngID(rngID);
	}

	public DFAStateCollBldr getDFAStateCollBldr() {
		return dfaStateCollBldr;
	}

	public DFAMxCollBldr getDFAMxCollBldr() {
		return dfaMxCollBldr;
	}

	public int getNextOne4AllDfaID() {
		return dfaIDCntr.getNextOne4AllDfaID();
	}



	public DFACharIDMxCollBldr getDfaCharIDMxCollBldr() {
		return dfaCharIDMxCollBldr;
	}

	public void convertEdge2ChIDColumn() throws Exception {
		this.dfaCharIDMxCollBldr = new DFACharIDMxCollBldr(this);
		dfaCharIDMxCollBldr.edge2ChIDColumn();
	}

	public LxDefineNFAMachMap getLxDefineNfaHTMap() {
		return rxSubsMap;
	}
	
	public NFABldr getNfaBldr() {
		return nfaBldr;
	}

	public void setNfaBldr(NFABldr nfaBldr) {
		this.nfaBldr = nfaBldr;
	}
}


