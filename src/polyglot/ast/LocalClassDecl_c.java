/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
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
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A local class declaration statement.  The node is just a wrapper around
 * a class declaration.
 */
public class LocalClassDecl_c extends Stmt_c implements LocalClassDecl {
    protected ClassDecl decl;

    public LocalClassDecl_c(Position pos, ClassDecl decl) {
        super(pos);
        assert (decl != null);
        this.decl = decl;
    }

    /** Get the class declaration. */
    @Override
    public ClassDecl decl() {
        return this.decl;
    }

    /** Set the class declaration. */
    @Override
    public LocalClassDecl decl(ClassDecl decl) {
        LocalClassDecl_c n = (LocalClassDecl_c) copy();
        n.decl = decl;
        return n;
    }

    /** Reconstruct the statement. */
    protected LocalClassDecl_c reconstruct(ClassDecl decl) {
        if (decl != this.decl) {
            LocalClassDecl_c n = (LocalClassDecl_c) copy();
            n.decl = decl;
            return n;
        }

        return this;
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    @Override
    public Term firstChild() {
        return decl();
    }

    /**
     * Visit this term in evaluation order.
     */
    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(decl(), this, EXIT);
        return succs;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        ClassDecl decl = (ClassDecl) visitChild(this.decl, v);
        return reconstruct(decl);
    }

    @Override
    public void addDecls(Context c) {
        // We should now be back in the scope of the enclosing block.
        // Add the type.
        if (!decl.type().toClass().isLocal())
            throw new InternalCompilerError("Non-local " + decl.type()
                    + " found in method body.");
        c.addNamed(decl.type().toClass());
    }

    @Override
    public String toString() {
        return decl.toString();
    }

    /** Write the statement to an output file. */
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
