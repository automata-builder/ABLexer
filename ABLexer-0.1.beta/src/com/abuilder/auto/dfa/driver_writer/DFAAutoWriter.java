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

package com.abuilder.auto.dfa.driver_writer;

import com.abuilder.auto.dfa.toolbox.DFACharIDMxCollBldr;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr;
import com.abuilder.auto.dfa.toolbox.DFAProduct;
import com.abuilder.parser.ParseException;
import com.abuilder.parser.toolbox.ScriptBuilder;
import com.abuilder.util.ABUtil;

public class DFAAutoWriter {
	
	private final DFAProduct dfaProd;
	private final DFAMxCollBldr dfaMxCollBldr;
	private final DFACharIDMxCollBldr chIDMxBldr;
	private final ScriptBuilder scBldr;
	
	public DFAAutoWriter(DFAProduct dfaProd, ScriptBuilder scBldr) {
		this.dfaProd = dfaProd;
		this.dfaMxCollBldr = dfaProd.getDFAMxCollBldr();
		this.chIDMxBldr = dfaProd.getDfaCharIDMxCollBldr();
		this.scBldr = scBldr;
		
	}
	
	public String getDfaAutoMxClass() throws ParseException{
		
		StringBuilder sb = new StringBuilder();
		String nl = ABUtil.getSysLineSep();
		sb.append(dfaProd.getAlphabet().getAlpha2ChIDLookupComment() + nl);
		sb.append(dfaProd.getAlphabet().getAlphabetFinals() + nl);
		sb.append(dfaProd.getAlphabet().getAscii2ChIDAsString() + nl);
		sb.append(dfaProd.getRxRangeMap().getMxID2RangeBoundsAsString() + nl);
		sb.append(dfaMxCollBldr.getMxID2ReAndTailAsString() + nl);
		
		
		sb.append(chIDMxBldr.getMxDfaSize() + nl);
		sb.append(chIDMxBldr.getMxHuggerAsString() + nl);
		
		sb.append(chIDMxBldr.getEpsRowIndex2TrueAsString() + nl);
		sb.append(chIDMxBldr.getEpsRowIndex2ValueIndexAsString() + nl);
		sb.append(chIDMxBldr.getEpsValueIndexAndValueAsString() + nl);
		sb.append(chIDMxBldr.getNonEpsMxIndex2TrueAsString() + nl);
		sb.append(chIDMxBldr.getNonEpsOneDimMxIndex2ValueIndexAsString() + nl);
		sb.append(chIDMxBldr.getNonEpsValueIndexAndValueAsString() + nl);
		sb.append(dfaProd.getDFAStateCollBldr().getMxID2DfaSsSetAsString() + nl);
		sb.append(dfaProd.getNFAMach().getSsSize(nl) + nl);
		sb.append(dfaProd.getNFAMach().getRuleSsIDAndAnchorsAsString() + nl);
		sb.append(scBldr.getSsRuleOrderAsString() + nl);
		sb.append(scBldr.getRuleNamesAsString() + nl);
		return sb.toString();
	}
	
	public String getActionSwitch(){		
		return dfaProd.getNFAMach().getActionSwitch();
	}
}
