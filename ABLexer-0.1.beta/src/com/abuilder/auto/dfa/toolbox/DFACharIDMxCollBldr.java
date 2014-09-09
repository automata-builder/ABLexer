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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

//import sun.print.resources.serviceui;

import com.abuilder.auto.dfa.nfa.Alphabet;
import com.abuilder.auto.dfa.nfa.NFATrnEdge;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.CharClassEdge;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.EdgeType;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.SimpleEdge;
import com.abuilder.auto.dfa.toolbox.DFAMxCell.DFAMxEpsCell;
import com.abuilder.auto.dfa.toolbox.DFAMxCell.DFAMxMultiTrnCell;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFAMxRow;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFARngMatrix;
import com.abuilder.util.ABUtil;

public class DFACharIDMxCollBldr {

	private final DFAProduct product;
	private final Alphabet alphabet;
	private final DFAMxCollBldr dfaEdgeMxCollBldr;
	private final TreeMap<Integer, DFACharIDMatrix> mxID_chIDMx = new TreeMap<>();

	public DFACharIDMxCollBldr(DFAProduct product) {
		this.product = product;
		this.alphabet = product.getAlphabet();
		this.dfaEdgeMxCollBldr = product.getDFAMxCollBldr();
	}

	public void edge2ChIDColumn() throws Exception {

		for (DFABaseMatrix edgeMx : dfaEdgeMxCollBldr.getDFAMxColl()) {
			DFACharIDMatrix chIDMx = new DFACharIDMatrix(edgeMx.mxID);
			mxID_chIDMx.put(edgeMx.mxID, chIDMx);
			nonEpsEdge2CharID(edgeMx, chIDMx);
		}
	}

	private void nonEpsEdge2CharID(DFABaseMatrix edgeMx, DFACharIDMatrix chIDMx) throws Exception {

		if (!edgeMx.hasNonEpsTran()) {
			return;
		}

		for (int r = 0; r < edgeMx.getRowCount(); ++r) {

			DFAMxRow edgeRow = edgeMx.getDFAMxRow(r);

			for (NFATrnEdge onEdge : edgeRow.onEdges()) {

				if (onEdge == Alphabet.EpsCharEdge) {
					continue;
				}

				// SingleValueCell cell = (SingleValueCell)edgeRow.getDFATran(onEdge);
				DFAMxMultiTrnCell cell = (DFAMxMultiTrnCell) edgeRow.getDFATran(onEdge);

				if (onEdge.getType() == EdgeType.SIMPLE) {

					int onCh = ((SimpleEdge) onEdge).getChar();
					int onChID = alphabet.getCharIDWithChar(onCh);

					for (DFAMxTrn v : cell.values()) {
						chIDMx.setDFATran(r, onChID, v.getToDfaID());
					}
					// chIDMx.setDFATran(r, onChID, cell.getValue().getToDfaID());

				} else {// Char Class

					CharClassEdge onCcEdge = (CharClassEdge) onEdge;

					for (int onCh = onCcEdge.nextSetBit(0); onCh >= 0; onCh = onCcEdge.nextSetBit(onCh + 1)) {
						int onChID = alphabet.getCharIDWithChar(onCh);

						for (DFAMxTrn v : cell.values()) {
							chIDMx.setDFATran(r, onChID, v.getToDfaID());
						}
						// chIDMx.setDFATran(r, onChID, cell.getValue().getToDfaID());
					}
				}
			}
		}
	}

	public byte[][] getNonEpsMxIndex2True(){
		
		byte[][] nonEpsMxIndex2True = new byte[mxID_chIDMx.size()][];
	
		int mxi = -1;
		for (DFACharIDMatrix chIDMx : mxID_chIDMx.values()) {

			++mxi;
			
			DFABaseMatrix edgeMx = dfaEdgeMxCollBldr.getDFAMatrix(chIDMx.mxID);
			TrueTableBldr trueTabBldr = new TrueTableBldr(edgeMx.getRowCount(), alphabet.charIDSize());

			for (int r = 0; r < dfaEdgeMxCollBldr.rowSize(chIDMx.mxID); ++r) {

				DFACharIDMxRow row = chIDMx.getRow(r);
				if (row == null) {
					continue;
				}
				for (int onChID = 0; onChID < alphabet.charIDSize(); ++onChID) {

					if (onChID != Alphabet.CHID.EPS_CHID && row.getDFATran(onChID) != null) {
						trueTabBldr.setTrueBoolValue(row.rowID, onChID);
					}

				}
			}

			nonEpsMxIndex2True[mxi] = trueTabBldr.getTrueTab();
		}
		
		
		return nonEpsMxIndex2True;
	}
	
	public String getNonEpsMxIndex2TrueAsString() {// For non-eps values

		String nl = ABUtil.getSysLineSep();

		StringBuilder sb = new StringBuilder();
		sb.append("private final byte[][] nonEpsMxIndex2True = {");

		int mxi = 0;
		for (DFACharIDMatrix chIDMx : mxID_chIDMx.values()) {

			DFABaseMatrix edgeMx = dfaEdgeMxCollBldr.getDFAMatrix(chIDMx.mxID);
			TrueTableBldr trueTabBldr = new TrueTableBldr(edgeMx.getRowCount(), alphabet.charIDSize());

			for (int r = 0; r < dfaEdgeMxCollBldr.rowSize(chIDMx.mxID); ++r) {

				DFACharIDMxRow row = chIDMx.getRow(r);
				if (row == null) {
					continue;
				}
				for (int onChID = 0; onChID < alphabet.charIDSize(); ++onChID) {

					if (onChID != Alphabet.CHID.EPS_CHID && row.getDFATran(onChID) != null) {
						trueTabBldr.setTrueBoolValue(row.rowID, onChID);
					}

				}
			}

			if (mxi++ > 0)
				sb.append(", ");
			sb.append(nl + "\t{ ");

			for (int i = 0; i < trueTabBldr.size(); ++i) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(trueTabBldr.getBitSet(i));
			}
			sb.append(" }");
		}
		sb.append(nl + "};");
		return sb.toString();
	}

	public short[][] getEpsValueIndex() {
		
		short[][] epsValueIndex = new short[dfaEdgeMxCollBldr.getDFAMxColl().size()][];

		int mxi = -1;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			++mxi;

			ArrayList<Integer> list = new ArrayList<>();

			int valueIndex = 0;
			for (DFAMxRow row : mx.getRows()) {

				DFAMxEpsCell cell = (DFAMxEpsCell) row.getDFATran(Alphabet.EpsCharEdge);

				if (cell != null) {

					list.add(valueIndex);
					valueIndex += cell.size() * 2;
				}
			}
			epsValueIndex[mxi] = new short[list.size()];
			for (int i = 0; i < epsValueIndex[mxi].length; i++) {
				epsValueIndex[mxi][i] = list.get(i).shortValue();
			}
		}

		return epsValueIndex;
	}

	public short[][] getEpsValue(){
		
		short[][] epsValue = new short[dfaEdgeMxCollBldr.getDFAMxColl().size()][];

		int mxi = -1;
		
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			++mxi;

			ArrayList<Integer> list = new ArrayList<>();
			
			for (DFAMxRow row : mx.getRows()) {

				DFAMxEpsCell cell = (DFAMxEpsCell) row.getDFATran(Alphabet.EpsCharEdge);

				if (cell != null) {

					for (DFAMxTrn v : cell.values()) {
				
						list.add(v.getToMxID());
						list.add(v.getToDfaID());
					}
				}
			}
			
			epsValue[mxi] = new short[list.size()];
			for (int i = 0; i < epsValue[mxi].length; i++) {
				epsValue[mxi][i] = list.get(i).shortValue();
			}
		}

		return epsValue;
	}
	
	public String getEpsValueIndexAndValueAsString() {

		String nl = ABUtil.getSysLineSep();

		StringBuilder index2Value = new StringBuilder("private final short[][] epsValueIndex = {" + nl + "\t");
		StringBuilder value = new StringBuilder("private final short[][] epsValue = {" + nl + "\t");

		int mxi = 0;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			if (mxi++ > 0) {
				index2Value.append(", ");
				value.append(", ");
			}

			index2Value.append("{ ");
			value.append("{ ");

			int ri = 0;
			int valueIndex = 0;
			for (DFAMxRow row : mx.getRows()) {

				DFAMxEpsCell cell = (DFAMxEpsCell) row.getDFATran(Alphabet.EpsCharEdge);

				if (cell != null) {

					if (ri++ > 0) {
						index2Value.append(", ");
						value.append(", ");
					}

					index2Value.append(valueIndex);
					valueIndex += cell.size() * 2;

					int vi = 0;
					for (DFAMxTrn v : cell.values()) {
						if (vi++ > 0) {
							value.append(", ");
						}
						value.append(v.getToMxID() + ", " + v.getToDfaID());
					}

				}
			}

			index2Value.append(" }");
			value.append(" }");
		}

		index2Value.append(nl + "};");
		value.append(nl + "};");

		return index2Value.toString() + nl + nl + value.toString();
	}

	public short[][] getNonEpsValueIndexTable(){
		short[][] nonEpsValueIndex = new short[mxID_chIDMx.size()][];
		
		int mxi = -1;
		for (DFACharIDMatrix chIDMx : mxID_chIDMx.values()) {

			++mxi;

			ArrayList<Integer> list = new ArrayList<>();
			int valueIndex = 0;

			for (DFACharIDMxRow row : chIDMx.rows()) {
	
				for (Integer onChID : row.chIDs()) {

					list.add(valueIndex);

					DFACharIDMxCell cell = row.getDFATran(onChID);
					valueIndex += cell.size();
				}
			}
			nonEpsValueIndex[mxi] = new short[list.size()];
			for (int i = 0; i < nonEpsValueIndex[mxi].length; i++) {
				nonEpsValueIndex[mxi][i] = list.get(i).shortValue();
			}
		}
		return nonEpsValueIndex;
	}
	
	public short[][] getNonEpsValueTable(){
		
		short[][] nonEpsValue = new short[mxID_chIDMx.size()][];
		
		int mxi = -1;
		for (DFACharIDMatrix chIDMx : mxID_chIDMx.values()) {

			++mxi;

			ArrayList<Integer> list = new ArrayList<>();
	
			for (DFACharIDMxRow row : chIDMx.rows()) {

				for (Integer onChID : row.chIDs()) {

	
					DFACharIDMxCell cell = row.getDFATran(onChID);

					for (Integer dfaID : cell.dfaIDs) {
						list.add(dfaID);
					}
				}

			}
			
			nonEpsValue[mxi] = new short[list.size()];
			for (int i = 0; i < nonEpsValue[mxi].length; i++) {
				nonEpsValue[mxi][i] = list.get(i).shortValue();
			}
		}
		return nonEpsValue;
	}
	
	public String getNonEpsValueIndexAndValueAsString() {

		String nl = ABUtil.getSysLineSep();
		StringBuilder index2Value = new StringBuilder("private final short[][] nonEpsValueIndex = {");
		StringBuilder value = new StringBuilder("private final short[][] nonEpsValue = {");

		int mxi = 0;
		for (DFACharIDMatrix chIDMx : mxID_chIDMx.values()) {

			if (mxi++ > 0) {
				index2Value.append(",");
				value.append(",");
			}
			index2Value.append(nl + "\t{ ");
			value.append(nl + "\t{ ");

			int valueIndex = 0;

			int ri = 0;
			for (DFACharIDMxRow row : chIDMx.rows()) {

				if (ri++ > 0) {
					index2Value.append(", ");
					value.append(", ");
				}

				int ci = 0;
				for (Integer onChID : row.chIDs()) {

					if (ci++ > 0) {
						index2Value.append(", ");
						value.append(", ");
					}
					index2Value.append(valueIndex);

					DFACharIDMxCell cell = row.getDFATran(onChID);
					valueIndex += cell.size();

					int di = 0;
					for (Integer dfaID : cell.dfaIDs) {
						if (di++ > 0) {
							value.append(", ");
						}
						value.append(dfaID);
					}
				}

			}

			index2Value.append(" }");
			value.append(" }");
		}

		index2Value.append(nl + "};");
		value.append(nl + "};");

		return index2Value.toString() + nl + nl + value.toString();
	}

	public int[][] getNonEpsOneDimMxIndex2ValueIndex(){
		
		int[][] nonEpsOneDimMxIndex2ValueIndex = new int[mxID_chIDMx.size()][];
		
		int mxi = -1;
		for (DFACharIDMatrix chIDMx : mxID_chIDMx.values()) {
			
			++mxi;
			
			ArrayList<Integer> list = new ArrayList<>();
			
			for (DFACharIDMxRow row : chIDMx.rows()) {

				for (Integer onChID : row.chIDs()) {

			
					int oneDimMxIndex = row.rowID * alphabet.charIDSize() + onChID;

					list.add(oneDimMxIndex);
				}
			}
			
			nonEpsOneDimMxIndex2ValueIndex[mxi] = new int[list.size()];
			for (int i = 0; i < nonEpsOneDimMxIndex2ValueIndex[mxi].length; i++) {
				nonEpsOneDimMxIndex2ValueIndex[mxi][i] = list.get(i);
			}
		}
		return nonEpsOneDimMxIndex2ValueIndex;
	}
	
	public String getNonEpsOneDimMxIndex2ValueIndexAsString() {

		String nl = ABUtil.getSysLineSep();
		
		// //Changed 'short' to 'int', because of 'BinarySearch.binarySearch()' &
		// 'nonEpsOneDimMxIndex2ValueIndex'
		StringBuilder sb = new StringBuilder("private final int[][] nonEpsOneDimMxIndex2ValueIndex = {" + nl);

		int mxi = 0;
		for (DFACharIDMatrix chIDMx : mxID_chIDMx.values()) {

			if (mxi++ > 0) {
				sb.append("," + nl);
			}

			sb.append("\t{ ");

			int ri = 0;
			for (DFACharIDMxRow row : chIDMx.rows()) {

				if (ri++ > 0) {
					sb.append(", ");
				}

				int ci = 0;
				for (Integer onChID : row.chIDs()) {

					if (ci++ > 0) {
						sb.append(", ");
					}

					int oneDimMxIndex = row.rowID * alphabet.charIDSize() + onChID;

					sb.append(oneDimMxIndex);
				}
			}
			sb.append(" }");

		}
		sb.append(nl + "};");
		return sb.toString();
	}

	public int[][] getEpsRowIndex2ValueIndex() {

		int[][] epsRowIndex2ValueIndex = new int[dfaEdgeMxCollBldr.getDFAMxColl().size()][];

		int mxi = -1;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			++mxi;

			ArrayList<Integer> list = new ArrayList<>();

			for (DFAMxRow row : mx.getRows()) {
				DFAMxEpsCell cell = (DFAMxEpsCell) row.getDFATran(Alphabet.EpsCharEdge);
				if (cell != null) {
					list.add(row.rowID);

				}
			}

			epsRowIndex2ValueIndex[mxi] = new int[list.size()];
			for (int i = 0; i < epsRowIndex2ValueIndex[mxi].length; i++) {
				epsRowIndex2ValueIndex[mxi][i] = list.get(i);
			}
		}
		return epsRowIndex2ValueIndex;
	}

	public String getEpsRowIndex2ValueIndexAsString() {

		String nl = ABUtil.getSysLineSep();

		// Changed 'short' to 'int', because of 'BinarySearch.binarySearch()' &
		// 'nonEpsOneDimMxIndex2ValueIndex'

		StringBuilder sb = new StringBuilder("private final int[][] epsRowIndex2ValueIndex = {" + nl + "\t");

		int mxi = 0;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			if (mxi++ > 0) {
				sb.append(", ");
			}

			sb.append("{ ");

			int ri = 0;
			for (DFAMxRow row : mx.getRows()) {
				DFAMxEpsCell cell = (DFAMxEpsCell) row.getDFATran(Alphabet.EpsCharEdge);
				if (cell != null) {
					if (ri++ > 0) {
						sb.append(", ");
					}
					sb.append(row.rowID);
				}
			}

			sb.append(" }");
		}

		sb.append(nl + "};");
		return sb.toString();
	}

	public byte[][] getEpsTrueTable() {
		byte[][] epsRowIndex2True = new byte[dfaEdgeMxCollBldr.getDFAMxColl().size()][];

		int mxi = -1;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			++mxi;

			TrueTableBldr truTabBldr = new TrueTableBldr(mx.getRowCount(), 1);

			for (DFAMxRow row : mx.getRows()) {
				if (row.getDFATran(Alphabet.EpsCharEdge) != null) {
					truTabBldr.setTrueBoolValue(row.rowID, 0);
				}
			}

			epsRowIndex2True[mxi] = new byte[truTabBldr.size()];

			for (int i = 0; i < truTabBldr.size(); ++i) {
				epsRowIndex2True[mxi][i] = truTabBldr.getBitSet(i);
			}
		}
		return epsRowIndex2True;
	}

	public String getMxDfaSize(){
		
		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder("private final short mxDfaSize[] = {");
		
		int mxi = 0;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			if (mxi++ > 0) {
				sb.append(", ");
			}
		
			sb.append(mx.getRowCount());
		}
		sb.append("};" + nl);

		return sb.toString();
	}
	
	public String getMxHuggerAsString(){
		
		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder("private final short huggerMxID[] = {");
		
		int mxi = 0;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			if (mxi++ > 0) {
				sb.append(", ");
			}
			
			if(mx.mxID == DFABaseMatrix.BASE_MXID){
				sb.append(-1);
				continue;
			}
			DFARngMatrix rngMx = (DFARngMatrix)mx;
			if(rngMx.getHuggerRngID() == DFABaseMatrix.BASE_MXID){
				sb.append(-1);
				continue;
			}
			sb.append(rngMx.getHuggerRngID());
		}
		
		sb.append("};" + nl);

		return sb.toString();
	}
	
	public String getEpsRowIndex2TrueAsString() {

		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder("private final byte[][] epsRowIndex2True = {");

		int mxi = 0;
		for (DFABaseMatrix mx : dfaEdgeMxCollBldr.getDFAMxColl()) {

			if (mxi++ > 0) {
				sb.append(",");
			}

			TrueTableBldr truTabBldr = new TrueTableBldr(mx.getRowCount(), 1);

			for (DFAMxRow row : mx.getRows()) {
				if (row.getDFATran(Alphabet.EpsCharEdge) != null) {
					truTabBldr.setTrueBoolValue(row.rowID, 0);
				}
			}

			sb.append(nl + "\t{ ");
			for (int i = 0; i < truTabBldr.size(); ++i) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(truTabBldr.getBitSet(i));
			}
			sb.append(" }");
		}

		sb.append(nl + "};");

		return sb.toString();
	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	public static class MxCellIndex {
		public final int row;
		public final int col;

		public MxCellIndex(int row, int col) {
			this.row = row;
			this.col = col;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + col;
			result = prime * result + row;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MxCellIndex other = (MxCellIndex) obj;
			if (col != other.col)
				return false;
			if (row != other.row)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[row=" + row + ", col=" + col + "]";
		}

	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	class DFACharIDMxCell {

		private final TreeSet<Integer> dfaIDs = new TreeSet<>();

		public boolean add(int dfaID) {
			return dfaIDs.add(dfaID);
		}

		public int size() {
			return dfaIDs.size();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			int i = 0;
			for (Integer dfaID : dfaIDs) {
				if (i++ > 0) {
					sb.append(", ");
				}
				if (dfaID < 0) {
					sb.append("AccRule=" + dfaID);
				} else {
					sb.append(dfaID);
				}
			}
			sb.append("]");
			return sb.toString();
		}

		public TreeSet<Integer> getDfaIDs() {
			return dfaIDs;
		}

	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	class DFACharIDMxRow {

		public final int rowID;
		private final TreeMap<Integer, DFACharIDMxCell> chID_MxCell = new TreeMap<>();

		public DFACharIDMxRow(int rowID) {
			this.rowID = rowID;
		}

		public boolean setDFATran(int onChID, int dfaID) {
			DFACharIDMxCell cell = chID_MxCell.get(onChID);
			if (cell == null) {
				cell = new DFACharIDMxCell();
				chID_MxCell.put(onChID, cell);
			}

			return cell.add(dfaID);
		}

		public DFACharIDMxCell getDFATran(int onChID) {
			return chID_MxCell.get(onChID);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[rowID=" + rowID + "\n");
			int i = 0;
			for (Integer onChID : chID_MxCell.keySet()) {
				if (i++ > 0) {
					sb.append("\n");
				}
				sb.append("\tOnChID=" + onChID + " " + chID_MxCell.get(onChID));

			}

			return sb.toString();
		}

		public Set<Integer> chIDs() {
			return chID_MxCell.keySet();
		}

	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	class DFACharIDMatrix {

		public final int mxID;
		private final TreeMap<Integer, DFACharIDMxRow> dfaID_MxRow = new TreeMap<>();
		private int cellSize;

		public DFACharIDMatrix(int mxID) {
			this.mxID = mxID;
		}

		public void setDFATran(int rowID, int onChID, int dfaID) {
			DFACharIDMxRow row = dfaID_MxRow.get(rowID);
			if (row == null) {
				row = new DFACharIDMxRow(rowID);
				dfaID_MxRow.put(rowID, row);
			}

			if (row.setDFATran(onChID, dfaID)) {
				++cellSize;
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (DFACharIDMxRow row : dfaID_MxRow.values()) {
				if (i++ > 0) {
					sb.append("\n");
				}
				sb.append(row);
			}
			return sb.toString();
		}

		public DFACharIDMxRow getRow(int rowID) {
			return dfaID_MxRow.get(rowID);
		}

		public int getCellSize() {
			return cellSize;
		}

		public Collection<DFACharIDMxRow> rows() {
			return dfaID_MxRow.values();
		}
	}
}
