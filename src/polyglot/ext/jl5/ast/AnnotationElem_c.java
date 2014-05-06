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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.ClassLit;
import polyglot.ast.Expr;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.AnnotationElementValue;
import polyglot.ext.jl5.types.AnnotationElementValueArray;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class AnnotationElem_c extends Term_c implements AnnotationElem {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode typeName;
    protected List<ElementValuePair> elements;

    public AnnotationElem_c(Position pos, TypeNode typeName,
            List<ElementValuePair> elements) {
        super(pos);
        this.typeName = typeName;
        this.elements = ListUtil.copy(elements, true);
    }

    @Override
    public TypeNode typeName() {
        return typeName;
    }

    @Override
    public AnnotationElem typeName(TypeNode typeName) {
        return typeName(this, typeName);
    }

    protected <N extends AnnotationElem_c> N typeName(N n, TypeNode typeName) {
        AnnotationElem_c ext = n;
        if (ext.typeName == typeName) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.typeName = typeName;
        return n;
    }

    @Override
    public List<ElementValuePair> elements() {
        return this.elements;
    }

    protected <N extends AnnotationElem_c> N elements(N n,
            List<ElementValuePair> elements) {
        AnnotationElem_c ext = n;
        if (CollectionUtil.equals(ext.elements, elements)) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.elements = ListUtil.copy(elements, true);
        return n;
    }

    protected <N extends AnnotationElem_c> N reconstruct(N n,
            TypeNode typeName, List<ElementValuePair> elements) {
        n = typeName(n, typeName);
        n = elements(n, elements);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode tn = visitChild(this.typeName, v);
        List<ElementValuePair> elements = visitList(this.elements, v);
        return reconstruct(this, tn, elements);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Node n = this;
        // only make annotation elements out of annotation types
        if (!typeName.type().isClass()
                || !JL5Flags.isAnnotation(typeName.type().toClass().flags())) {
            throw new SemanticException("Annotation: " + typeName
                    + " must be an annotation type, ", n.position());

        }
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("@");
        print(typeName, w, pp);
        if (this.isMarkerAnnotation()) {
            // marker annotation, so no values to print out.
            return;
        }
        w.write("(");

        // Single-element annotation named "value": special case
        if (this.isSingleElementAnnotation()) {
            ElementValuePair p = elements().get(0);
            print(p.value(), w, pp);
        }
        else {

            for (Iterator<ElementValuePair> it = elements().iterator(); it.hasNext();) {
                print(it.next(), w, pp);
                if (it.hasNext()) {
                    w.write(", ");
                }
            }
        }
        w.write(") ");
    }

    public Term entry() {
        return this;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public String toString() {
        return "Annotation Type: " + typeName();
    }

    @Override
    public Term firstChild() {
        return typeName;
    }

    @Override
    public boolean isMarkerAnnotation() {
        return elements().isEmpty();
    }

    @Override
    public boolean isSingleElementAnnotation() {
        return elements().size() == 1
                && elements().get(0).name().equals("value");
    }

    @Override
    public Map<String, AnnotationElementValue> toAnnotationElementValues(
            Lang lang, JL5TypeSystem ts) throws SemanticException {
        Map<String, AnnotationElementValue> m = new LinkedHashMap<>();
        for (ElementValuePair p : this.elements()) {
            List<? extends MethodInstance> methods =
                    this.typeName().type().toClass().methodsNamed(p.name());
            if (methods.size() != 1) {
                throw new InternalCompilerError("Annotation has more than one method named \""
                        + p.name() + "\": " + methods);
            }
            MethodInstance mi = methods.get(0);
            Type intendedType = mi.returnType();

            AnnotationElementValue v =
                    toAnnotationElementValue(lang, p.value(), intendedType, ts);

            if (intendedType.isArray()
                    && !(v instanceof AnnotationElementValueArray)) {
                // it's actually meant to be an array type, but a singleton was entered
                v =
                        ts.AnnotationElementValueArray(p.value().position(),
                                                       Collections.singletonList(v));
            }

            m.put(p.name(), v);
        }
        return m;
    }

    private AnnotationElementValue toAnnotationElementValue(Lang lang,
            Term value, Type intendedType, JL5TypeSystem ts)
            throws SemanticException {
        Type intendedBaseType;
        if (intendedType.isArray()) {
            intendedBaseType = intendedType.toArray().base();
        }
        else {
            intendedBaseType = intendedType;
        }

        if (value instanceof ElementValueArrayInit) {
            if (!intendedType.isArray()) {
                throw new SemanticException("Array given when expected type is "
                                                    + intendedType.toString(),
                                            value.position());
            }
            ElementValueArrayInit init = (ElementValueArrayInit) value;
            List<AnnotationElementValue> vals = new ArrayList<>();
            for (Term v : init.elements()) {
                vals.add(toAnnotationElementValue(lang, v, intendedBaseType, ts));
            }
            return ts.AnnotationElementValueArray(value.position(), vals);
        }
        if (value instanceof AnnotationElem) {
            AnnotationElem ae = (AnnotationElem) value;
            Type aeType = ae.typeName().type();
            // Check against intended type.
            if (aeType.isCanonical()
                    && !ts.isImplicitCastValid(aeType, intendedBaseType)) {
                throw new SemanticException("Expected a value of type "
                        + intendedBaseType, value.position());
            }
            return ts.AnnotationElementValueAnnotation(value.position(),
                                                       aeType,
                                                       ae.toAnnotationElementValues(lang,
                                                                                    ts));
        }
        // Otherwise, it should be a constant value.
        if (!(value instanceof Expr)) {
            throw new InternalCompilerError("Unexpected node: " + value + " : "
                    + value.getClass(), value.position());
        }
        Expr ev = (Expr) value;
        ts.checkAnnotationValueConstant(ev);
        Object constVal = lang.constantValue(ev, lang);
        if (value instanceof ClassLit) {
            constVal = ((ClassLit) value).typeNode().type();
        }
        // Check against intended type
        if (ev.type().isCanonical()
                && !ts.isImplicitCastValid(ev.type(), intendedBaseType)) {
            throw new SemanticException("Expected a value of type "
                    + intendedBaseType, value.position());
        }
        AnnotationElementValue c =
                ts.AnnotationElementValueConstant(value.position(),
                                                  intendedBaseType,
                                                  constVal);

        return c;
    }
}
