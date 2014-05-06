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
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code Catch} represents one half of a {@code try-catch}
 * statement.  Specifically, the second half.
 */
public class Catch_c extends Stmt_c implements Catch {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Formal formal;
    protected Block body;

//    @Deprecated
    public Catch_c(Position pos, Formal formal, Block body) {
        this(pos, formal, body, null);
    }

    public Catch_c(Position pos, Formal formal, Block body, Ext ext) {
        super(pos, ext);
        assert (formal != null && body != null);
        this.formal = formal;
        this.body = body;
    }

    @Override
    public Type catchType() {
        return formal.declType();
    }

    @Override
    public Formal formal() {
        return this.formal;
    }

    @Override
    public Catch formal(Formal formal) {
        return formal(this, formal);
    }

    protected <N extends Catch_c> N formal(N n, Formal formal) {
        if (n.formal == formal) return n;
        n = copyIfNeeded(n);
        n.formal = formal;
        return n;
    }

    @Override
    public Block body() {
        return this.body;
    }

    @Override
    public Catch body(Block body) {
        return body(this, body);
    }

    protected <N extends Catch_c> N body(N n, Block body) {
        if (n.body == body) return n;
        n = copyIfNeeded(n);
        n.body = body;
        return n;
    }

    /** Reconstruct the catch block. */
    protected <N extends Catch_c> N reconstruct(N n, Formal formal, Block body) {
        n = formal(n, formal);
        n = body(n, body);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Formal formal = visitChild(this.formal, v);
        Block body = visitChild(this.body, v);
        return reconstruct(this, formal, body);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!catchType().isThrowable()) {
            throw new SemanticException("Can only throw subclasses of \""
                    + ts.Throwable() + "\".", formal.position());

        }

        return this;
    }

    @Override
    public String toString() {
        return "catch (" + formal + ") " + body;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("catch (");
        printBlock(formal, w, tr);
        w.write(")");
        printSubStmt(body, w, tr);
    }

    @Override
    public Term firstChild() {
        return formal;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(formal, body, ENTRY);
        v.visitCFG(body, this, EXIT);
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Catch(this.position, this.formal, this.body);
    }

}
