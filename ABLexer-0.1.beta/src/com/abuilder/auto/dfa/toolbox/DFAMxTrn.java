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


public class DFAMxTrn {
		
	private final DFAProduct product;
	private final int toMxID;
	private final int toDfaID;
	
	public DFAMxTrn(DFAProduct product, int toMxID, int toDfaID) {
		this.product = product;
		this.toMxID = toMxID;
		this.toDfaID = toDfaID;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + toDfaID;
		result = prime * result + toMxID;
		return result;
	}

	public boolean has(int toTrMxID, int toDfaID){
		if(this.toMxID == toTrMxID && this.toDfaID == toDfaID){
			return true;
		}
		return false;
	}
			
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DFAMxTrn other = (DFAMxTrn) obj;
		if (toDfaID != other.toDfaID)
			return false;
		if (toMxID != other.toMxID)
			return false;
		return true;
	}


	public int getToMxID() {
		return toMxID;
	}

	@Override
	public String toString() {
				
		if(toDfaID < 0){
			return "AccRuleID="+(-toDfaID);
		}
		
		String toDfaIDText;
		
		switch(toDfaID){
		
		case DFAState.DFAID.REENTRY_DFAID:
			toDfaIDText = "Re";
			break;
					
		default:
			toDfaIDText = ""+ toDfaID;
		}
		return "[" + toMxID + ", " + toDfaIDText + "]";
	}

	public int getToDfaID() {
		return toDfaID;
	}
}