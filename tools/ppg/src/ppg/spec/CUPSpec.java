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

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

import ppg.PPG;
import ppg.atoms.GrammarPart;
import ppg.atoms.Nonterminal;
import ppg.atoms.Precedence;
import ppg.atoms.Production;
import ppg.atoms.SymbolList;
import ppg.code.Code;
import ppg.util.CodeWriter;

public class CUPSpec extends Spec {
    private Vector<Production> productions;
    // maps nonterminal to its index in the vector of productions
    private Hashtable<String, Integer> ntProds;
    private String start;
    private final int NT_NOT_FOUND = -1;

    public CUPSpec(
            String pkg,
            Vector<String> imp,
            Vector<Code> codeParts,
            Vector<SymbolList> syms,
            Vector<Precedence> precedence,
            String startSym,
            Vector<Production> prods) {
        super();
        packageName = pkg;
        imports = imp;
        replaceCode(codeParts);
        symbols = syms;
        prec = precedence;
        start = startSym;
        productions = prods;
        ntProds = new Hashtable<>();
        hashNonterminals();
    }

    public void setStart(String startSym) {
        if (startSym != null) start = startSym;
    }

    private void hashNonterminals() {
        ntProds.clear();
        if (productions == null) return;

        Production prod;
        for (int i = 0; i < productions.size(); i++) {
            prod = productions.elementAt(i);
            ntProds.put(prod.getLHS().getName(), new Integer(i));
        }
    }

    @Override
    public CUPSpec coalesce() {
        // cannot have a parent by definition
        return this;
    }

    /**
     * Provides a copy of the production that was present in the original
     * grammar, but is equal (minus semantic actions) to the given production set.
     * Thus, we transfer the semantic actions without having to re-specify them.
     */
    public Production findProduction(Production p) {
        // find the nonterminal which would contain this production
        Nonterminal nt = p.getLHS();
        int pos = errorNotFound(findNonterminal(nt), nt);
        Production sourceProd = productions.elementAt(pos);

        Vector<Vector<GrammarPart>> sourceRHSList = sourceProd.getRHS();

        Vector<Vector<GrammarPart>> rhs = p.getRHS();
        Production result = new Production(nt, new Vector<Vector<GrammarPart>>());

        for (int i = 0; i < rhs.size(); i++) {
            Vector<GrammarPart> toMatch = rhs.elementAt(i);
            for (int j = 0; j < sourceRHSList.size(); j++) {
                Vector<GrammarPart> source = sourceRHSList.elementAt(j);
                if (Production.isSameProduction(toMatch, source)) {
                    Vector<GrammarPart> clone = new Vector<>();
                    for (int k = 0; k < source.size(); k++) {
                        clone.addElement((GrammarPart) source.elementAt(k).clone());
                    }
                    // result.addToRHS((Vector) source.clone());
                    result.addToRHS(clone);
                    break;
                }
            }
        }

        return result;
    }

    public void removeEmptyProductions() {
        Production prod;
        for (int i = 0; i < productions.size(); i++) {
            prod = productions.elementAt(i);
            if (prod.getRHS().size() == 0) {
                productions.removeElementAt(i);
                i--;
            }
        }
    }

    @Override
    public Object clone() {
        String newPkgName = (packageName == null) ? null : packageName.toString();
        /*******************/
        Vector<String> newImports = new Vector<>();
        for (int i = 0; i < imports.size(); i++) {
            newImports.addElement(imports.elementAt(i).toString());
        }
        /*******************/
        Vector<Code> newCode = new Vector<>();
        if (actionCode != null) newCode.addElement(actionCode);
        if (initCode != null) newCode.addElement(initCode);
        if (parserCode != null) newCode.addElement(parserCode);
        if (scanCode != null) newCode.addElement(scanCode);
        /*for (int i=0; i < code.size(); i++) {
        	newCode.addElement( ((Code) code.elementAt(i)).clone());
        }*/
        /*******************/
        Vector<SymbolList> newSymbols = new Vector<>();
        for (int i = 0; i < symbols.size(); i++) {
            newSymbols.addElement((SymbolList) symbols.elementAt(i).clone());
        }
        /*******************/
        Vector<Precedence> newPrec = new Vector<>();
        for (int i = 0; i < prec.size(); i++) {
            newPrec.addElement((Precedence) prec.elementAt(i).clone());
        }
        /*******************/
        String newStart = (start == null) ? null : start.toString();
        /*******************/
        Vector<Production> newProductions = new Vector<>();
        for (int i = 0; i < productions.size(); i++) {
            newProductions.addElement((Production) productions.elementAt(i).clone());
        }

        return new CUPSpec(
                newPkgName, newImports, newCode, newSymbols, newPrec, newStart, newProductions);

        /*
        return new CUPSpec(newPkgName,
        				   (Vector) imports.clone(),
        				   (Vector) code.clone(),
        				   (Vector) symbols.clone(),
        				   (Vector) prec.clone(),
        				   newStart,
        				   (Vector) productions.clone());
        */
    }

    public void addSymbols(Vector<SymbolList> syms) {
        if (syms == null) return;

        for (int i = 0; i < syms.size(); i++) {
            symbols.addElement(syms.elementAt(i));
        }
    }

    public void dropSymbol(String gs) {
        boolean dropped = false;
        for (int i = 0; i < symbols.size(); i++) {
            SymbolList list = symbols.elementAt(i);
            dropped = dropped || list.dropSymbol(gs);
        }
        // TODO: error if symbol being dropped was not found
        /*
        if (!dropped)
        	throw new PPGError("file", -1, "symbol "+gs+" not found.");
        */
    }

    public void dropProductions(Production p) {
        Nonterminal nt = p.getLHS();
        int pos = errorNotFound(findNonterminal(nt), nt);
        // should be a valid index from which we can drop productions
        Production prod = productions.elementAt(pos);
        prod.drop(p);
    }

    public void dropProductions(Nonterminal nt) {
        int pos = errorNotFound(findNonterminal(nt), nt);
        // should be a valid index from which we can drop productions
        Production prod = productions.elementAt(pos);
        prod.drop((Production) prod.clone());
    }

    public void dropAllProductions(String nt) {
        int pos = findNonterminal(nt);
        // a terminal will not be in the hash
        if (pos == NT_NOT_FOUND) return;
        // remove the whole lhs ::= rhs entry from the list of productions
        productions.removeElementAt(pos);
        // now we need to rehash since positions changed
        hashNonterminals();
    }

    public void addProductions(Production p) {
        Nonterminal nt = p.getLHS();
        int pos = findNonterminal(nt);
        if (pos == NT_NOT_FOUND) {
            // add a hash mapping for this entry
            ntProds.put(nt.getName(), new Integer(productions.size()));
            // just append to our list
            productions.addElement(p);
        } else {
            // attach to specific nonterminal in our list of productions
            Production prod = productions.elementAt(pos);
            prod.add(p);
            // productions.setElementAt(prod, pos);
        }
    }

    /**
     * Returns int which is the position of the nonterminal in the production
     * list, or exits if it is not found
     */
    private int findNonterminal(Nonterminal nt) {
        return findNonterminal(nt.getName());
    }

    private int findNonterminal(String nt) {
        Integer pos = ntProds.get(nt);
        if (pos == null) return NT_NOT_FOUND;
        else return pos.intValue();
    }

    private int errorNotFound(int i, Nonterminal nt) {
        if (i == NT_NOT_FOUND) {
            // index not found, hence we have no such terminal
            System.err.println(PPG.HEADER + "nonterminal " + nt + " not found.");
            System.exit(1);
        }
        return i;
    }

    @Override
    public void unparse(CodeWriter cw) {
        cw.begin(0);
        if (packageName != null) {
            cw.write("package " + packageName + ";");
            cw.newline();
            cw.newline();
        }

        // import
        for (int i = 0; i < imports.size(); i++) {
            cw.write("import " + imports.elementAt(i) + ";");
            cw.newline();
        }
        if (imports.size() > 0) cw.newline();

        // code
        if (actionCode != null) cw.write(actionCode.toString());
        if (initCode != null) cw.write(initCode.toString());
        if (parserCode != null) cw.write(parserCode.toString());
        if (scanCode != null) cw.write(scanCode.toString());
        cw.newline();

        // symbols
        for (int i = 0; i < symbols.size(); i++) {
            cw.write(symbols.elementAt(i).toString());
            cw.newline();
        }
        cw.newline();

        // precedence
        for (int i = 0; i < prec.size(); i++) {
            cw.write(prec.elementAt(i).toString());
            cw.newline();
        }
        cw.newline();

        // start
        if (start != null) {
            cw.write("start with " + start + ";");
            cw.newline();
            cw.newline();
        }

        // productions
        for (int i = 0; i < productions.size(); i++) {
            productions.elementAt(i).unparse(cw);
        }
        cw.newline();
        cw.end();

        // Write out to stdout in a naive manner
        /*
        try {
        	export(System.out);
        } catch (Exception e) {
        	System.err.println(HEADER+"Exception: "+e.getMessage());
        	return;
        }
        */
    }

    /**
     * Write out the CUP specification to the stream
     */
    public void export(PrintStream out) throws Exception {
        // package
        out.println("package " + packageName + ";");
        out.println();

        // import
        for (int i = 0; i < imports.size(); i++)
            out.println("import " + imports.elementAt(i) + ";");
        out.println();

        // code
        /*
        for (int i=0; i < code.size(); i++)
        	out.println( ((Code) code.elementAt(i)).toString() );
        */
        if (actionCode != null) out.println(actionCode.toString());
        if (initCode != null) out.println(initCode.toString());
        if (parserCode != null) out.println(parserCode.toString());
        if (scanCode != null) out.println(scanCode.toString());
        out.println();

        // symbols
        for (int i = 0; i < symbols.size(); i++) out.println(symbols.elementAt(i).toString());
        out.println();

        // precedence
        for (int i = 0; i < prec.size(); i++) out.println(prec.elementAt(i).toString());
        out.println();

        // start
        out.println("start with " + start + ";");
        out.println();

        // productions
        for (int i = 0; i < productions.size(); i++)
            out.println(productions.elementAt(i).toString());
        out.println();

        out.flush();
        out.close();
    }
}
