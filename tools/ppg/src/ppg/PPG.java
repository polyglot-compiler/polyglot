package jltools.util.jlgen;

import java.io.*;
import jltools.util.jlgen.cmds.*;
import jltools.util.jlgen.lex.*;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.spec.*;
import jltools.util.jlgen.util.*;

public class JLgen
{
	private static final String HEADER = "jlgen: ";
	private static final String DEBUG_HEADER = "jlgen [debug]: ";
	public static boolean debug = false;

	public static void DEBUG (String s) {
		if (debug)
			System.out.println(DEBUG_HEADER + s);
	}
	
	public static void main (String args[]) {
		FileInputStream fileInput;
		String filename = null;
		try {
			filename = args[0];
			fileInput = new FileInputStream(filename);
		}
		catch (FileNotFoundException e) {
			System.out.println("Error: "+filename+" is not found.");
			return;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(HEADER+"Error: No file name given.");
			return;
		}

		File file = new File(filename);
		String simpleName = file.getName(); 
		Lexer lex = new Lexer(fileInput, simpleName);
		
		Parser parser = new Parser(filename, lex);
		try {
			parser.parse();
		} catch (Exception e) {
			System.out.println(HEADER+"Exception: "+e.getMessage());
			return;
		}
		Spec spec = (Spec)parser.getProgramNode();		
		/* try #1		String parentDir = file.getPath();
		spec.parseChain(parentDir == null ? "" : parentDir);		*/
				/* try #2: uses java1.2 function
		File parent = file.getParentFile();
		spec.parseChain(parent == null ? "" : parent.getPath());		*/
		
		// try #3		String parent = file.getParent();		spec.parseChain(parent == null ? "" : parent);
				// now we have a linked list of inheritance, namely		// JLgen1, JLgen2, ..., JLgenN, CUP		// We combine two at a time, starting from the end with the CUP spec
		CUPSpec combined = spec.coalesce();				CodeWriter cw = new CodeWriter(System.out, 72); 
		try {
			combined.unparse(cw);
			cw.flush();		} catch (IOException e) {
			System.out.println(HEADER+"exception: "+e.getMessage());
			return;
		}		
	}		private static void pause() {		if (debug) {
			try{System.in.read();}catch(Exception e){}
		}	}
}