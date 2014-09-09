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

package com.abuilder.parser;
public class MyToken extends Token implements Comparable<MyToken>
{
  /**
   * Constructs a new token for the specified Image and Kind.
   */
  public MyToken(int kind, String image)
  {
     this.kind = kind;
     this.image = image;
  }

  int realKind = ABLexParserConstants.GT;

  /**
   * Returns a new Token object.
  */

  public static final Token newToken(int ofKind, String tokenImage)
  {
    return new MyToken(ofKind, tokenImage);
  }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MyToken {\n\tbeginLine: ");
		builder.append(beginLine);
		builder.append("\n\tbeginColumn: ");
		builder.append(beginColumn);
		builder.append("\n\tendLine: ");
		builder.append(endLine);
		builder.append("\n\tendColumn: ");
		builder.append(endColumn);
		builder.append("\n\timage: ");
		builder.append(image);
		builder.append("\n\tspecialToken: ");
		builder.append(specialToken);
		builder.append("\n}");
	
		return builder.toString();
	}

	@Override
	public int compareTo(MyToken o) {
		
		if(this.beginLine < o.beginLine){
			return -1;
		}
		
		if(this.beginLine > o.beginLine){
			return 1;
		}
		
		//this.beginLine == o.beginLine

		if(this.beginColumn < o.beginColumn){
			return -1;
		}

		if(this.beginColumn > o.beginColumn){
			return 1;
		}

		//they are equal
		
		return 0;
	

	}


  
}
