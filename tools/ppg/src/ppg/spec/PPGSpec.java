package jltools.util.jlgen.spec;

import java.io.*;
import java.util.*;
import jltools.util.jlgen.*;
import jltools.util.jlgen.cmds.*;
import jltools.util.jlgen.lex.*;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.util.*;

public class JLgenSpec extends Spec
{
	private String include;
	private Vector commands;
	private Spec parent;
	private final String HEADER = "jlgen [spec]: ";
	
	/**
	 * JLgen spec
	 */
	public JLgenSpec(String incFile, Vector cmds)
	{		super();
		include = incFile;
		commands = cmds;
		parent = null;
	}		public JLgenSpec (String incFile, String pkg, Vector imp,
					  Vector codeParts, Vector syms,					  Vector precedence, String startSym, Vector cmds)
	{		super();		include = incFile;
		packageName = pkg;
		imports = imp;
		code = codeParts;
		symbols = syms;
		prec = precedence;
		start = startSym;
		commands = cmds;
		parent = null;
	}
	
	/**
	 * Parse the chain of inheritance via include files
	 */
	public void parseChain() {
		try {
			FileInputStream fileInput = new FileInputStream(include);
			File f = new File(include);
			String simpleName = f.getName();
			Lexer lex = new Lexer(fileInput, simpleName);
			Parser parser = new Parser(simpleName, lex);
			JLgen.DEBUG("parsing "+simpleName);
			parser.parse();
			parent = (Spec)parser.getProgramNode();
			fileInput.close();
		} catch (Exception e) {
			System.out.println(HEADER+"Exception: "+e.getMessage());
			return;
		}
		parent.setChild(this);		parent.parseChain();
	}		public CUPSpec coalesce() {		// parent cannot be null by definition
		CUPSpec combined = parent.coalesce();		
		// override package name
		combined.setPkgName(packageName);
		
		// add imported classes		combined.addImports(imports);
				// override action/parser/init/scan code		
				// add in (non)terminals
		combined.addSymbols(symbols);
				// override start symbol
		combined.setStart(start);		
		// combine this spec with the rest 
		// of the chain and return the result		// Order of processing:		// drop, transferL, override, extend, transferR, new
		Command cmd;
				// DROP
		DropCmd drop;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof DropCmd) {				drop = (DropCmd) cmd;
				if (drop.isProdDrop()) {
					// remove all productions that have NT as lhs					combined.dropProductions(drop.getProduction());
				} else { /* NT Drop */
					// remove nonterminal from list of symbols					combined.dropSymbol(drop.getNonterminal());
					// remove all productions that have NT as lhs					combined.dropAllProductions(drop.getNonterminal());
				}			}
		}		
		// OVERRIDE
		OverrideCmd override;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof OverrideCmd) {				override = (OverrideCmd) cmd;
				combined.dropAllProductions(override.getLHS());
				combined.addProductions(override.getProduction());			}
		}
		
		// EXTEND
		ExtendCmd extend;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof ExtendCmd) {				extend = (ExtendCmd) cmd;
				combined.addProductions(extend.getProduction());			}
		}
		
		// TRANSFER_L
		TransferCmd transfer;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof TransferCmd) {				transfer = (TransferCmd) cmd;
				// ???			}
		}
		
		// TRANSFER_R
		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof TransferCmd) {				transfer = (TransferCmd) cmd;
				// ???			}
		}		
		// NEW PRODUCTIONS		NewProdCmd newProd;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof NewProdCmd) {				newProd = (NewProdCmd) cmd;
				combined.addProductions(newProd.getProduction());			}
		}
				return combined;
	}
			/**
	 * Write out contents to a CodeWriter
	 */
	public void unparse(CodeWriter cw) throws ParserError {
		cw.begin(0);
		if (include != null) {
			cw.write(include+"\n");
		}
		if (commands != null) {
			for (int i=0; i < commands.size(); i++) {
				((Command)commands.elementAt(i)).unparse(cw);
			}
		}
		cw.end();
	}
}
