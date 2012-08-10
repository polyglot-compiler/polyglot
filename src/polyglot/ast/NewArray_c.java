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

import java.util.Collections;
import java.util.Iterator;
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
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>NewArray</code> represents a new array expression such as <code>new
 * File[8][] { null }</code>.  It consists of an element type (e.g.,
 * <code>File</code>), a list of dimension expressions (e.g., 8), 0 or more
 * additional dimensions (e.g., 1 for []), and an array initializer.  The
 * dimensions of the array initializer must equal the number of additional "[]"
 * dimensions.
 */
public class NewArray_c extends Expr_c implements NewArray {
    protected TypeNode baseType;
    protected List<Expr> dims;
    protected int addDims;
    protected ArrayInit init;

    public NewArray_c(Position pos, TypeNode baseType, List<Expr> dims,
            int addDims, ArrayInit init) {
        super(pos);
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

    /** Get the base type node of the expression. */
    @Override
    public TypeNode baseType() {
        return this.baseType;
    }

    /** Set the base type node of the expression. */
    @Override
    public NewArray baseType(TypeNode baseType) {
        NewArray_c n = (NewArray_c) copy();
        n.baseType = baseType;
        return n;
    }

    /** Get the dimension expressions of the expression. */
    @Override
    public List<Expr> dims() {
        return Collections.unmodifiableList(this.dims);
    }

    /** Set the dimension expressions of the expression. */
    @Override
    public NewArray dims(List<Expr> dims) {
        NewArray_c n = (NewArray_c) copy();
        n.dims = ListUtil.copy(dims, true);
        return n;
    }

    /** Get the number of dimensions of the expression. */
    @Override
    public int numDims() {
        return dims.size() + addDims;
    }

    /** Get the number of additional dimensions of the expression. */
    @Override
    public int additionalDims() {
        return this.addDims;
    }

    /** Set the number of additional dimensions of the expression. */
    @Override
    public NewArray additionalDims(int addDims) {
        NewArray_c n = (NewArray_c) copy();
        n.addDims = addDims;
        return n;
    }

    /** Get the initializer of the expression. */
    @Override
    public ArrayInit init() {
        return this.init;
    }

    /** Set the initializer of the expression. */
    @Override
    public NewArray init(ArrayInit init) {
        NewArray_c n = (NewArray_c) copy();
        n.init = init;
        return n;
    }

    /** Reconstruct the expression. */
    protected NewArray_c reconstruct(TypeNode baseType, List<Expr> dims,
            ArrayInit init) {
        if (baseType != this.baseType
                || !CollectionUtil.equals(dims, this.dims) || init != this.init) {
            NewArray_c n = (NewArray_c) copy();
            n.baseType = baseType;
            n.dims = ListUtil.copy(dims, true);
            n.init = init;
            return n;
        }

        return this;
    }

    /** Visit the children of the expression. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode baseType = (TypeNode) visitChild(this.baseType, v);
        List<Expr> dims = visitList(this.dims, v);
        ArrayInit init = (ArrayInit) visitChild(this.init, v);
        return reconstruct(baseType, dims, init);
    }

    /** Type check the expression. */
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
            init.typeCheckElements(type);
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

        return child.type();
    }

    @Override
    public String toString() {
        return "new " + baseType + "[...]";
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("new ");
        print(baseType, w, tr);

        for (Iterator<Expr> i = dims.iterator(); i.hasNext();) {
            Expr e = i.next();
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
