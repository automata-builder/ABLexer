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

package com.abuilder.scriptparser.lexer.parser.toolbox;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.NFABldr;
import com.abuilder.auto.dfa.nfa.NFAMachine;
import com.abuilder.auto.dfa.nfa.NFAState;
import com.abuilder.auto.dfa.nfa.NFAMachHT;
import com.abuilder.auto.dfa.nfa.rx_range.RxRange;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.exception.MyLogicException;

public class LxDefineNFAMachMap {

	private final DFAProduct product;
	private final LinkedHashMap<String, LxDefineNfaHT> name_NfaMachHT = new LinkedHashMap<>();
	
	public LxDefineNFAMachMap(DFAProduct product) {
		this.product = product;
	}

	public void put(String name, NFAMachine m) {
		
		name_NfaMachHT.put(name, new LxDefineNfaHT(product, name, m));
	}
	

	public int size() {
		return name_NfaMachHT.size();
	}


	public LxDefineNfaHT get(String name) {
		return name_NfaMachHT.get(name);
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LxDefineNFAMachMap: size=" + name_NfaMachHT.size());
		for (LxDefineNfaHT subs : name_NfaMachHT.values()) {
			sb.append("\n" +subs);;
		}
		return sb.toString();
	}

	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class LxDefineNfaHT {
		
		private final DFAProduct product;
		private final String name;
		private NFAMachine subsMach;
		private ArrayDeque<Match> matchQ;
		private MatchMap org_Copy;
		private int rID, sID;
		
		public LxDefineNfaHT(DFAProduct product, String nameID, NFAMachine mach) {
			this.product = product;
			this.name = nameID;
			this.subsMach = mach;
		}

		/**
		 * 
		 * @author mike
		 *
		 */
		class MatchMap {
			private final TreeMap<NFAState, Match> org_Copy = new TreeMap<>();

			public Match put(NFAState org, Match value) {
				return org_Copy.put(org, value);
			}

			public Match get(NFAState key) {
				return org_Copy.get(key);
			}

			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				sb.append("MatchMap: size=" + org_Copy.size());
				for(Match m : org_Copy.values()){
					sb.append("\n" + m);
				}
				return sb.toString();
			}

			public int size() {
				return org_Copy.size();
			}
			
			
		}
		
		/**
		 * 
		 * @param org
		 * @return
		 */
		
		private NFAState addMatch(NFAState org){
			NFAState copy = new NFAState(product, rID, sID);
			Match m = new Match(org, copy);
			matchQ.add(m );
			org_Copy.put(org, m);
			return copy;
		}
		
		public NFAMachHT dumpMachCopy() {
			
			NFAMachine dumpHere = product.getNfaBldr().getNFAMach();
//			System.out.println("Dest: " + dumpHere);
//			System.out.println("Src: " + subsMach);
//			System.out.println();
			
			if(dumpHere.isDefineMach()){
				
				this.rID = NFABldr.DEFINE_RID;
				this.sID = NFABldr.DEFINE_SID;
				
			} else {
				
				this.rID = product.getNfaBldr().getCurrRuleRID();//getRuleCount();
				this.sID = product.getNfaBldr().getCurrRuleSID();//getSStateID();
			}
			
			this.org_Copy = new MatchMap();
			this.matchQ = new ArrayDeque<>();
						
			addMatch(subsMach.getRoot());
			NFAState tailOrg = null;
			HashSet<NFAState> done = new HashSet<>();
			
			while(matchQ.size() > 0){
				
				Match curr = matchQ.remove();
				if(curr.org.getEdge() != null){
					curr.copy.setEdge(curr.org.getEdge());
				}
				curr.copy.setType(curr.org.getType());
								
				for (int i = 0; i < 2; i++) {
					
					if(curr.org.getNext(i) != null){
						
						if(done.contains(curr.org.getNext(i))){
							if(curr.copy.getNext(i) == null){
								NFAState copy = org_Copy.get(curr.org.getNext(i)).copy;
								curr.copy.setNext(i, copy);
							}
							
							continue;
						}
						done.add(curr.org.getNext(i));
						
						NFAState copy = addMatch(curr.org.getNext(i));
						curr.copy.setNext(i, copy);
					}
				}
				
				if(curr.org.getNext1() == null && curr.org.getNext2() == null){
					if(tailOrg != null && tailOrg != curr.org){
						throw new MyLogicException();
					}
					tailOrg = curr.org;
				}
			}
			
			for(NFAState nfa : subsMach.nfaValues(0)){
				Match m = org_Copy.get(nfa);
				if(m == null){
					throw new MyLogicException();
				}
			}
			
			for(RxRange org : subsMach.getRxRangeMap().values()){
				NFAState headCopy = org_Copy.get(org.getHeadNFA()).copy;
				NFAState reCopy = org_Copy.get(org.getReNFA()).copy;
				NFAState gbCopy = org_Copy.get(org.getGbNFA()).copy;
				NFAState tailCopy = org_Copy.get(org.getTailNFA()).copy;
				dumpHere.addRxRange(headCopy, reCopy, gbCopy, tailCopy, org.getBounds());
			}
			
			NFAState machHeadCopy = org_Copy.get(subsMach.getRoot()).copy;
			NFAState machTailCopy = org_Copy.get(tailOrg).copy;
			
			cleanUp();
			
			return new NFAMachHT(machHeadCopy, machTailCopy);
		}

		private void cleanUp(){
			this.rID = sID = -1;
			this.org_Copy = null;
			this.matchQ = null;
		}
		/**
		 * 
		 * @author mike
		 *
		 */
		class Match {
			final NFAState org;
			final NFAState copy;
			
			public Match(NFAState org, NFAState copy) {
				this.org = org;
				this.copy = copy;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("org=");
				builder.append(org);
				builder.append("\ncopy=");
				builder.append(copy);
				return builder.toString();
			}
			
		}
		
		public String getNameID() {
			return name;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("AARxSubsNFAHT: ");
			builder.append("\n" + name);

			return builder.toString();
		}
		
	}

}
