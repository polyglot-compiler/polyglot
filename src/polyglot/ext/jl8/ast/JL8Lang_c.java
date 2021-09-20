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

import polyglot.ast.*;
import polyglot.ext.jl7.ast.J7Lang_c;
import polyglot.util.InternalCompilerError;

public class JL8Lang_c extends J7Lang_c implements JL8Lang {
    public static final JL8Lang_c instance = new JL8Lang_c();

    public static JL8Lang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof JL8Lang) return (JL8Lang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected JL8Lang_c() {
    }

    protected static JL8Ext jl8Ext(Node n) {
        return JL8Ext.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return jl8Ext(n);
    }

    // TODO:  Implement dispatch methods for new AST operations.
    // TODO:  Override *Ops methods for AST nodes with new extension nodes.
}
