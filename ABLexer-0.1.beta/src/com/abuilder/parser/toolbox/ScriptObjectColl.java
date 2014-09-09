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

package com.abuilder.parser.toolbox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.abuilder.parser.ParseException;

public class ScriptObjectColl<V extends ScriptObject> {
	
	private final Map<ABIdentifier, V> idfr_Obj = new LinkedHashMap<>();
	private final ArrayList<V> objList = new ArrayList<>();
	//private final Logger logger = Logger.getLogger(getClass().getName());
	
	public V add(V obj) throws ParseException{
		
		objList.add(obj);
		
		if(idfr_Obj.containsKey(obj.getIdentifier())){
			
			V oldObj = idfr_Obj.get(obj.getIdentifier());
			throw new ParseException(obj.getIdentifier().getPos() + ";  " + obj.getType() + " " + obj.getIdentifier() + " already defined.");
			
		} else {
			idfr_Obj.put(obj.getIdentifier(), obj);
		}
		
		return obj;
	}

	public int size() {
		return objList.size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScriptObjectColl {\n\tobjList: ");
		builder.append(objList);
		builder.append("\n}");
		return builder.toString();
	}

	public V getItem(ABIdentifier idfr) {
		return idfr_Obj.get(idfr);
	}

	public V getItem(int index) {
		return objList.get(index);
	}
	
	
}
