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

import java.util.Collections;
import java.util.List;

import polyglot.types.ArrayType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code NewArray} represents a new array expression such as
 * {@code new File[8][] { null }}.  It consists of an element type (e.g.,
 * {@code File}), a list of dimension expressions (e.g., 8), 0 or more
 * additional dimensions (e.g., 1 for {@code []}), and an array initializer.
 * The dimensions of the array initializer must equal the number of additional
 * {@code []} dimensions.
 */
public class NewArray_c extends Expr_c implements NewArray {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode baseType;
    protected List<Expr> dims;
    protected int addDims;
    protected ArrayInit init;

//    @Deprecated
    public NewArray_c(Position pos, TypeNode baseType, List<Expr> dims,
            int addDims, ArrayInit init) {
        this(pos, baseType, dims, addDims, init, null);
    }

    public NewArray_c(Position pos, TypeNode baseType, List<Expr> dims,
            int addDims, ArrayInit init, Ext ext) {
        super(pos, ext);
        assert (baseType != null && dims != null); // init may be null
        assert (addDims >= 0);
        assert (!dims.isEmpty() || init != null); // dims may be empty only if there is an initializer
        assert (addDims > 0 || init == null); // init may be non-null only if addDims > 0
        assert (dims.size() + addDims > 0); // must allocate something

        this.baseType = baseType;
        this.dims = ListUtil.copy(dims, true);
        this.addDims = addDims;
        this.init = init;
    }

    @Override
    public TypeNode baseType() {
        return this.baseType;
    }

    @Override
    public NewArray baseType(TypeNode baseType) {
        return baseType(this, baseType);
    }

    protected <N extends NewArray_c> N baseType(N n, TypeNode baseType) {
        if (n.baseType == baseType) return n;
        n = copyIfNeeded(n);
        n.baseType = baseType;
        return n;
    }

    @Override
    public List<Expr> dims() {
        return this.dims;
    }

    @Override
    public NewArray dims(List<Expr> dims) {
        return dims(this, dims);
    }

    protected <N extends NewArray_c> N dims(N n, List<Expr> dims) {
        if (CollectionUtil.equals(n.dims, dims)) return n;
        n = copyIfNeeded(n);
        n.dims = ListUtil.copy(dims, true);
        return n;
    }

    @Override
    public int numDims() {
        return dims.size() + addDims;
    }

    @Override
    public int additionalDims() {
        return this.addDims;
    }

    @Override
    public NewArray additionalDims(int addDims) {
        return additionalDims(this, addDims);
    }

    protected <N extends NewArray_c> N additionalDims(N n, int addDims) {
        if (n.addDims == addDims) return n;
        n = copyIfNeeded(n);
        n.addDims = addDims;
        return n;
    }

    @Override
    public ArrayInit init() {
        return this.init;
    }

    @Override
    public NewArray init(ArrayInit init) {
        return init(this, init);
    }

    protected <N extends NewArray_c> N init(N n, ArrayInit init) {
        if (n.init == init) return n;
        n = copyIfNeeded(n);
        n.init = init;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends NewArray_c> N reconstruct(N n, TypeNode baseType,
            List<Expr> dims, ArrayInit init) {
        n = baseType(n, baseType);
        n = dims(n, dims);
        n = init(n, init);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode baseType = visitChild(this.baseType, v);
        List<Expr> dims = visitList(this.dims, v);
        ArrayInit init = visitChild(this.init, v);
        return reconstruct(this, baseType, dims, init);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        for (Expr expr : dims) {
            if (!ts.isImplicitCastValid(expr.type(), ts.Int())) {
                throw new SemanticException("Array dimension must be an integer.",
                                            expr.position());
            }
        }

        ArrayType type = arrayOf(ts, baseType.type(), dims.size() + addDims);

        if (init != null) {
            init.typeCheckElements(tc, type);
        }

        return type(type);
    }

    protected ArrayType arrayOf(TypeSystem ts, Type baseType, int dims) {
        return ts.arrayOf(baseType, dims);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == init) {
            return this.type;
        }

        if (this.dims() != null && this.dims().contains(child)) {
            return av.typeSystem().Int();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "new " + baseType + "[...]";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("new ");
        print(baseType, w, tr);

        for (Expr e : dims) {
            w.write("[");
            printBlock(e, w, tr);
            w.write("]");
        }

        for (int i = 0; i < addDims; i++) {
            w.write("[]");
        }

        if (init != null) {
            w.write(" ");
            print(init, w, tr);
        }
    }

    @Override
    public Term firstChild() {
        return baseType;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (init != null) {
            v.visitCFG(baseType, listChild(dims, init), ENTRY);
            v.visitCFGList(dims, init, ENTRY);
            v.visitCFG(init, this, EXIT);
        }
        else {
            v.visitCFG(baseType, listChild(dims, (Expr) null), ENTRY);
            v.visitCFGList(dims, this, EXIT);
        }

        return succs;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        if (dims != null && !dims.isEmpty()) {
            // if dimension expressions are given, then
            // a NegativeArraySizeException may be thrown.
            try {
                return CollectionUtil.list(ts.typeForName("java.lang.NegativeArraySizeException"));
            }
            catch (SemanticException e) {
                throw new InternalCompilerError("Cannot find class java.lang.NegativeArraySizeException",
                                                e);
            }
        }
        return Collections.<Type> emptyList();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.NewArray(this.position,
                           this.baseType,
                           this.dims,
                           this.addDims,
                           this.init);
    }

}
