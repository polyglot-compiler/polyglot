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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.ast.ProcedureCall;
import polyglot.ast.ProcedureCallOps;
import polyglot.ast.TypeNode;
import polyglot.types.ReferenceType;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public abstract class JL5ProcedureCallExt extends JL5Ext implements
        JL5ProcedureCall, ProcedureCallOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<TypeNode> typeArgs;

    @Override
    public ProcedureCall node() {
        return (ProcedureCall) super.node();
    }

    @Override
    public List<TypeNode> typeArgs() {
        return this.typeArgs;
    }

    @Override
    public ProcedureCall typeArgs(List<TypeNode> typeArgs) {
        if (this.typeArgs == typeArgs) {
            return this.node();
        }
        ProcedureCall n = (ProcedureCall) this.node().copy();
        JL5ProcedureCallExt ext = (JL5ProcedureCallExt) JL5Ext.ext(n);
        ext.typeArgs = typeArgs;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        JL5ProcedureCallExt ext = (JL5ProcedureCallExt) JL5Ext.ext(this.node());

        List<TypeNode> typeArgs = this.node().visitList(ext.typeArgs(), v);

        Node newN = superLang().visitChildren(this.node(), v);
        JL5ProcedureCallExt newext = (JL5ProcedureCallExt) JL5Ext.ext(newN);

        if (!CollectionUtil.equals(typeArgs, newext.typeArgs())) {
            // the type args changed! Let's update the node.
            if (newN == this.node()) {
                // we need to create a copy.
                newN = newN.copy();
                newext = (JL5ProcedureCallExt) JL5Ext.ext(newN);
            }
            else {
                // the call to super.visitChildren(v) already
                // created a copy of the node (and thus of its extension).
            }
            newext.typeArgs = typeArgs;
        }
        return newN;
    }

    protected List<ReferenceType> actualTypeArgs() {
        ProcedureCall n = this.node();
        JL5ProcedureCallExt ext = (JL5ProcedureCallExt) JL5Ext.ext(n);
        List<ReferenceType> actualTypeArgs =
                new ArrayList<ReferenceType>(ext.typeArgs().size());
        for (TypeNode tn : ext.typeArgs()) {
            actualTypeArgs.add((ReferenceType) tn.type());
        }
        return actualTypeArgs;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        ProcedureCall n = this.node();
        JL5ProcedureCallExt ext = (JL5ProcedureCallExt) JL5Ext.ext(n);

        if (ext.typeArgs() != null && !ext.typeArgs().isEmpty()) {
            w.write("<");
            Iterator<TypeNode> it = ext.typeArgs().iterator();
            while (it.hasNext()) {
                TypeNode tn = it.next();
                ((Node_c) n).print(tn, w, tr);
                if (it.hasNext()) {
                    w.write(",");
                    w.allowBreak(0, " ");
                }
            }
            w.write(">");
            w.allowBreak(0, " ");
        }
    }

    @Override
    public void printArgs(CodeWriter w, PrettyPrinter tr) {
        superLang().printArgs(this.node(), w, tr);
    }
}
