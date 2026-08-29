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
package polyglot.ext.jl8.ast;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ext.jl8.types.JL8LocalInstance;
import polyglot.types.Context;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

/**
 * Java 8 relaxes the rule that a local variable referred to from a nested class
 * body or lambda expression must be declared final: it is enough for the
 * variable to be effectively final (JLS 4.12.4). Since effective finality
 * depends on assignments elsewhere in the enclosing code declaration, this
 * extension only records the reference; JL8DefiniteAssignmentChecker decides
 * whether the variable is in fact effectively final.
 */
public class JL8LocalExt extends JL8Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Local n = (Local) node();
        Context c = tc.context();
        LocalInstance li = c.findLocal(n.name());

        if (!c.isLocal(li.name())) {
            // The variable is declared outside the code declaration that this
            // reference appears in, so the reference captures it.
            LocalInstance orig = li.orig();
            if (orig instanceof JL8LocalInstance) {
                ((JL8LocalInstance) orig).setCapturedAt(n.position());
            }
        }

        return n.localInstance(li).type(li.type());
    }
}
