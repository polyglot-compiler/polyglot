package jltools.util.jlgen.spec;
import java.util.*;
import jltools.util.jlgen.code.*;import jltools.util.jlgen.parse.*;

public abstract class Spec implements Unparse
{	protected String packageName, start;
	protected Vector code, imports, symbols, prec;	protected InitCode initCode;
	protected ActionCode actCode;	protected ParserCode parserCode;	protected ScanCode scanCode;
	protected JLgenSpec child;		public Spec () {
		child = null;
	}	
	public void setStart (String startSym) {
		start = startSym;
	}		public void setPkgName (String pkgName) {
		packageName = pkgName;
	}
	
	public void replaceCode (Vector imports) {
		
	}	
	public void addImports (Vector imp) {		for (int i=0; i < imp.size(); i++) {			imports.addElement(imp.elementAt(i));		}	}
	
		
		public void setChild (JLgenSpec childSpec) {		child = childSpec;
	}		// default action is to do nothing: as CUP does
	public void parseChain() {}

	/**
	 * Combine the chain of inheritance into one CUP spec
	 */	public abstract CUPSpec coalesce();	
}
