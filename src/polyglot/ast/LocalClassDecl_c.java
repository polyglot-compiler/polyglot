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

package polyglot.ast;

import java.util.List;

import polyglot.types.Context;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A local class declaration statement.  The node is just a wrapper around
 * a class declaration.
 */
public class LocalClassDecl_c extends Stmt_c implements LocalClassDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected ClassDecl decl;

//    @Deprecated
    public LocalClassDecl_c(Position pos, ClassDecl decl) {
        this(pos, decl, null);
    }

    public LocalClassDecl_c(Position pos, ClassDecl decl, Ext ext) {
        super(pos, ext);
        assert (decl != null);
        this.decl = decl;
    }

    @Override
    public ClassDecl decl() {
        return this.decl;
    }

    @Override
    public LocalClassDecl decl(ClassDecl decl) {
        return decl(this, decl);
    }

    protected <N extends LocalClassDecl_c> N decl(N n, ClassDecl decl) {
        if (n.decl == decl) return n;
        n = copyIfNeeded(n);
        n.decl = decl;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends LocalClassDecl_c> N reconstruct(N n, ClassDecl decl) {
        n = decl(n, decl);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        ClassDecl decl = visitChild(this.decl, v);
        return reconstruct(this, decl);
    }

    @Override
    public Term firstChild() {
        return decl();
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(decl(), this, EXIT);
        return succs;
    }

    @Override
    public void addDecls(Context c) {
        // We should now be back in the scope of the enclosing block.
        // Add the type, if any.
        if (decl.type() == null) return;

        if (!decl.type().toClass().isLocal())
            throw new InternalCompilerError("Non-local " + decl.type()
                    + " found in method body.");
        c.addNamed(decl.type().toClass());
    }

    @Override
    public String toString() {
        return decl.toString();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printBlock(decl, w, tr);
        w.write(";");
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.LocalClassDecl(this.position, this.decl);
    }

}
