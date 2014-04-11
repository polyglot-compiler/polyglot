/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package ppg.spec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import ppg.PPG;
import ppg.PPGError;
import ppg.atoms.GrammarPart;
import ppg.atoms.Nonterminal;
import ppg.atoms.Precedence;
import ppg.atoms.Production;
import ppg.atoms.SemanticAction;
import ppg.atoms.SymbolList;
import ppg.cmds.Command;
import ppg.cmds.DropCmd;
import ppg.cmds.ExtendCmd;
import ppg.cmds.NewProdCmd;
import ppg.cmds.OverrideCmd;
import ppg.cmds.TransferCmd;
import ppg.code.Code;
import ppg.code.ScanCode;
import ppg.lex.Lexer;
import ppg.parse.Parser;
import ppg.util.CodeWriter;

public class PPGSpec extends Spec {
    private String include;
    private Vector<Command> commands;
    private Vector<Code> code;
    private Spec parent;
    private Vector<String> startSyms;

    /**
     * PPG spec
     *
    public PPGSpec (String incFile, String pkg, Vector imp,
    				  Vector codeParts, Vector syms,
    				  Vector precedence, String startSym, Vector cmds)
    {
    	super();
    	include = incFile;
    	packageName = pkg;
    	imports = imp;
    	code = codeParts;
    	symbols = syms;
    	prec = precedence;
    	start = startSym; startSyms = null;
    	commands = cmds;
    	parent = null;
    }
    */

    public PPGSpec(String incFile, String pkg, Vector<String> imp,
            Vector<Code> codeParts, Vector<SymbolList> syms,
            Vector<Precedence> precedence, Vector<String> startList,
            Vector<Command> cmds) {
        super();
        include = incFile;
        packageName = pkg;
        imports = imp;
        code = codeParts;
        symbols = syms;
        prec = precedence;
        startSyms = startList;
        commands = cmds;
        parent = null;
    }

    public boolean isMultiStartSymbol() {
        return (startSyms.size() > 1);
    }

    /*
    	Token curr_sym;
    	
    	// for each [start_sym, method, token] create code:
    	method () {
    		curr_sym = token;
    		parse();
    	}

    	// ^^^^ parse code ^^^^ 
    
    	// in front of scanner, add code:
    	// return the "fake" symbol only once after it's set
    	if (curr_sym != null)
    		Token result = curr_sym;
    		curr_sym = null;
    		return result;
    	}
    	
    	// ^^^^ scan code ^^^^
    
    	// code added to grammar:
    	start with new_unique_start_symbol;
    	
    	new_unique_start_symbol ::=
    		token_1 start_sym_1:s {: RESULT = s; :}
    	|	token_2 start_sym_2:s {: RESULT = s; :}
    	|	...
    	;
    
    	// ^^^^ grammar ^^^^
    */

    public void patchMultiStartSymbols(CUPSpec cupSpec) {
        if (!isMultiStartSymbol()) {
            cupSpec.setStart(startSyms.elementAt(0));
            return;
        }

        // Parse Code
        String parseCode = "";
        // should it be dynamically generated?
        String currSymbolName = "ppg_curr_sym";
        parseCode += "Symbol " + currSymbolName + ";\n\n";

        // Generate token names
        Vector<String> tokens = new Vector<>();
        for (int i = 0; i < startSyms.size(); i += 2) {
            tokens.addElement("JLGEN_TOKEN_" + String.valueOf(i / 2));
        }

        String startSym, method, token;
        for (int i = 0; i < startSyms.size(); i += 2) {
            startSym = startSyms.elementAt(i);
            method = startSyms.elementAt(i + 1);
            token = tokens.elementAt(i / 2); //startSyms.elementAt(i+2);
            parseCode +=
                    "@Override\n" + "public Symbol " + method
                            + "() throws Exception {\n" + "\t" + currSymbolName
                            + " = " + "getSymbolFactory().newSymbol(\""
                            + method + "\", " + PPG.SYMBOL_CLASS_NAME + "."
                            + token + ")" + ";\n" + "\t"
                            + "return parse();\n}\n\n";
        }
        // append parseCode to the actual parse code
        cupSpec.parserCode.append(parseCode);

        /************************************/

        // Scan code
        String scanCodeAdd =
                "\n// scan code generated by PPG\n" + "if (" + currSymbolName
                        + "!= null) {\n" + "\tSymbol result = "
                        + currSymbolName + ";\n" + "\t" + currSymbolName
                        + " = null" + ";\n" + "\treturn result;\n" + "}\n"
                        + "// end scan code generated by PPG\n\n";
        // prepend scanCode before the actual scan code
        if (cupSpec.scanCode != null)
            cupSpec.scanCode.prepend(scanCodeAdd);
        else cupSpec.scanCode = new ScanCode(scanCodeAdd);

        /************************************/

        // create a new start symbol
        String newStartSym = "multi_start_symbool";

        // set start symbol
        cupSpec.setStart(newStartSym);
        Nonterminal startNT = new Nonterminal(newStartSym, null);
        Vector<String> newSymbols = new Vector<>();
        newSymbols.addElement(newStartSym);

        // add start symbol to the grammar
        SymbolList sl =
                new SymbolList(SymbolList.NONTERMINAL, null, newSymbols);
        Vector<SymbolList> addedSymbols = new Vector<>();
        addedSymbols.addElement(sl);
        cupSpec.addSymbols(addedSymbols);

        // add token declaration to the grammar
        SymbolList tokenList =
                new SymbolList(SymbolList.TERMINAL, "Symbol", tokens);
        Vector<SymbolList> addedTokens = new Vector<>();
        addedTokens.addElement(tokenList);
        cupSpec.addSymbols(addedTokens);

        Vector<Vector<GrammarPart>> rhs = new Vector<>();

        //String grammarPatch = newStartSym + " ::=\n";
        for (int i = 0; i < startSyms.size(); i += 2) {
            Vector<GrammarPart> rhsPart = new Vector<>();
            startSym = startSyms.elementAt(i);
            token = tokens.elementAt(i / 2); //startSyms.elementAt(i+2); 
            //if (i > 0) grammarPatch += "|";
            //grammarPatch += "\t"+token+" "+startSym+":s {: RESULT = s; :}\n";
            // add new symbols into vector
            rhsPart.addElement(new Nonterminal(token, null));
            rhsPart.addElement(new Nonterminal(startSym, "s"));
            rhsPart.addElement(new SemanticAction("RESULT = s;"));
            rhs.addElement(rhsPart);
        }
        //grammarPatch += ";\n";

        // patch the grammar
        Production p = new Production(startNT, rhs);
        cupSpec.addProductions(p);
    }

    /**
     * Parse the chain of inheritance via include files
     */
    @Override
    public void parseChain(String basePath) {
        InputStream is;
        File file = null;
        String simpleName = include;
        try {
            // first look on the classpath.
            is = ClassLoader.getSystemResourceAsStream(include);
            if (is != null) {
                PPG.DEBUG("found " + include + " as a resource");
            }
            else {
                // nothing was found on the class path. Try the basePath...
                String fullPath =
                        ((basePath == "") ? "" : basePath
                                + System.getProperty("file.separator"))
                                + include;
                PPG.DEBUG("looking for " + fullPath + " as a file");
                file = new File(fullPath);
                is = new FileInputStream(file);
                simpleName = file.getName();
            }

            try (InputStreamReader reader = new InputStreamReader(is)) {
                Lexer lex = new Lexer(reader, simpleName);
                Parser parser = new Parser(simpleName, lex);

                PPG.DEBUG("parsing " + simpleName);
                parser.parse();
            }
            parent = (Spec) Parser.getProgramNode();
            is.close();

        }
        catch (FileNotFoundException e) {
            System.err.println(PPG.HEADER + simpleName + " not found.");
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println(PPG.HEADER + "Exception: " + e.getMessage());
            System.exit(1);
        }
        parent.setChild(this);

        String parentDir = null;
        if (file != null) {
            parentDir = file.getParent();
        }
        parent.parseChain(parentDir == null ? "" : parentDir);
    }

    @Override
    public CUPSpec coalesce() throws PPGError {
        // parent cannot be null by definition
        CUPSpec combined = parent.coalesce();

        // work with a copy so we have the unmodified original to refer to
        CUPSpec newSpec = (CUPSpec) combined.clone();

        // override package name
        newSpec.setPkgName(packageName);

        // add imported classes
        newSpec.addImports(imports);

        /* override precedence, using these rules:
         *
         * precedence list null: delete precedence list of parent
         * precedence list of length 0: leave precedence of parent
         * precedence list of length >0: override with current list
         */
        //TODO: test precedence inheritance/overriding/ignoring
        if (prec == null) {
            newSpec.prec.removeAllElements();
        }
        else if (prec.size() == 0) {
            // do nothing to parent's precedence list
        }
        else {
            // override with current
            newSpec.prec.removeAllElements();
            newSpec.prec.addAll(prec);
        }

        // override action/parser/init/scan code
        newSpec.replaceCode(code);

        // add in (non)terminals
        newSpec.addSymbols(symbols);

        // override start symbol(s), patch grammar (if multi-start-symbol)
        if (child == null) patchMultiStartSymbols(newSpec);

        // combine this spec with the rest 
        // of the chain and return the result
        processTransferL(combined, newSpec);
        processDrop(combined, newSpec);
        processOverride(combined, newSpec);
        processTransferR(combined, newSpec);
        processExtend(combined, newSpec);
        processNew(combined, newSpec);

        // clean the spec, remove nonterminals with no productions
        newSpec.removeEmptyProductions();

        return newSpec;
    }

    private void processDrop(CUPSpec combined, CUPSpec newSpec) {
        // DROP
        Command cmd;
        DropCmd drop;
        for (int i = 0; i < commands.size(); i++) {
            cmd = commands.elementAt(i);
            if (cmd instanceof DropCmd) {
                drop = (DropCmd) cmd;
                if (drop.isProdDrop()) {
                    // remove all productions that have NT as lhs
                    newSpec.dropProductions(drop.getProduction());
                }
                else { /* symbol Drop */
                    Vector<String> symbols = drop.getSymbols();
                    String sym;
                    for (int j = 0; j < symbols.size(); j++) {
                        sym = symbols.elementAt(j);
                        // remove nonterminals from list of symbols
                        newSpec.dropSymbol(sym);
                        // remove all productions that have NT as lhs, if possible
                        newSpec.dropAllProductions(sym);
                    }
                }
            }
        }
    }

    private void processOverride(CUPSpec combined, CUPSpec newSpec) {
        // OVERRIDE
        Command cmd;
        OverrideCmd override;
        for (int i = 0; i < commands.size(); i++) {
            cmd = commands.elementAt(i);
            if (cmd instanceof OverrideCmd) {
                override = (OverrideCmd) cmd;
                newSpec.dropProductions(override.getLHS());
                newSpec.addProductions(override.getProduction());
            }
        }
    }

    private void processExtend(CUPSpec combined, CUPSpec newSpec) {
        // EXTEND
        Command cmd;
        ExtendCmd extend;
        for (int i = 0; i < commands.size(); i++) {
            cmd = commands.elementAt(i);
            if (cmd instanceof ExtendCmd) {
                extend = (ExtendCmd) cmd;
                newSpec.addProductions(extend.getProduction());
            }
        }
    }

    private void processTransferL(CUPSpec combined, CUPSpec newSpec) {
        // TRANSFER_L
        for (int i = 0; i < commands.size(); i++) {
            Command cmd = commands.elementAt(i);
            if (cmd instanceof TransferCmd) {
                TransferCmd transfer = (TransferCmd) cmd;
                Vector<Production> prodList = transfer.getTransferList();

                // there must be at least one production by the grammar definition
                Production prod = prodList.elementAt(0);
                prod = (Production) prod.clone();
                for (int j = 1; j < prodList.size(); j++) {
                    Production prodNew = prodList.elementAt(j);
                    prod.union((Production) prodNew.clone());
                    //prod.union( (Production) prodList.elementAt(j) );	
                }

                prod.setLHS(transfer.getSource());
                newSpec.dropProductions(prod);
            }
        }
    }

    private void processTransferR(CUPSpec combined, CUPSpec newSpec) {
        // TRANSFER_R
        for (int i = 0; i < commands.size(); i++) {
            Command cmd = commands.elementAt(i);
            if (cmd instanceof TransferCmd) {
                TransferCmd transfer = (TransferCmd) cmd;
                Vector<Production> prodList = transfer.getTransferList();
                for (int j = 0; j < prodList.size(); j++) {
                    Production prod = prodList.elementAt(j);
                    Nonterminal target = prod.getLHS();
                    // make sure we get the productions from the source!
                    prod.setLHS(transfer.getSource());
                    Production prodTransfer = combined.findProduction(prod);
                    // but set the LHS back to the actual target
                    // so it is added to the right nonterminal
                    prodTransfer.setLHS(target);
                    newSpec.addProductions(prodTransfer);
                    //newSpec.addProductions(prod);
                }
            }
        }
    }

    private void processNew(CUPSpec combined, CUPSpec newSpec) {
        // NEW PRODUCTIONS
        NewProdCmd newProd;
        Command cmd;
        for (int i = 0; i < commands.size(); i++) {
            cmd = commands.elementAt(i);
            if (cmd instanceof NewProdCmd) {
                newProd = (NewProdCmd) cmd;
                newSpec.addProductions(newProd.getProduction());
            }
        }
    }

    /**
     * Write out contents to a CodeWriter
     */
    @Override
    public void unparse(CodeWriter cw) {
        cw.begin(0);
        if (include != null) {
            cw.write(include + "\n");
        }
        if (commands != null) {
            for (int i = 0; i < commands.size(); i++) {
                commands.elementAt(i).unparse(cw);
            }
        }
        cw.end();
    }
}
