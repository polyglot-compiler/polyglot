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

import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An {@code ArrayAccess} is an immutable representation of an
 * access of an array member.
 */
public class ArrayAccess_c extends Expr_c implements ArrayAccess {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr array;
    protected Expr index;

//    @Deprecated
    public ArrayAccess_c(Position pos, Expr array, Expr index) {
        this(pos, array, index, null);
    }

    public ArrayAccess_c(Position pos, Expr array, Expr index, Ext ext) {
        super(pos, ext);
        assert (array != null && index != null);
        this.array = array;
        this.index = index;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    @Override
    public Expr array() {
        return this.array;
    }

    @Override
    public ArrayAccess array(Expr array) {
        return array(this, array);
    }

    protected <N extends ArrayAccess_c> N array(N n, Expr array) {
        if (n.array == array) return n;
        n = copyIfNeeded(n);
        n.array = array;
        return n;
    }

    @Override
    public Expr index() {
        return this.index;
    }

    @Override
    public ArrayAccess index(Expr index) {
        return index(this, index);
    }

    protected <N extends ArrayAccess_c> N index(N n, Expr index) {
        if (n.index == index) return n;
        n = copyIfNeeded(n);
        n.index = index;
        return n;
    }

    @Override
    public Flags flags() {
        return Flags.NONE;
    }

    /** Reconstruct the expression. */
    protected <N extends ArrayAccess_c> N reconstruct(N n, Expr array,
            Expr index) {
        n = array(n, array);
        n = index(n, index);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr array = visitChild(this.array, v);
        Expr index = visitChild(this.index, v);
        return reconstruct(this, array, index);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!array.type().isArray()) {
            throw new SemanticException("Subscript can only follow an array type.",
                                        position());
        }

        if (!ts.isImplicitCastValid(index.type(), ts.Int())) {
            throw new SemanticException("Array subscript must be an integer.",
                                        position());
        }

        return type(array.type().toArray().base());
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == index) {
            return ts.Int();
        }

        if (child == array) {
            return ts.arrayOf(this.type);
        }

        return child.type();
    }

    @Override
    public String toString() {
        return array + "[" + index + "]";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printSubExpr(array, w, tr);
        w.write("[");
        printBlock(index, w, tr);
        w.write("]");
    }

    @Override
    public Term firstChild() {
        return array;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(array, index, ENTRY);
        v.visitCFG(index, this, EXIT);
        return succs;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return CollectionUtil.list((Type) ts.OutOfBoundsException(),
                                   ts.NullPointerException());
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ArrayAccess(this.position, this.array, this.index);
    }
}
