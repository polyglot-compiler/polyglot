package jltools.util.jlgen.spec;

import java.io.*;
import java.util.*;
import jltools.util.jlgen.*;
import jltools.util.jlgen.atoms.*;
import jltools.util.jlgen.cmds.*;
import jltools.util.jlgen.lex.*;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.util.*;

public class JLgenSpec extends Spec
{
	private String include;
	private Vector commands, code;
	private Spec parent;	
	/**
	 * JLgen spec
	 */	public JLgenSpec (String incFile, String pkg, Vector imp,
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
	public void parseChain (String basePath) {		File file = null;		String fullPath = "";
		try {			fullPath = basePath + System.getProperty("file.separator") + include;
			FileInputStream fileInput = new FileInputStream(fullPath);
			file = new File(fullPath);
			String simpleName = file.getName();
			Lexer lex = new Lexer(fileInput, simpleName);
			Parser parser = new Parser(simpleName, lex);
			JLgen.DEBUG("parsing "+simpleName);
			parser.parse();
			parent = (Spec)parser.getProgramNode();
			fileInput.close();		} catch (FileNotFoundException e) {			System.out.println(JLgen.HEADER+fullPath+" not found.");			System.exit(1);
		} catch (Exception e) {
			System.out.println(JLgen.HEADER+"Exception: "+e.getMessage());
			System.exit(1);
		}
		parent.setChild(this);		/*
		String parentDir = file.getPath();
		parent.parseChain(parentDir == null ? "" : parentDir);
		*/
		String parentDir = file.getParent();		parent.parseChain(parentDir == null ? "" : parentDir);
	}		public CUPSpec coalesce() {		// parent cannot be null by definition
		CUPSpec combined = parent.coalesce();		
		// work with a copy so we have the unmodified original to refer to		CUPSpec newSpec = (CUPSpec) combined.clone();		
		// override package name
		newSpec.setPkgName(packageName);
		
		// add imported classes		newSpec.addImports(imports);
				// override action/parser/init/scan code		newSpec.replaceCode(code);
				// add in (non)terminals
		newSpec.addSymbols(symbols);
				// override start symbol
		newSpec.setStart(start);		
		// combine this spec with the rest 
		// of the chain and return the result
		processTransferL(combined, newSpec);		processDrop(combined, newSpec);		processOverride(combined, newSpec);
		processTransferR(combined, newSpec);		processExtend(combined, newSpec);		processNew(combined, newSpec);		
		// clean the spec, remove nonterminals with no productions
		newSpec.removeEmptyProductions();
				return newSpec;
	}
	
	private void processDrop (CUPSpec combined, CUPSpec newSpec) {
		// DROP
		Command cmd;
		DropCmd drop;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof DropCmd) {				drop = (DropCmd) cmd;
				if (drop.isProdDrop()) {
					// remove all productions that have NT as lhs					newSpec.dropProductions(drop.getProduction());
				} else { /* symbol Drop */
					// remove nonterminal from list of symbols					newSpec.dropSymbol(drop.getSymbol());
					// remove all productions that have NT as lhs, if possible					newSpec.dropAllProductions(drop.getSymbol());
				}			}
		}	}	private void processOverride (CUPSpec combined, CUPSpec newSpec) {
		// OVERRIDE
		Command cmd;
		OverrideCmd override;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof OverrideCmd) {				override = (OverrideCmd) cmd;
				newSpec.dropProductions(override.getLHS());
				newSpec.addProductions(override.getProduction());			}
		}
	}		private void processExtend (CUPSpec combined, CUPSpec newSpec) {
		// EXTEND
		Command cmd;
		ExtendCmd extend;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof ExtendCmd) {				extend = (ExtendCmd) cmd;
				newSpec.addProductions(extend.getProduction());			}
		}
	}		private void processTransferL (CUPSpec combined, CUPSpec newSpec) {
		// TRANSFER_L
		Command cmd;
		TransferCmd transfer;
		Production prod;		Nonterminal source;
		Vector prodList;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof TransferCmd) {
				transfer = (TransferCmd) cmd;
				source = transfer.getSource();				prodList = transfer.getTransferList();
				
				// there must be at least one production by the grammar definition				prod = (Production) prodList.elementAt(0);				prod = (Production) prod.clone();
				for (int j=1; j < prodList.size(); j++) {					Production prodNew = (Production) prodList.elementAt(j);					prod.union( (Production) prodNew.clone() );	
					//prod.union( (Production) prodList.elementAt(j) );	
				}
								prod.setLHS(transfer.getSource());
				newSpec.dropProductions(prod);			}
		}
	}		private void processTransferR (CUPSpec combined, CUPSpec newSpec) {
		// TRANSFER_R
		Command cmd;
		TransferCmd transfer;		Production prod, prodTransfer;		Vector prodList;
		Nonterminal target;		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof TransferCmd) {
				transfer = (TransferCmd) cmd;
				prodList = transfer.getTransferList();				for (int j=0; j < prodList.size(); j++) {
					prod = (Production) prodList.elementAt(j);
					target = prod.getLHS();					// make sure we get the productions from the source!
					prod.setLHS(transfer.getSource());
					prodTransfer = combined.findProduction(prod);					// but set the LHS back to the actual target					// so it is added to the right nonterminal					prodTransfer.setLHS(target);
					newSpec.addProductions(prodTransfer);
					//newSpec.addProductions(prod);				}			}
		}	}		private void processNew (CUPSpec combined, CUPSpec newSpec) {
		// NEW PRODUCTIONS		NewProdCmd newProd;		Command cmd;
		for (int i=0; i < commands.size(); i++) {
			cmd = (Command) commands.elementAt(i);			if (cmd instanceof NewProdCmd) {				newProd = (NewProdCmd) cmd;
				newSpec.addProductions(newProd.getProduction());			}
		}
	}		/**
	 * Write out contents to a CodeWriter
	 */
	public void unparse (CodeWriter cw) {
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
