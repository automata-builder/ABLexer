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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import com.abuilder.auto.dfa.nfa.NFATrnEdge.CharClassEdge;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.EdgeType;
import com.abuilder.auto.dfa.nfa.NFATrnEdge.SimpleEdge;
import com.abuilder.exception.MyLogicException;
import com.abuilder.util.ABUtil;

public class Alphabet {

	public interface CHR {
		int EPS_CHR = -2;
		int INVALID_CHR = -1;
	}
	
	public interface CHID {
		int EOF_CHID = -2;
		int INVALID_CHID = -1;
		int EPS_CHID = 0;
	}
	
	public static final int EPS_EDGE_ID = 0;
	public static final SimpleEdge EpsCharEdge = new SimpleEdge(EPS_EDGE_ID, CHR.EPS_CHR);
	
	private final TreeMap<Integer, SimpleEdge> ch_SimpleEdge = new TreeMap<>();
	private final HashMap<BitSet, CharClassEdge> bitSet_CclEdge = new HashMap<>();
	private final ArrayList<NFATrnEdge> edgeList = new ArrayList<>();
	private final TreeMap<Integer, ArrayList<NFATrnEdge>> ch_EdgeList = new TreeMap<>();
	private final TreeMap<Integer, Integer> ch_ChID = new TreeMap<>();
	private final ArrayList<IAlpha> chID_Chs = new ArrayList<>();
	
	public Alphabet(){
		ch_SimpleEdge.put(CHR.EPS_CHR, EpsCharEdge);
		addEdge(EpsCharEdge);
		setChID2Char(CHR.EPS_CHR);
	}
	
	public SimpleEdge addSimpleEdge(int ch){
				
		SimpleEdge simple = ch_SimpleEdge.get(ch);
		if(simple == null){
			simple = new SimpleEdge(edgeSize(), ch);
			ch_SimpleEdge.put(ch, simple);
			addEdge(simple);
		}
		return simple;
	}
	
	public CharClassEdge addCharClassEdge(BitSet bitSet, boolean isNegate){
		
		CharClassEdge temp = new CharClassEdge(edgeSize(), bitSet, isNegate);		
		CharClassEdge cclEdge = bitSet_CclEdge.get(temp.getUsedBitSet());
		
		if(cclEdge == null){
			
			cclEdge = temp;
			bitSet_CclEdge.put(cclEdge.getUsedBitSet(), cclEdge);
			addEdge(cclEdge);
		}
	
		return cclEdge;
	}
	
	public int charSize() {
		return ch_EdgeList.size();
	}
	
	public int charIDSize(){
		return chID_Chs.size();//aka DFA CharID Column size
	}
	
	public Set<Integer> charSet() {
		return ch_EdgeList.keySet();
	}

	public int edgeSize(){
		return edgeList.size();
	}
		
	
	private void assure(int n, int expected){
		if(n != expected){
			throw new MyLogicException();
		}
	}
	
	private void addEdge(NFATrnEdge edge){
		assure(edge.edgeID, edgeList.size());
		edgeList.add(edge);
		
		switch(edge.getType()){
		case SIMPLE:
		
			int ch = ((SimpleEdge)edge).getChar();
			connect(ch, edge);
			break;
		
		case CHAR_CLASS:
			
			BitSet ccl = ((CharClassEdge)edge).getUsedBitSet();
			for(ch = ccl.nextSetBit(0); ch >= 0; ch = ccl.nextSetBit(ch+1)){
				connect(ch, edge);
			}
			break;
		
		default:
			throw new MyLogicException();
		}
	}
	
	private void connect(int ch, NFATrnEdge edge){
		ArrayList<NFATrnEdge> edgeColl = ch_EdgeList.get(ch);
		if(edgeColl == null){
			edgeColl = new ArrayList<>();
			ch_EdgeList.put(ch, edgeColl);
		}
		edgeColl.add(edge);
	}
	
	public int getCharWithIndex(int charIndex){
		return ch_EdgeList.keySet().toArray(new Integer[ch_EdgeList.size()])[charIndex];
	}
	
	public ArrayList<NFATrnEdge> getEdgeListWithChar(int ch){
		return ch_EdgeList.get(ch);
	}
	
	public NFATrnEdge getEdgeWithEdgeID(int edgeID){
		return edgeList.get(edgeID);
	}

	public int getCharIDWithChar(int ch){
		Integer chID = ch_ChID.get(ch);
		if(chID == null){
			return CHID.INVALID_CHID;
		}
		return chID;
	}
	
	public IAlpha getAlphaWithCharID(int chID){
		return chID_Chs.get(chID);
	}
	
	private void setChID2Char(int ch){
		if(ch == CHR.EPS_CHR && chID_Chs.size() != CHID.EPS_CHID){
			throw new RuntimeException();
		}
		ch_ChID.put(ch, chID_Chs.size());
		chID_Chs.add(new Alpha(ch));
	}
	
	public void setCharIDs(){
		
		for(Integer ch: ch_EdgeList.keySet()){
			
			if(ch == CHR.EPS_CHR){
				continue;
			}
			
			ArrayList<NFATrnEdge> edgeList = ch_EdgeList.get(ch);
			
			if(edgeList.size() > 1){
				setChID2Char(ch);
				continue;
			}
			
			//edgeList.size() = 1
			
			NFATrnEdge edge = edgeList.get(0);
			if(edge.getType() == EdgeType.SIMPLE){
				setChID2Char(ch);
				continue;
			}
			
			//edge = char class
			
			CharClassEdge chrCls = (CharClassEdge)edge;
			if(chrCls.getChID() == CharClassEdge.INVALID_CHR_ID){
				chrCls.setChID(chID_Chs.size());
				chID_Chs.add(new AlphaSet());
			}
			ch_ChID.put(ch, chrCls.getChID());
			((AlphaSet)chID_Chs.get(chrCls.getChID())).add(ch);
		}
	}

	public String getAlphabetFinals(){
		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder();
		sb.append("final short chIDSize = " + charIDSize() + ";" + nl);
//		sb.append("final static short EPS_CHID = " + CHID.EPS_CHID + ";" + nl);
//		sb.append("final static short EOF_CHID = " + CHID.EOF_CHID + ";" + nl);
//		sb.append("final static short INVALID_CHID = " + CHID.INVALID_CHID + ";");
		return sb.toString();
	}
	
	public String getAlpha2ChIDLookupComment(){
		StringBuilder sb = new StringBuilder();
		String nl = ABUtil.getSysLineSep();
		sb.append("/**" + nl);
		sb.append("\tAlphabet Size = " + ch_ChID.size() + nl + "\t");
		sb.append("['ch' = chID]: ");
		//sb.append("-----------"+nl+"\t");
		int ci = 0;
		for(Integer ch : ch_ChID.keySet()){
			if(ci++ > 0)
				sb.append(", ");
			sb.append(Ascii.toString(ch) + "=" + ch_ChID.get(ch));
		}
		sb.append(nl + "**/" + nl);
		return sb.toString();
	}
	
	public short[] getAscii2ChID(){
		short[] ch2ChID = new short[128];
		for (int i = 0; i < ch2ChID.length; i++) {
			ch2ChID[i] = (short) getCharIDWithChar(i);
		}
		return ch2ChID;
	}
	
	public String getAscii2ChIDAsString(){
		
		String nl = ABUtil.getSysLineSep();
		StringBuilder sb = new StringBuilder();
		sb.append("private final short[] ch2ChID = {" + nl);
		
		final int ROWS = 4, COLUMNS = 32;
		
		for(int r = 0; r < ROWS; ++r){
			if(r > 0){
				sb.append("," + nl);
			}
			sb.append("\t");
			for(int c = 0; c < COLUMNS; ++c){
				if(c > 0){
					sb.append(", ");
				}
				int chID = getCharIDWithChar(r * COLUMNS + c);
				sb.append(String.format("%2d", chID));
			}
		}
		sb.append(nl + "};");
		
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @author mike
	 *
	 */
	interface IAlpha {
		enum AlphaType {
			SINGLE, SET;
		}
		AlphaType getType();
	}
	
	/**
	 * 
	 * @author mike
	 *
	 */
	class Alpha implements IAlpha{

		private final int ch;
		
		public Alpha(int ch) {
			this.ch = ch;
		}


		@Override
		public AlphaType getType() {
			return AlphaType.SINGLE;
		}


		public int getCh() {
			return ch;
		}


		@Override
		public String toString() {
			return "" + ch + " '" + Ascii.toString(ch) + "'";
		}
		
	}
	/**
	 * 
	 * @author mike
	 *
	 */
	class AlphaSet implements IAlpha {
		
		private final BitSet alphas = new BitSet();
		
		public int size() {
			return alphas.cardinality();
		}

		public void add(int ch) {
			alphas.set(ch);
		}

		public int next(int fromIndex) {
			return alphas.nextSetBit(fromIndex);
		}
		
		@Override
		public AlphaType getType() {
			return AlphaType.SINGLE;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[ ");
			int ai = 0;
			for (int i = alphas.nextSetBit(0); i >= 0; i = alphas.nextSetBit(i+1)) {
				if(ai++ > 0)
					sb.append("  ");
				sb.append(i + " '" + Ascii.toString(i) + "'"  );
			}
			sb.append("]");
			return sb.toString();
		}
		
	}
	/**
	 * 
	 * @author mike
	 *
	 */
	public static class Ascii {
		
		public static String toString(int c) {
			switch (c) {

			case CHR.EPS_CHR:
				return "'EPS_CHR'";

			case '\t':
				return "'\\t'";

			case '\b':
				return "'\\b'";

			case '\n':
				return "'\\n'";

			case '\r':
				return "'\\r'";

			case '\f':
				return "'\\f'";

			case '\\':
				return "'\\'";

			default:

				if (c < 0) {

					return "INVALID_CHR";

				} else {
					
					return "'" + (char) c + "'";
				}
			}
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Alphabet [Alpha Size="+charSize()+", chID Size=" + charIDSize());
		for(int i = 0; i < chID_Chs.size(); ++i){
			sb.append("\n\nchID=" + i);
			sb.append("\n\t" + chID_Chs.get(i));
		}
		sb.append("\n");
		
		return sb.toString();
	}
}
