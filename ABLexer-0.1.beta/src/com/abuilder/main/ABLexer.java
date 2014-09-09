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

package com.abuilder.main;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import com.abuilder.auto.dfa.DFABldr;
import com.abuilder.auto.dfa.driver_writer.LexClassWriter;
import com.abuilder.io.ABFile;
import com.abuilder.io.ABFileReader;
import com.abuilder.parser.ABLexParser;
import com.abuilder.parser.ParseException;
import com.abuilder.parser.ParseHelp;
import com.abuilder.parser.toolbox.ScriptBuilder;
import com.abuilder.util.ABUtil;

public class ABLexer {

	private static final int OK_EXIT = 0, ERR_EXIT = 1;
	private static final String APP_VERSION = "1.0 Beta";
	private int exitStatus = 0;
	private final boolean isDebug;

	public ABLexer(boolean isDebug, String infilePath, String outdirPath) {

		this.isDebug = isDebug;
		File srcFile = ABFile.getInputFile(infilePath);
		File outDir = ABFile.getOutdir(outdirPath);

		if (srcFile == null || outDir == null) {
			return;
		}
		exitStatus = create(srcFile, outDir);
	}

	private int create(File srcFile, File outDir) {

		try {

			ScriptBuilder scBldr = parse(stripFileExt(srcFile.getName()), ABFileReader.read(srcFile));

			if (scBldr != null) {

				if (!scBldr.isLexClosDefined()) {
					System.err.println("No " + getClass().getSimpleName() + " closure defined. ");
					System.err.println(getClass().getSimpleName() + ": Nothing done.");
					return 1;
				}

				createLex(outDir, srcFile.getName(), scBldr);
			}

			System.out.println("Input  file: " + srcFile.getCanonicalPath());
			System.out.println("Output file: " + outDir.getCanonicalPath() + ABUtil.getSysFileSep() + srcFile.getName());
			System.out.println(getClass().getSimpleName() + ": Finished OK.");
			return ABLexer.OK_EXIT;

		} catch (com.abuilder.parser.TokenMgrError e) {

			System.err.println("ERROR: " + e.getMessage());
			System.err.println(getClass().getSimpleName() + ": Nothing done.");

		} catch (ParseException e) {

			System.err.println("ERROR: " + e.getMessage());
			System.err.println(getClass().getSimpleName() + ": Nothing done.");

		} catch (Exception e) {

			e.printStackTrace();
		}

		return ABLexer.ERR_EXIT;
	}

	private ScriptBuilder parse(String infileName, char[] cbuf) throws ParseException {

		ScriptBuilder scBldr = null;
		CharArrayReader cr = new CharArrayReader(cbuf);
		BufferedReader br = new BufferedReader(cr);

		ABLexParser parser = new ABLexParser(br);
		ParseHelp.setParser(parser);
		parser.setFileName(infileName);

		parser.CompilationUnit();
		scBldr = parser.getScriptBuilder();

		if (scBldr.getStateColl().size() == 0) {
			throw new ParseException(getClass().getSimpleName() + " must contain at least one state with at least one rule.");
		}

		scBldr.setCBuf(cbuf);
		scBldr.setUserActions();

		return scBldr;
	}

	private void createLex(File outdir, String outfileName, ScriptBuilder scBldr) throws Exception {

		DFABldr bldr = new DFABldr(scBldr.getDfaProd());
		bldr.buildDFA();

		LexClassWriter writer = new LexClassWriter(new File(outdir, outfileName), scBldr);
		writer.write(isDebug);
	}

	private String stripFileExt(String name) {

		int i = name.indexOf('.');
		if (i > -1) {
			return name.substring(0, i);
		} else {
			return name;
		}
	}

	int getExitStatus() {
		return exitStatus;
	}

	private static void usage() {
		System.out.println("Usage:\n\t" + ABLexer.class.getName() + " <options> <source_file> <output_directory>");
		System.out.println("options: ");
		System.out.println("-v  show version.");
		System.out.println("-?  show usage.");
		System.out.println("-d  set debug=true.");
	}

	/**
	 * @param args
	 */

	public static void main(String[] args) {

		ABLexer lex = null;
		int exitStatus = ABLexer.OK_EXIT;

		if (args.length == 0) {

			errExit();

		} else {

			switch (args[0]) {

			case "-v":
				System.out.println("version: " + APP_VERSION);
				break;

			case "-?":
				usage();
				break;

			case "-d":

				if (args.length != 3) {
					errExit();
				}
				lex = new ABLexer(true, args[1], args[2]);
				exitStatus = lex.getExitStatus();
				break;

			default:

				if (args.length == 2) {

					lex = new ABLexer(false, args[0], args[1]);
					exitStatus = lex.getExitStatus();

				} else {

					errExit();
				}
			}
		}

		System.exit(exitStatus);
	}

	private static void errExit() {
		usage();
		System.exit(ABLexer.ERR_EXIT);
	}
}
