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

package com.abuilder.lex.skeleton;


//#LEX_IMPORT_START#

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

//#LEX_IMPORT_END#

public class ABLexerSkeletonV1 {

	private boolean debugMode = true;
	
	// #LEX_FIELD_START#

	private final DfaAuto dfaAuto = new DfaAuto();
	private final BitSet accRIDs = new BitSet(dfaAuto.getRuleSize() + 1);
	private final BitSet prevAccRIDs = new BitSet(dfaAuto.getRuleSize() + 1);
	private final MxCellDeque epsTrnQue = new MxCellDeque();
	private final MxDfaSituationMngr dfaSituMngr = new MxDfaSituationMngr(dfaAuto);
	private final CharBufferAccessMatch cbufMatch = new CharBufferAccessMatch();
	private LexMatch lexMatch;
	private InputBuffer input;
	private boolean isFirstRun = true;
	private short onChID;

	// DEBUG
	private int matchPos, matchCount;

	/**
	 * 
	 * @author mike
	 * 
	 */
	public static class LexMatch {

		private final String match;
		private final int rID;
		private final int offsetLineNum, endLineNum;

		public LexMatch(CharBufferAccessMatch match) {
			this.match = match.getMatch();
			this.rID = match.getRuleID();
			this.offsetLineNum = match.getSourceOffsetLineNum();
			this.endLineNum = match.getSourceEndLineNum();
		}

		public int length() {
			return match.length();
		}

		public int getOffsetLineNum() {
			return offsetLineNum;
		}

		public int getEndLineNum() {
			return endLineNum;
		}

		public String getMatch() {
			return match;
		}

		public int getRuleID() {
			return rID;
		}
	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	public static class Rule {
		public final int rID;
		public final String name;

		private Rule(int rID, String name) {
			this.rID = rID;
			this.name = name;
		}
	}

	public void setActiveStates(short... sIDs) {
		dfaAuto.setActiveStates(sIDs);
	}

	// #LEX_FIELD_END#



	Integer lexIt() throws Exception {

		// #LEX_LOOP_METHOD_START#

		if (isEndOfFileReached()) {
			throw new IOException("End of input has been reached.");
		}

		BitSet matchRIDs = null;
		boolean isExitLoop = false;
		int accRID;

		if (isFirstRun) {
			onFirstRunInit();
		}

		onEveryRunInit();

		while (!isExitLoop) {

			top: while (true) {


				moveOnEps();

				passAccRIDs();

				input.advance();
				nextChID();

				switch (onChID) {

				case DfaAuto.EOF_CHID:
				case DfaAuto.INVALID_CHID:

					if (onChID == DfaAuto.EOF_CHID && !hasMoreAfterEndOfFileReached()) {
						isExitLoop = true;
					}

					if (prevAccRIDs.cardinality() > 0) {
						input.pushBack(input.getNextCBix() - (input.getLxmCBufEnd() + 1));
						matchRIDs = prevAccRIDs;
						break top;
					}

					resetPtrs();

					if (isExitLoop) {
						return null;
					}
					continue top;

				default:

					moveOnNonEps();
				}

				if (!dfaSituMngr.hasFrom()) {

					resetFromMxPtrs();
					input.pushBack(input.getNextCBix() - (input.getLxmCBufEnd() + 1));

					if (accRIDs.cardinality() > 0 || prevAccRIDs.cardinality() > 0) {

						matchRIDs = accRIDs.cardinality() > 0 ? accRIDs : prevAccRIDs;
						break top;
					}

					resetPtrs();
				}
			}

			accRID = getAccRuleID(matchRIDs);

			if (dfaAuto.isBolAnchor(accRID)) {
				input.moveOffset();
			}
			if (dfaAuto.isEolAnchor(accRID)) {
				input.pushBack(1);
				if (input.getCurrChar() == '\r') {
					input.pushBack(1);
				}
			}

			setBufferWindowMatch(accRID);

			if (debugMode) {
				++matchCount;
				System.out.println("\n========= MATCH START # " + matchCount + " ===============\n");
				System.out.println("All matched rules: " + matchRIDs);
				System.out.println(cbufMatch.toString());
				System.out.println("\n========= MATCH END # " + matchCount + "===============\n");
			}

			resetAccRIDs();
			resetPtrs();

			// #LEX_LOOP_METHOD_END#

			switch (accRID) {
			case 0:

			default:
				if (accRID == 0)
					break;
				throw new Exception("Bad accept ruleID {" + accRID + "}");
			}
		}

		return null;
	}

	// #LEX_BODY_START#

	private int getAccRuleID(BitSet matchRIDs) throws Exception {
		return dfaAuto.getAccRuleID(matchRIDs);
	}

	public void setInputStream(InputStream in) {
		setReader(new InputStreamReader(in), InputBuffer.DEFAULT_ENSURE_SIZE);
	}

	public void setReader(InputStreamReader in) {
		setReader(in, InputBuffer.DEFAULT_ENSURE_SIZE);
	}

	public void setReader(InputStreamReader in, int ensureBufferSize) {
		if (input != null) {
			input.setReader(in, ensureBufferSize);
		} else {
			input = getInputBuffer(in, ensureBufferSize);
		}
	}

	public void setFilePath(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (input != null) {
			input.setReader(new FileReader(file), (int) file.length());
		} else {
			input = getInputBuffer(new FileReader(file), (int) file.length());
		}
	}

	private InputBuffer getInputBuffer(InputStreamReader in, int ensureBufSize) {
		return new InputBuffer(in, ensureBufSize);
	}

	public boolean hasMoreAfterEndOfFileReached() {
		return false;
	}

	private void onFirstRunInit() throws Exception {
		isFirstRun = false;
		resetFromMxPtrs();
		resetInput();
	}

	private void onEveryRunInit() {
		cbufMatch.clear();
		lexMatch = null;
	}

	private void resetPtrs() throws Exception {
		resetFromMxPtrs();
		resetInput();
	}

	private void resetInput() throws Exception {
		input.setLxmOffset();
		matchPos = 0;
	}

	private void resetFromMxPtrs() throws Exception {

		dfaSituMngr.clearFromDfa();
		dfaSituMngr.getSituation(DfaAuto.BASE_MXID).addFromDfa(DfaAuto.INIT_DFAID);
	}

	private void resetAccRIDs() {
		accRIDs.clear();
		prevAccRIDs.clear();
	}

	private void passAccRIDs() {
		if (accRIDs.cardinality() > 0) {
			for (int i = accRIDs.nextSetBit(0); i >= 0; i = accRIDs.nextSetBit(i + 1)) {
				prevAccRIDs.set(i);
			}
			accRIDs.clear();
		}
	}

	private void moveOnEps() throws Exception {

		Set<MxCell> doneSet = new HashSet<>();
		getEpsTrans(dfaSituMngr.fromIterators(), doneSet);

		final MxCell cell = new MxCell();

		while (epsTrnQue.size() > 0) {

			epsTrnQue.remove(cell);
			gotoNext(cell, DfaAuto.EPS_CHID);
			getEpsTrans(dfaSituMngr.gotoIterators(), doneSet);
			dfaSituMngr.moveGoto2From(true);
		}
	}

	private void moveOnNonEps() throws Exception {

		for (int mxID = 0; mxID < dfaAuto.getMxSize(); ++mxID) {

			Iterator<Integer> fromDfaIt = dfaSituMngr.fromIterator(mxID);

			while (fromDfaIt.hasNext()) {
				MxCell fromCell = new MxCell(mxID, fromDfaIt.next());
				gotoNext(fromCell, onChID);
			}

		}

		dfaSituMngr.clearFromDfa();
		dfaSituMngr.moveGoto2From(false);
	}

	private void addAccRID(int ruleID) {

		if (ruleID >= 0) {
			throw new RuntimeException();
		}

		if (input.isCurrLftNLAnch()) {
			return;
		}

		ruleID = (-ruleID) - 1;

		if (!dfaAuto.isRuleSsOn(ruleID)) {
			return;
		}

		if (prevAccRIDs.cardinality() > 0) {
			prevAccRIDs.clear();
		}

		accRIDs.set(ruleID);
		input.setLxmEnd();
	}

	private void gotoNext(MxCell fromCell, short onChID) throws Exception {

		if (onChID == DfaAuto.EPS_CHID) {
			gotoNextOnEps(fromCell);
		} else {
			gotoNextOnNonEps(fromCell, onChID);
		}

	}

	private void gotoNextOnEps(MxCell fromCell) throws Exception {

		if (fromCell.getMxID() == DfaAuto.BASE_MXID) {

			go2NextOnEpsFromBaseMx(fromCell);

		} else {

			go2NextOnEpsFromRangeMx(fromCell);

		}
	}

	private void gotoNextOnNonEps(MxCell fromCell, short onChID) throws Exception {
		if (fromCell.getMxID() == DfaAuto.BASE_MXID) {
			go2NextOnNonEpsFromBaseMx(fromCell, onChID);
		} else {
			go2NextOnNonEpsFromRangeMx(fromCell, onChID);
		}
	}

	private void go2NextOnEpsFromBaseMx(MxCell fromCell) throws Exception {

		MxCellList onEpsTrns = dfaAuto.getEpsTrans(fromCell);
		if (onEpsTrns.size() == 0) {
			return;
		}

		MxCell epsTrn = new MxCell();

		for (int i = 0; i < onEpsTrns.size(); ++i) {

			onEpsTrns.get(i, epsTrn);

			if (epsTrn.getDfaID() < 0) {// Accept
				addAccRID(epsTrn.getDfaID());
				continue;
			}

			if (!dfaAuto.isDfaStartStateTurnOn(epsTrn)) {
				continue;
			}

			if (epsTrn.getMxID() == DfaAuto.BASE_MXID) {

				BaseMxDfaSituation situ = dfaSituMngr.getSituation(epsTrn.getMxID());
				situ.addGotoDfa(epsTrn.getDfaID());

			} else {

				RngMxDfaSituation situ = (RngMxDfaSituation) dfaSituMngr.getSituation(epsTrn.getMxID());
				situ.addGotoStartLoop(epsTrn.getDfaID(), null);
			}

		}
	}

	private void go2NextOnNonEpsFromBaseMx(MxCell fromCell, short onChID) throws Exception {

		int[] toDfaIDs = dfaAuto.getNonEpsTran(DfaAuto.BASE_MXID, fromCell.getDfaID(), onChID);
		if (toDfaIDs == null) {
			return;
		}

		BaseMxDfaSituation baseSitu = dfaSituMngr.getSituation(DfaAuto.BASE_MXID);
		MxCell cell = new MxCell(DfaAuto.BASE_MXID, -1);

		for (int i = 0; i < toDfaIDs.length; ++i) {

			cell.setDfaID(toDfaIDs[i]);

			if (cell.getDfaID() < 0) {// ACCEPT

				addAccRID(cell.getDfaID());

			} else {

				if (!dfaAuto.isDfaStartStateTurnOn(cell)) {// AA_MxPtrPool.BASE_MXID,
																										// toDfaIDs[i])) {
					continue;
				}

				baseSitu.addGotoDfa(cell.getDfaID());

			}
		}

	}

	private void go2NextOnEpsFromRangeMx(MxCell fromCell) throws Exception {

		MxCellList epsTrns = dfaAuto.getEpsTrans(fromCell);// aa_mxPP.getMxID(fromPix),
																												// aa_mxPP.getDfaID(fromPix));

		if (epsTrns.size() == 0) {
			return;
		}

		boolean needClone;

		if (dfaAuto.hasNonEpsTranOnAnyChID(fromCell)) {
			needClone = true;
		} else {
			needClone = false;
		}

		MxCell epsTrn = new MxCell();

		for (int i = 0; i < epsTrns.size(); ++i) {

			epsTrns.get(i, epsTrn);

			goto2NextFromRangeMx(fromCell, epsTrn, needClone);
		}

	}

	private void go2NextOnNonEpsFromRangeMx(MxCell fromCell, short onChID) throws Exception {

		int[] toDfaIDs = dfaAuto.getNonEpsTran(fromCell, onChID);
		if (toDfaIDs == null) {
			return;
		}

		MxCell gotoCell = new MxCell();
		gotoCell.setMxID(fromCell.getMxID());

		for (int i = 0; i < toDfaIDs.length; ++i) {

			gotoCell.setDfaID(toDfaIDs[i]);

			goto2NextFromRangeMx(fromCell, gotoCell, false);
		}
	}

	private void goto2NextFromRangeMx(MxCell fromCell, MxCell gotoCell, final boolean needClone) throws Exception {

		RngMxDfaSituation situ = (RngMxDfaSituation) dfaSituMngr.getSituation(fromCell.getMxID());

		if (gotoCell.getDfaID() == DfaAuto.REDFAID) {

			short reDfaID = dfaAuto.getReDfaID(fromCell.getMxID());
			// reDfaIDs are always zeros
			if (situ.huggerMxID == -1) {

				boolean tailTrnToBase = situ.makeOutterGb2ReTran(fromCell.getDfaID(), reDfaID, needClone);
				if (tailTrnToBase) {
					MxCell tailTrn = dfaAuto.getTailTran(fromCell.getMxID());
					BaseMxDfaSituation base = dfaSituMngr.getSituation(0);
					base.addGotoDfa(tailTrn.getDfaID());
				}

			} else {

				DfaGearsZip dadGs = situ.makeInnerGb2ReTran(fromCell.getDfaID(), reDfaID, needClone);

				if (dadGs != null) {// bunch of lcs >= minBnd
					MxCell tailTrn = dfaAuto.getTailTran(fromCell.getMxID());
					RngMxDfaSituation dadSitu = (RngMxDfaSituation) dfaSituMngr.getSituation(tailTrn.getMxID());
					if (needClone) {
						dadGs = dadGs.clone();
					}
					dadSitu.addGotoDfa(tailTrn.getDfaID(), dadGs);

				}
			}

		} else {

			if (fromCell.getMxID() == gotoCell.getMxID()) {

				situ.makeTran(fromCell.getDfaID(), gotoCell.getDfaID());

			} else {

				DfaGearsZip gs = situ.getFromQue(fromCell.getDfaID());

				if (needClone) {
					gs = gs.clone();
				}

				// Pass to child
				RngMxDfaSituation childSitu = (RngMxDfaSituation) dfaSituMngr.getSituation(gotoCell.getMxID());
				childSitu.addGotoStartLoop(gotoCell.getDfaID(), gs);
			}
		}
	}

	private void nextChID() {
		short ch = input.getCurrChar();
		this.onChID = dfaAuto.nextChID(ch);

		if (debugMode) {
			++matchPos;
			System.out.print("\tmatchPos=" + matchPos + " ascii=" + ch + " " + aa_charToString(ch));
			System.out.println(" chID=" + onChID + " sourcePos=" + (input.getLxmSourceOffset() + matchPos - 1));
		}
	}

	private String aa_charToString(short ch) {

		if (ch >= 33 && ch <= 126) {
			return "'" + (char) ch + "'";
		}

		switch (ch) {
		case '\n':
			return "'\\n'";

		case '\r':
			return "'\\r'";

		case '\t':
			return "'\\t'";

		case '\f':
			return "'\\f'";

		case '\b':
			return "'\\b'";

		default:
			return "";
		}
	}

	private void getEpsTrans(Iterator<Integer>[] iters, Set<MxCell> doneSet) throws Exception {

		for (int mxID = 0; mxID < iters.length; mxID++) {

			while (iters[mxID].hasNext()) {

				MxCell cell = new MxCell(mxID, iters[mxID].next());
				if (!doneSet.contains(cell) && dfaAuto.hasEpsTran(cell)) {
					doneSet.add(cell);
					epsTrnQue.add(cell);
				}
			}
		}
	}

	private void setBufferWindowMatch(int rID) {
		cbufMatch.setMatch(input.getCharBuffer(), input.getLxmSourceOffset(), input.getLxmCBufOffset(), input.getLxmLength(), input.getLxmSourceOffsetLineNum(), input.getLxmSourceEndLineNum(), rID);
	}

	private CharBufferAccessMatch getCharBufferAccessMatch() {
		return cbufMatch;
	}

	public LexMatch getMatch() {

		if (!gotMatch()) {
			return null;
		}
		if (lexMatch == null) {
			lexMatch = new LexMatch(cbufMatch);
		}
		return lexMatch;
	}

	public boolean gotMatch() {
		if (cbufMatch.getLexemeCharBufferOffset() != -1) {
			return true;
		}
		return false;
	}

	public boolean isEndOfFileReached() throws IOException {
		if (input == null) {
			throw new IOException("Input source has not been set.");
		}
		return input.isEndOfFileReached();
	}

	/**
	 * 
	 * @author mike
	 * 
	 */
	class InputBuffer {

		private static final int DEFAULT_LOOK_RADIUS = 16;
		private static final int DEFAULT_BLOCK_SIZE = 512;
		private static final int DEFAULT_BLOCK_SIZE_FACTOR = 1;// 2;
		private static final int DEFAULT_ENSURE_SIZE = -1;
		private static final int RESIZE_FACTOR = 2;
		static final int EOI_CHAR = -2; // END OF INPUT
		static final int INVALID_CHAR = -1;

		private final int blockSize; // read block size
		private final int lookRadius;

		private char[] cbuf;
		private int lxmOffset;// start of current lexeme
		private int lxmEnd;// end of current lexeme
		private int nxtCBix; // next buffer index
		private int lnNum, offsetLnNum, endLnNum;// line number at bix
		private int readEnd; // end of logical buffer
		private int slideTotal, nlLftAnchPos;

		private boolean isEof;
		private BufferedReader br;

		private InputBuffer(InputStreamReader in) {
			this(in, DEFAULT_ENSURE_SIZE, DEFAULT_BLOCK_SIZE, DEFAULT_LOOK_RADIUS);
		}

		private InputBuffer(InputStreamReader in, int ensureBufSize) {
			this(in, ensureBufSize, DEFAULT_BLOCK_SIZE, DEFAULT_LOOK_RADIUS);
		}

		private InputBuffer(InputStreamReader in, int ensureBufSize, int blockSize, int lookRadius) {

			this.blockSize = blockSize;
			this.lookRadius = lookRadius;

			setReader(in, ensureBufSize);
		}

		private void setReader(InputStreamReader in, int ensureBufSize) {
			closeInputStream();
			resetFields(in, ensureBufSize);
		}

		private void resetFields(InputStreamReader in, int ensureBufSize) {
			br = new BufferedReader(in);
			isEof = false;

			nxtCBix = lxmOffset = lxmEnd = readEnd = lnNum = offsetLnNum = endLnNum = slideTotal = 0;

			initBuffer(ensureBufSize);
			cbuf[readEnd++] = '\n'; // Left NL Anchor ^abc
		}

		private void initBuffer(int ensureBufSize) {

			if (cbuf != null && cbuf.length >= ensureBufSize) {
				return;
			}

			cbuf = new char[(blockSize * getBlockSizeFactor(ensureBufSize)) + (2 * lookRadius)];
			return;
		}

		private void slideLeft() {

			int leftEdge = Math.min(lxmOffset, nxtCBix - lookRadius);
			int keepSize = nxtCBix + lookRadius - leftEdge;

			int totalBlocks = (cbuf.length - keepSize) / blockSize;

			if (totalBlocks > 0) {

				System.arraycopy(cbuf, leftEdge, cbuf, 0, keepSize);

			} else {// resize buffer

				char[] temp = new char[cbuf.length * RESIZE_FACTOR];
				System.arraycopy(cbuf, leftEdge, temp, 0, keepSize);
				cbuf = temp;
			}

			lxmOffset -= leftEdge;
			lxmEnd -= leftEdge;
			nxtCBix -= leftEdge;
			readEnd -= leftEdge;

			slideTotal += leftEdge;
			nlLftAnchPos -= leftEdge;
		}

		private int getBlockSizeFactor(int ensureBufferSize) {
			if (ensureBufferSize == DEFAULT_ENSURE_SIZE) {
				return DEFAULT_BLOCK_SIZE_FACTOR;
			}
			return Math.max(DEFAULT_BLOCK_SIZE_FACTOR, getBlockSizeFactor(ensureBufferSize, blockSize));
		}

		private int getBlockSizeFactor(int ensureSize, int blockSize) {

			double q = (double) ensureSize / blockSize;
			return (int) Math.ceil(q);
		}

		private boolean isEndOfFileReached() {
			return isEof;
		}

		private boolean isDone() {
			return isEof && nxtCBix >= readEnd;
		}

		private boolean hasMore() {
			return nxtCBix < readEnd;
		}

		private boolean inHeadLookRadius() {
			return nxtCBix >= readEnd - lookRadius;
		}

		private void advance() throws Exception {

			if (isDone()) {
				return;
			}

			if (inHeadLookRadius() && !isEof) {// Do refill

				int rhSideBlocks = (cbuf.length - readEnd) / blockSize;

				if (rhSideBlocks == 0) {
					slideLeft();
				}

				refill(readEnd);
			}

			if (nxtCBix < readEnd && cbuf[nxtCBix] == '\n') {
				++lnNum;
			}
			++nxtCBix;// even if eof is true && nxtBix >= readEnd
		}

		private void refill(int from) throws Exception {

			if (!br.ready()) {
				if (hasMore()) {
					return;
				}
			}

			int length = ((cbuf.length - from) / blockSize) * blockSize;

			if (length == 0) {
				throw new Exception();
			}

			int got = br.read(cbuf, from, length);

			if (got == -1) {
				closeInputStream();
				return;
			}

			readEnd = from + got;
		}

		private void closeInputStream() {
			isEof = true;
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
				br = null;
			}
		}

		private int getLxmLength() {
			return lxmEnd - lxmOffset + 1;
		}

		private char[] getCharBuffer() {
			return cbuf;
		}

		private char charAt(int index) {
			return cbuf[index];
		}

		private short getCurrChar() {
			return getLaChar(0);
		}

		private boolean isCurrLftNLAnch() {
			return lxmOffset == nlLftAnchPos;
		}

		private short getLaChar(int n) {

			int la = nxtCBix + (n - 1);

			if (isEof && la >= readEnd) {
				return EOI_CHAR;
			}

			if (la < 0 || la >= readEnd) {
				return INVALID_CHAR;
			}

			return (short) cbuf[la];
		}

		private void moveOffset() {
			if (lxmOffset >= lxmEnd) {
				return;
			}
			if (cbuf[lxmOffset++] == '\n') {
				++offsetLnNum;
			}
		}

		private void pushBack(int n) {

			if (n == 0) {
				return;
			}

			while (n-- > 0 && nxtCBix > lxmOffset + 1) {
				if (cbuf[--nxtCBix] == '\n') {
					--lnNum;
				}
			}

			if (nxtCBix - 1 < lxmEnd) {
				lxmEnd = nxtCBix - 1;
			}
		}

		private void setLxmOffset() {
			lxmOffset = lxmEnd = nxtCBix;
			offsetLnNum = lnNum;
		}

		private void setLxmEnd() {
			lxmEnd = nxtCBix - 1;
			endLnNum = lnNum;
			if (cbuf[lxmEnd] == '\n') {
				--endLnNum;
			}
		}

		private int getLxmCBufOffset() {
			return lxmOffset;
		}

		private int getLxmCBufEnd() {
			return lxmEnd;
		}

		private int getLxmSourceOffset() {
			return slideTotal + lxmOffset - 1; // -1; setLeftNLAnchor; extra '\n'
		}

		private int getNextCBix() {
			return nxtCBix;
		}

		private int getLxmSourceOffsetLineNum() {
			return offsetLnNum;
		}

		private int getLxmSourceEndLineNum() {
			return endLnNum;
		}
	}

	/**
	 * 
	 * @author mike
	 * 
	 */

	private class CharBufferAccessMatch {

		private char[] cbuf;
		private String match;
		private int lxmSourceOffset, lxmCBufOffset;
		private int lexemeLength;
		private int lxmSourceOffsetLineNum, lxmSourceEndLineNum;
		private int ruleID;

		CharBufferAccessMatch() {
			clear();
		}

		private void clear() {
			lxmCBufOffset = lexemeLength = lxmSourceEndLineNum = ruleID = -1;
			cbuf = null;
			match = null;
		}

		public String getMatch() {
			if (match == null) {
				match = new String(cbuf, lxmCBufOffset, lexemeLength);
			}
			return match;
		}

		private void setMatch(char[] cbuf, int sourceOffset, int cBufoffset, int length, int offsetLnNum, int endLnNum, int ruleID) {
			this.cbuf = cbuf;
			this.lxmSourceOffset = sourceOffset;
			this.lxmCBufOffset = cBufoffset;
			this.lexemeLength = length;
			this.lxmSourceOffsetLineNum = offsetLnNum;
			this.lxmSourceEndLineNum = endLnNum;
			this.ruleID = ruleID;
			this.match = null;
		}

		private char[] getCharBuffer() {
			return cbuf;
		}

		private char charAt(int index) {
			return cbuf[lxmCBufOffset + index];
		}

		private int getLexemeCharBufferOffset() {
			return lxmCBufOffset;
		}

		private int lexemeLength() {
			return lexemeLength;
		}

		private int getSourceOffsetLineNum() {
			return lxmSourceOffsetLineNum;
		}

		private int getSourceEndLineNum() {
			return lxmSourceEndLineNum;
		}

		private int getRuleID() {
			return ruleID;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CharBufferAccessMatch {");
			builder.append("\n\tLexeme Source Offset: ");
			builder.append(lxmSourceOffset);
			builder.append("\n\tLexeme CharBuffer Offset: ");
			builder.append(lxmCBufOffset);
			builder.append("\n\tLexeme Length: ");
			builder.append(lexemeLength);
			builder.append("\n\tLexeme Source Offset Line Number: ");
			builder.append(lxmSourceOffsetLineNum);
			builder.append("\n\tLexeme Source End Line Number: ");
			builder.append(lxmSourceEndLineNum);
			builder.append("\n\tRule: ");
			builder.append(dfaAuto.getRuleName((short) ruleID));
			builder.append("\n\tmatch: ");
			builder.append(getMatch());
			builder.append("\n}");
			return builder.toString();
		}
	}

	/**
	 * 
	 * 
	 * @author Mike-Red
	 * 
	 */

	class BaseMxDfaSituation {

		protected final int mxID;
		private BitSet fromDfas, gotoDfas;
		protected final DfaAuto auto;

		BaseMxDfaSituation(int mxID, DfaAuto auto) {
			this.mxID = mxID;
			this.fromDfas = new BitSet(auto.getDfaSize(mxID));
			this.gotoDfas = new BitSet(auto.getDfaSize(mxID));
			this.auto = auto;
		}

		int fromSize() {
			return fromDfas.cardinality();
		}

		Iterator<Integer> fromIterator() {
			return getIterator(fromDfas);
		}

		Iterator<Integer> gotoIterator() {
			return getIterator(gotoDfas);
		}

		Iterator<Integer> getIterator(final BitSet bitSet) {

			Iterator<Integer> it = new Iterator<Integer>() {

				int nextDfa = bitSet.nextSetBit(0);

				@Override
				public boolean hasNext() {
					return nextDfa >= 0;
				}

				@Override
				public Integer next() {
					int retVal = nextDfa;
					nextDfa = bitSet.nextSetBit(nextDfa + 1);
					return retVal;
				}

				@Override
				public void remove() {
				}

			};
			return it;
		}

		BitSet getGotoDfas() {
			return gotoDfas;
		}

		BitSet getFromDfas() {
			return fromDfas;
		}

		void addGotoDfa(int gotoDfa) {
			gotoDfas.set(gotoDfa);
		}

		void addFromDfa(int dfaID) {
			fromDfas.set(dfaID);
		}

		public void makeTran(int fromDfa, int gotoDfa) {
			// fromDfas.set(fromDfa, false);
			// gotoDfas.set(gotoDfa, true);
			throw new RuntimeException("This is never called");
		}

		void moveGoto2From() {
			fromDfas.or(gotoDfas);
			gotoDfas.clear();
		}

		public int getDfaSize() {
			return fromDfas.cardinality();
		}

		public void clearFromDfa() {
			fromDfas.clear();
		}

		public void clearGoto() {
			gotoDfas.clear();
		}

	}

	/**
	 * 
	 * 
	 * 
	 * @author Mike-Red
	 * 
	 */

	class RngMxDfaSituation extends BaseMxDfaSituation {

		final int huggerMxID;
		final int reDfaID;
		final int huggerSize;

		final private Map<Integer, DfaGearsZip> fromMap = new HashMap<>();
		final private Map<Integer, DfaGearsZip> gotoMap = new HashMap<>();

		final int bounds[] = new int[2];

		RngMxDfaSituation(int mxID, DfaAuto auto) {
			super(mxID, auto);
			this.huggerMxID = auto.getHuggerMxID(mxID);
			this.huggerSize = auto.getHuggerSize(mxID);
			this.reDfaID = auto.getReDfaID(mxID);
			bounds[0] = auto.getMinBnd(mxID);
			bounds[1] = auto.getMaxBnd(mxID);
		}

		@Override
		void moveGoto2From() {

			if (gotoMap.size() > 0) {

				for (Entry<Integer, DfaGearsZip> entries : gotoMap.entrySet()) {

					int dfa = entries.getKey();

					DfaGearsZip gotoGs = entries.getValue();

					DfaGearsZip fromGs = fromMap.get(dfa);

					if (fromGs == null) {

						fromMap.put(dfa, gotoGs);

					} else {

						fromGs.merge(gotoGs);
					}
				}

				gotoMap.clear();
			}

		}

		Iterator<Integer> fromIterator() {
			return fromMap.keySet().iterator();
		}

		Iterator<Integer> gotoIterator() {
			return gotoMap.keySet().iterator();
		}

		int fromSize() {
			return fromMap.size();
		}

		public void clearFromDfa() {
			fromMap.clear();
		}

		void addGotoDfa(int gotoDfa, DfaGearsZip newGs) {

			DfaGearsZip old = gotoMap.get(gotoDfa);
			if (old == null) {
				gotoMap.put(gotoDfa, newGs);
			} else {
				old.merge(newGs);
			}

		}

		void addGotoStartLoop(int gotoDfa, DfaGearsZip huggers) {

			if (gotoDfa != this.reDfaID) {
				throw new RuntimeException();
			}

			DfaGearsZip gs = gotoMap.get(reDfaID);
			if (gs == null) {
				gs = new DfaGearsZip(mxID, auto);
				gotoMap.put(reDfaID, gs);
			}
			gs.addNewSpin(huggers);

		}

		public void addGoto(DfaGearsZip gs, int gotoDfa) {

			DfaGearsZip gotoGs = gotoMap.get(gotoDfa);

			if (gotoGs == null) {

				gotoMap.put(gotoDfa, gs);

			} else {

				gotoGs.merge(gs);
			}
		}

		@Override
		public void makeTran(int fromDfa, int gotoDfa) {

			DfaGearsZip fromGs = fromMap.get(fromDfa);
			DfaGearsZip gotoGs = gotoMap.get(gotoDfa);

			if (gotoGs == null) {

				gotoMap.put(gotoDfa, fromGs);

			} else {

				gotoGs.merge(fromGs);
				throw new RuntimeException();
			}

		}

		DfaGearsZip getFromQue(int fromDfa) {
			return fromMap.get(fromDfa);
		}

		boolean makeOutterGb2ReTran(int fromGbDfa, int gotoReDfa, boolean needClone) {
			if (gotoReDfa != this.reDfaID) {
				throw new RuntimeException();
			}

			DfaGearsZip gs = fromMap.get(fromGbDfa);

			if (needClone) {
				gs = gs.clone();
			}
			boolean tailTrnToBase = gs.makeOutterGb2ReTran();

			if (!gs.isExpired()) {

				addGoto(gs, gotoReDfa);

			}

			return tailTrnToBase;
		}

		DfaGearsZip makeInnerGb2ReTran(int fromGbDfa, int gotoReDfa, boolean needClone) {

			DfaGearsZip gs = fromMap.get(fromGbDfa);
			if (needClone) {
				gs = gs.clone();
			}
			DfaGearsZip dad = gs.makeInnerGb2ReTran();
			if (!gs.isExpired()) {
				addGoto(gs, gotoReDfa);
			}

			return dad;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RngMxDFASituation2 {\n\tmxID: ");
			builder.append(mxID);
			builder.append("\n\thuggerMxID: ");
			builder.append(huggerMxID);
			builder.append("\n\treDfaID: ");
			builder.append(reDfaID);
			builder.append("\n\tbounds: ");
			builder.append(Arrays.toString(bounds));
			builder.append("\n\tfromMap: ");
			builder.append(fromMap);
			builder.append("\n\tgotoMap: ");
			builder.append(gotoMap);
			builder.append("\n}");
			return builder.toString();
		}

		public Map<Integer, DfaGearsZip> getFromMap() {
			return fromMap;
		}

		public Map<Integer, DfaGearsZip> getGotoMap() {
			return gotoMap;
		}

	}

	/**
	 * 
	 * 
	 * @author Mike-Red
	 * 
	 */

	class MxDfaSituationMngr {

		private final BaseMxDfaSituation[] situations;
		private final Iterator<Integer>[] iters;

		public MxDfaSituationMngr(DfaAuto auto) {
			this.situations = new BaseMxDfaSituation[auto.getMxSize()];
			this.situations[DfaAuto.BASE_MXID] = new BaseMxDfaSituation(DfaAuto.BASE_MXID, auto);

			for (int mxID = DfaAuto.BASE_MXID + 1; mxID < auto.getMxSize(); mxID++) {
				situations[mxID] = new RngMxDfaSituation(mxID, auto);
			}

			this.iters = new Iterator[auto.getMxSize()];
		}

		boolean hasFrom() {
			for (int mxID = 0; mxID < situations.length; mxID++) {
				if (situations[mxID].fromSize() > 0) {
					return true;
				}
			}
			return false;
		}

		Iterator<Integer>[] fromIterators() {
			for (int mxID = 0; mxID < situations.length; mxID++) {
				iters[mxID] = situations[mxID].fromIterator();
			}
			return iters;
		}

		Iterator<Integer>[] gotoIterators() {
			for (int mxID = 0; mxID < situations.length; mxID++) {
				iters[mxID] = situations[mxID].gotoIterator();
			}
			return iters;
		}

		Iterator<Integer> fromIterator(int mxID) {
			return situations[mxID].fromIterator();
		}

		public MxCellList getFromAllCells() {
			MxCellList fromCells = new MxCellList();

			for (int i = 0; i < situations.length; i++) {
				Iterator<Integer> fromDfaIt = situations[i].fromIterator();

				while (fromDfaIt.hasNext()) {
					MxCell cell = new MxCell(i, fromDfaIt.next());
					fromCells.add(cell);
				}
			}
			return fromCells;
		}

		public BaseMxDfaSituation getSituation(int mxID) {
			return situations[mxID];
		}

		void clearFromDfa() {
			for (int i = 0; i < situations.length; i++) {
				situations[i].clearFromDfa();
			}
		}

		void clearGotoDfa() {
			for (int mxID = 0; mxID < situations.length; mxID++) {
				situations[mxID].clearGoto();
			}
		}

		void addGotoDfa(MxCell cell) {
			situations[cell.getMxID()].addGotoDfa(cell.getDfaID());
		}

		void moveGoto2From(boolean onEps) {
			for (int mxID = 0; mxID < situations.length; mxID++) {
				situations[mxID].moveGoto2From();
			}

		}
	}

	/**
	 * 
	 * @author Mike-Red
	 * 
	 */

	class DfaAuto {

		static final short INIT_DFAID = 0;
		private static final int BYTE_SIZE = 8;
		static final short EPS_CHID = 0;
		static final short EOF_CHID = -2;
		static final short INVALID_CHID = -1;
		static final short REDFAID = 32767;
		private static final short BADDFAID = -32768;
		private static final short INVALID_MIN_MATCH_RID = Short.MAX_VALUE;
		private static final int BOL_ANCHOR = 1;
		private static final int EOL_ANCHOR = 2;
		private static final int BINARY_SEARCH_NOT_FOUND = -1;
		public static final int BASE_MXID = 0;

		private final DfaAutoMx autoTables = new DfaAutoMx();
		private final BitSet ssOnSet;
		private final short[] ssMinMatchRID;
		private final ArrayList<Short> ssOnList;

		private int arrIndex, bitMask;
		private final MxCell tailTrn = new MxCell();

		DfaAuto() {
			this.ssOnSet = new BitSet(getSSSize());
			this.ssMinMatchRID = new short[getSSSize()];
			this.ssOnList = new ArrayList<Short>(getSSSize());
		}

		public short getDfaSize(int mxID) {
			return autoTables.mxDfaSize[mxID];
		}

		short[] getDfaSizes() {
			return autoTables.mxDfaSize;
		}

		public int getHuggerMxID(int mxID) {
			if (mxID > -1) {
				return autoTables.huggerMxID[mxID];
			}
			return -1;
		}

		public int getHuggerSize(int mxID) {
			int huggerMxID = getHuggerMxID(mxID);
			int size = 0;
			while (huggerMxID > -1) {
				huggerMxID = getHuggerMxID(huggerMxID);
				++size;
			}
			return size;
		}

		String getRuleName(short rID) {
			return autoTables.ruleNames[rID];
		}

		int getAccRuleID(BitSet mRIDs) throws Exception {

			if (ssOnList.size() == 0) {
				throw new Exception("'DfaAuto.ssOnList.size()' should be greater than zero.");
			}

			initSsMinMatchRID(mRIDs);

			short currSID = -1;
			short currRID = -1;

			for (int i = 0; i < ssOnList.size(); i++) {

				currSID = ssOnList.get(i);
				currRID = ssMinMatchRID[currSID];

				if (currRID != INVALID_MIN_MATCH_RID) {
					break;
				}
			}

			top: while (true) {

				for (int i = 0; i < autoTables.ssRuleOrder[currSID].length; i++) {

					if (autoTables.ssRuleOrder[currSID][i] < 0) {
						short insertSID = (short) -(autoTables.ssRuleOrder[currSID][i] + 1);
						if (ssMinMatchRID[insertSID] != INVALID_MIN_MATCH_RID) {
							currSID = insertSID;
							continue top;
						}

					} else if (currRID <= autoTables.ssRuleOrder[currSID][i]) {
						return currRID;
					}
				}

				return currRID;
			}
		}

		private void getHash(int rowIndex, int colIndex, int totalColumns) {
			int oneDimIndex = rowIndex * totalColumns + colIndex;
			arrIndex = oneDimIndex / BYTE_SIZE;
			int bitIndex = oneDimIndex % BYTE_SIZE;
			bitMask = 1 << bitIndex;
		}

		private void initSsMinMatchRID(BitSet mRIDs) {

			clearSsMinMatchRID();

			for (int mRID = mRIDs.nextSetBit(0); mRID >= 0; mRID = mRIDs.nextSetBit(mRID + 1)) {
				short sID = getRuleSsID(mRID);
				setSsMinMatchRID(sID, (short) mRID);
			}
		}

		private void clearSsMinMatchRID() {
			for (int i = 0; i < ssMinMatchRID.length; i++) {
				ssMinMatchRID[i] = INVALID_MIN_MATCH_RID;
			}
		}

		private void setSsMinMatchRID(short sID, short rID) {
			if (ssMinMatchRID[sID] > rID) {
				ssMinMatchRID[sID] = rID;
			}
		}

		short getSsMinMatchRID(short sID) {
			return ssMinMatchRID[sID];
		}

		boolean getBoolValue(byte[] _trueTab, int _rowIndex, int _colIndex, int _totalColumns) {
			getHash(_rowIndex, _colIndex, _totalColumns);
			if ((_trueTab[arrIndex] & bitMask) != 0) {
				return true;
			}
			return false;
		}

		boolean hasEpsTran(MxCell fromCell) {
			return getBoolValue(autoTables.epsRowIndex2True[fromCell.getMxID()], fromCell.getDfaID(), EPS_CHID, 1);
		}

		boolean hasEpsTran(int fromMxID, int fromDfaID) {
			return getBoolValue(autoTables.epsRowIndex2True[fromMxID], fromDfaID, EPS_CHID, 1);
		}

		private int binarySearch(int[] src, int key) {

			int low = 0;
			int high = src.length - 1;

			while (low <= high) {

				int mid = (low + high) >>> 1;

				if (src[mid] < key) {

					low = mid + 1;

				} else if (src[mid] > key) {

					high = mid - 1;

				} else {

					return mid; // key found
				}
			}

			return BINARY_SEARCH_NOT_FOUND; // key not found

		}

		MxCellList getEpsTrans(MxCell fromCell) {

			MxCellList epsTrnCells = new MxCellList();

			if (!hasEpsTran(fromCell)) {
				return null;
			}

			int r2v = binarySearch(autoTables.epsRowIndex2ValueIndex[fromCell.getMxID()], fromCell.getDfaID());

			int trnSize;

			if (r2v < autoTables.epsValueIndex[fromCell.getMxID()].length - 1) {
				trnSize = (autoTables.epsValueIndex[fromCell.getMxID()][r2v + 1] - autoTables.epsValueIndex[fromCell.getMxID()][r2v]) / 2;
			} else {
				trnSize = (autoTables.epsValue[fromCell.getMxID()].length - autoTables.epsValueIndex[fromCell.getMxID()][r2v]) / 2;
			}

			short vi = autoTables.epsValueIndex[fromCell.getMxID()][r2v];

			MxCell cell = new MxCell();
			for (int i = 0; i < trnSize; ++i) {
				cell.setMxID(autoTables.epsValue[fromCell.getMxID()][vi++]);
				cell.setDfaID(autoTables.epsValue[fromCell.getMxID()][vi++]);
				epsTrnCells.add(cell);
			}

			return epsTrnCells;
		}

		int[] getNonEpsTran(MxCell fromCell, short onChID) throws Exception {
			return getNonEpsTran(fromCell.getMxID(), fromCell.getDfaID(), onChID);
		}

		boolean hasNonEpsTranOnAnyChID(MxCell fromCell) {
			return hasNonEpsTranOnAnyChID(fromCell.getMxID(), fromCell.getDfaID());
		}

		boolean hasNonEpsTranOnAnyChID(int fromMxID, int fromDfaID) {
			for (int onChID = 0; onChID < autoTables.chIDSize; onChID++) {
				if (getBoolValue(autoTables.nonEpsMxIndex2True[fromMxID], fromDfaID, onChID, autoTables.chIDSize)) {
					return true;
				}
			}
			return false;
		}

		int[] getNonEpsTran(int fromMxID, int fromDfaID, short onChID) throws Exception {

			if (!getBoolValue(autoTables.nonEpsMxIndex2True[fromMxID], fromDfaID, onChID, autoTables.chIDSize)) {
				return null;
			}

			int oneDimMxIndex = fromDfaID * autoTables.chIDSize + onChID;
			int r2v = binarySearch(autoTables.nonEpsOneDimMxIndex2ValueIndex[fromMxID], oneDimMxIndex);

			if (r2v == BINARY_SEARCH_NOT_FOUND) {
				throw new Exception("Prog Error");
			}

			int trnSize;
			if (r2v < autoTables.nonEpsValueIndex[fromMxID].length - 1) {
				trnSize = autoTables.nonEpsValueIndex[fromMxID][r2v + 1] - autoTables.nonEpsValueIndex[fromMxID][r2v];

			} else {
				trnSize = autoTables.nonEpsValue[fromMxID].length - autoTables.nonEpsValueIndex[fromMxID][r2v];
			}

			short vi = autoTables.nonEpsValueIndex[fromMxID][r2v];

			int[] dfaIDs = new int[trnSize];
			for (int i = 0; i < dfaIDs.length; ++i) {
				dfaIDs[i] = autoTables.nonEpsValue[fromMxID][vi++];
			}

			return dfaIDs;
		}

		short nextChID(short ch) {
			if (ch == InputBuffer.EOI_CHAR) {
				return EOF_CHID;
			}

			if (ch >= 127) {
				return InputBuffer.INVALID_CHAR;
			}

			return autoTables.ch2ChID[ch];
		}

		short getReDfaID(int mxID) {
			return autoTables.mxID2ReDfaID[mxID - 1];
		}

		MxCell getTailTran(int mxID) {
			int index = (mxID - 1) * 2;
			tailTrn.setMxID(autoTables.mxID2TailPair[index++]);
			tailTrn.setDfaID(autoTables.mxID2TailPair[index]);
			return tailTrn;
		}

		public short getMinBnd(int mxID) {
			return autoTables.mxID2RangeBnd[(mxID - 1) * 2];
		}

		public short getMaxBnd(int mxID) {
			return autoTables.mxID2RangeBnd[(mxID - 1) * 2 + 1];
		}

		boolean isMaxBndInf(int mxID) {
			if (getMaxBnd(mxID) < 0) {
				return true;
			}
			return false;
		}

		int getMxSize() {
			return autoTables.epsRowIndex2True.length;
		}

		private int getSSSize() {
			return autoTables.ssSize;
		}

		int getRuleSize() {
			return autoTables.ruleSsIDs.length;
		}

		boolean isRuleSsOn(int rID) {
			if (ssOnSet.get(autoTables.ruleSsIDs[rID])) {
				return true;
			}
			return false;
		}

		private short getRuleSsID(int rID) {
			return autoTables.ruleSsIDs[rID];
		}

		boolean isBolAnchor(int rID) {
			if ((autoTables.ruleAnchors[rID] & BOL_ANCHOR) > 0) {
				return true;
			}
			return false;
		}

		boolean isEolAnchor(int rID) {
			if ((autoTables.ruleAnchors[rID] & EOL_ANCHOR) > 0) {
				return true;
			}
			return false;
		}

		boolean isDfaStartStateTurnOn(MxCell cell) {
			for (int i = ssOnSet.nextSetBit(0); i >= 0; i = ssOnSet.nextSetBit(i + 1)) {
				int byteIndex = i / BYTE_SIZE;
				int bitIndex = i % BYTE_SIZE;
				int mask = 1 << bitIndex;
				int index = cell.getDfaID() * autoTables.dfaSsSetByteSize + byteIndex;

				if ((autoTables.mxID2DfaSsSet[cell.getMxID()][index] & mask) > 0) {
					return true;
				}
			}
			return false;
		}

		void setActiveStates(short... onSIDs) {

			clearOnSS();

			for (short onSID : onSIDs) {

				ssOnList.add(onSID);
				ssOnSet.set(onSID);

				short[] ssIns = getSsInserts(onSID);
				if (ssIns != null) {
					for (int i = 0; i < ssIns.length; i++) {
						ssOnSet.set(ssIns[i]);
					}
				}
			}
		}

		private void clearOnSS() {
			if (ssOnList.size() > 0) {
				ssOnList.clear();
			}
			if (ssOnSet.cardinality() > 0) {
				ssOnSet.clear();
			}
		}

		private short[] getSsInserts(int sID) {
			return autoTables.ssInserts[sID];
		}
	}

	/**
	 * 
	 * 
	 * @author Mike-Red
	 * 
	 */
	private static int icntr1;

	class DfaGearsZip implements Cloneable {

		public final int zID;

		private GroupRrdBuilder grpBldr;
		final int mxID;
		final DfaAuto dfaAuto;
		final int hSize;
		final int maxBnd, minBnd;

		DfaGearsZip(int mxID, DfaAuto dfaAuto, GroupRrdBuilder grpBldr) {
			this.zID = icntr1++;
			this.mxID = mxID;
			this.dfaAuto = dfaAuto;
			this.hSize = dfaAuto.getHuggerSize(mxID) + 1;
			this.grpBldr = grpBldr;// new GroupRrdBuilder(hSize);
			this.maxBnd = dfaAuto.getMaxBnd(mxID);
			this.minBnd = dfaAuto.getMinBnd(mxID);
		}

		public DfaGearsZip(int mxID, DfaAuto dfaAuto) {
			this(mxID, dfaAuto, new GroupRrdBuilder(dfaAuto.getHuggerSize(mxID) + 1));
		}

		public boolean isExpired() {

			return grpBldr == null;
		}

		public boolean makeOutterGb2ReTran() {

			return makeGb2ReTrn(null);

		}

		public DfaGearsZip makeInnerGb2ReTran() {

			RrdBuilder prnBldr = new RrdBuilder(hSize - 1);

			makeGb2ReTrn(prnBldr);

			if (prnBldr.getRrdCount() > 0) {

				DfaGearsZip retV = new DfaGearsZip(dfaAuto.getHuggerMxID(mxID), dfaAuto, prnBldr.getGroupRrdBldr());
				if (retV.grpBldr == null || retV.grpBldr.__getRrdCount() == 0) {
					throw new RuntimeException();
				}
				return retV;
			}

			return null;
		}

		public boolean makeGb2ReTrn(RrdBuilder prnBldr) {

			boolean tailTran = false;

			short[] meOut = grpBldr.unpack();
			List<Integer> remRrdIDs = new ArrayList<>();

			int rrdCount = meOut.length / hSize;
			for (int i = 0; i < rrdCount; i++) {
				int rrdDp = i * hSize;
				int ix = rrdDp + hSize - 1;

				if (++meOut[ix] >= minBnd) {
					if (prnBldr != null) {
						prnBldr.addRrd(meOut, rrdDp);
					}
					tailTran = true;
				}

				if (meOut[ix] >= maxBnd) {
					remRrdIDs.add(i);
					continue;
				}
			}

			if (remRrdIDs.size() == rrdCount) {
				this.grpBldr = null;
				return tailTran;
			}

			if (remRrdIDs.size() > 0) {

				short[] meIn = new short[meOut.length - remRrdIDs.size() * hSize];
				int end = 0;
				for (int i = 0, j = 0; i < rrdCount; i++) {
					if (j < remRrdIDs.size() && remRrdIDs.get(j) == i) {
						++j;
						continue;
					}
					int rrdDp = i * hSize;
					for (int r = 0; r < hSize; r++) {

						meIn[end++] = meOut[rrdDp + r];
					}

				}
				meOut = meIn;

				rrdCount -= remRrdIDs.size();
			}
			GroupSorter gs = new GroupSorter(hSize, meOut, rrdCount);

			this.grpBldr = gs.createGroups();

			return tailTran;
		}

		public void addNewSpin(DfaGearsZip huggers) {

			if (hSize == 1) {

				grpBldr.addRrd(hSize - 1, 0);

			} else {

				int s1 = grpBldr.getSpinCount();
				int s2 = huggers.grpBldr.getSpinCount();
				int rrdCount = s1 + s2;
				short[] arr = new short[rrdCount * hSize];
				if (s1 > 0) {
					grpBldr.unpack(arr, 0, false, -1);
				}
				if (s2 < 1)
					throw new RuntimeException();
				huggers.grpBldr.unpack(arr, s1 * hSize, true, 0);
				GroupSorter group = new GroupSorter(hSize, arr, rrdCount);
				this.grpBldr = group.createGroups();

			}

		}

		public void merge(DfaGearsZip other) {

			int s1 = grpBldr.getSpinCount();
			int s2 = other.grpBldr.getSpinCount();
			int rrdCount = s1 + s2;
			short[] arr = new short[rrdCount * hSize];

			grpBldr.unpack(arr, 0, false, -1);
			other.grpBldr.unpack(arr, s1 * hSize, false, -1);

			GroupSorter group = new GroupSorter(hSize, arr, rrdCount);
			this.grpBldr = group.createGroups();

		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("DfaGearsZip {zID: " + zID + ", ");
			builder.append(grpBldr);
			return builder.toString();
		}

		public GroupRrdBuilder getGrpBldr() {
			return grpBldr;
		}

		@Override
		public DfaGearsZip clone() {
			GroupRrdBuilder bldrCln = null;
			if (this.grpBldr != null) {
				bldrCln = this.grpBldr.clone();
			}
			DfaGearsZip cln = new DfaGearsZip(mxID, dfaAuto, bldrCln);
			return cln;
		}

		public int getRrdCount() {
			if (grpBldr == null)
				return 0;
			return grpBldr.getRrdCount();
		}
	}

	/**
	 * 
	 * @author Mike-Red
	 * 
	 */

	class GroupSorter {

		private final int rrdSize;
		private final short[] data;
		private final int rrdCount;
		private final Rrd[] rr;
		private int sortIndex = 0;
		private final SpanList[] spanList;
		final GroupRrdBuilder grpRrdBldr;

		public GroupSorter(int rrdSize, short[] data, int rrdCount) {
			this.rrdSize = rrdSize;
			this.data = data;
			this.rrdCount = rrdCount;
			this.rr = new Rrd[rrdCount];
			if (rrdSize == 1) {
				int a = 0;
			}
			this.spanList = new SpanList[rrdSize - 1];
			for (int hrhID = 0; hrhID < spanList.length; hrhID++) {
				spanList[hrhID] = new SpanList();
			}
			for (int rID = 0; rID < rrdCount; rID++) {
				rr[rID] = new Rrd(rID);
			}
			grpRrdBldr = new GroupRrdBuilder(rrdSize);
		}

		int getRrdDataPtr(int rID) {
			return rID * rrdSize;
		}

		short getRrdValue(int rID, int index) {
			return data[getRrdDataPtr(rID) + index];
		}

		public GroupRrdBuilder createGroups() {

			if (rrdSize == 1) {

				Arrays.sort(rr, 0, rrdCount);

				for (int i = 0; i < rrdCount; i++) {

					if (i > 0 && rr[i].getHrhID2Value(0) == rr[i - 1].getHrhID2Value(0)) {
						continue;
					}
					grpRrdBldr.addRrd(0, rr[i].getHrhID2Value(0));
				}
				String s = grpRrdBldr.toString();
				return grpRrdBldr;
			}

			if (rrdCount == 1) {

				for (int i = 0; i < rrdSize; i++) {
					grpRrdBldr.addRrd(i, data[i]);
				}
				return grpRrdBldr;
			}

			groupsort();

			List<Integer> spins = new ArrayList<>();

			for (int i = 0; i < spanList[0].size(); i++) {
				int span_ix = spanList[0].getRrIX(i);
				spins.add((int) rr[span_ix].getHrhID2Value(0));
			}

			doGroups(0, spins);

			// ////////////////////////////////////////////////////

			for (int hrhID = 1; hrhID < rrdSize - 1; hrhID++) {

				int prn_spin_ix = 1;

				for (int span_ix = 0; span_ix < spanList[hrhID].size(); span_ix++) {

					final int rr_ix = spanList[hrhID].getRrIX(span_ix);

					if (prn_spin_ix < spanList[hrhID - 1].size() && rr_ix == spanList[hrhID - 1].getRrIX(prn_spin_ix)) {

						++prn_spin_ix;
						doGroups(hrhID, spins);
					}

					spins.add((int) rr[rr_ix].getHrhID2Value(hrhID));
				}

				doGroups(hrhID, spins);
			}

			if (rrdSize > 1) {

				int prn_spin_ix = 1;
				final int hrhID = rrdSize - 1;
				final int prnHrhID = hrhID - 1;

				for (int rr_ix = 0; rr_ix < rr.length; rr_ix++) {
					if (prn_spin_ix < spanList[prnHrhID].size() && rr_ix == spanList[prnHrhID].getRrIX(prn_spin_ix)) {

						++prn_spin_ix;
						doGroups(hrhID, spins);
					}

					spins.add((int) rr[rr_ix].getHrhID2Value(hrhID));
				}
				doGroups(hrhID, spins);
			}

			return grpRrdBldr;
		}

		void createRrd(List<Integer> rrd, List<Integer> spins, int low, int hi, int d) {

			if (hi - low > 3) {

				int offset = -spins.get(low) - 1;
				rrd.add(offset);
				rrd.add(d);
				rrd.add(hi - low);

			} else {

				for (int i = low; i < hi; i++) {
					rrd.add(spins.get(i));
				}
			}
		}

		void doGroups(int hrhID, List<Integer> spins) {

			if (spins.size() > 1) {
				List<Integer> elimDbl = new ArrayList<Integer>();
				elimDbl.add(spins.get(0));
				for (int i = 1; i < spins.size(); i++) {
					if (spins.get(i) == spins.get(i - 1)) {
						continue;
					}
					elimDbl.add(spins.get(i));
				}
				if (elimDbl.size() < spins.size()) {
					spins.clear();
					spins.addAll(elimDbl);
				}
			}

			if (spins.size() < 4) {
				grpRrdBldr.addRrd(hrhID, spins);

			} else {

				List<Integer> rrd = new ArrayList<>();

				int low = 0;
				int hi = 1;
				int d1 = spins.get(hi++) - spins.get(low);

				for (; hi < spins.size(); hi++) {

					int d2 = spins.get(hi) - spins.get(hi - 1);
					if (d2 == d1) {
						continue;
					}

					createRrd(rrd, spins, low, hi, d1);

					low = hi;
					if (hi < spins.size() - 1) {
						d1 = spins.get(low + 1) - spins.get(low);
					} else {
						d1 = Integer.MAX_VALUE;
					}
				}

				createRrd(rrd, spins, low, hi, d1);

				grpRrdBldr.addRrd(hrhID, rrd);

			}

			spins.clear();
		}

		public void groupsort() {

			HrhStack stk = new HrhStack(new HrhStackItem(0, 0, rrdCount));

			while (stk.size() > 0) {
				HrhStackItem top = stk.peek();

				if (top.hrhID < rrdSize - 1 && top.hasNextSpan()) {

					stk.push(new HrhStackItem(top.hrhID + 1, top.low, top.hi));
					continue;
				}

				stk.pop();
			}
		}

		/**
		 * 
		 * @author Mike-Red
		 * 
		 */
		class SpanList {
			private final List<Integer> spans = new ArrayList<>();

			public boolean addLowBound(Integer e) {
				return spans.add(e);
			}

			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				sb.append(spans);
				return sb.toString();
			}

			public int size() {
				return spans.size();
			}

			public Integer getRrIX(int index) {
				return spans.get(index);
			}

		}

		/**
		 * 
		 * @author Mike-Red
		 * 
		 */
		class HrhStackItem {
			final int hrhID;
			final int prnLow, prnHi;
			private int low, hi;

			public HrhStackItem(int hrhID, int fromIndex, int toIndex) {
				this.hrhID = hrhID;
				this.prnLow = fromIndex;
				this.prnHi = toIndex;
				sortIndex = hrhID;
				Arrays.sort(rr, prnLow, prnHi);
				this.hi = prnLow;
			}

			boolean hasNextSpan() {

				low = hi++;
				if (low >= prnHi) {
					return false;
				}
				spanList[hrhID].addLowBound(low);

				return nextSpan();
			}

			private boolean nextSpan() {

				while (hi < prnHi && rr[low].getHrhID2Value(hrhID) == rr[hi].getHrhID2Value(hrhID)) {
					++hi;
				}

				return hi <= prnHi;
			}

			void sort() {
				sortIndex = hrhID;
				Arrays.sort(rr, low, hi);
			}
		}

		/**
		 * 
		 * @author Mike-Red
		 * 
		 */
		class HrhStack {
			private final ArrayDeque<HrhStackItem> stk = new ArrayDeque<>();

			HrhStack(HrhStackItem root) {
				stk.push(root);
			}

			public HrhStackItem peek() {
				return stk.peek();
			}

			public void push(HrhStackItem e) {
				stk.push(e);
			}

			public HrhStackItem pop() {
				return stk.pop();
			}

			public int size() {
				return stk.size();
			}

		}

		/**
		 * 
		 * @author Mike-Red
		 * 
		 */
		class Rrd implements Comparable<Rrd> {

			final int rID;

			public Rrd(int rID) {
				this.rID = rID;
			}

			short getHrhID2Value(int _index) {
				return getRrdValue(rID, _index);
			}

			@Override
			public int compareTo(Rrd o) {
				short v1 = getHrhID2Value(sortIndex);
				short v2 = o.getHrhID2Value(sortIndex);

				if (v1 < v2) {
					return 1;
				}
				if (v1 > v2) {
					return -1;
				}

				return 0;
			}

			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < rrdSize; i++) {
					if (i > 0) {
						sb.append(",");
					}
					sb.append(getHrhID2Value(i));
				}
				return sb.toString();
			}

		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getClass().getName());
			sb.append(" {\n\trrdSize: ");
			sb.append(rrdSize);
			sb.append("\n\tdata: ");
			sb.append(Arrays.toString(data));
			sb.append("\n\tsortIndex: ");
			sb.append(sortIndex);
			sb.append("\n\trrdCount: ");
			sb.append(rrdCount);
			sb.append("\n\trr: ");
			sb.append(Arrays.toString(rr));
			sb.append("\n\n--------\n");
			for (int i = 0; i < rr.length; i++) {
				sb.append("\n" + rr[i] + "   ix=" + i);
			}
			sb.append("\n}");
			return sb.toString();
		}
	}

	/**
	 * 
	 * @author Mike-Red
	 * 
	 */

	class RrdBuilder {
		private short[] data;
		private int dend;
		final int rrdSize;

		public RrdBuilder(int rrdSize) {
			this.rrdSize = rrdSize;
			this.data = new short[16 * rrdSize];
		}

		public void addRrd(short[] meOut, int ix) {
			for (int i = 0; i < rrdSize; i++) {
				if (dend >= data.length)
					resize();
				data[dend++] = meOut[ix + i];

			}
		}

		void resize() {
			short[] tmp = new short[data.length * 2];
			System.arraycopy(data, 0, tmp, 0, dend);
			data = tmp;
		}

		public int getRrdCount() {
			return dend / rrdSize;
		}

		public GroupRrdBuilder getGroupRrdBldr() {
			int rrdCount = getRrdCount();

			if (rrdCount < 1)
				throw new RuntimeException();

			GroupSorter gs = new GroupSorter(rrdSize, data, rrdCount);

			return gs.createGroups();

		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < dend; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append(data[i]);
			}
			return sb.toString();
		}
	}

	/**
	 * 
	 * @author Mike-Red
	 * 
	 */
	private static int icntr2;

	class GroupRrdBuilder implements Cloneable {
		public final int bID;

		final int hSize;
		final short[][] hData;
		final short[] hend;
		public static final int HDATA_INIT_SIZE = 100;

		public GroupRrdBuilder(int hSize) {
			this(hSize, null, null);
		}

		private GroupRrdBuilder(int hSize, short[][] orgHData, short[] orgHEnd) {
			this.bID = icntr2++;
			this.hSize = hSize;
			this.hData = new short[hSize][];
			this.hend = new short[hSize];

			if (orgHData == null) {

				for (int hID = 0; hID < hData.length; hID++) {
					hData[hID] = new short[HDATA_INIT_SIZE];
				}

			} else {

				for (int hID = 0; hID < hSize; hID++) {
					hend[hID] = orgHEnd[hID];
				}

				for (int hID = 0; hID < hData.length; hID++) {
					hData[hID] = new short[orgHData[hID].length];
					System.arraycopy(orgHData[hID], 0, this.hData[hID], 0, orgHEnd[hID]);
				}
			}
		}

		public void addRrd(final int hID, List<Integer> rrd) {

			final int rrdDataPtr = hend[hID];

			write(hID, rrd.size() + 1);

			for (Integer s : rrd) {
				write(hID, s);
			}

			findDuplicateRrd(hID, rrdDataPtr);
		}

		public void addRrd(final int hID, int spin) {

			final int rrdDataPtr = hend[hID];

			write(hID, 2);

			write(hID, spin);

			findDuplicateRrd(hID, rrdDataPtr);
		}

		private void findDuplicateRrd(final int hrhID, final int rrdDataPtr) {
			if (rrdDataPtr > 0) {
				int mRID = getMatchRrdID(hrhID, rrdDataPtr);
				if (mRID > -1) {
					mRID = -mRID - 1; // for case of zero
					hend[hrhID] = (short) rrdDataPtr;
					write(hrhID, mRID);
				}
			}
		}

		private int getMatchRrdID(final int hrhID, final int lastDp) {
			int currDp = 0;
			int rrdID = -1;
			while (currDp < lastDp) {
				++rrdID;
				int rrdLen = hData[hrhID][currDp];
				if (rrdLen < 0) {
					++currDp;
					continue;
				}
				if (compareRrds(hrhID, currDp, lastDp)) {
					return rrdID;
				}
				currDp += hData[hrhID][currDp];
			}
			return -1;
		}

		private boolean compareRrds(int hrhID, int dp1, int dp2) {
			int rrdLen = hData[hrhID][dp1];
			for (int i = 0; i < rrdLen; i++, ++dp1, ++dp2) {
				if (hData[hrhID][dp1] != hData[hrhID][dp2]) {
					return false;
				}
			}
			return true;
		}

		private void write(int hrhID, int value) {
			if (hend[hrhID] >= hData[hrhID].length) {
				resize(hrhID);
			}

			hData[hrhID][hend[hrhID]++] = (short) value;

		}

		private void resize(int hrhID) {
			short[] tmp = new short[hData[hrhID].length * 2];
			System.arraycopy(hData[hrhID], 0, tmp, 0, hData[hrhID].length);
			hData[hrhID] = tmp;
		}

		public int getRrdSpinCount(int hid, int dp) {
			int spinCount = 0;
			int rrdLen = hData[hid][dp];
			if (rrdLen < 0)
				throw new RuntimeException();

			for (int i = 1; i < rrdLen; i++) {

				int v = hData[hid][dp + i];
				if (v > -1) {
					++spinCount;
				} else {
					i += 2;
					int glen = hData[hid][dp + i];
					spinCount += glen;
				}

			}
			return spinCount;
		}

		int getMyHID() {
			return hSize - 1;
		}

		public int getRrdCount() {
			return getSpinCount();
		}

		public int getSpinCount() {

			int hid = hSize - 1;
			int spinCount = 0;
			int dp = 0;

			while (dp < hend[hid]) {

				int fst = hData[hid][dp];
				if (fst == 0)
					throw new RuntimeException();

				if (fst > 0) {

					spinCount += getRrdSpinCount(hid, dp);
					dp += fst;

				} else {

					int rrdID = -(fst + 1);
					spinCount += getRrdSpinCount(hid, getRrdDataPtr(hid, rrdID));
					++dp;
				}

			}

			return spinCount;
		}

		/**
		 * 
		 * @author Mike-Red
		 * 
		 */
		class Group implements Iterable<Integer> {
			final int offset, delta, length;

			public Group(int offset, int delta, int length) {
				this.offset = offset;
				this.delta = delta;
				this.length = length;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + delta;
				result = prime * result + length;
				result = prime * result + offset;
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
				Group other = (Group) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (delta != other.delta)
					return false;
				if (length != other.length)
					return false;
				if (offset != other.offset)
					return false;
				return true;
			}

			private GroupRrdBuilder getOuterType() {
				return GroupRrdBuilder.this;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("Group {\n\toffset: ");
				builder.append(offset);
				builder.append("\n\tdelta: ");
				builder.append(delta);
				builder.append("\n\tlength: ");
				builder.append(length);
				builder.append("\n}");
				return builder.toString();
			}

			@Override
			public Iterator<Integer> iterator() {
				// TODO Auto-generated method stub
				return new Iterator<Integer>() {

					int index = -1;

					@Override
					public void remove() {
					}

					@Override
					public Integer next() {
						if (index >= length)
							throw new RuntimeException();
						return offset + index * delta;
					}

					@Override
					public boolean hasNext() {
						return ++index < length;
					}
				};
			}

		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(unpackToString() + "\n");
			return sb.toString();
		}

		/**
		 * 
		 * @author Mike-Red
		 * 
		 */
		class Rrd {
			final short[] rrd;

			public Rrd(short[] rrd) {
				this.rrd = rrd;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + Arrays.hashCode(rrd);
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
				Rrd other = (Rrd) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (!Arrays.equals(rrd, other.rrd))
					return false;
				return true;
			}

			private GroupRrdBuilder getOuterType() {
				return GroupRrdBuilder.this;
			}

		}

		int getRrdDataPtr(int hrhID, int rrdID) {
			int dp = 0;
			for (int i = 0; i < rrdID; i++) {
				int rrdLen = hData[hrhID][dp];
				if (rrdLen > 0) {
					dp += rrdLen;
				} else if (rrdLen < 0) {
					dp += 1;
				} else {
					throw new RuntimeException();
				}

			}
			return dp;
		}

		public short[] unpack() {
			int s1 = getSpinCount();
			short[] arr = new short[s1 * hSize];
			unpack(arr, 0, false, -1);
			return arr;
		}

		public int __getRrdCount() {
			short[] arr = unpack();
			return arr.length / hSize;
		}

		public String unpackToString() {
			short[] arr = unpack();
			StringBuilder sb = new StringBuilder();
			sb.append("bID:" + bID + "\n");
			sb.append("Rrd Sz=" + arr.length / hSize + "\n");
			for (int i = 0; i < arr.length; i++) {
				if (i > 0) {
					if (i % hSize == 0) {
						sb.append("\n");
					} else {
						sb.append(",");
					}
				}
				sb.append(arr[i]);
			}
			return sb.toString();
		}

		/**
		 * 
		 */

		public void unpack(short[] dst, int dstPos, boolean isAdd, int add) {

			List<Short> rrd = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			int rrdCount = 0;
			int[] valCnts = new int[hSize];
			int topRrdDp = 0;
			int topRrdID = 0;

			while (topRrdDp < hend[0]) {

				Stack stk = new Stack(new StackItem(0, topRrdDp));
				topRrdDp = getRrdDataPtr(0, ++topRrdID);

				while (stk.size() > 0) {

					StackItem top = stk.peek();

					if (top.hasRrdNext()) {

						rrd.add((short) top.rrdNext());
						++valCnts[top.hrhID];

						if (top.hrhID < hSize - 1) {

							int hrhID = top.hrhID + 1;
							int rrdDataPtr = getRrdDataPtr(hrhID, valCnts[top.hrhID] - 1);
							stk.push(new StackItem(hrhID, rrdDataPtr));
							continue;
						}
						sb.append(rrd);
						for (int i = 0; i < rrd.size(); i++) {
							dst[dstPos++] = rrd.get(i);
						}
						if (isAdd) {
							dst[dstPos++] = (short) add;
							sb.append(add);
						}
						sb.append("  id=" + (rrdCount++) + "\n");
						rrd.remove(rrd.size() - 1);
						continue;
					}
					stk.pop();
					if (stk.size() > 0) {

						rrd.remove(rrd.size() - 1);
					}

				}

			}
		}

		int getDataPtr(int hID, int rrdID) {
			int dp = 0;
			int currRrdID = -1;
			while (dp < hend[hID]) {
				if (++currRrdID == rrdID) {
					return dp;
				}

				int fst = hData[hID][dp];
				if (fst == 0)
					throw new RuntimeException();
				if (fst > 0) {
					dp += fst;
				} else {
					++dp;
				}
			}
			return -1;
		}

		class StackItem {
			final int hrhID;
			final int rrdDp, rrdLen;
			private int ix = 1;
			private Group grp;
			private Iterator<Integer> git;
			private int nextVal;

			public StackItem(int hrhID, int rrdDataPtr) {
				this.hrhID = hrhID;

				int fst = getRrdLen(rrdDataPtr);
				if (fst < 0) {
					int rrdID = -(fst + 1);
					rrdDp = getDataPtr(hrhID, rrdID);
					rrdLen = getRrdLen(rrdDp);

				} else {
					rrdDp = rrdDataPtr;
					rrdLen = fst;
				}
				if (rrdLen < 2) {
					throw new RuntimeException();
				}
			}

			short getRrdLen(int dp) {
				return hData[hrhID][dp];
			}

			short getData() {
				return hData[hrhID][rrdDp + ix++];
			}

			boolean hasRrdNext() {

				if (git != null && git.hasNext()) {
					nextVal = git.next();
					return true;
				}

				if (ix >= rrdLen) {
					return false;
				}

				int v = getData();
				if (v >= 0) {

					nextVal = v;
					return true;
				}

				int offset = -(v + 1);
				grp = new Group(offset, getData(), getData());

				git = grp.iterator();
				if (!git.hasNext()) {
					throw new RuntimeException();
				}
				nextVal = git.next();
				return true;
			}

			int rrdNext() {

				return nextVal;
			}

		}

		/**
		 * 
		 * @author Mike-Red
		 * 
		 */
		class Stack {
			private final ArrayDeque<StackItem> stk = new ArrayDeque<>();

			Stack(StackItem root) {
				push(root);
			}

			public StackItem peek() {
				return stk.peek();
			}

			public void push(StackItem e) {
				stk.push(e);
			}

			public StackItem pop() {
				return stk.pop();
			}

			public int size() {
				return stk.size();
			}

		}

		@Override
		public GroupRrdBuilder clone() {
			return new GroupRrdBuilder(hSize, hData, hend);
		}
	}

	/**
	 * 
	 * 
	 * @author Mike-Red
	 * 
	 */

	class MxCellList {

		private static final int DEFAULT_CAPACITY = 16;
		private int[] arr;
		private int fill;

		MxCellList() {
			this(DEFAULT_CAPACITY);
		}

		MxCellList(int capacity) {
			this.arr = new int[capacity * MxCell.Fields.Size];
		}

		void get(int index, MxCell cell) {
			int at = index * MxCell.Fields.Size;
			cell.setMxID(arr[at + MxCell.Fields.MxID]);
			cell.setDfaID(arr[at + MxCell.Fields.DfaID]);
		}

		void add(MxCell cell) {

			if (fill == arr.length) {
				resize();
			}
			arr[fill + MxCell.Fields.MxID] = cell.getMxID();
			arr[fill + MxCell.Fields.DfaID] = cell.getDfaID();
			fill += MxCell.Fields.Size;
		}

		int size() {
			return fill / MxCell.Fields.Size;
		}

		public void clear() {
			fill = 0;
		}

		private void resize() {
			int[] temp = new int[arr.length * 2];
			System.arraycopy(arr, 0, temp, 0, arr.length);
			arr = temp;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("MxCellList {\n");
			sb.append("\tsize: " + size());
			MxCell cell = new MxCell();
			for (int i = 0; i < size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				get(i, cell);
				sb.append("\n\t" + cell);
			}
			sb.append("\n}");
			return sb.toString();
		}

	}

	/**
	 * 
	 * 
	 * @author Mike-Red
	 * 
	 */

	class MxCellDeque {

		private static final int DEFAULT_CAPACITY = 16;

		private int[] arr;
		private int hp, tp, fill; // head, tail ptrs

		MxCellDeque() {
			this(DEFAULT_CAPACITY);
		}

		MxCellDeque(int capacity) {
			this.arr = new int[capacity * MxCell.Fields.Size];
		}

		void add(MxCell cell) {

			if (tp == arr.length) {
				tp = 0;
			}

			if (fill == arr.length) {
				resize();
			}

			arr[tp + MxCell.Fields.MxID] = cell.getMxID();
			arr[tp + MxCell.Fields.DfaID] = cell.getDfaID();

			tp += MxCell.Fields.Size;
			fill += MxCell.Fields.Size;
		}

		int size() {
			return fill / MxCell.Fields.Size;
		}

		void remove(MxCell cell) {
			if (hp == arr.length) {
				hp = 0;
			}

			cell.setMxID(arr[hp + MxCell.Fields.MxID]);
			cell.setDfaID(arr[hp + MxCell.Fields.DfaID]);

			hp += MxCell.Fields.Size;
			fill -= MxCell.Fields.Size;
		}

		private void resize() {

			int[] temp = new int[arr.length * 2];

			int hp2End = arr.length - hp;

			System.arraycopy(arr, hp, temp, 0, hp2End);

			if (hp2End < arr.length) {// hp > 0, tp > 0
				System.arraycopy(arr, 0, temp, hp2End, tp);
			}

			hp = 0;
			tp = arr.length;
			arr = temp;
		}
	}

	/**
	 * 
	 * @author Mike-Red
	 * 
	 */
	class MxCell {

		class Fields {
			public static final short MxID = 0;
			public static final short DfaID = 1;
			public static final short Size = 2;
		}

		private int[] fields = new int[Fields.Size];

		MxCell() {
		}

		MxCell(int mxID, int dfaID) {
			fields[Fields.MxID] = mxID;
			fields[Fields.DfaID] = dfaID;
		}

		public int getMxID() {
			return fields[Fields.MxID];
		}

		public void setMxID(int mxID) {
			fields[Fields.MxID] = mxID;
		}

		public int getDfaID() {
			return fields[Fields.DfaID];
		}

		public void setDfaID(int dfaID) {
			fields[Fields.DfaID] = dfaID;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("MxCell [");

			sb.append("mxID: ");
			sb.append(fields[Fields.MxID]);
			sb.append(", dfaID: ");
			sb.append(fields[Fields.DfaID]);

			sb.append("]");
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(fields);
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
			MxCell other = (MxCell) obj;
			if (!Arrays.equals(fields, other.fields))
				return false;
			return true;
		}
	}

	/**
	 * 
	 * @author Mike-Red
	 * 
	 */

	// #LEX_BODY_END#

	class DfaAutoMx {

		 short chIDSize;
		 short[] ch2ChID;
		 short[] mxID2RangeBnd;
		 short[] mxID2ReDfaID;
		 short[] mxID2TailPair;
		 short mxDfaSize[];
		 short huggerMxID[];
		 byte[][] epsRowIndex2True;
		 int[][] epsRowIndex2ValueIndex;
		 short[][] epsValueIndex;
		 short[][] epsValue;
		 byte[][] nonEpsMxIndex2True;
		 int[][] nonEpsOneDimMxIndex2ValueIndex;
		 short[][] nonEpsValueIndex;
		 short[][] nonEpsValue;
		 int dfaSsSetByteSize;
		 byte[][] mxID2DfaSsSet;
		 int ssSize;
		 short[] ruleSsIDs;
		 byte[] ruleAnchors;
		 short[][] ssRuleOrder;
		 short[][] ssInserts;
		 String[] ruleNames;
	}


}
