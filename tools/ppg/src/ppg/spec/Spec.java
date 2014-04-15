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

import java.util.*;
import ppg.*;
import ppg.atoms.Precedence;
import ppg.atoms.SymbolList;
import ppg.code.*;
import ppg.parse.*;

public abstract class Spec implements Unparse {
    protected String packageName;
    protected Vector<String> imports;
    protected Vector<SymbolList> symbols;
    protected Vector<Precedence> prec;
    protected InitCode initCode;
    protected ActionCode actionCode;
    protected ParserCode parserCode;
    protected ScanCode scanCode;
    protected PPGSpec child;

    public Spec() {
        initCode = null;
        actionCode = null;
        parserCode = null;
        scanCode = null;
        child = null;
    }

    public void setPkgName(String pkgName) {
        if (pkgName != null) packageName = pkgName;
    }

    public void replaceCode(Vector<Code> codeParts) {
        if (codeParts == null) return;

        Code code = null;
        for (int i = 0; i < codeParts.size(); i++) {
            try {
                code = codeParts.elementAt(i);
                if (code instanceof ActionCode) {
                    actionCode = (ActionCode) code.clone();
                }
                else if (code instanceof InitCode) {
                    initCode = (InitCode) code.clone();
                }
                else if (code instanceof ParserCode) {
                    parserCode = (ParserCode) code.clone();
                }
                else { // must be ScanCode
                    if (code != null) scanCode = (ScanCode) code.clone();
                }
            }
            catch (Exception e) {
                System.err.println(PPG.HEADER
                        + " Spec::replaceCode(): not a code segment "
                        + "found in code Vector: "
                        + ((code == null) ? "null" : code.toString()));
                System.exit(1);
            }
        }
    }

    public void addImports(Vector<String> imp) {
        if (imp == null) return;

        for (int i = 0; i < imp.size(); i++) {
            imports.addElement(imp.elementAt(i));
        }
    }

    public void setChild(PPGSpec childSpec) {
        child = childSpec;
    }

    // default action is to do nothing: as CUP does
    public void parseChain(String basePath) {
    }

    /**
     * Combine the chain of inheritance into one CUP spec
     */
    public abstract CUPSpec coalesce() throws PPGError;

}
