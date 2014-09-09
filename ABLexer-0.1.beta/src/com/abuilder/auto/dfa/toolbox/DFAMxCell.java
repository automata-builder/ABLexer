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

import java.util.HashSet;

import com.abuilder.auto.dfa.nfa.Alphabet;
import com.abuilder.auto.dfa.nfa.NFATrnEdge;

public abstract class DFAMxCell {
	
	private final DFAProduct product;
	public final NFATrnEdge onEdge;//aka column id
	
	public DFAMxCell(DFAProduct product, NFATrnEdge onEdge) {
		this.product = product;
		this.onEdge = onEdge;
	}
		
	public abstract void remDFATran(DFAMxTrn mxCellValue);
	
	public abstract void addDFATran(int toTrMxID, int toDfaID) throws Exception;
	public abstract void addDFATran(DFAMxTrn mxCellValue);
	
	
	public abstract int size();
	public abstract HashSet<DFAMxTrn> values();
	public abstract boolean contains(DFAMxTrn v);
	
	public DFAProduct getProduct() {
		return product;
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class DFAMxEpsCell extends DFAMxMultiTrnCell {

		public DFAMxEpsCell(DFAProduct product) throws Exception {
			super(product, Alphabet.EpsCharEdge);
		}		
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class DFAMxMultiTrnCell extends DFAMxCell{
		
		private final HashSet<DFAMxTrn> cellValues = new HashSet<DFAMxTrn>();
		
		public DFAMxMultiTrnCell(DFAProduct product, NFATrnEdge onEdge) throws Exception {
			super(product, onEdge);
		}
		
		
		public void addDFATran(int toTrMxID, int toDfaID) throws Exception{
			addDFATran(new DFAMxTrn(getProduct(), toTrMxID, toDfaID));
		}


		public void addDFATran(DFAMxTrn mxCellValue){
			cellValues.add(mxCellValue);
		}
		
		@Override
		public void remDFATran(DFAMxTrn mxCellValue) {
			cellValues.remove(mxCellValue);
		}
		
		@Override
		public int size() {
			return cellValues.size();
		}

		@Override
		public HashSet<DFAMxTrn> values() {
			return cellValues;
		}

		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for(DFAMxTrn value : cellValues){
				if(i++ > 0){
					sb.append(", ");
				}
				sb.append(value);
			}
			return sb.toString();
		}
		
		@Override
		public boolean contains(DFAMxTrn v){
			return cellValues.contains(v);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof DFAMxMultiTrnCell)){
				return false;
			}
			DFAMxMultiTrnCell other = (DFAMxMultiTrnCell)obj;
			for(DFAMxTrn otherPair : other.values()){
				if(!cellValues.contains(otherPair)){
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class DFAMxSingleTrnCell extends DFAMxCell{
		
		private final DFAMxTrn value;
		
		public DFAMxSingleTrnCell(DFAProduct product, NFATrnEdge onEdge, int toTrMxID, int toDfaID)  {
			super(product, onEdge);
			if(onEdge == Alphabet.EpsCharEdge){
				throw new RuntimeException();
			}
			this.value = new DFAMxTrn(product, toTrMxID, toDfaID);
		}
		
		@Override
		public HashSet<DFAMxTrn> values() {
			HashSet<DFAMxTrn> set = new HashSet<>();
			set.add(value);
			return set;
		}
		
		@Override
		public boolean contains(DFAMxTrn v){
			return value.equals(v);
		}


		@Override
		public int size() {
			return 1;
		}

		public DFAMxTrn getValue() {
			return value;
		}


		@Override
		public void addDFATran(int toMxID, int toDfaID) throws Exception {
			if(!value.has(toMxID, toDfaID)){
				throw new Exception();
			}
		}

		@Override
		public void addDFATran(DFAMxTrn mxCellValue) {
			if(!this.value.equals(mxCellValue)){
				throw new IllegalArgumentException();
			}
		}

		@Override
		public String toString() {
			return ""+value;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof DFAMxSingleTrnCell)){
				return false;
			}
			DFAMxSingleTrnCell other = (DFAMxSingleTrnCell)obj;
			if(this.value.equals(other.value)){
				return true;
			}
			return false;
		}

		@Override
		public void remDFATran(DFAMxTrn mxCellValue) {
			throw new RuntimeException("Unimplemented");
		}
	}
}
