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
package ppg.atoms;

import java.util.Vector;

public class SymbolList {
    public static final int TERMINAL = 0;
    public static final int NONTERMINAL = 1;

    private int variety;
    private String type;
    private Vector<String> symbols;

    public SymbolList(int which, String type, Vector<String> syms) {
        variety = which;
        this.type = type;
        symbols = syms;
    }

    public boolean dropSymbol(String gs) {
        for (int i = 0; i < symbols.size(); i++) {
            if (gs.equals(symbols.elementAt(i))) {
                symbols.removeElementAt(i);
                // assume we do not have duplicates
                return true;
            }
        }
        return false;
    }

    @Override
    public Object clone() {
        String newType = (type == null) ? null : type.toString();
        Vector<String> newSyms = new Vector<>();
        for (int i = 0; i < symbols.size(); i++) {
            newSyms.addElement(symbols.elementAt(i).toString());
        }
        return new SymbolList(variety, newType, newSyms);
    }

    @Override
    public String toString() {
        String result = "";

        if (symbols.size() > 0) {
            switch (variety) {
            case (TERMINAL):
                result = "terminal ";
                break;
            case (NONTERMINAL):
                result = "non terminal ";
                break;
            }

            if (type != null) result += type + " ";

            int size = symbols.size();
            for (int i = 0; i < size; i++) {
                result += symbols.elementAt(i);
                if (i < size - 1) result += ", ";
            }
            result += ";";
        }

        return result;
    }
}
