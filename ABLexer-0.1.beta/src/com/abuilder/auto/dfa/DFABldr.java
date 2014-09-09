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

package com.abuilder.auto.dfa;

import java.util.ArrayDeque;
import java.util.TreeMap;
import java.util.TreeSet;

import com.abuilder.auto.dfa.nfa.Alphabet;
import com.abuilder.auto.dfa.nfa.NFAMachine;
import com.abuilder.auto.dfa.nfa.NFATrnEdge;
import com.abuilder.auto.dfa.nfa.NFAConstants.NFAType;
import com.abuilder.auto.dfa.nfa.NFAState.NFACard;
import com.abuilder.auto.dfa.nfa.closure.NFACls;
import com.abuilder.auto.dfa.nfa.closure.NFAClsCollBldr;
import com.abuilder.auto.dfa.nfa.closure.NFAClsCollBldr.NFAClsList;
import com.abuilder.auto.dfa.nfa.rx_range.RxRange;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr;
import com.abuilder.auto.dfa.toolbox.DFAState;
import com.abuilder.auto.dfa.toolbox.DFAStateCollBldr;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFARngMatrix;
import com.abuilder.auto.dfa.toolbox.MinimizeHelper;
import com.abuilder.auto.dfa.toolbox.MxCollMatchCellMinimizer;
import com.abuilder.auto.dfa.toolbox.MxCollRedundantEpsCellMinimizer;
import com.abuilder.auto.dfa.toolbox.NullDFARowRemover;
import com.abuilder.exception.MyLogicException;

public class DFABldr {

	private final DFAProduct dfaProd;
	private final NFAMachine nfaMach;
	private final NFAClsCollBldr nfaClsCollBldr;
	private final DFAStateCollBldr dfaStateCollBldr;
	private final DFAMxCollBldr dfaMxCollBldr;
	private final ArrayDeque<DFAState> newDfaQue = new ArrayDeque<DFAState>();
	
	public DFABldr(DFAProduct product){
		this.dfaProd = product;
		this.nfaMach = product.getNFAMach();
		this.nfaClsCollBldr = new NFAClsCollBldr(product);
		this.dfaStateCollBldr = product.getDFAStateCollBldr();
		this.dfaMxCollBldr = product.getDFAMxCollBldr();
	}
	
	public void buildDFA() throws Exception{

		initDFAMxColl();
		buildDFAMxColl();
		
		
		MinimizeHelper minHelp = new MinimizeHelper(dfaProd);
		MxCollMatchCellMinimizer matchCellMin = new MxCollMatchCellMinimizer(dfaProd, minHelp);
//		matchCellMin.remove();
		MxCollRedundantEpsCellMinimizer redEpsMin = new MxCollRedundantEpsCellMinimizer(dfaProd);
		redEpsMin.remove();
		
		NullDFARowRemover nullRowRem = new NullDFARowRemover(dfaProd, minHelp);
		nullRowRem.remove();
		
		dfaProd.convertEdge2ChIDColumn();
		
		//System.out.println(nfaMach);
	}

	
	public void initDFAMxColl() throws Exception {

		int mxID = DFABaseMatrix.BASE_MXID;
		TreeSet<NFACard> initNfaIDs = new TreeSet<>();
		initNfaIDs.add(nfaMach.getRootCard());

		NFAClsList nfaClsList = nfaClsCollBldr.getNFAClsList(mxID, initNfaIDs);

		for (NFACls nfaCls : nfaClsList.getList()) {
			addNewDFA(nfaCls);
		}
	}

	private void buildDFAMxColl() throws Exception {

		while (newDfaQue.size() > 0) {

			DFAState prnDfa = newDfaQue.remove();
						
			for (NFATrnEdge onEdge : prnDfa.getTrChIDs()) {

				TreeMap<Integer, TreeSet<NFACard>> trMxID_NfaIDs = prnDfa.getTrMxID_NfaIDs(onEdge);
				
				for(Integer trMxID : trMxID_NfaIDs.keySet()){
							
		
					NFAClsList nfaClsList = nfaClsCollBldr.getNFAClsList(trMxID, trMxID_NfaIDs.get(trMxID));	
					
					for (NFACls trnCls : nfaClsList.getList()) {

						if (trnCls.isReType()) {
							setReTran(prnDfa, onEdge, trnCls);
							continue;
						}
						
						if (trnCls.isTailType()) {
							setTailTran(trnCls, prnDfa);
							continue;
						}

						
						if (trnCls.getTrEdgeSet() == null) {
							noTrChIDsCase(trnCls, prnDfa, onEdge);
							continue;
						}

						DFAState toDfa = getToDFA(trnCls);
						if(toDfa.isAccept()){
							setAccTran(trnCls, prnDfa, onEdge);
						}
						
						dfaMxCollBldr.setDFATran(prnDfa.myMxID, prnDfa.dfaID, onEdge, toDfa.myMxID, toDfa.dfaID);
					}
				}
			}
		}
	}
	
	private DFAState getToDFA(NFACls nfaCls) throws Exception{
		
		DFAState toDfa = dfaStateCollBldr.getDFAState(nfaCls);
		if (toDfa == null) {
			toDfa = addNewDFA(nfaCls);
		}
		return toDfa;
	}

	
	private void setReTran(DFAState prnDfa, NFATrnEdge onEdge, NFACls reCls) throws Exception{
		
		
		DFARngMatrix dfaMx = (DFARngMatrix) dfaMxCollBldr.getDFAMatrix(reCls.myMxID, true);
		dfaMxCollBldr.setDFATran(prnDfa.myMxID, prnDfa.dfaID, onEdge, reCls.myMxID, DFAState.DFAID.REENTRY_DFAID);
		
		if (reCls.getTrEdgeSet() != null){
			DFAState toDfa = getToDFA(reCls);
			dfaMx.addReTran(reCls.myMxID, toDfa.dfaID);
			return;
		}
		
		throw new Exception();
	}
	
	private void setTailTran(NFACls tailCls, DFAState prnDfa) throws Exception{
		
		RxRange tailsRng = dfaProd.getNFA(tailCls.getPrnNFACard()).getRange(NFAType.RNG_TL);
		
		{//erase later
			int tailRngID = tailsRng.getHeadNFA().getNext1().getHuggerRngID();
			if(tailsRng.rngID != tailRngID){
				throw new MyLogicException();
			}
		}
		
		DFARngMatrix dfaMx = (DFARngMatrix) dfaMxCollBldr.getDFAMatrix(tailsRng.rngID, true);
		int dfaID = DFAState.DFAID.INVALID_DFAID;	
		
		if (tailCls.getTrEdgeSet() != null){
			
			DFAState toDfa = getToDFA(tailCls);
			dfaID = toDfa.dfaID;
			dfaMx.addTailTran(tailCls.myMxID, toDfa.dfaID);
		
		} else {
			
			throw new MyLogicException();
		}
		
		if(prnDfa.getMyTrMxID() != tailsRng.rngID){//prnDfa=Range Head, minBound=0; [{0, num} or *]
			
			if(tailsRng.getMinBnd() != 0){
				throw new Exception();
			}
			dfaMxCollBldr.setDFATran(prnDfa.myMxID, prnDfa.dfaID, Alphabet.EpsCharEdge, prnDfa.myMxID, dfaID);
		
		} else if(tailsRng.getMaxBnd() < 2){//prnDfa=Range Goback & maxBound=1; [ {0,1} or '?' ]
		
			dfaMxCollBldr.setDFATran(prnDfa.myMxID, prnDfa.dfaID, Alphabet.EpsCharEdge, tailsRng.rngID, DFAState.DFAID.REENTRY_DFAID);
		}
		
	}
	
	private void noTrChIDsCase(NFACls nfaCls, DFAState prnDfa, NFATrnEdge onEdge) throws Exception {
		
		if (nfaCls.isAccept()) {
			setAccTran(nfaCls, prnDfa, onEdge);
		}		
	}

	private void setAccTran(NFACls nfaCls, DFAState prnDfa, NFATrnEdge onEdge) throws Exception{

		for(Integer ssID : nfaCls.getAccSSIDs()){
			
			dfaMxCollBldr.setDFATran(prnDfa.myMxID, prnDfa.dfaID, onEdge, DFABaseMatrix.ACCEPT_MXID, -(nfaCls.getTopRuleID(ssID) + 1));
		}
	}
	
	private DFAState addNewDFA(NFACls nfaCls) throws Exception {
		DFAState newDfa = dfaStateCollBldr.add(new DFAState(dfaProd, nfaCls));
		dfaMxCollBldr.addDFARow(newDfa.myMxID, newDfa);

		if (newDfa.getTrChIDs() != null) {
			newDfaQue.add(newDfa);
		}
		return newDfa;
	}
}
