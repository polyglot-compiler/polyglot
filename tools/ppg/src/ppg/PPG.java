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
	public static boolean debug = true;

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

		File f = new File(filename);
		String simpleName = f.getName();
		Lexer lex = new Lexer(fileInput, simpleName);
		
		Parser parser = new Parser(filename, lex);
		try {
			parser.parse();
		} catch (Exception e) {
			System.out.println(HEADER+"Exception: "+e.getMessage());
			return;
		}
		Spec spec = (Spec)parser.getProgramNode();
		
		spec.parseChain();
		
	}
}
