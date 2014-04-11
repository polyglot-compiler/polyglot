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
package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ext.jl5.types.reflect.JL5LazyClassInitializer;
import polyglot.ext.param.types.PClass;
import polyglot.frontend.Source;
import polyglot.types.ClassType;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType_c;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.SerialVersionUID;

public class JL5ParsedClassType_c extends ParsedClassType_c implements
        JL5ParsedClassType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected PClass<TypeVariable, ReferenceType> pclass;
    protected List<TypeVariable> typeVars = Collections.emptyList();
    protected List<EnumInstance> enumConstants;
    protected List<AnnotationTypeElemInstance> annotationElems;
    protected boolean annotationsResolved = false;
    protected Annotations annotations;

    public JL5ParsedClassType_c(TypeSystem ts, LazyClassInitializer init,
            Source fromSource) {
        super(ts, init, fromSource);
        this.annotationElems = new LinkedList<>();
        this.enumConstants = new LinkedList<>();
    }

    @Override
    public void addEnumConstant(EnumInstance ei) {
        if (!this.fields.contains(ei)) {
            addField(ei);
        }
        enumConstants.add(ei);
    }

    @Override
    public List<EnumInstance> enumConstants() {
        if (init instanceof JL5LazyClassInitializer) {
            ((JL5LazyClassInitializer) init).initEnumConstants();
        }
        return enumConstants;
    }

    @Override
    public EnumInstance enumConstantNamed(String name) {
        for (EnumInstance ei : enumConstants()) {
            if (ei.name().equals(name)) {
                return ei;
            }
        }
        return null;
    }

    @Override
    public AnnotationTypeElemInstance annotationElemNamed(String name) {
        for (AnnotationTypeElemInstance ai : annotationElems()) {
            if (ai.name().equals(name)) {
                return ai;
            }
        }
        return null;
    }

    @Override
    public void addAnnotationElem(AnnotationTypeElemInstance ai) {
        addMethod(ai);
        annotationElems.add(ai);
    }

    @Override
    public List<AnnotationTypeElemInstance> annotationElems() {
        if (init instanceof JL5LazyClassInitializer) {
            ((JL5LazyClassInitializer) init).initAnnotationElems();
        }
        return Collections.unmodifiableList(annotationElems);
    }

    // find methods with compatible name and formals as the given one
    @Override
    public List<? extends JL5MethodInstance> methods(JL5MethodInstance mi) {
        List<JL5MethodInstance> l = new LinkedList<>();

        for (JL5MethodInstance pi : methodsNamed(mi.name())) {
            if (pi.hasFormals(mi.formalTypes())) {
                l.add(pi);
            }
        }
        return l;
    }

    @Override
    public List<JL5MethodInstance> methodsNamed(String name) {
        @SuppressWarnings("unchecked")
        List<JL5MethodInstance> result =
                (List<JL5MethodInstance>) super.methodsNamed(name);
        return result;
    }

    @Override
    public boolean isEnclosedImpl(ClassType maybe_outer) {
        if (super.isEnclosedImpl(maybe_outer)) {
            return true;
        }
        // try it with the stripped out outer...
        if (outer() != null && super.outer() != this.outer()) {
            return super.outer().equals(maybe_outer)
                    || super.outer().isEnclosed(maybe_outer);
        }
        return false;
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        if (super.isCastValidImpl(toType)) {
            return true;
        }
        return (this.isSubtype(toType) || toType.isSubtype(this));
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        throw new InternalCompilerError("Should not be called in JL5");
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        LinkedList<Type> chain = null;
        if (ts.isSubtype(this, toType)) {
            chain = new LinkedList<>();
            chain.add(this);
            chain.add(toType);
        }
        else if (toType.isPrimitive()) {
            // see if unboxing will let us cast to the primitive
            if (ts.primitiveTypeOfWrapper(this) != null) {
                chain =
                        ts.isImplicitCastValidChain(ts.primitiveTypeOfWrapper(this),
                                                    toType);
                if (chain != null) {
                    chain.addFirst(this);
                }
            }
        }
        return chain;
    }

    // /////////////////////////////////////
    //
    @Override
    public PClass<TypeVariable, ReferenceType> pclass() {
        return pclass;
    }

    @Override
    public void setPClass(PClass<TypeVariable, ReferenceType> pc) {
        this.pclass = pc;
    }

    @Override
    public void setTypeVariables(List<TypeVariable> typeVars) {
        if (typeVars == null) {
            this.typeVars = Collections.emptyList();
        }
        else {
            this.typeVars = ListUtil.copy(typeVars, true);
            // Go through and set the declaring class of the type variables.
            for (TypeVariable tv : typeVars) {
                tv.setDeclaringClass(this);
            }
        }
    }

    @Override
    public List<TypeVariable> typeVariables() {
        if (this.typeVars == null) {
            return Collections.emptyList();
        }
        return this.typeVars;
    }

    @Override
    public JL5Subst erasureSubst() {
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        return ts.erasureSubst(this);
    }

    /** Pretty-print the name of this class to w. */
    @Override
    public void print(CodeWriter w) {
        // XXX This code duplicates the logic of toString.
        this.printNoParams(w);
        if (this.typeVars == null || this.typeVars.isEmpty()) {
            return;
        }
        w.write("<");
        Iterator<TypeVariable> it = this.typeVars.iterator();
        while (it.hasNext()) {
            TypeVariable act = it.next();
            w.write(act.name());
            if (it.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.write(">");
    }

    @Override
    public void printNoParams(CodeWriter w) {
        super.print(w);
    }

    @Override
    public String toStringNoParams() {
        return super.toString();
    }

    @Override
    public String toString() {
        if (this.typeVars == null || this.typeVars.isEmpty()) {
            return super.toString();
        }
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append('<');
        Iterator<TypeVariable> it = this.typeVars.iterator();
        while (it.hasNext()) {
            TypeVariable act = it.next();
            sb.append(act);
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public String translateAsReceiver(Resolver c) {
        // it is a nested class, but a receiver, then use the erased class.
        if (isMember()) {
            JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
            JL5ClassType erased = ((JL5ClassType) ts.erasureType(this));
            if (erased != this) {
                return erased.translateAsReceiver(c);
            }
        }
        return super.translate(c);
    }

    @Override
    public String translate(Resolver c) {
        if (isMember() && flags.isStatic()) {
            ReferenceType container = container();
            if (container instanceof JL5ParsedClassType) {
                // For a static nested class whose enclosing class has a type
                // parameter, the enclosing class is needed as the qualifier.
                JL5ParsedClassType pct = (JL5ParsedClassType) container;
                if (!pct.typeVariables().isEmpty())
                    return container.translate(c) + "." + name();
            }
        }
        // Translate without printing out any parameters.
        return super.translate(c);
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (super.descendsFromImpl(ancestor)) {
            return true;
        }
        if (!this.typeVariables().isEmpty()) {
            // check for raw class
            JL5TypeSystem ts = (JL5TypeSystem) this.ts;
            Type rawClass = ts.rawClass(this, this.position);
            if (ts.isSubtype(rawClass, ancestor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Annotations annotations() {
        if (init instanceof JL5LazyClassInitializer) {
            ((JL5LazyClassInitializer) init).initAnnotations();
        }
        return this.annotations;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

    @Override
    public boolean enumValueOfMethodNeeded() {
        for (MethodInstance mi : this.methods) {
            if (mi.name().equals("valueOf") && mi.formalTypes().size() == 1) {
                Type t = mi.formalTypes().get(0);
                if (ts.String().equals(t)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean enumValuesMethodNeeded() {
        for (MethodInstance mi : this.methods) {
            if (mi.name().equals("values") && mi.formalTypes().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean annotationsResolved() {
        return this.annotationsResolved;
    }

    @Override
    public void setAnnotationsResolved(boolean annotationsResolved) {
        this.annotationsResolved = annotationsResolved;
    }

    @Override
    public Set<Type> superclasses() {
        if (this.superType() == null) {
            return Collections.<Type> emptySet();
        }
        return Collections.singleton(this.superType());
    }

}
