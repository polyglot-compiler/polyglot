package jltools.util.jlgen.spec;
import java.util.*;
import jltools.util.jlgen.code.*;import jltools.util.jlgen.parse.*;

public abstract class Spec implements Unparse
{	protected String packageName, start;
	protected Vector code, imports, symbols, prec;	protected InitCode initCode;
	protected ActionCode actionCode;	protected ParserCode parserCode;	protected ScanCode scanCode;
	protected JLgenSpec child;		public Spec () {		initCode = null;		actionCode = null;		parserCode = null;		scanCode = null;
		child = null;
	}	
	public void setStart (String startSym) {
		start = startSym;
	}		public void setPkgName (String pkgName) {
		packageName = pkgName;
	}
	
	public void replaceCode (Vector codeParts) {		if (codeParts == null) 			return;				Code code;
		for (int i=0; i < codeParts.size(); i++) {
			try {				code = (Code) codeParts.elementAt(i);				if (code instanceof ActionCode) {					actionCode = (ActionCode) code;				} else if (code instanceof InitCode) {					initCode = (InitCode) code;				} else if (code instanceof ParserCode) {					parserCode = (ParserCode) code;				} else { // must be ScanCode 					scanCode = (ScanCode) code;				}
			} catch (Exception e) {
				// (!instanceof Code), so just skip it	
			}		}
	}	
	public void addImports (Vector imp) {
		if (imp == null)			return;
				for (int i=0; i < imp.size(); i++) {			imports.addElement(imp.elementAt(i));		}	}
	
		
		public void setChild (JLgenSpec childSpec) {		child = childSpec;
	}		// default action is to do nothing: as CUP does
	public void parseChain(String basePath) {}

	/**
	 * Combine the chain of inheritance into one CUP spec
	 */	public abstract CUPSpec coalesce();	
}
