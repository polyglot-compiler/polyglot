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
package polyglot.ext.jl8.ast;

import java.util.List;
import polyglot.ast.Assign;
import polyglot.ast.Cast;
import polyglot.ast.Expr_c;
import polyglot.ast.Ext;
import polyglot.ast.FieldDecl;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Precedence;
import polyglot.ast.Return;
import polyglot.ast.Term;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.CodeInstance;
import polyglot.types.FunctionInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

public class FunctionValue_c extends Expr_c implements FunctionValue {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected FunctionSpec functionSpec;

    //    @Deprecated
    FunctionValue_c(FunctionSpec functionSpec) {
        this(functionSpec, null);
    }

    FunctionValue_c(FunctionSpec functionSpec, Ext ext) {
        super(functionSpec.position(), ext);
        this.functionSpec = functionSpec;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LAMBDA;
    }

    @Override
    public FunctionSpec functionSpec() {
        return this.functionSpec;
    }

    @Override
    public FunctionValue functionSpec(FunctionSpec functionSpec) {
        return reconstruct(this, functionSpec);
    }

    protected <N extends FunctionValue_c> N reconstruct(N n, FunctionSpec functionSpec) {
        if (n.functionSpec == functionSpec) return n;
        n = copyIfNeeded(n);
        n.functionSpec = functionSpec;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        FunctionSpec declaration = visitChild(this.functionSpec, v);
        return reconstruct(this, declaration);
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc) throws SemanticException {
        if (parent instanceof Return) {
            CodeInstance ci = tc.context().currentCode();
            if (ci instanceof FunctionInstance) {
                Type type = ((FunctionInstance) ci).returnType();
                if (!type.isCanonical()) return this;
                setTargetType(type, tc);
            }
        }
        if (parent instanceof Assign) {
            Assign assign = (Assign) parent;
            Type type = assign.left().type();
            if (type == null || !type.isCanonical()) return this;
            setTargetType(type, tc);
        }
        if (parent instanceof LocalDecl) {
            LocalDecl localDecl = (LocalDecl) parent;
            Type type = localDecl.declType();
            if (!type.isCanonical()) return this;
            setTargetType(type, tc);
        }
        if (parent instanceof FieldDecl) {
            FieldDecl fieldDecl = (FieldDecl) parent;
            Type type = fieldDecl.type().type();
            if (type == null || !type.isCanonical()) return this;
            setTargetType(type, tc);
        }
        if (parent instanceof Cast) {
            Cast cast = (Cast) parent;
            Type type = cast.castType().type();
            if (!type.isCanonical()) return this;
            setTargetType(type, tc);
        }
        return super.typeCheckOverride(parent, tc);
    }

    @Override
    public void setTargetType(Type type, TypeChecker tc) throws SemanticException {
        this.functionSpec =
                this.functionSpec.withTargetType(
                        type,
                        (JL8TypeSystem) tc.context().typeSystem(),
                        tc.nodeFactory(),
                        tc.context().currentClass());
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(this.functionSpec.targetType());
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        lang().translate(this.functionSpec.equivalentNewCode(tr.nodeFactory()), w, tr);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (pp instanceof Translator) {
            this.translate(w, (Translator) pp);
            return;
        }
        this.functionSpec.prettyPrint(w, pp);
    }

    @Override
    public String toString() {
        return this.functionSpec.toString();
    }

    @Override
    public Term firstChild() {
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        JL8NodeFactory jl8NodeFactory = (JL8NodeFactory) nf;
        return jl8NodeFactory.FunctionValue(this.functionSpec);
    }
}
