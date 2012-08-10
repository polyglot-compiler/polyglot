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
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>Catch</code> represents one half of a <code>try-catch</code>
 * statement.  Specifically, the second half.
 */
public class Catch_c extends Stmt_c implements Catch {
    protected Formal formal;
    protected Block body;

    public Catch_c(Position pos, Formal formal, Block body) {
        super(pos);
        assert (formal != null && body != null);
        this.formal = formal;
        this.body = body;
    }

    /** Get the catchType of the catch block. */
    @Override
    public Type catchType() {
        return formal.declType();
    }

    /** Get the formal of the catch block. */
    @Override
    public Formal formal() {
        return this.formal;
    }

    /** Set the formal of the catch block. */
    @Override
    public Catch formal(Formal formal) {
        Catch_c n = (Catch_c) copy();
        n.formal = formal;
        return n;
    }

    /** Get the body of the catch block. */
    @Override
    public Block body() {
        return this.body;
    }

    /** Set the body of the catch block. */
    @Override
    public Catch body(Block body) {
        Catch_c n = (Catch_c) copy();
        n.body = body;
        return n;
    }

    /** Reconstruct the catch block. */
    protected Catch_c reconstruct(Formal formal, Block body) {
        if (formal != this.formal || body != this.body) {
            Catch_c n = (Catch_c) copy();
            n.formal = formal;
            n.body = body;
            return n;
        }

        return this;
    }

    /** Visit the children of the catch block. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Formal formal = (Formal) visitChild(this.formal, v);
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(formal, body);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    /** Type check the catch block. */
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

    /** Write the catch block to an output file. */
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
