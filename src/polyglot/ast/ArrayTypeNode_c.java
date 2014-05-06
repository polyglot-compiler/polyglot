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

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * An {@code ArrayTypeNode} is a type node for a non-canonical
 * array type.
 */
public class ArrayTypeNode_c extends TypeNode_c implements ArrayTypeNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode base;

//    @Deprecated
    public ArrayTypeNode_c(Position pos, TypeNode base) {
        this(pos, base, null);
    }

    public ArrayTypeNode_c(Position pos, TypeNode base, Ext ext) {
        super(pos, ext);
        assert (base != null);
        this.base = base;
    }

    @Override
    public boolean isDisambiguated() {
        return false;
    }

    @Override
    public TypeNode base() {
        return base;
    }

    @Override
    public ArrayTypeNode base(TypeNode base) {
        return base(this, base);
    }

    protected <N extends ArrayTypeNode_c> N base(N n, TypeNode base) {
        if (n.base == base) return n;
        n = copyIfNeeded(n);
        n.base = base;
        return n;
    }

    protected <N extends ArrayTypeNode_c> N reconstruct(N n, TypeNode base) {
        n = base(n, base);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode base = visitChild(this.base, v);
        return reconstruct(this, base);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();
        return type(ts.arrayOf(position(), base.type()));
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        TypeSystem ts = ar.typeSystem();
        NodeFactory nf = ar.nodeFactory();

        if (!base.isDisambiguated()) {
            return this;
        }

        Type baseType = base.type();

        if (!baseType.isCanonical()) {
            return this;
        }

        return nf.CanonicalTypeNode(position(),
                                    ts.arrayOf(position(), baseType));
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot type check ambiguous node "
                                                + this + ".");
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot exception check ambiguous node "
                                                + this + ".");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        print(base, w, tr);
        w.write("[]");
    }

    @Override
    public String toString() {
        return base.toString() + "[]";
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ArrayTypeNode(this.position, this.base);
    }
}
