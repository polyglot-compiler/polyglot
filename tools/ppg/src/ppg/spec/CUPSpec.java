package jltools.util.jlgen.spec;

import java.io.*;
import java.util.*;
import jltools.util.jlgen.atoms.*;
import jltools.util.jlgen.code.*;
import jltools.util.jlgen.lex.*;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.util.*;

public class CUPSpec extends Spec
{
	private static final String HEADER = "jlgen [cupspec]: ";
	private String packageName, start;
	private Vector imports, code, symbols, prec, productions;
	
	public CUPSpec (String pkg, Vector imp, Vector codeParts, Vector syms,
					Vector precedence, String startSym, Vector prods)
	{
		packageName = pkg;
		imports = imp;
		code = codeParts;
		symbols = syms;
		prec = precedence;
		start = startSym;
		productions = prods;
	}

	public void unparse(CodeWriter cw) throws ParserError {
		/*
		cw.begin(0);
		cw.write("CUP Spec\n");
		cw.end();
		*/
		try {
			export(System.out);
		} catch (Exception e) {
			System.out.println(HEADER+"Exception: "+e.getMessage());
			return;
		}
	}
	
	/**
	 * Write out the CUP specification to the stream
	 */
	public void export(PrintStream out) throws Exception {
		// package
		out.println("package " + packageName + ";");
		out.println();
		
		// import
		for (int i=0; i < imports.size(); i++)
			out.println("import " + (String) imports.elementAt(i) + ";");
		out.println();

		// code
		for (int i=0; i < code.size(); i++)
			out.println( ((Code) code.elementAt(i)).toString() );
		out.println();
		
		// symbols
		for (int i=0; i < symbols.size(); i++)
			out.println( ((SymbolList) symbols.elementAt(i)).toString() );
		out.println();
		
		// precedence
		
		// start
		out.println("start with " + start + ";");
		out.println();
		
		// productions
		for (int i=0; i < productions.size(); i++)
			out.println( ((Production) productions.elementAt(i)).toString() );
		out.println();
		
		out.flush();
		out.close();
	}
}
