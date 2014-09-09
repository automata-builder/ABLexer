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

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.Alphabet;
import com.abuilder.auto.dfa.nfa.NFATrnEdge;
import com.abuilder.auto.dfa.nfa.rx_range.RxRange;
import com.abuilder.auto.dfa.toolbox.DFAMxCell.DFAMxEpsCell;
import com.abuilder.auto.dfa.toolbox.DFAMxCell.DFAMxMultiTrnCell;
import com.abuilder.auto.dfa.toolbox.DFAMxCell.DFAMxSingleTrnCell;
import com.abuilder.util.ABUtil;

public class DFAMxCollBldr {
	
	private final DFAProduct dfaProd;
	private final TreeMap<Integer, DFABaseMatrix> mxID_Mx = new TreeMap<Integer, DFABaseMatrix>();

	public DFAMxCollBldr(DFAProduct product) {
		this.dfaProd = product;
	}
	
	public void addDFARow(int mxID, DFAState dfa) throws Exception{
		
		DFABaseMatrix trnMx = mxID_Mx.get(mxID);
		if(trnMx == null){
			
			trnMx = initMx(mxID);
			mxID_Mx.put(mxID, trnMx);
		}
		trnMx.addDFARow(dfa);
	}
	
	private DFABaseMatrix initMx(int mxID){
		if(mxID == DFABaseMatrix.BASE_MXID){
			return new DFABaseMatrix(dfaProd, mxID);
		} 
		return new DFARngMatrix(dfaProd, mxID);
	}
	
	public void setDFATran(int fromMxID, int fromDfaID, NFATrnEdge onEdge, int toMxID, int toDfaID) throws Exception{
		
		DFABaseMatrix trnMx = mxID_Mx.get(fromMxID);
		trnMx.setDFATran(fromDfaID, onEdge, toMxID, toDfaID);
	}
		
	public DFAMxCell getDFATran(int fromMxID, int fromDfaID, NFATrnEdge onEdge){
		
		DFABaseMatrix trnMx = mxID_Mx.get(fromMxID);
		return trnMx.getDFATran(fromDfaID, onEdge);
	}
	
	public int rowSize(int mxID){
		return  mxID_Mx.get(mxID).getRowCount();
	}
	
	public int getMxCount(){
		return mxID_Mx.size();
	}
	
	
	public Set<Integer> getMxIDs() {
		return mxID_Mx.keySet();
	}

	public Collection<DFABaseMatrix> getDFAMxColl() {
		return mxID_Mx.values();
	}
	
	public DFABaseMatrix getDFAMatrix(int mxID){
		return getDFAMatrix(mxID, false); 
	}
	
	public DFABaseMatrix getDFAMatrix(int mxID, boolean create) {
		DFABaseMatrix mx =  mxID_Mx.get(mxID);
		
		if(mx == null && create){
			mx = initMx(mxID);
			mxID_Mx.put(mxID, mx);
		}
		return mx;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DFAEdgeMxCollBldr [mxID_Mx: Size=" + mxID_Mx.size());
		for(DFABaseMatrix dfaTrnMx : mxID_Mx.values()){
			sb.append("\n\n" + dfaTrnMx);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public String getMxFinals(){
		StringBuilder sb = new StringBuilder();
		sb.append("final static short REDFAID = " + DFAState.DFAID.REENTRY_DFAID + ";");
		sb.append("\nfinal static short BADDFAID = " + DFAState.DFAID.INVALID_DFAID+ ";");
		return sb.toString();
	}
	
	public short[] getMxID2ReDfaID(){
		if(mxID_Mx.size() == 1){
			return null;
		}
		short[] mxID2ReDfaID = new short[mxID_Mx.size() - 1];
		
		int i = 0;
		for(Integer mxID: mxID_Mx.tailMap(DFABaseMatrix.BASE_MXID, false).keySet()){

			DFARngMatrix mx = (DFARngMatrix)getDFAMatrix(mxID);
			mxID2ReDfaID[i++] = (short) mx.getReDfaID();
		}
		return mxID2ReDfaID;
	}
	
	public short[] getMxID2TailPair(){
		if(mxID_Mx.size() == 1){
			return null;
		}
		short[] mxID2TailPair = new short[(mxID_Mx.size() - 1)*2];
		
		int i = 0;
		for(Integer mxID: mxID_Mx.tailMap(DFABaseMatrix.BASE_MXID, false).keySet()){

			DFARngMatrix mx = (DFARngMatrix)getDFAMatrix(mxID);
			DFAMxTrn v = mx.getTailTrn();
			
			mxID2TailPair[i++] = (short) v.getToMxID();
			mxID2TailPair[i++] = (short) v.getToDfaID();
			
		}
		return mxID2TailPair;
	}
	

	public String getMxID2ReAndTailAsString(){
		
		String nl = ABUtil.getSysLineSep();
		StringBuilder reDfaID = new StringBuilder();
		StringBuilder tailPair = new StringBuilder();
		
		reDfaID.append("private final short[] mxID2ReDfaID = {"+nl+"\t");
		tailPair.append("private final short[] mxID2TailPair = {"+nl+"\t");
		
		int mxi = 0;
		for(Integer mxID: mxID_Mx.tailMap(DFABaseMatrix.BASE_MXID, false).keySet()){
			if(mxi++ > 0){
				reDfaID.append(", ");
				tailPair.append(", ");
			}
			DFARngMatrix mx = (DFARngMatrix)getDFAMatrix(mxID);

			reDfaID.append(mx.getReDfaID());
			DFAMxTrn v = mx.getTailTrn();
			tailPair.append(v.getToMxID() + ", " + v.getToDfaID());
		}
		reDfaID.append(nl + "};");
		tailPair.append(nl + "};");
		return reDfaID.toString() + nl + tailPair.toString();
	}
		
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class DFARngMatrix extends DFABaseMatrix {

		private DFAMxSingleTrnCell reTrn, tailTrn;
		private final int huggerRngID;
		private final int ruleID;
		
		public DFARngMatrix(DFAProduct dfaProd, int mxID) {
			super(dfaProd, mxID);
			RxRange range = dfaProd.getNFAMach().getRxRangeMap().getWithRngID(mxID);
			this.huggerRngID = range.getHeadNFA().getHuggerRngID();
			this.ruleID = range.getHeadNFA().getRuleID();
		}		
		
		public void addTailTran(int toMxID, int toDfaID) throws Exception{
					
			if(tailTrn == null){
				
				tailTrn = new DFAMxSingleTrnCell(getProduct(), null, toMxID, toDfaID);
			
			} else {
				
				if(!tailTrn.getValue().has(toMxID, toDfaID)){
					throw new Exception();
				}
			}
		}

		public void addReTran(int toMxID, int toDfaID) throws Exception{
			
			if(reTrn == null){
				
				reTrn = new DFAMxSingleTrnCell(getProduct(), null, toMxID, toDfaID);
			
			} else {
				
				if(!reTrn.getValue().has(toMxID, toDfaID)){
					throw new Exception();
				}
			}
		}
		
		public void replaceReTran(int newToDfaID) { 
			System.out.print("Replacing reTran:\n\told: " + reTrn);
			reTrn = new DFAMxSingleTrnCell(getProduct(), null, reTrn.getValue().getToMxID(), newToDfaID);
			System.out.println("-> new: " + reTrn);
		}
		
		public void replaceTailTran(int newToDfaID) { 
			System.out.print("Replacing tailTran:\n\told: " + tailTrn);
			tailTrn = new DFAMxSingleTrnCell(getProduct(), null, tailTrn.getValue().getToMxID(), newToDfaID);
			System.out.println("-> new: " + tailTrn);
		}
		
		public DFAMxSingleTrnCell getTailTranCell() {
			return tailTrn;
		}

		public DFAMxSingleTrnCell getReTranCell() {
			return reTrn;
		}

		public int getReDfaID() {
			if(reTrn == null){
				return DFAState.DFAID.INVALID_DFAID; //re=null [{0,1} or '?']
			}
			return reTrn.getValue().getToDfaID();
		}

		public DFAMxTrn getTailTrn(){
			return tailTrn.getValue();
		}

		public int getHuggerRngID() {
			return huggerRngID;
		}
		
		public int getRuleID() {
			return ruleID;
		}
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class DFABaseMatrix {
		
		public static final int BASE_MXID = 0;
		public static final int RANGE_INIT_MXID = BASE_MXID + 1;
		public static final int INVALID_MXID = BASE_MXID - 1;		
		public static final int ACCEPT_MXID = INVALID_MXID - 1;
		
		private final DFAProduct product;
		public final int mxID;
		private final TreeMap<Integer, DFAMxRow> index_Row = new TreeMap<>();
		private boolean hasNonEpsTrn = false;
		
		public DFABaseMatrix(DFAProduct product, int mxID) {
			this.product = product;
			this.mxID = mxID;
		}
		
		public TreeMap<Integer, DFAMxRow> getRowMap(){
			return index_Row;
		}
		
		public void addDFARow(DFAState dfa) throws Exception{
			
			if(index_Row.size() != dfa.dfaID){
				throw new Exception();
			}

			index_Row.put(index_Row.size(), new DFAMxRow(product, dfa));
		}
				
		public void setDFATran(int fromDfaID, NFATrnEdge onEdge, int toMxID, int toDfaID) throws Exception{
			index_Row.get(fromDfaID).setDFATran(onEdge, toMxID, toDfaID);
			if(onEdge != Alphabet.EpsCharEdge){
				hasNonEpsTrn = true;
			}
		}
		
		
		public DFAMxCell getDFATran(int fromDfaID, NFATrnEdge onEdge){
			return index_Row.get(fromDfaID).getDFATran(onEdge);
		}
		
		public int getRowCount(){
			return index_Row.size();
		}

		public DFAProduct getProduct() {
			return product;
		}

		public DFAMxRow getDFAMxRow(int dfaID) {
			return index_Row.get(dfaID);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("mxID="+mxID);
			for(DFAMxRow row : index_Row.values()){
				sb.append("\n" + row);
			}
			sb.append("]");
			return sb.toString();
		}

		public int getMxID() {
			return mxID;
		}

		public boolean hasNonEpsTran() {
			return hasNonEpsTrn;
		}

		public Collection<DFAMxRow> getRows() {
			return index_Row.values();
		}
		
		DFAMxRow[] toRowArray(){
			return index_Row.values().toArray(new DFAMxRow[index_Row.size()]);
		}
		
		public DFAMxRow getRow(int index) {
			return index_Row.get(index);
		}
		
		public DFAMxRow removeRow(int index){
			return index_Row.remove(index);
		}
		
		public void swapRowID(int oldRowID, int newRowID){
			DFAMxRow row = index_Row.remove(oldRowID);
			row.setRowID(newRowID);
			index_Row.put(row.getRowID(), row);
		}
		
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class DFAMxRow {
	
		private final DFAProduct product;
		public int rowID; //dfaID
		private final TreeMap<NFATrnEdge, DFAMxCell> onEdge_MxCell = new TreeMap<>();
		private final boolean isAccept;
		
		public DFAMxRow(DFAProduct product, DFAState dfa) {
			this.product = product;
			this.rowID = dfa.dfaID;
			this.isAccept = dfa.isAccept();
		}
				
		public void setDFATran(NFATrnEdge onEdge, int toMxID, int toDfaID) throws Exception{
			DFAMxCell cell = onEdge_MxCell.get(onEdge);
			if(cell == null){
				if(onEdge == Alphabet.EpsCharEdge){
					cell = new DFAMxEpsCell(product);
				} else {
					//cell = new SingleValueCell(product, onEdge, toMxID, toDfaID);
					cell = new DFAMxMultiTrnCell(product, onEdge);
				}
				onEdge_MxCell.put(onEdge, cell);
			}
			
			cell.addDFATran(toMxID, toDfaID);
		}
		
		
		public DFAMxCell getDFATran(NFATrnEdge onEdge){
			return onEdge_MxCell.get(onEdge);
		}

		public boolean isAccept() {
			return isAccept;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("DFATrnMxRow [dfaID=");
			sb.append(rowID);
			sb.append(", isAccept=");
			sb.append(isAccept);
			sb.append(", onEdge_MxCell=");
			for(NFATrnEdge onEdge : onEdge_MxCell.keySet()){
				sb.append("\nonEdge=" + onEdge);
				DFAMxCell cell = onEdge_MxCell.get(onEdge);
				sb.append(" " + cell);
			}
			
			sb.append("]");
			return sb.toString();
		}

		public int size() {
			return onEdge_MxCell.size();
		}

		public Set<NFATrnEdge> onEdges() {
			return onEdge_MxCell.keySet();
		}		
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof DFAMxRow)){
				return false;
			}
			DFAMxRow other = (DFAMxRow)obj;
			if(this.isAccept != other.isAccept()){
				return false;
			}
			if(this.size() != other.size()){
				return false;
			}
			
			for(NFATrnEdge edge : this.onEdge_MxCell.keySet()){
				DFAMxCell otherCell = other.getDFATran(edge);
				if(otherCell == null){
					return false;
				}
				DFAMxCell myCell = this.onEdge_MxCell.get(edge);
				if(!myCell.equals(otherCell)){
					return false;
				}
			}
			return true;
		}

		public Collection<DFAMxCell> values() {
			return onEdge_MxCell.values();
		}

		public int getRowID() {
			return rowID;
		}

		public void setRowID(int rowID) {
			this.rowID = rowID;
		}
	}
}
