package jltools.util.jlgen;

import java.io.*;
import jltools.util.jlgen.cmds.*;
import jltools.util.jlgen.lex.*;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.spec.*;
import jltools.util.jlgen.util.*;

public class JLgen
{
	public static final String HEADER = "jlgen: ";
	public static final String DEBUG_HEADER = "jlgen [debug]: ";
	public static boolean debug = false;

	public static void DEBUG (String s) {
		if (debug)
			System.out.println(DEBUG_HEADER + s);
	}
	
	public static void main (String args[]) {
		FileInputStream fileInput;
		String filename = null;				for (int i=0; i < args.length; i++) {
			// assume all switches begin with a dash '-'
			if (args[i].charAt(0) == '-') {
				if (args[i].equals("-c")) {
					// constant class
				}
				else { // invalid switch
					System.err.println(HEADER+" invalid switch: "+args[i]);
					usage();
				}
			} else {
				// not a switch: this must be a filename
				// but only do the 1st filename on the command line
				if (filename == null) {
					filename = args[i];
				} else {
					System.err.println("Error: multiple source files specified.");
					usage();
				}
			}
		}		if (filename == null) {
			System.err.println("Error: no filename specified.");
			usage();
		}		
		try {
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
		String parent = file.getParent();		spec.parseChain(parent == null ? "" : parent);
		
		/* now we have a linked list of inheritance, namely		 * JLgen_1, JLgen_2, ..., JLgen_n, CUP		 * We combine two at a time, starting from the end with the CUP spec		 */
		try {
			CUPSpec combined = spec.coalesce();			CodeWriter cw = new CodeWriter(System.out, 72); 
			combined.unparse(cw);
			cw.flush();
		} catch (JLgenError e) {
			System.out.println(e.getMessage());
			System.exit(1);		} catch (IOException e) {
			System.out.println(HEADER+"exception: "+e.getMessage());
			System.exit(1);
		}	}	
	
	public static void usage() {
		System.err.println("Usage: jlgen [-c ConstClass] <input file>\nwhere:\n"+
						   "\t-c <Class>\tclass prepended to token names to pass to <func>\n"+
						   "\t<input>\ta JLgen or CUP source file\n");
		System.exit(1);	}}