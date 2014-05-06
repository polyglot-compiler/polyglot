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

import java.util.List;

import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

public class ElementValuePair_c extends Term_c implements ElementValuePair {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Id name;
    protected Term value;

    public ElementValuePair_c(Position pos, Id name, Term value) {
        super(pos);
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name.id();
    }

    @Override
    public Id id() {
        return name;
    }

    public ElementValuePair id(Id name) {
        return id(this, name);
    }

    protected <N extends ElementValuePair_c> N id(N n, Id name) {
        ElementValuePair_c ext = n;
        if (ext.name == name) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.name = name;
        return n;
    }

    @Override
    public Term value() {
        return value;
    }

    public ElementValuePair value(Term value) {
        return value(this, value);
    }

    protected <N extends ElementValuePair_c> N value(N n, Term value) {
        ElementValuePair_c ext = n;
        if (ext.value == value) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.value = value;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Term value = visitChild(this.value, v);
        return value(value);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkAnnotationValueConstant(value);
        return this;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(name + "=");
        print(value, w, pp);
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        w.write(name + "=");
        print(value, w, tr);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(value, this, Term.EXIT);
        return succs;
    }

    @Override
    public Term firstChild() {
        return this.value;
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }

}
