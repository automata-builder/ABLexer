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


public class TrueTableBldr {

	private static final int BYTE_SIZE = 8;
	private final int rowSize, colSize;
	private final byte[] trueTab;
	
	public TrueTableBldr(int rowSize, int colSize) {
		this.rowSize = rowSize;
		this.colSize = colSize;
		this.trueTab = initTrueTable(rowSize, colSize);
	}

	public void setTrueBoolValue(int rowIndex, int colIndex){
		setTrueBoolValue(trueTab, rowIndex, colIndex, colSize);
	}
	
	public boolean getBoolValue(int rowIndex, int colIndex){
		return getBoolValue(trueTab, rowIndex, colIndex, colSize);
	}
	
	public int size(){
		return trueTab.length;
	}
	
	public byte getBitSet(int index){
		return trueTab[index];
	}
	
	public static void setTrueBoolValue(byte[] _trueTab, int _rowIndex, int _colIndex, int _totalColumns){

		ByteArrHash h = new ByteArrHash(_rowIndex, _colIndex, _totalColumns);
		_trueTab[h.arrIndex] |= h.bitMask;
	}
	
	public static boolean getBoolValue(byte[] _trueTab, int _rowIndex, int _colIndex, int _totalColumns){
		ByteArrHash h = new ByteArrHash(_rowIndex, _colIndex, _totalColumns);
		if((_trueTab[h.arrIndex] & h.bitMask) != 0){
			return true;
		}
		return false;
	}
	
	public static byte[] initTrueTable(int _rowSize, int _colSize){
		
		double cellSize = _rowSize * _colSize;
		double trueTabSize_D = cellSize / BYTE_SIZE;
		int trueTabSize_I = (int)trueTabSize_D;
		if(trueTabSize_D > trueTabSize_I){
			++trueTabSize_I;
		}
		return new byte[trueTabSize_I];
	}

	public byte[] getTrueTab() {
		return trueTab;
	}

}
/**
 * 
 * @author mike
 *
 */
class ByteArrHash {
	
	public static final int BYTE_SIZE = 8;
	private final int bitIndex;
	public final int arrIndex, bitMask;

	public ByteArrHash(int rowIndex, int colIndex, int totalColumns) {
		int oneDimIndex = rowIndex * totalColumns + colIndex;
		this.arrIndex = oneDimIndex / BYTE_SIZE;
		this.bitIndex = oneDimIndex % BYTE_SIZE;
		this.bitMask = 1 << bitIndex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ByteArrHash [bitIndex=");
		builder.append(bitIndex);
		builder.append(", arrIndex=");
		builder.append(arrIndex);
		builder.append(", bitMask=");
		builder.append(bitMask);
		builder.append("]");
		return builder.toString();
	}
}
