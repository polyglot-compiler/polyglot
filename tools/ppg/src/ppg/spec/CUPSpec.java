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
	private Vector productions;
	
	public CUPSpec (String pkg, Vector imp, Vector codeParts, Vector syms,
					Vector precedence, String startSym, Vector prods)
	{		super();
		packageName = pkg;
		imports = imp;
		code = codeParts;
		symbols = syms;
		prec = precedence;
		start = startSym;
		productions = prods;
	}		public CUPSpec coalesce() {
		// cannot have a parent by definition		return this;	}	
		public void addSymbols(Vector syms) {		if (syms == null)
			return;				for (int i=0; i < syms.size(); i++) {			symbols.addElement(syms.elementAt(i));		}
	}	
	public void dropSymbol(GrammarSymbol gs) {		for (int i=0; i < symbols.size(); i++ ) {			Vector list = (Vector) symbols.elementAt(i);
			for (int j=0; j < list.size(); j++) {
				if (gs.equals(list.elementAt(j)))
					list.removeElementAt(j);
			}
		}	}
	
	public void dropProductions(Production p) {			}
		public void dropAllProductions(Nonterminal nt) {			}
	public void addProductions(Production p) {			}
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
