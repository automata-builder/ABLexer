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

import java.util.Arrays;

import com.abuilder.auto.dfa.nfa.Alphabet.Ascii;


public interface NFAConstants {
	enum TOKEN {
	    AMPERSAND,	//@
			ANY_CHR,      //.
	    ANGL_BRQ_OPEN, //<
	    ANGL_BRQ_CLOSE, //>
	    ASTERISK, //*
	    CARET, 			//^
	    BACKSLASH, //\
	    BOL_ANCHOR,       //^  
	    CCL_END,      //]
	    CCL_START,    //[
	    CCL_NEGATE,		//[^]
	    KLEENE_CLOS,  //*
	    COLON, //:
	    COMMA,
	    CR, //cariage return
	    CURLY_BRQ_CLOSE,  //} 
	    CURLY_BRQ_OPEN,   //{
	    DBL_QT,
	    DASH,         //-
	    DIGIT,
	    DOLLAR_SGN, //$
	    DOT, //.
	    EOF,          //EOF
	    EOL_ANCHOR,       //$
	    EOP,// = 1,   //end of pattern
	    EOL, //end of line
	    EQUAL_SGN, //=
	    EXCLM_SGN,
	    FRWD_SLASH, ///
	    GRAVE_ACCENT,//grave accent, `
	    NL, //new line
	    DECLR_DELIM, 	//%
	    LTTR,          //letter
	    LITERALLY,			//literal interpretation 
	    OPTINAL_CLOS,  //? Holub: OPTINAL 
	    OR,
	    PLUS_SGN,
	    POSITIVE_CLOS,  //+ 
	    POUND_SGN,
	    QUESTION_MARK,
	    RND_PRNTH_CLOSE,  //)
	    RND_PRNTH_OPEN,   //(
	    SECTN_DELIM, //%%
	    SNGL_QT,
	    SPACE,	//white space
	    NP,	//non-printable
	    SEMICLN, //;
	    START_STE_DECLR, 
	    TAB, //\t
	    TILDE, //~
	    UNDERSCORE,
	    VERT_BAR
	}
	
	/**
	 *
	 */
	
	public static class TokenMap {
		static final TOKEN[] t = new TOKEN[128];
		static {
			// 1        									2        								3        											4        											5        									6        											7        									8        									9        							10       							11       											12       											13       								14       										15
			t[0]  =TOKEN.NP; 							t[1]  =TOKEN.NP; 				t[2]  =TOKEN.NP;							t[3 ] =TOKEN.NP; 							t[4]  =TOKEN.NP; 					t[5]  =TOKEN.NP; 							t[6]  =TOKEN.NP; 					t[7]  =TOKEN.NP; 					t[8]  =TOKEN.NP; 			t[9]  =TOKEN.TAB; 		t[10] =TOKEN.NL; 						  t[11] =TOKEN.NP;							t[12] =TOKEN.NP; 				t[13] =TOKEN.CR; 					  t[14] =TOKEN.NP;
			t[15] =TOKEN.NP; 							t[16] =TOKEN.NP; 				t[17] =TOKEN.NP; 							t[18] =TOKEN.NP; 							t[19] =TOKEN.NP; 					t[20] =TOKEN.NP; 							t[21] =TOKEN.NP; 					t[22] =TOKEN.NP; 					t[23] =TOKEN.NP; 			t[24] =TOKEN.NP; 			t[25] =TOKEN.NP; 		  				t[26] =TOKEN.NP; 							t[27] =TOKEN.NP; 				t[28] =TOKEN.NP; 						t[29] =TOKEN.NP;
			t[30] =TOKEN.NP; 							t[31] =TOKEN.NP; 				t[32] =TOKEN.SPACE; 					t[33] =TOKEN.EXCLM_SGN; 			t[34] =TOKEN.DBL_QT; 			t[35] =TOKEN.POUND_SGN; 			t[36] =TOKEN.DOLLAR_SGN; 	t[37] =TOKEN.DECLR_DELIM; t[38] =TOKEN.SNGL_QT; t[39] =TOKEN.SNGL_QT; t[40] =TOKEN.RND_PRNTH_OPEN; 	t[41] =TOKEN.RND_PRNTH_CLOSE;	t[42] =TOKEN.ASTERISK;	t[43] =TOKEN.PLUS_SGN;			t[44] =TOKEN.COMMA; 	
			t[45] =TOKEN.DASH;  					t[46] =TOKEN.DOT;		    t[47] =TOKEN.FRWD_SLASH; 			t[48] =TOKEN.DIGIT; 					t[49] =TOKEN.DIGIT; 			t[50] =TOKEN.DIGIT; 					t[51] =TOKEN.DIGIT; 			t[52] =TOKEN.DIGIT; 			t[53] =TOKEN.DIGIT; 	t[54] =TOKEN.DIGIT; 	t[55] =TOKEN.DIGIT; 					t[56] =TOKEN.DIGIT; 					t[57] =TOKEN.DIGIT; 		t[58] =TOKEN.COLON; 				t[59] =TOKEN.SEMICLN;
			t[60] =TOKEN.ANGL_BRQ_OPEN; 	t[61] =TOKEN.EQUAL_SGN;	t[62] =TOKEN.ANGL_BRQ_CLOSE; 	t[63] =TOKEN.QUESTION_MARK;		t[64] =TOKEN.AMPERSAND;   t[65] =TOKEN.LTTR; 				  	t[66] =TOKEN.LTTR; 				t[67] =TOKEN.LTTR; 				t[68] =TOKEN.LTTR; 		t[69] =TOKEN.LTTR; 		t[70] =TOKEN.LTTR; 						t[71] =TOKEN.LTTR; 						t[72] =TOKEN.LTTR; 			t[73] =TOKEN.LTTR; 					t[74] =TOKEN.LTTR; 	
			t[75] =TOKEN.LTTR; 						t[76] =TOKEN.LTTR; 			t[77] =TOKEN.LTTR; 						t[78] =TOKEN.LTTR;						t[79] =TOKEN.LTTR; 				t[80] =TOKEN.LTTR; 						t[81] =TOKEN.LTTR; 				t[82] =TOKEN.LTTR; 				t[83] =TOKEN.LTTR; 		t[84] =TOKEN.LTTR; 		t[85] =TOKEN.LTTR; 						t[86] =TOKEN.LTTR; 						t[87] =TOKEN.LTTR; 			t[88] =TOKEN.LTTR; 					t[89] =TOKEN.LTTR; 	
			t[90] =TOKEN.LTTR;	  				t[91] =TOKEN.CCL_START; t[92] =TOKEN.BACKSLASH; 			t[93] =TOKEN.CCL_END; 				t[94] =TOKEN.CARET;				t[95] =TOKEN.UNDERSCORE; 			t[96] =TOKEN.GRAVE_ACCENT;t[97] =TOKEN.LTTR; 				t[98] =TOKEN.LTTR; 		t[99] =TOKEN.LTTR; 		t[100]=TOKEN.LTTR; 				  	t[101]=TOKEN.LTTR; 						t[102]=TOKEN.LTTR; 			t[103]=TOKEN.LTTR; 					t[104]=TOKEN.LTTR; 	
			t[105]=TOKEN.LTTR;   					t[106]=TOKEN.LTTR; 			t[107]=TOKEN.LTTR; 						t[108]=TOKEN.LTTR; 						t[109]=TOKEN.LTTR;				t[110]=TOKEN.LTTR; 						t[111]=TOKEN.LTTR;		  	t[112]=TOKEN.LTTR; 			  t[113]=TOKEN.LTTR; 		t[114]=TOKEN.LTTR; 		t[115]=TOKEN.LTTR; 						t[116]=TOKEN.LTTR; 						t[117]=TOKEN.LTTR; 			t[118]=TOKEN.LTTR; 					t[119]=TOKEN.LTTR; 	
			t[120]=TOKEN.LTTR;   					t[121]=TOKEN.LTTR; 			t[122]=TOKEN.LTTR;						t[123]=TOKEN.CURLY_BRQ_OPEN; 	t[124]=TOKEN.VERT_BAR; 	  t[125]=TOKEN.CURLY_BRQ_CLOSE; t[126]=TOKEN.TILDE;				t[127]=TOKEN.NP;
		}
		
		public static TOKEN lexeme2Token(int lexeme) {
			return t[lexeme];
		}
		
		public static int size(){
			return t.length;
		}
		
		static final int[] l = new int[TOKEN.values().length];
		
		static {
			Arrays.fill(l, -1);
			l[TOKEN.CCL_START.ordinal()] = '[';
			l[TOKEN.CCL_END.ordinal()] = ']';
			l[TOKEN.DECLR_DELIM.ordinal()] = '%';
		}
		
		public static int token2Lexeme(TOKEN token){
			return l[token.ordinal()];
		}
	}
	
	/**
	 *
	 */
	
//	class CHAR {
//		public static String toString(int c){
//			if(c < 0)
//				return "INVLD("+c+")";
//			switch(c){
//			case '\r':
//				return "'\\r' "+c;
//			case '\n':
//				return "'\\n' "+c;
//			case '\t':
//				return "'\\t' "+c;
//			case ' ':
//				return "'ws' "+c;
//			default:
//				if(c >= 0 && c <= 31)
//					return "'NP'("+c+")";
//				//return "'"+(char)c + "'("+c+")";
//				return "'"+(char)c + "'"+c;
//			}
//		}
//	}
	
	/**
	 *
	 */
	
//	public static class EDGE {
//		public static final int EPSILON = -1;
//		public static final int CCL = -2;
//		public static final int EMPTY = -3;
//		public static String toString(int e){
//			switch(e){
//				case EPSILON:
//					return "EPSILON";
//				case CCL:
//					return "CCL";
//				case EMPTY:
//					return "EMPTY";
//				default:
//					return Ascii.toString(e);
//			}
//		}
//	}
	
	/**
	 *
	 */
	
	public static class ANCHOR {
		public static final int NONE = 0;
		public static final int BOL = 1;
		public static final int EOL = 2;
		public static final int BOTH = BOL | EOL;
	}
	
	/**
	 * 
	 *
	 */
	
	public static class NFAType {
		//KLN_CLS = KLEENE CLOSURE
		public static final int NONE 				= 0; 	//00000000=0
		public static final int RNG_HD 			= 1; 	//00000001=1
		public static final int RNG_RE 			= 2; 	//00000010=2
		public static final int RNG_GB 			= 4; 	//00000100=4
		public static final int RNG_TL 			= 8; 	//00001000=8
		public static final int OR_HUB_HD		= 16;	//00010000=16
		public static final int OR_HUB_TL		= 32;	//00100000=32
		public static final int RULE_HUB		= 64;	//01000000=64
		public static final int FWRD_SLASH	= 128;//10000000=128
		
		public static boolean isRngHead(int nfaType){
			return (nfaType & RNG_HD) != 0;
		}
		
		public static boolean isRngReent(int nfaType){
			return (nfaType & RNG_RE) != 0;
		}
		
		public static boolean isRngGoback(int nfaType){
			return (nfaType & RNG_GB) != 0;
		}
		
		public static boolean isRngTail(int nfaType){
			return (nfaType & RNG_TL) != 0;
		}
		
		public static boolean isRuleHub(int nfaType){
			return (nfaType & RULE_HUB) != 0;
		}
		
		public static boolean isOrHubHead(int nfaType){
			return (nfaType & OR_HUB_HD) != 0;
		}
		
		public static boolean isOrHubTail(int nfaType){
			return (nfaType & OR_HUB_TL) != 0;
		}
		
		public static String toString(int nfaType) {
			StringBuilder sb = new StringBuilder();
			if(nfaType == 0){
				return "NONE";
			}
			
			boolean appnd = false;
			if((nfaType & RNG_HD) > 0){
				sb.append("RNG_HEAD");
				appnd = true;
			}
			
			if((nfaType & RNG_RE) > 0){
				if(appnd)sb.append(", ");
				sb.append("RNG_REENTRY");
				appnd = true;
			}
			
			if((nfaType & RNG_GB) > 0){
				if(appnd)sb.append(", ");
				sb.append("RNG_GOBACK");
				appnd = true;
			}
			
			if((nfaType & RNG_TL) > 0) {
				if(appnd)sb.append(", ");
				sb.append("RNG_TAIL");
				appnd = true;
			}
			
			if((nfaType & OR_HUB_HD	) > 0){
				if(appnd)sb.append(", ");
				sb.append("OR_HUB_HEAD");
				appnd = true;
			}
			
			if((nfaType & OR_HUB_TL	) > 0){
				if(appnd)sb.append(", ");
				sb.append("OR_HUB_TAIL");
				appnd = true;
			}
			
			if((nfaType & RULE_HUB	) > 0){
				if(appnd)sb.append(", ");
				sb.append("RULE_HUB");
				appnd = true;
			}
			
			if((nfaType & FWRD_SLASH	) > 0){
				if(appnd)sb.append(", ");
				sb.append("FWRD_SLASH");
			}
			return sb.toString();
		}
	}
}
