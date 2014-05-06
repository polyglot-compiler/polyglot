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
package polyglot.ext.jl5.ast;

import polyglot.ast.AmbReceiver;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5FieldExt extends JL5ExprExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Field n = (Field) superLang().typeCheck(this.node(), tc);
        if (n.fieldInstance() instanceof EnumInstance
                && !(n instanceof EnumConstant)) {
            // it's an enum, so replace this with the appropriate AST node for enum constants.
            JL5NodeFactory nf = (JL5NodeFactory) tc.nodeFactory();
            Field ec =
                    nf.EnumConstant(n.position(),
                                    n.target(),
                                    nf.Id(n.id().position(), n.name()));
            ec = (Field) ec.type(n.type());
            ec = ec.fieldInstance(n.fieldInstance());
            n = ec;
        }
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        Field n = (Field) this.node();
        w.begin(0);
        if (!n.isTargetImplicit()) {
            // explicit target.
            if (n.target() instanceof Expr) {
                n.printSubExpr((Expr) n.target(), w, tr);
            }
            else if (n.target() instanceof TypeNode
                    || n.target() instanceof AmbReceiver) {
                if (tr instanceof JL5Translator) {
                    JL5Translator jltr = (JL5Translator) tr;
                    jltr.printReceiver(n.target(), w);
                }
                else {
                    print(n.target(), w, tr);
                }
            }

            w.write(".");
            w.allowBreak(2, 3, "", 0);
        }
        tr.print(n, n.id(), w);
        w.end();
    }
}
