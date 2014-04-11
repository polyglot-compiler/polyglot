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

public class Precedence {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int NONASSOC = 2;

    private int type;
    private Vector<GrammarSymbol> symbols;

    public Precedence(int type, Vector<GrammarSymbol> syms) {
        this.type = type;
        symbols = syms;
    }

    @Override
    public Object clone() {
        Vector<GrammarSymbol> newSyms = new Vector<>();
        for (int i = 0; i < symbols.size(); i++) {
            newSyms.addElement((GrammarSymbol) symbols.elementAt(i).clone());
        }
        return new Precedence(type, newSyms);
    }

    @Override
    public String toString() {
        String result = "precedence ";
        switch (type) {
        case (LEFT):
            result += "left ";
            break;
        case (RIGHT):
            result += "right ";
            break;
        case (NONASSOC):
            result += "nonassoc ";
            break;
        }

        for (int i = 0; i < symbols.size(); i++) {
            result += symbols.elementAt(i);
            if (i < symbols.size() - 1) result += ", ";
        }
        return result + ";";
    }
}
